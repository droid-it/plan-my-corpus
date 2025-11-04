package ui.screens

import AppState
import Screen
import updateBrowserUrl
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.GoalPriority
import kotlin.math.pow

@Composable
fun DashboardScreen(appState: AppState) {
    val analysis = appState.analysis
    val health = analysis.corpusHealth

    // Calculate current corpus (total of all current investments)
    val currentCorpus = appState.data.investments.filter { it.isEnabled }.sumOf { it.currentValue }

    // Check if user needs onboarding
    val hasNoProfile = appState.data.userProfile.currentAge == 30 &&
                       appState.data.userProfile.retirementAge == 60 // Default values
    val hasNoInvestments = appState.data.investments.isEmpty()
    val hasNoGoals = appState.data.goals.isEmpty()
    val hasNoContributions = appState.data.ongoingContributions.isEmpty()

    val needsOnboarding = hasNoProfile || hasNoInvestments || hasNoGoals

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Onboarding Nudge - Show only when user hasn't set up basics
        if (needsOnboarding) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Welcome! Let's get started",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Text(
                        text = "Set up these basics to see your financial health:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (hasNoProfile || appState.data.userProfile.currentMonthlyExpenses == 50000.0) {
                        OnboardingItem(
                            icon = Icons.Default.Person,
                            title = "Set up your profile",
                            description = "Add your age, retirement plans, and monthly expenses",
                            onClick = { appState.navigateTo(Screen.UserProfile) }
                        )
                    }

                    if (hasNoInvestments) {
                        OnboardingItem(
                            icon = Icons.Default.AccountBalance,
                            title = "Add your current investments",
                            description = "Track your existing portfolio value",
                            onClick = { appState.currentScreen = Screen.Portfolio; updateBrowserUrl(Screen.Portfolio) }
                        )
                    }

                    if (hasNoGoals) {
                        OnboardingItem(
                            icon = Icons.Default.Flag,
                            title = "Define your financial goals",
                            description = "What are you planning for? Retirement, education, house?",
                            onClick = { appState.currentScreen = Screen.Goals; updateBrowserUrl(Screen.Goals) }
                        )
                    }

                    if (hasNoContributions) {
                        OnboardingItem(
                            icon = Icons.Default.Add,
                            title = "Add ongoing contributions",
                            description = "Add your SIPs, PPF, or other recurring investments",
                            onClick = { appState.currentScreen = Screen.Contributions; updateBrowserUrl(Screen.Contributions) }
                        )
                    }
                }
            }
        }

        // Data Storage Information Banner
        if (!appState.dataStorageBannerDismissed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Your Data is Safe & Private",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "All your financial data is stored locally in your browser. Nothing is sent to any server. " +
                                   "Your information never leaves your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Export your data from Settings to save snapshots for year-over-year comparison or backup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = { appState.dismissDataStorageBanner() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Overall Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (health.canMeetAllIncludingRetirement)
                    MaterialTheme.colorScheme.primaryContainer
                else if (health.canMeetMustHaveGoals)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Overall Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (health.canMeetAllIncludingRetirement) "On Track - All Goals + Retirement"
                           else if (health.canMeetAllGoals) "Caution - Goals Only (Retirement Short)"
                           else if (health.canMeetMustHaveGoals) "Caution - Must-Have Goals Only"
                           else "Off Track",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Current Corpus: ₹${formatAmount(currentCorpus)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Corpus at Retirement: ₹${formatAmount(health.totalCorpusAtRetirement)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total Required: ₹${formatAmount(health.totalRequiredIncludingRetirement)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Retirement Corpus Card with Collapsible Explanation
        var isRetirementExpanded by remember { mutableStateOf(false) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header with expand/collapse button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Retirement Corpus",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { isRetirementExpanded = !isRetirementExpanded }) {
                        Icon(
                            imageVector = if (isRetirementExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isRetirementExpanded) "Collapse" else "Expand"
                        )
                    }
                }

                Text(
                    text = "₹${formatAmount(health.retirementCorpusRequired)}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = if (health.canMeetRetirement) "Funded" else "Shortfall: ₹${formatAmount(health.retirementCorpusRequired - health.totalCorpusAtRetirement)}",
                    color = if (health.canMeetRetirement)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Retirement Surplus/Shortfall
                HorizontalDivider()

                Text(
                    text = if (health.overallSurplus >= 0) "Surplus at Retirement" else "Shortfall at Retirement",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${formatAmount(kotlin.math.abs(health.overallSurplus))}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (health.overallSurplus >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Available after all goals and living expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Corpus at life expectancy
                val lastProjection = analysis.yearlyProjections.lastOrNull()
                if (lastProjection != null) {
                    if (lastProjection.totalCorpus > 0) {
                        Text(
                            text = "Corpus at age ${lastProjection.age}: ₹${formatAmount(lastProjection.totalCorpus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Find when corpus goes to zero
                        val zeroYear = analysis.yearlyProjections.findLast { it.totalCorpus > 0 }
                        if (zeroYear != null) {
                            Text(
                                text = "WARNING: Corpus depletes at age ${zeroYear.age + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Collapsible Explanation
                if (isRetirementExpanded) {
                    Divider()

                    val profile = appState.data.userProfile
                    val yearsToRetirement = profile.retirementAge - profile.currentAge
                    val yearsInRetirement = profile.lifeExpectancy - profile.retirementAge
                    val inflationCategory = appState.data.inflationCategories.find {
                        it.id == profile.expenseInflationCategoryId
                    }
                    val expenseInflationRate = inflationCategory?.rate ?: 6.0

                    // Calculate monthly expense at retirement
                    val monthlyExpenseAtRetirement = profile.currentMonthlyExpenses *
                        (1 + expenseInflationRate / 100).pow(yearsToRetirement.toDouble())

                    Text(
                        text = "How we calculated this:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Text(
                        text = "• Current monthly expenses: ₹${formatAmount(profile.currentMonthlyExpenses)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "• Inflation-adjusted at retirement (${yearsToRetirement} years): ₹${formatAmount(monthlyExpenseAtRetirement)}/month",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "• Years in retirement: ${yearsInRetirement} years (age ${profile.retirementAge} to ${profile.lifeExpectancy})",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "• Expense inflation rate: ${expenseInflationRate}% (${inflationCategory?.name ?: "General"})",
                        style = MaterialTheme.typography.bodySmall
                    )

                    val postRetirementRate = appState.data.userProfile.postRetirementGrowthRate

                    Text(
                        text = "• Expected post-retirement growth: ${(postRetirementRate * 10).toLong() / 10.0}%",
                        style = MaterialTheme.typography.bodySmall
                    )

                    val realRate = postRetirementRate - expenseInflationRate
                    Text(
                        text = "• Real return rate (growth - inflation): ${(realRate * 10).toLong() / 10.0}%",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "The retirement corpus uses Present Value calculation - you don't need the entire amount on day 1. Starting at ₹${formatAmount(monthlyExpenseAtRetirement)}/month at retirement, expenses grow with ${expenseInflationRate}% inflation while your corpus grows at ${(postRetirementRate * 10).toLong() / 10.0}% for ${yearsInRetirement} years.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Total Required at Retirement:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    val retirementYear = analysis.currentYear + (profile.retirementAge - profile.currentAge)
                    val preRetirementGoals = analysis.goalAnalyses.filter { it.goal.targetYear <= retirementYear }
                    val postRetirementGoals = analysis.goalAnalyses.filter { it.goal.targetYear > retirementYear }
                    val postRetirementRateDecimal = postRetirementRate / 100.0
                    val postRetirementGoalsPV = postRetirementGoals.sumOf { goalAnalysis ->
                        val yearsFromRetirement = goalAnalysis.goal.targetYear - retirementYear
                        goalAnalysis.inflationAdjustedAmount / (1 + postRetirementRateDecimal).pow(yearsFromRetirement.toDouble())
                    }

                    // Calculate actual impact of pre-retirement goals including lost growth
                    val preRetirementGoalsImpact = health.totalRequiredIncludingRetirement - postRetirementGoalsPV - health.retirementCorpusRequired

                    Text(
                        text = "• Pre-retirement goals: ₹${formatAmount(preRetirementGoalsImpact)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "  (includes lost growth from early withdrawal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "• Post-retirement goals: ₹${formatAmount(postRetirementGoalsPV)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "  (Present Value - discounted to retirement)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "• Retirement living expenses: ₹${formatAmount(health.retirementCorpusRequired)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "  (Present Value - grows while withdrawn)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Total Required: ₹${formatAmount(health.totalRequiredIncludingRetirement)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "All post-retirement amounts use Present Value - accounting for the fact that money continues to grow while being withdrawn gradually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Must-Have Goals Status
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Must-Have Goals",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${health.mustHaveGoalsFunded} / ${health.mustHaveGoalsTotal}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Funded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Required: ₹${formatAmount(health.totalMustHaveGoalsRequired)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Good-to-Have Goals Status
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Good-to-Have Goals",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${health.goodToHaveGoalsFunded} / ${health.goodToHaveGoalsTotal}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Funded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Investments", style = MaterialTheme.typography.labelMedium)
                    Text("${appState.data.investments.size}", style = MaterialTheme.typography.titleLarge)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Goals", style = MaterialTheme.typography.labelMedium)
                    Text("${appState.data.goals.size}", style = MaterialTheme.typography.titleLarge)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ongoing Contributions", style = MaterialTheme.typography.labelMedium)
                    Text("${appState.data.ongoingContributions.size}", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
fun OnboardingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> "${(amount / 10000000 * 100).toLong() / 100.0} Cr"
        amount >= 100000 -> "${(amount / 100000 * 100).toLong() / 100.0} L"
        else -> "${amount.toLong()}"
    }
}
