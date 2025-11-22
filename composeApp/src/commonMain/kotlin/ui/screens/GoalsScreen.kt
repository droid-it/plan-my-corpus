package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import model.*
import randomUUID
import ui.components.CurrencyTextField
import ui.components.ViewMode
import getCurrentYear
import kotlin.math.pow

/**
 * Auto-assign timeline based on target year
 */
private fun autoAssignTimeline(targetYear: Int, currentYear: Int): GoalTimeline {
    val years = targetYear - currentYear
    return when {
        years <= 5 -> GoalTimeline.SHORT_TERM
        years <= 10 -> GoalTimeline.MEDIUM_TERM
        else -> GoalTimeline.LONG_TERM
    }
}

@Composable
fun GoalsScreen(appState: AppState) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<FinancialGoal?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.EXPANDED) }

    // Handle auto-open from quick start guide
    LaunchedEffect(appState.shouldOpenAddGoalDialog) {
        if (appState.shouldOpenAddGoalDialog) {
            showAddDialog = true
            appState.clearDialogTriggers()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { viewMode = if (viewMode == ViewMode.EXPANDED) ViewMode.COMPACT else ViewMode.EXPANDED }
                ) {
                    Icon(
                        imageVector = if (viewMode == ViewMode.EXPANDED) Icons.Default.TableRows else Icons.Default.ViewAgenda,
                        contentDescription = if (viewMode == ViewMode.EXPANDED) "Switch to Compact View" else "Switch to Expanded View"
                    )
                }
                Text(
                    text = if (viewMode == ViewMode.EXPANDED) "Expanded View" else "Compact View",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                )
            }
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }

        // Sample Data Card for Goals
        if (appState.data.isSampleData && !appState.sampleDataGoalsCardDismissed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Sample Goals",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(onClick = { appState.dismissSampleDataGoalsCard() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Text(
                        text = "We've added sample financial goals including a recurring vacation goal. Feel free to edit, delete, or add your own goals. You can start clean by deleting all data from Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (appState.data.goals.isEmpty()) {
            Text("No goals yet. Click + to start planning your financial future!")
        } else {
            if (viewMode == ViewMode.EXPANDED) {
                appState.data.goals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        inflationCategories = appState.data.inflationCategories,
                        onEdit = { editingGoal = it },
                        onDelete = { appState.removeGoal(it.id) },
                        onToggle = { appState.toggleGoal(it.id) }
                    )
                }
            } else {
                GoalsCompactView(
                    goals = appState.data.goals,
                    inflationCategories = appState.data.inflationCategories,
                    onEdit = { editingGoal = it },
                    onDelete = { appState.removeGoal(it.id) },
                    onToggle = { appState.toggleGoal(it.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        GoalDialog(
            goal = null,
            inflationCategories = appState.data.inflationCategories,
            isFirstGoal = appState.data.goals.isEmpty(),
            onDismiss = { showAddDialog = false },
            onSave = { goal ->
                appState.addGoal(goal)
                showAddDialog = false
            },
            appState = appState
        )
    }

    editingGoal?.let { goal ->
        GoalDialog(
            goal = goal,
            inflationCategories = appState.data.inflationCategories,
            isFirstGoal = false,
            onDismiss = { editingGoal = null },
            onSave = { updated ->
                appState.updateGoal(goal.id, updated)
                editingGoal = null
            },
            appState = appState
        )
    }
}

@Composable
fun GoalCard(
    goal: FinancialGoal,
    inflationCategories: List<InflationCategory>,
    onEdit: (FinancialGoal) -> Unit,
    onDelete: (FinancialGoal) -> Unit,
    onToggle: (FinancialGoal) -> Unit
) {
    val inflationCategory = inflationCategories.find { it.id == goal.inflationCategoryId }

    // Calculate future value
    val inflationRate = inflationCategory?.rate ?: 6.0
    val currentYear = getCurrentYear()
    val years = goal.targetYear - currentYear
    val futureValue = if (years > 0) {
        goal.targetAmount * (1 + inflationRate / 100).pow(years.toDouble())
    } else {
        goal.targetAmount
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!goal.isEnabled) Modifier.alpha(0.5f) else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Chip(goal.priority.name)
                    IconButton(onClick = { onEdit(goal) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(goal) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            if (goal.isRecurring) {
                Text(
                    "Recurring: Every ${goal.recurringFrequencyMonths} months",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "From age ${goal.recurringStartAge ?: "?"} to ${goal.recurringEndAge ?: "?"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("Target (today): ₹${formatAmount(goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium)

            if (!goal.isRecurring) {
                Text(
                    "Future value (${goal.targetYear}): ₹${formatAmount(futureValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (years > 0) {
                    Text(
                        "Inflation-adjusted over $years years at ${inflationRate}% (${inflationCategory?.name ?: "General"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text("Timeline: ${goal.timeline}", style = MaterialTheme.typography.bodySmall)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.isEnabled) "Included in calculations" else "Excluded from calculations",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goal.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                Switch(
                    checked = goal.isEnabled,
                    onCheckedChange = { onToggle(goal) }
                )
            }
        }
    }
}

@Composable
fun GoalsCompactView(
    goals: List<FinancialGoal>,
    inflationCategories: List<InflationCategory>,
    onEdit: (FinancialGoal) -> Unit,
    onDelete: (FinancialGoal) -> Unit,
    onToggle: (FinancialGoal) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Name", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(2f))
                Text("Target", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.5f))
                Text("Year", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                Text("Priority", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Timeline", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Inflation", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Status", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f))
                Text("Actions", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }

            HorizontalDivider()

            // Data Rows
            goals.forEach { goal ->
                val inflationCategory = inflationCategories.find { it.id == goal.inflationCategoryId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .then(if (!goal.isEnabled) Modifier.alpha(0.5f) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Name
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(2f)
                    )

                    // Target Amount
                    Text(
                        text = "₹${formatAmount(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1.5f)
                    )

                    // Year
                    Text(
                        text = if (goal.isRecurring) "Recurring" else goal.targetYear.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.8f)
                    )

                    // Priority
                    Text(
                        text = goal.priority.name.take(4),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )

                    // Timeline
                    Text(
                        text = goal.timeline.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )

                    // Inflation Category
                    Text(
                        text = "${inflationCategory?.rate ?: 0}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )

                    // Status (Toggle)
                    Switch(
                        checked = goal.isEnabled,
                        onCheckedChange = { onToggle(goal) },
                        modifier = Modifier.weight(0.8f)
                    )

                    // Actions
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onEdit(goal) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(goal) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (goal != goals.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: FinancialGoal?,
    inflationCategories: List<InflationCategory>,
    isFirstGoal: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (FinancialGoal) -> Unit,
    appState: AppState
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var targetYear by remember { mutableStateOf(goal?.targetYear?.toString() ?: "") }
    var selectedInflationCategoryId by remember { mutableStateOf(goal?.inflationCategoryId ?: inflationCategories.firstOrNull()?.id ?: "") }
    var selectedPriority by remember { mutableStateOf(goal?.priority ?: GoalPriority.MUST_HAVE) }
    var isRecurring by remember { mutableStateOf(goal?.isRecurring ?: false) }
    var recurringFrequencyMonths by remember { mutableStateOf(goal?.recurringFrequencyMonths?.toString() ?: "12") }
    var recurringStartAge by remember { mutableStateOf(goal?.recurringStartAge?.toString() ?: appState.data.userProfile.currentAge.toString()) }
    var recurringEndAge by remember { mutableStateOf(goal?.recurringEndAge?.toString() ?: appState.data.userProfile.retirementAge.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Add Goal" else "Edit Goal") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show info card for first goal
                if (isFirstGoal && goal == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Tip: Manage Inflation Categories",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "You can add more inflation categories or edit rates from the Settings page to better match your goals.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    supportingText = { Text("E.g., House Down Payment, Child's Education, Vacation") },
                    modifier = Modifier.fillMaxWidth()
                )

                CurrencyTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = "Target Amount (today's value)",
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Recurring Goal Toggle - show early so user makes a choice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recurring Goal", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "For goals that repeat (e.g., annual vacation)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }

                HorizontalDivider()

                // Show appropriate fields based on goal type
                if (isRecurring) {
                    // Recurring goal fields
                    OutlinedTextField(
                        value = recurringFrequencyMonths,
                        onValueChange = { recurringFrequencyMonths = it },
                        label = { Text("Frequency (months)") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("E.g., 12 for annual, 6 for bi-annual") }
                    )

                    OutlinedTextField(
                        value = recurringStartAge,
                        onValueChange = { recurringStartAge = it },
                        label = { Text("Start Age") },
                        supportingText = { Text("Your age when this recurring goal begins") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recurringEndAge,
                        onValueChange = { recurringEndAge = it },
                        label = { Text("End Age") },
                        supportingText = { Text("Your age when this recurring goal ends") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // One-time goal field
                    OutlinedTextField(
                        value = targetYear,
                        onValueChange = { targetYear = it },
                        label = { Text("Target Year") },
                        supportingText = { Text("Year when you need this amount (e.g., 2030)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // Priority dropdown
                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = it }
                ) {
                    OutlinedTextField(
                        value = selectedPriority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        supportingText = { Text("Must-have goals are calculated separately in analysis") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        GoalPriority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name) },
                                onClick = {
                                    selectedPriority = priority
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                // Inflation category dropdown
                var expandedInflation by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedInflation,
                    onExpandedChange = { expandedInflation = it }
                ) {
                    val selectedCategory = inflationCategories.find { it.id == selectedInflationCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name} (${it.rate}%)" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Inflation Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInflation) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInflation,
                        onDismissRequest = { expandedInflation = false }
                    ) {
                        inflationCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.name} (${category.rate}%)") },
                                onClick = {
                                    selectedInflationCategoryId = category.id
                                    expandedInflation = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // For recurring goals, use start age as the target year (placeholder, not actually used)
                    val effectiveTargetYear = if (isRecurring) {
                        val startAge = recurringStartAge.toIntOrNull() ?: appState.data.userProfile.currentAge
                        getCurrentYear() + (startAge - appState.data.userProfile.currentAge)
                    } else {
                        targetYear.toIntOrNull() ?: 2030
                    }

                    // Auto-assign timeline based on target year
                    val autoTimeline = autoAssignTimeline(effectiveTargetYear, getCurrentYear())

                    val newGoal = FinancialGoal(
                        id = goal?.id ?: randomUUID(),
                        name = name,
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        targetYear = effectiveTargetYear,
                        inflationCategoryId = selectedInflationCategoryId,
                        priority = selectedPriority,
                        timeline = autoTimeline,
                        isRecurring = isRecurring,
                        recurringFrequencyMonths = recurringFrequencyMonths.toIntOrNull() ?: 12,
                        recurringStartAge = if (isRecurring) recurringStartAge.toIntOrNull() else null,
                        recurringEndAge = if (isRecurring) recurringEndAge.toIntOrNull() else null
                    )
                    onSave(newGoal)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
