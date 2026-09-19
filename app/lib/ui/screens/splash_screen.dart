import 'package:flutter/material.dart';

import '../theme.dart';
import '../widgets/logo.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key, required this.onDone});
  final VoidCallback onDone;

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> with SingleTickerProviderStateMixin {
  late final AnimationController _c = AnimationController(vsync: this, duration: const Duration(milliseconds: 1500))
    ..forward().whenComplete(widget.onDone);

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: AnimatedBuilder(
          animation: _c,
          builder: (context, _) {
            final drop = Curves.elasticOut.transform((_c.value / 0.55).clamp(0.0, 1.0));
            final brow = Curves.easeOutBack.transform(((_c.value - 0.5) / 0.25).clamp(0.0, 1.0));
            final text = Curves.easeOut.transform(((_c.value - 0.35) / 0.4).clamp(0.0, 1.0));
            return Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Transform.scale(scale: 0.4 + 0.6 * drop, child: NaglyLogo(size: 120, browRaise: brow)),
                const SizedBox(height: 22),
                Opacity(
                  opacity: text,
                  child: Transform.translate(
                    offset: Offset(0, 12 * (1 - text)),
                    child: Column(
                      children: [
                        Text('Nagly', style: Theme.of(context).textTheme.displaySmall),
                        const SizedBox(height: 4),
                        const Text('Someone who cares.',
                            style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: NaglyColors.textSecondary)),
                      ],
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
