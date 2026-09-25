import 'dart:math';

import 'models.dart';
import 'mood_engine.dart';
import 'persona_catalog.dart';

const minNudgeIntervalMs = 45 * 60 * 1000;
const maxWaterNudges = 8;
const _msPerHour = 60 * 60 * 1000;

int _hourOnSameDayMs(int nowMs, int hour) {
  final now = DateTime.fromMillisecondsSinceEpoch(nowMs);
  return DateTime(now.year, now.month, now.day, hour).millisecondsSinceEpoch;
}

int sleepBoundaryMs(int nowMs, int wakeHour, int sleepHour) {
  final wakeMs = _hourOnSameDayMs(nowMs, wakeHour);
  final sleepMs = _hourOnSameDayMs(nowMs, sleepHour);
  if (sleepHour > wakeHour) return sleepMs;
  // Overnight: once we're past wake, sleep is tomorrow.
  return nowMs >= wakeMs ? sleepMs + 24 * _msPerHour : sleepMs;
}

/// Spread the remaining water over the rest of the waking day,
/// at most [maxWaterNudges] reminders, never closer than 45 minutes.
List<int> nextNudgeTimes({
  required int nowMs,
  required Profile profile,
  required int consumedMl,
}) {
  final remaining = max(0, profile.dailyMl - consumedMl);
  if (remaining == 0) return const [];

  final wakeMs = _hourOnSameDayMs(nowMs, profile.wakeHour);
  final sleepMs = sleepBoundaryMs(nowMs, profile.wakeHour, profile.sleepHour);
  final windowStart = max(nowMs + minNudgeIntervalMs, wakeMs);
  if (windowStart >= sleepMs) return const [];

  final windowMs = sleepMs - windowStart;
  final sipsLeft = max(1.0, remaining / 250.0);
  final interval = max(minNudgeIntervalMs, (windowMs / sipsLeft).round());

  final times = <int>[];
  var cursor = windowStart;
  while (cursor < sleepMs && times.length < maxWaterNudges) {
    times.add(cursor);
    cursor += interval;
  }
  return times;
}

/// Nudges that already fired since the last drink — each one the user let slide.
int ignoredNudgeCount({
  required List<int> nudgeHistory,
  required int nowMs,
  required int? lastLogMs,
}) {
  final dayStart = dateOnly(DateTime.fromMillisecondsSinceEpoch(nowMs))
      .millisecondsSinceEpoch;
  final since = max(dayStart, lastLogMs ?? 0);
  return nudgeHistory.where((t) => t <= nowMs && t > since).length;
}

Mood projectedMood({
  required int atMs,
  required Profile profile,
  required int consumedMl,
  required int ignoredCount,
}) {
  final hour = DateTime.fromMillisecondsSinceEpoch(atMs).hour;
  final progress = profile.dailyMl <= 0 ? 0.0 : consumedMl / profile.dailyMl;
  return computeMood(
    progressRatio: progress,
    expectedRatio: expectedRatio(hour, profile.wakeHour, profile.sleepHour),
    ignoredNudgeCount: ignoredCount,
  );
}

class PlannedNudge {
  const PlannedNudge({
    required this.atMs,
    required this.title,
    required this.body,
    required this.skipLabel,
    required this.mood,
  });

  final int atMs;
  final String title;
  final String body;
  final String skipLabel;
  final Mood mood;
}

/// Build the water nudges with persona lines. Each later nudge assumes the earlier
/// ones were ignored, so the tone escalates if the user keeps ignoring.
List<PlannedNudge> planWaterNudges({
  required int nowMs,
  required Profile profile,
  required int consumedMl,
  required int ignoredSoFar,
  Persona? persona,
  Random? random,
}) {
  final speaker = persona ?? PersonaCatalog.get(profile.personaId);
  final times = nextNudgeTimes(
    nowMs: nowMs,
    profile: profile,
    consumedMl: consumedMl,
  );
  String? previous;
  final rnd = random ?? Random();
  return [
    for (var i = 0; i < times.length; i++)
      () {
        final mood = projectedMood(
          atMs: times[i],
          profile: profile,
          consumedMl: consumedMl,
          ignoredCount: ignoredSoFar + i,
        );
        final part = dayPartFor(
          DateTime.fromMillisecondsSinceEpoch(times[i]).hour,
          profile.wakeHour,
          profile.sleepHour,
        );
        final body = pickLine(
          speaker,
          mood,
          dayPart: part,
          previousLine: previous,
          random: rnd,
        );
        previous = body;
        final skips =
            speaker.skipLabels[mood] ??
            speaker.skipLabels[Mood.neutral] ??
            const ['Skip'];
        return PlannedNudge(
          atMs: times[i],
          title: '${speaker.emoji} ${speaker.displayName}',
          body: body,
          skipLabel: skips[rnd.nextInt(skips.length)],
          mood: mood,
        );
      }(),
  ];
}

/// Next occurrences (up to [days]) of a medication that aren't already logged.
List<DateTime> nextMedOccurrences({
  required Medication med,
  required DateTime now,
  required Set<String> loggedDateKeys,
  int days = 3,
}) {
  final out = <DateTime>[];
  for (var offset = 0; offset < days + 1 && out.length < days; offset++) {
    final at = DateTime(
      now.year,
      now.month,
      now.day + offset,
      med.hour,
      med.minute,
    );
    if (!at.isAfter(now)) continue;
    if (loggedDateKeys.contains(dateKey(at))) continue;
    out.add(at);
  }
  return out;
}
