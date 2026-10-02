import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../domain/persona_catalog.dart';
import '../../state/app_controller.dart';
import '../app.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';

// ── Custom amount ─────────────────────────────────────────────
Future<int?> showCustomAmountSheet(BuildContext context) {
  final c = context.read<AppController>();
  final unit = c.profile.volumeUnit;
  var ml = c.recentCustomMl ?? 350;
  return showModalBottomSheet<int>(
    context: context,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, set) => Padding(
        padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('How much?', style: Theme.of(ctx).textTheme.titleLarge),
            const SizedBox(height: 12),
            Text(
              formatVolume(ml, unit),
              style: Theme.of(ctx).textTheme.displaySmall
                  ?.copyWith(color: NaglyColors.primaryDeep),
            ),
            Slider(
              value: ml.toDouble(),
              min: 50,
              max: 1500,
              divisions: 29,
              label: formatVolume(ml, unit),
              onChanged: (v) {
                HapticFeedback.selectionClick();
                set(() => ml = v.round());
              },
            ),
            Wrap(
              spacing: 8,
              children: [
                for (final preset in const [100, 330, 750, 1000])
                  ChoiceChip(
                    label: Text(formatVolume(preset, unit)),
                    selected: ml == preset,
                    onSelected: (_) => set(() => ml = preset),
                  ),
              ],
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: () => Navigator.pop(ctx, ml),
              child: Text('Log ${formatVolume(ml, unit)}'),
            ),
          ],
        ),
      ),
    ),
  );
}

// ── Quick picks for the med / supplement name field ──────────
/// Nagly's "medication" mode is for anything you take on schedule: BP tablets,
/// vitamins, creatine, protein shakes. One tap fills the name (and a dose hint).
const medQuickPicks = <(String, String, String)>[
  ('💊', 'BP tablet', '1 tablet'),
  ('☀️', 'Vitamin D', '1 capsule'),
  ('💪', 'Creatine', '5 g'),
  ('🥤', 'Protein shake', '1 scoop'),
  ('🐟', 'Omega-3', '1 capsule'),
  ('🍊', 'Multivitamin', '1 tablet'),
];

class MedQuickPicks extends StatelessWidget {
  const MedQuickPicks({super.key, required this.onPick});

  /// Called with (name, dose).
  final void Function(String name, String dose) onPick;

  @override
  Widget build(BuildContext context) => Wrap(
    spacing: 8,
    runSpacing: 8,
    children: [
      for (final (emoji, name, dose) in medQuickPicks)
        ActionChip(
          avatar: Text(emoji),
          label: Text(name),
          onPressed: () {
            HapticFeedback.selectionClick();
            onPick(name, dose);
          },
        ),
    ],
  );
}

