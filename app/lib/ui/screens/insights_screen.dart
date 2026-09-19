import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../domain/insights.dart';
import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../state/app_controller.dart';
import '../theme.dart';
import '../widgets/common.dart';

class InsightsScreen extends StatelessWidget {
  const InsightsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final w = c.insights;
    final unit = c.profile.volumeUnit;
    final adherence = c.weeklyMedAdherence;
    return DayBackground(
      hour: c.now.hour,
      intensity: 0.45,
      child: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 32),
          children: [
            Text('Insights', style: Theme.of(context).textTheme.headlineMedium),
            const SizedBox(height: 4),
            const Text(
              'Your last 7 days',
              style: TextStyle(
                fontWeight: FontWeight.w800,
                color: NaglyColors.textSecondary,
              ),
            ),
            const SizedBox(height: 16),
            NCard(
              padding: const EdgeInsets.fromLTRB(12, 18, 12, 12),
              child: _WeekChart(
                days: w.days,
                goal: c.profile.dailyMl,
                unit: unit,
              ),
            ),
            const SizedBox(height: 12),
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 1.75,
              children: [
                _Stat(
                  label: 'Current streak',
                  value: '${w.currentStreak}',
                  unit: w.currentStreak == 1 ? 'day' : 'days',
                  emoji: '🔥',
                ),
                _Stat(
                  label: 'Daily average',
                  value: '${mlToDisplay(w.dailyAverageMl, unit)}',
                  unit: unitLabel(unit),
                  emoji: '💧',
                ),
                _Stat(
                  label: 'Goal met',
                  value: '${w.goalMetDays}',
                  unit: '/ 7',
                  emoji: '🎯',
                ),
                _Stat(
                  label: 'Best hour',
                  value: w.bestHour == null
                      ? '—'
                      : formatHour(w.bestHour!).split(' ').first,
                  unit: w.bestHour == null
                      ? ''
                      : formatHour(w.bestHour!).split(' ').last,
                  emoji: '⏰',
                ),
                if (c.profile.careMode == CareMode.medication &&
                    adherence.due > 0) ...[
                  _Stat(
                    label: 'Doses taken',
                    value: '${adherence.taken}',
                    unit: '/ ${adherence.due}',
                    emoji: '💊',
                    color: NaglyColors.med,
                  ),
                  _Stat(
                    label: 'Best streak',
                    value: '${w.bestStreak}',
                    unit: w.bestStreak == 1 ? 'day' : 'days',
                    emoji: '🏆',
                  ),
                ],
              ],
            ),
            const SizedBox(height: 20),
            NCard(
              color: const Color(0xFFF3FAFD),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '👨‍👩‍👧 Keep your family in the loop',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: NaglyColors.ink,
                    ),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Share a simple weekly summary with someone who worries about you. Nothing is uploaded — you choose who sees it.',
                    style: TextStyle(
                      fontWeight: FontWeight.w700,
                      color: NaglyColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: () => SharePlus.instance.share(
                      ShareParams(text: c.weeklyReport),
                    ),
                    icon: const Icon(Icons.ios_share_rounded),
                    label: const Text('Share my week'),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Stat extends StatelessWidget {
  const _Stat({
    required this.label,
    required this.value,
    required this.unit,
    required this.emoji,
    this.color,
  });
  final String label, value, unit, emoji;
  final Color? color;

  @override
  Widget build(BuildContext context) => NCard(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          '$emoji  $label',
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w800,
            color: NaglyColors.textSecondary,
          ),
        ),
        FittedBox(
          fit: BoxFit.scaleDown,
          alignment: Alignment.centerLeft,
          child: Text.rich(
            TextSpan(
              children: [
                TextSpan(
                  text: value,
                  style: TextStyle(
                    fontSize: 30,
                    fontWeight: FontWeight.w900,
                    color: color ?? NaglyColors.ink,
                  ),
                ),
                TextSpan(
                  text: ' $unit',
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w800,
                    color: NaglyColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    ),
  );
}

/// Seven bars vs a dashed goal line; bars grow in on first paint.
class _WeekChart extends StatelessWidget {
  const _WeekChart({
    required this.days,
    required this.goal,
    required this.unit,
  });
  final List<DayTotal> days;
  final int goal;
  final VolumeUnit unit;

  @override
  Widget build(BuildContext context) {
    final maxV =
        [goal, ...days.map((d) => d.totalMl)].reduce((a, b) => a > b ? a : b) *
        1.1;
    const chartH = 150.0;
    return Semantics(
      label:
          'Weekly water chart. ${days.map((d) => '${DateFormat.E().format(d.date)} ${formatVolume(d.totalMl, unit)}').join(', ')}',
      excludeSemantics: true,
      child: Column(
        children: [
          Row(
            children: [
              CustomPaint(painter: _DashPainter(), size: const Size(18, 1)),
              const SizedBox(width: 6),
              Text(
                'Daily goal · ${formatVolume(goal, unit)}',
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  color: NaglyColors.coral,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          SizedBox(
            height: chartH,
            child: Stack(
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    for (final (i, d) in days.indexed)
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 5),
                          child: TweenAnimationBuilder<double>(
                            tween: Tween(begin: 0, end: d.totalMl / maxV),
                            duration: Duration(milliseconds: 600 + i * 80),
                            curve: Curves.easeOutCubic,
                            builder: (context, v, _) => Container(
                              height: (chartH * v).clamp(4, chartH),
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(8),
                                gradient: d.totalMl >= goal
                                    ? const LinearGradient(
                                        begin: Alignment.topCenter,
                                        end: Alignment.bottomCenter,
                                        colors: [
                                          Color(0xFF6FD3FA),
                                          NaglyColors.primaryDeep,
                                        ],
                                      )
                                    : null,
                                color: d.totalMl >= goal
                                    ? null
                                    : NaglyColors.surfaceVariant.withValues(
                                        alpha: 1,
                                      ),
                                border: d.totalMl >= goal
                                    ? null
                                    : Border.all(
                                        color: const Color(0xFFCFDDE5),
                                      ),
                              ),
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
                Positioned(
                  left: 0,
                  right: 0,
                  bottom: chartH * goal / maxV,
                  child: CustomPaint(
                    painter: _DashPainter(),
                    size: const Size(double.infinity, 1),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              for (final d in days)
                Expanded(
                  child: Text(
                    DateFormat.E().format(d.date).substring(0, 1),
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontWeight: FontWeight.w800,
                      color: NaglyColors.textSecondary,
                    ),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DashPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final p = Paint()
      ..color = NaglyColors.coral.withValues(alpha: 0.7)
      ..strokeWidth = 1.5;
    for (double x = 0; x < size.width; x += 8) {
      canvas.drawLine(Offset(x, 0), Offset(x + 4, 0), p);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
