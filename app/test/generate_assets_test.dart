// Renders store/launcher artwork from the same painter the app uses.
// Run: flutter test test/generate_assets_test.dart --dart-define=GENERATE_ASSETS=true
import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nagly/ui/theme.dart';
import 'package:nagly/ui/widgets/logo.dart';

const _enabled = bool.fromEnvironment('GENERATE_ASSETS');

Future<void> _png(String path, int w, int h, void Function(Canvas, Size) paint) async {
  final rec = ui.PictureRecorder();
  final canvas = Canvas(rec, Rect.fromLTWH(0, 0, w.toDouble(), h.toDouble()));
  paint(canvas, Size(w.toDouble(), h.toDouble()));
  final img = await rec.endRecording().toImage(w, h);
  final bytes = await img.toByteData(format: ui.ImageByteFormat.png);
  File(path)
    ..createSync(recursive: true)
    ..writeAsBytesSync(bytes!.buffer.asUint8List());
}

Future<void> _loadNunito() async {
  final loader = FontLoader('Nunito');
  for (final w in [400, 800, 900]) {
    loader.addFont(Future.value(ByteData.sublistView(File('assets/fonts/Nunito-$w.ttf').readAsBytesSync())));
  }
  await loader.load();
}

void _text(Canvas c, String t, Offset o, double size, FontWeight w, Color color, {double maxW = 900}) {
  final tp = TextPainter(
    text: TextSpan(text: t, style: TextStyle(fontFamily: 'Nunito', fontSize: size, fontWeight: w, color: color, height: 1.15)),
    textDirection: TextDirection.ltr,
  )..layout(maxWidth: maxW);
  tp.paint(c, o);
}

void main() {
  testWidgets('generate icons & store art', (tester) async {
    await tester.runAsync(() async {
      await _loadNunito();
      const res = 'android/app/src/main/res';
      const densities = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192};
      for (final e in densities.entries) {
        final s = e.value;
        await _png('$res/mipmap-${e.key}/ic_launcher.png', s, s,
            (c, size) => NaglyLogoPainter().paint(c, size));
        await _png('$res/mipmap-${e.key}/ic_launcher_round.png', s, s, (c, size) {
          c.clipPath(Path()..addOval(Offset.zero & size));
          NaglyLogoPainter(cornerRadius: 0).paint(c, size);
        });
        // Adaptive icon foreground: 108dp canvas, art within the 66dp safe zone.
        final f = (s * 108 / 48).round();
        await _png('$res/mipmap-${e.key}/ic_launcher_foreground.png', f, f,
            (c, size) => NaglyLogoPainter(withBackground: false, foregroundScale: 0.62).paint(c, size));
      }
      await _png('store/icon-512.png', 512, 512, (c, s) => NaglyLogoPainter(cornerRadius: 0).paint(c, s));
      await _png('store/icon-1024.png', 1024, 1024, (c, s) => NaglyLogoPainter(cornerRadius: 0).paint(c, s));

      // Play feature graphic 1024x500.
      await _png('store/feature-graphic.png', 1024, 500, (c, size) {
        c.drawRect(
          Offset.zero & size,
          Paint()
            ..shader = const LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFF16A3B0), NaglyColors.brand],
            ).createShader(Offset.zero & size),
        );
        c.save();
        c.translate(70, 110);
        NaglyLogoPainter(withBackground: false).paint(c, const Size(280, 280));
        c.restore();
        _text(c, 'Nagly', const Offset(390, 120), 104, FontWeight.w900, Colors.white);
        _text(c, 'Someone who cares.', const Offset(396, 250), 44, FontWeight.w800, const Color(0xFFDDF4FC));
        _text(c, 'Mom, Dad, Dadi & your Bestie nag you to\ndrink water and take your pills.', const Offset(398, 320), 28,
            FontWeight.w700, const Color(0xCCFFFFFF), maxW: 600);
      });
    });
  }, skip: !_enabled);
}
