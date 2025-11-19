package model

import kotlinx.serialization.Serializable

@Serializable
data class Investment(
    val id: String,
    val name: String,
    val currentValue: Double,
    val categoryId: String,
    val customXIRR: Double? = null, // Optional: override category's expected return (null = use category default)
    val isEnabled: Boolean = true // Allow temporarily disabling without deleting
)
