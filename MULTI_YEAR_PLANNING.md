# Multi-Year Analysis & Comparison - Planning Document

## Overview

Transform Plan My Corpus from a single-snapshot tool to a multi-year financial tracking system that:
- Tracks actual financial data year by year
- Compares projected vs actual performance
- Identifies under/over-performing investments
- Provides actionable insights from historical trends
- Maintains backward compatibility with existing exports

## Problem Statement

**Current Limitation**: The app currently operates on a single snapshot model. Users can export yearly snapshots but cannot:
- Track actual corpus growth over time
- Compare what they projected vs what actually happened
- Identify which investments are performing better/worse than expected
- See multi-year trends in their financial health

**Desired State**: A multi-year tracking system where:
- Users can record actual financial data for different years
- The system automatically compares projections vs actuals
- Investment performance is evaluated against expectations
- Historical trends inform future planning decisions

---

## Core Concept: Year-Based Data Entry

### The "Year Context" Model

**Key Insight**: Financial data exists in two forms:
1. **Projected Data**: Future expectations (XIRR assumptions, planned contributions, estimated goals)
2. **Actual Data**: Historical reality (what investments were actually worth, actual contributions made, actual goal spending)

**Solution**: Introduce a "year context" that allows users to:
- Switch between different years (e.g., 2023, 2024, 2025)
- Enter actual data for past years
- View projections for future years
- Record what actually happened vs what was planned

### Year Types

```
Year 2023 (HISTORICAL)
├── Investments: Actual values as of Dec 31, 2023
├── Contributions: What was actually contributed
├── Goals: What was actually spent
└── Status: LOCKED (historical record)

Year 2024 (HISTORICAL)
├── Investments: Actual values as of Dec 31, 2024
├── Contributions: What was actually contributed
├── Goals: What was actually spent
└── Status: LOCKED (historical record)

Year 2025 (CURRENT)
├── Investments: Current live values
├── Contributions: Ongoing/planned
├── Goals: Planned spending
└── Status: ACTIVE (editable, used for projections)
```

---

## Data Model Changes

### 1. New Core Entities

#### YearlySnapshot
```kotlin
@Serializable
data class YearlySnapshot(
    val year: Int,                              // e.g., 2024
    val snapshotDate: String,                   // "2024-12-31" (ISO format)
    val snapshotType: SnapshotType,             // HISTORICAL, CURRENT
    val investments: List<Investment>,
    val ongoingContributions: List<OngoingContribution>,
    val goals: List<FinancialGoal>,
    val actualAnnualContributions: Double = 0.0, // Total actually contributed this year
    val actualGoalSpending: Double = 0.0,       // Total actually spent on goals this year
    val notes: String = ""                      // User notes for this year
)

enum class SnapshotType {
    HISTORICAL,  // Past year, locked
    CURRENT      // Active year, used for projections
}
```

#### MultiYearFinancialPlan
```kotlin
@Serializable
data class MultiYearFinancialPlan(
    val version: String = "2.0",                // Format version
    val userProfile: UserProfile,               // Shared across all years
    val inflationCategories: List<InflationCategory>, // Shared
    val investmentCategories: List<InvestmentCategory>, // Shared
    val yearlySnapshots: List<YearlySnapshot>,  // Time-series data
    val currentYear: Int,                       // Which year is "active"
    val baselineYear: Int,                      // First year of tracking
    val exportTimestamp: Long = 0,
    val metadata: ExportMetadata = ExportMetadata()
)

@Serializable
data class ExportMetadata(
    val appVersion: String = "2.0.0",
    val exportDate: String = "",
    val snapshotLabel: String = "",
    val userNotes: String = "",
    val totalYears: Int = 0
)
```

### 2. Migration Strategy

