package ui.screens

import AppState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(appState: AppState) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("About", "Disclaimer", "Contact")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AboutTab()
            1 -> DisclaimerTab()
            2 -> ContactTab()
        }
    }
}

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Financial Planner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Version 1.0.0 (MVP)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Text(
            text = "About This App",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "A comprehensive financial planning application built with Kotlin Multiplatform and Compose Multiplatform. " +
                    "Plan your retirement, track financial goals, and visualize your financial future with precision.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Key Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureItem("User Profile Management - Set age, retirement age, life expectancy, monthly expenses")
                FeatureItem("Financial Goals - Create and track goals with priorities and inflation adjustment")
                FeatureItem("Investment Portfolio - Monitor investments with XIRR tracking")
                FeatureItem("Recurring Goals - Plan for goals that repeat over time (e.g., annual vacation)")
                FeatureItem("Ongoing Contributions - Track SIPs, PPF, and other recurring investments with step-up support")
                FeatureItem("Retirement Planning - Comprehensive corpus calculation with life expectancy projections")
                FeatureItem("Multi-Category Inflation - Different rates for expenses, education, healthcare, etc.")
                FeatureItem("Interactive Charts - Visual corpus projection from current age to life expectancy")
                FeatureItem("Goal Analysis - Funding status, shortfall calculations, and year-by-year breakdown")
                FeatureItem("Export/Import - Save and restore complete financial snapshots")
                FeatureItem("Auto-Save - All changes automatically saved to browser storage")
            }
        }

        Text(
            text = "Technology Stack",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("• Kotlin Multiplatform 2.0.21")
                Text("• Compose Multiplatform 1.7.0")
                Text("• WebAssembly (Wasm) Target")
                Text("• Browser localStorage for data persistence")
                Text("• Material Design 3")
            }
        }

        Text(
            text = "Privacy & Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "All your financial data is stored locally in your browser's localStorage. " +
                    "No data is sent to any server. Your information never leaves your device.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DisclaimerTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Important Disclaimer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = "Please read this disclaimer carefully before using this application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        DisclaimerSection(
            title = "Not Financial Advice",
            content = "This application is provided for informational and educational purposes only. " +
                    "It is NOT professional financial advice, investment advice, or a recommendation to buy or sell any financial product. " +
                    "Always consult with a qualified financial advisor before making any financial decisions."
        )

        DisclaimerSection(
            title = "Calculation Assumptions & Limitations",
            content = "This app uses simplified financial models and assumptions:\n\n" +
                    "• XIRR rates are user-provided estimates and may not reflect actual market returns\n" +
                    "• Inflation rates are assumptions and may differ from actual future inflation\n" +
                    "• Tax implications are NOT considered in calculations\n" +
                    "• Market volatility and sequence of returns risk are not modeled\n" +
                    "• Life expectancy is an estimate; actual lifespan may vary\n" +
                    "• Emergency funds, insurance needs, and debt are not explicitly modeled\n\n" +
                    "Real-world financial planning requires comprehensive analysis by a certified financial planner."
        )

        DisclaimerSection(
            title = "Risk Warnings",
            content = "• Past performance does not guarantee future results\n" +
                    "• Investment returns can be negative; you may lose money\n" +
                    "• Inflation may be higher or lower than projected\n" +
                    "• Healthcare costs often exceed general inflation\n" +
                    "• Unexpected life events (job loss, medical emergencies, etc.) can impact your financial plan\n" +
                    "• This tool cannot predict market crashes, recessions, or economic crises"
        )

        DisclaimerSection(
            title = "Data Privacy",
            content = "All your financial data is stored locally in your browser's localStorage. " +
                    "We do not collect, transmit, or store your data on any server. " +
                    "Your data remains on your device. If you clear your browser data or use a different device, " +
                    "you will need to restore from an exported backup."
        )

        DisclaimerSection(
            title = "No Warranty",
            content = "This software is provided \"as is\" without warranty of any kind, express or implied. " +
                    "The creators of this application are not liable for any financial losses, damages, " +
                    "or consequences arising from the use of this application. Use at your own risk."
        )

        DisclaimerSection(
            title = "Terms of Use",
            content = "By using this application, you acknowledge that:\n\n" +
                    "• You have read and understood this disclaimer\n" +
                    "• You will not rely solely on this application for financial planning decisions\n" +
                    "• You will consult with qualified financial professionals before making significant financial decisions\n" +
                    "• You accept full responsibility for your financial decisions\n" +
                    "• You understand the limitations and assumptions of the calculations"
        )

        Text(
            text = "If you do not agree with these terms, please do not use this application.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ContactTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Contact & Feedback",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "We'd love to hear from you!",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Whether you've found a bug, have a feature suggestion, or just want to share feedback, " +
                            "we're here to help improve this application.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        ContactSection(
            icon = Icons.Default.BugReport,
            title = "Report a Bug",
            content = "Found something not working as expected?\n\n" +
                    "When reporting bugs, please include:\n" +
                    "• Steps to reproduce the issue\n" +
                    "• What you expected to happen\n" +
                    "• What actually happened\n" +
                    "• Your browser and OS version\n" +
                    "• Screenshots if applicable (remove sensitive data first!)"
        )

        ContactSection(
            icon = Icons.Default.Lightbulb,
            title = "Suggest a Feature",
            content = "Have an idea to make this app better?\n\n" +
                    "We're always looking for ways to improve! Tell us:\n" +
                    "• What feature you'd like to see\n" +
                    "• Why it would be useful\n" +
                    "• How you envision it working"
        )

        ContactSection(
            icon = Icons.Default.Chat,
            title = "General Feedback",
            content = "Share your thoughts about the app:\n\n" +
                    "• What do you like about it?\n" +
                    "• What could be improved?\n" +
                    "• How has it helped your financial planning?"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GitHub: [Repository Link]\nEmail: [Your Email]",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Note: Replace with actual contact information before deployment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = "Response Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "This is an open-source project maintained by volunteers. " +
                    "While we strive to respond promptly, please allow reasonable time for replies. " +
                    "Thank you for your patience!",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DisclaimerSection(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ContactSection(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
