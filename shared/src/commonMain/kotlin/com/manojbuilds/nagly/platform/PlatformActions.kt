package com.manojbuilds.nagly.platform

expect object PlatformActions {
    fun openUrl(url: String)
    fun shareApp(message: String)
    fun rateApp()
    fun openEmail(to: String, subject: String)
}
