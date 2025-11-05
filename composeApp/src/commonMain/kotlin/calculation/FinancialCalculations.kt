package calculation

import model.*
import kotlin.math.pow

/**
 * Core financial calculation engine
 */
object FinancialCalculations {

    /**
     * Calculate future value with inflation adjustment
     * FV = PV × (1 + rate)^years
     */
    fun inflationAdjusted(presentValue: Double, inflationRate: Double, years: Int): Double {
        return presentValue * (1 + inflationRate / 100).pow(years)
    }

    /**
     * Calculate future value of current investment at a specific year
     * considering different growth rates before and after retirement
     */
    fun calculateInvestmentFutureValue(
        currentValue: Double,
        currentYear: Int,
        targetYear: Int,
        retirementYear: Int,
        preRetirementXIRR: Double,
        postRetirementGrowthRate: Double
    ): Double {
        if (targetYear <= currentYear) return currentValue

        val yearsToTarget = targetYear - currentYear

        // If target is before retirement, use only pre-retirement rate
        if (targetYear <= retirementYear) {
            return currentValue * (1 + preRetirementXIRR / 100).pow(yearsToTarget)
        }

        // If already retired, use only post-retirement rate (single blended rate)
        if (currentYear >= retirementYear) {
            return currentValue * (1 + postRetirementGrowthRate / 100).pow(yearsToTarget)
        }

        // If target spans retirement, calculate in two phases
        val yearsToRetirement = retirementYear - currentYear
        val yearsAfterRetirement = targetYear - retirementYear

        val valueAtRetirement = currentValue * (1 + preRetirementXIRR / 100).pow(yearsToRetirement)
        return valueAtRetirement * (1 + postRetirementGrowthRate / 100).pow(yearsAfterRetirement)
    }

    /**
     * Calculate future value of ongoing contributions
     * Supports step-up contributions that increase yearly by a percentage
     */
    fun calculateContributionsFutureValue(
        contribution: OngoingContribution,
        currentYear: Int,
        targetYear: Int,
        retirementYear: Int,
        preRetirementXIRR: Double,
        postRetirementGrowthRate: Double
    ): Double {
        if (targetYear <= currentYear) return 0.0

        // Convert contribution to annual amount
        val initialAnnualContribution = when (contribution.frequency) {
            ContributionFrequency.MONTHLY -> contribution.amount * 12
            ContributionFrequency.QUARTERLY -> contribution.amount * 4
            ContributionFrequency.YEARLY -> contribution.amount
        }

        // Determine when contributions stop
        val contributionEndYear = if (contribution.durationYears != null) {
            currentYear + contribution.durationYears
        } else {
            retirementYear // Default: continue until retirement
        }

        // If contributions have already ended, return 0
        if (contributionEndYear <= currentYear) return 0.0

        val stepUpRate = contribution.stepUpPercentage / 100.0

        // If no step-up, use optimized annuity formula
        if (stepUpRate == 0.0) {
            return calculateContributionsFutureValueNoStepUp(
                initialAnnualContribution,
                currentYear,
                targetYear,
                retirementYear,
                contributionEndYear,
                preRetirementXIRR,
                postRetirementGrowthRate
            )
        }

        // With step-up, calculate year by year
        var corpus = 0.0
        var currentAnnualContribution = initialAnnualContribution

        for (year in (currentYear + 1)..minOf(contributionEndYear, targetYear)) {
            // Add this year's contribution
            corpus += currentAnnualContribution

            // Grow the corpus for this year
            val growthRate = if (year <= retirementYear) preRetirementXIRR else postRetirementGrowthRate
            corpus *= (1.0 + growthRate / 100.0)

            // Step up the contribution for next year
            currentAnnualContribution *= (1.0 + stepUpRate)
        }

        // If contributions ended before target year, grow the final corpus
        if (contributionEndYear < targetYear) {
            val yearsAfterContributionEnds = targetYear - contributionEndYear
            for (year in 1..yearsAfterContributionEnds) {
                val currentYearAbsolute = contributionEndYear + year
                val growthRate = if (currentYearAbsolute <= retirementYear) preRetirementXIRR else postRetirementGrowthRate
                corpus *= (1.0 + growthRate / 100.0)
            }
        }

        return corpus
    }

