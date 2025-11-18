package migration

import kotlinx.serialization.json.*
import model.*
import getCurrentYear
import currentTimeMillis
import getCurrentDateISO

/**
 * Handles data migration between different versions of the financial plan format
 */
object DataMigration {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Import financial plan from JSON, automatically detecting and migrating versions
     * @param jsonString The JSON string to import
     * @return MultiYearFinancialPlan (v2.0 format)
     * @throws Exception if import fails
     */
    fun importFinancialPlan(jsonString: String): MultiYearFinancialPlan {
        val jsonElement = Json.parseToJsonElement(jsonString)

        // Check if it's the new v2.0 format
        if (jsonElement is JsonObject && jsonElement.containsKey("version")) {
            val version = jsonElement["version"]?.jsonPrimitive?.content
            if (version == "2.0") {
                // New multi-year format - decode directly
                return json.decodeFromString<MultiYearFinancialPlan>(jsonString)
            }
        }

        // Legacy v1.0 format - migrate
        val legacyData = json.decodeFromString<FinancialPlanData>(jsonString)
        return migrateLegacyToMultiYear(legacyData)
    }

    /**
     * Export financial plan to JSON in v2.0 format
     */
    fun exportFinancialPlan(data: MultiYearFinancialPlan): String {
        return Json { prettyPrint = true }.encodeToString(
            MultiYearFinancialPlan.serializer(),
            data
        )
    }

    /**
     * Migrate legacy v1.0 FinancialPlanData to v2.0 MultiYearFinancialPlan
     */
    fun migrateLegacyToMultiYear(legacy: FinancialPlanData): MultiYearFinancialPlan {
        val currentYear = getCurrentYear()
        val currentDate = getCurrentDateISO()

        return MultiYearFinancialPlan(
            version = "2.0",
            userProfile = legacy.userProfile,
            inflationCategories = legacy.inflationCategories,
            investmentCategories = legacy.investmentCategories,
            yearlySnapshots = listOf(
                YearlySnapshot(
                    year = currentYear,
                    snapshotDate = currentDate,
                    snapshotType = SnapshotType.CURRENT,
                    investments = legacy.investments,
                    futureLumpsumInvestments = legacy.futureLumpsumInvestments,
                    ongoingContributions = legacy.ongoingContributions,
                    goals = legacy.goals,
                    actualAnnualContributions = 0.0,
                    actualGoalSpending = 0.0,
                    notes = ""
                )
            ),
            currentYear = currentYear,
            baselineYear = currentYear,
            exportTimestamp = currentTimeMillis(),
            metadata = ExportMetadata(
                appVersion = "2.0.0",
                exportDate = currentDate,
                snapshotLabel = "Migrated from v1.0",
                userNotes = "Automatically migrated from single-snapshot format",
                totalYears = 1
            ),
            isSampleData = legacy.isSampleData
        )
    }

    /**
     * Convert MultiYearFinancialPlan back to FinancialPlanData for backward compatibility
     * Uses the current year's snapshot
     */
    fun multiYearToLegacy(multiYear: MultiYearFinancialPlan): FinancialPlanData {
        val currentSnapshot = multiYear.yearlySnapshots.find {
            it.year == multiYear.currentYear && it.snapshotType == SnapshotType.CURRENT
        } ?: multiYear.yearlySnapshots.firstOrNull()

        return if (currentSnapshot != null) {
            FinancialPlanData(
                userProfile = multiYear.userProfile,
                inflationCategories = multiYear.inflationCategories,
                investmentCategories = multiYear.investmentCategories,
                investments = currentSnapshot.investments,
                futureLumpsumInvestments = currentSnapshot.futureLumpsumInvestments,
                ongoingContributions = currentSnapshot.ongoingContributions,
                goals = currentSnapshot.goals,
                snapshotTimestamp = multiYear.exportTimestamp,
                isSampleData = multiYear.isSampleData
            )
        } else {
            // Fallback to empty data if no snapshot found
            FinancialPlanData(
                userProfile = multiYear.userProfile,
                inflationCategories = multiYear.inflationCategories,
                investmentCategories = multiYear.investmentCategories,
                isSampleData = multiYear.isSampleData
            )
        }
    }
}
