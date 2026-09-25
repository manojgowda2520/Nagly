import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nagly/config/integrations.dart';
import 'package:nagly/data/database.dart';
import 'package:nagly/ui/screens/settings_screen.dart';
import 'package:nagly/domain/models.dart';
import 'package:nagly/services/ads.dart';
import 'package:nagly/services/billing.dart';
import 'package:nagly/services/notifications.dart';
import 'package:nagly/services/push.dart';
import 'package:nagly/state/app_controller.dart';
import 'package:nagly/ui/app.dart';
import 'package:provider/provider.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

/// Records what would be scheduled instead of talking to the OS.
class FakeNotifications extends NotificationService {
  int syncs = 0;
  bool permissionAsked = false;

  @override
  Future<void> init({void Function(NotificationResponse)? onResponse}) async {}
  @override
  Future<bool> requestPermission() async => permissionAsked = true;
  @override
  Future<bool> permissionGranted() async => true;
  @override
  Future<void> sync(NaglyDatabase db, {DateTime? now}) async => syncs++;
}

Future<(AppController, NaglyDatabase, FakeNotifications)> _boot(
  WidgetTester tester, {
  bool tourSeen = true,
}) async {
  databaseFactory = databaseFactoryFfiNoIsolate;
  final db = await NaglyDatabase.open(path: inMemoryDatabasePath);
  await db.setBool(Keys.tourSeen, tourSeen);
  addTearDown(
    db.close,
  ); // always close, so a failed test can't leak state into the next
  final notifications = FakeNotifications();
  final c = AppController(
    db: db,
    billing: FakeBillingService(db),
    ads: FakeAdService(),
    push: FakePushService(),
    notifications: notifications,
  );
  await c.init();
  await tester.binding.setSurfaceSize(const Size(430, 932));
  await tester.pumpWidget(
    ChangeNotifierProvider.value(value: c, child: const NaglyApp()),
  );
  await tester.pump(const Duration(milliseconds: 1600)); // splash
  await tester.pump(const Duration(milliseconds: 600));
  return (c, db, notifications);
}

Future<void> _tap(WidgetTester tester, Finder f) async {
  await tester.ensureVisible(f);
  await tester.tap(f);
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 500));
}

/// Pump frames until [f] finds something (sheets arrive a few frames after events).
Future<void> _pumpUntil(WidgetTester tester, Finder f, {int tries = 20}) async {
  for (var i = 0; i < tries && f.evaluate().isEmpty; i++) {
    await tester.pump(const Duration(milliseconds: 200));
  }
  await tester.pump(
    const Duration(milliseconds: 600),
  ); // let the sheet finish sliding in
}

Future<void> _teardown(WidgetTester tester, AppController c) async {
  await tester.pumpWidget(const SizedBox());
  c.dispose();
  await tester.pump(const Duration(seconds: 1));
}

