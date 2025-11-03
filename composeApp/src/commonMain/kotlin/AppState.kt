import androidx.compose.runtime.*
import model.*
import calculation.CorpusAnalyzer
import calculation.FinancialAnalysis
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import storage.PlatformStorage

/**
 * Main application state container
 */
class AppState {
    private var _data by mutableStateOf(FinancialPlanData())

    var data: FinancialPlanData
        get() = _data
        private set(value) {
            _data = value
            // Auto-save to localStorage on every change
            autoSave()
        }

    var currentScreen by mutableStateOf(getInitialScreen())
        internal set

    internal var navigationStack = mutableListOf<Screen>()

    init {
        // Load data from localStorage on init
        loadFromStorage()
    }

    private fun getInitialScreen(): Screen {
        val path = getBrowserPath()
        return when (path) {
            "/" -> Screen.Dashboard
            "/profile" -> Screen.UserProfile
            "/inflation-rates" -> Screen.InflationRates
            "/investment-categories" -> Screen.InvestmentCategories
            "/portfolio" -> Screen.Portfolio
            "/contributions" -> Screen.Contributions
            "/goals" -> Screen.Goals
            "/analysis" -> Screen.Analysis
            "/settings" -> Screen.Settings
            "/about" -> Screen.About
            else -> Screen.Dashboard
        }
    }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            navigationStack.add(currentScreen)
            currentScreen = screen
            updateBrowserUrl(screen)
        }
    }

    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            val previousScreen = navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = previousScreen
            updateBrowserUrl(previousScreen)
        } else {
            currentScreen = Screen.Settings
            updateBrowserUrl(Screen.Settings)
        }
    }

    // Computed analysis - recalculated whenever data changes
    val analysis: FinancialAnalysis
        @Composable
        get() {
            val currentYear = remember { getCurrentYear() }
            return remember(data) {
                CorpusAnalyzer.analyze(data, currentYear)
            }
        }

    // Update functions
    fun updateUserProfile(profile: UserProfile) {
        data = data.copy(userProfile = profile)
    }

    fun updateInflationCategories(categories: List<InflationCategory>) {
        data = data.copy(inflationCategories = categories)
    }

    fun addInflationCategory(category: InflationCategory) {
        data = data.copy(inflationCategories = data.inflationCategories + category)
    }

    fun updateInflationCategory(id: String, updated: InflationCategory) {
        data = data.copy(
            inflationCategories = data.inflationCategories.map { if (it.id == id) updated else it }
        )
    }

    fun removeInflationCategory(id: String) {
        data = data.copy(inflationCategories = data.inflationCategories.filter { it.id != id })
    }

    fun updateInvestmentCategories(categories: List<InvestmentCategory>) {
        data = data.copy(investmentCategories = categories)
    }

    fun addInvestmentCategory(category: InvestmentCategory) {
        data = data.copy(investmentCategories = data.investmentCategories + category)
    }

    fun updateInvestmentCategory(id: String, updated: InvestmentCategory) {
        data = data.copy(
            investmentCategories = data.investmentCategories.map { if (it.id == id) updated else it }
        )
    }

    fun removeInvestmentCategory(id: String) {
        data = data.copy(investmentCategories = data.investmentCategories.filter { it.id != id })
    }

    fun addInvestment(investment: Investment) {
        data = data.copy(investments = data.investments + investment)
    }

    fun updateInvestment(id: String, updated: Investment) {
        data = data.copy(
            investments = data.investments.map { if (it.id == id) updated else it }
        )
    }

    fun removeInvestment(id: String) {
        data = data.copy(investments = data.investments.filter { it.id != id })
    }

    fun addContribution(contribution: OngoingContribution) {
        data = data.copy(ongoingContributions = data.ongoingContributions + contribution)
    }

    fun updateContribution(id: String, updated: OngoingContribution) {
        data = data.copy(
            ongoingContributions = data.ongoingContributions.map { if (it.id == id) updated else it }
        )
    }

    fun removeContribution(id: String) {
        data = data.copy(ongoingContributions = data.ongoingContributions.filter { it.id != id })
    }

    fun addGoal(goal: FinancialGoal) {
        data = data.copy(goals = data.goals + goal)
    }

    fun updateGoal(id: String, updated: FinancialGoal) {
        data = data.copy(
            goals = data.goals.map { if (it.id == id) updated else it }
        )
    }

    fun removeGoal(id: String) {
        data = data.copy(goals = data.goals.filter { it.id != id })
    }

    fun toggleGoal(id: String) {
        data = data.copy(
            goals = data.goals.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
        )
    }

    fun toggleInvestment(id: String) {
        data = data.copy(
            investments = data.investments.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
        )
    }

    fun toggleContribution(id: String) {
        data = data.copy(
            ongoingContributions = data.ongoingContributions.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
        )
    }

    // JSON export/import
    fun exportToJson(): String {
        val snapshot = data.copy(
            snapshotTimestamp = currentTimeMillis()
        )
        return Json.encodeToString(snapshot)
    }

    fun importFromJson(json: String) {
        try {
            data = Json.decodeFromString<FinancialPlanData>(json)
        } catch (e: Exception) {
            println("Error importing JSON: ${e.message}")
        }
    }

    fun loadData(planData: FinancialPlanData) {
        data = planData
    }

    // Storage operations
    private fun autoSave() {
        try {
            val json = Json.encodeToString(data)
            PlatformStorage.saveToLocalStorage(json)
        } catch (e: Exception) {
            println("Error auto-saving: ${e.message}")
        }
    }

    private fun loadFromStorage() {
        try {
            val json = PlatformStorage.loadFromLocalStorage()
            if (json != null) {
                _data = Json { ignoreUnknownKeys = true }.decodeFromString<FinancialPlanData>(json)
            }
        } catch (e: Exception) {
            println("Error loading from storage: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            // Clear corrupted data and start fresh
            PlatformStorage.clearLocalStorage()
        }
    }

    fun downloadSnapshot() {
        try {
            val json = exportToJson()
            val filename = "financial_plan_${formatDateForFilename()}.json"
            PlatformStorage.downloadJson(json, filename)
        } catch (e: Exception) {
            println("Error downloading snapshot: ${e.message}")
        }
    }

    fun uploadSnapshot(onComplete: () -> Unit = {}) {
        PlatformStorage.uploadJson { json ->
            importFromJson(json)
            onComplete()
        }
    }
}

/**
 * Navigation screens
 */
enum class Screen {
    Dashboard,
    UserProfile,
    InflationRates,
    InvestmentCategories,
    Portfolio,
    Contributions,
    Goals,
    Analysis,
    Settings,
    About
}

// Platform-specific functions
expect fun getCurrentYear(): Int
expect fun currentTimeMillis(): Long
expect fun randomUUID(): String
expect fun updateBrowserUrl(screen: Screen)
expect fun getBrowserPath(): String
expect fun formatDateForFilename(): String