// ── Medication editor ─────────────────────────────────────────
Future<void> showMedicationEditor(
  BuildContext context, {
  Medication? existing,
}) {
  final c = context.read<AppController>();
  final name = TextEditingController(text: existing?.name ?? '');
  final dose = TextEditingController(text: existing?.dose ?? '');
  var time = TimeOfDay(
    hour: existing?.hour ?? 9,
    minute: existing?.minute ?? 0,
  );
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, set) => SingleChildScrollView(
        padding: EdgeInsets.fromLTRB(
          24,
          0,
          24,
          24 + MediaQuery.of(ctx).viewInsets.bottom,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              existing == null ? 'Add med or supplement' : 'Edit reminder',
              style: Theme.of(ctx).textTheme.titleLarge,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: name,
              autofocus: existing == null,
              textCapitalization: TextCapitalization.sentences,
              decoration: const InputDecoration(
                labelText: 'Name',
                hintText: 'e.g. Vitamin D, Creatine',
                border: OutlineInputBorder(),
              ),
            ),
            if (existing == null) ...[
              const SizedBox(height: 10),
              MedQuickPicks(
                onPick: (n, d) => set(() {
                  name.text = n;
                  if (dose.text.trim().isEmpty) dose.text = d;
                }),
              ),
            ],
            const SizedBox(height: 12),
            TextField(
              controller: dose,
              decoration: const InputDecoration(
                labelText: 'Dose (optional)',
                hintText: 'e.g. 1 tablet, 1 scoop',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            NCard(
              onTap: () async {
                final t = await showTimePicker(context: ctx, initialTime: time);
                if (t != null) set(() => time = t);
              },
              child: Row(
                children: [
                  const Text('⏰', style: TextStyle(fontSize: 20)),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Text(
                      'Daily reminder',
                      style: TextStyle(fontWeight: FontWeight.w800),
                    ),
                  ),
                  Text(
                    time.format(ctx),
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                      color: NaglyColors.med,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            ListenableBuilder(
              listenable: name,
              builder: (_, _) => FilledButton(
                style: FilledButton.styleFrom(backgroundColor: NaglyColors.med),
                onPressed: name.text.trim().isEmpty
                    ? null
                    : () async {
                        Navigator.pop(ctx);
                        if (existing == null) {
                          await c.addMedication(
                            name.text.trim(),
                            time.hour,
                            time.minute,
                            dose: dose.text.trim(),
                          );
                        } else {
                          await c.updateMedication(
                            existing.copyWith(
                              name: name.text.trim(),
                              dose: dose.text.trim(),
                              hour: time.hour,
                              minute: time.minute,
                            ),
                          );
                        }
                      },
                child: const Text('Save'),
              ),
            ),
            if (existing != null)
              TextButton(
                style: TextButton.styleFrom(
                  foregroundColor: NaglyColors.danger,
                ),
                onPressed: () {
                  Navigator.pop(ctx);
                  c.removeMedication(existing.id);
                },
                child: const Text('Remove reminder'),
              ),
          ],
        ),
      ),
    ),
  );
}

// ── Locked persona: go Pro ────────────────
Future<void> showUnlockSheet(BuildContext context, Persona persona) async {
  final rel = PersonaCatalog.relationshipOf(persona);
  openPaywall(context, placement: 'persona_locked', relationshipId: rel.id);
}

// ── Trial ending / ended, in her voice ──────────────────────────
Future<void> showTrialEndingSheet(
  BuildContext context, {
  required bool ended,
  String? departedPersona,
}) {
  final c = context.read<AppController>();
  final p = c.persona;
  return showModalBottomSheet<void>(
    context: context,
    builder: (ctx) => Padding(
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Stack(
            clipBehavior: Clip.none,
            children: [
              PersonaAvatar(emoji: p.emoji, mood: Mood.disappointed, size: 84),
              const Positioned(
                right: -6,
                bottom: -4,
                child: Text('🥺', style: TextStyle(fontSize: 30)),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Text(
            ended
                ? 'My full care plan has ended'
                : 'My full care plan ends today',
            textAlign: TextAlign.center,
            style: Theme.of(ctx).textTheme.titleLarge,
          ),
          const SizedBox(height: 10),
          Text(
            ended
                ? '${departedPersona != null ? '$departedPersona had to go for now. ' : ''}"I\'m still here for your water — and your most important pill. Always free. But I\'ll miss the rest of the family."'
                : '"You\'ve had all of me free for 7 days. Keep me around? Water and one med or supplement stay free — but I\'ll miss the rest."',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w700,
              color: NaglyColors.textSecondary,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              openPaywall(context, placement: 'trial_end');
            },
            child: const Text('See plans'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Keep free (water + 1 reminder)'),
          ),
        ],
      ),
    ),
  );
}

Future<void> showUpsellDialog(BuildContext context, int streak) {
  final p = context.read<AppController>().persona;
  return showDialog<void>(
    context: context,
    builder: (ctx) => AlertDialog(
      icon: const Text('💛', style: TextStyle(fontSize: 44)),
      title: Text(
        '${PersonaCatalog.relationshipOf(p).displayName} misses nagging you fully',
        textAlign: TextAlign.center,
      ),
      content: Text(
        '$streak-day streak! Unlock every persona and unlimited med & supplement reminders — try the Annual plan free for 7 days.',
        textAlign: TextAlign.center,
      ),
      actionsAlignment: MainAxisAlignment.center,
      actions: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            FilledButton(
              onPressed: () {
                Navigator.pop(ctx);
                openPaywall(context, placement: 'streak_upsell');
              },
              child: const Text('See Pro'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Maybe later'),
            ),
          ],
        ),
      ],
    ),
  );
}

