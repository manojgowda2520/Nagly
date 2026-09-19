import 'access.dart';
import 'models.dart';
import 'mood_engine.dart';

/// Tags synced to OneSignal so cloud Journeys (win-back, streak milestones,
/// trial ending, upsell) can be segmented and written in the persona's voice.
///
/// Exactly 6 tags — the OneSignal free plan's limit — and never health data.
/// Time-based tags are Unix timestamps (seconds) so OneSignal's "time elapsed"
/// filters stay accurate even when the user stops opening the app.
Map<String, String> computePushTags({
  required Profile profile,
  required List<DrinkLog> recentLogs,
  required Access access,
  required DateTime now,
}) {
  final byDay = totalsByDay(recentLogs);
  final lastLogMs = recentLogs.isEmpty
      ? now.millisecondsSinceEpoch
      : recentLogs.map((l) => l.timestampMs).reduce((a, b) => a > b ? a : b);
  final trialStartedMs = access.trialEndsAtMs == null
      ? null
      : access.trialEndsAtMs! - trialMs;

  return {
    'persona_id': profile.personaId,
    'care_mode': profile.careMode.name,
    'current_streak': '${currentStreak(byDay, profile.dailyMl, now)}',
    'last_log_at': '${lastLogMs ~/ 1000}',
    'trial_started_at': '${(trialStartedMs ?? 0) ~/ 1000}',
    'is_pro': '${access.isPro}',
  };
}
