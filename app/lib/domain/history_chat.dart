import 'package:intl/intl.dart';

import 'models.dart';
import 'mood_engine.dart';
import 'persona_catalog.dart';

sealed class ChatItem {
  const ChatItem();
}

class DayDivider extends ChatItem {
  const DayDivider(this.label);
  final String label;
}

class ChatMessage extends ChatItem {
  const ChatMessage({
    required this.id,
    required this.isUser,
    required this.text,
    required this.timestampMs,
    this.mood,
    this.isMed = false,
  });

  final String id;
  final bool isUser;
  final String text;
  final int timestampMs;

  /// Persona messages carry the mood they were said in (drives the mood stripe).
  final Mood? mood;
  final bool isMed;
}

/// The signature History screen: every drink becomes a little conversation —
/// the persona's line at that moment, then your reply.
List<ChatItem> buildChatTimeline({
  required List<DrinkLog> logs,
  required List<MedLog> medLogs,
  required Map<int, Medication> medsById,
  required Profile profile,
  required Persona persona,
}) {
  final events = <({int ms, DrinkLog? drink, MedLog? med})>[
    for (final l in logs) (ms: l.timestampMs, drink: l, med: null),
    for (final m in medLogs) (ms: m.atMs, drink: null, med: m),
  ]..sort((a, b) => a.ms.compareTo(b.ms));
  if (events.isEmpty) return const [];

  final items = <ChatItem>[];
  final dayFmt = DateFormat('EEEE, MMMM d');
  DateTime? currentDay;
  var dayTotal = 0;
  String? previous;

  for (final e in events) {
    final at = DateTime.fromMillisecondsSinceEpoch(e.ms);
    final day = dateOnly(at);
    if (day != currentDay) {
      currentDay = day;
      dayTotal = 0;
      items.add(DayDivider(dayFmt.format(day)));
    }
    if (e.drink case final log?) {
      final before = profile.dailyMl <= 0 ? 0.0 : dayTotal / profile.dailyMl;
      dayTotal += log.amountMl;
      final after = profile.dailyMl <= 0 ? 0.0 : dayTotal / profile.dailyMl;
      final expected = expectedRatio(
        at.hour,
        profile.wakeHour,
        profile.sleepHour,
      );
      // The nag that prompted this sip reflects where you were *before* drinking —
      // unless this sip finished the goal, in which case she's proud.
      final mood = after >= 1
          ? Mood.proud
          : computeMood(
              progressRatio: before,
              expectedRatio: expected,
              ignoredNudgeCount: 0,
            );
      final lines = PersonaCatalog.linesFor(
        persona,
        mood,
        dayPartFor(at.hour, profile.wakeHour, profile.sleepHour),
      );
      final pool = lines.where((l) => l != previous).toList();
      final from = pool.isEmpty ? lines : pool;
      final line = from.isEmpty ? 'Nice sip!' : from[log.id % from.length];
      previous = line;
      items
        ..add(
          ChatMessage(
            id: 'p-${log.id}',
            isUser: false,
            text: line,
            timestampMs: e.ms - 60000,
            mood: mood,
          ),
        )
        ..add(
          ChatMessage(
            id: 'u-${log.id}',
            isUser: true,
            text: '+${formatVolume(log.amountMl, profile.volumeUnit)}',
            timestampMs: e.ms,
          ),
        );
    } else if (e.med case final m?) {
      final med = medsById[m.medId];
      final name = med?.name ?? 'medication';
      if (m.status == MedStatus.taken) {
        items
          ..add(
            ChatMessage(
              id: 'mu-${m.id}',
              isUser: true,
              text: '💊 Took $name',
              timestampMs: e.ms,
              isMed: true,
            ),
          )
          ..add(
            ChatMessage(
              id: 'mp-${m.id}',
              isUser: false,
              text: PersonaCatalog.fillMed(
                persona.medTaken[m.id % persona.medTaken.length],
                name,
              ),
              timestampMs: e.ms + 1,
              mood: Mood.proud,
              isMed: true,
            ),
          );
      } else {
        items
          ..add(
            ChatMessage(
              id: 'mu-${m.id}',
              isUser: true,
              text: '💊 Skipped $name',
              timestampMs: e.ms,
              isMed: true,
            ),
          )
          ..add(
            ChatMessage(
              id: 'mp-${m.id}',
              isUser: false,
              text: PersonaCatalog.fillMed(
                persona.medMissed[m.id % persona.medMissed.length],
                name,
              ),
              timestampMs: e.ms + 1,
              mood: Mood.disappointed,
              isMed: true,
            ),
          );
      }
    }
  }
  return items;
}