// ── Make it yours (custom name + emoji, Pro) ─────────────────
Future<void> showCustomIdentitySheet(BuildContext context) {
  final c = context.read<AppController>();
  final base = PersonaCatalog.get(c.profile.personaId);
  final name = TextEditingController(text: c.profile.customName);
  var emoji = c.profile.customEmoji.isEmpty
      ? base.emoji
      : c.profile.customEmoji;
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, set) {
        final preview = name.text.trim().isEmpty
            ? base.displayName
            : name.text.trim();
        return SingleChildScrollView(
          padding: EdgeInsets.fromLTRB(
            24,
            0,
            24,
            24 + MediaQuery.of(ctx).viewInsets.bottom,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Make it yours', style: Theme.of(ctx).textTheme.titleLarge),
              const SizedBox(height: 4),
              Text(
                "${base.displayName}'s voice, under the name you'd actually hear.",
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: NaglyColors.textSecondary,
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: name,
                autofocus: true,
                maxLength: PersonaCatalog.customNameMaxLength,
                textCapitalization: TextCapitalization.words,
                // Names like "Subbu Amma" or "Nani" get "corrected" otherwise.
                autocorrect: false,
                enableSuggestions: false,
                onChanged: (_) => set(() {}),
                decoration: const InputDecoration(
                  labelText: 'Name',
                  hintText: 'e.g. Subbu Amma, Priya, Papa',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 4),
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: [
                  for (final e in {base.emoji, ...PersonaCatalog.customEmojis})
                    Semantics(
                      button: true,
                      selected: e == emoji,
                      label: 'Emoji $e',
                      child: GestureDetector(
                        onTap: () {
                          HapticFeedback.selectionClick();
                          set(() => emoji = e);
                        },
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 150),
                          width: 44,
                          height: 44,
                          alignment: Alignment.center,
                          decoration: BoxDecoration(
                            color: e == emoji
                                ? NaglyColors.primary.withValues(alpha: 0.18)
                                : Colors.white,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: e == emoji
                                  ? NaglyColors.primaryDeep
                                  : NaglyColors.outline,
                              width: e == emoji ? 2 : 1,
                            ),
                          ),
                          child: Text(e, style: const TextStyle(fontSize: 24)),
                        ),
                      ),
                    ),
                ],
              ),
              const SizedBox(height: 16),
              NCard(
                child: Row(
                  children: [
                    Text(emoji, style: const TextStyle(fontSize: 28)),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            preview,
                            style: const TextStyle(
                              fontWeight: FontWeight.w900,
                              color: NaglyColors.ink,
                            ),
                          ),
                          Text(
                            base.signature,
                            style: const TextStyle(
                              fontWeight: FontWeight.w700,
                              color: NaglyColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () {
                  HapticFeedback.mediumImpact();
                  c.setCustomIdentity(name: name.text, emoji: emoji);
                  Navigator.pop(ctx);
                },
                child: const Text('Save'),
              ),
              if (c.profile.customName.isNotEmpty)
                TextButton(
                  onPressed: () {
                    c.setCustomIdentity(name: '', emoji: '');
                    Navigator.pop(ctx);
                  },
                  child: Text('Use "${base.displayName}" again'),
                ),
            ],
          ),
        );
      },
    ),
  );
}
