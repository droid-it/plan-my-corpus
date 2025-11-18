import androidx.compose.runtime.*
import model.*
import calculation.CorpusAnalyzer
import calculation.FinancialAnalysis
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import storage.PlatformStorage
import migration.DataMigration

/**
 * Main application state container
 * Version 2.0: Now supports multi-year tracking
 */
class AppState {
    // Internal multi-year data structure
    private var _multiYearData by mutableStateOf<MultiYearFinancialPlan?>(null)

    // Selected year for viewing/editing
    var selectedYear by mutableStateOf(getCurrentYear())
        private set

    // Backward-compatible data property - exposes current year's snapshot as FinancialPlanData
    var data: FinancialPlanData
        get() = DataMigration.multiYearToLegacy(
            _multiYearData ?: createEmptyMultiYearPlan()
        )
        private set(value) {
            // Convert single snapshot to multi-year format
            _multiYearData = DataMigration.migrateLegacyToMultiYear(value)
            // Auto-save to localStorage on every change
            autoSave()
        }

    // Multi-year data accessor (for future year-aware features)
    val multiYearData: MultiYearFinancialPlan
        get() = _multiYearData ?: createEmptyMultiYearPlan()

    private fun createEmptyMultiYearPlan(): MultiYearFinancialPlan {
        val year = getCurrentYear()
        val date = getCurrentDateISO()
        return MultiYearFinancialPlan(
            version = "2.0",
            userProfile = UserProfile(),
            inflationCategories = DefaultInflationCategories.defaults(),
            investmentCategories = DefaultInvestmentCategories.defaults(),
            yearlySnapshots = listOf(
                YearlySnapshot(
                    year = year,
                    snapshotDate = date,
                    snapshotType = SnapshotType.CURRENT,
                    investments = emptyList(),
                    futureLumpsumInvestments = emptyList(),
                    ongoingContributions = emptyList(),
                    goals = emptyList()
                )
            ),
            currentYear = year,
            baselineYear = year,
            exportTimestamp = currentTimeMillis(),
            metadata = ExportMetadata()
        )
    }

    var currentScreen by mutableStateOf(getInitialScreen())
        internal set

    internal var navigationStack = mutableListOf<Screen>()

    // UI preferences (stored separately from financial data)
    var dataStorageBannerDismissed by mutableStateOf(false)
        private set
    var welcomeCardDismissed by mutableStateOf(false)
        private set
    var quickStartCardDismissed by mutableStateOf(false)
        private set
    var sampleDataGoalsCardDismissed by mutableStateOf(false)
        private set
    var sampleDataPortfolioCardDismissed by mutableStateOf(false)
        private set
    var sampleDataPlanningCardDismissed by mutableStateOf(false)
        private set

    // Snackbar/Toast message state
    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    // Dialog trigger flags for navigation from quick start guide
    var shouldOpenAddGoalDialog by mutableStateOf(false)
    var shouldOpenAddInvestmentDialog by mutableStateOf(false)

    init {
        // Load data from localStorage on init
        loadFromStorage()
        loadUIPreferences()
    }

