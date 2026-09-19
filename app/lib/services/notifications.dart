import 'dart:convert';
import 'dart:ui';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_timezone/flutter_timezone.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

import '../data/database.dart';
import '../domain/access.dart';
import '../domain/models.dart';
import '../domain/mood_engine.dart';
import '../domain/nudge_plan.dart';
import '../domain/persona_catalog.dart';

/// Port the UI isolate listens on so background notification actions can ask it to reload.
const uiRefreshPortName = 'nagly_ui_refresh';

abstract final class NudgeAction {
  static const add250 = 'ADD_250';
  static const add500 = 'ADD_500';
  static const skip = 'SKIP';
  static const medTaken = 'MED_TAKEN';
  static const medSnooze = 'MED_SNOOZE';
  static const medSkip = 'MED_SKIP';
}

abstract final class _Ids {
  static const waterBase = 1; // 1..8
  static const trialEnding = 900;
  static const comeback = 901;
  static int med(int medId, int dayOffset) =>
      10000 + medId * 10 + dayOffset; // offsets 0..2
  static int medSnooze(int medId) => 10000 + medId * 10 + 9;
}

const _waterChannel = AndroidNotificationChannel(
  'nagly_nudges',
  'Water nudges',
  description: 'Reminders to drink water, in your nagger\'s voice',
  importance: Importance.defaultImportance,
);
const _medChannel = AndroidNotificationChannel(
  'nagly_meds',
  'Medication reminders',
  description: 'Reminders to take your medication',
  importance: Importance.high,
);
const _careChannel = AndroidNotificationChannel(
  'nagly_care',
  'Check-ins',
  description:
      'Occasional check-ins when you\'ve gone quiet or your trial is ending',
  importance: Importance.defaultImportance,
);

@pragma('vm:entry-point')
Future<void> notificationActionBackground(NotificationResponse response) async {
  WidgetsFlutterBinding.ensureInitialized();
  DartPluginRegistrant.ensureInitialized();
  final db = await NaglyDatabase.open();
  final service = NotificationService();
  await service.init();
  await service.handleResponse(db, response);
  IsolateNameServer.lookupPortByName(uiRefreshPortName)?.send('refresh');
}

class NotificationService {
  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();
  static bool _tzReady = false;

  static Future<void> _initTimezones() async {
    if (_tzReady) return;
    tzdata.initializeTimeZones();
    try {
      final info = await FlutterTimezone.getLocalTimezone();
      tz.setLocalLocation(tz.getLocation(info.identifier));
    } catch (e) {
      debugPrint('Timezone lookup failed, using UTC: $e');
    }
    _tzReady = true;
  }

  Future<void> init({void Function(NotificationResponse)? onResponse}) async {
    await _initTimezones();
    await _plugin.initialize(
      settings: const InitializationSettings(
        android: AndroidInitializationSettings('ic_stat_nagly'),
        iOS: DarwinInitializationSettings(
          requestAlertPermission: false,
          requestBadgePermission: false,
          requestSoundPermission: false,
        ),
      ),
      onDidReceiveNotificationResponse: onResponse,
      onDidReceiveBackgroundNotificationResponse: notificationActionBackground,
    );
    final android = _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >();
    for (final c in const [_waterChannel, _medChannel, _careChannel]) {
      await android?.createNotificationChannel(c);
    }
  }

  Future<bool> requestPermission() async {
    final android = _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >();
    if (android != null)
      return await android.requestNotificationsPermission() ?? false;
    final ios = _plugin
        .resolvePlatformSpecificImplementation<
          IOSFlutterLocalNotificationsPlugin
        >();
    return await ios?.requestPermissions(
          alert: true,
          sound: true,
          badge: false,
        ) ??
        false;
  }

  Future<bool> permissionGranted() async {
    final android = _plugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >();
    return await android?.areNotificationsEnabled() ?? true;
  }

