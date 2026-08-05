package com.manojbuilds.nagly.di

import com.manojbuilds.nagly.data.DatabaseDriverFactory
import org.koin.dsl.module

fun iosPlatformModule() = module {
    single { DatabaseDriverFactory() }
}
