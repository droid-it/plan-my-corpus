package model

import kotlinx.serialization.Serializable

@Serializable
data class FutureLumpsumInvestment(
    val id: String,
    val name: String,
    val plannedAmount: Double,
    val plannedYear: Int,
    val categoryId: String,
    val isEnabled: Boolean = true // Allow temporarily disabling without deleting
)
