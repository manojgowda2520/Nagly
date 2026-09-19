import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../../domain/history_chat.dart';
import '../../domain/models.dart';
import '../../state/app_controller.dart';
import '../theme.dart';
import '../widgets/common.dart';
import '../widgets/persona_widgets.dart';

/// The signature screen: your hydration story told as a chat with your nagger.
class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final c = context.watch<AppController>();
    final items = c.chatTimeline;
    return DayBackground(
      hour: c.now.hour,
      intensity: 0.45,
      child: SafeArea(
        bottom: false,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 4),
              child: Text('History', style: Theme.of(context).textTheme.headlineMedium),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
              child: Text('Your story with ${c.persona.displayName} ${c.persona.emoji}',
                  style: const TextStyle(fontWeight: FontWeight.w800, color: NaglyColors.textSecondary)),
            ),
            Expanded(
              child: items.isEmpty
                  ? Center(
                      child: Padding(
                        padding: const EdgeInsets.all(32),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            PersonaAvatar(emoji: c.persona.emoji, mood: Mood.worried, size: 80),
                            const SizedBox(height: 16),
                            SpeechBubble(text: "No sips yet? I'm waiting, you know.", name: c.persona.displayName),
                          ],
                        ),
                      ),
                    )
                  : ListView.builder(
                      reverse: true,
                      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
                      itemCount: items.length,
                      itemBuilder: (context, i) => _ChatRow(item: items[items.length - 1 - i], emoji: c.persona.emoji),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ChatRow extends StatelessWidget {
  const _ChatRow({required this.item, required this.emoji});
  final ChatItem item;
  final String emoji;

  @override
  Widget build(BuildContext context) {
    return switch (item) {
      DayDivider(:final label) => Padding(
          padding: const EdgeInsets.symmetric(vertical: 14),
          child: Center(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
              decoration: BoxDecoration(color: NaglyColors.surfaceVariant, borderRadius: BorderRadius.circular(12)),
              child: Text(label, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: NaglyColors.textSecondary)),
            ),
          ),
        ),
      ChatMessage m => _Bubble(m: m, emoji: emoji),
    };
  }
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.m, required this.emoji});
  final ChatMessage m;
  final String emoji;

  @override
  Widget build(BuildContext context) {
    final time = DateFormat.Hm().format(DateTime.fromMillisecondsSinceEpoch(m.timestampMs));
    final userColor = m.isMed ? NaglyColors.med : NaglyColors.primaryDeep;
    final bubble = Container(
      constraints: BoxConstraints(maxWidth: MediaQuery.sizeOf(context).width * 0.68),
      padding: const EdgeInsets.fromLTRB(14, 10, 14, 8),
      decoration: BoxDecoration(
        color: m.isUser ? userColor : Colors.white,
        borderRadius: BorderRadius.only(
          topLeft: const Radius.circular(18),
          topRight: const Radius.circular(18),
          bottomLeft: Radius.circular(m.isUser ? 18 : 4),
          bottomRight: Radius.circular(m.isUser ? 4 : 18),
        ),
        border: m.isUser ? null : Border(left: BorderSide(color: NaglyColors.mood(m.mood ?? Mood.neutral), width: 4)),
        boxShadow: const [BoxShadow(color: Color(0x10122730), blurRadius: 8, offset: Offset(0, 3))],
      ),
      child: Column(
        crossAxisAlignment: m.isUser ? CrossAxisAlignment.end : CrossAxisAlignment.start,
        children: [
          Text(m.text,
              style: TextStyle(
                  fontSize: 15, fontWeight: FontWeight.w800, color: m.isUser ? Colors.white : NaglyColors.ink, height: 1.3)),
          const SizedBox(height: 2),
          Text(time, style: TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: m.isUser ? Colors.white70 : NaglyColors.textSecondary)),
        ],
      ),
    );
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: m.isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!m.isUser) ...[
            CircleAvatar(radius: 16, backgroundColor: const Color(0xFFFFF3DD), child: Text(emoji, style: const TextStyle(fontSize: 16))),
            const SizedBox(width: 8),
          ],
          bubble,
        ],
      ),
    );
  }
}
