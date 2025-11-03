package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import model.*
import randomUUID
import ui.components.CurrencyTextField
import getCurrentYear
import kotlin.math.pow

@Composable
fun GoalsScreen(appState: AppState) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<FinancialGoal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }

        if (appState.data.goals.isEmpty()) {
            Text("No goals yet. Click + to start planning your financial future!")
        } else {
            appState.data.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: FinancialGoal?,
    inflationCategories: List<InflationCategory>,
    onDismiss: () -> Unit,
    onSave: (FinancialGoal) -> Unit,
    appState: AppState
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var targetYear by remember { mutableStateOf(goal?.targetYear?.toString() ?: "") }
    var selectedInflationCategoryId by remember { mutableStateOf(goal?.inflationCategoryId ?: inflationCategories.firstOrNull()?.id ?: "") }
    var selectedPriority by remember { mutableStateOf(goal?.priority ?: GoalPriority.MUST_HAVE) }
    var selectedTimeline by remember { mutableStateOf(goal?.timeline ?: GoalTimeline.LONG_TERM) }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recurringEndAge,
                        onValueChange = { recurringEndAge = it },
                        label = { Text("End Age") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // One-time goal field
                    OutlinedTextField(
                        value = targetYear,
                        onValueChange = { targetYear = it },
                        label = { Text("Target Year") },
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

                // Timeline dropdown
                var expandedTimeline by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedTimeline,
                    onExpandedChange = { expandedTimeline = it }
                ) {
                    OutlinedTextField(
                        value = selectedTimeline.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Timeline") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeline) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTimeline,
                        onDismissRequest = { expandedTimeline = false }
                    ) {
                        GoalTimeline.entries.forEach { timeline ->
                            DropdownMenuItem(
                                text = { Text(timeline.name) },
                                onClick = {
                                    selectedTimeline = timeline
                                    expandedTimeline = false
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
                    OutlinedTextField(
                        value = inflationCategories.find { it.id == selectedInflationCategoryId }?.name ?: "",
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

                    val newGoal = FinancialGoal(
                        id = goal?.id ?: randomUUID(),
                        name = name,
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        targetYear = effectiveTargetYear,
                        inflationCategoryId = selectedInflationCategoryId,
                        priority = selectedPriority,
                        timeline = selectedTimeline,
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
