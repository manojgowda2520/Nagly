import 'package:flutter/material.dart';

import '../theme.dart';

/// The Nagly mark: a water drop with one raised eyebrow — the look every
/// mother gives you when you say you "drank enough today".
class NaglyLogo extends StatelessWidget {
  const NaglyLogo({
    super.key,
    this.size = 96,
    this.withBackground = true,
    this.browRaise = 1,
  });

  final double size;
  final bool withBackground;

  /// 0..1 — animate the eyebrow for the splash.
  final double browRaise;

  @override
  Widget build(BuildContext context) => CustomPaint(
    size: Size.square(size),
    painter: NaglyLogoPainter(
      withBackground: withBackground,
      browRaise: browRaise,
    ),
  );
}

class NaglyLogoPainter extends CustomPainter {
  NaglyLogoPainter({
    this.withBackground = true,
    this.browRaise = 1,
    this.foregroundScale = 1,
    this.cornerRadius = 0.23,
  });

  final bool withBackground;
  final double browRaise;

  /// Shrinks the drop for Android adaptive-icon safe zones.
  final double foregroundScale;

  /// Background corner radius as a fraction of size; 0 = full-bleed square (store icons).
  final double cornerRadius;

  @override
  void paint(Canvas canvas, Size size) {
    final s = size.width;
    if (withBackground) {
      final r = RRect.fromRectAndRadius(
        Offset.zero & size,
        Radius.circular(s * cornerRadius),
      );
      canvas.drawRRect(
        r,
        Paint()
          ..shader = const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF16A3B0), NaglyColors.brand],
          ).createShader(Offset.zero & size),
      );
    }
    canvas.save();
    canvas.translate(s / 2, s / 2);
    canvas.scale(foregroundScale);
    canvas.translate(-s / 2, -s / 2);

    // Drop
    final cx = s / 2;
    final drop = Path()
      ..moveTo(cx, s * 0.14)
      ..cubicTo(
        cx + s * 0.06,
        s * 0.28,
        cx + s * 0.3,
        s * 0.44,
        cx + s * 0.3,
        s * 0.6,
      )
      ..cubicTo(cx + s * 0.3, s * 0.77, cx + s * 0.165, s * 0.88, cx, s * 0.88)
      ..cubicTo(
        cx - s * 0.165,
        s * 0.88,
        cx - s * 0.3,
        s * 0.77,
        cx - s * 0.3,
        s * 0.6,
      )
      ..cubicTo(cx - s * 0.3, s * 0.44, cx - s * 0.06, s * 0.28, cx, s * 0.14)
      ..close();
    canvas.drawShadow(drop, const Color(0x66000000), s * 0.03, false);
    canvas.drawPath(
      drop,
      Paint()
        ..shader = const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFFFFFFFF), Color(0xFFDDF4FC)],
        ).createShader(Rect.fromLTWH(0, s * 0.14, s, s * 0.74)),
    );
    // Shine
    canvas.drawOval(
      Rect.fromCenter(
        center: Offset(cx - s * 0.2, s * 0.73),
        width: s * 0.045,
        height: s * 0.1,
      ),
      Paint()..color = const Color(0xFFBFEAF8),
    );

    final ink = Paint()
      ..color = NaglyColors.ink
      ..strokeCap = StrokeCap.round;
    // Eyes
    canvas.drawCircle(Offset(cx - s * 0.1, s * 0.63), s * 0.035, ink);
    canvas.drawCircle(Offset(cx + s * 0.1, s * 0.63), s * 0.035, ink);
    // Brows: left flat, right raised (the nag look)
    final brow = ink
      ..style = PaintingStyle.stroke
      ..strokeWidth = s * 0.028;
    canvas.drawLine(
      Offset(cx - s * 0.15, s * 0.545),
      Offset(cx - s * 0.05, s * 0.55),
      brow,
    );
    final lift = s * 0.045 * browRaise;
    canvas.drawLine(
      Offset(cx + s * 0.05, s * 0.54 - lift * 0.4),
      Offset(cx + s * 0.15, s * 0.52 - lift),
      brow,
    );
    // Mouth: a knowing flat line
    canvas.drawLine(
      Offset(cx - s * 0.04, s * 0.74),
      Offset(cx + s * 0.045, s * 0.735),
      brow,
    );
    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant NaglyLogoPainter old) =>
      old.browRaise != browRaise || old.withBackground != withBackground;
}
