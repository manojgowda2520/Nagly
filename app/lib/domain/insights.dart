import 'models.dart';
import 'mood_engine.dart';

class DayTotal {
  const DayTotal(this.date, this.totalMl);
  final DateTime date;
  final int totalMl;
}

class WeeklyInsights {
  const WeeklyInsights({
    required this.days,
    required this.dailyAverageMl,
    required this.goalMetDays,
    required this.currentStreak,
    required this.bestStreak,
    required this.bestHour,
    required this.hasData,
  });

  final List<DayTotal> days;
  final int dailyAverageMl;
  final int goalMetDays;
  final int currentStreak;
  final int bestStreak;

  /// Hour of day (0-23) with the most water logged over the window; null if no data.
  final int? bestHour;
  final bool hasData;
}

WeeklyInsights computeWeeklyInsights({
  required List<DrinkLog> logs,
  required int dailyMl,
  required DateTime now,
}) {
  final byDay = totalsByDay(logs);
  final today = dateOnly(now);
  final days = [
    for (var offset = 6; offset >= 0; offset--)
      () {
        final d = DateTime(today.year, today.month, today.day - offset);
        return DayTotal(d, byDay[d] ?? 0);
      }(),
  ];
  final hasData = days.any((d) => d.totalMl > 0);

  // Average over days that had any logging, so a fresh install isn't dragged to zero.
  final active = days.where((d) => d.totalMl > 0).toList();
  final avg = active.isEmpty ? 0 : active.fold<int>(0, (s, d) => s + d.totalMl) ~/ active.length;

  final byHour = <int, int>{};
  for (final l in logs) {
    final h = DateTime.fromMillisecondsSinceEpoch(l.timestampMs).hour;
    byHour[h] = (byHour[h] ?? 0) + l.amountMl;
  }
  final bestHour = byHour.isEmpty
      ? null
      : (byHour.entries.toList()..sort((a, b) => b.value.compareTo(a.value))).first.key;

  return WeeklyInsights(
    days: days,
    dailyAverageMl: avg,
    goalMetDays: days.where((d) => d.totalMl >= dailyMl).length,
    currentStreak: currentStreak(byDay, dailyMl, now),
    bestStreak: bestStreak(byDay, dailyMl),
    bestHour: bestHour,
    hasData: hasData,
  );
}

String formatHour(int hour) {
  final h = hour % 12 == 0 ? 12 : hour % 12;
  return '$h ${hour < 12 ? 'AM' : 'PM'}';
}
