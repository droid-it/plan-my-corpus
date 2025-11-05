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

enum class FutureInvestmentType {
    LUMPSUM,
    RECURRING
}

@Composable
fun PortfolioScreen(appState: AppState) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddCurrentDialog by remember { mutableStateOf(false) }
    var showFutureTypeSelector by remember { mutableStateOf(false) }
    var editingInvestment by remember { mutableStateOf<Investment?>(null) }
    var editingFutureLumpsum by remember { mutableStateOf<FutureLumpsumInvestment?>(null) }
    var editingContribution by remember { mutableStateOf<OngoingContribution?>(null) }

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
                text = { Text("Current Portfolio") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Future Investments") }
            )
        }

        when (selectedTab) {
            0 -> CurrentPortfolioTab(
                appState = appState,
                onShowAddDialog = { showAddCurrentDialog = true },
                onEditInvestment = { editingInvestment = it }
            )
            1 -> FutureInvestmentsTab(
                appState = appState,
                onShowAddDialog = { showFutureTypeSelector = true },
                onEditFutureLumpsum = { editingFutureLumpsum = it },
                onEditContribution = { editingContribution = it }
            )
        }
    }

    // Add Current Investment Dialog
    if (showAddCurrentDialog) {
        InvestmentDialog(
            investment = null,
            categories = appState.data.investmentCategories,
            isFirstInvestment = appState.data.investments.isEmpty(),
            onDismiss = { showAddCurrentDialog = false },
            onSave = { investment ->
                appState.addInvestment(investment)
                showAddCurrentDialog = false
            }
        )
    }

    // Edit Current Investment Dialog
    editingInvestment?.let { investment ->
        InvestmentDialog(
            investment = investment,
            categories = appState.data.investmentCategories,
            isFirstInvestment = false,
            onDismiss = { editingInvestment = null },
            onSave = { updated ->
                appState.updateInvestment(investment.id, updated)
                editingInvestment = null
            }
        )
    }

    // Future Investment Type Selector
    if (showFutureTypeSelector) {
        FutureInvestmentTypeDialog(
            onDismiss = { showFutureTypeSelector = false },
            onSelectType = { type ->
                showFutureTypeSelector = false
                when (type) {
                    FutureInvestmentType.LUMPSUM -> editingFutureLumpsum = FutureLumpsumInvestment(
                        id = randomUUID(),
                        name = "",
                        plannedAmount = 0.0,
                        plannedYear = getCurrentYear(),
                        categoryId = appState.data.investmentCategories.firstOrNull()?.id ?: "",
                        isEnabled = true
                    )
                    FutureInvestmentType.RECURRING -> editingContribution = OngoingContribution(
                        id = randomUUID(),
                        name = "",
                        amount = 0.0,
                        frequency = ContributionFrequency.MONTHLY,
                        categoryId = appState.data.investmentCategories.firstOrNull()?.id ?: "",
                        durationYears = null,
                        stepUpPercentage = 0.0,
                        isEnabled = true
                    )
                }
            }
        )
    }

    // Edit Future Lumpsum Dialog
    editingFutureLumpsum?.let { investment ->
        val isNew = investment.name.isEmpty()
        FutureLumpsumDialog(
            investment = if (isNew) null else investment,
            categories = appState.data.investmentCategories,
            onDismiss = { editingFutureLumpsum = null },
            onSave = { updated ->
                if (isNew) {
                    appState.addFutureLumpsumInvestment(updated)
                } else {
                    appState.updateFutureLumpsumInvestment(investment.id, updated)
                }
                editingFutureLumpsum = null
            }
        )
    }

    // Edit Contribution Dialog
    editingContribution?.let { contribution ->
        val isNew = contribution.name.isEmpty()
        ContributionDialog(
            contribution = if (isNew) null else contribution,
            categories = appState.data.investmentCategories,
            isFirstContribution = false,
            onDismiss = { editingContribution = null },
            onSave = { updated ->
                if (isNew) {
                    appState.addContribution(updated)
                } else {
                    appState.updateContribution(contribution.id, updated)
                }
                editingContribution = null
            }
        )
    }
}

@Composable
fun CurrentPortfolioTab(
    appState: AppState,
    onShowAddDialog: () -> Unit,
    onEditInvestment: (Investment) -> Unit
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
                text = "Total: ₹${formatAmount(appState.data.investments.filter { it.isEnabled }.sumOf { it.currentValue })}",
                style = MaterialTheme.typography.titleMedium
            )
            FloatingActionButton(onClick = onShowAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment")
            }
        }

        if (appState.data.investments.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No current investments yet. Click + to add your existing investments!",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            appState.data.investments.forEach { investment ->
                InvestmentCard(
                    investment = investment,
                    categories = appState.data.investmentCategories,
                    onEdit = onEditInvestment,
                    onDelete = { appState.removeInvestment(it.id) },
                    onToggle = { appState.toggleInvestment(it.id) }
                )
            }
        }
    }
}

