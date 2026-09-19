import 'dart:async';
import 'dart:isolate';
import 'dart:math';
import 'dart:ui';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

import '../config/integrations.dart';
import '../data/database.dart';
import '../domain/access.dart';
import '../domain/history_chat.dart';
import '../domain/insights.dart';
import '../domain/models.dart';
import '../domain/mood_engine.dart';
import '../domain/nudge_plan.dart';
import '../domain/persona_catalog.dart';
import '../domain/push_tags.dart';
import '../domain/relationship_meter.dart';
import '../services/ads.dart';
import '../services/billing.dart';
import '../services/notifications.dart';
import '../services/push.dart';

/// One-shot things the UI shell should present (dialogs, sheets, navigation).
sealed class AppEvent {
  const AppEvent();
}

class PersonaFallbackEvent extends AppEvent {
  const PersonaFallbackEvent(this.message);
  final String message;
}

class TrialEndingEvent extends AppEvent {
  const TrialEndingEvent();
}

class TrialEndedEvent extends AppEvent {
  const TrialEndedEvent({this.departedPersona});

  /// Name of the Pro persona that had to leave, if any.
  final String? departedPersona;
}

class UpsellEvent extends AppEvent {
  const UpsellEvent(this.streak);
  final int streak;
}

class GoalReachedEvent extends AppEvent {
  const GoalReachedEvent();
}

class RouteEvent extends AppEvent {
  const RouteEvent(this.route);
  final PushRoute route;
}

class AppController extends ChangeNotifier with WidgetsBindingObserver {
  AppController({
    required this.db,
    required this.billing,
    required this.ads,
    required this.push,
    required this.notifications,
  });

  final NaglyDatabase db;
  final BillingService billing;
  final AdService ads;
  final PushService push;
  final NotificationService notifications;

  final _events = StreamController<AppEvent>.broadcast();
  final _pendingEvents = <AppEvent>[];

  /// UI events. Anything emitted while nobody is listening (e.g. right as onboarding
  /// finishes, before the shell mounts) is held until [flushPendingEvents].
  Stream<AppEvent> get events => _events.stream;

  /// Call right after subscribing to [events] to receive anything that was held.
  void flushPendingEvents() {
    final pending = List<AppEvent>.of(_pendingEvents);
    _pendingEvents.clear();
    pending.forEach(_events.add);
  }

  void _emit(AppEvent e) =>
      _events.hasListener ? _events.add(e) : _pendingEvents.add(e);

  // ── Raw state ─────────────────────────────────────────────
  Profile profile = const Profile();
  List<DrinkLog> todayLogs = const [];
  List<DrinkLog> recentLogs = const []; // last 60 days, including today
  List<Medication> meds = const [];
  Map<int, Medication> allMedsById = const {};
  List<MedLog> recentMedLogs = const []; // last 14 days
  Map<String, int> unlocks = const {};
  int? trialEndsAtMs;
  bool notificationsEnabled = true;
  bool permissionGranted = true;
  List<int> nudgeHistory = const [];
  bool loaded = false;
  DateTime now = DateTime.now();

  String? _line;
  Mood? _lineMood;
  Timer? _ticker;
  Timer? _syncDebounce;
  ReceivePort? _refreshPort;
  DateTime _loadedDay = dateOnly(DateTime.now());

  // ── Lifecycle ─────────────────────────────────────────────
  Future<void> init() async {
    await billing.init();
    billing.isPro.addListener(_onProChanged);
    await reload();
    WidgetsBinding.instance.addObserver(this);
    _ticker = Timer.periodic(const Duration(minutes: 1), (_) => _tick());

    // Background notification actions ping us so the UI reflects them immediately.
    _refreshPort = ReceivePort();
    IsolateNameServer.removePortNameMapping(uiRefreshPortName);
    IsolateNameServer.registerPortWithName(
      _refreshPort!.sendPort,
      uiRefreshPortName,
    );
    _refreshPort!.listen((_) => reload());

    final installId = await _installId();
    unawaited(
      push
          .init(
            installId,
            onRoute: (r) => _emit(RouteEvent(r)),
            onAction: _onPushAction,
          )
          .catchError((Object e) => debugPrint('Push init failed: $e')),
    );
    unawaited(
      ads.init().catchError((Object e) => debugPrint('Ads init failed: $e')),
    );
    permissionGranted = await notifications.permissionGranted();
    _scheduleSync();
  }

