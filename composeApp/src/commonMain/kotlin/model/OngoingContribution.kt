package model

import kotlinx.serialization.Serializable

enum class ContributionFrequency {
    MONTHLY,
    QUARTERLY,
    YEARLY
}

@Serializable
data class OngoingContribution(
    val id: String,
    val name: String,
    val amount: Double,
    val frequency: ContributionFrequency,
    val categoryId: String,
    val durationYears: Int? = null, // null means continue until retirement
    val stepUpPercentage: Double = 0.0, // yearly increase percentage (e.g., 10 for 10% annual increase)
    val isEnabled: Boolean = true // Allow temporarily disabling without deleting
)
