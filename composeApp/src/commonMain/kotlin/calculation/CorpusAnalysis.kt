package calculation

import model.*
import kotlin.math.pow

/**
 * Result of analyzing a single financial goal
 */
data class GoalAnalysis(
    val goal: FinancialGoal,
    val inflationAdjustedAmount: Double,
    val corpusAtGoalYear: Double,
    val isFunded: Boolean,
    val shortfall: Double // negative if overfunded
)

/**
 * Overall corpus health status
 */
data class CorpusHealth(
    val totalCorpusAtRetirement: Double,
    val retirementCorpusRequired: Double,
    val totalMustHaveGoalsRequired: Double,
    val totalAllGoalsRequired: Double,
    val totalRequiredIncludingRetirement: Double,
    val canMeetRetirement: Boolean,
    val canMeetMustHaveGoals: Boolean,
    val canMeetAllGoals: Boolean,
    val canMeetAllIncludingRetirement: Boolean,
    val mustHaveGoalsFunded: Int,
    val mustHaveGoalsTotal: Int,
    val goodToHaveGoalsFunded: Int,
    val goodToHaveGoalsTotal: Int,
    val overallSurplus: Double // total corpus - (total goals + retirement) (can be negative)
)

/**
 * Year-by-year projection
 */
data class YearProjection(
    val year: Int,
    val age: Int,
    val totalCorpus: Double,
    val goalsMaturing: List<GoalAnalysis> // goals that mature this year
)

/**
 * Complete financial analysis
 */
data class FinancialAnalysis(
    val currentYear: Int,
    val corpusHealth: CorpusHealth,
    val goalAnalyses: List<GoalAnalysis>,
    val yearlyProjections: List<YearProjection>
)

/**
 * Analyzes overall corpus health and goal funding
 */
object CorpusAnalyzer {

    /**
     * Perform complete financial analysis
     */
    fun analyze(data: FinancialPlanData, currentYear: Int): FinancialAnalysis {
        val retirementYear = currentYear + (data.userProfile.retirementAge - data.userProfile.currentAge)

        // Analyze each enabled goal - expand recurring goals into multiple analyses
        val goalAnalyses = data.goals.filter { it.isEnabled }.flatMap { goal ->
            if (goal.isRecurring) {
                analyzeRecurringGoal(goal, data, currentYear)
            } else {
                listOf(analyzeGoal(goal, data, currentYear))
            }
        }

        // Calculate corpus health
        val corpusHealth = calculateCorpusHealth(data, currentYear, retirementYear, goalAnalyses)

        // Generate yearly projections
        val yearlyProjections = generateYearlyProjections(data, currentYear, retirementYear, goalAnalyses)

        return FinancialAnalysis(
            currentYear = currentYear,
            corpusHealth = corpusHealth,
            goalAnalyses = goalAnalyses,
            yearlyProjections = yearlyProjections
        )
    }

    /**
     * Analyze a recurring goal by creating a GoalAnalysis for each occurrence
     */
    private fun analyzeRecurringGoal(
        goal: FinancialGoal,
        data: FinancialPlanData,
        currentYear: Int
    ): List<GoalAnalysis> {
        val startAge = goal.recurringStartAge ?: return emptyList()
        val endAge = goal.recurringEndAge ?: return emptyList()
        val frequencyMonths = goal.recurringFrequencyMonths
        val currentAge = data.userProfile.currentAge

        val analyses = mutableListOf<GoalAnalysis>()
        var occurrenceAge = startAge

        while (occurrenceAge <= endAge) {
            val occurrenceYear = currentYear + (occurrenceAge - currentAge)

            // Create a one-time goal for this occurrence
            val occurrenceGoal = goal.copy(
                targetYear = occurrenceYear,
                isRecurring = false
            )

            analyses.add(analyzeGoal(occurrenceGoal, data, currentYear))

            // Move to next occurrence
            occurrenceAge += (frequencyMonths / 12.0).toInt()
            if (frequencyMonths % 12 != 0) occurrenceAge += 1
        }

        return analyses
    }