  Future<String> _installId() async {
    final existing = await db.getString(Keys.installId);
    if (existing != null) return existing;
    final r = Random.secure();
    final id = List.generate(
      16,
      (_) => r.nextInt(256).toRadixString(16).padLeft(2, '0'),
    ).join();
    await db.setString(Keys.installId, id);
    return id;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      notifications.permissionGranted().then((g) {
        permissionGranted = g;
        notifyListeners();
      });
      reload().then((_) => _scheduleSync());
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    billing.isPro.removeListener(_onProChanged);
    _ticker?.cancel();
    _syncDebounce?.cancel();
    _refreshPort?.close();
    IsolateNameServer.removePortNameMapping(uiRefreshPortName);
    _events.close();
    super.dispose();
  }

  void _onProChanged() {
    db.setBool(Keys.isPro, billing.isPro.value);
    notifyListeners();
    _scheduleSync();
  }

  void _tick() {
    now = DateTime.now();
    if (dateOnly(now) != _loadedDay) {
      reload().then((_) => _scheduleSync());
      return;
    }
    _checkAccessEvents();
    notifyListeners();
  }

  Future<void> reload() async {
    now = DateTime.now();
    final today = dateOnly(now);
    _loadedDay = today;
    final tomorrow = today.add(const Duration(days: 1));
    profile = await db.profile();
    recentLogs = await db.drinksBetween(
      today.subtract(const Duration(days: 59)).millisecondsSinceEpoch,
      tomorrow.millisecondsSinceEpoch,
    );
    todayLogs = recentLogs
        .where((l) => l.timestampMs >= today.millisecondsSinceEpoch)
        .toList();
    meds = await db.medications();
    allMedsById = await db.allMedicationsById();
    recentMedLogs = await db.medLogsSince(
      dateKey(today.subtract(const Duration(days: 13))),
    );
    unlocks = await db.activeUnlocks(now.millisecondsSinceEpoch);
    trialEndsAtMs = await db.getInt(Keys.trialEndsAt);
    notificationsEnabled = await db.getBool(
      Keys.notificationsEnabled,
      fallback: true,
    );
    nudgeHistory = await db.nudgeHistory();
    loaded = true;
    _checkAccessEvents();
    notifyListeners();
  }

  /// Re-plan reminders and sync push tags shortly after changes settle.
  void _scheduleSync() {
    _syncDebounce?.cancel();
    _syncDebounce = Timer(const Duration(milliseconds: 400), () async {
      await notifications.sync(db);
      nudgeHistory = await db.nudgeHistory();
      if (!profile.onboarded) return;
      final tags = computePushTags(
        profile: profile,
        recentLogs: recentLogs,
        access: access,
        now: now,
      );
      unawaited(
        push
            .setTags(tags)
            .catchError((Object e) => debugPrint('Push tags failed: $e')),
      );
      unawaited(
        push
            .setTriggers({
              'streak': '$streak',
              'trial_days_left': '${access.trialDaysLeft}',
              'is_pro': '${access.isPro}',
            })
            .catchError((Object e) => debugPrint('Push triggers failed: $e')),
      );
      unawaited(
        billing.setAttributes({
          'persona_id': profile.personaId,
          'care_mode': profile.careMode.name,
          'current_streak': '$streak',
          'bond_level': bondLevel.name,
        }),
      );
    });
  }

  Future<void> _changed() async {
    await reload();
    _scheduleSync();
  }

  // ── Derived state ─────────────────────────────────────────
  Persona get persona => PersonaCatalog.get(profile.personaId);

  Access get access => Access(
    isPro: billing.isPro.value,
    trialEndsAtMs: trialEndsAtMs,
    unlocks: unlocks,
    nowMs: now.millisecondsSinceEpoch,
  );

  int get consumedMl => todayLogs.fold(0, (s, l) => s + l.amountMl);

  double get progress =>
      profile.dailyMl <= 0 ? 0 : consumedMl / profile.dailyMl;

  double get expected =>
      expectedRatio(now.hour, profile.wakeHour, profile.sleepHour);

  int get ignoredCount => ignoredNudgeCount(
    nudgeHistory: nudgeHistory,
    nowMs: now.millisecondsSinceEpoch,
    lastLogMs: todayLogs.isEmpty ? null : todayLogs.last.timestampMs,
  );

