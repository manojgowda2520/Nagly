package com.manojbuilds.nagly.notifications

/**
 * Platform notification bridge.
 */
expect class Notifier {
    suspend fun requestPermission(): Boolean
    fun schedule(
        id: Int,
        atEpochMs: Long,
        title: String,
        body: String,
        actions: NudgeActions,
    )
    fun cancelAll()
}
