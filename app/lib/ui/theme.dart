import 'package:flutter/material.dart';

import '../domain/models.dart';

/// Nagly tokens — light only (locked decision: the white look is the brand).
abstract final class NaglyColors {
  static const background = Color(0xFFFAFBFC);
  static const card = Color(0xFFFFFFFF);
  static const primary = Color(0xFF4FC3F7);
  static const primaryDeep = Color(0xFF1F93B4);
  static const brand = Color(0xFF0E7C86);
  static const accent = Color(0xFFFF8A65);
  static const coral = Color(0xFFE85D3B);
  static const success = Color(0xFF4CAF50);
  static const warning = Color(0xFFFFC107);
  static const danger = Color(0xFFEF5350);
  static const ink = Color(0xFF122730);
  static const textPrimary = Color(0xFF212121);
  static const textSecondary = Color(0xFF5C7682);
  static const outline = Color(0xFFE3EAEE);
  static const surfaceVariant = Color(0xFFF0F4F8);
  static const med = Color(0xFF7E6BE0);
  static const gold = Color(0xFFF2B53B);

  static Color mood(Mood m) => switch (m) {
        Mood.neutral => const Color(0xFFA5D6A7),
        Mood.worried => const Color(0xFFFFD166),
        Mood.disappointed => const Color(0xFFB0BEC5),
        Mood.proud => const Color(0xFF81D4FA),
      };

  /// Soft day-part tint at the top of screens: peach morning, sky afternoon,
  /// amber evening, deep navy night.
  static Color dayTint(int hour) {
    if (hour >= 5 && hour <= 10) return const Color(0xFFFFE4D6);
    if (hour >= 11 && hour <= 16) return const Color(0xFFD6F0FA);
    if (hour >= 17 && hour <= 20) return const Color(0xFFFFE8C8);
    return const Color(0xFFD9DEF5);
  }
}

abstract final class NaglySpace {
  static const xxs = 4.0;
  static const xs = 8.0;
  static const sm = 16.0;
  static const md = 24.0;
  static const lg = 32.0;
}

ThemeData buildNaglyTheme() {
  const font = 'Nunito';
  final base = ThemeData(
    useMaterial3: true,
    fontFamily: font,
    colorScheme: ColorScheme.fromSeed(
      seedColor: NaglyColors.primary,
      primary: NaglyColors.primaryDeep,
      secondary: NaglyColors.brand,
      tertiary: NaglyColors.accent,
      surface: NaglyColors.card,
      brightness: Brightness.light,
    ),
    scaffoldBackgroundColor: NaglyColors.background,
  );
  final t = base.textTheme;
  return base.copyWith(
    textTheme: t.copyWith(
      displayLarge: t.displayLarge?.copyWith(fontWeight: FontWeight.w900, color: NaglyColors.ink, letterSpacing: -1),
      displaySmall: t.displaySmall?.copyWith(fontWeight: FontWeight.w900, color: NaglyColors.ink, letterSpacing: -0.5),
      headlineMedium: t.headlineMedium?.copyWith(fontWeight: FontWeight.w900, color: NaglyColors.ink, letterSpacing: -0.5),
      headlineSmall: t.headlineSmall?.copyWith(fontWeight: FontWeight.w800, color: NaglyColors.ink),
      titleLarge: t.titleLarge?.copyWith(fontWeight: FontWeight.w800, color: NaglyColors.ink),
      titleMedium: t.titleMedium?.copyWith(fontWeight: FontWeight.w800, color: NaglyColors.ink),
      titleSmall: t.titleSmall?.copyWith(fontWeight: FontWeight.w700, color: NaglyColors.ink),
      bodyLarge: t.bodyLarge?.copyWith(fontWeight: FontWeight.w600, color: NaglyColors.textPrimary, height: 1.4),
      bodyMedium: t.bodyMedium?.copyWith(fontWeight: FontWeight.w600, color: NaglyColors.textPrimary, height: 1.4),
      bodySmall: t.bodySmall?.copyWith(fontWeight: FontWeight.w600, color: NaglyColors.textSecondary),
      labelLarge: t.labelLarge?.copyWith(fontWeight: FontWeight.w800),
      labelSmall: t.labelSmall?.copyWith(fontWeight: FontWeight.w800, letterSpacing: 1.1, color: NaglyColors.textSecondary),
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      foregroundColor: NaglyColors.ink,
      centerTitle: false,
      titleTextStyle: TextStyle(fontFamily: font, fontSize: 22, fontWeight: FontWeight.w900, color: NaglyColors.ink),
    ),
    cardTheme: const CardThemeData(
      color: NaglyColors.card,
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(20)),
        side: BorderSide(color: NaglyColors.outline),
      ),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: NaglyColors.primaryDeep,
        foregroundColor: Colors.white,
        minimumSize: const Size.fromHeight(56),
        shape: const StadiumBorder(),
        textStyle: const TextStyle(fontFamily: font, fontSize: 17, fontWeight: FontWeight.w800),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: NaglyColors.primaryDeep,
        minimumSize: const Size(48, 48),
        shape: const StadiumBorder(),
        side: const BorderSide(color: NaglyColors.outline, width: 1.5),
        textStyle: const TextStyle(fontFamily: font, fontSize: 15, fontWeight: FontWeight.w800),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: NaglyColors.textSecondary,
        minimumSize: const Size(48, 48),
        textStyle: const TextStyle(fontFamily: font, fontSize: 15, fontWeight: FontWeight.w700),
      ),
    ),
    navigationBarTheme: NavigationBarThemeData(
      backgroundColor: NaglyColors.card,
      surfaceTintColor: Colors.transparent,
      indicatorColor: NaglyColors.primary.withValues(alpha: 0.18),
      elevation: 0,
      height: 68,
      labelTextStyle: WidgetStateProperty.resolveWith((s) => TextStyle(
            fontFamily: font,
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: s.contains(WidgetState.selected) ? NaglyColors.primaryDeep : NaglyColors.textSecondary,
          )),
      iconTheme: WidgetStateProperty.resolveWith((s) => IconThemeData(
            color: s.contains(WidgetState.selected) ? NaglyColors.primaryDeep : NaglyColors.textSecondary,
          )),
    ),
    switchTheme: SwitchThemeData(
      trackColor: WidgetStateProperty.resolveWith(
          (s) => s.contains(WidgetState.selected) ? NaglyColors.primary : NaglyColors.outline),
      thumbColor: const WidgetStatePropertyAll(Colors.white),
      trackOutlineColor: const WidgetStatePropertyAll(Colors.transparent),
    ),
    snackBarTheme: const SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      backgroundColor: NaglyColors.ink,
      contentTextStyle: TextStyle(fontFamily: font, fontWeight: FontWeight.w700, color: Colors.white),
      shape: StadiumBorder(),
    ),
    bottomSheetTheme: const BottomSheetThemeData(
      backgroundColor: NaglyColors.card,
      surfaceTintColor: Colors.transparent,
      showDragHandle: true,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(28))),
    ),
    dialogTheme: const DialogThemeData(
      backgroundColor: NaglyColors.card,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(28))),
    ),
    dividerTheme: const DividerThemeData(color: NaglyColors.outline, space: 1),
  );
}
