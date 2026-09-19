import 'dart:math';

import 'package:flutter/material.dart';

import '../../domain/models.dart';
import '../../domain/relationship_meter.dart';
import '../theme.dart';

/// Emoji face in a mood-colored ring that *acts* its mood:
/// proud bounces, worried wobbles, disappointed sighs and sinks.
class PersonaAvatar extends StatefulWidget {
  const PersonaAvatar({super.key, required this.emoji, this.mood = Mood.neutral, this.size = 88, this.animate = true});

  final String emoji;
  final Mood mood;
  final double size;
  final bool animate;

  @override
  State<PersonaAvatar> createState() => _PersonaAvatarState();
}

class _PersonaAvatarState extends State<PersonaAvatar> with SingleTickerProviderStateMixin {
  late final AnimationController _c = AnimationController(vsync: this, duration: _durationFor(widget.mood));

  static Duration _durationFor(Mood m) => switch (m) {
        Mood.proud => const Duration(milliseconds: 900),
        Mood.worried => const Duration(milliseconds: 1400),
        Mood.disappointed => const Duration(milliseconds: 3200),
        Mood.neutral => const Duration(milliseconds: 3600),
      };

  @override
  void initState() {
    super.initState();
    if (widget.animate) _c.repeat();
  }

  @override
  void didUpdateWidget(covariant PersonaAvatar old) {
    super.didUpdateWidget(old);
    if (old.mood != widget.mood) {
      _c.duration = _durationFor(widget.mood);
      if (widget.animate) _c.repeat();
    }
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ring = NaglyColors.mood(widget.mood);
    return AnimatedBuilder(
      animation: _c,
      builder: (context, child) {
        final t = _c.value * 2 * pi;
        var dy = 0.0, angle = 0.0, scale = 1.0;
        switch (widget.mood) {
          case Mood.proud:
            dy = -(sin(t).abs()) * widget.size * 0.08;
            scale = 1 + sin(t).abs() * 0.04;
          case Mood.worried:
            angle = sin(t * 2) * 0.06;
          case Mood.disappointed:
            dy = (sin(t) + 1) * widget.size * 0.025;
            angle = -0.05;
          case Mood.neutral:
            scale = 1 + sin(t) * 0.02;
        }
        return Transform.translate(
          offset: Offset(0, dy),
          child: Transform.rotate(angle: angle, child: Transform.scale(scale: scale, child: child)),
        );
      },
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 500),
        width: widget.size,
        height: widget.size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFFFFBF2), Color(0xFFFFF0D9)],
          ),
          border: Border.all(color: ring, width: widget.size * 0.055),
          boxShadow: [BoxShadow(color: ring.withValues(alpha: 0.55), blurRadius: widget.size * 0.3, spreadRadius: 1)],
        ),
        alignment: Alignment.center,
        child: Text(widget.emoji, style: TextStyle(fontSize: widget.size * 0.5)),
      ),
    );
  }
}

/// Persona speech bubble with a little tail. Text changes cross-fade after a
/// brief "typing…" beat so it feels like she's actually replying.
class SpeechBubble extends StatefulWidget {
  const SpeechBubble({super.key, required this.text, this.name, this.onTap, this.tail = true, this.accent});

  final String text;
  final String? name;
  final VoidCallback? onTap;
  final bool tail;
  final Color? accent;

  @override
  State<SpeechBubble> createState() => _SpeechBubbleState();
}

class _SpeechBubbleState extends State<SpeechBubble> {
  late String _shown = widget.text;
  bool _typing = false;

