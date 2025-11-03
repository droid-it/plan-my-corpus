package ui.screens

import AppState
import Screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Data Management Section
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { appState.downloadSnapshot() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Snapshot")
            }
            OutlinedButton(
                onClick = { appState.uploadSnapshot() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Snapshot")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Configuration Section
        Text(
            text = "Configuration",
            style = MaterialTheme.typography.titleMedium
        )

        // User Profile
        SettingsCard(
            title = "User Profile",
            description = "Age, retirement age, life expectancy, monthly expenses",
            onClick = { appState.navigateTo(Screen.UserProfile) }
        )

        // Investment Categories
        SettingsCard(
            title = "Investment Categories",
            description = "Edit XIRR rates for each investment category",
            onClick = { appState.navigateTo(Screen.InvestmentCategories) }
        )

        // Inflation Rates
        SettingsCard(
            title = "Inflation Rates",
            description = "Edit inflation rates for different categories",
            onClick = { appState.navigateTo(Screen.InflationRates) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Information Section
        Text(
            text = "Information",
            style = MaterialTheme.typography.titleMedium
        )

        // About & Disclaimer
        SettingsCard(
            title = "About & Disclaimer",
            description = "App information, legal disclaimer, and contact",
            onClick = { appState.navigateTo(Screen.About) }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate"
            )
        }
    }
}
