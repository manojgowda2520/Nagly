package com.manojbuilds.nagly.billing

import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isPro: StateFlow<Boolean>
    suspend fun refresh()
    suspend fun purchase(packageId: String): Result<Unit>
    suspend fun restore(): Result<Unit>
}

/**
 * TODO: RevenueCatBillingRepository
 * - Configure with Integrations.REVENUECAT_* keys
 * - Entitlement id: `pro`
 * - Offering id: `default` (monthly + annual)
 * - Gate: customerInfo.entitlements["pro"]?.isActive == true
 */
