package com.manojbuilds.nagly.di

import android.content.Context
import com.manojbuilds.nagly.notifications.NotificationCoordinator
import org.koin.android.ext.koin.androidContext
import org.koin.mp.KoinPlatform

fun initKoinAndroid(context: Context) {
    initKoin(platformModules = listOf(androidPlatformModule())) {
        androidContext(context)
    }
    KoinPlatform.getKoin().get<NotificationCoordinator>().start()
}