#### Import Logic
```kotlin
// Detect format version on import
fun importFinancialPlan(jsonString: String): MultiYearFinancialPlan {
    val json = Json.parseToJsonElement(jsonString)

    if (json.jsonObject.containsKey("version")) {
        val version = json.jsonObject["version"]?.jsonPrimitive?.content
        if (version == "2.0") {
            // New multi-year format
            return Json.decodeFromString<MultiYearFinancialPlan>(jsonString)
        }
    }

    // Legacy format (v1.0) - migrate
    val legacyData = Json.decodeFromString<FinancialPlanData>(jsonString)
    return migrateLegacyToMultiYear(legacyData)
}

fun migrateLegacyToMultiYear(legacy: FinancialPlanData): MultiYearFinancialPlan {
    val currentYear = getCurrentYear() // 2025

    return MultiYearFinancialPlan(
        version = "2.0",
        userProfile = legacy.userProfile,
        inflationCategories = legacy.inflationCategories,
        investmentCategories = legacy.investmentCategories,
        yearlySnapshots = listOf(
            YearlySnapshot(
                year = currentYear,
                snapshotDate = getCurrentDateISO(),
                snapshotType = SnapshotType.CURRENT,
                investments = legacy.investments,
                ongoingContributions = legacy.ongoingContributions,
                goals = legacy.goals
            )
        ),
        currentYear = currentYear,
        baselineYear = currentYear,
        exportTimestamp = System.currentTimeMillis(),
        metadata = ExportMetadata(
            exportDate = getCurrentDateISO(),
            snapshotLabel = "Migrated from v1.0",
            totalYears = 1
        )
    )
}
```

#### Export Logic
```kotlin
// Always export in v2.0 format
fun exportFinancialPlan(data: MultiYearFinancialPlan): String {
    return Json.encodeToString(data)
}
```

---

## UI Changes

### 1. Year Selector Component

**Location**: Persistent header/toolbar across all data entry screens

```
┌─────────────────────────────────────────────┐
│  Plan My Corpus                             │
│                                             │
│  Year: [2023▼] [2024▼] [2025▼] (current)   │
│         ↑ HISTORICAL    ↑ ACTIVE            │
│                                             │
│  [🔒 Historical years are read-only]        │
└─────────────────────────────────────────────┘
```

**Behavior**:
- Dropdown shows all tracked years
- Current year marked with badge
- Historical years show lock icon
- Switching year changes ALL data entry screens
- Analysis screen shows comparison when multiple years exist

### 2. Screen-by-Screen Changes

#### Portfolio Screen
```
When year = CURRENT (2025):
┌─────────────────────────────────────────┐
│  Investment: PPF                        │
│  Current Value: ₹5,00,000              │
│  Category: Debt                         │
│  Expected XIRR: 7.1%                    │
│  [Edit] [Delete] [Toggle]               │
└─────────────────────────────────────────┘

When year = HISTORICAL (2024):
┌─────────────────────────────────────────┐
│  Investment: PPF                        │
│  Value on Dec 31, 2024: ₹4,50,000      │
│  Category: Debt                         │
│  Expected XIRR: 7.1%                    │
│  📊 Actual Growth: +₹50,000 (+12.5%)   │
│  [Read-only - Historical Record]        │
└─────────────────────────────────────────┘
```

#### Contributions Screen
```
When year = CURRENT:
- Show ongoing contributions (as usual)
- Allow editing

When year = HISTORICAL:
- Show contributions active during that year
- Display: "Actually contributed: ₹X in 2024"
- Compare vs planned: "Planned: ₹1,20,000 | Actual: ₹1,15,000 (-4.2%)"
```

#### Goals Screen
```
When year = CURRENT:
- Show all future goals
- Allow editing

When year = HISTORICAL:
- Show goals that matured in that year
- Display: "Target: ₹5,00,000 | Actually spent: ₹4,80,000"
- Show reason if less: "Postponed" / "Partially fulfilled" / "Cost less than expected"
```

### 3. Analysis Screen - Multi-Year View

#### New Tab: "Performance Analysis"

