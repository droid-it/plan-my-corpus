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

    fun defaults() = listOf(
        InvestmentCategory(EQUITY, "Equity", 12.0),
        InvestmentCategory(DEBT, "Debt", 7.0),
        InvestmentCategory(HYBRID, "Hybrid", 10.0)
    )
}
