import 'package:flutter_test/flutter_test.dart';
import 'package:nagly/domain/access.dart';
import 'package:nagly/domain/history_chat.dart';
import 'package:nagly/domain/insights.dart';
import 'package:nagly/domain/models.dart';
import 'package:nagly/domain/mood_engine.dart';
import 'package:nagly/domain/nudge_plan.dart';
import 'package:nagly/domain/persona_catalog.dart';
import 'package:nagly/domain/push_tags.dart';
import 'package:nagly/domain/relationship_meter.dart';

void main() {
  group('mood', () {
    test('proud once goal is met, regardless of ignores', () {
      expect(
        computeMood(progressRatio: 1, expectedRatio: 0.5, ignoredNudgeCount: 5),
        Mood.proud,
      );
    });
    test('disappointed after two ignored nudges', () {
      expect(
        computeMood(
          progressRatio: 0.5,
          expectedRatio: 0.5,
          ignoredNudgeCount: 2,
        ),
        Mood.disappointed,
      );
    });
    test('worried when more than 15% behind', () {
      expect(
        computeMood(
          progressRatio: 0.3,
          expectedRatio: 0.5,
          ignoredNudgeCount: 0,
        ),
        Mood.worried,
      );
      expect(
        computeMood(
          progressRatio: 0.4,
          expectedRatio: 0.5,
          ignoredNudgeCount: 0,
        ),
        Mood.neutral,
      );
    });
  });

  group('day math', () {
    test('expected ratio for a normal day', () {
      expect(expectedRatio(6, 7, 22), 0);
      expect(expectedRatio(22, 7, 22), 1);
      expect(expectedRatio(14, 7, 22), closeTo(7 / 15, 1e-9));
    });
    test('expected ratio wraps midnight for night owls', () {
      expect(expectedRatio(23, 22, 6), closeTo(1 / 8, 1e-9));
      expect(expectedRatio(3, 22, 6), closeTo(5 / 8, 1e-9));
      expect(expectedRatio(12, 22, 6), 1);
    });
    test('day parts split the waking window in thirds', () {
      expect(dayPartFor(8, 7, 22), DayPart.morning);
      expect(dayPartFor(14, 7, 22), DayPart.afternoon);
      expect(dayPartFor(20, 7, 22), DayPart.evening);
    });
    test('recommended goal scales with weight and activity, rounded to 50', () {
      expect(recommendedDailyMl(70, ActivityLevel.sedentary), 2450);
      expect(recommendedDailyMl(70, ActivityLevel.light), 2700);
      expect(recommendedDailyMl(30, ActivityLevel.sedentary), 1500);
      expect(recommendedDailyMl(200, ActivityLevel.veryActive), 4800);
    });
    test('volume conversions round-trip closely', () {
      expect(mlToDisplay(2000, VolumeUnit.oz), 68);
      expect(displayToMl(68, VolumeUnit.oz), closeTo(2000, 15));
    });
  });

  group('streaks', () {
    final today = DateTime(2026, 9, 19);
    Map<DateTime, int> days(List<int> offsets) => {
      for (final o in offsets) DateTime(2026, 9, 19 - o): 2000,
    };

    test(
      'counts back from today',
      () => expect(currentStreak(days([0, 1, 2]), 2000, today), 3),
    );
    test(
      'an unfinished today does not break yesterday\'s streak',
      () => expect(currentStreak(days([1, 2]), 2000, today), 2),
    );
    test(
      'a gap ends the streak',
      () => expect(currentStreak(days([2, 3]), 2000, today), 0),
    );
    test(
      'best streak finds the longest run',
      () => expect(bestStreak(days([0, 2, 3, 4, 7]), 2000), 3),
    );
    test('streaks survive month boundaries', () {
      final m = {DateTime(2026, 8, 31): 2000, DateTime(2026, 9, 1): 2000};
      expect(currentStreak(m, 2000, DateTime(2026, 9, 1)), 2);
    });
  });

  group('bond meter', () {
    test('takes the higher of streak and consistency', () {
      expect(computeRelationshipLevel(0, 10), RelationshipLevel.family);
      expect(computeRelationshipLevel(14, 0), RelationshipLevel.soulReminder);
      expect(computeRelationshipLevel(0, 0), RelationshipLevel.stranger);
    });
    test('progress is capped at max level', () {
      expect(
        relationshipProgressToNext(RelationshipLevel.soulReminder, 30, 14),
        1,
      );
      expect(relationshipProgressToNext(RelationshipLevel.stranger, 0, 0), 0);
    });
  });

  group('nudge plan', () {
    const profile = Profile(
      dailyMl: 2000,
      wakeHour: 7,
      sleepHour: 22,
      onboarded: true,
    );
    final nine = DateTime(2026, 9, 19, 9).millisecondsSinceEpoch;

    test('no nudges once the goal is met', () {
      expect(
        nextNudgeTimes(nowMs: nine, profile: profile, consumedMl: 2000),
        isEmpty,
      );
    });
    test(
      'nudges stay inside waking hours, spaced at least 45 minutes, max 8',
      () {
        final times = nextNudgeTimes(
          nowMs: nine,
          profile: profile,
          consumedMl: 0,
        );
        expect(times.length, lessThanOrEqualTo(maxWaterNudges));
        expect(times.first, nine + minNudgeIntervalMs);
        for (var i = 1; i < times.length; i++) {
          expect(
            times[i] - times[i - 1],
            greaterThanOrEqualTo(minNudgeIntervalMs),
          );
        }
        expect(
          times.last,
          lessThan(DateTime(2026, 9, 19, 22).millisecondsSinceEpoch),
        );
      },
    );
    test('no nudges after bedtime', () {
      final late = DateTime(2026, 9, 19, 21, 30).millisecondsSinceEpoch;
      expect(
        nextNudgeTimes(nowMs: late, profile: profile, consumedMl: 0),
        isEmpty,
      );
    });
    test('ignored = fired since the last drink, today only', () {
      final h = [
        8,
        10,
        12,
        14,
      ].map((x) => DateTime(2026, 9, 19, x).millisecondsSinceEpoch).toList();
      final now = DateTime(2026, 9, 19, 13).millisecondsSinceEpoch;
      expect(
        ignoredNudgeCount(nudgeHistory: h, nowMs: now, lastLogMs: null),
        3,
      );
      expect(
        ignoredNudgeCount(
          nudgeHistory: h,
          nowMs: now,
          lastLogMs: DateTime(2026, 9, 19, 11).millisecondsSinceEpoch,
        ),
        1,
      );
    });
    test('planned tone escalates when nudges keep being ignored', () {
      final plan = planWaterNudges(
        nowMs: nine,
        profile: profile,
        consumedMl: 0,
        ignoredSoFar: 0,
      );
      expect(plan.last.mood, Mood.disappointed);
      expect(
        plan.every((n) => n.body.isNotEmpty && n.skipLabel.isNotEmpty),
        isTrue,
      );
    });
    test('medication occurrences skip already-logged days', () {
      const med = Medication(id: 1, name: 'BP', hour: 9, minute: 0);
      final now = DateTime(2026, 9, 19, 8);
      final all = nextMedOccurrences(med: med, now: now, loggedDateKeys: {});
      expect(all.first, DateTime(2026, 9, 19, 9));
      final skipped = nextMedOccurrences(
        med: med,
        now: now,
        loggedDateKeys: {'2026-09-19'},
      );
      expect(skipped.first, DateTime(2026, 9, 20, 9));
      expect(skipped.length, 3);
    });
  });

  group('access & monetization', () {
    const now = 1000000000000;
    Access a({
      bool pro = false,
      int? trialEnd,
      Map<String, int> unlocks = const {},
    }) => Access(
      isPro: pro,
      trialEndsAtMs: trialEnd,
      unlocks: unlocks,
      nowMs: now,
    );

    test('Mom is always free; Dad needs Pro, trial or an ad unlock', () {
      final dad = PersonaCatalog.get('punjabi_dad');
      expect(a().personaAccessible(PersonaCatalog.get('indian_mom')), isTrue);
      expect(a().personaAccessible(dad), isFalse);
      expect(a(pro: true).personaAccessible(dad), isTrue);
      expect(a(trialEnd: now + 1).personaAccessible(dad), isTrue);
      expect(a(trialEnd: now - 1).personaAccessible(dad), isFalse);
      expect(a(unlocks: {'dad': now + 5}).personaAccessible(dad), isTrue);
      expect(a(unlocks: {'dad': now - 5}).personaAccessible(dad), isFalse);
    });
    test(
      'one medication reminder is free forever; extras pause, never delete',
      () {
        const meds = [
          Medication(id: 3, name: 'C', hour: 8, minute: 0),
          Medication(id: 1, name: 'A', hour: 9, minute: 0),
        ];
        expect(a().canAddMedication(0), isTrue);
        expect(a().canAddMedication(1), isFalse);
        expect(a(pro: true).canAddMedication(10), isTrue);
        expect(a().activeMedications(meds).map((m) => m.id), [1]);
        expect(a(trialEnd: now + 1).activeMedications(meds).length, 2);
      },
    );
    test('trial days round up and flag the final 24h', () {
      final t = a(trialEnd: now + 30 * 60 * 60 * 1000);
      expect(t.trialDaysLeft, 2);
      expect(t.trialEndsWithin24h, isFalse);
      expect(a(trialEnd: now + 60 * 60 * 1000).trialEndsWithin24h, isTrue);
    });
    test('lapsed persona falls back to Mom with a message', () {
      const p = Profile(personaId: 'corny_dad', onboarded: true);
      final f = resolvePersonaFallback(p, a(trialEnd: now - 1));
      expect(f?.profile.personaId, PersonaCatalog.freeFallbackId);
      expect(resolvePersonaFallback(p, a(pro: true)), isNull);
    });
  });

  group('catalog', () {
    test('every persona has lines for every mood and day part, skips and med lines', () {
      for (final p in PersonaCatalog.all) {
        for (final m in Mood.values) {
          for (final d in DayPart.values) {
            expect(
              PersonaCatalog.linesFor(p, m, d),
              isNotEmpty,
              reason: '${p.id} $m $d',
            );
          }
          expect(p.skipLabels[m], isNotEmpty);
        }
        expect(
          p.medDue.every((l) => l.contains('{med}') || p.id == 'silent_dad'),
          isTrue,
          reason: p.id,
        );
        expect(p.medTaken, isNotEmpty);
        expect(p.medMissed, isNotEmpty);
        expect(p.comeback, isNotEmpty);
      }
    });
    test('12 personas across 4 relationships, Mom free', () {
      expect(PersonaCatalog.all.length, 12);
      for (final r in PersonaCatalog.relationships) {
        expect(PersonaCatalog.variantsOf(r.id).length, 3);
      }
      expect(PersonaCatalog.relationship('mom').tier, Tier.free);
    });
  });

  test('chat timeline interleaves persona lines and your replies by day', () {
    final d = DateTime(2026, 9, 18, 10).millisecondsSinceEpoch;
    final items = buildChatTimeline(
      logs: [
        DrinkLog(id: 1, timestampMs: d, amountMl: 250),
        DrinkLog(id: 2, timestampMs: d + 3600000, amountMl: 500),
      ],
      medLogs: [
        MedLog(
          id: 1,
          medId: 7,
          dateKey: '2026-09-18',
          status: MedStatus.taken,
          atMs: d + 60000,
        ),
      ],
      medsById: {7: const Medication(id: 7, name: 'BP', hour: 10, minute: 0)},
      profile: const Profile(dailyMl: 700),
      persona: PersonaCatalog.get('indian_mom'),
    );
    expect(items.first, isA<DayDivider>());
    final msgs = items.whereType<ChatMessage>().toList();
    expect(
      msgs.where((m) => m.isUser).map((m) => m.text),
      containsAll(['+250 ml', '💊 Took BP', '+500 ml']),
    );
    expect(
      msgs.where((m) => !m.isUser).last.mood,
      Mood.proud,
      reason: 'the sip that completes the goal earns pride',
    );
  });

  test('weekly insights compute averages, goal days and best hour', () {
    final now = DateTime(2026, 9, 19, 20);
    final logs = [
      for (var d = 0; d < 3; d++)
        DrinkLog(
          id: d,
          timestampMs: DateTime(2026, 9, 19 - d, 10).millisecondsSinceEpoch,
          amountMl: 2000,
        ),
      DrinkLog(
        id: 9,
        timestampMs: DateTime(2026, 9, 15, 15).millisecondsSinceEpoch,
        amountMl: 500,
      ),
    ];
    final w = computeWeeklyInsights(logs: logs, dailyMl: 2000, now: now);
    expect(w.goalMetDays, 3);
    expect(w.currentStreak, 3);
    expect(w.bestHour, 10);
    expect(w.dailyAverageMl, (6000 + 500) ~/ 4);
  });

  test(
    'push tags: 6 tags, timestamps for server-side "time elapsed" filters',
    () {
      final now = DateTime(2026, 9, 19, 12);
      final lastLog = DateTime(2026, 9, 16, 9);
      final trialEnd = now.add(const Duration(days: 2));
      final tags = computePushTags(
        profile: const Profile(
          personaId: 'the_bestie',
          careMode: CareMode.medication,
        ),
        recentLogs: [
          DrinkLog(
            id: 1,
            timestampMs: lastLog.millisecondsSinceEpoch,
            amountMl: 250,
          ),
        ],
        access: Access(
          isPro: false,
          trialEndsAtMs: trialEnd.millisecondsSinceEpoch,
          unlocks: const {},
          nowMs: now.millisecondsSinceEpoch,
        ),
        now: now,
      );
      expect(tags.length, 6, reason: 'OneSignal free plan allows 6 data tags');
      expect(tags['persona_id'], 'the_bestie');
      expect(tags['last_log_at'], '${lastLog.millisecondsSinceEpoch ~/ 1000}');
      expect(
        tags['trial_started_at'],
        '${(trialEnd.millisecondsSinceEpoch - trialMs) ~/ 1000}',
      );
      expect(tags['is_pro'], 'false');
    },
  );
}