```
┌─────────────────────────────────────────────────────────┐
│  CORPUS GROWTH: PROJECTED VS ACTUAL                     │
│                                                         │
│  📊 Chart: Dual-line graph                             │
│      - Blue line: Projected (from 2023 baseline)       │
│      - Green line: Actual (from snapshots)             │
│      - Gray area: Projection from current to future    │
│                                                         │
│  Current Corpus (2025): ₹45,00,000                     │
│  Projected Corpus (2025 from 2023): ₹42,00,000         │
│  Variance: +₹3,00,000 (+7.1%) ✓ AHEAD OF PLAN         │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  INVESTMENT PERFORMANCE                                 │
│                                                         │
│  🟢 OUTPERFORMERS (Actual > Expected)                  │
│  ┌──────────────────────────────────────────────┐      │
│  │ Equity Mutual Fund                          │      │
│  │ Expected XIRR: 12% | Actual XIRR: 15.3%    │      │
│  │ Outperformance: +3.3% (+₹45,000 extra)     │      │
│  └──────────────────────────────────────────────┘      │
│                                                         │
│  🔴 UNDERPERFORMERS (Actual < Expected)                │
│  ┌──────────────────────────────────────────────┐      │
│  │ Real Estate Fund                            │      │
│  │ Expected XIRR: 10% | Actual XIRR: 6.2%     │      │
│  │ Underperformance: -3.8% (-₹28,000)         │      │
│  └──────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  CONTRIBUTION DISCIPLINE                                │
│                                                         │
│  Year  | Planned      | Actual       | Variance        │
│  ───────────────────────────────────────────────────    │
│  2023  | ₹1,20,000   | ₹1,20,000    | 0% ✓           │
│  2024  | ₹1,32,000   | ₹1,15,000    | -12.9% ⚠       │
│  2025  | ₹1,45,000   | (in progress) | -             │
│                                                         │
│  💡 Insight: You're ₹17,000 behind on contributions    │
│              Consider catching up to stay on track     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  GOAL ACHIEVEMENT ACCURACY                              │
│                                                         │
│  Child's School Fees (2024)                            │
│  Projected cost: ₹3,50,000                             │
│  Actual cost: ₹3,20,000 (-8.6% than expected)         │
│                                                         │
│  💡 Education inflation lower than assumed (6% vs 8%)  │
│     Consider revising future education goals           │
└─────────────────────────────────────────────────────────┘
```

---

## Calculation Changes

### 1. Projected vs Actual Corpus Growth

```kotlin
data class CorpusGrowthAnalysis(
    val year: Int,
    val projectedCorpus: Double,   // What we thought it would be
    val actualCorpus: Double,       // What it actually is
    val variance: Double,           // Difference
    val variancePercentage: Double, // % difference
    val contributors: VarianceBreakdown
)

data class VarianceBreakdown(
    val marketPerformance: Double,     // From investments growing differently
    val contributionDiscipline: Double, // From over/under contributing
    val goalTiming: Double,            // From goals costing more/less
    val other: Double
)

fun calculateProjectedVsActual(
    baselineSnapshot: YearlySnapshot,
    targetYear: Int,
    actualSnapshot: YearlySnapshot?,
    categories: List<InvestmentCategory>
): CorpusGrowthAnalysis {
    // Calculate what we projected from baseline
    val projected = projectCorpusFrom(baselineSnapshot, targetYear, categories)

    // Get actual if available
    val actual = actualSnapshot?.let { calculateActualCorpus(it) } ?: 0.0

    // Analyze variance
    val breakdown = analyzeVariance(baselineSnapshot, actualSnapshot, projected, actual)

    return CorpusGrowthAnalysis(
        year = targetYear,
        projectedCorpus = projected,
        actualCorpus = actual,
        variance = actual - projected,
        variancePercentage = ((actual - projected) / projected) * 100,
        contributors = breakdown
    )
}
```

### 2. Investment Performance Analysis

