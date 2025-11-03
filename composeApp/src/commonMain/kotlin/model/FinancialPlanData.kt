package model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialPlanData(
    val userProfile: UserProfile = UserProfile(),
    val inflationCategories: List<InflationCategory> = DefaultInflationCategories.defaults(),
    val investmentCategories: List<InvestmentCategory> = DefaultInvestmentCategories.defaults(),
    val investments: List<Investment> = emptyList(),
    val ongoingContributions: List<OngoingContribution> = emptyList(),
    val goals: List<FinancialGoal> = emptyList(),
    val snapshotTimestamp: Long = 0 // For year-over-year comparison
)
