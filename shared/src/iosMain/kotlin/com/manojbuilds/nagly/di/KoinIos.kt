package com.manojbuilds.nagly.di

fun initKoinIos() {
    initKoin(platformModules = listOf(iosPlatformModule()))
}
