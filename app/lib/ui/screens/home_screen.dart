import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../domain/persona_catalog.dart';
import '../../state/app_controller.dart';
import '../app.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';
import '../widgets/water_bottle.dart';
import 'sheets.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final medMode = c.profile.careMode == CareMode.medication;
    return DayBackground(
      hour: c.now.hour,
      child: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 32),
          children: [
            _Header(medMode: medMode),
            const SizedBox(height: 20),
            if (medMode) const _MedicationHome() else const _HydrationHero(),
            const SizedBox(height: 8),
            const _TodayDrinks(),
          ],
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.medMode});
  final bool medMode;

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final trial = c.access.inTrial ? ' · ⏳ ${c.access.trialDaysLeft}d free' : '';
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(medMode ? "Today's care" : greetingFor(c.now.hour), style: Theme.of(context).textTheme.headlineMedium),
              const SizedBox(height: 4),
              Text('${c.profile.careMode.emoji} ${c.profile.careMode.label} · 🔥 ${c.streak}-day streak$trial',
                  style: const TextStyle(fontWeight: FontWeight.w800, color: NaglyColors.textSecondary)),
            ],
          ),
        ),
        GestureDetector(
          onTap: () => context.read<TabSwitcher>().value = MainTab.personas,
          child: BondChip(level: c.bondLevel, progress: c.bondProgress),
        ),
      ],
    );
  }
}

class _PersonaSays extends StatelessWidget {
  const _PersonaSays({this.overrideLine});
  final String? overrideLine;

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    return Column(
      children: [
        Semantics(
          button: true,
          label: 'Change persona',
          child: GestureDetector(
            onTap: () => context.read<TabSwitcher>().value = MainTab.personas,
            child: PersonaAvatar(emoji: c.persona.emoji, mood: c.mood, size: 92),
          ),
        ),
        const SizedBox(height: 18),
        SpeechBubble(
          text: overrideLine ?? c.line,
          name: c.persona.displayName,
          onTap: () {
            HapticFeedback.selectionClick();
            c.cycleLine();
          },
        ),
      ],
    );
  }
}

