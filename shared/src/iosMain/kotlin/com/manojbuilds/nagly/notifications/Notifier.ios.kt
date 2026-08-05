package com.manojbuilds.nagly.notifications

actual class Notifier {
    actual suspend fun requestPermission(): Boolean {
        // Real UNUserNotificationCenter handling arrives in step 7.
        return true
    }

    actual fun schedule(id: Int, atEpochMs: Long, title: String, body: String) {
        // no-op until step 7
    }

    actual fun cancelAll() {
        // no-op until step 7
    }
}
