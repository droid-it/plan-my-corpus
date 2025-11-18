package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.SnapshotType
import model.YearlySnapshot

/**
 * Year selector component for multi-year view
 * Displays a dropdown to select which year's data to view/edit
 */
@Composable
fun YearSelector(
    currentYear: Int,
    selectedYear: Int,
    yearlySnapshots: List<YearlySnapshot>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viewing Year:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    // Show current year badge
                    val snapshot = yearlySnapshots.find { it.year == selectedYear }
                    val isHistorical = snapshot?.snapshotType == SnapshotType.HISTORICAL

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock icon for historical years
                        if (isHistorical) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Historical (Read-only)",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(text = selectedYear.toString())

                        // Star icon for current year
                        if (selectedYear == currentYear) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Current Year",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Sort years in descending order (newest first)
                    yearlySnapshots.sortedByDescending { it.year }.forEach { snapshot ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (snapshot.snapshotType == SnapshotType.HISTORICAL) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Historical",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = snapshot.year.toString(),
                                        style = if (snapshot.year == selectedYear) {
                                            MaterialTheme.typography.bodyLarge
                                        } else {
                                            MaterialTheme.typography.bodyMedium
                                        }
                                    )

                                    if (snapshot.year == currentYear) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Current",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "(current)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onYearSelected(snapshot.year)
                                expanded = false
                            },
                            enabled = true
                        )
                    }
                }
            }
        }

        // Info banner for historical years
        val selectedSnapshot = yearlySnapshots.find { it.year == selectedYear }
        if (selectedSnapshot?.snapshotType == SnapshotType.HISTORICAL) {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Historical data is read-only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
