package model

import kotlinx.serialization.Serializable

@Serializable
data class InvestmentCategory(
    val id: String,
    val name: String,
    val preRetirementXIRR: Double // percentage, e.g., 12.0 for 12%
)

object DefaultInvestmentCategories {
    val EQUITY = "equity"
    val DEBT = "debt"
    val HYBRID = "hybrid"
    val REAL_ESTATE = "real-estate"
    val GOLD = "gold"

    fun defaults() = listOf(
        InvestmentCategory(EQUITY, "Equity (Stocks/Mutual Funds)", 12.0),
        InvestmentCategory(DEBT, "Debt (Bonds/FD)", 7.0),
        InvestmentCategory(REAL_ESTATE, "Real Estate", 8.0),
        InvestmentCategory(GOLD, "Gold", 6.0),
        InvestmentCategory(HYBRID, "Mixed Portfolio", 10.0)
    )
}