```kotlin
data class InvestmentPerformanceAnalysis(
    val investment: Investment,
    val category: InvestmentCategory,
    val expectedXIRR: Double,
    val actualXIRR: Double,
    val performanceVariance: Double,
    val monetaryImpact: Double,  // How much extra/less money this generated
    val status: PerformanceStatus
)

enum class PerformanceStatus {
    OUTPERFORMING,  // Actual > Expected
    ON_TRACK,       // Within ±1% of expected
    UNDERPERFORMING // Actual < Expected
}

fun analyzeInvestmentPerformance(
    baselineSnapshot: YearlySnapshot,
    currentSnapshot: YearlySnapshot,
    categories: List<InvestmentCategory>
): List<InvestmentPerformanceAnalysis> {
    return baselineSnapshot.investments.mapNotNull { baseInvestment ->
        val currentInvestment = currentSnapshot.investments.find { it.id == baseInvestment.id }
        currentInvestment?.let {
            val category = categories.find { cat -> cat.id == it.categoryId }!!
            val yearsElapsed = currentSnapshot.year - baselineSnapshot.year

            // Calculate expected value
            val expectedValue = baseInvestment.currentValue *
                Math.pow(1 + category.preRetirementXIRR / 100, yearsElapsed.toDouble())

            // Calculate actual XIRR
            val actualXIRR = calculateActualXIRR(
                baseValue = baseInvestment.currentValue,
                currentValue = it.currentValue,
                years = yearsElapsed
            )

            // Calculate monetary impact
            val monetaryImpact = it.currentValue - expectedValue

            InvestmentPerformanceAnalysis(
                investment = it,
                category = category,
                expectedXIRR = category.preRetirementXIRR,
                actualXIRR = actualXIRR,
                performanceVariance = actualXIRR - category.preRetirementXIRR,
                monetaryImpact = monetaryImpact,
                status = when {
                    actualXIRR > category.preRetirementXIRR + 1 -> PerformanceStatus.OUTPERFORMING
                    actualXIRR < category.preRetirementXIRR - 1 -> PerformanceStatus.UNDERPERFORMING
                    else -> PerformanceStatus.ON_TRACK
                }
            )
        }
    }
}

fun calculateActualXIRR(baseValue: Double, currentValue: Double, years: Int): Double {
    // XIRR formula: (Current/Base)^(1/years) - 1
    return (Math.pow(currentValue / baseValue, 1.0 / years) - 1) * 100
}
```

### 3. Contribution Discipline Tracking

```kotlin
data class ContributionDisciplineAnalysis(
    val year: Int,
    val plannedContributions: Double,
    val actualContributions: Double,
    val variance: Double,
    val variancePercentage: Double,
    val status: DisciplineStatus
)

enum class DisciplineStatus {
    EXCEEDED,   // Contributed more than planned
    ON_TRACK,   // Within ±5% of plan
    BEHIND      // Contributed less than planned
}

fun analyzeContributionDiscipline(
    yearlySnapshots: List<YearlySnapshot>
): List<ContributionDisciplineAnalysis> {
    return yearlySnapshots.filter { it.snapshotType == SnapshotType.HISTORICAL }.map { snapshot ->
        val planned = calculatePlannedContributions(snapshot)
        val actual = snapshot.actualAnnualContributions
        val variance = actual - planned
        val variancePercentage = (variance / planned) * 100

        ContributionDisciplineAnalysis(
            year = snapshot.year,
            plannedContributions = planned,
            actualContributions = actual,
            variance = variance,
            variancePercentage = variancePercentage,
            status = when {
                variancePercentage > 5 -> DisciplineStatus.EXCEEDED
                variancePercentage < -5 -> DisciplineStatus.BEHIND
                else -> DisciplineStatus.ON_TRACK
            }
        )
    }
}
```

---

## Additional Multi-Year Analysis Features

### 1. Inflation Accuracy Tracking

**Concept**: Compare assumed inflation rates vs actual cost increases

```kotlin
data class InflationAccuracyAnalysis(
    val category: InflationCategory,
    val assumedRate: Double,
    val actualRate: Double,      // Derived from goal costs
    val variance: Double,
    val impactOnFutureGoals: Double
)

// Example insight:
// "Education inflation was 6.2% vs assumed 8%"
// "Your education goals may cost ₹50,000 less than projected"
```

### 2. Net Worth Trajectory

**Concept**: Track total net worth over time

```kotlin
data class NetWorthAnalysis(
    val year: Int,
    val totalInvestments: Double,
    val yearOverYearGrowth: Double,
    val growthPercentage: Double,
    val milestones: List<NetWorthMilestone>
)

data class NetWorthMilestone(
    val amount: Double,
    val achievedYear: Int?,
    val projectedYear: Int?,
    val status: MilestoneStatus
)

// Example milestones:
// - ₹10 Lakhs: Achieved 2023 (1 year early!)
// - ₹50 Lakhs: Projected 2028
// - ₹1 Crore: Projected 2032
```

