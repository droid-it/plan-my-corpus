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

@Composable
fun ContributionsScreen(appState: AppState, showHeader: Boolean = true) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContribution by remember { mutableStateOf<OngoingContribution?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(if (showHeader) 16.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (showHeader) Arrangement.End else Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (!showHeader) {
                // When in Portfolio tab, show total contributions
                val totalContributions = appState.data.ongoingContributions
                    .filter { it.isEnabled }
                    .sumOf { it.amount }
                Text(
                    text = "Monthly Total: ₹${formatAmount(totalContributions)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contribution")
            }
        }

        if (appState.data.ongoingContributions.isEmpty()) {
            Text("No ongoing contributions yet. Click + to add one!")
        } else {
            appState.data.ongoingContributions.forEach { contribution ->
                ContributionCard(
                    contribution = contribution,
                    categories = appState.data.investmentCategories,
                    onEdit = { editingContribution = it },
                    onDelete = { appState.removeContribution(it.id) },
                    onToggle = { appState.toggleContribution(it.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        ContributionDialog(
            contribution = null,
            categories = appState.data.investmentCategories,
            onDismiss = { showAddDialog = false },
            onSave = { contribution ->
                appState.addContribution(contribution)
                showAddDialog = false
            }
        )
    }

    editingContribution?.let { contribution ->
        ContributionDialog(
            contribution = contribution,
            categories = appState.data.investmentCategories,
            onDismiss = { editingContribution = null },
            onSave = { updated ->
                appState.updateContribution(contribution.id, updated)
                editingContribution = null
            }
        )
    }
}

@Composable
fun ContributionCard(
    contribution: OngoingContribution,
    categories: List<InvestmentCategory>,
    onEdit: (OngoingContribution) -> Unit,
    onDelete: (OngoingContribution) -> Unit,
    onToggle: (OngoingContribution) -> Unit
) {
    val category = categories.find { it.id == contribution.categoryId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!contribution.isEnabled) Modifier.alpha(0.5f) else Modifier
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(contribution.name, style = MaterialTheme.typography.titleMedium)
                    Text("Amount: ₹${formatAmount(contribution.amount)} ${contribution.frequency}")
                    if (contribution.stepUpPercentage > 0) {
                        Text("Step-up: ${contribution.stepUpPercentage}% yearly", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    val durationText = if (contribution.durationYears != null) {
                        "Duration: ${contribution.durationYears} years"
                    } else {
                        "Until retirement"
                    }
                    Text(durationText, style = MaterialTheme.typography.bodyMedium)
                    category?.let {
                        Text("Category: ${it.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onEdit(contribution) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(contribution) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = if (contribution.isEnabled) "Included in calculations" else "Excluded from calculations",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (contribution.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                Switch(
                    checked = contribution.isEnabled,
                    onCheckedChange = { onToggle(contribution) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionDialog(
    contribution: OngoingContribution?,
    categories: List<InvestmentCategory>,
    onDismiss: () -> Unit,
    onSave: (OngoingContribution) -> Unit
) {
    var name by remember { mutableStateOf(contribution?.name ?: "") }
    var amount by remember { mutableStateOf(contribution?.amount?.toString() ?: "") }
    var durationYears by remember { mutableStateOf(contribution?.durationYears?.toString() ?: "") }
    var stepUpPercentage by remember { mutableStateOf(contribution?.stepUpPercentage?.toString() ?: "0") }
    var selectedFrequency by remember { mutableStateOf(contribution?.frequency ?: ContributionFrequency.MONTHLY) }
    var selectedCategoryId by remember { mutableStateOf(contribution?.categoryId ?: categories.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contribution == null) "Add Contribution" else "Edit Contribution") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contribution Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                CurrencyTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationYears,
                    onValueChange = { durationYears = it },
                    label = { Text("Duration (years)") },
                    supportingText = { Text("Leave empty to continue until retirement") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stepUpPercentage,
                    onValueChange = { stepUpPercentage = it },
                    label = { Text("Annual Step-up (%)") },
                    supportingText = { Text("Yearly increase in contribution (e.g., 10 for 10%)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Frequency dropdown
                var expandedFrequency by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedFrequency,
                    onExpandedChange = { expandedFrequency = it }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrequency,
                        onDismissRequest = { expandedFrequency = false }
                    ) {
                        ContributionFrequency.entries.forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name) },
                                onClick = {
                                    selectedFrequency = frequency
                                    expandedFrequency = false
                                }
                            )
                        }
                    }
                }

                // Category dropdown
                var expandedCategory by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name} (${it.preRetirementXIRR}%)" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Investment Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.name} (${category.preRetirementXIRR}%)") },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expandedCategory = false
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
                    val newContribution = OngoingContribution(
                        id = contribution?.id ?: randomUUID(),
                        name = name,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        frequency = selectedFrequency,
                        categoryId = selectedCategoryId,
                        durationYears = durationYears.toIntOrNull(),
                        stepUpPercentage = stepUpPercentage.toDoubleOrNull() ?: 0.0
                    )
                    onSave(newContribution)
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
