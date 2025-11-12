# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Kotlin Multiplatform** Plan My Corpus application using **Compose Multiplatform (CMP)** for UI. The project targets WebAssembly (Wasm) for web deployment.

### Application Purpose

A comprehensive financial planning tool that helps users:
- Track their financial goals (short, medium, and long-term)
- Monitor current investments and ongoing contributions with step-up support
- Calculate if they're on track to meet their financial goals
- Account for inflation, investment growth, and retirement planning
- Export/import complete financial snapshots for year-over-year tracking
- Temporarily disable items to test scenarios without deleting data

### Key Features

1. **User Profile Management**
   - Current age, retirement age, and life expectancy tracking
   - Monthly expense tracking with inflation category linkage
   - Single post-retirement growth rate for entire corpus (simplified model)

2. **Flexible Inflation Management**
   - Category-based inflation rates (general, education, health, custom)
   - Each goal and expense can be linked to a specific inflation category
   - Full CRUD operations on inflation categories

3. **Investment Tracking**
   - Current portfolio with actual XIRR (historical returns) per investment
   - Investment categories with expected XIRR for future growth
   - Different growth rates for pre-retirement vs post-retirement periods
   - Ongoing contributions with:
     - Flexible frequency (monthly/quarterly/yearly)
     - Optional duration limits (e.g., 15-year PPF)
     - Annual step-up percentage for salary-linked SIPs
   - Enable/disable toggle for scenario testing

4. **Goal Management**
   - Financial goals with target amounts and years
   - Priority system: must-have vs good-to-have goals
   - Timeline categorization: short/medium/long term
   - Inflation-adjusted calculations per goal
   - Goal withdrawal accounting with lost growth opportunity
   - Enable/disable toggle for scenario testing

5. **Corpus Health Analysis**
   - Total corpus projections vs total goal requirements
   - Investments are not linked to specific goals - corpus health shows if total funds can meet all goals
   - Clear indication of whether must-have goals are funded
   - Year-by-year projection breakdown from current age to life expectancy
   - Post-retirement drawdown simulation with expense withdrawals
   - Retirement surplus/shortfall with detailed breakdown
   - Life expectancy corpus or depletion age warning

6. **Interactive Visualizations**
   - Corpus projection chart with clickable year markers
   - Goal realization markers on timeline
   - Retirement milestone indicator
   - Realistic post-retirement drawdown visualization
   - Real-time calculation updates across all screens

7. **Data Export/Import**
   - JSON export with complete snapshot (all settings, data, and timestamp)
   - Import capability for session restoration
   - Year-over-year comparison support
   - All data models are @Serializable

8. **Data Persistence**
   - Auto-save to browser localStorage on every change
   - Manual JSON export for backups and tracking

## Development Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Run the web application in development mode
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build production web bundle
./gradlew :composeApp:wasmJsBrowserProductionWebpack

