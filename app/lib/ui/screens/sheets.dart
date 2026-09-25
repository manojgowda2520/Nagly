import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../../config/integrations.dart';
import '../../domain/access.dart';
import '../../domain/models.dart';
import '../../domain/mood_engine.dart';
import '../../domain/persona_catalog.dart';
import '../../services/ads.dart';
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
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, set) => Padding(
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
              existing == null ? 'Add medication' : 'Edit medication',
              style: Theme.of(ctx).textTheme.titleLarge,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: name,
              autofocus: existing == null,
              textCapitalization: TextCapitalization.sentences,
              decoration: const InputDecoration(
                labelText: 'Name',
                hintText: 'e.g. Vitamin D',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: dose,
              decoration: const InputDecoration(
                labelText: 'Dose (optional)',
                hintText: 'e.g. 1 tablet',
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
                child: const Text('Remove medication'),
              ),
          ],
        ),
      ),
    ),
  );
}

// ── Locked persona: watch an ad (24h) or go Pro ────────────────
Future<void> showUnlockSheet(BuildContext context, Persona persona) async {
  final rel = PersonaCatalog.relationshipOf(persona);
  if (!Integrations.adsEnabled) {
    openPaywall(context, placement: 'persona_locked', relationshipId: rel.id);
    return;
  }
  return showModalBottomSheet<void>(
    context: context,
    builder: (ctx) => Padding(
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          PersonaAvatar(emoji: persona.emoji, mood: Mood.proud, size: 84),
          const SizedBox(height: 14),
          SpeechBubble(text: persona.signature, name: persona.displayName),
          const SizedBox(height: 18),
          Text(
            '🎁 Try ${rel.displayName} free',
            style: Theme.of(ctx).textTheme.titleLarge,
          ),
          const SizedBox(height: 6),
          Text(
            'Watch one short ad to unlock all of ${rel.displayName}\'s voices for 24 hours.',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontWeight: FontWeight.w700,
              color: NaglyColors.textSecondary,
            ),
          ),
          const SizedBox(height: 18),
          FilledButton.icon(
            onPressed: () async {
              Navigator.pop(ctx);
              await watchAdToUnlock(context, persona);
            },
            icon: const Icon(Icons.play_circle_fill_rounded),
            label: const Text('Watch ad · unlock 24h'),
          ),
          if (Integrations.purchasesEnabled)
            TextButton(
              onPressed: () {
                Navigator.pop(ctx);
                openPaywall(
                  context,
                  placement: 'persona_locked',
                  relationshipId: rel.id,
                );
              },
              child: const Text('Or go Pro to keep them forever →'),
            ),
        ],
      ),
    ),
  );
}

Future<void> watchAdToUnlock(BuildContext context, Persona persona) =>
    watchAdFor(
      context,
      key: persona.relationshipId,
      label: persona.displayName,
      onGranted: (c) => c.selectPersona(persona.id),
    );

/// Play a rewarded ad and, if it's watched to the end, grant a 24h unlock of [key]
/// (a relationship id or [medsUnlockKey]).
Future<void> watchAdFor(
  BuildContext context, {
  required String key,
  required String label,
  Future<void> Function(AppController c)? onGranted,
}) async {
  final c = context.read<AppController>();
  final messenger = ScaffoldMessenger.of(context);
  final placement = 'unlock_$key';
  final Future<AdResult> result;
  if (c.ads case final FakeAdService fake) {
    result = fake.showRewarded(placement: placement);
    unawaited(
      Navigator.of(context).push(
        PageRouteBuilder<void>(
          opaque: true,
          pageBuilder: (_, _, _) =>
              _FakeAdScreen(ads: fake, personaName: label),
        ),
      ),
    );
  } else {
    result = c.ads.showRewarded(placement: placement);
  }
  switch (await result) {
    case AdResult.rewarded:
      await c.grantAdUnlock(key);
      await onGranted?.call(c);
      HapticFeedback.heavyImpact();
      messenger.showSnackBar(
        SnackBar(content: Text('$label unlocked for 24 hours 💛')),
      );
    case AdResult.cancelled:
      messenger.showSnackBar(
        const SnackBar(content: Text('Ad closed early — no unlock this time')),
      );
    case AdResult.unavailable:
      messenger.showSnackBar(
        const SnackBar(
          content: Text('No ad available right now. Try again in a bit.'),
        ),
      );
  }
}

