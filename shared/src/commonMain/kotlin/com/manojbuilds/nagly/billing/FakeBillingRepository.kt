package com.manojbuilds.nagly.billing

import com.manojbuilds.nagly.db.NaglyDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBillingRepository(
    private val database: NaglyDatabase,
) : BillingRepository {
    private val _isPro = MutableStateFlow(readPro())
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override suspend fun refresh() {
        _isPro.value = readPro()
    }

    override suspend fun purchase(packageId: String): Result<Unit> {
        delay(1_000)
        writePro(true)
        _isPro.value = true
        return Result.success(Unit)
    }

    override suspend fun restore(): Result<Unit> {
        delay(500)
        // Sandbox restore re-reads persisted flag (simulates prior purchase on device).
        refresh()
        return Result.success(Unit)
    }

    private fun readPro(): Boolean {
        val value = database.appFlagQueries.select(KEY_IS_PRO).executeAsOneOrNull()
        return value != null && value != 0L
    }

    private fun writePro(enabled: Boolean) {
        database.appFlagQueries.upsert(KEY_IS_PRO, if (enabled) 1L else 0L)
    }

    companion object {
        const val KEY_IS_PRO = "is_pro"
        const val PACKAGE_MONTHLY = "monthly"
        const val PACKAGE_ANNUAL = "annual"
    }
}
