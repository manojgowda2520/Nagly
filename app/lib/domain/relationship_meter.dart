import 'mood_engine.dart';

/// Bond depth derived from streak and 14-day consistency — no persistence.
enum RelationshipLevel {
  stranger('Stranger', '👋', 0, 0),
  friendly('Friendly', '🙂', 1, 3),
  close('Close', '💛', 3, 6),
  family('Family', '🏠', 7, 10),
  soulReminder('Soul Reminder', '✨', 14, 13);

  const RelationshipLevel(
    this.label,
    this.emoji,
    this.streakThreshold,
    this.consistencyThreshold,
  );

  final String label;
  final String emoji;
  final int streakThreshold;
  final int consistencyThreshold;

  RelationshipLevel? get next =>
      index + 1 < values.length ? values[index + 1] : null;
}

RelationshipLevel levelFromStreak(int streak) {
  if (streak >= 14) return RelationshipLevel.soulReminder;
  if (streak >= 7) return RelationshipLevel.family;
  if (streak >= 3) return RelationshipLevel.close;
  if (streak >= 1) return RelationshipLevel.friendly;
  return RelationshipLevel.stranger;
}

RelationshipLevel levelFromConsistency(int daysMetIn14) {
  if (daysMetIn14 >= 13) return RelationshipLevel.soulReminder;
  if (daysMetIn14 >= 10) return RelationshipLevel.family;
  if (daysMetIn14 >= 6) return RelationshipLevel.close;
  if (daysMetIn14 >= 3) return RelationshipLevel.friendly;
  return RelationshipLevel.stranger;
}

/// The higher of the streak-based and consistency-based levels.
RelationshipLevel computeRelationshipLevel(int currentStreak, int daysMetIn14) {
  final a = levelFromStreak(currentStreak);
  final b = levelFromConsistency(daysMetIn14);
  return a.index >= b.index ? a : b;
}

/// Progress toward the next level, 0..1. 1 at max level.
double relationshipProgressToNext(
  RelationshipLevel level,
  int currentStreak,
  int daysMetIn14,
) {
  final next = level.next;
  if (next == null) return 1;
  final s = (currentStreak / next.streakThreshold).clamp(0.0, 1.0);
  final c = (daysMetIn14 / next.consistencyThreshold).clamp(0.0, 1.0);
  return ((s + c) / 2).clamp(0.0, 1.0);
}

int countDaysMetGoalInWindow(
  Map<DateTime, int> logsByDay,
  int dailyMl,
  DateTime today, {
  int windowDays = 14,
}) {
  final t = dateOnly(today);
  var count = 0;
  for (var offset = 0; offset < windowDays; offset++) {
    final d = DateTime(t.year, t.month, t.day - offset);
    if ((logsByDay[d] ?? 0) >= dailyMl) count++;
  }
  return count;
}
