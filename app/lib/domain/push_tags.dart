import 'access.dart';
import 'models.dart';
import 'mood_engine.dart';

/// Tags synced to OneSignal so cloud Journeys (win-back, streak milestones,
/// trial ending, upsell) can be segmented and written in the persona's voice.
///
/// Exactly 6 tags — the OneSignal free plan's limit. Never includes health data
/// (no medication names, no intake amounts).
Map<String, String> computePushTags({
  required Profile profile,
  required List<DrinkLog> recentLogs,
  required Access access,
  required DateTime now,
}) {
  final byDay = totalsByDay(recentLogs);
  final today = dateOnly(now);
  final lastLogMs = recentLogs.isEmpty
      ? null
      : recentLogs.map((l) => l.timestampMs).reduce((a, b) => a > b ? a : b);
  final lastLogDaysAgo = lastLogMs == null
      ? 999
      : today
            .difference(
              dateOnly(DateTime.fromMillisecondsSinceEpoch(lastLogMs)),
            )
            .inDays
            .clamp(0, 999);

  return {
    'persona_id': profile.personaId,
    'care_mode': profile.careMode.name,
    'current_streak': '${currentStreak(byDay, profile.dailyMl, now)}',
    'last_log_days_ago': '$lastLogDaysAgo',
    // -1 = no trial (Pro, or trial over); 0 = last day.
    'trial_days_left': '${access.inTrial ? access.trialDaysLeft - 1 : -1}',
    'is_pro': '${access.isPro}',
  };
}
