package com.manojbuilds.nagly.notifications

/**
 * Platform notification bridge.
 * Step 5: permission stub. Step 7: real scheduling + action button.
 */
expect class Notifier {
    suspend fun requestPermission(): Boolean
    fun schedule(id: Int, atEpochMs: Long, title: String, body: String)
    fun cancelAll()
}
