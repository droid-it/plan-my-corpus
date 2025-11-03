package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.UserProfile
import ui.components.CurrencyTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var currentAge by remember { mutableStateOf(appState.data.userProfile.currentAge.toString()) }
        var retirementAge by remember { mutableStateOf(appState.data.userProfile.retirementAge.toString()) }
        var lifeExpectancy by remember { mutableStateOf(appState.data.userProfile.lifeExpectancy.toString()) }
        var monthlyExpenses by remember { mutableStateOf(appState.data.userProfile.currentMonthlyExpenses.toString()) }
        var postRetirementGrowthRate by remember { mutableStateOf(appState.data.userProfile.postRetirementGrowthRate.toString()) }
        var selectedInflationCategoryId by remember { mutableStateOf(appState.data.userProfile.expenseInflationCategoryId) }
        var expandedDropdown by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = currentAge,
            onValueChange = { currentAge = it },
            label = { Text("Current Age") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = retirementAge,
            onValueChange = { retirementAge = it },
            label = { Text("Retirement Age") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lifeExpectancy,
            onValueChange = { lifeExpectancy = it },
            label = { Text("Life Expectancy") },
            modifier = Modifier.fillMaxWidth()
        )

        CurrencyTextField(
            value = monthlyExpenses,
            onValueChange = { monthlyExpenses = it },
            label = "Current Monthly Expenses",
            supportingText = { Text("In today's value - used for retirement planning") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = postRetirementGrowthRate,
            onValueChange = { postRetirementGrowthRate = it },
            label = { Text("Post-Retirement Growth Rate (%)") },
            supportingText = { Text("Expected annual return on entire corpus after retirement") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = it }
        ) {
            OutlinedTextField(
                value = appState.data.inflationCategories.find { it.id == selectedInflationCategoryId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Expense Inflation Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                appState.data.inflationCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text("${category.name} (${category.rate}%)") },
                        onClick = {
                            selectedInflationCategoryId = category.id
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                val age = currentAge.toIntOrNull() ?: 30
                val retAge = retirementAge.toIntOrNull() ?: 60
                val lifeExp = lifeExpectancy.toIntOrNull() ?: 85
                val expenses = monthlyExpenses.toDoubleOrNull() ?: 50000.0
                val postRetGrowth = postRetirementGrowthRate.toDoubleOrNull() ?: 10.0
                appState.updateUserProfile(
                    UserProfile(
                        currentAge = age,
                        retirementAge = retAge,
                        lifeExpectancy = lifeExp,
                        currentMonthlyExpenses = expenses,
                        expenseInflationCategoryId = selectedInflationCategoryId,
                        postRetirementGrowthRate = postRetGrowth
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}