  /// Apply a notification action (from lock screen or foreground) to the database,
  /// then re-plan every reminder.
  Future<void> handleResponse(
    NaglyDatabase db,
    NotificationResponse response,
  ) async {
    final action = response.actionId;
    final payload = response.payload ?? '';
    switch (action) {
      case NudgeAction.add250:
        await db.addDrink(250);
      case NudgeAction.add500:
        await db.addDrink(500);
      case NudgeAction.skip:
        break; // The fired nudge already counts as ignored; re-planning escalates the tone.
      case NudgeAction.medTaken || NudgeAction.medSkip || NudgeAction.medSnooze:
        final parts = payload.split(':'); // med:<id>:<dateKey>
        if (parts.length != 3) break;
        final medId = int.tryParse(parts[1]);
        if (medId == null) break;
        if (action == NudgeAction.medSnooze) {
          final snoozes = await _snoozes(db);
          snoozes['$medId'] = DateTime.now()
              .add(const Duration(minutes: 30))
              .millisecondsSinceEpoch;
          await db.setString(_snoozeKey, jsonEncode(snoozes));
        } else {
          await db.logMed(
            medId,
            parts[2],
            action == NudgeAction.medTaken
                ? MedStatus.taken
                : MedStatus.skipped,
          );
        }
    }
    if (response.id != null) await _plugin.cancel(id: response.id!);
    await sync(db);
  }

  static const _snoozeKey = 'med_snoozes';

  Future<Map<String, int>> _snoozes(NaglyDatabase db) async {
    final raw = await db.getString(_snoozeKey);
    if (raw == null) return {};
    return (jsonDecode(raw) as Map).map(
      (k, v) => MapEntry(k as String, v as int),
    );
  }

  /// Rebuild every pending reminder from what's in the database.
  /// Safe to call from the UI or a background isolate.
  Future<void> sync(NaglyDatabase db, {DateTime? now}) async {
    await _initTimezones();
    final at = now ?? DateTime.now();
    final nowMs = at.millisecondsSinceEpoch;
    await _plugin.cancelAllPendingNotifications();

    final profile = await db.profile();
    final enabled = await db.getBool(Keys.notificationsEnabled, fallback: true);
    if (!profile.onboarded || !enabled) return;

    final access = Access(
      isPro: await db.getBool(Keys.isPro),
      trialEndsAtMs: await db.getInt(Keys.trialEndsAt),
      unlocks: await db.activeUnlocks(nowMs),
      nowMs: nowMs,
    );
    final persona = PersonaCatalog.get(profile.personaId);
    final dayStart = dateOnly(at);
    final today = await db.drinksBetween(
      dayStart.millisecondsSinceEpoch,
      dayStart.add(const Duration(days: 1)).millisecondsSinceEpoch,
    );
    final consumed = today.fold<int>(0, (s, l) => s + l.amountMl);

    // ── Water ──
    final history = (await db.nudgeHistory())
        .where((t) => t <= nowMs && t >= dayStart.millisecondsSinceEpoch)
        .toList();
    final ignored = ignoredNudgeCount(
      nudgeHistory: history,
      nowMs: nowMs,
      lastLogMs: today.isEmpty ? null : today.last.timestampMs,
    );
    final nudges = planWaterNudges(
      nowMs: nowMs,
      profile: profile,
      consumedMl: consumed,
      ignoredSoFar: ignored,
    );
    for (var i = 0; i < nudges.length; i++) {
      final n = nudges[i];
      await _schedule(
        id: _Ids.waterBase + i,
        at: DateTime.fromMillisecondsSinceEpoch(n.atMs),
        title: n.title,
        body: n.body,
        channel: _waterChannel,
        payload: 'water',
        actions: [
          const AndroidNotificationAction(NudgeAction.add250, '+250 ml'),
          const AndroidNotificationAction(NudgeAction.add500, '+500 ml'),
          AndroidNotificationAction(NudgeAction.skip, n.skipLabel),
        ],
      );
    }
    await db.setNudgeHistory([...history, ...nudges.map((n) => n.atMs)]);

    // ── Medication ──
    if (profile.careMode == CareMode.medication) {
      final meds = access.activeMedications(await db.medications());
      final logs = await db.medLogsSince(dateKey(dayStart));
      final snoozes = await _snoozes(db);
      for (final med in meds) {
        final logged = logs
            .where((l) => l.medId == med.id)
            .map((l) => l.dateKey)
            .toSet();
        final times = nextMedOccurrences(
          med: med,
          now: at,
          loggedDateKeys: logged,
        );
        for (var i = 0; i < times.length; i++) {
          await _scheduleMed(med, persona, times[i], _Ids.med(med.id, i), i);
        }
        final snoozeAt = snoozes['${med.id}'];
        if (snoozeAt != null &&
            snoozeAt > nowMs &&
            !logged.contains(dateKey(at))) {
          await _scheduleMed(
            med,
            persona,
            DateTime.fromMillisecondsSinceEpoch(snoozeAt),
            _Ids.medSnooze(med.id),
            7,
          );
        }
      }
    }

    // ── Trial ending (in her voice) ──
    if (access.inTrial) {
      final end = DateTime.fromMillisecondsSinceEpoch(access.trialEndsAtMs!);
      var when = DateTime(end.year, end.month, end.day, 10);
      if (!when.isBefore(end)) when = when.subtract(const Duration(days: 1));
      if (when.isAfter(at)) {
        await _schedule(
          id: _Ids.trialEnding,
          at: when,
          title: '${persona.emoji} ${persona.displayName}',
          body: "My full care plan ends today. Keep me around? I'll still do water for free.",
          channel: _careChannel,
          payload: 'trial',
        );
      }
    }

    // ── Gone quiet: a local win-back two days after the last sip ──
    final lastLog = today.isNotEmpty ? today.last.timestampMs : null;
    final base = DateTime.fromMillisecondsSinceEpoch(lastLog ?? nowMs);
    final comebackAt = DateTime(
      base.year,
      base.month,
      base.day + 2,
      (profile.wakeHour + 4).clamp(0, 23),
    );
    if (comebackAt.isAfter(at)) {
      await _schedule(
        id: _Ids.comeback,
        at: comebackAt,
        title: '${persona.emoji} ${persona.displayName}',
        body: pickFrom(persona.comeback),
        channel: _careChannel,
        payload: 'comeback',
      );
    }
  }

