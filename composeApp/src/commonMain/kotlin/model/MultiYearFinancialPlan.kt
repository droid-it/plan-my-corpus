package model

import kotlinx.serialization.Serializable

/**
 * Multi-year financial plan container
 * Version 2.0 format that supports tracking financial data across multiple years
 * Replaces the single-snapshot FinancialPlanData model
 */
@Serializable
data class MultiYearFinancialPlan(
    val version: String = "2.0",                        // Format version
    val userProfile: UserProfile,                       // Shared across all years
    val inflationCategories: List<InflationCategory>,   // Shared across all years
    val investmentCategories: List<InvestmentCategory>, // Shared across all years
    val yearlySnapshots: List<YearlySnapshot>,          // Time-series data
    val currentYear: Int,                               // Which year is "active"
    val baselineYear: Int,                              // First year of tracking
    val exportTimestamp: Long = 0,
    val metadata: ExportMetadata = ExportMetadata(),
    val isSampleData: Boolean = false                   // Flag to indicate if this is sample demo data
)
