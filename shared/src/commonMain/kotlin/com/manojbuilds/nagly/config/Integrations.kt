package com.manojbuilds.nagly.config

/**
 * Single place to flip sandbox fakes → real SDK clients.
 * Keep keys empty until accounts exist; never commit secrets.
 */
object Integrations {
    const val SANDBOX_MODE: Boolean = true

    // TODO: RevenueCat Android public SDK key
    const val REVENUECAT_ANDROID_KEY: String = ""

    // TODO: RevenueCat iOS public SDK key
    const val REVENUECAT_IOS_KEY: String = ""

    // TODO: OneSignal App ID
    const val ONESIGNAL_APP_ID: String = ""

    // TODO: AdMob application id
    const val ADMOB_APP_ID: String = ""

    // TODO: AdMob rewarded unit id
    const val ADMOB_REWARDED_UNIT_ID: String = ""

    const val PRIVACY_URL: String = "https://example.com/nagly/privacy"
    const val TERMS_URL: String = "https://example.com/nagly/terms"
    const val MANAGE_SUBSCRIPTION_URL: String = "https://example.com/nagly/subscription"
    const val SUPPORT_EMAIL: String = "support@example.com"
    const val SHARE_MESSAGE: String = "Stay hydrated with Nagly — your family nags you to drink water. https://example.com/nagly"
}
