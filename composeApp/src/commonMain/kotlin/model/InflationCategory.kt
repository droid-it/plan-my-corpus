package model

import kotlinx.serialization.Serializable

@Serializable
data class InflationCategory(
    val id: String,
    val name: String,
    val rate: Double // percentage, e.g., 6.0 for 6%
)

object DefaultInflationCategories {
    val GENERAL = "general"
    val EDUCATION = "education"
    val HEALTH = "health"

    fun defaults() = listOf(
        InflationCategory(GENERAL, "General", 6.0),
        InflationCategory(EDUCATION, "Education", 8.0),
        InflationCategory(HEALTH, "Health", 10.0)
    )
}
