import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../theme.dart';

/// Widgets the spotlight tour can point at. Attach with `KeyedSubtree(key: …)`.
abstract final class TourKeys {
  static final bubble = GlobalKey(debugLabel: 'tour-bubble');
  static final bottle = GlobalKey(debugLabel: 'tour-bottle');
  static final quickAdd = GlobalKey(debugLabel: 'tour-quick-add');
  static final bond = GlobalKey(debugLabel: 'tour-bond');
  static final personasTab = GlobalKey(debugLabel: 'tour-personas-tab');
  static final historyTab = GlobalKey(debugLabel: 'tour-history-tab');
  static final insightsTab = GlobalKey(debugLabel: 'tour-insights-tab');
  static final settingsTab = GlobalKey(debugLabel: 'tour-settings-tab');
}

class TourStep {
  const TourStep({
    required this.target,
    required this.title,
    required this.body,
  });

  final GlobalKey target;
  final String title;
  final String body;
}

/// Dims the screen, cuts a spotlight around each step's widget and explains it.
/// Steps whose widget isn't on screen (e.g. the bottle in medication mode) are
/// skipped. Completes when the user finishes or taps Skip.
Future<void> showFeatureTour(BuildContext context, List<TourStep> steps) async {
  final overlay = Overlay.of(context, rootOverlay: true);
  final done = Completer<void>();
  late OverlayEntry entry;
  entry = OverlayEntry(
    builder: (_) => _Tour(
      steps: steps,
      onFinish: () {
        entry.remove();
        if (!done.isCompleted) done.complete();
      },
    ),
  );
  overlay.insert(entry);
  return done.future;
}

class _Tour extends StatefulWidget {
  const _Tour({required this.steps, required this.onFinish});
  final List<TourStep> steps;
  final VoidCallback onFinish;

  @override
  State<_Tour> createState() => _TourState();
}

class _TourState extends State<_Tour> {
  int _index = -1;
  Rect? _hole;

  /// The list the last step scrolled. Lists drop far-away children, so a
  /// target that isn't built may just be scrolled out of view.
  ScrollPosition? _lastScroll;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _go(0));
  }

  Future<void> _go(int from) async {
    for (var i = from; i < widget.steps.length; i++) {
      var ctx = widget.steps[i].target.currentContext;
      if (ctx == null && _lastScroll != null && _lastScroll!.pixels > 0) {
        await _lastScroll!.animateTo(
          0,
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeOut,
        );
        await WidgetsBinding.instance.endOfFrame;
        ctx = widget.steps[i].target.currentContext;
      }
      if (!mounted) return;
      if (ctx == null || !ctx.mounted) continue;
      final scrollable = Scrollable.maybeOf(ctx);
      if (scrollable != null) {
        _lastScroll = scrollable.position;
        await Scrollable.ensureVisible(
          ctx,
          alignment: 0.4,
          duration: const Duration(milliseconds: 250),
        );
      }
      if (!mounted || !ctx.mounted) return;
      final box = ctx.findRenderObject() as RenderBox?;
      if (box == null || !box.hasSize) continue;
      final rect = box.localToGlobal(Offset.zero) & box.size;
      setState(() {
        _index = i;
        _hole = rect.inflate(8);
      });
      return;
    }
    widget.onFinish();
  }

  void _next() {
    HapticFeedback.selectionClick();
    _go(_index + 1);
  }

  bool get _isLast {
    for (var i = _index + 1; i < widget.steps.length; i++) {
      if (widget.steps[i].target.currentContext != null) return false;
    }
    return true;
  }

  @override
  Widget build(BuildContext context) {
    final hole = _hole;
    if (_index < 0 || hole == null) return const SizedBox.shrink();
    final step = widget.steps[_index];
    final padding = MediaQuery.paddingOf(context);
    return LayoutBuilder(
      builder: (context, constraints) =>
          _layout(step, hole, constraints.biggest, padding),
    );
  }

  Widget _layout(TourStep step, Rect hole, Size size, EdgeInsets padding) {
    // Put the card on whichever side of the spotlight has more room.
    final spaceAbove = hole.top - padding.top;
    final spaceBelow = size.height - hole.bottom - padding.bottom;
    final below = spaceBelow >= spaceAbove;
    const cardMargin = 16.0;

    return Material(
      type: MaterialType.transparency,
      child: Stack(
        children: [
          Positioned.fill(
            child: GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTap: _next,
              child: TweenAnimationBuilder<Rect?>(
                tween: RectTween(end: hole),
                duration: const Duration(milliseconds: 280),
                curve: Curves.easeOutCubic,
                builder: (_, r, _) =>
                    CustomPaint(painter: _SpotlightPainter(r ?? hole)),
              ),
            ),
          ),
          Positioned(
            left: cardMargin,
            right: cardMargin,
            top: below ? hole.bottom + 14 : null,
            bottom: below ? null : size.height - hole.top + 14,
            child: ConstrainedBox(
              constraints: BoxConstraints(
                maxHeight: ((below ? spaceBelow : spaceAbove) - 24).clamp(
                  120.0,
                  double.infinity,
                ),
              ),
              child: _TourCard(
                key: ValueKey(_index),
                title: step.title,
                body: step.body,
                stepLabel:
                    '${widget.steps.indexOf(step) + 1} of ${widget.steps.length}',
                isLast: _isLast,
                onNext: _next,
                onSkip: widget.onFinish,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _TourCard extends StatelessWidget {
  const _TourCard({
    super.key,
    required this.title,
    required this.body,
    required this.stepLabel,
    required this.isLast,
    required this.onNext,
    required this.onSkip,
  });

  final String title, body, stepLabel;
  final bool isLast;
  final VoidCallback onNext, onSkip;

  @override
  Widget build(BuildContext context) => TweenAnimationBuilder<double>(
    tween: Tween(begin: 0, end: 1),
    duration: const Duration(milliseconds: 220),
    builder: (_, t, child) => Opacity(
      opacity: t,
      child: Transform.translate(offset: Offset(0, 8 * (1 - t)), child: child),
    ),
    child: Semantics(
      liveRegion: true,
      container: true,
      child: Container(
        padding: const EdgeInsets.fromLTRB(18, 16, 12, 8),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          boxShadow: const [
            BoxShadow(
              color: Color(0x33000000),
              blurRadius: 24,
              offset: Offset(0, 8),
            ),
          ],
        ),
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w900,
                  color: NaglyColors.ink,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                body,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: NaglyColors.textSecondary,
                  height: 1.35,
                ),
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  Text(
                    stepLabel,
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      color: NaglyColors.textSecondary,
                    ),
                  ),
                  const Spacer(),
                  if (!isLast)
                    TextButton(onPressed: onSkip, child: const Text('Skip')),
                  TextButton(
                    onPressed: isLast ? onSkip : onNext,
                    child: Text(isLast ? 'Got it' : 'Next'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    ),
  );
}

class _SpotlightPainter extends CustomPainter {
  _SpotlightPainter(this.hole);
  final Rect hole;

  @override
  void paint(Canvas canvas, Size size) {
    final dim = Path()..addRect(Offset.zero & size);
    final cut = Path()
      ..addRRect(RRect.fromRectAndRadius(hole, const Radius.circular(20)));
    canvas.drawPath(
      Path.combine(PathOperation.difference, dim, cut),
      Paint()..color = const Color(0xB3000000),
    );
    canvas.drawRRect(
      RRect.fromRectAndRadius(hole, const Radius.circular(20)),
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2
        ..color = NaglyColors.primary,
    );
  }

  @override
  bool shouldRepaint(_SpotlightPainter old) => old.hole != hole;
}
