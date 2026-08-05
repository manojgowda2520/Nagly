package com.manojbuilds.nagly.ui.history

import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.computeMood
import com.manojbuilds.nagly.domain.dayPartFor
import com.manojbuilds.nagly.domain.expectedRatio
import com.manojbuilds.nagly.domain.model.DrinkLog
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.UserGoal
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface ChatItem {
    data class Message(val message: ChatMessage) : ChatItem
    data class DayDivider(val label: String) : ChatItem
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampMs: Long,
    val personaEmoji: String? = null,
)

fun buildChatTimeline(
    logs: List<DrinkLog>,
    goal: UserGoal,
    persona: Persona,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): List<ChatItem> {
    if (logs.isEmpty()) return emptyList()

    val sorted = logs.sortedBy { it.timestampMs }
    val byDay = sorted.groupBy {
        Instant.fromEpochMilliseconds(it.timestampMs).toLocalDateTime(timeZone).date
    }

    val items = mutableListOf<ChatItem>()
    var previousLine: String? = null

    byDay.forEach { (date, dayLogs) ->
        val label = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, " +
            "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.day}"
        items += ChatItem.DayDivider(label)

        var dayTotal = 0
        dayLogs.forEach { log ->
            val local = Instant.fromEpochMilliseconds(log.timestampMs).toLocalDateTime(timeZone)
            val hour = local.hour
            dayTotal += log.amountMl
            val progress = if (goal.dailyMl <= 0) 0f else dayTotal.toFloat() / goal.dailyMl
            val expected = expectedRatio(hour, goal.wakeHour, goal.sleepHour)
            val mood = computeMood(progress, expected, ignoredNudgeCount = 0)
            val dayPart = dayPartFor(hour, goal.wakeHour, goal.sleepHour)
            val lines = PersonaCatalog.linesFor(persona, mood, dayPart)
            val line = if (lines.isEmpty()) {
                "Nice sip!"
            } else {
                val candidates = if (previousLine == null) lines else lines.filter { it != previousLine }
                val pool = candidates.ifEmpty { lines }
                pool[(log.id % pool.size).toInt()]
            }
            previousLine = line

            items += ChatItem.Message(
                ChatMessage(
                    id = "p-${log.id}",
                    isUser = false,
                    text = line,
                    timestampMs = log.timestampMs,
                    personaEmoji = persona.emoji,
                ),
            )
            items += ChatItem.Message(
                ChatMessage(
                    id = "u-${log.id}",
                    isUser = true,
                    text = "+${log.amountMl}ml",
                    timestampMs = log.timestampMs + 1,
                ),
            )
        }
    }
    return items
}

fun formatChatTime(timestampMs: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val local = Instant.fromEpochMilliseconds(timestampMs).toLocalDateTime(timeZone)
    return local.hour.toString().padStart(2, '0') + ":" +
        local.minute.toString().padStart(2, '0')
}
