import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../config/integrations.dart';
import '../domain/models.dart';
import '../services/push.dart';
import '../state/app_controller.dart';
import 'screens/history_screen.dart';
import 'screens/home_screen.dart';
import 'screens/insights_screen.dart';
import 'screens/onboarding_screen.dart';
import 'screens/paywall_screen.dart';
import 'screens/personas_screen.dart';
import 'screens/settings_screen.dart';
import 'screens/sheets.dart';
import 'screens/splash_screen.dart';
import 'theme.dart';
import 'widgets/common.dart';
import 'widgets/feature_tour.dart';

final navigatorKey = GlobalKey<NavigatorState>();

class NaglyApp extends StatelessWidget {
  const NaglyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nagly',
      debugShowCheckedModeBanner: false,
      navigatorKey: navigatorKey,
      theme: buildNaglyTheme(),
      themeMode: ThemeMode.light,
      home: const _Root(),
    );
  }
}

class _Root extends StatefulWidget {
  const _Root();

  @override
  State<_Root> createState() => _RootState();
}

class _RootState extends State<_Root> {
  bool _splashDone = false;

  @override
  Widget build(BuildContext context) {
    final onboarded = context.select<AppController, bool>(
      (c) => c.profile.onboarded,
    );
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 500),
      child: !_splashDone
          ? SplashScreen(
              key: const ValueKey('splash'),
              onDone: () => setState(() => _splashDone = true),
            )
          : onboarded
          ? const MainShell(key: ValueKey('shell'))
          : const OnboardingScreen(key: ValueKey('onboarding')),
    );
  }
}

enum MainTab { home, history, personas, insights, settings }

/// Lets any screen switch tabs (e.g. Home → Personas).
class TabSwitcher extends ValueNotifier<MainTab> {
  TabSwitcher() : super(MainTab.home);
}

