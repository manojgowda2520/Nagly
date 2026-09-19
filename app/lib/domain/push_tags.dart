import 'access.dart';
import 'models.dart';
import 'mood_engine.dart';

/// Tags synced to OneSignal so cloud journeys (win-back, streak milestones,
/// trial ending, upsell) can be segmented and written in the persona's voice.
Map<String, String> computePushTags({
  required Profile profile,
  required List<DrinkLog> recentLogs,
  required Access access,
  required DateTime now,
  int medCount = 0,
  String bondLevel = '',
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
    'daily_goal_ml': '${profile.dailyMl}',
    'last_log_days_ago': '$lastLogDaysAgo',
    'is_pro': '${access.isPro}',
    'in_trial': '${access.inTrial}',
    'trial_ends_at': '${(access.trialEndsAtMs ?? 0) ~/ 1000}',
    'med_count': '$medCount',
    'bond_level': bondLevel,
  };
}