  Mood get mood => computeMood(
    progressRatio: progress,
    expectedRatio: expected,
    ignoredNudgeCount: ignoredCount,
  );

  DayPart get dayPart =>
      dayPartFor(now.hour, profile.wakeHour, profile.sleepHour);

  /// The persona's current line. Stable until the mood changes or the user taps for another.
  String get line {
    final m = mood;
    if (_line == null ||
        _lineMood != m ||
        !PersonaCatalog.linesFor(persona, m, dayPart).contains(_line)) {
      _line = pickLine(persona, m, dayPart: dayPart, previousLine: _line);
      _lineMood = m;
    }
    return _line!;
  }

  void cycleLine() {
    _line = pickLine(persona, mood, dayPart: dayPart, previousLine: _line);
    notifyListeners();
  }

  int get behindMl => max(0, (expected * profile.dailyMl).round() - consumedMl);

  Map<DateTime, int> get _byDay => totalsByDay(recentLogs);

  int get streak => currentStreak(_byDay, profile.dailyMl, now);

  int get daysMetIn14 => countDaysMetGoalInWindow(_byDay, profile.dailyMl, now);

  RelationshipLevel get bondLevel =>
      computeRelationshipLevel(streak, daysMetIn14);

  double get bondProgress =>
      relationshipProgressToNext(bondLevel, streak, daysMetIn14);

  String get nextNudgeLabel {
    if (!notificationsEnabled) return 'Reminders are off';
    if (consumedMl >= profile.dailyMl) return 'Goal met — no more nudges today';
    final next = nextNudgeTimes(
      nowMs: now.millisecondsSinceEpoch,
      profile: profile,
      consumedMl: consumedMl,
    );
    if (next.isEmpty) return 'Next nudge tomorrow morning';
    final mins = ((next.first - now.millisecondsSinceEpoch) / 60000).round();
    return mins >= 60
        ? 'Next nudge in ${mins ~/ 60}h ${mins % 60}m'
        : 'Next nudge in ${mins}m';
  }

  int? get recentCustomMl => recentLogs.reversed
      .map((l) => l.amountMl)
      .where((ml) => ml != 250 && ml != 500)
      .firstOrNull;

  String get todayKey => dateKey(now);

  MedLog? medStatusToday(Medication med) => recentMedLogs
      .where((l) => l.medId == med.id && l.dateKey == todayKey)
      .firstOrNull;

  List<Medication> get activeMeds => access.activeMedications(meds);

  bool isMedPaused(Medication med) => !activeMeds.any((m) => m.id == med.id);

  WeeklyInsights get insights => computeWeeklyInsights(
    logs: recentLogs
        .where(
          (l) =>
              l.timestampMs >=
              dateOnly(now)
                  .subtract(const Duration(days: 13))
                  .millisecondsSinceEpoch,
        )
        .toList(),
    dailyMl: profile.dailyMl,
    now: now,
  );

  /// Doses taken vs due over the last 7 days (only counting days since each med was added).
  ({int taken, int due}) get weeklyMedAdherence {
    final today = dateOnly(now);
    var taken = 0, due = 0;
    for (var offset = 0; offset < 7; offset++) {
      final d = today.subtract(Duration(days: offset));
      final key = dateKey(d);
      for (final med in activeMeds) {
        final isFuture =
            offset == 0 &&
            DateTime(d.year, d.month, d.day, med.hour, med.minute).isAfter(now);
        final log = recentMedLogs
            .where((l) => l.medId == med.id && l.dateKey == key)
            .firstOrNull;
        if (isFuture && log == null) continue;
        if (log == null &&
            offset > 0 &&
            !recentMedLogs.any((l) => l.medId == med.id))
          continue;
        due++;
        if (log?.status == MedStatus.taken) taken++;
      }
    }
    return (taken: taken, due: due);
  }

  List<ChatItem> get chatTimeline => buildChatTimeline(
    logs: recentLogs
        .where(
          (l) =>
              l.timestampMs >=
              dateOnly(now)
                  .subtract(const Duration(days: 13))
                  .millisecondsSinceEpoch,
        )
        .toList(),
    medLogs: recentMedLogs,
    medsById: allMedsById,
    profile: profile,
    persona: persona,
  );

