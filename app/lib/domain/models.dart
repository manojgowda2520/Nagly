enum Mood { neutral, worried, disappointed, proud }

enum DayPart { morning, afternoon, evening, anytime }

enum Tier { free, pro }

enum ActivityLevel { sedentary, light, active, veryActive }

enum VolumeUnit { ml, oz }

/// What the persona nags you about. Both are free; Medication (meds + water)
/// covers one medication for free, unlimited with Pro or during the trial.
enum CareMode { hydration, medication }

extension CareModeX on CareMode {
  String get label => switch (this) {
    CareMode.hydration => 'Hydration',
    CareMode.medication => 'Medication',
  };
  String get emoji => switch (this) {
    CareMode.hydration => '💧',
    CareMode.medication => '💊',
  };
  String get tagline => switch (this) {
    CareMode.hydration => 'Water · the classic',
    CareMode.medication => 'Pills + water · 1 med free',
  };
}

class DrinkLog {
  const DrinkLog({
    required this.id,
    required this.timestampMs,
    required this.amountMl,
  });

  final int id;
  final int timestampMs;
  final int amountMl;
}

class Medication {
  const Medication({
    required this.id,
    required this.name,
    required this.hour,
    required this.minute,
    this.dose = '',
  });

  final int id;
  final String name;
  final String dose;
  final int hour;
  final int minute;

  Medication copyWith({String? name, String? dose, int? hour, int? minute}) =>
      Medication(
        id: id,
        name: name ?? this.name,
        dose: dose ?? this.dose,
        hour: hour ?? this.hour,
        minute: minute ?? this.minute,
      );
}

enum MedStatus { taken, skipped }

class MedLog {
  const MedLog({
    required this.id,
    required this.medId,
    required this.dateKey,
    required this.status,
    required this.atMs,
  });

  final int id;
  final int medId;

  /// Local calendar day, `yyyy-MM-dd`.
  final String dateKey;
  final MedStatus status;
  final int atMs;
}

class Profile {
  const Profile({
    this.dailyMl = 2000,
    this.wakeHour = 7,
    this.sleepHour = 22,
    this.personaId = 'indian_mom',
    this.onboarded = false,
    this.volumeUnit = VolumeUnit.ml,
    this.careMode = CareMode.hydration,
    this.weightKg = 70,
    this.activity = ActivityLevel.light,
  });

  final int dailyMl;
  final int wakeHour;
  final int sleepHour;
  final String personaId;
  final bool onboarded;
  final VolumeUnit volumeUnit;
  final CareMode careMode;
  final int weightKg;
  final ActivityLevel activity;

  Profile copyWith({
    int? dailyMl,
    int? wakeHour,
    int? sleepHour,
    String? personaId,
    bool? onboarded,
    VolumeUnit? volumeUnit,
    CareMode? careMode,
    int? weightKg,
    ActivityLevel? activity,
  }) => Profile(
    dailyMl: dailyMl ?? this.dailyMl,
    wakeHour: wakeHour ?? this.wakeHour,
    sleepHour: sleepHour ?? this.sleepHour,
    personaId: personaId ?? this.personaId,
    onboarded: onboarded ?? this.onboarded,
    volumeUnit: volumeUnit ?? this.volumeUnit,
    careMode: careMode ?? this.careMode,
    weightKg: weightKg ?? this.weightKg,
    activity: activity ?? this.activity,
  );

  Map<String, Object?> toJson() => {
    'dailyMl': dailyMl,
    'wakeHour': wakeHour,
    'sleepHour': sleepHour,
    'personaId': personaId,
    'onboarded': onboarded,
    'volumeUnit': volumeUnit.name,
    'careMode': careMode.name,
    'weightKg': weightKg,
    'activity': activity.name,
  };

  factory Profile.fromJson(Map<String, Object?> json) {
    T byName<T extends Enum>(List<T> values, Object? name, T fallback) =>
        values.where((v) => v.name == name).firstOrNull ?? fallback;
    const d = Profile();
    return Profile(
      dailyMl: (json['dailyMl'] as int?) ?? d.dailyMl,
      wakeHour: (json['wakeHour'] as int?) ?? d.wakeHour,
      sleepHour: (json['sleepHour'] as int?) ?? d.sleepHour,
      personaId: (json['personaId'] as String?) ?? d.personaId,
      onboarded: (json['onboarded'] as bool?) ?? d.onboarded,
      volumeUnit: byName(VolumeUnit.values, json['volumeUnit'], d.volumeUnit),
      careMode: byName(CareMode.values, json['careMode'], d.careMode),
      weightKg: (json['weightKg'] as int?) ?? d.weightKg,
      activity: byName(ActivityLevel.values, json['activity'], d.activity),
    );
  }
}
