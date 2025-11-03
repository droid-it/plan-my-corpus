package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.InvestmentCategory
import randomUUID

@Composable
fun InvestmentCategoriesScreen(appState: AppState) {
    var editingCategory by remember { mutableStateOf<InvestmentCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }

        Text(
            text = "Manage investment categories and their XIRR rates. These rates are used to project future corpus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        appState.data.investmentCategories.forEach { category ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                        Text("Expected XIRR: ${category.preRetirementXIRR}%", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { editingCategory = category }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { appState.removeInvestmentCategory(category.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onSave = { newCategory ->
                appState.addInvestmentCategory(newCategory)
                showAddDialog = false
            }
        )
    }

    editingCategory?.let { category ->
        CategoryEditDialog(
            category = category,
            onDismiss = { editingCategory = null },
            onSave = { updated ->
                appState.updateInvestmentCategory(category.id, updated)
                editingCategory = null
            }
        )
    }
}

@Composable
fun CategoryEditDialog(
    category: InvestmentCategory?,
    onDismiss: () -> Unit,
    onSave: (InvestmentCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var preRetirementXIRR by remember { mutableStateOf(category?.preRetirementXIRR?.toString() ?: "12.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Investment Category" else "Edit ${category.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (category == null) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name") },
                        supportingText = { Text("e.g., Real Estate, Gold, Crypto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = preRetirementXIRR,
                    onValueChange = { preRetirementXIRR = it },
                    label = { Text("Expected XIRR (%)") },
                    supportingText = { Text("Expected annual return rate") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val xirr = preRetirementXIRR.toDoubleOrNull() ?: 12.0

                    val result = if (category == null) {
                        InvestmentCategory(
                            id = randomUUID(),
                            name = name,
                            preRetirementXIRR = xirr
                        )
                    } else {
                        category.copy(
                            preRetirementXIRR = xirr
                        )
                    }
                    onSave(result)
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