    /**
     * Analyze a single goal
     */
    private fun analyzeGoal(
        goal: FinancialGoal,
        data: FinancialPlanData,
        currentYear: Int
    ): GoalAnalysis {
        val inflationAdjusted = FinancialCalculations.calculateGoalInflationAdjusted(
            goal,
            currentYear,
            data.inflationCategories
        )

        // Calculate corpus at goal year EXCLUDING this goal
        // (to check if we can afford it before withdrawing)
        val dataWithoutThisGoal = data.copy(
            goals = data.goals.filter { it.isEnabled && it.id != goal.id }
        )

        val corpusAtGoalYear = FinancialCalculations.calculateTotalCorpus(
            dataWithoutThisGoal,
            currentYear,
            goal.targetYear
        )

        val isFunded = corpusAtGoalYear >= inflationAdjusted
        val shortfall = inflationAdjusted - corpusAtGoalYear

        return GoalAnalysis(
            goal = goal,
            inflationAdjustedAmount = inflationAdjusted,
            corpusAtGoalYear = corpusAtGoalYear,
            isFunded = isFunded,
            shortfall = shortfall
        )
    }

    /**
     * Calculate overall corpus health
     */
    private fun calculateCorpusHealth(
        data: FinancialPlanData,
        currentYear: Int,
        retirementYear: Int,
        goalAnalyses: List<GoalAnalysis>
    ): CorpusHealth {
        // Calculate corpus at retirement WITHOUT subtracting any goals (for display purposes)
        val dataWithoutGoals = data.copy(goals = emptyList())
        val corpusAtRetirementBeforeGoals = FinancialCalculations.calculateTotalCorpus(
            dataWithoutGoals,
            currentYear,
            retirementYear
        )

        // Calculate corpus at retirement WITH pre-retirement goals subtracted (includes lost growth)
        val corpusAtRetirementAfterPreGoals = FinancialCalculations.calculateTotalCorpus(
            data,
            currentYear,
            retirementYear
        )

        // Calculate retirement corpus requirement
        val retirementCorpusRequired = FinancialCalculations.calculateRetirementCorpus(data, currentYear)

        val mustHaveGoals = goalAnalyses.filter { it.goal.priority == GoalPriority.MUST_HAVE }
        val goodToHaveGoals = goalAnalyses.filter { it.goal.priority == GoalPriority.GOOD_TO_HAVE }

        // Separate pre-retirement and post-retirement goals
        val preRetirementGoals = goalAnalyses.filter { it.goal.targetYear <= retirementYear }
        val postRetirementGoals = goalAnalyses.filter { it.goal.targetYear > retirementYear }

        val totalMustHaveRequired = mustHaveGoals.sumOf { it.inflationAdjustedAmount }
        val totalGoodToHaveRequired = goodToHaveGoals.sumOf { it.inflationAdjustedAmount }
        val totalAllGoalsRequired = totalMustHaveRequired + totalGoodToHaveRequired

        // For pre-retirement goals, calculate their impact including lost growth
        val preRetirementGoalsImpact = corpusAtRetirementBeforeGoals - corpusAtRetirementAfterPreGoals

        // For post-retirement goals, discount them back to retirement year using post-retirement growth rate
        val postRetirementRate = data.userProfile.postRetirementGrowthRate / 100.0
        val postRetirementGoalsPV = postRetirementGoals.sumOf { goalAnalysis ->
            val yearsFromRetirement = goalAnalysis.goal.targetYear - retirementYear
            // Discount the inflation-adjusted amount back to retirement
            goalAnalysis.inflationAdjustedAmount / (1 + postRetirementRate).pow(yearsFromRetirement)
        }

        // Total required at retirement = pre-retirement goals impact + post-retirement goals PV + retirement corpus
        val totalRequiredAtRetirement = preRetirementGoalsImpact + postRetirementGoalsPV + retirementCorpusRequired

        val mustHaveGoalsFunded = mustHaveGoals.count { it.isFunded }
        val goodToHaveGoalsFunded = goodToHaveGoals.count { it.isFunded }

        // Check if corpus can meet various combinations
        val canMeetRetirement = retirementCorpusRequired <= corpusAtRetirementBeforeGoals
        val canMeetMustHave = totalMustHaveRequired <= corpusAtRetirementBeforeGoals
        val canMeetAll = totalAllGoalsRequired <= corpusAtRetirementBeforeGoals
        val canMeetAllIncludingRetirement = totalRequiredAtRetirement <= corpusAtRetirementBeforeGoals

        return CorpusHealth(
            totalCorpusAtRetirement = corpusAtRetirementBeforeGoals,
            retirementCorpusRequired = retirementCorpusRequired,
            totalMustHaveGoalsRequired = totalMustHaveRequired,
            totalAllGoalsRequired = totalAllGoalsRequired,
            totalRequiredIncludingRetirement = totalRequiredAtRetirement,
            canMeetRetirement = canMeetRetirement,
            canMeetMustHaveGoals = canMeetMustHave,
            canMeetAllGoals = canMeetAll,
            canMeetAllIncludingRetirement = canMeetAllIncludingRetirement,
            mustHaveGoalsFunded = mustHaveGoalsFunded,
            mustHaveGoalsTotal = mustHaveGoals.size,
            goodToHaveGoalsFunded = goodToHaveGoalsFunded,
            goodToHaveGoalsTotal = goodToHaveGoals.size,
            overallSurplus = corpusAtRetirementBeforeGoals - totalRequiredAtRetirement
        )
    }

