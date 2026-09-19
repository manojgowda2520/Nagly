import 'dart:math';

import 'models.dart';
import 'persona_catalog.dart';

Mood computeMood({
  required double progressRatio,
  required double expectedRatio,
  required int ignoredNudgeCount,
}) {
  if (progressRatio >= 1) return Mood.proud;
  if (ignoredNudgeCount >= 2) return Mood.disappointed;
  if (progressRatio < expectedRatio - 0.15) return Mood.worried;
  return Mood.neutral;
}

int _wakingHours(int wakeHour, int sleepHour) =>
    sleepHour > wakeHour ? sleepHour - wakeHour : (24 - wakeHour) + sleepHour;

int _elapsedFromWake(int hour, int wakeHour, int sleepHour) {
  final waking = _wakingHours(wakeHour, sleepHour);
  if (sleepHour > wakeHour) {
    if (hour < wakeHour) return 0;
    if (hour >= sleepHour) return waking;
    return hour - wakeHour;
  }
  // Overnight schedule, e.g. wake 22 sleep 6.
  if (hour >= wakeHour) return hour - wakeHour;
  if (hour < sleepHour) return (24 - wakeHour) + hour;
  return waking;
}

/// Split the waking window into morning / afternoon / evening thirds.
DayPart dayPartFor(int hour, int wakeHour, int sleepHour) {
  if (wakeHour == sleepHour) return DayPart.afternoon;
  final third = _wakingHours(wakeHour, sleepHour) / 3;
  final elapsed = _elapsedFromWake(hour, wakeHour, sleepHour);
  if (elapsed < third) return DayPart.morning;
  if (elapsed < third * 2) return DayPart.afternoon;
  return DayPart.evening;
}

/// Fraction of the waking day elapsed. 0 before wake, 1 after sleep.
double expectedRatio(int nowHour, int wakeHour, int sleepHour) {
  if (wakeHour == sleepHour) return 1;
  final waking = _wakingHours(wakeHour, sleepHour);
  return (_elapsedFromWake(nowHour, wakeHour, sleepHour) / waking).clamp(
    0.0,
    1.0,
  );
}

/// How far behind schedule: 0 = on track or ahead, 1 = far behind.
double behindSeverity(double progressRatio, double expectedRatio) {
  if (expectedRatio <= 0) return 0;
  final gap = expectedRatio - progressRatio;
  if (gap <= 0) return 0;
  return (gap / expectedRatio).clamp(0.0, 1.0);
}

int recommendedDailyMl(int weightKg, ActivityLevel activity) {
  final base = (weightKg * 35).clamp(1500, 4000);
  final multiplier = switch (activity) {
    ActivityLevel.sedentary => 1.0,
    ActivityLevel.light => 1.1,
    ActivityLevel.active => 1.2,
    ActivityLevel.veryActive => 1.3,
  };
  // Round to the nearest 50 ml so the goal reads cleanly.
  final ml = ((base * multiplier) / 50).round() * 50;
  return ml.clamp(1500, 4800);
}

DateTime dateOnly(DateTime d) => DateTime(d.year, d.month, d.day);

String dateKey(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

DateTime _minusDays(DateTime d, int days) =>
    DateTime(d.year, d.month, d.day - days);

List<DateTime> _completedDays(Map<DateTime, int> logsByDay, int dailyMl) =>
    (logsByDay.entries
          .where((e) => e.value >= dailyMl)
          .map((e) => dateOnly(e.key))
          .toSet()
          .toList())
      ..sort();

/// Consecutive completed days ending today or yesterday.
/// Today incomplete does not break a streak that ended yesterday.
int currentStreak(Map<DateTime, int> logsByDay, int dailyMl, DateTime today) {
  if (logsByDay.isEmpty || dailyMl <= 0) return 0;
  final days = _completedDays(logsByDay, dailyMl);
  if (days.isEmpty) return 0;
  final t = dateOnly(today);
  final runEnd = days.last;
  if (runEnd != t && runEnd != _minusDays(t, 1)) return 0;
  var streak = 1;
  for (var i = days.length - 1; i >= 1; i--) {
    if (days[i - 1] == _minusDays(days[i], 1)) {
      streak++;
    } else {
      break;
    }
  }
  return streak;
}

int bestStreak(Map<DateTime, int> logsByDay, int dailyMl) {
  if (logsByDay.isEmpty || dailyMl <= 0) return 0;
  final days = _completedDays(logsByDay, dailyMl);
  if (days.isEmpty) return 0;
  var best = 1, run = 1;
  for (var i = 1; i < days.length; i++) {
    if (days[i - 1] == _minusDays(days[i], 1)) {
      run++;
      best = max(best, run);
    } else {
      run = 1;
    }
  }
  return best;
}

String pickLine(
  Persona persona,
  Mood mood, {
  DayPart dayPart = DayPart.anytime,
  String? previousLine,
  Random? random,
}) {
  final lines = PersonaCatalog.linesFor(persona, mood, dayPart);
  if (lines.isEmpty) return 'Sip some water.';
  if (lines.length == 1) return lines.first;
  final candidates = previousLine == null
      ? lines
      : lines.where((l) => l != previousLine).toList();
  final pool = candidates.isEmpty ? lines : candidates;
  return pool[(random ?? Random()).nextInt(pool.length)];
}

String pickFrom(List<String> lines, {String? previous, Random? random}) {
  if (lines.isEmpty) return '';
  final pool = lines.where((l) => l != previous).toList();
  final from = pool.isEmpty ? lines : pool;
  return from[(random ?? Random()).nextInt(from.length)];
}

Map<DateTime, int> totalsByDay(Iterable<DrinkLog> logs) {
  final map = <DateTime, int>{};
  for (final log in logs) {
    final day = dateOnly(DateTime.fromMillisecondsSinceEpoch(log.timestampMs));
    map[day] = (map[day] ?? 0) + log.amountMl;
  }
  return map;
}

const _mlPerOz = 29.5735;

int mlToDisplay(int ml, VolumeUnit unit) =>
    unit == VolumeUnit.ml ? ml : (ml / _mlPerOz).round();

int displayToMl(int value, VolumeUnit unit) =>
    unit == VolumeUnit.ml ? value : (value * _mlPerOz).round();

String unitLabel(VolumeUnit unit) => unit == VolumeUnit.ml ? 'ml' : 'oz';

String formatVolume(int ml, VolumeUnit unit) =>
    '${mlToDisplay(ml, unit)} ${unitLabel(unit)}';
