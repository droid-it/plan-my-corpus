package model

import kotlinx.serialization.Serializable

@Serializable
data class Investment(
    val id: String,
    val name: String,
    val currentValue: Double,
    val currentXIRR: Double, // current XIRR percentage
    val isEnabled: Boolean = true // Allow temporarily disabling without deleting
)