    private fun getInitialScreen(): Screen {
        val path = getBrowserPath()
        return when (path) {
            "/" -> Screen.Dashboard
            "/profile" -> Screen.UserProfile
            "/inflation-rates" -> Screen.InflationRates
            "/investment-categories" -> Screen.InvestmentCategories
            "/portfolio" -> Screen.Portfolio
            "/contributions" -> Screen.Portfolio // Redirect old contributions URL to Portfolio
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

    // Navigation with dialog trigger
    fun navigateToAddGoal() {
        shouldOpenAddGoalDialog = true
        navigateTo(Screen.Goals)
    }

    fun navigateToAddInvestment() {
        shouldOpenAddInvestmentDialog = true
        navigateTo(Screen.Portfolio)
    }

    fun clearDialogTriggers() {
        shouldOpenAddGoalDialog = false
        shouldOpenAddInvestmentDialog = false
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

    fun addFutureLumpsumInvestment(investment: FutureLumpsumInvestment) {
        data = data.copy(futureLumpsumInvestments = data.futureLumpsumInvestments + investment)
    }

    fun updateFutureLumpsumInvestment(id: String, updated: FutureLumpsumInvestment) {
        data = data.copy(
            futureLumpsumInvestments = data.futureLumpsumInvestments.map { if (it.id == id) updated else it }
        )
    }

    fun removeFutureLumpsumInvestment(id: String) {
        data = data.copy(futureLumpsumInvestments = data.futureLumpsumInvestments.filter { it.id != id })
    }

    fun toggleFutureLumpsumInvestment(id: String) {
        data = data.copy(
            futureLumpsumInvestments = data.futureLumpsumInvestments.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
        )
    }

    // JSON export/import (v2.0 format with backward compatibility)
    fun exportToJson(): String {
        return try {
            val multiYear = _multiYearData ?: createEmptyMultiYearPlan()
            val updated = multiYear.copy(
                exportTimestamp = currentTimeMillis(),
                metadata = multiYear.metadata.copy(
                    exportDate = getCurrentDateISO(),
                    totalYears = multiYear.yearlySnapshots.size
                )
            )
            DataMigration.exportFinancialPlan(updated)
        } catch (e: Exception) {
            println("Error exporting to JSON: ${e.message}")
            // Fallback to legacy export
            Json.encodeToString(data)
        }
    }

    fun importFromJson(json: String): Boolean {
        return try {
            // Use DataMigration to automatically handle v1.0 and v2.0 formats
            val imported = DataMigration.importFinancialPlan(json)
            _multiYearData = imported
            selectedYear = imported.currentYear
            true
        } catch (e: Exception) {
            println("Error importing JSON: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            false
        }
    }

    fun loadData(planData: FinancialPlanData) {
        data = planData
    }

    fun loadMultiYearData(multiYear: MultiYearFinancialPlan) {
        _multiYearData = multiYear
        selectedYear = multiYear.currentYear
        autoSave()
    }

    // Storage operations (v2.0 format with backward compatibility)
    private fun autoSave() {
        try {
            val json = exportToJson()
            PlatformStorage.saveToLocalStorage(json)
        } catch (e: Exception) {
            println("Error auto-saving: ${e.message}")
        }
    }

    private fun loadFromStorage() {
        try {
            val json = PlatformStorage.loadFromLocalStorage()
            if (json != null) {
                // Import using migration logic to handle both v1.0 and v2.0
                val imported = DataMigration.importFinancialPlan(json)
                _multiYearData = imported
                selectedYear = imported.currentYear
            } else {
                // First-time user: load sample data
                val sampleData = model.SampleData.createSampleFinancialData()
                _multiYearData = DataMigration.migrateLegacyToMultiYear(sampleData)
                selectedYear = _multiYearData?.currentYear ?: getCurrentYear()
                // Auto-save the sample data
                autoSave()
            }
        } catch (e: Exception) {
            println("Error loading from storage: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            // Clear corrupted data and load sample data
            PlatformStorage.clearLocalStorage()
            val sampleData = model.SampleData.createSampleFinancialData()
            _multiYearData = DataMigration.migrateLegacyToMultiYear(sampleData)
            selectedYear = _multiYearData?.currentYear ?: getCurrentYear()
            autoSave()
        }
    }

    fun downloadSnapshot() {
        try {
            val json = exportToJson()
            val filename = "plan_my_corpus_${formatDateForFilename()}.json"
            PlatformStorage.downloadJson(json, filename)
            showSnackbar("Data exported successfully!")
        } catch (e: Exception) {
            println("Error downloading snapshot: ${e.message}")
            showSnackbar("Error: Failed to export data.")
        }
    }

    fun uploadSnapshot(onComplete: () -> Unit = {}) {
        PlatformStorage.uploadJson { json ->
            val success = importFromJson(json)
            if (success) {
                showSnackbar("Data imported successfully!")
            } else {
                showSnackbar("Error: Failed to import data. Please check the file format.")
            }
            onComplete()
        }
    }

    // Snackbar management
    fun showSnackbar(message: String) {
        snackbarMessage = message
    }

    fun clearSnackbar() {
        snackbarMessage = null
    }

    // Clear all data and reset to defaults
    fun clearAllData() {
        try {
            // Clear financial data from localStorage
            PlatformStorage.clearLocalStorage()
            // Reset to default empty data
            _multiYearData = createEmptyMultiYearPlan()
            selectedYear = getCurrentYear()
            autoSave()
            // Keep UI preferences (like banner dismissal) but could clear if needed
            showSnackbar("All data cleared successfully")
        } catch (e: Exception) {
            println("Error clearing data: ${e.message}")
            showSnackbar("Error: Failed to clear data")
        }
    }

    // Clear sample data label (user acknowledges data is theirs now)
    fun clearSampleDataLabel() {
        _multiYearData = _multiYearData?.copy(isSampleData = false) ?: createEmptyMultiYearPlan()
        autoSave()
        showSnackbar("Sample data label removed")
    }

    // Year selection (for future multi-year features)
    fun selectYear(year: Int) {
        val snapshot = multiYearData.yearlySnapshots.find { it.year == year }
        if (snapshot != null) {
            selectedYear = year
            showSnackbar("Viewing data for year $year")
        } else {
            showSnackbar("No data available for year $year")
        }
    }

    // Add a new yearly snapshot
    fun addYearlySnapshot(snapshot: YearlySnapshot) {
        _multiYearData = _multiYearData?.copy(
            yearlySnapshots = (multiYearData.yearlySnapshots + snapshot).sortedBy { it.year }
        )
        autoSave()
    }

    // UI Preferences management
    fun dismissDataStorageBanner() {
        dataStorageBannerDismissed = true
        saveUIPreferences()
    }

    fun dismissWelcomeCard() {
        welcomeCardDismissed = true
        saveUIPreferences()
    }

    fun dismissQuickStartCard() {
        quickStartCardDismissed = true
        saveUIPreferences()
    }

    fun dismissSampleDataGoalsCard() {
        sampleDataGoalsCardDismissed = true
        saveUIPreferences()
    }

    fun dismissSampleDataPortfolioCard() {
        sampleDataPortfolioCardDismissed = true
        saveUIPreferences()
    }

    fun dismissSampleDataPlanningCard() {
        sampleDataPlanningCardDismissed = true
        saveUIPreferences()
    }

    private fun saveUIPreferences() {
        try {
            PlatformStorage.saveUIPreference("dataStorageBannerDismissed", dataStorageBannerDismissed.toString())
            PlatformStorage.saveUIPreference("welcomeCardDismissed", welcomeCardDismissed.toString())
            PlatformStorage.saveUIPreference("quickStartCardDismissed", quickStartCardDismissed.toString())
            PlatformStorage.saveUIPreference("sampleDataGoalsCardDismissed", sampleDataGoalsCardDismissed.toString())
            PlatformStorage.saveUIPreference("sampleDataPortfolioCardDismissed", sampleDataPortfolioCardDismissed.toString())
            PlatformStorage.saveUIPreference("sampleDataPlanningCardDismissed", sampleDataPlanningCardDismissed.toString())
        } catch (e: Exception) {
            println("Error saving UI preferences: ${e.message}")
        }
    }

    private fun loadUIPreferences() {
        try {
            dataStorageBannerDismissed = PlatformStorage.loadUIPreference("dataStorageBannerDismissed") == "true"
            welcomeCardDismissed = PlatformStorage.loadUIPreference("welcomeCardDismissed") == "true"
            quickStartCardDismissed = PlatformStorage.loadUIPreference("quickStartCardDismissed") == "true"
            sampleDataGoalsCardDismissed = PlatformStorage.loadUIPreference("sampleDataGoalsCardDismissed") == "true"
            sampleDataPortfolioCardDismissed = PlatformStorage.loadUIPreference("sampleDataPortfolioCardDismissed") == "true"
            sampleDataPlanningCardDismissed = PlatformStorage.loadUIPreference("sampleDataPlanningCardDismissed") == "true"
        } catch (e: Exception) {
            println("Error loading UI preferences: ${e.message}")
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
expect fun getCurrentDateISO(): String
expect fun hideLoader()