### 3. Goal Cost Prediction Improvement

**Concept**: Use historical goal spending to improve future estimates

```kotlin
data class GoalCostPrediction(
    val goalCategory: String,  // "Education", "Travel", "Home", etc.
    val historicalGoals: List<GoalActual>,
    val averageInflation: Double,
    val recommendedInflationRate: Double,
    val confidenceLevel: String  // "High" if 3+ data points
)

data class GoalActual(
    val year: Int,
    val projected: Double,
    val actual: Double,
    val variance: Double
)

// Example insight:
// "Based on 3 education goals, actual costs averaged 7.2% below projections"
// "Consider using 6% inflation for education instead of 8%"
```

### 4. Retirement Readiness Trend

**Concept**: Track how retirement preparedness changes over time

```kotlin
data class RetirementReadinessTrend(
    val year: Int,
    val projectedRetirementCorpus: Double,
    val requiredRetirementCorpus: Double,
    val readinessPercentage: Double,  // Projected/Required * 100
    val yearsToRetirement: Int,
    val onTrack: Boolean
)

// Chart showing readiness trend:
// 2023: 45% ready (15 years to go)
// 2024: 52% ready (14 years to go) ↑ +7%
// 2025: 58% ready (13 years to go) ↑ +6%
// Trend: Improving! On track for 100% by retirement
```

### 5. Contribution Efficiency Score

**Concept**: Measure how efficiently contributions are converting to corpus

```kotlin
data class ContributionEfficiency(
    val year: Int,
    val totalContributed: Double,
    val corpusGrowth: Double,
    val efficiencyRatio: Double,  // Growth / Contributed
    val benchmark: Double,         // Expected ratio based on XIRR
    val performance: EfficiencyStatus
)

enum class EfficiencyStatus {
    EXCELLENT,  // Ratio > 1.5 (market gains boosting)
    GOOD,       // Ratio 1.0 - 1.5
    AVERAGE,    // Ratio 0.8 - 1.0
    POOR        // Ratio < 0.8 (market losses)
}

// Example insight:
// "In 2024, you contributed ₹1,15,000 and corpus grew by ₹2,45,000"
// "Efficiency: 2.13x (Excellent!) - Market gains amplified your contributions"
```

---

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-2)
- [ ] Create new data models (MultiYearFinancialPlan, YearlySnapshot)
- [ ] Implement import/export with backward compatibility
- [ ] Update AppState to handle multi-year data
- [ ] Add year selector UI component
- [ ] Test migration from v1.0 to v2.0

### Phase 2: Data Entry (Weeks 3-4)
- [ ] Update Portfolio screen for year-based entry
- [ ] Update Contributions screen for year-based entry
- [ ] Update Goals screen for year-based entry
- [ ] Add "actual values" fields for historical years
- [ ] Implement read-only mode for historical snapshots

### Phase 3: Basic Analysis (Weeks 5-6)
- [ ] Implement projected vs actual corpus calculation
- [ ] Create dual-line comparison chart
- [ ] Add variance calculations
- [ ] Build investment performance analyzer
- [ ] Show outperformers/underperformers

### Phase 4: Advanced Analysis (Weeks 7-8)
- [ ] Contribution discipline tracking
- [ ] Goal cost accuracy analysis
- [ ] Net worth trajectory
- [ ] Inflation accuracy tracking
- [ ] Retirement readiness trend

### Phase 5: Insights & Polish (Weeks 9-10)
- [ ] Contribution efficiency score
- [ ] Automated insights generation
- [ ] Goal cost prediction improvements
- [ ] UI polish and refinements
- [ ] Comprehensive testing

---

## Key Design Decisions

### 1. Year as Context, Not Version

**Decision**: Years are not separate "versions" but rather timestamped snapshots of the same plan.

