import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../domain/persona_catalog.dart';
import '../../state/app_controller.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';
import '../widgets/water_bottle.dart';
import 'sheets.dart';

enum _Step {
  weight,
  activity,
  hours,
  building,
  goal,
  mode,
  medication,
  relationship,
  variant,
  firstGlass,
  permission,
}

/// The activation funnel: personalize → reveal goal → choose mode & nagger →
/// first sip (the emotional hook) → notification permission asked in her voice.
class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  _Step _step = _Step.weight;
  bool _forward = true;
  int _weightKg = 70;
  bool _lb = false;
  ActivityLevel _activity = ActivityLevel.light;
  int _wake = 7;
  int _sleep = 22;
  CareMode _mode = CareMode.hydration;
  String _relationship = 'mom';
  String _personaId = 'indian_mom';
  bool _firstGlass = false;
  String? _reaction;
  final _medName = TextEditingController();
  TimeOfDay _medTime = const TimeOfDay(hour: 9, minute: 0);

  int get _goal => recommendedDailyMl(_weightKg, _activity);

  List<_Step> get _steps => [
    for (final s in _Step.values)
      if (s != _Step.medication || _mode == CareMode.medication) s,
  ];

  Persona get _persona => PersonaCatalog.get(_personaId);

  @override
  void dispose() {
    _medName.dispose();
    super.dispose();
  }

  void _go(_Step s, {bool forward = true}) {
    HapticFeedback.selectionClick();
    setState(() {
      _forward = forward;
      _step = s;
    });
    if (s == _Step.building) {
      Future.delayed(const Duration(milliseconds: 1800), () {
        if (mounted && _step == _Step.building) _go(_Step.goal);
      });
    }
  }

  void _next() {
    final i = _steps.indexOf(_step);
    if (i < _steps.length - 1) _go(_steps[i + 1]);
  }

  void _back() {
    final i = _steps.indexOf(_step);
    if (i <= 0) return;
    var prev = _steps[i - 1];
    if (prev == _Step.building) prev = _Step.hours;
    _go(prev, forward: false);
  }

  Future<void> _finish({required bool askPermission}) async {
    final c = context.read<AppController>();
    if (askPermission) await c.requestNotificationPermission();
    if (_mode == CareMode.medication && _medName.text.trim().isNotEmpty) {
      await c.addMedication(
        _medName.text.trim(),
        _medTime.hour,
        _medTime.minute,
      );
    }
    await c.finishOnboarding(
      Profile(
        dailyMl: _goal,
        wakeHour: _wake,
        sleepHour: _sleep,
        personaId: _personaId,
        careMode: _mode,
        weightKg: _weightKg,
        activity: _activity,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final idx = _steps.indexOf(_step);
    return PopScope(
      canPop: idx == 0,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && _step != _Step.building) _back();
      },
      child: Scaffold(
        body: DayBackground(
          hour: DateTime.now().hour,
          intensity: 0.6,
          child: SafeArea(
            child: Column(
              children: [
                SizedBox(
                  height: 56,
                  child: Row(
                    children: [
                      if (idx > 0 && _step != _Step.building)
                        IconButton(
                          tooltip: 'Back',
                          onPressed: _back,
                          icon: const Icon(Icons.arrow_back_rounded),
                        )
                      else
                        const SizedBox(width: 48),
                      Expanded(
                        child: _Dots(count: _steps.length, index: idx),
                      ),
                      const SizedBox(width: 48),
                    ],
                  ),
                ),
                Expanded(
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 350),
                    transitionBuilder: (child, anim) {
                      final incoming = child.key == ValueKey(_step);
                      final dx = (incoming == _forward) ? 0.15 : -0.15;
                      return FadeTransition(
                        opacity: anim,
                        child: SlideTransition(
                          position:
                              Tween(
                                begin: Offset(dx, 0),
                                end: Offset.zero,
                              ).animate(
                                CurvedAnimation(
                                  parent: anim,
                                  curve: Curves.easeOutCubic,
                                ),
                              ),
                          child: child,
                        ),
                      );
                    },
                    child: KeyedSubtree(
                      key: ValueKey(_step),
                      child: _buildStep(),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildStep() => switch (_step) {
    _Step.weight => _weightStep(),
    _Step.activity => _activityStep(),
    _Step.hours => _hoursStep(),
    _Step.building => _buildingStep(),
    _Step.goal => _goalStep(),
    _Step.mode => _modeStep(),
    _Step.medication => _medicationStep(),
    _Step.relationship => _relationshipStep(),
    _Step.variant => _variantStep(),
    _Step.firstGlass => _firstGlassStep(),
    _Step.permission => _permissionStep(),
  };

  Widget _frame({
    required String title,
    String? subtitle,
    required Widget body,
    Widget? footer,
  }) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 8, 24, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(title, style: Theme.of(context).textTheme.headlineMedium),
          if (subtitle != null) ...[
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: const TextStyle(
                fontSize: 16,
                color: NaglyColors.textSecondary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
          const SizedBox(height: 24),
          Expanded(child: body),
          if (footer != null) footer,
        ],
      ),
    );
  }

  Widget _continue({
    String label = 'Continue',
    VoidCallback? onPressed,
    bool enabled = true,
  }) => FilledButton(
    onPressed: enabled ? (onPressed ?? _next) : null,
    child: Text(label),
  );

  // ── Steps ────────────────────────────────────────────────
  Widget _weightStep() {
    final shown = _lb ? (_weightKg * 2.20462).round() : _weightKg;
    void change(int delta) => setState(() {
      final kg = _lb ? ((shown + delta) / 2.20462).round() : _weightKg + delta;
      _weightKg = kg.clamp(30, 250);
    });
    return _frame(
      title: 'How much do you weigh?',
      subtitle: 'So I can set the right goal for you.',
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _RoundIcon(
                icon: Icons.remove_rounded,
                label: 'Decrease weight',
                onTap: () => change(-1),
              ),
              const SizedBox(width: 20),
              SizedBox(
                width: 150,
                child: Column(
                  children: [
                    Text(
                      '$shown',
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.displayLarge
                          ?.copyWith(fontSize: 72),
                    ),
                    Text(
                      _lb ? 'lb' : 'kg',
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        color: NaglyColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 20),
              _RoundIcon(
                icon: Icons.add_rounded,
                label: 'Increase weight',
                onTap: () => change(1),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Slider(
            value: _weightKg.toDouble(),
            min: 30,
            max: 180,
            onChanged: (v) => setState(() => _weightKg = v.round()),
          ),
          SegmentedButton<bool>(
            segments: const [
              ButtonSegment(value: false, label: Text('kg')),
              ButtonSegment(value: true, label: Text('lb')),
            ],
            selected: {_lb},
            showSelectedIcon: false,
            onSelectionChanged: (s) => setState(() => _lb = s.first),
          ),
        ],
      ),
      footer: _continue(),
    );
  }

  Widget _activityStep() {
    const options = [
      (ActivityLevel.sedentary, '🛋️', 'Sedentary', 'Mostly sitting'),
      (ActivityLevel.light, '🚶', 'Lightly active', 'Some walking'),
      (ActivityLevel.active, '🏃', 'Active', 'Daily exercise'),
      (
        ActivityLevel.veryActive,
        '🔥',
        'Very active',
        'Athlete / physical work',
      ),
    ];
    return _frame(
      title: 'How active are you?',
      body: ListView(
        children: [
          for (final (level, emoji, title, sub) in options)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _ChoiceCard(
                selected: _activity == level,
                leading: Text(emoji, style: const TextStyle(fontSize: 30)),
                title: title,
                subtitle: sub,
                onTap: () => setState(() => _activity = level),
              ),
            ),
        ],
      ),
      footer: _continue(),
    );
  }

  Widget _hoursStep() {
    final waking = _sleep > _wake ? _sleep - _wake : 24 - _wake + _sleep;
    return _frame(
      title: 'When are you awake?',
      subtitle: 'Nags only happen inside your day. Promise.',
      body: Column(
        children: [
          _HourRow(
            emoji: '☀️',
            label: 'Wake',
            hour: _wake,
            onChanged: (h) => setState(() => _wake = h),
          ),
          const SizedBox(height: 12),
          _HourRow(
            emoji: '🌙',
            label: 'Sleep',
            hour: _sleep,
            onChanged: (h) => setState(() => _sleep = h),
          ),
          const SizedBox(height: 28),
          Text('$waking', style: Theme.of(context).textTheme.displaySmall),
          const Text(
            'waking hours',
            style: TextStyle(
              fontWeight: FontWeight.w800,
              color: NaglyColors.textSecondary,
            ),
          ),
        ],
      ),
      footer: _continue(enabled: waking >= 4),
    );
  }

  Widget _buildingStep() => Center(
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const SizedBox(
          width: 64,
          height: 64,
          child: CircularProgressIndicator(strokeWidth: 6),
        ),
        const SizedBox(height: 28),
        Text(
          'Building your plan…',
          style: Theme.of(context).textTheme.headlineSmall,
        ),
        const SizedBox(height: 8),
        const Text(
          'Mom is doing the math 👩',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: NaglyColors.textSecondary,
          ),
        ),
      ],
    ),
  );

  Widget _goalStep() {
    const labels = {
      ActivityLevel.sedentary: 'sedentary',
      ActivityLevel.light: 'lightly active',
      ActivityLevel.active: 'active',
      ActivityLevel.veryActive: 'very active',
    };
    final waking = _sleep > _wake ? _sleep - _wake : 24 - _wake + _sleep;
    return _frame(
      title: '',
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TweenAnimationBuilder<double>(
              tween: Tween(begin: 0, end: _goal.toDouble()),
              duration: const Duration(milliseconds: 1200),
              curve: Curves.easeOutCubic,
              builder: (context, v, _) => Text(
                '${v.round()}',
                style: Theme.of(context).textTheme.displayLarge
                    ?.copyWith(fontSize: 84, color: NaglyColors.primaryDeep),
              ),
            ),
            const Text(
              'ml',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w900,
                color: NaglyColors.primaryDeep,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Your daily goal',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 6),
            Text(
              'Based on $_weightKg kg · ${labels[_activity]} · ${waking}h awake',
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontWeight: FontWeight.w700,
                color: NaglyColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
      footer: _continue(label: 'Sounds good'),
    );
  }

  Widget _modeStep() => _frame(
    title: 'What should I care about?',
    subtitle: 'Pick a mode. You can change it any time.',
    body: ListView(
      children: [
        _ChoiceCard(
          selected: _mode == CareMode.hydration,
          leading: const Text('💧', style: TextStyle(fontSize: 30)),
          title: 'Hydration',
          subtitle: 'Water · the classic',
          trailing: const Tag('FREE', color: NaglyColors.success),
          onTap: () => setState(() => _mode = CareMode.hydration),
        ),
        const SizedBox(height: 12),
        _ChoiceCard(
          selected: _mode == CareMode.medication,
          leading: const Text('💊', style: TextStyle(fontSize: 30)),
          title: 'Meds & Supplements',
          subtitle: 'Pills, vitamins, protein, creatine — plus water. One reminder is free forever: nobody should pay to be reminded of their most important pill.',
          trailing: const Tag('1 FREE', color: NaglyColors.med),
          onTap: () => setState(() => _mode = CareMode.medication),
        ),
      ],
    ),
    footer: _continue(),
  );

  Widget _medicationStep() => _frame(
    title: 'What should I remind you to take?',
    subtitle: 'You can add more later. Skip if you prefer.',
    body: SingleChildScrollView(
      child: Column(
        children: [
          TextField(
            controller: _medName,
            textCapitalization: TextCapitalization.sentences,
            decoration: InputDecoration(
              hintText: 'e.g. BP tablet, Vitamin D, Creatine',
              filled: true,
              fillColor: Colors.white,
              prefixIcon: const Padding(
                padding: EdgeInsets.all(12),
                child: Text('💊', style: TextStyle(fontSize: 20)),
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(18),
                borderSide: const BorderSide(color: NaglyColors.outline),
              ),
            ),
          ),
          const SizedBox(height: 10),
          MedQuickPicks(onPick: (n, _) => setState(() => _medName.text = n)),
          const SizedBox(height: 12),
          NCard(
            onTap: () async {
              final t = await showTimePicker(
                context: context,
                initialTime: _medTime,
              );
              if (t != null) setState(() => _medTime = t);
            },
            child: Row(
              children: [
                const Text('⏰', style: TextStyle(fontSize: 22)),
                const SizedBox(width: 12),
                const Expanded(
                  child: Text(
                    'Remind me at',
                    style: TextStyle(fontWeight: FontWeight.w800),
                  ),
                ),
                Text(
                  _medTime.format(context),
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                    color: NaglyColors.med,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    ),
    footer: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        ListenableBuilder(
          listenable: _medName,
          builder: (_, _) =>
              _continue(enabled: _medName.text.trim().isNotEmpty),
        ),
        TextButton(
          onPressed: () {
            _medName.clear();
            _next();
          },
          child: const Text('Skip for now'),
        ),
      ],
    ),
  );

  Widget _relationshipStep() => _frame(
    title: 'Who nags you best?',
    subtitle: 'Everyone is free for your first 7 days. Mom stays free forever.',
    body: ListView(
      children: [
        for (final r in PersonaCatalog.relationships)
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: _ChoiceCard(
              selected: _relationship == r.id,
              leading: Text(r.emoji, style: const TextStyle(fontSize: 32)),
              title: r.displayName,
              subtitle: r.tagline,
              trailing: r.tier == Tier.free
                  ? const Tag('FREE', color: NaglyColors.success)
                  : const Tag('7 DAYS FREE', color: NaglyColors.primaryDeep),
              onTap: () => setState(() {
                _relationship = r.id;
                _personaId = PersonaCatalog.variantsOf(r.id).first.id;
              }),
            ),
          ),
      ],
    ),
    footer: _continue(),
  );

  Widget _variantStep() {
    final rel = PersonaCatalog.relationship(_relationship);
    return _frame(
      title: 'Pick your ${rel.displayName}',
      subtitle: 'Same love, different voice.',
      body: ListView(
        children: [
          for (final p in PersonaCatalog.variantsOf(_relationship))
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _ChoiceCard(
                selected: _personaId == p.id,
                leading: Text(p.emoji, style: const TextStyle(fontSize: 32)),
                title: p.displayName,
                subtitle: '"${p.signature}"',
                onTap: () => setState(() => _personaId = p.id),
              ),
            ),
        ],
      ),
      footer: _continue(),
    );
  }

  Widget _firstGlassStep() => _frame(
    title: 'Log your first glass',
    subtitle: _firstGlass ? null : 'Tap the bottle 💧',
    body: Column(
      children: [
        Expanded(
          child: Center(
            child: WaterBottle(
              progress: _firstGlass ? 250 / _goal : 0,
              label: _firstGlass ? '250' : null,
              sublabel: _firstGlass ? 'ml' : null,
              onTap: _firstGlass
                  ? null
                  : () async {
                      HapticFeedback.mediumImpact();
                      await context.read<AppController>().logDrink(250);
                      setState(() {
                        _firstGlass = true;
                        _reaction = pickLine(_persona, Mood.proud);
                      });
                    },
            ),
          ),
        ),
        AnimatedOpacity(
          opacity: _reaction == null ? 0 : 1,
          duration: const Duration(milliseconds: 400),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              PersonaAvatar(emoji: _persona.emoji, mood: Mood.proud, size: 56),
              const SizedBox(width: 12),
              Expanded(
                child: SpeechBubble(
                  text: '${_reaction ?? ''} 💛',
                  name: _persona.displayName,
                  tail: false,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
      ],
    ),
    footer: _continue(enabled: _firstGlass),
  );

  Widget _permissionStep() => _frame(
    title:
        'Let ${PersonaCatalog.relationship(_relationship).displayName} remind you?',
    body: Column(
      children: [
        const Text('🔔', style: TextStyle(fontSize: 56)),
        const SizedBox(height: 20),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            PersonaAvatar(emoji: _persona.emoji, size: 56),
            const SizedBox(width: 12),
            Expanded(
              child: SpeechBubble(
                name: _persona.displayName,
                tail: false,
                text:
                    '${_persona.signature} Let me nudge you when you forget'
                    '${_mode == CareMode.medication ? ' — and remind you about your pills' : ''}.',
              ),
            ),
          ],
        ),
        const Spacer(),
        const Text(
          'Reminders only arrive during your waking hours. You can log right from the notification.',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: NaglyColors.textSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 16),
      ],
    ),
    footer: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _continue(
          label: 'Allow notifications',
          onPressed: () => _finish(askPermission: true),
        ),
        TextButton(
          onPressed: () => _finish(askPermission: false),
          child: const Text('Maybe later'),
        ),
      ],
    ),
  );
}

class _Dots extends StatelessWidget {
  const _Dots({required this.count, required this.index});
  final int count, index;

  @override
  Widget build(BuildContext context) => Semantics(
    label: 'Step ${index + 1} of $count',
    child: Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        count,
        (i) => AnimatedContainer(
          duration: const Duration(milliseconds: 300),
          margin: const EdgeInsets.symmetric(horizontal: 3),
          width: i == index ? 22 : 7,
          height: 7,
          decoration: BoxDecoration(
            color: i <= index ? NaglyColors.primaryDeep : NaglyColors.outline,
            borderRadius: BorderRadius.circular(4),
          ),
        ),
      ),
    ),
  );
}

class _ChoiceCard extends StatelessWidget {
  const _ChoiceCard({
    required this.selected,
    required this.leading,
    required this.title,
    required this.subtitle,
    required this.onTap,
    this.trailing,
  });

  final bool selected;
  final Widget leading;
  final String title, subtitle;
  final Widget? trailing;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      selected: selected,
      button: true,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        decoration: BoxDecoration(
          color: selected
              ? NaglyColors.primary.withValues(alpha: 0.1)
              : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: selected ? NaglyColors.primaryDeep : NaglyColors.outline,
            width: selected ? 2 : 1,
          ),
        ),
        child: Material(
          type: MaterialType.transparency,
          child: InkWell(
            borderRadius: BorderRadius.circular(20),
            onTap: () {
              HapticFeedback.selectionClick();
              onTap();
            },
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  leading,
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: const TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.w900,
                            color: NaglyColors.ink,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          subtitle,
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            color: NaglyColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (trailing != null) ...[
                    const SizedBox(width: 8),
                    trailing!,
                  ],
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _RoundIcon extends StatelessWidget {
  const _RoundIcon({
    required this.icon,
    required this.label,
    required this.onTap,
  });
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => IconButton.filledTonal(
    tooltip: label,
    iconSize: 30,
    style: IconButton.styleFrom(minimumSize: const Size(60, 60)),
    onPressed: () {
      HapticFeedback.selectionClick();
      onTap();
    },
    icon: Icon(icon),
  );
}

class _HourRow extends StatelessWidget {
  const _HourRow({
    required this.emoji,
    required this.label,
    required this.hour,
    required this.onChanged,
  });
  final String emoji, label;
  final int hour;
  final ValueChanged<int> onChanged;

  @override
  Widget build(BuildContext context) => NCard(
    child: Row(
      children: [
        Text(emoji, style: const TextStyle(fontSize: 26)),
        const SizedBox(width: 12),
        Expanded(
          child: Text(
            label,
            style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w900),
          ),
        ),
        _RoundIcon(
          icon: Icons.remove_rounded,
          label: '$label earlier',
          onTap: () => onChanged((hour + 23) % 24),
        ),
        SizedBox(
          width: 76,
          child: Text(
            formatHourLabel(hour),
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w900,
              color: NaglyColors.ink,
            ),
          ),
        ),
        _RoundIcon(
          icon: Icons.add_rounded,
          label: '$label later',
          onTap: () => onChanged((hour + 1) % 24),
        ),
      ],
    ),
  );
}
