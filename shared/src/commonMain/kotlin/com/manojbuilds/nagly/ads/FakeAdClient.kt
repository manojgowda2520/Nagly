package com.manojbuilds.nagly.ads

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAdClient : AdClient {
    private val _ready = MutableStateFlow(true)
    override val isRewardedReady: StateFlow<Boolean> = _ready.asStateFlow()

    var cancelled: Boolean = false

    override suspend fun loadRewarded() {
        delay(200)
        _ready.value = true
        cancelled = false
    }

    override suspend fun showRewarded(): Result<Unit> {
        cancelled = false
        _ready.value = false
        repeat(30) {
            if (cancelled) {
                _ready.value = true
                return Result.failure(Exception("Ad cancelled"))
            }
            delay(100)
        }
        _ready.value = true
        return Result.success(Unit)
    }

    fun cancel() {
        cancelled = true
    }
}
