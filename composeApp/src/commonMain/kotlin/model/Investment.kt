package model

import kotlinx.serialization.Serializable

@Serializable
data class Investment(
    val id: String,
    val name: String,
    val currentValue: Double,
    val categoryId: String,
    val actualXIRR: Double, // actual historical return percentage
    val isEnabled: Boolean = true // Allow temporarily disabling without deleting
)
