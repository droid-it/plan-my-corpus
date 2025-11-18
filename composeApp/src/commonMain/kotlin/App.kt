import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val appState = remember { AppState() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Hide loader after first composition
    LaunchedEffect(Unit) {
        hideLoader()
    }

    // Show snackbar when message is available
    LaunchedEffect(appState.snackbarMessage) {
        appState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            appState.clearSnackbar()
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getScreenTitle(appState.currentScreen)) },
                    navigationIcon = {
                        if (shouldShowBackButton(appState.currentScreen)) {
                            IconButton(onClick = { appState.navigateBack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar(appState)
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                when (appState.currentScreen) {
                    Screen.Dashboard -> DashboardScreen(appState)
                    Screen.UserProfile -> UserProfileScreen(appState)
                    Screen.InflationRates -> InflationRatesScreen(appState)
                    Screen.InvestmentCategories -> InvestmentCategoriesScreen(appState)
                    Screen.Portfolio -> PortfolioScreen(appState)
                    Screen.Goals -> GoalsScreen(appState)
                    Screen.Analysis -> AnalysisScreen(appState)
                    Screen.Settings -> SettingsScreen(appState)
                    Screen.About -> AboutScreen(appState)
                }
            }
        }
    }
}

@Composable
fun NavigationBar(appState: AppState) {
    NavigationBar {
        NavigationItem(
            screen = Screen.Dashboard,
            icon = Icons.Default.Dashboard,
            label = "Dashboard",
            currentScreen = appState.currentScreen,
            onClick = {
                appState.navigationStack.clear()
                appState.currentScreen = Screen.Dashboard
                updateBrowserUrl(Screen.Dashboard)
            }
        )
        NavigationItem(
            screen = Screen.Portfolio,
            icon = Icons.Default.AccountBalance,
            label = "Portfolio",
            currentScreen = appState.currentScreen,
            onClick = {
                appState.navigationStack.clear()
                appState.currentScreen = Screen.Portfolio
                updateBrowserUrl(Screen.Portfolio)
            }
        )
        NavigationItem(
            screen = Screen.Goals,
            icon = Icons.Default.Flag,
            label = "Goals",
            currentScreen = appState.currentScreen,
            onClick = {
                appState.navigationStack.clear()
                appState.currentScreen = Screen.Goals
                updateBrowserUrl(Screen.Goals)
            }
        )
        NavigationItem(
            screen = Screen.Analysis,
            icon = Icons.Default.Analytics,
            label = "Analysis",
            currentScreen = appState.currentScreen,
            onClick = {
                appState.navigationStack.clear()
                appState.currentScreen = Screen.Analysis
                updateBrowserUrl(Screen.Analysis)
            }
        )
        NavigationItem(
            screen = Screen.Settings,
            icon = Icons.Default.Settings,
            label = "Settings",
            currentScreen = appState.currentScreen,
            onClick = {
                appState.navigationStack.clear()
                appState.currentScreen = Screen.Settings
                updateBrowserUrl(Screen.Settings)
            }
        )
    }
}

@Composable
fun RowScope.NavigationItem(
    screen: Screen,
    icon: ImageVector,
    label: String,
    currentScreen: Screen,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = currentScreen == screen,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) }
    )
}

fun getScreenTitle(screen: Screen): String {
    return when (screen) {
        Screen.Dashboard -> "Plan My Corpus"
        Screen.UserProfile -> "User Profile"
        Screen.InflationRates -> "Inflation Rates"
        Screen.InvestmentCategories -> "Investment Categories"
        Screen.Portfolio -> "Portfolio"
        Screen.Goals -> "Goals"
        Screen.Analysis -> "Analysis"
        Screen.Settings -> "Settings"
        Screen.About -> "About"
    }
}

fun shouldShowBackButton(screen: Screen): Boolean {
    return when (screen) {
        Screen.UserProfile,
        Screen.InflationRates,
        Screen.InvestmentCategories,
        Screen.About -> true
        else -> false
    }
}