class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  final _tabs = TabSwitcher();
  StreamSubscription<void>? _sub;
  bool _confetti = false;
  bool _touring = false;
  late final AppController _controller;

  @override
  void initState() {
    super.initState();
    _tabs.addListener(() => setState(() {}));
    _controller = context.read<AppController>()..addListener(_maybeStartTour);
    WidgetsBinding.instance.addPostFrameCallback((_) => _maybeStartTour());
    // One modal at a time: each event waits for the previous one to be dismissed.
    final c = context.read<AppController>();
    _sub = c.events.asyncMap(_onEvent).listen((_) {});
    c.flushPendingEvents();
  }

  /// Spotlight tour: once after onboarding (and after updating), or on request
  /// from Settings.
  void _maybeStartTour() {
    final c = _controller;
    if (_touring || c.tourSeen || !c.profile.onboarded) return;
    _touring = true;
    _tabs.value = MainTab.home;
    if (HomeScreen.scroll.hasClients) HomeScreen.scroll.jumpTo(0);
    Future<void>.delayed(const Duration(milliseconds: 700), () async {
      if (!mounted) return;
      await showFeatureTour(
        context,
        tourSteps(
          c.persona.displayName,
          medMode: c.profile.careMode == CareMode.medication,
        ),
      );
      await c.markTourSeen();
      _touring = false;
    });
  }

  @override
  void dispose() {
    _controller.removeListener(_maybeStartTour);
    _sub?.cancel();
    _tabs.dispose();
    super.dispose();
  }

  Future<void> _onEvent(AppEvent e) async {
    final ctx = navigatorKey.currentContext;
    if (ctx == null || !mounted) return;
    switch (e) {
      case GoalReachedEvent():
        setState(() => _confetti = true);
      case PersonaFallbackEvent(:final message):
        await showDialog<void>(
          context: ctx,
          builder: (_) => AlertDialog(
            title: const Text("They're gone for now"),
            content: Text(message),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx),
                child: const Text('Okay'),
              ),
              FilledButton(
                style: FilledButton.styleFrom(minimumSize: const Size(120, 48)),
                onPressed: () {
                  Navigator.pop(ctx);
                  openPaywall(ctx, placement: 'persona_expired');
                },
                child: Text(
                  Integrations.purchasesEnabled ? 'Keep them' : 'Watch an ad',
                ),
              ),
            ],
          ),
        );
      case TrialEndingEvent():
        await showTrialEndingSheet(ctx, ended: false);
      case TrialEndedEvent(:final departedPersona):
        await showTrialEndingSheet(
          ctx,
          ended: true,
          departedPersona: departedPersona,
        );
      case UpsellEvent(:final streak):
        await showUpsellDialog(ctx, streak);
      case RouteEvent(:final route):
        switch (route) {
          case PushRoute.paywall:
            openPaywall(ctx, placement: 'push');
          case PushRoute.home:
            _tabs.value = MainTab.home;
          case PushRoute.personas:
            _tabs.value = MainTab.personas;
          case PushRoute.history:
            _tabs.value = MainTab.history;
          case PushRoute.insights:
            _tabs.value = MainTab.insights;
        }
    }
  }

  @override
  Widget build(BuildContext context) {
    final tab = _tabs.value;
    return ChangeNotifierProvider.value(
      value: _tabs,
      child: PopScope(
        canPop: tab == MainTab.home,
        onPopInvokedWithResult: (didPop, _) {
          if (!didPop) _tabs.value = MainTab.home;
        },
        child: Stack(
          children: [
            Scaffold(
              body: IndexedStack(
                index: tab.index,
                children: const [
                  HomeScreen(),
                  HistoryScreen(),
                  PersonasScreen(),
                  InsightsScreen(),
                  SettingsScreen(),
                ],
              ),
              bottomNavigationBar: DecoratedBox(
                decoration: const BoxDecoration(
                  border: Border(top: BorderSide(color: NaglyColors.outline)),
                ),
                child: NavigationBar(
                  selectedIndex: tab.index,
                  onDestinationSelected: (i) => _tabs.value = MainTab.values[i],
                  destinations: [
                    const NavigationDestination(
                      icon: Icon(Icons.water_drop_outlined),
                      selectedIcon: Icon(Icons.water_drop),
                      label: 'Home',
                    ),
                    NavigationDestination(
                      icon: KeyedSubtree(
                        key: TourKeys.historyTab,
                        child: const Icon(Icons.chat_bubble_outline),
                      ),
                      selectedIcon: const Icon(Icons.chat_bubble),
                      label: 'History',
                    ),
                    NavigationDestination(
                      icon: KeyedSubtree(
                        key: TourKeys.personasTab,
                        child: const Icon(Icons.people_outline),
                      ),
                      selectedIcon: const Icon(Icons.people),
                      label: 'Personas',
                    ),
                    NavigationDestination(
                      icon: KeyedSubtree(
                        key: TourKeys.insightsTab,
                        child: const Icon(Icons.insights_outlined),
                      ),
                      selectedIcon: const Icon(Icons.insights),
                      label: 'Insights',
                    ),
                    NavigationDestination(
                      icon: KeyedSubtree(
                        key: TourKeys.settingsTab,
                        child: const Icon(Icons.settings_outlined),
                      ),
                      selectedIcon: const Icon(Icons.settings),
                      label: 'Settings',
                    ),
                  ],
                ),
              ),
            ),
            if (_confetti)
              Positioned.fill(
                child: ConfettiBurst(
                  onDone: () => setState(() => _confetti = false),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

/// Opens the paywall — or, in Plan B (no purchases), the rewarded-ad unlock sheet.
void openPaywall(
  BuildContext context, {
  required String placement,
  String? relationshipId,
}) {
  if (!Integrations.purchasesEnabled) {
    showAdUnlockHub(
      context,
      focusRelationshipId: relationshipId,
      medsFirst: placement == 'medication_limit',
    );
    return;
  }
  Navigator.of(context).push(
    MaterialPageRoute<void>(
      fullscreenDialog: true,
      builder: (_) =>
          PaywallScreen(placement: placement, relationshipId: relationshipId),
    ),
  );
}

/// The spotlight tour, in order. [name] is the persona the user hears.
List<TourStep> tourSteps(String name, {bool medMode = false}) => [
  TourStep(
    target: TourKeys.bubble,
    title: '$name has more to say',
    body: 'Tap the bubble for another line. Their mood changes with how you\'re doing today.',
  ),
  TourStep(
    target: TourKeys.bond,
    title: 'Your bond grows',
    body: 'Stay consistent and you go from Stranger to Soul Reminder.',
  ),
  TourStep(
    target: TourKeys.bottle,
    title: 'Tap to sip, tilt to slosh',
    body: 'Tap the bottle to log a glass. Tilt your phone and watch the water move.',
  ),
  TourStep(
    target: TourKeys.quickAdd,
    title: 'Log in one tap',
    body: 'Every reminder has these buttons too, so you can log straight from your lock screen.',
  ),
  TourStep(
    target: TourKeys.personasTab,
    title: 'Pick your nagger',
    body: 'Mom, Dad, Grandparent, Bestie, and new: Spouse. Give them a real name with "Make it yours".',
  ),
  TourStep(
    target: TourKeys.historyTab,
    title: 'Your history is a chat',
    body: 'Every sip and every dose becomes a little conversation with $name.',
  ),
  TourStep(
    target: TourKeys.insightsTab,
    title: 'See your week',
    body: 'Streaks, charts, and a weekly summary you can share with family.',
  ),
  TourStep(
    target: TourKeys.settingsTab,
    title: 'Pills, vitamins, creatine too',
    body: medMode
        ? 'Add more meds or supplements any time in Settings: BP tablets, vitamins, protein, creatine.'
        : 'Switch Care mode to Meds & Supplements in Settings and $name will remind you about pills, vitamins, protein or creatine too.',
  ),
];
