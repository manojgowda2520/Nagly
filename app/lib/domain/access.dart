import 'models.dart';
import 'persona_catalog.dart';

const tempUnlockMs = 24 * 60 * 60 * 1000;
const trialDays = 7;
const trialMs = trialDays * 24 * 60 * 60 * 1000;

/// Medication reminders are never fully paywalled: everyone keeps one, forever.
/// Nobody should lose the reminder for their most important pill because they didn't pay.
const freeMedicationLimit = 1;

/// Unlock-table key for a 24h rewarded-ad unlock of unlimited medication reminders.
const medsUnlockKey = 'meds';

/// Everything that decides what the user can use right now.
///
/// Free forever: Mom personas, water, and one medication reminder.
/// Full access: Pro purchase, or the 7-day app-managed welcome trial (no card).
/// Rewarded ad: 24h unlock of one locked relationship (Dad, Grandparent, Bestie).
class Access {
  const Access({
    required this.isPro,
    required this.trialEndsAtMs,
    required this.unlocks,
    required this.nowMs,
  });

  final bool isPro;

  /// Null until onboarding finishes and the trial starts.
  final int? trialEndsAtMs;

  /// Active unlock expiries keyed by relationship id (or [medsUnlockKey]).
  final Map<String, int> unlocks;
  final int nowMs;

  bool get inTrial => !isPro && trialEndsAtMs != null && nowMs < trialEndsAtMs!;

  bool get trialEnded =>
      !isPro && trialEndsAtMs != null && nowMs >= trialEndsAtMs!;

  bool get fullAccess => isPro || inTrial;

  /// Whole days left in the trial, rounded up (the last day reads "1 day left").
  int get trialDaysLeft {
    if (!inTrial) return 0;
    return ((trialEndsAtMs! - nowMs) / (24 * 60 * 60 * 1000)).ceil();
  }

  bool get trialEndsWithin24h =>
      inTrial && trialEndsAtMs! - nowMs <= 24 * 60 * 60 * 1000;

  bool _unlocked(String key) => (unlocks[key] ?? 0) > nowMs;

  bool relationshipAccessible(String relationshipId) {
    if (PersonaCatalog.relationship(relationshipId).tier == Tier.free)
      return true;
    return fullAccess || _unlocked(relationshipId);
  }

  bool personaAccessible(Persona persona) =>
      relationshipAccessible(persona.relationshipId);

  /// How many medications get reminders. Null = unlimited.
  int? get medicationLimit =>
      fullAccess || _unlocked(medsUnlockKey) ? null : freeMedicationLimit;

  bool canAddMedication(int currentCount) {
    final limit = medicationLimit;
    return limit == null || currentCount < limit;
  }

  /// Which medications are actively reminded. Beyond the free limit, the earliest
  /// added stay active and the rest are paused (never deleted).
  List<Medication> activeMedications(List<Medication> meds) {
    final limit = medicationLimit;
    if (limit == null || meds.length <= limit) return meds;
    final byAge = [...meds]..sort((a, b) => a.id.compareTo(b.id));
    final keep = byAge.take(limit).map((m) => m.id).toSet();
    return meds.where((m) => keep.contains(m.id)).toList();
  }

  /// Expiry of a temporary ad unlock, when it's the only reason for access.
  int? adUnlockExpiry(String key) =>
      !fullAccess && _unlocked(key) ? unlocks[key] : null;
}

/// When a trial or ad unlock lapses, drop back to Mom. Returns null if nothing changes.
({Profile profile, String message})? resolvePersonaFallback(
  Profile profile,
  Access access,
) {
  final persona = PersonaCatalog.get(profile.personaId);
  if (access.personaAccessible(persona)) return null;
  return (
    profile: profile.copyWith(
      personaId: PersonaCatalog.freeFallbackId,
      customName: '',
      customEmoji: '',
    ),
    message:
        "${persona.displayName} had to go for now. Mom's back on duty — she never really left.",
  );
}

String countdownLabel(int expiresAtMs, int nowMs) {
  final remaining = expiresAtMs - nowMs < 0 ? 0 : expiresAtMs - nowMs;
  final hours = remaining ~/ (60 * 60 * 1000);
  final minutes = (remaining % (60 * 60 * 1000)) ~/ (60 * 1000);
  return hours > 0 ? '${hours}h ${minutes}m left' : '${minutes}m left';
}
