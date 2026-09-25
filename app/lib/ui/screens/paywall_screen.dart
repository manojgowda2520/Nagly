import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../config/integrations.dart';
import '../../domain/models.dart';
import '../../domain/persona_catalog.dart';
import '../../services/billing.dart';
import '../../state/app_controller.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';

/// Lifetime-first, three-plan paywall. It's *personal*: when opened from a locked
/// persona, that persona is the one asking you to keep them around.
class PaywallScreen extends StatefulWidget {
  const PaywallScreen({
    super.key,
    required this.placement,
    this.relationshipId,
  });

  final String placement;
  final String? relationshipId;

  @override
  State<PaywallScreen> createState() => _PaywallScreenState();
}

class _PaywallScreenState extends State<PaywallScreen> {
  late final Future<List<Plan>> _plans = context
      .read<AppController>()
      .billing
      .plans(placement: widget.placement);
  PlanKind _selected = PlanKind.lifetime;
  bool _busy = false;

  Persona get _pleader {
    final c = context.read<AppController>();
    if (widget.relationshipId != null)
      return PersonaCatalog.variantsOf(widget.relationshipId!).first;
    return c.persona;
  }

  String get _headline => switch (widget.placement) {
    'persona_locked' || 'persona_expired' =>
      'Keep ${PersonaCatalog.relationship(_pleader.relationshipId).displayName} around',
    'medication_limit' => 'Every pill, remembered',
    'custom_persona' => 'Make them yours',
    'trial_end' => 'Keep the whole family',
    _ => 'Unlock every nagger',
  };

  String get _plea => switch (widget.placement) {
    'medication_limit' => "Your first pill stays free forever. Let me remind you about the rest too.",
    'custom_persona' => "Call me by the name you'd actually hear. Amma, Papa, Priya — your choice.",
    'persona_locked' || 'persona_expired' => _pleader.signature,
    _ => "Water and one medication stay free forever. Pro is for the whole family.",
  };