void main() {
  testWidgets(
    'onboarding: medication mode, Pro persona during trial, first glass',
    (tester) async {
      final (c, db, notifications) = await _boot(tester);

      expect(find.text('How much do you weigh?'), findsOneWidget);
      await _tap(tester, find.text('Continue')); // weight
      await _tap(tester, find.text('Active')); // activity
      await _tap(tester, find.text('Continue'));
      await _tap(tester, find.text('Continue')); // hours
      expect(find.text('Building your plan…'), findsOneWidget);
      await tester.pump(const Duration(milliseconds: 1900));
      await tester.pump(const Duration(milliseconds: 500));
      expect(find.text('Your daily goal'), findsOneWidget);
      await _tap(tester, find.text('Sounds good'));

      await _tap(tester, find.text('Meds & Supplements'));
      await _tap(tester, find.text('Continue'));

      // Continue must be disabled until a name is typed.
      final continueBtn = find.widgetWithText(FilledButton, 'Continue');
      expect(tester.widget<FilledButton>(continueBtn).onPressed, isNull);
      await tester.enterText(find.byType(TextField), 'BP tablet');
      await tester.pump();
      expect(tester.widget<FilledButton>(continueBtn).onPressed, isNotNull);
      await _tap(tester, continueBtn);

      await _tap(tester, find.text('Dad'));
      await _tap(tester, find.text('Continue'));
      await _tap(tester, find.text('Corny Dad'));
      await _tap(tester, find.text('Continue'));

      expect(find.text('Log your first glass'), findsOneWidget);
      expect(
        tester
            .widget<FilledButton>(find.widgetWithText(FilledButton, 'Continue'))
            .onPressed,
        isNull,
      );
      await _tap(tester, find.bySemanticsLabel(RegExp('Water bottle')));
      await tester.pump(const Duration(seconds: 1));
      await _tap(tester, find.text('Continue'));

      expect(find.text('Let Dad remind you?'), findsOneWidget);
      await _tap(tester, find.text('Allow notifications'));
      await tester.pump(const Duration(seconds: 1));
      expect(notifications.permissionAsked, isTrue);

      // Landed on Home in Medication mode, still with Corny Dad (trial covers Pro voices).
      expect(find.text("Today's care"), findsOneWidget);
      expect(c.profile.personaId, 'corny_dad');
      expect(c.profile.careMode, CareMode.medication);
      expect(c.profile.activity, ActivityLevel.active);
      expect(c.access.inTrial, isTrue);
      expect(c.meds.single.name, 'BP tablet');
      expect(c.consumedMl, 250);
      expect(find.text('BP tablet'), findsWidgets);

      // Taking the medication logs it for today.
      await _tap(tester, find.text('Took it'));
      await tester.pump(const Duration(seconds: 1));
      expect(c.medStatusToday(c.meds.single)?.status, MedStatus.taken);
      // Conversions are reported to OneSignal Outcomes.
      final push = c.push as FakePushService;
      expect(
        push.outcomes,
        containsAll(['water_logged', 'water_ml=250.0', 'med_taken']),
      );
      expect(push.tags.keys.toSet(), {
        'persona_id',
        'care_mode',
        'current_streak',
        'last_log_at',
        'trial_started_at',
        'is_pro',
      });

      // A second medication is allowed during the trial.
      expect(c.access.canAddMedication(1), isTrue);
      expect(notifications.syncs, greaterThan(0));

      await _teardown(tester, c);
    },
  );

  testWidgets(
    'trial end drops Pro persona to Mom, keeps 1 med, paywall purchase restores',
    (tester) async {
      final (c, db, _) = await _boot(tester);
      await c.addMedication('A', 8, 0);
      await c.addMedication('B', 20, 0);
      await c.finishOnboarding(
        const Profile(personaId: 'gym_coach', careMode: CareMode.medication),
      );
      await tester.pump(const Duration(seconds: 1));
      expect(c.profile.personaId, 'gym_coach');
      expect(c.activeMeds.length, 2);

      await c.sandboxExpireTrial();
      await tester.pump(const Duration(seconds: 1));
      // Persona fallback dialog, in the Mom's voice.
      expect(
        find.text("They're gone for now"),
        findsNothing,
        reason: 'trial-ended sheet explains it instead',
      );
      expect(c.profile.personaId, 'indian_mom');
      expect(c.activeMeds.map((m) => m.name), [
        'A',
      ], reason: 'first medication stays free');
      await tester.pump(const Duration(seconds: 1));
      // Trial-ended sheet.
      await _pumpUntil(tester, find.text('My full care plan has ended'));
      expect(find.text('My full care plan has ended'), findsOneWidget);
      await _tap(tester, find.text('See plans'));
      await tester.pump(const Duration(seconds: 1));

      expect(find.text('Keep the whole family'), findsOneWidget);
      expect(find.text('Lifetime'), findsOneWidget);
      expect(find.text('Annual'), findsOneWidget);
      expect(find.text('Monthly'), findsOneWidget);
      await _tap(tester, find.text('Go Pro'));
      await tester.pump(const Duration(seconds: 2));
      expect(c.access.isPro, isTrue);
      expect(c.activeMeds.length, 2);

      await _teardown(tester, c);
    },
  );

  testWidgets(
    'feature tour: runs once after onboarding, replays from Settings',
    (tester) async {
      final (c, db, _) = await _boot(tester, tourSeen: false);
      await c.finishOnboarding(const Profile());
      await _pumpUntil(tester, find.text('Indian Mom has more to say'));
      expect(find.text('Indian Mom has more to say'), findsOneWidget);
      expect(find.text('1 of 7'), findsOneWidget);

      await _tap(tester, find.text('Next'));
      expect(find.text('Your bond grows'), findsOneWidget);
      for (final title in [
        'Tap to sip, tilt to slosh',
        'Log in one tap',
        'Pick your nagger',
        'Your history is a chat',
      ]) {
        await _tap(tester, find.text('Next'));
        expect(find.text(title), findsOneWidget);
      }
      await _tap(tester, find.text('Next'));
      expect(find.text('See your week'), findsOneWidget);
      await _tap(tester, find.text('Got it'));
      await tester.pump(const Duration(milliseconds: 500));
      expect(find.text('See your week'), findsNothing);
      expect(c.tourSeen, isTrue);
      expect(await db.getBool(Keys.tourSeen), isTrue);

      // Settings → Show app tour brings it back; Skip ends it.
      await _tap(tester, find.text('Settings'));
      await tester.scrollUntilVisible(
        find.text('Show app tour'),
        300,
        scrollable: find
            .descendant(
              of: find.byType(SettingsScreen),
              matching: find.byType(Scrollable),
            )
            .first,
      );
      await _tap(tester, find.text('Show app tour'));
      await _pumpUntil(tester, find.text('Indian Mom has more to say'));
      expect(find.text('Indian Mom has more to say'), findsOneWidget);
      await _tap(tester, find.text('Skip'));
      await tester.pump(const Duration(milliseconds: 500));
      expect(find.text('Indian Mom has more to say'), findsNothing);
      expect(c.tourSeen, isTrue);

      await _teardown(tester, c);
    },
  );

  testWidgets('spouse persona + "Make it yours" rename during the trial', (
    tester,
  ) async {
    final (c, _, _) = await _boot(tester);
    await c.finishOnboarding(const Profile());
    await tester.pump(const Duration(seconds: 1));
    await _tap(tester, find.text('Personas'));
    await _tap(tester, find.text('Spouse'));
    await _tap(tester, find.text('Caring Wife'));
    expect(c.profile.personaId, 'caring_wife');

    await _tap(tester, find.text('Give them a real name'));
    await _pumpUntil(tester, find.text('Make it yours'));
    await tester.enterText(find.byType(TextField), 'Priya');
    await tester.pump();
    await _tap(tester, find.text('🥰'));
    await _tap(tester, find.text('Save'));
    await tester.pump(const Duration(seconds: 1));
    expect(c.persona.displayName, 'Priya');
    expect(c.persona.emoji, '🥰');
    expect(c.persona.id, 'caring_wife');
    expect(find.text('Priya'), findsWidgets);

    // Switching persona drops the custom name (it belonged to the Caring Wife).
    await c.selectPersona('indian_mom');
    await tester.pump(const Duration(seconds: 1));
    expect(c.persona.displayName, 'Indian Mom');
    expect(c.profile.customName, '');

    await _teardown(tester, c);
  });

  testWidgets('locked persona → rewarded ad unlocks the relationship for 24h', (
    tester,
  ) async {
    Integrations.monetizationMode = MonetizationMode.both;
    addTearDown(
      () => Integrations.monetizationMode = MonetizationMode.payments,
    );
    final (c, db, _) = await _boot(tester);
    await c.finishOnboarding(const Profile());
    await c.sandboxExpireTrial();
    await tester.pump(const Duration(seconds: 1));
    await tester.pump(const Duration(seconds: 1));
    await _pumpUntil(tester, find.text('Keep free (water + 1 reminder)'));
    await _tap(tester, find.text('Keep free (water + 1 reminder)'));
    await _tap(tester, find.text('Personas'));
    await _tap(tester, find.text('Bestie'));
    await _tap(tester, find.text('The Bestie'));
    expect(find.text('Watch ad · unlock 24h'), findsOneWidget);
    await _tap(tester, find.text('Watch ad · unlock 24h'));
    expect(find.text('Sandbox ad'), findsOneWidget);
    await tester.pump(const Duration(seconds: 6));
    await tester.pump(const Duration(seconds: 1));
    expect(c.profile.personaId, 'the_bestie');
    expect(c.access.relationshipAccessible('bestie'), isTrue);
    expect(c.access.relationshipAccessible('dad'), isFalse);

    await _teardown(tester, c);
  });

  testWidgets(
    'Plan B (no purchases): paywall becomes ad-unlock hub; ad unlocks extra meds',
    (tester) async {
      Integrations.purchasesEnabled = false;
      addTearDown(() => Integrations.purchasesEnabled = true);
      final (c, _, _) = await _boot(tester);
      await c.addMedication('A', 8, 0);
      await c.addMedication('B', 20, 0);
      await c.finishOnboarding(const Profile(careMode: CareMode.medication));
      await c.sandboxExpireTrial();
      await tester.pump(const Duration(seconds: 1));
      await tester.pump(const Duration(seconds: 1));
      await _pumpUntil(tester, find.text('Unlock with a short ad'));
      expect(
        find.text('Unlock with a short ad'),
        findsOneWidget,
        reason: 'trial-ended sheet offers ads, not plans',
      );
      await _tap(tester, find.text('Unlock with a short ad'));
      await _pumpUntil(tester, find.text('🎁 Unlock with a short ad'));
      expect(find.text('🎁 Unlock with a short ad'), findsOneWidget);
      expect(find.text('Go Pro'), findsNothing);
      expect(c.activeMeds.length, 1);
      await _tap(tester, find.text('Unlimited meds & supplements'));
      expect(find.text('Sandbox ad'), findsOneWidget);
      await tester.pump(const Duration(seconds: 6));
      await tester.pump(const Duration(seconds: 1));
      expect(c.activeMeds.length, 2);
      expect(c.access.canAddMedication(2), isTrue);
      await _teardown(tester, c);
    },
  );

  test('remote monetization_mode switch', () {
    addTearDown(
      () => Integrations.monetizationMode = MonetizationMode.payments,
    );
    expect(Integrations.purchasesEnabled, isTrue);
    expect(Integrations.adsEnabled, isFalse);
    Integrations.applyRemoteMode('ads');
    expect(Integrations.purchasesEnabled, isFalse);
    expect(Integrations.adsEnabled, isTrue);
    Integrations.applyRemoteMode('both');
    expect(Integrations.purchasesEnabled, isTrue);
    expect(Integrations.adsEnabled, isTrue);
    Integrations.applyRemoteMode('nonsense');
    expect(Integrations.monetizationMode, MonetizationMode.both);
  });
}