@Composable
fun FutureInvestmentsTab(
    appState: AppState,
    onShowAddDialog: () -> Unit,
    onEditFutureLumpsum: (FutureLumpsumInvestment) -> Unit,
    onEditContribution: (OngoingContribution) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(onClick = onShowAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add Future Investment")
            }
        }

        // Section 1: Future Lumpsum Investments
        Text(
            "One-time Investments",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (appState.data.futureLumpsumInvestments.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No planned lumpsum investments. Click + to add a one-time future investment!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            appState.data.futureLumpsumInvestments.forEach { investment ->
                FutureLumpsumCard(
                    investment = investment,
                    categories = appState.data.investmentCategories,
                    currentYear = getCurrentYear(),
                    onEdit = onEditFutureLumpsum,
                    onDelete = { appState.removeFutureLumpsumInvestment(it.id) },
                    onToggle = { appState.toggleFutureLumpsumInvestment(it.id) }
                )
            }
        }

        // Section 2: Recurring Contributions
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            "Recurring Contributions (SIPs)",
            style = MaterialTheme.typography.titleMedium
        )

        if (appState.data.ongoingContributions.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No recurring contributions. Click + to add a periodic investment (SIP)!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            appState.data.ongoingContributions.forEach { contribution ->
                ContributionCard(
                    contribution = contribution,
                    categories = appState.data.investmentCategories,
                    onEdit = onEditContribution,
                    onDelete = { appState.removeContribution(it.id) },
                    onToggle = { appState.toggleContribution(it.id) }
                )
            }
        }
    }
}

@Composable
fun FutureInvestmentTypeDialog(
    onDismiss: () -> Unit,
    onSelectType: (FutureInvestmentType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Future Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What type of future investment would you like to add?")

                Button(
                    onClick = { onSelectType(FutureInvestmentType.LUMPSUM) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("One-time Lumpsum", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "A single investment planned for a specific year",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = { onSelectType(FutureInvestmentType.RECURRING) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Recurring SIP/Contribution", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Regular periodic investments (monthly/quarterly/yearly)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InvestmentCard(
    investment: Investment,
    categories: List<InvestmentCategory>,
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

@Composable
fun FutureLumpsumCard(
    investment: FutureLumpsumInvestment,
    categories: List<InvestmentCategory>,
    currentYear: Int,
    onEdit: (FutureLumpsumInvestment) -> Unit,
    onDelete: (FutureLumpsumInvestment) -> Unit,
    onToggle: (FutureLumpsumInvestment) -> Unit
) {
    val category = categories.find { it.id == investment.categoryId }
    val yearsFromNow = investment.plannedYear - currentYear

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
                    Text("Amount: ₹${formatAmount(investment.plannedAmount)}")
                    Text(
                        "Planned Year: ${investment.plannedYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (yearsFromNow > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    if (yearsFromNow > 0) {
                        Text("($yearsFromNow years from now)", style = MaterialTheme.typography.bodySmall)
                    }
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
    categories: List<InvestmentCategory>,
    isFirstInvestment: Boolean = false,
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
        title = { Text(if (investment == null) "Add Current Investment" else "Edit Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Show info card for first investment
                if (isFirstInvestment && investment == null) {
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
                                "Tip: Manage Investment Categories",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "You can add more investment categories or edit expected XIRR rates from the Settings page.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Investment Name") },
                    supportingText = { Text("E.g., PPF Account, ELSS Fund, Real Estate") },
                    modifier = Modifier.fillMaxWidth()
                )

                CurrencyTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Current Value",
                    supportingText = { Text("Today's market value of your investment") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = xirr,
                    onValueChange = { xirr = it },
                    label = { Text("Actual XIRR (%)") },
                    supportingText = { Text("Historical return rate of this specific investment") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name} (${it.preRetirementXIRR}%)" } ?: "",
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
                                text = { Text("${category.name} (${category.preRetirementXIRR}%)") },
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
                        actualXIRR = xirrDouble,
                        isEnabled = investment?.isEnabled ?: true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureLumpsumDialog(
    investment: FutureLumpsumInvestment?,
    categories: List<InvestmentCategory>,
    onDismiss: () -> Unit,
    onSave: (FutureLumpsumInvestment) -> Unit
) {
    var name by remember { mutableStateOf(investment?.name ?: "") }
    var amount by remember { mutableStateOf(investment?.plannedAmount?.toString() ?: "") }
    var year by remember { mutableStateOf(investment?.plannedYear?.toString() ?: getCurrentYear().toString()) }
    var selectedCategoryId by remember { mutableStateOf(investment?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (investment == null) "Add Future Lumpsum Investment" else "Edit Future Lumpsum") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Investment Name") },
                    supportingText = { Text("E.g., Bonus Investment, Inheritance, Property Sale") },
                    modifier = Modifier.fillMaxWidth()
                )

                CurrencyTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Planned Amount",
                    supportingText = { Text("One-time investment amount you plan to make") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Planned Year") },
                    supportingText = { Text("Year when you plan to make this investment") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name} (${it.preRetirementXIRR}%)" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Investment Category") },
                        supportingText = { Text("Expected growth category for this investment") },
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
                                text = { Text("${category.name} (${category.preRetirementXIRR}%)") },
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
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val yearInt = year.toIntOrNull() ?: getCurrentYear()
                    val newInvestment = FutureLumpsumInvestment(
                        id = investment?.id ?: randomUUID(),
                        name = name,
                        plannedAmount = amountDouble,
                        plannedYear = yearInt,
                        categoryId = selectedCategoryId,
                        isEnabled = investment?.isEnabled ?: true
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

// Platform-specific function
expect fun getCurrentYear(): Int