class _HydrationHero extends StatelessWidget {
  const _HydrationHero();

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final unit = c.profile.volumeUnit;
    final done = c.consumedMl >= c.profile.dailyMl;
    return Column(
      children: [
        const _PersonaSays(),
        const SizedBox(height: 22),
        WaterBottle(
          progress: c.progress,
          label: '${(c.progress * 100).round()}%',
          onTap: () => logWithFeedback(context, 250),
        ),
        const SizedBox(height: 16),
        Text.rich(
          TextSpan(children: [
            TextSpan(text: '${mlToDisplay(c.consumedMl, unit)}', style: Theme.of(context).textTheme.headlineMedium),
            TextSpan(
                text: ' / ${formatVolume(c.profile.dailyMl, unit)}',
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: NaglyColors.textSecondary)),
          ]),
        ),
        const SizedBox(height: 4),
        Text(
          done
              ? '🎉 Goal met — she\'s proud'
              : c.behindMl > 0
                  ? '↓ ${formatVolume(c.behindMl, unit)} behind'
                  : '✓ On track',
          style: TextStyle(
            fontWeight: FontWeight.w900,
            color: done ? NaglyColors.success : c.behindMl > 0 ? NaglyColors.coral : NaglyColors.success,
          ),
        ),
        const SizedBox(height: 18),
        const QuickAddRow(),
        const SizedBox(height: 12),
        Text(c.nextNudgeLabel, style: const TextStyle(fontWeight: FontWeight.w700, color: NaglyColors.textSecondary)),
        if (!c.permissionGranted && c.notificationsEnabled) ...[
          const SizedBox(height: 12),
          NCard(
            color: const Color(0xFFFFF4E5),
            onTap: c.requestNotificationPermission,
            child: const Row(
              children: [
                Text('🔕', style: TextStyle(fontSize: 22)),
                SizedBox(width: 10),
                Expanded(
                  child: Text("Notifications are off, so she can't nag you. Tap to turn them on.",
                      style: TextStyle(fontWeight: FontWeight.w800)),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }
}

Future<void> logWithFeedback(BuildContext context, int ml) async {
  HapticFeedback.mediumImpact();
  final c = context.read<AppController>();
  final messenger = ScaffoldMessenger.of(context);
  await c.logDrink(ml);
  messenger
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text('+${formatVolume(ml, c.profile.volumeUnit)} logged'),
      duration: const Duration(seconds: 3),
      action: SnackBarAction(label: 'Undo', textColor: NaglyColors.primary, onPressed: c.undoLast),
    ));
}

class QuickAddRow extends StatelessWidget {
  const QuickAddRow({super.key, this.compact = false});
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final unit = context.select<AppController, VolumeUnit>((c) => c.profile.volumeUnit);
    String label(int ml) => unit == VolumeUnit.ml ? '+$ml' : '+${mlToDisplay(ml, unit)} oz';
    return Row(
      children: [
        Expanded(child: PillButton(label: label(250), filled: true, semanticLabel: 'Log 250 millilitres', onPressed: () => logWithFeedback(context, 250))),
        const SizedBox(width: 10),
        Expanded(child: PillButton(label: label(500), filled: true, semanticLabel: 'Log 500 millilitres', onPressed: () => logWithFeedback(context, 500))),
        if (!compact) ...[
          const SizedBox(width: 10),
          Expanded(
            child: PillButton(
              label: 'Custom',
              onPressed: () async {
                final ml = await showCustomAmountSheet(context);
                if (ml != null && context.mounted) await logWithFeedback(context, ml);
              },
            ),
          ),
        ],
      ],
    );
  }
}

class _MedicationHome extends StatelessWidget {
  const _MedicationHome();

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final unit = c.profile.volumeUnit;
    final due = c.activeMeds.where((m) => c.medStatusToday(m) == null).toList();
    final nextDue = due.where((m) => DateTime(c.now.year, c.now.month, c.now.day, m.hour, m.minute).isBefore(c.now.add(const Duration(hours: 1)))).firstOrNull;
    final override = nextDue == null
        ? null
        : PersonaCatalog.fillMed(c.persona.medDue[nextDue.id % c.persona.medDue.length], nextDue.name);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _PersonaSays(overrideLine: override),
        const SizedBox(height: 20),
        NCard(
          child: Row(
            children: [
              WaterBottle(progress: c.progress, width: 60, height: 88, enableTilt: false),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text.rich(TextSpan(children: [
                      TextSpan(text: '${mlToDisplay(c.consumedMl, unit)}', style: Theme.of(context).textTheme.titleLarge),
                      TextSpan(text: ' / ${formatVolume(c.profile.dailyMl, unit)}', style: const TextStyle(fontWeight: FontWeight.w800, color: NaglyColors.textSecondary)),
                    ])),
                    Text(c.behindMl > 0 ? '${formatVolume(c.behindMl, unit)} behind' : 'On track',
                        style: TextStyle(fontWeight: FontWeight.w800, color: c.behindMl > 0 ? NaglyColors.coral : NaglyColors.success)),
                    const SizedBox(height: 10),
                    const QuickAddRow(compact: true),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SectionLabel('Medication'),
        for (final med in c.meds) ...[
          MedicationCard(med: med),
          const SizedBox(height: 10),
        ],
        OutlinedButton.icon(
          onPressed: () => addMedicationFlow(context),
          icon: const Icon(Icons.add_rounded),
          label: Text(c.meds.isEmpty ? 'Add your first medication' : 'Add medication'),
        ),
      ],
    );
  }
}

Future<void> addMedicationFlow(BuildContext context) async {
  final c = context.read<AppController>();
  if (!c.access.canAddMedication(c.meds.length)) {
    openPaywall(context, placement: 'medication_limit');
    return;
  }
  await showMedicationEditor(context);
}

class MedicationCard extends StatelessWidget {
  const MedicationCard({super.key, required this.med});
  final Medication med;

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final status = c.medStatusToday(med);
    final paused = c.isMedPaused(med);
    final at = DateTime(c.now.year, c.now.month, c.now.day, med.hour, med.minute);
    final overdue = status == null && at.isBefore(c.now);
    final time = DateFormat.jm().format(at);
    return NCard(
      onTap: () => showMedicationEditor(context, existing: med),
      child: Row(
        children: [
          Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(color: NaglyColors.med.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(14)),
            alignment: Alignment.center,
            child: const Text('💊', style: TextStyle(fontSize: 22)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(med.dose.isEmpty ? med.name : '${med.name} · ${med.dose}',
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w900, color: NaglyColors.ink)),
                Text(
                  paused
                      ? 'Paused — Pro keeps every reminder'
                      : switch (status?.status) {
                          MedStatus.taken => 'Taken at ${DateFormat.jm().format(DateTime.fromMillisecondsSinceEpoch(status!.atMs))}',
                          MedStatus.skipped => 'Skipped today',
                          null => overdue ? 'Overdue · was due $time' : 'Due $time',
                        },
                  style: TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 13,
                    color: overdue && !paused ? NaglyColors.coral : NaglyColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          if (paused)
            TextButton(onPressed: () => openPaywall(context, placement: 'medication_limit'), child: const Text('Unpause'))
          else if (status == null)
            FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(96, 44), backgroundColor: NaglyColors.med),
              onPressed: () {
                HapticFeedback.mediumImpact();
                c.markMed(med, MedStatus.taken);
              },
              child: const Text('Took it'),
            )
          else
            TextButton(
              onPressed: () => c.markMed(med, null),
              child: Text(status.status == MedStatus.taken ? '✓ Taken' : 'Undo',
                  style: TextStyle(color: status.status == MedStatus.taken ? NaglyColors.success : null)),
            ),
        ],
      ),
    );
  }
}

class _TodayDrinks extends StatelessWidget {
  const _TodayDrinks();

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    if (c.todayLogs.isEmpty) return const SizedBox.shrink();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SectionLabel("Today's sips"),
        NCard(
          padding: EdgeInsets.zero,
          child: Column(
            children: [
              for (final log in c.todayLogs.reversed)
                Dismissible(
                  key: ValueKey(log.id),
                  direction: DismissDirection.endToStart,
                  background: Container(
                    color: NaglyColors.danger.withValues(alpha: 0.12),
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 20),
                    child: const Icon(Icons.delete_outline, color: NaglyColors.danger),
                  ),
                  onDismissed: (_) => c.deleteDrink(log.id),
                  child: ListTile(
                    leading: const Text('💧', style: TextStyle(fontSize: 20)),
                    title: Text('+${formatVolume(log.amountMl, c.profile.volumeUnit)}',
                        style: const TextStyle(fontWeight: FontWeight.w900)),
                    trailing: Text(DateFormat.jm().format(DateTime.fromMillisecondsSinceEpoch(log.timestampMs)),
                        style: const TextStyle(fontWeight: FontWeight.w700, color: NaglyColors.textSecondary)),
                  ),
                ),
            ],
          ),
        ),
        const Padding(
          padding: EdgeInsets.only(top: 6),
          child: Text('Swipe left to remove an entry', textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: NaglyColors.textSecondary)),
        ),
      ],
    );
  }
}
