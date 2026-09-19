import 'dart:math';

import 'package:flutter/material.dart';

import '../theme.dart';

/// Screen background: a soft day-part tint fading into white.
class DayBackground extends StatelessWidget {
  const DayBackground({super.key, required this.hour, required this.child, this.intensity = 1});

  final int hour;
  final double intensity;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(seconds: 2),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          stops: const [0, 0.55],
          colors: [
            NaglyColors.dayTint(hour).withValues(alpha: 0.85 * intensity),
            NaglyColors.background,
          ],
        ),
      ),
      child: child,
    );
  }
}

class NCard extends StatelessWidget {
  const NCard({super.key, required this.child, this.padding = const EdgeInsets.all(16), this.onTap, this.color});

  final Widget child;
  final EdgeInsets padding;
  final VoidCallback? onTap;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: color ?? NaglyColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(20)),
        side: BorderSide(color: NaglyColors.outline),
      ),
      clipBehavior: Clip.antiAlias,
      child: InkWell(onTap: onTap, child: Padding(padding: padding, child: child)),
    );
  }
}

class SectionLabel extends StatelessWidget {
  const SectionLabel(this.text, {super.key});
  final String text;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(4, 20, 4, 8),
        child: Text(text.toUpperCase(), style: Theme.of(context).textTheme.labelSmall),
      );
}

/// Chunky pill button with a press-squish.
class PillButton extends StatefulWidget {
  const PillButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.filled = false,
    this.color,
    this.icon,
    this.semanticLabel,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool filled;
  final Color? color;
  final Widget? icon;
  final String? semanticLabel;

  @override
  State<PillButton> createState() => _PillButtonState();
}

class _PillButtonState extends State<PillButton> {
  bool _down = false;

  @override
  Widget build(BuildContext context) {
    final c = widget.color ?? NaglyColors.primaryDeep;
    return Semantics(
      button: true,
      label: widget.semanticLabel ?? widget.label,
      excludeSemantics: true,
      child: GestureDetector(
        onTapDown: (_) => setState(() => _down = true),
        onTapCancel: () => setState(() => _down = false),
        onTapUp: (_) => setState(() => _down = false),
        onTap: widget.onPressed,
        child: AnimatedScale(
          scale: _down ? 0.92 : 1,
          duration: const Duration(milliseconds: 110),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            constraints: const BoxConstraints(minHeight: 52),
            padding: const EdgeInsets.symmetric(horizontal: 18),
            decoration: BoxDecoration(
              color: widget.filled ? c : Colors.white,
              borderRadius: BorderRadius.circular(30),
              border: Border.all(color: widget.filled ? c : NaglyColors.outline, width: 1.5),
              boxShadow: widget.filled
                  ? [BoxShadow(color: c.withValues(alpha: 0.35), blurRadius: 14, offset: const Offset(0, 6))]
                  : const [BoxShadow(color: Color(0x0F122730), blurRadius: 10, offset: Offset(0, 4))],
            ),
            alignment: Alignment.center,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (widget.icon != null) ...[widget.icon!, const SizedBox(width: 6)],
                Flexible(
                  child: Text(
                    widget.label,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: widget.filled ? Colors.white : c,
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
}

/// Confetti burst for goal completion.
class ConfettiBurst extends StatefulWidget {
  const ConfettiBurst({super.key, required this.onDone});
  final VoidCallback onDone;

  @override
  State<ConfettiBurst> createState() => _ConfettiBurstState();
}

class _ConfettiBurstState extends State<ConfettiBurst> with SingleTickerProviderStateMixin {
  late final AnimationController _c =
      AnimationController(vsync: this, duration: const Duration(milliseconds: 2600))
        ..forward().whenComplete(widget.onDone);
  final _pieces = List.generate(90, (i) => _Piece(Random(i)));

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => IgnorePointer(
        child: AnimatedBuilder(
          animation: _c,
          builder: (context, _) => CustomPaint(size: Size.infinite, painter: _ConfettiPainter(_pieces, _c.value)),
        ),
      );
}

class _Piece {
  _Piece(Random r)
      : angle = -pi / 2 + (r.nextDouble() - 0.5) * 1.6,
        speed = 0.6 + r.nextDouble() * 0.8,
        spin = (r.nextDouble() - 0.5) * 18,
        size = 6 + r.nextDouble() * 7,
        color = const [
          NaglyColors.primary,
          NaglyColors.accent,
          NaglyColors.gold,
          NaglyColors.med,
          NaglyColors.success,
          NaglyColors.brand,
        ][r.nextInt(6)];
  final double angle, speed, spin, size;
  final Color color;
}

class _ConfettiPainter extends CustomPainter {
  _ConfettiPainter(this.pieces, this.t);
  final List<_Piece> pieces;
  final double t;

  @override
  void paint(Canvas canvas, Size size) {
    final origin = Offset(size.width / 2, size.height * 0.45);
    for (final p in pieces) {
      final v = p.speed * size.height * 0.9;
      final x = origin.dx + cos(p.angle) * v * t;
      final y = origin.dy + sin(p.angle) * v * t + 0.5 * 1400 * t * t;
      final paint = Paint()..color = p.color.withValues(alpha: (1 - t).clamp(0.0, 1.0));
      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(p.spin * t);
      canvas.drawRRect(
          RRect.fromRectAndRadius(Rect.fromCenter(center: Offset.zero, width: p.size, height: p.size * 0.55),
              const Radius.circular(2)),
          paint);
      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(covariant _ConfettiPainter old) => old.t != t;
}

/// Small rounded tag, e.g. "FREE" / "🔒 Pro" / "BEST VALUE".
class Tag extends StatelessWidget {
  const Tag(this.text, {super.key, this.color = NaglyColors.brand, this.filled = false});
  final String text;
  final Color color;
  final bool filled;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
        decoration: BoxDecoration(
          color: filled ? color : color.withValues(alpha: 0.12),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(text,
            style: TextStyle(
                fontSize: 11, fontWeight: FontWeight.w900, letterSpacing: 0.6, color: filled ? Colors.white : color)),
      );
}

String formatHourLabel(int h) {
  final hr = h % 12 == 0 ? 12 : h % 12;
  return '$hr ${h < 12 ? 'AM' : 'PM'}';
}

String greetingFor(int hour) {
  if (hour >= 5 && hour < 12) return 'Good morning';
  if (hour >= 12 && hour < 17) return 'Good afternoon';
  if (hour >= 17 && hour < 22) return 'Good evening';
  return 'Up late?';
}