**Rationale**:
- User profile, inflation categories, and investment categories are shared
- Only investment values, contributions, and goals vary by year
- Allows easy comparison and trend analysis

### 2. Historical Snapshots are Immutable

**Decision**: Once a year is marked as HISTORICAL, it cannot be edited (read-only).

**Rationale**:
- Preserves historical accuracy
- Prevents accidental data corruption
- Enables trustworthy trend analysis
- Users can always export and manually edit if needed

### 3. Current Year is the Projection Baseline

**Decision**: All future projections use the CURRENT year as the starting point.

**Rationale**:
- Reflects most up-to-date reality
- Historical years are for analysis, not projection
- Simplifies calculation logic

### 4. Backward Compatibility is Mandatory

**Decision**: All v1.0 exports must import cleanly into v2.0.

**Rationale**:
- Users may have existing exports
- Smooth migration path
- Builds trust in the application

### 5. Export Only in Latest Format

**Decision**: Exports always use v2.0 format, even if data is simple.

**Rationale**:
- Simplifies export logic
- Ensures future compatibility
- Single source of truth for format

---

## Open Questions for Discussion

1. **Snapshot Frequency**: Should users be able to create mid-year snapshots (e.g., June 2024) or only year-end?
   - **Pro**: More granular tracking
   - **Con**: Added complexity, harder to compare

2. **Automatic Snapshot Creation**: Should the app auto-create a historical snapshot at year-end?
   - **Pro**: Ensures data is preserved
   - **Con**: May capture incomplete data if user hasn't updated

3. **Investment Tracking Method**: Should users:
   - A) Manually update investment values each year
   - B) Connect to external APIs for automatic updates
   - C) Both options available

4. **Goal Partial Fulfillment**: How to handle goals that are partially fulfilled?
   - Example: Car purchase goal of ₹10L, but only spent ₹8L
   - Should remaining ₹2L roll back into corpus or be reallocated?

5. **Contribution Tracking**: Should the app:
   - A) Ask users to manually enter "actual contributions" for historical years
   - B) Derive from investment value changes
   - C) Both for validation

6. **Performance Benchmarking**: Should we add:
   - Market index comparison (e.g., Nifty 50, Sensex)
   - Peer comparison (anonymized aggregate data)
   - Neither (keep it personal)

7. **Data Privacy**: For multi-year exports:
   - Should exports be encrypted?
   - Should there be a password protection option?
   - Or keep it simple (unencrypted JSON)?

---

## Success Metrics

After implementation, the feature is successful if users can:

1. ✅ Import their existing v1.0 exports without data loss
2. ✅ Switch between years and see different data
3. ✅ Record actual investment values for past years
4. ✅ See a clear projection vs actual comparison chart
5. ✅ Identify which investments are outperforming/underperforming
6. ✅ Track their contribution discipline over time
7. ✅ Get actionable insights from historical trends
8. ✅ Export multi-year data and re-import successfully

---

## Technical Considerations

### LocalStorage Size Limits

**Issue**: Browser localStorage has ~5-10MB limit. Multi-year data could grow large.

**Mitigation**:
- Monitor storage usage
- Warn user if approaching limits
- Suggest exporting and archiving old years
- Consider compression for historical snapshots

### Performance with Large Datasets

**Issue**: Calculations across 10+ years could be slow.

**Mitigation**:
- Lazy load historical analysis
- Cache calculation results
- Use web workers for heavy computations
- Paginate year-by-year projections

### Data Consistency

**Issue**: User might edit investment categories/inflation rates, affecting historical analysis.

**Mitigation**:
- Store category rates with each snapshot (snapshot what was assumed)
- Show warning when editing shared data: "This affects all years"
- Consider version history for categories

---

## Conclusion

This multi-year feature transforms Plan My Corpus from a static projection tool into a dynamic financial tracking system. By comparing projections vs reality, users gain:

1. **Accountability**: See if they're sticking to their plans
2. **Accuracy**: Refine assumptions based on historical data
3. **Adaptability**: Adjust strategies based on what's working/not working
4. **Confidence**: Build trust in long-term projections through validated trends

The phased implementation approach ensures backward compatibility while delivering value incrementally.