/// Plan B "store": every Pro feature, one short ad away, for 24 hours.
Future<void> showAdUnlockHub(
  BuildContext context, {
  String? focusRelationshipId,
  bool medsFirst = false,
}) {
  final c = context.read<AppController>();
  final a = c.access;
  final rels =
      PersonaCatalog.relationships
          .where((r) => !a.relationshipAccessible(r.id))
          .toList()
        ..sort(
          (x, y) =>
              (y.id == focusRelationshipId ? 1 : 0) -
              (x.id == focusRelationshipId ? 1 : 0),
        );
  final medsLocked = a.medicationLimit != null;
  Widget row(
    BuildContext ctx,
    String emoji,
    String title,
    String sub,
    Future<void> Function() onTap,
  ) => Padding(
    padding: const EdgeInsets.only(bottom: 10),
    child: NCard(
      onTap: () async {
        Navigator.pop(ctx);
        await onTap();
      },
      child: Row(
        children: [
          Text(emoji, style: const TextStyle(fontSize: 28)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                Text(
                  sub,
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    color: NaglyColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          const Tag('▶ 24h', color: NaglyColors.primaryDeep),
        ],
      ),
    ),
  );
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    builder: (ctx) => SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              '🎁 Unlock with a short ad',
              style: Theme.of(ctx).textTheme.titleLarge,
            ),
            const SizedBox(height: 6),
            const Text(
              'Nagly is free. Watch one ad to borrow any of these for 24 hours.',
              style: TextStyle(
                fontWeight: FontWeight.w700,
                color: NaglyColors.textSecondary,
              ),
            ),
            const SizedBox(height: 16),
            if (medsLocked && medsFirst)
              row(
                ctx,
                '💊',
                'Unlimited medications',
                'Remind me about every pill',
                () => watchAdFor(
                  context,
                  key: medsUnlockKey,
                  label: 'Unlimited medications',
                ),
              ),
            for (final r in rels)
              row(
                ctx,
                r.emoji,
                r.displayName,
                r.tagline,
                () => watchAdFor(
                  context,
                  key: r.id,
                  label: r.displayName,
                  onGranted: (c) =>
                      c.selectPersona(PersonaCatalog.variantsOf(r.id).first.id),
                ),
              ),
            if (medsLocked && !medsFirst)
              row(
                ctx,
                '💊',
                'Unlimited medications',
                'Remind me about every pill',
                () => watchAdFor(
                  context,
                  key: medsUnlockKey,
                  label: 'Unlimited medications',
                ),
              ),
            if (rels.isEmpty && !medsLocked)
              const Padding(
                padding: EdgeInsets.all(16),
                child: Text(
                  'Everything is unlocked right now 💛',
                  textAlign: TextAlign.center,
                ),
              ),
          ],
        ),
      ),
    ),
  );
}

/// Sandbox stand-in for a rewarded video: 5s countdown, closable (forfeits reward).
class _FakeAdScreen extends StatefulWidget {
  const _FakeAdScreen({required this.ads, required this.personaName});
  final FakeAdService ads;
  final String personaName;

  @override
  State<_FakeAdScreen> createState() => _FakeAdScreenState();
}

class _FakeAdScreenState extends State<_FakeAdScreen>
    with SingleTickerProviderStateMixin {
  late final AnimationController _c =
      AnimationController(vsync: this, duration: const Duration(seconds: 5))
        ..forward().whenComplete(() {
          if (!mounted) return;
          widget.ads.complete();
          Navigator.pop(context);
        });

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => PopScope(
    onPopInvokedWithResult: (didPop, _) {
      if (_c.isAnimating) widget.ads.cancel();
    },
    child: Scaffold(
      backgroundColor: NaglyColors.ink,
      body: SafeArea(
        child: Stack(
          children: [
            Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('📺', style: TextStyle(fontSize: 64)),
                  const SizedBox(height: 12),
                  const Text(
                    'Sandbox ad',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    'Unlocking ${widget.personaName}…',
                    style: const TextStyle(
                      color: Colors.white70,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: 200,
                    child: AnimatedBuilder(
                      animation: _c,
                      builder: (_, _) => LinearProgressIndicator(
                        value: _c.value,
                        color: NaglyColors.primary,
                        backgroundColor: Colors.white24,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Positioned(
              right: 8,
              top: 8,
              child: IconButton(
                tooltip: 'Close ad',
                color: Colors.white,
                icon: const Icon(Icons.close),
                onPressed: () {
                  _c.stop();
                  widget.ads.cancel();
                  Navigator.pop(context);
                },
              ),
            ),
          ],
        ),
      ),
    ),
  );
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
                : '"You\'ve had all of me free for 7 days. Keep me around? Water and one medication stay free — but I\'ll miss the rest."',
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
            child: Text(
              Integrations.purchasesEnabled
                  ? 'See plans'
                  : 'Unlock with a short ad',
            ),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Keep free (water + 1 pill)'),
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
        '$streak-day streak! Unlock every persona and unlimited medication reminders — try the Annual plan free for 7 days.',
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
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, set) {
        final preview = name.text.trim().isEmpty
            ? base.displayName
            : name.text.trim();
        return Padding(
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
                onChanged: (_) => set(() {}),
                decoration: const InputDecoration(
                  labelText: 'Name',
                  hintText: 'e.g. Lakshmi Amma, Priya, Papa',
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