  @override
  void didUpdateWidget(covariant SpeechBubble old) {
    super.didUpdateWidget(old);
    if (old.text != widget.text) {
      setState(() => _typing = true);
      Future.delayed(const Duration(milliseconds: 550), () {
        if (!mounted) return;
        setState(() {
          _typing = false;
          _shown = widget.text;
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Semantics(
      liveRegion: true,
      label: '${widget.name ?? 'Persona'} says: $_shown',
      button: widget.onTap != null,
      excludeSemantics: true,
      child: GestureDetector(
        onTap: widget.onTap,
        child: CustomPaint(
          painter: widget.tail ? _TailPainter() : null,
          child: AnimatedSize(
            duration: const Duration(milliseconds: 250),
            curve: Curves.easeOut,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(18, 14, 18, 16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(22),
                border: Border.all(color: NaglyColors.outline),
                boxShadow: const [BoxShadow(color: Color(0x14122730), blurRadius: 18, offset: Offset(0, 8))],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (widget.name != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: Text(widget.name!,
                          style: TextStyle(
                              fontSize: 12.5, fontWeight: FontWeight.w900, color: widget.accent ?? NaglyColors.brand)),
                    ),
                  AnimatedSwitcher(
                    duration: const Duration(milliseconds: 250),
                    child: _typing
                        ? const _TypingDots(key: ValueKey('typing'))
                        : Text(
                            _shown,
                            key: ValueKey(_shown),
                            style: const TextStyle(
                                fontSize: 18, fontWeight: FontWeight.w800, color: NaglyColors.ink, height: 1.3),
                          ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _TailPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final p = Path()
      ..moveTo(size.width / 2 - 12, 1)
      ..lineTo(size.width / 2, -12)
      ..lineTo(size.width / 2 + 12, 1)
      ..close();
    canvas.drawPath(p, Paint()..color = Colors.white);
    canvas.drawPath(
        Path()
          ..moveTo(size.width / 2 - 12, 0.5)
          ..lineTo(size.width / 2, -12)
          ..lineTo(size.width / 2 + 12, 0.5),
        Paint()
          ..style = PaintingStyle.stroke
          ..color = NaglyColors.outline);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _TypingDots extends StatefulWidget {
  const _TypingDots({super.key});

  @override
  State<_TypingDots> createState() => _TypingDotsState();
}

class _TypingDotsState extends State<_TypingDots> with SingleTickerProviderStateMixin {
  late final AnimationController _c = AnimationController(vsync: this, duration: const Duration(milliseconds: 900))
    ..repeat();

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => SizedBox(
        height: 23,
        child: AnimatedBuilder(
          animation: _c,
          builder: (context, _) => Row(
            mainAxisSize: MainAxisSize.min,
            children: List.generate(3, (i) {
              final v = sin((_c.value * 2 * pi) - i * 0.8).clamp(0.0, 1.0);
              return Container(
                margin: const EdgeInsets.symmetric(horizontal: 3),
                width: 8,
                height: 8,
                transform: Matrix4.translationValues(0, -4 * v, 0),
                decoration: BoxDecoration(
                  color: NaglyColors.textSecondary.withValues(alpha: 0.4 + 0.6 * v),
                  shape: BoxShape.circle,
                ),
              );
            }),
          ),
        ),
      );
}

/// "💛 Close" chip with a thin progress bar toward the next bond level.
class BondChip extends StatelessWidget {
  const BondChip({super.key, required this.level, required this.progress, this.onTap});

  final RelationshipLevel level;
  final double progress;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final next = level.next;
    return Tooltip(
      message: next == null ? 'Maximum bond' : 'Keep your streak going to reach ${next.label}',
      child: Semantics(
        label: 'Bond level ${level.label}',
        child: Container(
          padding: const EdgeInsets.fromLTRB(12, 8, 12, 8),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: NaglyColors.outline),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('${level.emoji} ${level.label}',
                  style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w900, color: NaglyColors.ink)),
              const SizedBox(height: 6),
              SizedBox(
                width: 84,
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: TweenAnimationBuilder<double>(
                    tween: Tween(end: progress),
                    duration: const Duration(milliseconds: 800),
                    builder: (context, v, _) => LinearProgressIndicator(
                      value: v,
                      minHeight: 5,
                      backgroundColor: NaglyColors.surfaceVariant,
                      color: NaglyColors.gold,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