  /// A plain-text weekly summary for sharing with family (no data leaves the device otherwise).
  String get weeklyReport {
    final w = insights;
    final adherence = weeklyMedAdherence;
    final b = StringBuffer('My week with Nagly ${persona.emoji}\n')
      ..writeln(
        '💧 Water goal met ${w.goalMetDays}/7 days · avg ${formatVolume(w.dailyAverageMl, profile.volumeUnit)}/day',
      )
      ..writeln(
        '🔥 Current streak: ${w.currentStreak} day${w.currentStreak == 1 ? '' : 's'}',
      );
    if (profile.careMode == CareMode.medication && adherence.due > 0) {
      b.writeln(
        '💊 Medication taken ${adherence.taken}/${adherence.due} doses',
      );
    }
    b.writeln(
      '${bondLevel.emoji} Bond with ${persona.displayName}: ${bondLevel.label}',
    );
    return b.toString();
  }

  // ── Access events ─────────────────────────────────────────
  bool _checkingAccess = false;
  String? _departedPersona;

  Future<void> _checkAccessEvents() async {
    if (!loaded ||
        !profile.onboarded ||
        trialEndsAtMs == null ||
        _checkingAccess)
      return;
    _checkingAccess = true;
    try {
      final a = access;
      final trialJustEnded =
          a.trialEnded && !await db.getBool(Keys.trialEndedShown);
      final fallback = resolvePersonaFallback(profile, a);
      if (fallback != null) {
        final departed = persona.displayName;
        profile = fallback.profile;
        await db.saveProfile(profile);
        // When the trial ends, one sheet explains everything; otherwise (an ad unlock
        // lapsing) a small dialog does.
        if (!trialJustEnded) _emit(PersonaFallbackEvent(fallback.message));
        _departedPersona = departed;
        _scheduleSync();
      }
      if (a.trialEndsWithin24h &&
          await db.getString(Keys.trialSheetShownOn) != todayKey) {
        await db.setString(Keys.trialSheetShownOn, todayKey);
        _emit(const TrialEndingEvent());
      }
      if (trialJustEnded) {
        await db.setBool(Keys.trialEndedShown, true);
        _emit(TrialEndedEvent(departedPersona: _departedPersona));
        _scheduleSync();
      }
      // An engaged free user gets one gentle offer a week.
      if (Integrations.purchasesEnabled && !a.fullAccess && streak >= 3) {
        final last = await db.getInt(Keys.upsellShownAt) ?? 0;
        if (now.millisecondsSinceEpoch - last > 7 * 24 * 60 * 60 * 1000) {
          await db.setInt(Keys.upsellShownAt, now.millisecondsSinceEpoch);
          _emit(UpsellEvent(streak));
        }
      }
    } finally {
      _checkingAccess = false;
    }
  }

  // ── Actions ───────────────────────────────────────────────
  /// Action buttons on cloud pushes (e.g. a win-back with "+250 ml").
  void _onPushAction(String actionId) {
    switch (actionId) {
      case PushActionIds.add250:
        logDrink(250);
      case PushActionIds.add500:
        logDrink(500);
    }
  }

  void _outcome(String name, {double? value, bool unique = false}) => unawaited(
    push
        .outcome(name, value: value, unique: unique)
        .catchError((Object e) => debugPrint('Push outcome failed: $e')),
  );

  Future<void> logDrink(int ml) async {
    final before = consumedMl;
    await db.addDrink(ml);
    await _changed();
    // OneSignal attributes these to the push that brought the user here, so the
    // dashboard shows which messages actually make people drink.
    _outcome(PushOutcomes.waterLogged);
    _outcome(PushOutcomes.waterMl, value: ml.toDouble());
    if (before < profile.dailyMl && consumedMl >= profile.dailyMl) {
      _outcome(PushOutcomes.goalMet, unique: true);
      _emit(const GoalReachedEvent());
    }
  }

  Future<void> undoLast() async {
    if (todayLogs.isEmpty) return;
    await db.deleteDrink(todayLogs.last.id);
    await _changed();
  }

  Future<void> deleteDrink(int id) async {
    await db.deleteDrink(id);
    await _changed();
  }

  Future<void> updateProfile(Profile Function(Profile) change) async {
    profile = change(profile);
    await db.saveProfile(profile);
    await _changed();
  }

  Future<void> selectPersona(String id) =>
      updateProfile((p) => p.copyWith(personaId: id));

  Future<void> setCareMode(CareMode mode) =>
      updateProfile((p) => p.copyWith(careMode: mode));