  Future<void> _buy(Plan plan) async {
    setState(() => _busy = true);
    HapticFeedback.mediumImpact();
    final outcome = await context.read<AppController>().purchase(plan);
    if (!mounted) return;
    setState(() => _busy = false);
    switch (outcome) {
      case PurchaseOutcome.success:
        HapticFeedback.heavyImpact();
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Welcome to Pro 💛 Everyone\'s here now.'),
          ),
        );
      case PurchaseOutcome.cancelled:
        break;
      case PurchaseOutcome.failed:
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Purchase didn\'t go through. You were not charged.'),
          ),
        );
    }
  }

  Future<void> _restore() async {
    setState(() => _busy = true);
    final ok = await context.read<AppController>().restore();
    if (!mounted) return;
    setState(() => _busy = false);
    if (ok) Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          ok
              ? 'Pro restored 💛'
              : 'No previous purchase found for this account.',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final p = _pleader;
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            stops: [0, 0.45],
            colors: [Color(0xFFDDF4FC), NaglyColors.background],
          ),
        ),
        child: SafeArea(
          child: FutureBuilder<List<Plan>>(
            future: _plans,
            builder: (context, snap) {
              final plans = snap.data ?? const <Plan>[];
              final selected =
                  plans.where((pl) => pl.kind == _selected).firstOrNull ??
                  plans.firstOrNull;
              return Column(
                children: [
                  Align(
                    alignment: Alignment.centerLeft,
                    child: IconButton(
                      tooltip: 'Close',
                      onPressed: () => Navigator.pop(context),
                      icon: const Icon(Icons.close_rounded),
                    ),
                  ),
                  Expanded(
                    child: ListView(
                      padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
                      children: [
                        Text(
                          _headline,
                          style: Theme.of(context).textTheme.headlineMedium,
                        ),
                        const SizedBox(height: 14),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            PersonaAvatar(
                              emoji: p.emoji,
                              mood: Mood.proud,
                              size: 56,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: SpeechBubble(
                                text: _plea,
                                name: p.displayName,
                                tail: false,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),
                        const _Perk(
                          emoji: '🧔',
                          text: 'All 9 extra voices: Dad, Grandparent & Bestie',
                        ),
                        const _Perk(
                          emoji: '💊',
                          text: 'Unlimited medication reminders',
                        ),
                        const _Perk(
                          emoji: '🌱',
                          text: 'Water + your first pill stay free, always',
                        ),
                        const SizedBox(height: 14),
                        if (snap.connectionState != ConnectionState.done)
                          const Padding(
                            padding: EdgeInsets.all(24),
                            child: Center(child: CircularProgressIndicator()),
                          )
                        else
                          for (final plan in plans) ...[
                            _PlanCard(
                              plan: plan,
                              selected: plan.kind == selected?.kind,
                              onTap: () {
                                HapticFeedback.selectionClick();
                                setState(() => _selected = plan.kind);
                              },
                            ),
                            const SizedBox(height: 10),
                          ],
                      ],
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(24, 0, 24, 8),
                    child: Column(
                      children: [
                        FilledButton(
                          onPressed: _busy || selected == null
                              ? null
                              : () => _buy(selected),
                          child: _busy
                              ? const SizedBox(
                                  width: 22,
                                  height: 22,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 3,
                                    color: Colors.white,
                                  ),
                                )
                              : Text(
                                  selected?.kind == PlanKind.annual
                                      ? 'Start 7-day free trial'
                                      : 'Go Pro',
                                ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          selected?.kind == PlanKind.lifetime
                              ? 'One payment. No subscription.'
                              : 'Renews automatically. Cancel any time in Google Play.',
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: NaglyColors.textSecondary,
                          ),
                        ),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            TextButton(
                              onPressed: _busy ? null : _restore,
                              child: const Text('Restore'),
                            ),
                            TextButton(
                              onPressed: () =>
                                  launchUrl(Uri.parse(Integrations.termsUrl)),
                              child: const Text('Terms'),
                            ),
                            TextButton(
                              onPressed: () =>
                                  launchUrl(Uri.parse(Integrations.privacyUrl)),
                              child: const Text('Privacy'),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _Perk extends StatelessWidget {
  const _Perk({required this.emoji, required this.text});
  final String emoji, text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 3),
    child: Row(
      children: [
        Container(
          width: 36,
          height: 36,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: NaglyColors.outline),
          ),
          child: Text(emoji, style: const TextStyle(fontSize: 18)),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w800,
              color: NaglyColors.ink,
            ),
          ),
        ),
      ],
    ),
  );
}

class _PlanCard extends StatelessWidget {
  const _PlanCard({
    required this.plan,
    required this.selected,
    required this.onTap,
  });
  final Plan plan;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Semantics(
    selected: selected,
    button: true,
    label: '${plan.title}, ${plan.price}. ${plan.detail}',
    excludeSemantics: true,
    child: GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 13),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: selected ? NaglyColors.primaryDeep : NaglyColors.outline,
            width: selected ? 2.5 : 1,
          ),
          boxShadow: selected
              ? [
                  BoxShadow(
                    color: NaglyColors.primary.withValues(alpha: 0.3),
                    blurRadius: 18,
                    offset: const Offset(0, 6),
                  ),
                ]
              : null,
        ),
        child: Row(
          children: [
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: 22,
              height: 22,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: selected ? NaglyColors.primaryDeep : Colors.transparent,
                border: Border.all(
                  color: selected
                      ? NaglyColors.primaryDeep
                      : NaglyColors.outline,
                  width: 2,
                ),
              ),
              child: selected
                  ? const Icon(Icons.check, size: 14, color: Colors.white)
                  : null,
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    plan.title,
                    style: const TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w900,
                      color: NaglyColors.ink,
                    ),
                  ),
                  Text(
                    '${plan.price} · ${plan.detail}',
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: NaglyColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            if (plan.badge != null)
              Tag(
                plan.badge!,
                color: plan.kind == PlanKind.lifetime
                    ? NaglyColors.brand
                    : NaglyColors.success,
                filled: plan.kind == PlanKind.lifetime,
              ),
          ],
        ),
      ),
    ),
  );
}
