package com.manojbuilds.nagly.push

interface PushClient {
    fun initialize()
    suspend fun requestPermission(): Boolean
    fun setTag(key: String, value: String)
    fun setExternalId(id: String)
}

/**
 * TODO: OneSignalPushClient
 * - Initialize with Integrations.ONESIGNAL_APP_ID
 * - Forward setTag / setExternalId to OneSignal user/tags APIs
 * - Do NOT route local hydration nudges through push
 */