  Future<void> finishOnboarding(Profile draft) async {
    // Start the trial *before* marking onboarded, so an access check can never see an
    // onboarded user with a Pro persona and no trial (which would bounce them to Mom).
    if (await db.getInt(Keys.trialEndsAt) == null) {
      await db.setInt(
        Keys.trialEndsAt,
        DateTime.now().millisecondsSinceEpoch + trialMs,
      );
    }
    profile = draft.copyWith(onboarded: true);
    await db.saveProfile(profile);
    await _changed();
  }

  Future<bool> requestNotificationPermission() async {
    permissionGranted = await notifications.requestPermission();
    notifyListeners();
    _scheduleSync();
    return permissionGranted;
  }

  Future<void> setNotificationsEnabled(bool enabled) async {
    await db.setBool(Keys.notificationsEnabled, enabled);
    await _changed();
  }

  Future<void> addMedication(
    String name,
    int hour,
    int minute, {
    String dose = '',
  }) async {
    await db.addMedication(name, hour, minute, dose: dose);
    await _changed();
  }

  Future<void> updateMedication(Medication med) async {
    await db.updateMedication(med);
    await _changed();
  }

  Future<void> removeMedication(int id) async {
    await db.removeMedication(id);
    await _changed();
  }

  Future<void> markMed(Medication med, MedStatus? status) async {
    if (status == MedStatus.taken) _outcome(PushOutcomes.medTaken);
    if (status == null) {
      await db.clearMedLog(med.id, todayKey);
    } else {
      await db.logMed(med.id, todayKey, status);
    }
    await _changed();
  }

  Future<void> grantAdUnlock(String key) async {
    await db.grantUnlock(
      key,
      DateTime.now().millisecondsSinceEpoch + tempUnlockMs,
    );
    await _changed();
  }

  Future<PurchaseOutcome> purchase(Plan plan) async {
    final outcome = await billing.purchase(plan);
    await _changed();
    return outcome;
  }

  Future<bool> restore() async {
    final ok = await billing.restore();
    await _changed();
    return ok;
  }

  Future<void> handleNotificationResponse(NotificationResponse response) async {
    if (response.actionId != null) {
      await notifications.handleResponse(db, response);
      await reload();
      return;
    }
    switch (response.payload) {
      case 'trial':
        _emit(const RouteEvent(PushRoute.paywall));
      case 'comeback' || 'water':
        _emit(const RouteEvent(PushRoute.home));
    }
  }

  // ── Sandbox helpers (only reachable when Integrations.sandboxMode) ──
  Future<void> sandboxSetPro(bool value) async {
    if (billing case final FakeBillingService fake) await fake.setPro(value);
    await _changed();
  }

  Future<void> sandboxEndTrialSoon() async {
    await db.setInt(
      Keys.trialEndsAt,
      DateTime.now().millisecondsSinceEpoch + 60 * 60 * 1000,
    );
    await db.remove(Keys.trialSheetShownOn);
    await db.setBool(Keys.trialEndedShown, false);
    await _changed();
  }

  Future<void> sandboxExpireTrial() async {
    await db.setInt(
      Keys.trialEndsAt,
      DateTime.now().millisecondsSinceEpoch - 1000,
    );
    await db.setBool(Keys.trialEndedShown, false);
    await _changed();
  }

  Future<void> sandboxSeedWeek() async {
    final r = Random(7);
    final today = dateOnly(DateTime.now());
    for (var d = 1; d <= 6; d++) {
      final day = today.subtract(Duration(days: d));
      final target = d == 3
          ? profile.dailyMl * 0.6
          : profile.dailyMl * (1.0 + r.nextDouble() * 0.15);
      var total = 0;
      var hour = profile.wakeHour + 1;
      while (total < target && hour < profile.sleepHour) {
        final ml = r.nextBool() ? 250 : 500;
        await db.addDrink(
          ml,
          atMs: DateTime(
            day.year,
            day.month,
            day.day,
            hour,
            r.nextInt(50),
          ).millisecondsSinceEpoch,
        );
        total += ml;
        hour += 1 + r.nextInt(2);
      }
      for (final med in meds) {
        if (d != 4)
          await db.logMed(
            med.id,
            dateKey(day),
            MedStatus.taken,
            atMs: DateTime(
              day.year,
              day.month,
              day.day,
              med.hour,
              med.minute + 5,
            ).millisecondsSinceEpoch,
          );
      }
    }
    await _changed();
  }
}
