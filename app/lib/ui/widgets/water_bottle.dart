import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:sensors_plus/sensors_plus.dart';

import '../theme.dart';

/// The living hero: a glass bottle whose water sloshes with real phone tilt,
/// animates its level, and sends up bubbles whenever you log a sip.
class WaterBottle extends StatefulWidget {
  const WaterBottle({
    super.key,
    required this.progress,
    this.label,
    this.sublabel,
    this.width = 170,
    this.height = 250,
    this.onTap,
    this.enableTilt = true,
  });

  final double progress;
  final String? label;
  final String? sublabel;
  final double width;
  final double height;
  final VoidCallback? onTap;
  final bool enableTilt;

  @override
  State<WaterBottle> createState() => _WaterBottleState();
}

class _WaterBottleState extends State<WaterBottle>
    with TickerProviderStateMixin {
  late final AnimationController _wave = AnimationController(
    vsync: this,
    duration: const Duration(seconds: 3),
  )..repeat();
  late final AnimationController _splash = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 1400),
  );
  StreamSubscription<AccelerometerEvent>? _accel;
  double _tilt = 0; // radians, smoothed
  double _slosh = 0; // extra wave amplitude from motion
  final _bubbles = <_Bubble>[];
  final _rand = Random();

  @override
  void initState() {
    super.initState();
    if (widget.enableTilt) {
      try {
        _accel =
            accelerometerEventStream(
              samplingPeriod: SensorInterval.uiInterval,
            ).listen((e) {
              // Keep the surface level with gravity: x tilt → surface counter-rotates.
              final target = (atan2(e.x, e.y.abs() + 0.001)).clamp(-0.5, 0.5);
              final delta = (target - _tilt).abs();
              _tilt += (target - _tilt) * 0.12;
              _slosh = (_slosh * 0.92 + delta * 1.5).clamp(0.0, 1.0);
            }, onError: (_) {});
      } catch (_) {
        // No accelerometer (emulator/tests) — the water still waves.
      }
    }
  }

  @override
  void didUpdateWidget(covariant WaterBottle old) {
    super.didUpdateWidget(old);
    if (widget.progress > old.progress + 0.001) {
      _bubbles
        ..clear()
        ..addAll(List.generate(14, (_) => _Bubble(_rand)));
      _splash.forward(from: 0);
      _slosh = 1;
    }
  }

  @override
  void dispose() {
    _accel?.cancel();
    _wave.dispose();
    _splash.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final pct = (widget.progress * 100).clamp(0, 999).round();
    return Semantics(
      label: 'Water bottle, $pct percent of daily goal',
      button: widget.onTap != null,
      child: GestureDetector(
        onTap: widget.onTap,
        child: SizedBox(
          width: widget.width,
          height: widget.height,
          child: TweenAnimationBuilder<double>(
            tween: Tween(end: widget.progress.clamp(0.0, 1.0)),
            duration: const Duration(milliseconds: 900),
            curve: Curves.easeOutBack,
            builder: (context, level, _) => AnimatedBuilder(
              animation: Listenable.merge([_wave, _splash]),
              builder: (context, _) => CustomPaint(
                painter: _BottlePainter(
                  level: level,
                  phase: _wave.value * 2 * pi,
                  tilt: _tilt,
                  slosh: _slosh,
                  splash: _splash.value,
                  bubbles: _bubbles,
                ),
                child: Padding(
                  padding: EdgeInsets.only(top: widget.height * 0.2),
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (widget.label != null)
                          Text(
                            widget.label!,
                            style: TextStyle(
                              fontSize: widget.width * 0.2,
                              fontWeight: FontWeight.w900,
                              color: level > 0.45
                                  ? Colors.white
                                  : NaglyColors.ink,
                              shadows: level > 0.45
                                  ? [
                                      const Shadow(
                                        color: Color(0x33000000),
                                        blurRadius: 8,
                                      ),
                                    ]
                                  : null,
                            ),
                          ),
                        if (widget.sublabel != null)
                          Text(
                            widget.sublabel!,
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                              color: level > 0.38
                                  ? Colors.white.withValues(alpha: 0.9)
                                  : NaglyColors.textSecondary,
                            ),
                          ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _Bubble {
  _Bubble(Random r)
    : x = 0.15 + r.nextDouble() * 0.7,
      size = 2 + r.nextDouble() * 5,
      speed = 0.5 + r.nextDouble() * 0.7,
      delay = r.nextDouble() * 0.35;
  final double x, size, speed, delay;
}

class _BottlePainter extends CustomPainter {
  _BottlePainter({
    required this.level,
    required this.phase,
    required this.tilt,
    required this.slosh,
    required this.splash,
    required this.bubbles,
  });

  final double level, phase, tilt, slosh, splash;
  final List<_Bubble> bubbles;

  Path _bottlePath(Size s) {
    final w = s.width, h = s.height;
    final neckW = w * 0.36, neckH = h * 0.1, shoulder = h * 0.2;
    final r = w * 0.2;
    return Path()
      ..moveTo((w - neckW) / 2, 0)
      ..lineTo((w + neckW) / 2, 0)
      ..lineTo((w + neckW) / 2, neckH)
      ..cubicTo(
        w,
        neckH + shoulder * 0.2,
        w,
        shoulder * 0.9,
        w,
        shoulder + r * 0.2,
      )
      ..lineTo(w, h - r)
      ..quadraticBezierTo(w, h, w - r, h)
      ..lineTo(r, h)
      ..quadraticBezierTo(0, h, 0, h - r)
      ..lineTo(0, shoulder + r * 0.2)
      ..cubicTo(
        0,
        shoulder * 0.9,
        0,
        neckH + shoulder * 0.2,
        (w - neckW) / 2,
        neckH,
      )
      ..close();
  }

  @override
  void paint(Canvas canvas, Size size) {
    final body = _bottlePath(size);
    final bounds = Offset.zero & size;

    // Glass
    canvas.drawShadow(body, const Color(0x5540A0C0), 18, false);
    canvas.drawPath(
      body,
      Paint()
        ..shader = const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFFFFFFFF), Color(0xFFEAF6FB)],
        ).createShader(bounds),
    );

    canvas.save();
    canvas.clipPath(body);
    final top = size.height * 0.1;
    final fillable = size.height - top;
    final surfaceY = size.height - fillable * level;
    final amp =
        4 + 8 * slosh + 6 * (1 - splash) * (splash > 0 && splash < 1 ? 1 : 0);

    Path wave(double offset, double ampScale) {
      final p = Path()..moveTo(-20, size.height + 20);
      for (double x = -20; x <= size.width + 20; x += 4) {
        final tiltY = tan(-tilt) * (x - size.width / 2);
        final y =
            surfaceY +
            tiltY +
            sin(x / size.width * 2 * pi + phase + offset) * amp * ampScale;
        p.lineTo(x, y);
      }
      return p
        ..lineTo(size.width + 20, size.height + 20)
        ..close();
    }

    if (level > 0.001) {
      canvas.drawPath(
        wave(pi, 0.8),
        Paint()..color = NaglyColors.primary.withValues(alpha: 0.45),
      );
      canvas.drawPath(
        wave(0, 1),
        Paint()
          ..shader = const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF6FD3FA), Color(0xFF1F93B4)],
          ).createShader(bounds),
      );
      // Bubbles after a sip.
      if (splash > 0 && splash < 1) {
        final bubblePaint = Paint()
          ..color = Colors.white.withValues(alpha: 0.7 * (1 - splash));
        for (final b in bubbles) {
          final t =
              ((splash - b.delay) / (1 - b.delay)).clamp(0.0, 1.0) * b.speed;
          final y = size.height - (size.height - surfaceY) * t;
          canvas.drawCircle(
            Offset(b.x * size.width + sin(t * 10) * 3, y),
            b.size,
            bubblePaint,
          );
        }
      }
    }
    canvas.restore();

    // Glass highlight + outline
    canvas.drawPath(
      Path()..addRRect(
        RRect.fromRectAndRadius(
          Rect.fromLTWH(
            size.width * 0.12,
            size.height * 0.3,
            size.width * 0.07,
            size.height * 0.45,
          ),
          const Radius.circular(20),
        ),
      ),
      Paint()..color = Colors.white.withValues(alpha: 0.55),
    );
    canvas.drawPath(
      body,
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5
        ..color = const Color(0xFFBFE3F0),
    );
    // Cap
    final capW = size.width * 0.42;
    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromLTWH(
          (size.width - capW) / 2,
          -size.height * 0.05,
          capW,
          size.height * 0.07,
        ),
        const Radius.circular(8),
      ),
      Paint()..color = NaglyColors.brand,
    );
  }

  @override
  bool shouldRepaint(covariant _BottlePainter old) => true;
}