    /**
     * Generate year-by-year projections extending to life expectancy
     * Post-retirement: accounts for corpus growth AND monthly expense withdrawals
     */
    private fun generateYearlyProjections(
        data: FinancialPlanData,
        currentYear: Int,
        retirementYear: Int,
        goalAnalyses: List<GoalAnalysis>
    ): List<YearProjection> {
        val lifeExpectancyYear = currentYear + (data.userProfile.lifeExpectancy - data.userProfile.currentAge)
        val projections = mutableListOf<YearProjection>()

        // Get inflation rate for expenses
        val inflationCategory = data.inflationCategories.find {
            it.id == data.userProfile.expenseInflationCategoryId
        }
        val expenseInflationRate = inflationCategory?.rate ?: 6.0
        val yearsToRetirement = retirementYear - currentYear

        // Calculate corpus at retirement WITHOUT any goals subtracted (for display)
        val dataWithoutGoals = data.copy(goals = emptyList())
        val corpusAtRetirementBeforeGoals = FinancialCalculations.calculateTotalCorpus(dataWithoutGoals, currentYear, retirementYear)

        // Calculate corpus at retirement WITH pre-retirement goals subtracted (with lost growth)
        val corpusAtRetirementAfterPreGoals = FinancialCalculations.calculateTotalCorpus(data, currentYear, retirementYear)

        // Calculate monthly expense at retirement
        val monthlyExpenseAtRetirement = data.userProfile.currentMonthlyExpenses *
            (1.0 + expenseInflationRate / 100.0).pow(yearsToRetirement.toDouble())

        // Get post-retirement goals with their inflation-adjusted amounts
        val postRetirementGoals = goalAnalyses
            .filter { it.goal.targetYear > retirementYear }
            .map { it.goal.targetYear to it.inflationAdjustedAmount }

        for (year in currentYear..lifeExpectancyYear) {
            val age = data.userProfile.currentAge + (year - currentYear)
            val goalsThisYear = goalAnalyses.filter { it.goal.targetYear == year }

            val corpus = if (year <= retirementYear) {
                // Pre-retirement: use standard calculation with goal withdrawals
                FinancialCalculations.calculateTotalCorpus(data, currentYear, year)
            } else {
                // Post-retirement: use drawdown simulation
                // Start from corpus after pre-retirement goals (with lost growth accounted for)
                FinancialCalculations.calculatePostRetirementCorpus(
                    startingCorpus = corpusAtRetirementAfterPreGoals,
                    retirementYear = retirementYear,
                    targetYear = year,
                    monthlyExpenseAtRetirement = monthlyExpenseAtRetirement,
                    expenseInflationRate = expenseInflationRate,
                    postRetirementGrowthRate = data.userProfile.postRetirementGrowthRate,
                    goalsAfterRetirement = postRetirementGoals
                )
            }

            projections.add(
                YearProjection(
                    year = year,
                    age = age,
                    totalCorpus = corpus,
                    goalsMaturing = goalsThisYear
                )
            )
        }

        return projections
    }
}
