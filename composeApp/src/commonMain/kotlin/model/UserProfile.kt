package model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val currentAge: Int = 30,
    val retirementAge: Int = 60,
    val lifeExpectancy: Int = 85,
    val currentMonthlyExpenses: Double = 50000.0, // Current monthly expenses in today's value
    val expenseInflationCategoryId: String = DefaultInflationCategories.GENERAL, // Inflation category for expenses
    val postRetirementGrowthRate: Double = 10.0 // Single growth rate for all corpus post-retirement (percentage)
)