  /// Demo/QA: fire a real water nudge (with its action buttons) a few seconds from now.
  Future<void> sendTestNudge(
    NaglyDatabase db, {
    Duration after = const Duration(seconds: 5),
  }) async {
    final profile = await db.profile();
    final at = DateTime.now().add(after);
    final plan = planWaterNudges(
      nowMs: at.millisecondsSinceEpoch - minNudgeIntervalMs,
      profile: profile.copyWith(wakeHour: 0, sleepHour: 23),
      consumedMl: 0,
      ignoredSoFar: 1,
    );
    if (plan.isEmpty) return;
    final n = plan.first;
    await _schedule(
      id: 99,
      at: at,
      title: n.title,
      body: n.body,
      channel: _waterChannel,
      payload: 'water',
      actions: [
        const AndroidNotificationAction(NudgeAction.add250, '+250 ml'),
        const AndroidNotificationAction(NudgeAction.add500, '+500 ml'),
        AndroidNotificationAction(NudgeAction.skip, n.skipLabel),
      ],
    );
  }

  Future<void> _scheduleMed(
    Medication med,
    Persona persona,
    DateTime at,
    int id,
    int seed,
  ) {
    final name = med.dose.isEmpty ? med.name : '${med.name} (${med.dose})';
    return _schedule(
      id: id,
      at: at,
      title: '${persona.emoji} ${persona.displayName}',
      body: PersonaCatalog.fillMed(
        persona.medDue[(med.id + seed) % persona.medDue.length],
        name,
      ),
      channel: _medChannel,
      payload: 'med:${med.id}:${dateKey(at)}',
      actions: const [
        AndroidNotificationAction(NudgeAction.medTaken, 'Took it 💊'),
        AndroidNotificationAction(NudgeAction.medSnooze, 'Snooze 30m'),
        AndroidNotificationAction(NudgeAction.medSkip, 'Not today'),
      ],
    );
  }

  Future<void> _schedule({
    required int id,
    required DateTime at,
    required String title,
    required String body,
    required AndroidNotificationChannel channel,
    required String payload,
    List<AndroidNotificationAction> actions = const [],
  }) async {
    try {
      await _plugin.zonedSchedule(
        id: id,
        scheduledDate: tz.TZDateTime.from(at, tz.local),
        title: title,
        body: body,
        payload: payload,
        // Inexact is fine for a nag, and avoids the restricted exact-alarm permission.
        androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
        notificationDetails: NotificationDetails(
          android: AndroidNotificationDetails(
            channel.id,
            channel.name,
            channelDescription: channel.description,
            importance: channel.importance,
            priority: channel.importance == Importance.high
                ? Priority.high
                : Priority.defaultPriority,
            color: const Color(0xFF0E7C86),
            styleInformation: BigTextStyleInformation(body),
            category: AndroidNotificationCategory.reminder,
            actions: actions,
          ),
          iOS: const DarwinNotificationDetails(),
        ),
      );
    } catch (e) {
      debugPrint('Failed to schedule notification $id: $e');
    }
  }
}
