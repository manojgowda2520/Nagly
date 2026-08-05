package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.notifications.NotificationCoordinator
import org.koin.mp.KoinPlatform

fun initKoinIos() {
    initKoin(platformModules = listOf(iosPlatformModule()))
    KoinPlatform.getKoin().get<NotificationCoordinator>().start()
}
