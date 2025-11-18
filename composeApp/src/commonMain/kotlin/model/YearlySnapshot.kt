package model

import kotlinx.serialization.Serializable

/**
 * Represents financial data for a specific year
 * Can be either a historical record (locked) or current active data
 */
@Serializable
data class YearlySnapshot(
    val year: Int,                                      // e.g., 2024
    val snapshotDate: String,                           // "2024-12-31" (ISO format)
    val snapshotType: SnapshotType,                     // HISTORICAL or CURRENT
    val investments: List<Investment>,
    val futureLumpsumInvestments: List<FutureLumpsumInvestment> = emptyList(),
    val ongoingContributions: List<OngoingContribution>,
    val goals: List<FinancialGoal>,
    val actualAnnualContributions: Double = 0.0,       // Total actually contributed this year
    val actualGoalSpending: Double = 0.0,              // Total actually spent on goals this year
    val notes: String = ""                              // User notes for this year
)
