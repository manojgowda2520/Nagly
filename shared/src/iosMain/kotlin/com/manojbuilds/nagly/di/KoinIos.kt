package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.notifications.NotificationCoordinator
import com.manojbuilds.nagly.push.PushTagSync
import org.koin.mp.KoinPlatform

fun initKoinIos() {
    initKoin(platformModules = listOf(iosPlatformModule()))
    val koin = KoinPlatform.getKoin()
    koin.get<NotificationCoordinator>().start()
    koin.get<PushTagSync>().start()
}
