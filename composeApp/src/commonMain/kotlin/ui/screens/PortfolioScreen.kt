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
import model.Investment
import randomUUID
import ui.components.CurrencyTextField

@Composable
fun PortfolioScreen(appState: AppState) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingInvestment by remember { mutableStateOf<Investment?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Current Investments") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Ongoing Contributions") }
            )
        }

        when (selectedTab) {
            0 -> CurrentInvestmentsTab(appState, showAddDialog, editingInvestment,
                onShowAddDialog = { showAddDialog = it },
                onEditingInvestment = { editingInvestment = it })
            1 -> ContributionsScreen(appState, showHeader = false)
        }
    }

    if (showAddDialog) {
        InvestmentDialog(
            investment = null,
            categories = appState.data.investmentCategories,
            onDismiss = { showAddDialog = false },
            onSave = { investment ->
                appState.addInvestment(investment)
                showAddDialog = false
            }
        )
    }

    editingInvestment?.let { investment ->
        InvestmentDialog(
            investment = investment,
            categories = appState.data.investmentCategories,
            onDismiss = { editingInvestment = null },
            onSave = { updated ->
                appState.updateInvestment(investment.id, updated)
                editingInvestment = null
            }
        )
    }
}

@Composable
fun CurrentInvestmentsTab(
    appState: AppState,
    showAddDialog: Boolean,
    editingInvestment: Investment?,
    onShowAddDialog: (Boolean) -> Unit,
    onEditingInvestment: (Investment?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Total: ₹${formatAmount(appState.data.investments.sumOf { it.currentValue })}",
                style = MaterialTheme.typography.titleMedium
            )
            FloatingActionButton(onClick = { onShowAddDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment")
            }
        }

        if (appState.data.investments.isEmpty()) {
            Text("No investments yet. Click + to add your first investment!")
        } else {
            appState.data.investments.forEach { investment ->
                InvestmentCard(
                    investment = investment,
                    categories = appState.data.investmentCategories,
                    onEdit = { onEditingInvestment(it) },
                    onDelete = { appState.removeInvestment(it.id) },
                    onToggle = { appState.toggleInvestment(it.id) }
                )
            }
        }
    }
}

@Composable
fun InvestmentCard(
    investment: Investment,
    categories: List<model.InvestmentCategory>,
    onEdit: (Investment) -> Unit,
    onDelete: (Investment) -> Unit,
    onToggle: (Investment) -> Unit
) {
    val category = categories.find { it.id == investment.categoryId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!investment.isEnabled) Modifier.alpha(0.5f) else Modifier
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
                    Text(investment.name, style = MaterialTheme.typography.titleMedium)
                    Text("Value: ₹${formatAmount(investment.currentValue)}")
                    Text("Actual XIRR: ${investment.actualXIRR}%")
                    category?.let {
                        Text("Category: ${it.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onEdit(investment) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(investment) }) {
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
                    text = if (investment.isEnabled) "Included in calculations" else "Excluded from calculations",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (investment.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                Switch(
                    checked = investment.isEnabled,
                    onCheckedChange = { onToggle(investment) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentDialog(
    investment: Investment?,
    categories: List<model.InvestmentCategory>,
    onDismiss: () -> Unit,
    onSave: (Investment) -> Unit
) {
    var name by remember { mutableStateOf(investment?.name ?: "") }
    var value by remember { mutableStateOf(investment?.currentValue?.toString() ?: "") }
    var xirr by remember { mutableStateOf(investment?.actualXIRR?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(investment?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (investment == null) "Add Investment" else "Edit Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Investment Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                CurrencyTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Current Value",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = xirr,
                    onValueChange = { xirr = it },
                    label = { Text("Actual XIRR (%)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Investment Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expandedDropdown = false
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
                    val valueDouble = value.toDoubleOrNull() ?: 0.0
                    val xirrDouble = xirr.toDoubleOrNull() ?: 0.0
                    val newInvestment = Investment(
                        id = investment?.id ?: randomUUID(),
                        name = name,
                        currentValue = valueDouble,
                        categoryId = selectedCategoryId,
                        actualXIRR = xirrDouble
                    )
                    onSave(newInvestment)
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