    /**
     * Optimized calculation for contributions without step-up (uses annuity formulas)
     */
    private fun calculateContributionsFutureValueNoStepUp(
        annualContribution: Double,
        currentYear: Int,
        targetYear: Int,
        retirementYear: Int,
        contributionEndYear: Int,
        preRetirementXIRR: Double,
        postRetirementGrowthRate: Double
    ): Double {
        val contributionYears = minOf(contributionEndYear - currentYear, targetYear - currentYear)

        // If target is before or at contribution end, simple annuity calculation
        if (targetYear <= contributionEndYear) {
            val rate = if (targetYear <= retirementYear) preRetirementXIRR else postRetirementGrowthRate
            return futureValueOfAnnuity(annualContribution, rate / 100, contributionYears)
        }

        // Contributions end before target year - calculate corpus at end, then grow it
        val yearsAfterContributionEnds = targetYear - contributionEndYear

        // Calculate corpus at contribution end
        val rateWhileContributing = if (contributionEndYear <= retirementYear) {
            preRetirementXIRR
        } else {
            // Contributions span retirement - need two-phase calculation
            val yearsToRetirement = retirementYear - currentYear
            val yearsAfterRetirement = contributionEndYear - retirementYear

            val corpusAtRetirement = futureValueOfAnnuity(annualContribution, preRetirementXIRR / 100, yearsToRetirement)
            val corpusAtEnd = corpusAtRetirement + futureValueOfAnnuity(annualContribution, postRetirementGrowthRate / 100, yearsAfterRetirement)

            // Grow this corpus to target year
            val growthRate = if (targetYear <= retirementYear) preRetirementXIRR else postRetirementGrowthRate
            return corpusAtEnd * (1 + growthRate / 100).pow(yearsAfterContributionEnds)
        }

        val corpusAtContributionEnd = futureValueOfAnnuity(annualContribution, rateWhileContributing / 100, contributionYears)

        // Grow the corpus from contribution end to target year
        val growthRate = if (targetYear <= retirementYear) preRetirementXIRR else postRetirementGrowthRate
        return corpusAtContributionEnd * (1 + growthRate / 100).pow(yearsAfterContributionEnds)
    }

    /**
     * Future Value of Annuity formula
     * FV = PMT × [(1 + r)^n - 1] / r
     */
    private fun futureValueOfAnnuity(payment: Double, rate: Double, periods: Int): Double {
        if (rate == 0.0) return payment * periods
        return payment * ((1 + rate).pow(periods) - 1) / rate
    }

    /**
     * Calculate total corpus at a specific year, accounting for goal withdrawals
     */
    fun calculateTotalCorpus(
        data: FinancialPlanData,
        currentYear: Int,
        targetYear: Int
    ): Double {
        val retirementYear = currentYear + (data.userProfile.retirementAge - data.userProfile.currentAge)
        val postRetirementRate = data.userProfile.postRetirementGrowthRate

        // Sum all current investments' future values (only enabled ones)
        val investmentsFV = data.investments.filter { it.isEnabled }.sumOf { investment ->
            calculateInvestmentFutureValue(
                investment.currentValue,
                currentYear,
                targetYear,
                retirementYear,
                investment.currentXIRR,
                postRetirementRate
            )
        }

        // Sum all ongoing contributions' future values (only enabled ones)
        val contributionsFV = data.ongoingContributions.filter { it.isEnabled }.sumOf { contribution ->
            val category = data.investmentCategories.find { it.id == contribution.categoryId }
            if (category != null) {
                calculateContributionsFutureValue(
                    contribution,
                    currentYear,
                    targetYear,
                    retirementYear,
                    category.preRetirementXIRR,
                    postRetirementRate
                )
            } else {
                0.0
            }
        }

        val totalBeforeWithdrawals = investmentsFV + contributionsFV

        // Subtract goal withdrawals that occur before target year
        // For each goal realized before target year, we need to:
        // 1. Calculate its inflation-adjusted amount at goal year
        // 2. Subtract it from corpus at goal year
        // 3. Calculate the impact on corpus at target year (lost growth opportunity)

        val totalWithdrawals = data.goals.filter { it.isEnabled }.sumOf { goal ->
            if (goal.isRecurring) {
                // Handle recurring goals
                calculateRecurringGoalWithdrawals(
                    goal,
                    currentYear,
                    targetYear,
                    retirementYear,
                    data.userProfile.currentAge,
                    data.inflationCategories,
                    data.investmentCategories,
                    postRetirementRate
                )
            } else {
                // Handle one-time goals
                if (goal.targetYear > targetYear) {
                    0.0 // Goal hasn't occurred yet
                } else {
                    calculateOneTimeGoalWithdrawal(
                        goal,
                        currentYear,
                        targetYear,
                        retirementYear,
                        data.inflationCategories,
                        data.investmentCategories,
                        postRetirementRate
                    )
                }
            }
        }

        return totalBeforeWithdrawals - totalWithdrawals
    }

