package com.manojbuilds.nagly.data

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal fun todayRangeMs(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Pair<Long, Long> {
    val today = clock.now().toLocalDateTime(timeZone).date
    val start = today.atStartOfDayIn(timeZone).toEpochMilliseconds()
    val end = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
    return start to end
}