# Clean build artifacts
./gradlew clean
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific target
./gradlew wasmJsTest
```

## Architecture

### Module Structure
- **composeApp**: Main application module containing:
  - `commonMain`: Shared UI code and business logic (Compose UI components)
  - `wasmJsMain`: Web-specific entry point and resources

### Key Files
- `App.kt` (composeApp/src/commonMain/kotlin/): Main Compose UI entry point
- `AppState.kt` (composeApp/src/commonMain/kotlin/): Centralized state management
- `main.kt` (composeApp/src/wasmJsMain/kotlin/): WebAssembly-specific initialization
- `index.html` (composeApp/src/wasmJsMain/resources/): HTML container for the web app

### Technology Stack
- **Kotlin**: 2.0.21
- **Compose Multiplatform**: 1.7.0
- **Gradle**: 8.10
- **Target Platform**: WebAssembly (wasmJs)
- **Serialization**: kotlinx-serialization-json
- **State Management**: Compose mutableStateOf with reactive updates

## Project Conventions

### Adding New UI Components
All UI components should be created in `composeApp/src/commonMain/kotlin/` to ensure they can be shared across potential future platforms (Android, iOS, Desktop).

### Icons
**IMPORTANT**: Always use Material Icons (`Icons.Default.*`) for all icons in the application. Never use ASCII/emoji icons as they may not render correctly in WebAssembly. Material Icons are part of the Compose Material3 library and are guaranteed to work across all platforms.

### Compose Dependencies
The project uses Material3 design system. When adding new dependencies, add them to the `commonMain` source set in `composeApp/build.gradle.kts` unless they are platform-specific.

### Build Output
The web application builds to `composeApp/build/dist/wasmJs/productionExecutable/`. The entry point is `index.html` which loads `composeApp.js`.

## Data Model Reference

### Complete Entity Structure

#### UserProfile
```kotlin
data class UserProfile(
    val currentAge: Int = 30,
    val retirementAge: Int = 60,
    val lifeExpectancy: Int = 85,
    val currentMonthlyExpenses: Double = 50000.0,
    val expenseInflationCategoryId: String = "general",
    val postRetirementGrowthRate: Double = 10.0 // Single rate for all corpus post-retirement
)
```

#### InflationCategory
```kotlin
data class InflationCategory(
    val id: String,
    val name: String,
    val rate: Double // Annual percentage
)
```

#### InvestmentCategory
```kotlin
data class InvestmentCategory(
    val id: String,
    val name: String,
    val preRetirementXIRR: Double,  // Expected pre-retirement return %
    val postRetirementXIRR: Double  // Expected post-retirement return %
)
```

#### Investment
```kotlin
data class Investment(
    val id: String,
    val name: String,
    val currentValue: Double,
    val categoryId: String,
    val actualXIRR: Double,        // Historical return %
    val isEnabled: Boolean = true  // Include in calculations
)
```

#### OngoingContribution
```kotlin
data class OngoingContribution(
    val id: String,
    val name: String,
    val amount: Double,
    val frequency: ContributionFrequency, // MONTHLY, QUARTERLY, YEARLY
    val categoryId: String,
    val durationYears: Int? = null,      // null = until retirement
    val stepUpPercentage: Double = 0.0,  // Yearly increase %
    val isEnabled: Boolean = true        // Include in calculations
)
```

#### FinancialGoal
```kotlin
data class FinancialGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,           // In today's value
    val targetYear: Int,
    val inflationCategoryId: String,
    val priority: GoalPriority,         // MUST_HAVE, GOOD_TO_HAVE
    val timeline: GoalTimeline,         // SHORT_TERM, MEDIUM_TERM, LONG_TERM
    val isEnabled: Boolean = true       // Include in calculations
)
```

#### FinancialPlanData (Root Container)
```kotlin
data class FinancialPlanData(
    val userProfile: UserProfile,
    val inflationCategories: List<InflationCategory>,
    val investmentCategories: List<InvestmentCategory>,
    val investments: List<Investment>,
    val ongoingContributions: List<OngoingContribution>,
    val goals: List<FinancialGoal>,
    val snapshotTimestamp: Long = 0
)
```

## Calculation Engine

### Key Calculation Functions

#### 1. Inflation Adjustment
```kotlin
fun inflationAdjusted(presentValue: Double, inflationRate: Double, years: Int): Double
// Formula: PV × (1 + rate)^years
```

#### 2. Investment Future Value
```kotlin
fun calculateInvestmentFutureValue(
    currentValue: Double,
    currentYear: Int,
    targetYear: Int,
    retirementYear: Int,
    preRetirementXIRR: Double,
    postRetirementGrowthRate: Double
): Double
// Handles dual-phase growth (pre/post retirement)
// Post-retirement uses single blended rate from UserProfile
```

#### 3. Contribution Future Value (with Step-Up Support)
```kotlin
fun calculateContributionsFutureValue(
    contribution: OngoingContribution,
    currentYear: Int,
    targetYear: Int,
    retirementYear: Int,
    preRetirementXIRR: Double,
    postRetirementGrowthRate: Double
): Double
// If stepUpPercentage = 0: Uses annuity formula (optimized)
// If stepUpPercentage > 0: Year-by-year calculation
// Accounts for contribution duration limits
```

#### 4. Total Corpus Calculation
```kotlin
fun calculateTotalCorpus(
    data: FinancialPlanData,
    currentYear: Int,
    targetYear: Int
): Double
// Sums all investments and contributions
// Subtracts goal withdrawals with lost growth opportunity
// Pre-retirement goals: lost growth to target year
// Post-retirement goals: withdrawn at goal year, no further growth
```

#### 5. Post-Retirement Corpus Drawdown
```kotlin
fun calculatePostRetirementCorpus(
    startingCorpus: Double,
    retirementYear: Int,
    targetYear: Int,
    monthlyExpenseAtRetirement: Double,
    expenseInflationRate: Double,
    postRetirementGrowthRate: Double,
    goalsAfterRetirement: List<Pair<Int, Double>>
): Double
// Year-by-year simulation:
// 1. Subtract annual expenses (inflation-adjusted)
// 2. Subtract goals maturing this year
// 3. Grow remaining corpus
// 4. Repeat until target year
```

#### 6. Retirement Corpus Requirement
```kotlin
fun calculateRetirementCorpus(
    data: FinancialPlanData,
    currentYear: Int
): Double
// Uses Present Value of Annuity formula
// PV = Monthly Expense × [(1 - (1 + real_rate)^(-n)) / real_rate]
// real_rate = (growth_rate - inflation_rate) / 12
```

### Corpus Analysis

#### CorpusAnalyzer.analyze()
Returns `FinancialAnalysis` containing:
- `corpusHealth`: Overall health metrics
- `goalAnalyses`: Per-goal funding status
- `yearlyProjections`: Year-by-year breakdown

#### CorpusHealth Metrics
- `totalCorpusAtRetirement`: Corpus before any withdrawals
- `retirementCorpusRequired`: PV of retirement expenses
- `totalMustHaveGoalsRequired`: Sum of must-have goal amounts
- `totalAllGoalsRequired`: Sum of all goal amounts
- `totalRequiredIncludingRetirement`: Goals + retirement corpus
- Funding status booleans for various scenarios
- `overallSurplus`: Available after all requirements

## Acceptance Criteria

### Core Functionality ✅
- ✅ User can set their current age, retirement age, and life expectancy
- ✅ User can define multiple inflation categories with custom rates
- ✅ User can define investment categories with pre-retirement and post-retirement XIRR
- ✅ User can add current investments with actual XIRR
- ✅ User can set up ongoing contributions (monthly/quarterly/yearly)
- ✅ User can set contribution duration limits
- ✅ User can set annual step-up percentage for contributions
- ✅ User can create financial goals with:
  - Target amount (in today's value)
  - Target year
  - Inflation category
  - Priority (must-have/good-to-have)
  - Timeline category (short/medium/long term)
- ✅ User can temporarily disable goals, investments, or contributions

### Calculations ✅
- ✅ Calculate inflation-adjusted goal amounts based on target year and inflation category
- ✅ Project total corpus value at any future year considering:
  - Current investments growing at expected XIRR (pre/post retirement)
  - Ongoing contributions growing at expected XIRR (pre/post retirement)
  - Different growth rates before and after retirement age
  - Step-up contributions with yearly increases
  - Goal withdrawals with lost growth opportunity
- ✅ Calculate total required funds for all goals
- ✅ Determine if corpus can meet must-have goals
- ✅ Determine if corpus can meet all goals (must-have + good-to-have)
- ✅ Provide year-by-year projection breakdown to life expectancy
- ✅ Calculate retirement corpus using Present Value formula
- ✅ Simulate post-retirement drawdown with expense withdrawals

### User Interface ✅
- ✅ Dashboard showing overall corpus health status
- ✅ Retirement surplus/shortfall with detailed breakdown
- ✅ Life expectancy corpus or depletion age indicator
- ✅ Screen to manage user profile (ages, expenses, rates)
- ✅ Screen to manage inflation categories (full CRUD)
- ✅ Screen to manage investment categories (full CRUD)
- ✅ Screen to manage current portfolio (full CRUD)
- ✅ Screen to manage ongoing contributions (full CRUD, including step-up)
- ✅ Screen to manage financial goals (full CRUD)
- ✅ Detailed analysis screen with:
  - Interactive corpus projection chart
  - Goal-by-goal breakdown (sorted chronologically)
  - Year-by-year projections table
- ✅ Navigation between all screens with back button support
- ✅ Settings hub screen
- ✅ Indian currency formatting (lakhs/crores)
- ✅ Enable/disable toggles on all item cards
- ✅ Input validation and error handling

### Data Management ✅
- ✅ Auto-save all data to browser localStorage
- ✅ Export complete data snapshot as JSON file (with timestamp)
- ✅ Import JSON file to restore or load previous snapshots
- ✅ JSON includes all data needed for year-over-year comparison

## Implementation Progress

### Completed ✅

✅ **Project Setup**
  - Kotlin Multiplatform and Compose Multiplatform configured
  - Gradle build system with WebAssembly target
  - IntelliJ IDEA compatibility with run configurations

✅ **Data Models**
  - UserProfile with life expectancy and monthly expenses
  - InflationCategory with defaults (general, education, health)
  - InvestmentCategory with pre/post-retirement XIRR
  - Investment with actual XIRR and enable/disable flag
  - OngoingContribution with frequency, duration, step-up, and enable/disable flag
  - FinancialGoal with priority, timeline, and enable/disable flag
  - FinancialPlanData (container for all data)
  - Serialization support (kotlinx-serialization)

✅ **Calculation Engine**
  - Inflation adjustment calculations
  - Future value calculations with pre/post retirement rates
  - Investment growth projections
  - Ongoing contributions future value with step-up support
  - Corpus projection at any year with goal withdrawal accounting
  - Goal funding analysis (per goal)
  - Corpus health analysis (must-have vs good-to-have)
  - Year-by-year breakdown to life expectancy
  - Retirement corpus using Present Value formula
  - Post-retirement drawdown simulation
  - Enable/disable filtering in all calculations

✅ **UI Navigation & Screens**
  - Material3 design with bottom navigation
  - Dashboard with:
    - Corpus health overview
    - Retirement surplus/shortfall card
    - Life expectancy projection
    - Detailed requirement breakdown
  - User Profile screen (fully editable)
  - Inflation Rates screen (full CRUD)
  - Investment Categories screen (full CRUD)
  - Portfolio screen with full CRUD and enable/disable toggle
  - Contributions screen with full CRUD, step-up field, and enable/disable toggle
  - Goals screen with full CRUD and enable/disable toggle
  - Analysis screen with:
    - Interactive corpus chart (clickable years)
    - Chronologically sorted goals
    - Year-by-year projections
  - Settings hub screen
  - Back navigation and URL routing

✅ **Data Persistence**
  - Auto-save to localStorage on every data change
  - JSON export to download complete snapshots
  - JSON import to restore/load previous sessions
  - Platform-specific storage implementation for web
  - Export/Import buttons in Settings

✅ **UI Components**
  - CurrencyTextField with Indian formatting
  - CorpusChart with interactive year markers
  - Enable/disable toggle switches with visual feedback
  - Reusable cards with consistent styling

### Planned (Roadmap)

#### 1. Enhanced Color Coding & Visual Indicators
- [ ] **Goal**: Make financial health immediately visible through color
- [ ] **Scope**: All screens - Dashboard, Goals, Analysis, Portfolio, Contributions
- [ ] **Features**:
  - Color-coded card backgrounds/borders based on funding status
    - Green: Funded/on-track
    - Yellow: Partially funded (50-99%)
    - Red: Unfunded/critical (<50%)
  - Visual progress bars showing goal funding percentage
  - Chart color zones (corpus trajectory vs required trajectory)
  - Performance-based color coding for investments:
    - Blue: High performers (>12% XIRR)
    - Green: Medium (8-12%)
    - Yellow: Low (<8%)
  - Status badges throughout UI (✓ ✗ ⚠)
- [ ] **Implementation**:
  - Create `ui/theme/FinancialColors.kt` with semantic color definitions
  - Update card components to accept status parameter
  - Enhance chart with colored zones/thresholds
  - Add funding percentage indicators

#### 2. Minimum SIP Calculator (Actionable Insights)
- [ ] **Goal**: Tell users exactly what action to take when off-track
- [ ] **Scope**: Calculate minimum monthly SIP to fund all unfunded goals
- [ ] **Features**:
  - New "Action Required" card on Dashboard (shown when off-track)
  - Display minimum monthly SIP amount
  - Show target investment category (where to invest this SIP)
  - Per-goal SIP breakdown
  - "Add Recommended SIP" quick action button (pre-fills contribution dialog)
- [ ] **Implementation**:
  - Add to `FinancialCalculations.kt`:
    ```kotlin
    fun calculateMinSIPForUnfundedGoals(
        data: FinancialPlanData,
        currentYear: Int,
        goalAnalyses: List<GoalAnalysis>,
        targetCategory: InvestmentCategory
    ): Double
    ```
  - Logic: Sum unfunded shortfalls, reverse annuity formula for monthly SIP
  - Dashboard card with category selector
  - Goals screen integration

#### 3. Rich Export Metadata
- [ ] **Goal**: Add comprehensive metadata to JSON snapshots for versioning and comparison
- [ ] **Scope**: Enhance export/import with rich context
- [ ] **Data Model Changes**:
  ```kotlin
  @Serializable
  data class SnapshotMetadata(
      val version: String = "1.0",
      val exportDate: String = "",              // ISO format: "2025-01-15"
      val snapshotLabel: String = "",           // User-provided: "Annual Review 2025"
      val userNotes: String = "",               // Optional multi-line notes
      val appVersion: String = "1.0.0",
      val totalCorpusSnapshot: Double = 0.0,    // Captured at export time
      val goalsFundedSnapshot: Int = 0,
      val goalsUnfundedSnapshot: Int = 0
  )

  // Add to FinancialPlanData:
  val metadata: SnapshotMetadata = SnapshotMetadata()
  ```
- [ ] **Features**:
  - Pre-export dialog asking for snapshot label and notes
  - Auto-populate timestamp, date, app version, snapshot metrics
  - Smart filename: `financial_plan_YYYY-MM-DD.json`
  - Import preview showing metadata before accepting
- [ ] **Implementation**:
  - Create `model/SnapshotMetadata.kt`
  - Update `FinancialPlanData.kt` to include metadata field
  - Update `AppState.downloadSnapshot()` to show dialog and populate metadata
  - Update `AppState.uploadSnapshot()` to show preview

#### 4. Year-over-Year Comparison
- [ ] **Goal**: Load multiple snapshots and visualize financial progress over time
- [ ] **Scope**: Compare current state with historical snapshots
- [ ] **Data Model** (Already Ready):
  - `snapshotTimestamp` field exists in FinancialPlanData ✓
  - Metadata will provide additional context (from Feature #3)
- [ ] **New Files**:
  - `calculation/ComparisonAnalyzer.kt`:
    ```kotlin
    data class YearOverYearComparison(
        val baseSnapshot: FinancialPlanData,
        val compareSnapshot: FinancialPlanData,
        val corpusGrowth: CorpusGrowthComparison,
        val goalProgress: GoalProgressComparison,
        val netWorthDelta: NetWorthComparison,
        val summary: ComparisonSummary
    )

    data class CorpusGrowthComparison(
        val baseCorpus: Double,
        val compareCorpus: Double,
        val absoluteChange: Double,
        val percentageChange: Double
    )

    data class GoalProgressComparison(
        val newGoalsAdded: Int,
        val goalsCompleted: Int,
        val fundingImproved: Int,
        val fundingDegraded: Int,
        val goalDeltas: List<GoalDelta>  // Per-goal comparison
    )

    data class GoalDelta(
        val goalName: String,
        val baseShortfall: Double,
        val compareShortfall: Double,
        val improvement: Double  // Positive = better funding
    )

    data class NetWorthComparison(
        val baseInvestments: Double,
        val compareInvestments: Double,
        val investmentGrowth: Double,
        val newContributionsAdded: Double
    )

    data class ComparisonSummary(
        val timeframeDays: Int,
        val averageCorpusGrowthRate: Double,
        val goalsProgressMessage: String,
        val overallHealthChange: String  // "Improved", "Stable", "Degraded"
    )

    object ComparisonAnalyzer {
        fun compare(
            base: FinancialPlanData,
            compare: FinancialPlanData,
            currentYear: Int
        ): YearOverYearComparison
    }
    ```
- [ ] **UI Changes - Analysis Screen**:
  - Add "Compare with Previous Snapshot" button
  - File picker to load comparison JSON
  - Toggle view: "Current" | "Comparison" | "Side-by-Side"
  - Side-by-side view:
    - Two corpus chart lines (blue=current, green=historical)
    - Delta cards showing changes:
      - Corpus: ▲ ₹XX.X Cr (+Y%)
      - Goals funded: 5 → 7 (+2)
      - Investment growth: ▲ ₹XX L
    - Detailed comparison section:
      - Corpus growth table (year-by-year)
      - Goal-by-goal progress
      - Net worth breakdown
  - "Clear Comparison" button to exit mode
- [ ] **Implementation Order**:
  1. Build ComparisonAnalyzer with comparison logic
  2. Update Analysis screen state to hold comparison data
  3. Add file picker and comparison loading
  4. Implement side-by-side chart visualization
  5. Add detailed comparison tables

#### 5. Liability Management & Net Worth Tracking
- [ ] **Goal**: Account for liabilities (loans, mortgages, debt) in corpus planning and net worth calculations
- [ ] **Scope**: Track debts, their impact on cash flow, and net worth over time
- [ ] **Rationale**:
  - Current model tracks only assets (investments, contributions) and goals
  - Real financial health requires net worth view (assets - liabilities)
  - Loan payments reduce available cash flow for new contributions
  - Debt payoff is often a critical financial goal
  - Interest paid on debt reduces effective investment returns
  - Retirement planning should account for becoming debt-free
- [ ] **Data Model Changes**:
  ```kotlin
  @Serializable
  data class Liability(
      val id: String,
      val name: String,                    // e.g., "Home Loan", "Car Loan", "Credit Card"
      val type: LiabilityType,             // MORTGAGE, PERSONAL_LOAN, VEHICLE_LOAN, CREDIT_CARD, OTHER
      val principalOutstanding: Double,    // Current outstanding amount
      val interestRate: Double,            // Annual interest rate %
      val emiAmount: Double,               // Monthly payment amount
      val startDate: String,               // Loan start date (ISO format)
      val endDate: String,                 // Expected payoff date
      val isEnabled: Boolean = true        // Include in calculations
  )

  enum class LiabilityType {
      MORTGAGE,
      PERSONAL_LOAN,
      VEHICLE_LOAN,
      EDUCATION_LOAN,
      CREDIT_CARD,
      OTHER
  }

  // Add to FinancialPlanData:
  val liabilities: List<Liability> = emptyList()
  ```
- [ ] **Key Calculations**:
  - **Net Worth Calculation**: Total Assets - Total Liabilities
  - **Effective Cash Flow**: Available monthly income after EMI payments
  - **Total Interest Burden**: Sum of interest paid over loan tenure
  - **Debt-Free Date**: When all liabilities will be paid off
  - **Liability Amortization Schedule**: Year-by-year principal vs interest breakdown
  - **Impact on Contributions**: How pre-paying debt affects available SIP capacity
  - **Opportunity Cost**: Investment returns lost due to debt service
- [ ] **UI Features**:
  - New "Liabilities" screen (full CRUD)
    - Add/edit/delete liabilities
    - View amortization schedule per liability
    - Enable/disable toggle for scenario testing
    - Total EMI burden summary
  - Dashboard enhancements:
    - Net Worth card (Assets - Liabilities)
    - Debt-to-Asset ratio
    - Monthly EMI burden vs available cash flow
    - Debt-Free milestone indicator
  - Analysis screen updates:
    - Net worth projection chart (assets and liabilities on same timeline)
    - "Should I pre-pay?" calculator comparing:
      - Pre-paying loan vs investing the amount
      - Interest saved vs potential investment returns
    - Liability payoff timeline visualization
  - New "Debt Goals" feature:
    - Option to add "Pay off [Loan Name]" as a financial goal
    - Auto-calculate lump sum needed for early closure
    - Show interest savings from pre-payment
- [ ] **Strategic Insights**:
  - Alert when debt-to-asset ratio exceeds healthy threshold (e.g., >40%)
  - Suggest optimal debt payoff vs investment strategy based on:
    - Loan interest rate vs expected investment returns
    - Tax benefits (e.g., home loan interest deduction)
    - Liquidity needs
  - Retirement readiness check: Flag if liabilities extend beyond retirement age
  - High-interest debt prioritization (avalanche method recommendation)
- [ ] **Implementation Considerations**:
  - Should EMI payments reduce available contribution capacity automatically?
    - Option 1: User manually accounts for EMIs when setting contribution amounts
    - Option 2: System auto-calculates "available for investment" after EMI deductions
    - Option 3: Hybrid - show both gross and net available cash flow
  - How to handle refinancing scenarios?
    - Allow editing liability terms mid-tenure
    - Track refinancing history in metadata
  - Should liabilities be linked to specific assets?
    - E.g., Home loan linked to property value (not currently tracked)
    - May require adding "Physical Assets" module (property, vehicles)
  - Tax implications of loan interest (especially home loans in India)
    - Deductible interest reduces effective interest rate
    - May need tax calculation module integration
- [ ] **Implementation Order**:
  1. Create `model/Liability.kt` with data model
  2. Add amortization calculation functions to `calculation/FinancialCalculations.kt`
  3. Create `calculation/NetWorthAnalyzer.kt` for net worth and debt analytics
  4. Update `FinancialPlanData` and serialization
  5. Create `ui/screens/LiabilitiesScreen.kt` with full CRUD
  6. Update Dashboard to show net worth and debt metrics
  7. Enhance Analysis screen with net worth projections
  8. Add pre-payment calculator and strategic insights

### Future Enhancements (Lower Priority)
- [ ] Multi-currency support
- [ ] Tax calculations (integrate with liability interest deductions)
- [ ] Physical asset tracking (property, vehicles) to match with liabilities
- [ ] Asset allocation recommendations
- [ ] Monte Carlo simulations
- [ ] Dark mode
- [ ] Mobile-responsive design
- [ ] PDF report generation

## Code Organization

### Package Structure
```
composeApp/src/commonMain/kotlin/
├── model/              # Data models (@Serializable)
│   ├── UserProfile.kt
│   ├── InflationCategory.kt
│   ├── InvestmentCategory.kt
│   ├── Investment.kt
│   ├── OngoingContribution.kt
│   ├── FinancialGoal.kt
│   └── FinancialPlanData.kt
├── calculation/        # Business logic and calculations
│   ├── FinancialCalculations.kt
│   └── CorpusAnalysis.kt
├── ui/                 # UI components and screens
│   ├── screens/        # Screen implementations
│   │   ├── DashboardScreen.kt
│   │   ├── UserProfileScreen.kt
│   │   ├── InflationRatesScreen.kt
│   │   ├── InvestmentCategoriesScreen.kt
│   │   ├── PortfolioScreen.kt
│   │   ├── ContributionsScreen.kt
│   │   ├── GoalsScreen.kt
│   │   ├── AnalysisScreen.kt
│   │   └── SettingsScreen.kt
│   └── components/     # Reusable components
│       ├── CurrencyTextField.kt
│       └── CorpusChart.kt
├── storage/            # Data persistence (localStorage, JSON)
│   └── PlatformStorage.kt
├── App.kt              # Main application entry with navigation
└── AppState.kt         # Centralized state management
```

### Key Calculation Concepts

1. **Inflation Adjustment**: `Future Value = Present Value × (1 + inflation_rate)^years`
2. **Investment Growth**: Different rates apply before and after retirement age
3. **Single Post-Retirement Rate**: Entire corpus grows at one blended rate after retirement (from UserProfile)
4. **Corpus Calculation**: Sum of all current investments + future value of ongoing contributions - goal withdrawals (with lost growth)
5. **Goal Funding Status**: Compare total corpus at goal year vs inflation-adjusted goal amount
6. **Step-Up Contributions**: Year-by-year compounding with increasing contribution amounts
7. **Post-Retirement Drawdown**: Simulate year-by-year withdrawals (expenses + goals) and growth
8. **Present Value**: Retirement corpus calculated as PV of future expenses, not total future expenses

### Important Implementation Notes

#### Enable/Disable Feature
- All items (goals, investments, contributions) have `isEnabled` field
- Disabled items are excluded from all calculations via `.filter { it.isEnabled }`
- UI shows disabled items with 50% opacity and red "Excluded" status
- Toggle switch on each card for easy scenario testing

#### Step-Up Calculations
- When `stepUpPercentage = 0`: Uses optimized annuity formula
- When `stepUpPercentage > 0`: Uses year-by-year loop for accuracy
- Contribution increases compound yearly: Year N amount = Year 1 × (1 + step_up)^(N-1)

#### Post-Retirement Model
- Single growth rate for entire corpus (simplified model)
- Year-by-year drawdown simulation for realistic projections
- Expenses inflate from retirement onwards
- Goals withdrawn in their target year
- Corpus grows at post-retirement rate each year

#### Goal Withdrawal Accounting
- Pre-retirement goals: Withdrawn amount + lost growth to target year
- Post-retirement goals: Present Value at retirement (discounted)
- Impact shown separately in dashboard breakdown

## Development Guidelines

### State Management
- All state is centralized in `AppState`
- UI screens are stateless composables
- State updates trigger automatic recomposition
- Auto-save on every state change

### Adding New Features
1. Update data models in `model/` (add @Serializable)
2. Update calculation logic in `calculation/`
3. Update `AppState` with new update functions
4. Create/update UI screens
5. Test with enable/disable toggle
6. Verify JSON export/import compatibility

### Testing Calculations
Use the enable/disable feature to test edge cases:
- Disable all goals to see pure corpus growth
- Disable investments to see contribution-only impact
- Disable step-up to compare with flat contributions

### Performance Considerations
- Year-by-year calculations (step-up, drawdown) are O(n) where n = years
- Optimized annuity formulas used when possible (no step-up)
- Calculations are fast enough for real-time updates (<100ms typical)

## Troubleshooting

### Build Issues
- Ensure JDK 17+ is being used
- Clear Gradle cache: `./gradlew clean`
- Invalidate IntelliJ caches: File → Invalidate Caches

### Runtime Issues
- Check browser console for JavaScript errors
- Clear localStorage to reset state: `localStorage.clear()`
- Export data before clearing to preserve work

### Calculation Issues
- Verify inflation categories are properly linked
- Check that investment categories exist for all investments/contributions
- Ensure retirement age > current age
- Verify goal target years are in the future