    /**
     * Calculate withdrawal impact for a one-time goal
     */
    private fun calculateOneTimeGoalWithdrawal(
        goal: FinancialGoal,
        currentYear: Int,
        targetYear: Int,
        retirementYear: Int,
        inflationCategories: List<InflationCategory>,
        investmentCategories: List<InvestmentCategory>,
        postRetirementRate: Double
    ): Double {
        val goalYear = goal.targetYear

        // Calculate inflation-adjusted goal amount
        val inflationAdjustedAmount = calculateGoalInflationAdjusted(
            goal,
            currentYear,
            inflationCategories
        )

        // If goal is realized before target year, account for lost growth
        if (goalYear < targetYear) {
            // Money withdrawn at goal year doesn't grow to target year
            val yearsOfLostGrowth = targetYear - goalYear
            val growthRate = if (targetYear <= retirementYear) {
                // Use average pre-retirement rate
                if (investmentCategories.isNotEmpty()) {
                    investmentCategories.map { it.preRetirementXIRR }.average() / 100
                } else {
                    0.12
                }
            } else if (goalYear >= retirementYear) {
                // Use single post-retirement rate
                postRetirementRate / 100
            } else {
                // Goal spans retirement - use blended rate
                val yearsToRetirement = retirementYear - goalYear
                val yearsAfterRetirement = targetYear - retirementYear
                val preRate = if (investmentCategories.isNotEmpty()) {
                    investmentCategories.map { it.preRetirementXIRR }.average() / 100
                } else {
                    0.12
                }
                val postRate = postRetirementRate / 100

                // Calculate value if it had grown
                val valueAtRetirement = inflationAdjustedAmount * (1 + preRate).pow(yearsToRetirement)
                val valueAtTarget = valueAtRetirement * (1 + postRate).pow(yearsAfterRetirement)
                return valueAtTarget
            }

            // Calculate what this withdrawal would have grown to
            return inflationAdjustedAmount * (1 + growthRate).pow(yearsOfLostGrowth)
        } else {
            // Goal is at target year, just the inflation-adjusted amount
            return inflationAdjustedAmount
        }
    }

    /**
     * Calculate total withdrawal impact for recurring goals
     */
    private fun calculateRecurringGoalWithdrawals(
        goal: FinancialGoal,
        currentYear: Int,
        targetYear: Int,
        retirementYear: Int,
        currentAge: Int,
        inflationCategories: List<InflationCategory>,
        investmentCategories: List<InvestmentCategory>,
        postRetirementRate: Double
    ): Double {
        val startAge = goal.recurringStartAge ?: return 0.0
        val endAge = goal.recurringEndAge ?: return 0.0
        val frequencyMonths = goal.recurringFrequencyMonths

        val startYear = currentYear + (startAge - currentAge)
        val endYear = currentYear + (endAge - currentAge)

        var totalWithdrawals = 0.0

        // Generate all occurrences within the age range
        var occurrenceAge = startAge
        while (occurrenceAge <= endAge) {
            val occurrenceYear = currentYear + (occurrenceAge - currentAge)

            // Only count occurrences that happen before or at target year
            if (occurrenceYear <= targetYear) {
                // Calculate inflation-adjusted amount for this occurrence
                val yearsFromNow = occurrenceYear - currentYear
                val inflationCategory = inflationCategories.find { it.id == goal.inflationCategoryId }
                val inflationRate = inflationCategory?.rate ?: 6.0
                val inflationAdjustedAmount = inflationAdjusted(goal.targetAmount, inflationRate, yearsFromNow)

                // Calculate growth impact
                if (occurrenceYear < targetYear) {
                    val yearsOfLostGrowth = targetYear - occurrenceYear
                    val growthRate = if (targetYear <= retirementYear) {
                        if (investmentCategories.isNotEmpty()) {
                            investmentCategories.map { it.preRetirementXIRR }.average() / 100
                        } else {
                            0.12
                        }
                    } else if (occurrenceYear >= retirementYear) {
                        postRetirementRate / 100
                    } else {
                        // Occurrence spans retirement
                        val yearsToRetirement = retirementYear - occurrenceYear
                        val yearsAfterRetirement = targetYear - retirementYear
                        val preRate = if (investmentCategories.isNotEmpty()) {
                            investmentCategories.map { it.preRetirementXIRR }.average() / 100
                        } else {
                            0.12
                        }
                        val postRate = postRetirementRate / 100

                        val valueAtRetirement = inflationAdjustedAmount * (1 + preRate).pow(yearsToRetirement)
                        val valueAtTarget = valueAtRetirement * (1 + postRate).pow(yearsAfterRetirement)
                        totalWithdrawals += valueAtTarget
                        // Move to next occurrence
                        occurrenceAge += (frequencyMonths / 12.0).toInt()
                        if (frequencyMonths % 12 != 0) occurrenceAge += 1
                        continue
                    }

                    totalWithdrawals += inflationAdjustedAmount * (1 + growthRate).pow(yearsOfLostGrowth)
                } else {
                    // Occurrence is at target year
                    totalWithdrawals += inflationAdjustedAmount
                }
            }

            // Move to next occurrence (convert months to years)
            occurrenceAge += (frequencyMonths / 12.0).toInt()
            if (frequencyMonths % 12 != 0) occurrenceAge += 1 // Round up partial years
        }

        return totalWithdrawals
    }

