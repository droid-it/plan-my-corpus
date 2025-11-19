package model

/**
 * Sample data generator for first-time users to explore the application
 */
object SampleData {
    fun createSampleFinancialData(): FinancialPlanData {
        return FinancialPlanData(
            userProfile = UserProfile(
                currentAge = 32,
                retirementAge = 60,
                lifeExpectancy = 85,
                currentMonthlyExpenses = 50000.0,
                expenseInflationCategoryId = "general",
                postRetirementGrowthRate = 9.0
            ),
            inflationCategories = DefaultInflationCategories.defaults(),
            investmentCategories = DefaultInvestmentCategories.defaults(),
            investments = listOf(
                Investment(
                    id = "sample-investment-1",
                    name = "Public Provident Fund (PPF)",
                    currentValue = 800000.0,
                    categoryId = "debt",
                    isEnabled = true
                ),
                Investment(
                    id = "sample-investment-2",
                    name = "Equity Mutual Funds",
                    currentValue = 1500000.0,
                    categoryId = "equity",
                    isEnabled = true
                ),
                Investment(
                    id = "sample-investment-3",
                    name = "Employee Provident Fund (EPF)",
                    currentValue = 1200000.0,
                    categoryId = "hybrid",
                    isEnabled = true
                )
            ),
            futureLumpsumInvestments = listOf(
                FutureLumpsumInvestment(
                    id = "sample-lumpsum-1",
                    name = "Year-end Bonus Investment",
                    plannedAmount = 200000.0,
                    plannedYear = 2026,
                    categoryId = "equity",
                    isEnabled = true
                )
            ),
            ongoingContributions = listOf(
                OngoingContribution(
                    id = "sample-contribution-1",
                    name = "Equity SIP",
                    amount = 10000.0,
                    frequency = ContributionFrequency.MONTHLY,
                    categoryId = "equity",
                    durationYears = null, // Until retirement
                    stepUpPercentage = 10.0, // 10% annual increase
                    isEnabled = true
                ),
                OngoingContribution(
                    id = "sample-contribution-2",
                    name = "PPF Annual Contribution",
                    amount = 150000.0,
                    frequency = ContributionFrequency.YEARLY,
                    categoryId = "debt",
                    durationYears = 15, // PPF has 15-year limit
                    stepUpPercentage = 0.0,
                    isEnabled = true
                ),
                OngoingContribution(
                    id = "sample-contribution-3",
                    name = "EPF Contribution",
                    amount = 15000.0,
                    frequency = ContributionFrequency.MONTHLY,
                    categoryId = "hybrid",
                    durationYears = null, // Until retirement
                    stepUpPercentage = 5.0,
                    isEnabled = true
                )
            ),
            goals = listOf(
                FinancialGoal(
                    id = "sample-goal-1",
                    name = "Emergency Fund",
                    targetAmount = 600000.0,
                    targetYear = 2026,
                    inflationCategoryId = "general",
                    priority = GoalPriority.MUST_HAVE,
                    timeline = GoalTimeline.SHORT_TERM,
                    isEnabled = true
                ),
                FinancialGoal(
                    id = "sample-goal-2",
                    name = "Biennial Vacation",
                    targetAmount = 300000.0,
                    targetYear = 2028,
                    inflationCategoryId = "general",
                    priority = GoalPriority.GOOD_TO_HAVE,
                    timeline = GoalTimeline.LONG_TERM,
                    isEnabled = true,
                    isRecurring = true,
                    recurringFrequencyMonths = 24, // Every 2 years
                    recurringStartAge = 34, // Starts at age 34 (year 2028)
                    recurringEndAge = 60 // Until retirement
                ),
                FinancialGoal(
                    id = "sample-goal-3",
                    name = "House Down Payment",
                    targetAmount = 3000000.0,
                    targetYear = 2033,
                    inflationCategoryId = "general",
                    priority = GoalPriority.GOOD_TO_HAVE,
                    timeline = GoalTimeline.MEDIUM_TERM,
                    isEnabled = true
                ),
                FinancialGoal(
                    id = "sample-goal-4",
                    name = "Child's Higher Education",
                    targetAmount = 2500000.0,
                    targetYear = 2040,
                    inflationCategoryId = "education",
                    priority = GoalPriority.MUST_HAVE,
                    timeline = GoalTimeline.LONG_TERM,
                    isEnabled = true
                )
            ),
            snapshotTimestamp = 0,
            isSampleData = true
        )
    }
}
