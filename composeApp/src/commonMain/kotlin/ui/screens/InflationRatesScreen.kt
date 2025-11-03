package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.InflationCategory
import randomUUID

@Composable
fun InflationRatesScreen(appState: AppState) {
    var editingCategory by remember { mutableStateOf<InflationCategory?>(null) }
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
            text = "Manage inflation rate categories for different expense types and goals.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        appState.data.inflationCategories.forEach { category ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                        Text("${category.rate}%", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { editingCategory = category }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { appState.removeInflationCategory(category.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        InflationCategoryDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onSave = { newCategory ->
                appState.addInflationCategory(newCategory)
                showAddDialog = false
            }
        )
    }

    editingCategory?.let { category ->
        InflationCategoryDialog(
            category = category,
            onDismiss = { editingCategory = null },
            onSave = { updated ->
                appState.updateInflationCategory(category.id, updated)
                editingCategory = null
            }
        )
    }
}

@Composable
fun InflationCategoryDialog(
    category: InflationCategory?,
    onDismiss: () -> Unit,
    onSave: (InflationCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var rate by remember { mutableStateOf(category?.rate?.toString() ?: "6.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Inflation Category" else "Edit ${category.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (category == null) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name") },
                        supportingText = { Text("e.g., Food, Travel, Utilities") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Inflation Rate (%)") },
                    supportingText = { Text("Annual inflation rate") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rateValue = rate.toDoubleOrNull() ?: 6.0

                    val result = if (category == null) {
                        InflationCategory(
                            id = randomUUID(),
                            name = name,
                            rate = rateValue
                        )
                    } else {
                        category.copy(rate = rateValue)
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