    /**
     * Calculate inflation-adjusted amount for a goal
     */
    fun calculateGoalInflationAdjusted(
        goal: FinancialGoal,
        currentYear: Int,
        inflationCategories: List<InflationCategory>
    ): Double {
        val inflationCategory = inflationCategories.find { it.id == goal.inflationCategoryId }
        val inflationRate = inflationCategory?.rate ?: 6.0
        val years = goal.targetYear - currentYear

        return inflationAdjusted(goal.targetAmount, inflationRate, years)
    }

    /**
     * Calculate corpus at a specific post-retirement year with expense withdrawals
     * This simulates year-by-year drawdown unlike calculateTotalCorpus which uses lost growth
     */
    fun calculatePostRetirementCorpus(
        startingCorpus: Double,
        retirementYear: Int,
        targetYear: Int,
        monthlyExpenseAtRetirement: Double,
        expenseInflationRate: Double,
        postRetirementGrowthRate: Double,
        goalsAfterRetirement: List<Pair<Int, Double>> // year to (year, amount)
    ): Double {
        if (targetYear <= retirementYear) return startingCorpus

        var corpus = startingCorpus

        for (year in (retirementYear + 1)..targetYear) {
            // Calculate and subtract this year's expenses FIRST (beginning of year withdrawal)
            // yearsIntoRetirement should be 0 for first year after retirement (retirementYear + 1)
            val yearsIntoRetirement = year - retirementYear - 1
            val monthlyExpenseThisYear = monthlyExpenseAtRetirement *
                (1.0 + expenseInflationRate / 100.0).pow(yearsIntoRetirement.toDouble())
            val annualExpense = monthlyExpenseThisYear * 12.0
            corpus -= annualExpense

            // Subtract any goals for this year
            val goalsThisYear = goalsAfterRetirement.filter { it.first == year }
            goalsThisYear.forEach { (_, amount) ->
                corpus -= amount
            }

            // Then grow the remaining corpus for the year
            corpus *= (1.0 + postRetirementGrowthRate / 100.0)
        }

        return corpus
    }

    /**
     * Calculate required retirement corpus
     *
     * This uses the "Present Value of Annuity" formula adjusted for inflation
     * Formula: PV = PMT × [(1 - (1 + r - i)^(-n)) / (r - i)]
     * Where:
     * - PMT = Monthly expense at retirement (inflation-adjusted)
     * - r = Post-retirement growth rate (single blended rate for entire corpus)
     * - i = Inflation rate
     * - n = Years in retirement
     */
    fun calculateRetirementCorpus(
        data: FinancialPlanData,
        currentYear: Int
    ): Double {
        val profile = data.userProfile
        val yearsToRetirement = profile.retirementAge - profile.currentAge
        val yearsInRetirement = profile.lifeExpectancy - profile.retirementAge

        if (yearsInRetirement <= 0) return 0.0

        // Find expense inflation category
        val inflationCategory = data.inflationCategories.find {
            it.id == profile.expenseInflationCategoryId
        }
        val expenseInflationRate = (inflationCategory?.rate ?: 6.0) / 100

        // Calculate monthly expenses at retirement (inflation-adjusted)
        val monthlyExpenseAtRetirement = profile.currentMonthlyExpenses *
            (1 + expenseInflationRate).pow(yearsToRetirement)

        // Use single post-retirement growth rate from profile
        val postRetirementRate = profile.postRetirementGrowthRate / 100

        // Monthly rate
        val monthlyGrowthRate = postRetirementRate / 12
        val monthlyInflationRate = expenseInflationRate / 12
        val realMonthlyRate = monthlyGrowthRate - monthlyInflationRate

        val monthsInRetirement = yearsInRetirement * 12

        // Present value of annuity formula
        return if (realMonthlyRate > 0.0001) {
            monthlyExpenseAtRetirement *
                ((1 - (1 + realMonthlyRate).pow(-monthsInRetirement)) / realMonthlyRate)
        } else {
            // If real rate is near zero, simple multiplication
            monthlyExpenseAtRetirement * monthsInRetirement
        }
    }
}
