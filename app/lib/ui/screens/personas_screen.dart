import 'package:flutter/material.dart';

import '../../config/integrations.dart';

import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../../domain/access.dart';
import '../../domain/models.dart';
import '../../domain/persona_catalog.dart';
import '../../state/app_controller.dart';
import '../app.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';
import 'sheets.dart';

/// Two-step picker: relationship → voice. Locked voices open the rewarded-ad unlock.
class PersonasScreen extends StatefulWidget {
  const PersonasScreen({super.key});

  @override
  State<PersonasScreen> createState() => _PersonasScreenState();
}

class _PersonasScreenState extends State<PersonasScreen> {
  String? _relationship;

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final access = c.access;
    final relId = _relationship ?? c.persona.relationshipId;
    final rel = PersonaCatalog.relationship(relId);
    final relOpen = access.relationshipAccessible(relId);
    final adExpiry = access.adUnlockExpiry(relId);

    return DayBackground(
      hour: c.now.hour,
      intensity: 0.45,
      child: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 32),
          children: [
            Text('Personas', style: Theme.of(context).textTheme.headlineMedium),
            const SizedBox(height: 4),
            Text(
              access.isPro
                  ? 'Pro — every voice is yours 💛'
                  : access.inTrial
                  ? 'Everyone is free for ${access.trialDaysLeft} more day${access.trialDaysLeft == 1 ? '' : 's'}'
                  : switch (Integrations.monetizationMode) {
                      MonetizationMode.payments =>
                        'Mom is free forever. Go Pro for everyone else.',
                      MonetizationMode.ads =>
                        'Mom is free forever. Others: watch an ad for 24h.',
                      MonetizationMode.both => 'Mom is free forever. Others: watch an ad for 24h, or go Pro.',
                    },
              style: const TextStyle(
                fontWeight: FontWeight.w800,
                color: NaglyColors.textSecondary,
              ),
            ),
            const SizedBox(height: 16),
            NCard(
              child: Row(
                children: [
                  PersonaAvatar(emoji: c.persona.emoji, mood: c.mood, size: 56),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          c.persona.displayName,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        Text(
                          'Bond: ${c.bondLevel.emoji} ${c.bondLevel.label}',
                          style: const TextStyle(
                            fontWeight: FontWeight.w800,
                            color: NaglyColors.textSecondary,
                          ),
                        ),
                        const SizedBox(height: 6),
                        ClipRRect(
                          borderRadius: BorderRadius.circular(4),
                          child: LinearProgressIndicator(
                            value: c.bondProgress,
                            minHeight: 6,
                            color: NaglyColors.gold,
                            backgroundColor: NaglyColors.surfaceVariant,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          c.bondLevel.next == null
                              ? 'Soul Reminder. It doesn\'t get closer.'
                              : 'Next: ${c.bondLevel.next!.label} — keep the streak going',
                          style: const TextStyle(
                            fontSize: 12,
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
            const SectionLabel('1 · Relationship'),
            Row(
              children: [
                for (final r in PersonaCatalog.relationships)
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      child: _RelTile(
                        rel: r,
                        selected: r.id == relId,
                        locked: !access.relationshipAccessible(r.id),
                        onTap: () {
                          HapticFeedback.selectionClick();
                          setState(() => _relationship = r.id);
                        },
                      ),
                    ),
                  ),
              ],
            ),
            SectionLabel('2 · Pick your ${rel.displayName}'),
            if (adExpiry != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Tag(
                  '⏳ Unlocked by ad · ${countdownLabel(adExpiry, c.now.millisecondsSinceEpoch)}',
                  color: NaglyColors.primaryDeep,
                ),
              ),
            for (final p in PersonaCatalog.variantsOf(relId)) ...[
              _VariantCard(
                persona: p,
                selected: p.id == c.profile.personaId,
                locked: !relOpen,
                onTap: () {
                  if (!relOpen) {
                    showUnlockSheet(context, p);
                    return;
                  }
                  HapticFeedback.mediumImpact();
                  c.selectPersona(p.id);
                },
              ),
              const SizedBox(height: 10),
            ],
            const SectionLabel('3 · Make it yours'),
            _MakeItYoursCard(
              persona: c.persona,
              baseName: PersonaCatalog.get(c.profile.personaId).displayName,
              isCustom:
                  c.persona.displayName !=
                  PersonaCatalog.get(c.profile.personaId).displayName,
              locked: !access.fullAccess,
              onTap: () {
                HapticFeedback.selectionClick();
                if (!access.fullAccess) {
                  openPaywall(context, placement: 'custom_persona');
                  return;
                }
                showCustomIdentitySheet(context);
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _MakeItYoursCard extends StatelessWidget {
  const _MakeItYoursCard({
    required this.persona,
    required this.baseName,
    required this.isCustom,
    required this.locked,
    required this.onTap,
  });
  final Persona persona;
  final String baseName;
  final bool isCustom, locked;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => NCard(
    onTap: onTap,
    child: Row(
      children: [
        Text(
          isCustom ? persona.emoji : '✏️',
          style: const TextStyle(fontSize: 30),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                isCustom ? persona.displayName : 'Give them a real name',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w900,
                  color: NaglyColors.ink,
                ),
              ),
              Text(
                isCustom
                    ? "$baseName's voice · tap to edit"
                    : 'Hear "Lakshmi Amma" or "Priya" instead of "$baseName"',
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: NaglyColors.textSecondary,
                ),
              ),
            ],
          ),
        ),
        if (locked)
          const Tag('PRO', color: NaglyColors.accent)
        else
          const Icon(
            Icons.chevron_right_rounded,
            color: NaglyColors.textSecondary,
          ),
      ],
    ),
  );
}

class _RelTile extends StatelessWidget {
  const _RelTile({
    required this.rel,
    required this.selected,
    required this.locked,
    required this.onTap,
  });
  final Relationship rel;
  final bool selected, locked;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Semantics(
    selected: selected,
    button: true,
    label: '${rel.displayName}${locked ? ', locked' : ''}',
    excludeSemantics: true,
    child: GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: selected
              ? NaglyColors.primary.withValues(alpha: 0.14)
              : Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: selected ? NaglyColors.primaryDeep : NaglyColors.outline,
            width: selected ? 2 : 1,
          ),
        ),
        child: Column(
          children: [
            Stack(
              clipBehavior: Clip.none,
              children: [
                Text(rel.emoji, style: const TextStyle(fontSize: 30)),
                if (locked)
                  const Positioned(
                    right: -12,
                    top: -6,
                    child: Text('🔒', style: TextStyle(fontSize: 13)),
                  ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              rel.id == 'grandparent' ? 'Grandma' : rel.displayName,
              style: const TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w900,
                color: NaglyColors.ink,
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _VariantCard extends StatelessWidget {
  const _VariantCard({
    required this.persona,
    required this.selected,
    required this.locked,
    required this.onTap,
  });
  final Persona persona;
  final bool selected, locked;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => AnimatedContainer(
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
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              PersonaAvatar(
                emoji: persona.emoji,
                mood: selected ? Mood.proud : Mood.neutral,
                size: 52,
                animate: selected,
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      persona.displayName,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w900,
                        color: NaglyColors.ink,
                      ),
                    ),
                    Text(
                      '"${persona.signature}"',
                      style: const TextStyle(
                        fontWeight: FontWeight.w700,
                        color: NaglyColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              if (locked)
                const Tag('🔒 Unlock', color: NaglyColors.accent)
              else if (selected)
                const Icon(
                  Icons.check_circle_rounded,
                  color: NaglyColors.primaryDeep,
                ),
            ],
          ),
        ),
      ),
    ),
  );
}
