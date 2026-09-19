import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

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
    final onboarded = context.select<AppController, bool>((c) => c.profile.onboarded);
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 500),
      child: !_splashDone
          ? SplashScreen(key: const ValueKey('splash'), onDone: () => setState(() => _splashDone = true))
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
  StreamSubscription<AppEvent>? _sub;
  bool _confetti = false;

  @override
  void initState() {
    super.initState();
    _tabs.addListener(() => setState(() {}));
    _sub = context.read<AppController>().events.listen(_onEvent);
  }

  @override
  void dispose() {
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
              TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Okay')),
              FilledButton(
                style: FilledButton.styleFrom(minimumSize: const Size(120, 48)),
                onPressed: () {
                  Navigator.pop(ctx);
                  openPaywall(ctx, placement: 'persona_expired');
                },
                child: const Text('Keep them'),
              ),
            ],
          ),
        );
      case TrialEndingEvent():
        await showTrialEndingSheet(ctx, ended: false);
      case TrialEndedEvent():
        await showTrialEndingSheet(ctx, ended: true);
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
                decoration: const BoxDecoration(border: Border(top: BorderSide(color: NaglyColors.outline))),
                child: NavigationBar(
                  selectedIndex: tab.index,
                  onDestinationSelected: (i) => _tabs.value = MainTab.values[i],
                  destinations: const [
                    NavigationDestination(icon: Icon(Icons.water_drop_outlined), selectedIcon: Icon(Icons.water_drop), label: 'Home'),
                    NavigationDestination(icon: Icon(Icons.chat_bubble_outline), selectedIcon: Icon(Icons.chat_bubble), label: 'History'),
                    NavigationDestination(icon: Icon(Icons.people_outline), selectedIcon: Icon(Icons.people), label: 'Personas'),
                    NavigationDestination(icon: Icon(Icons.insights_outlined), selectedIcon: Icon(Icons.insights), label: 'Insights'),
                    NavigationDestination(icon: Icon(Icons.settings_outlined), selectedIcon: Icon(Icons.settings), label: 'Settings'),
                  ],
                ),
              ),
            ),
            if (_confetti) Positioned.fill(child: ConfettiBurst(onDone: () => setState(() => _confetti = false))),
          ],
        ),
      ),
    );
  }
}

void openPaywall(BuildContext context, {required String placement, String? relationshipId}) {
  Navigator.of(context).push(MaterialPageRoute<void>(
    fullscreenDialog: true,
    builder: (_) => PaywallScreen(placement: placement, relationshipId: relationshipId),
  ));
}
