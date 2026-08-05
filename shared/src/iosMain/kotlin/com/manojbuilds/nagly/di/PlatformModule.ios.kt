package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.data.DatabaseDriverFactory
import com.manojbuilds.nagly.notifications.Notifier
import org.koin.dsl.module

fun iosPlatformModule() = module {
    single { DatabaseDriverFactory() }
    single { Notifier() }
}
