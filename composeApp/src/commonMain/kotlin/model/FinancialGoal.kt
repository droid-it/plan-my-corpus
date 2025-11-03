package model

import kotlinx.serialization.Serializable

enum class GoalPriority {
    MUST_HAVE,
    GOOD_TO_HAVE
}

enum class GoalTimeline {
    SHORT_TERM,  // 0-5 years
    MEDIUM_TERM, // 5-10 years
    LONG_TERM    // 10+ years
}

@Serializable
data class FinancialGoal(
    val id: String,
    val name: String,
    val targetAmount: Double, // in today's value
    val targetYear: Int,
    val inflationCategoryId: String,
    val priority: GoalPriority,
    val timeline: GoalTimeline,
    val isEnabled: Boolean = true, // Allow temporarily disabling without deleting
    val isRecurring: Boolean = false, // Whether this goal repeats
    val recurringFrequencyMonths: Int = 12, // How often it repeats (in months)
    val recurringStartAge: Int? = null, // Age when recurring goal starts (null for one-time goals)
    val recurringEndAge: Int? = null // Age when recurring goal ends (null for one-time goals)
)
