# Financial Planner

A comprehensive Kotlin Multiplatform financial planning application built with Compose Multiplatform for the web. Plan your retirement, track goals, and visualize your financial future with precision.

## Features

### Core Planning
- **User Profile**: Set current age, retirement age, life expectancy, and monthly expenses
- **Financial Goals**: Create goals with customizable priority (must-have vs good-to-have), timelines, and inflation categories
- **Investment Portfolio**: Monitor current investments across different asset categories with actual XIRR tracking
- **Ongoing Contributions**: Track SIPs, PPF, and other recurring investments with:
  - Custom frequencies (monthly/quarterly/yearly)
  - Optional duration limits (e.g., 15-year PPF)
  - **Annual step-up percentage** for salary-linked contributions
- **Retirement Planning**: Comprehensive retirement corpus calculation with life expectancy projections

### Advanced Calculations
- **Multi-Category Inflation**: Different inflation rates for general expenses, education, healthcare, and custom categories
- **Dual-Phase Investment Growth**: Separate XIRR rates for pre-retirement (equity-heavy) and post-retirement (debt-heavy) periods
- **Single Post-Retirement Rate**: Simplified model where entire corpus grows at one blended rate after retirement
- **Goal Withdrawal Accounting**:
  - Tracks money withdrawn for goals
  - Accounts for lost growth opportunity when goals are realized
  - Differentiates pre-retirement and post-retirement goal impact
- **Post-Retirement Drawdown**: Year-by-year simulation of corpus depletion with:
  - Monthly expense withdrawals (inflation-adjusted)
  - Goal withdrawals
  - Corpus growth at post-retirement rate
- **Corpus Projections**: Year-by-year projection from current age to life expectancy
- **Enable/Disable Items**: Temporarily exclude goals, investments, or contributions from calculations without deleting

### Visualization & Analysis
- **Dashboard**:
  - Corpus health overview
  - Retirement readiness indicators
  - Retirement surplus/shortfall with detailed breakdown
  - Corpus at life expectancy or depletion age warning
- **Interactive Corpus Chart**:
  - Visual timeline showing corpus growth from current age to life expectancy
  - Goal realization markers
  - Retirement milestone
  - Click any year to see exact corpus value
  - Pre-retirement and post-retirement goal impact clearly differentiated
- **Goal Analysis**:
  - Sorted by target year (chronological)
  - Funding status per goal
  - Shortfall calculations
- **Real-time Updates**: See impact of changes immediately across all screens

### Data Management
- **Auto-Save**: All changes automatically saved to browser localStorage
- **Export/Import**: Complete financial snapshots in JSON format
  - Year-over-year comparison support
  - Backup and restore functionality
- **Indian Currency Formatting**: Native support for lakhs and crores (₹1,00,000 = 1L, ₹1,00,00,000 = 1Cr)
- **Customizable Categories**: Full CRUD operations for investment and inflation categories

## Technology Stack

- **Kotlin Multiplatform**: 2.0.21
- **Compose Multiplatform**: 1.7.0
- **Target Platform**: WebAssembly (Wasm)
- **Gradle**: 8.10
- **Serialization**: kotlinx-serialization for JSON export/import
- **State Management**: Compose state with reactive updates
- **Storage**: Browser localStorage API for data persistence
- **Charts**: Custom Compose-based interactive charts

## Getting Started

### Prerequisites

- JDK 17 or higher
- IntelliJ IDEA (recommended) or any Kotlin-compatible IDE
- Minimum 6GB RAM allocated to Gradle (configured in gradle.properties)

### Opening in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select **File → Open**
3. Navigate to this project directory and select it
4. IntelliJ will automatically detect the Gradle project and import it
5. Wait for Gradle sync to complete

### Running the Application

#### Using IntelliJ Run Configurations

The project includes pre-configured run configurations:

1. **Run Development Server**: Start the development server with hot-reload
   - Select "Run Development Server" from the run configurations dropdown
   - Click the Run button (▶️)
   - Open your browser to `http://localhost:8080`

2. **Build Production**: Create a production build
   - Select "Build Production" from the run configurations dropdown
   - Click the Run button (▶️)
   - Output will be in `composeApp/build/dist/wasmJs/productionExecutable/`

#### Using Command Line

```bash
# Run development server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build for production
./gradlew :composeApp:wasmJsBrowserProductionWebpack

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Project Structure

```
financial-planner/
├── composeApp/              # Main application module
│   └── src/
│       ├── commonMain/      # Shared Kotlin code
│       │   ├── kotlin/
│       │   │   ├── model/          # Data models (all @Serializable)
│       │   │   ├── calculation/    # Financial calculation engine
│       │   │   ├── ui/             # UI components
│       │   │   │   ├── screens/    # Screen implementations
│       │   │   │   └── components/ # Reusable UI components
│       │   │   ├── storage/        # Data persistence layer
│       │   │   ├── App.kt          # Main app entry
│       │   │   └── AppState.kt     # State management
│       │   └── resources/
│       └── wasmJsMain/      # Web-specific code
│           ├── kotlin/
│           │   └── main.kt         # Web entry point
│           └── resources/
│               └── index.html      # HTML container
├── gradle/                  # Gradle wrapper files
├── .idea/                   # IntelliJ IDEA settings
│   └── runConfigurations/   # Pre-configured run configs
└── build.gradle.kts         # Build configuration
```

## Development

### Code Organization

- **model/**: Data classes for all financial entities (goals, investments, etc.)
- **calculation/**: Financial calculations and corpus analysis
- **ui/screens/**: Individual screen implementations
- **ui/components/**: Reusable UI components (CurrencyTextField, CorpusChart, etc.)
- **storage/**: Platform-specific storage implementations
- **AppState**: Centralized state management with reactive updates

### Key Concepts

1. **Inflation Adjustment**: Multiple categories (general, education, health, etc.) with customizable rates
2. **Dual-Phase Investment Growth**: Different XIRR rates for pre-retirement and post-retirement periods
3. **Single Post-Retirement Rate**: Entire corpus grows at one blended rate after retirement
4. **Corpus Health**: Total portfolio value vs. total goal requirements including retirement
5. **Goal Priority**: Must-have vs. good-to-have goals for better planning
6. **Goal Withdrawals**: Accounts for both the withdrawal and lost growth opportunity
7. **Contribution Duration**: Set specific durations for contributions (e.g., 15-year PPF commitment)
8. **Step-Up Contributions**: Annual percentage increases for salary-linked investments
9. **Post-Retirement Drawdown**: Year-by-year simulation of expense withdrawals and corpus depletion
10. **Present Value Calculations**: Retirement corpus uses PV formula for realistic planning

### Financial Formulas

#### Inflation Adjustment
```
Future Value = Present Value × (1 + inflation_rate)^years
```

#### Investment Growth (Pre-Retirement)
```
FV = Current Value × (1 + XIRR)^years
```

#### Contribution Growth (No Step-Up)
```
FV = Annual Contribution × [(1 + r)^n - 1] / r
```

#### Contribution Growth (With Step-Up)
Year-by-year calculation where:
- Year 1: Contribution × (1 + growth_rate)
- Year 2: Contribution × (1 + step_up) × (1 + growth_rate) + Year1_corpus × (1 + growth_rate)
- And so on...

#### Retirement Corpus (Present Value)
```
PV = Monthly Expense × [(1 - (1 + real_rate)^(-n)) / real_rate]
where real_rate = (growth_rate - inflation_rate) / 12 (monthly)
```

#### Post-Retirement Drawdown
Year-by-year simulation:
1. Subtract annual expenses (inflation-adjusted from retirement)
2. Subtract any goals maturing this year
3. Grow remaining corpus at post-retirement rate
4. Repeat until life expectancy

### Recent Updates

#### Latest Features (January 2025)
- **Enable/Disable Toggle**: Temporarily exclude items from calculations without deleting
  - Toggle switches on all goal, investment, and contribution cards
  - Visual indicators (transparency + status text)
  - Instant recalculation across all views
- **Annual Step-Up for Contributions**: Support for salary-linked SIPs
  - Percentage-based yearly increases
  - Year-by-year calculation for accuracy
  - Optimized path for non-step-up contributions
- **Enhanced Dashboard**:
  - Retirement surplus/shortfall card with detailed breakdown
  - Corpus at life expectancy or depletion age warning
  - Clear explanation of Present Value calculations
- **Interactive Corpus Chart**:
  - Click any year to see exact corpus value
  - Goal markers with tooltips
  - Retirement milestone indicator
  - Realistic post-retirement drawdown visualization

#### Calculation Improvements
- Fixed post-retirement corpus to show realistic drawdown (expenses + goals)
- Pre-retirement goals account for lost growth opportunity
- Post-retirement goals use Present Value discounting
- Separate handling of pre-retirement and post-retirement goal impact
- Single post-retirement growth rate for simplified planning

#### UI/UX Improvements
- Indian currency formatting (lakhs/crores) for all money inputs
- Full CRUD operations for investment and inflation categories
- Settings hub with navigation to all configuration screens
- Back navigation and URL routing for better user experience
- Real-time calculation updates with transparent formulas
- Step-up percentage display on contribution cards

## Data Model

### Complete JSON Export Structure

```json
{
  "userProfile": {
    "currentAge": 34,
    "retirementAge": 55,
    "lifeExpectancy": 85,
    "currentMonthlyExpenses": 50000,
    "expenseInflationCategoryId": "general",
    "postRetirementGrowthRate": 10.0
  },
  "inflationCategories": [
    {
      "id": "general",
      "name": "General",
      "rate": 6.0
    },
    {
      "id": "education",
      "name": "Education",
      "rate": 8.0
    },
    {
      "id": "health",
      "name": "Healthcare",
      "rate": 10.0
    }
  ],
  "investmentCategories": [
    {
      "id": "equity",
      "name": "Equity",
      "preRetirementXIRR": 14.0,
      "postRetirementXIRR": 8.0
    },
    {
      "id": "debt",
      "name": "Debt",
      "preRetirementXIRR": 8.0,
      "postRetirementXIRR": 7.0
    }
  ],
  "investments": [
    {
      "id": "inv-001",
      "name": "Equity Portfolio",
      "currentValue": 20000000,
      "categoryId": "equity",
      "actualXIRR": 15.2,
      "isEnabled": true
    }
  ],
  "ongoingContributions": [
    {
      "id": "cont-001",
      "name": "Monthly SIP",
      "amount": 200000,
      "frequency": "MONTHLY",
      "categoryId": "equity",
      "durationYears": null,
      "stepUpPercentage": 10.0,
      "isEnabled": true
    }
  ],
  "goals": [
    {
      "id": "goal-001",
      "name": "Child Education",
      "targetAmount": 5000000,
      "targetYear": 2040,
      "inflationCategoryId": "education",
      "priority": "MUST_HAVE",
      "timeline": "LONG_TERM",
      "isEnabled": true
    }
  ],
  "snapshotTimestamp": 1704067200000
}
```

### Field Descriptions

#### UserProfile
- `currentAge`: Current age in years
- `retirementAge`: Planned retirement age
- `lifeExpectancy`: Expected lifespan for planning
- `currentMonthlyExpenses`: Monthly living expenses (today's value)
- `expenseInflationCategoryId`: Inflation category ID for expense growth
- `postRetirementGrowthRate`: Single blended growth rate for all corpus post-retirement (%)

#### InflationCategory
- `id`: Unique identifier
- `name`: Display name
- `rate`: Annual inflation percentage

#### InvestmentCategory
- `id`: Unique identifier
- `name`: Display name (e.g., "Equity", "Debt")
- `preRetirementXIRR`: Expected annual return before retirement (%)
- `postRetirementXIRR`: Expected annual return after retirement (%)

#### Investment
- `id`: Unique identifier
- `name`: Investment name
- `currentValue`: Current worth in rupees
- `categoryId`: Reference to InvestmentCategory
- `actualXIRR`: Historical return achieved (%)
- `isEnabled`: Include in calculations (true/false)

#### OngoingContribution
- `id`: Unique identifier
- `name`: Contribution name (e.g., "Equity SIP")
- `amount`: Contribution amount per period
- `frequency`: MONTHLY, QUARTERLY, or YEARLY
- `categoryId`: Reference to InvestmentCategory
- `durationYears`: Number of years to continue (null = until retirement)
- `stepUpPercentage`: Annual increase percentage (0 = no step-up)
- `isEnabled`: Include in calculations (true/false)

#### FinancialGoal
- `id`: Unique identifier
- `name`: Goal name
- `targetAmount`: Required amount in today's value
- `targetYear`: Year when goal will be realized
- `inflationCategoryId`: Reference to InflationCategory
- `priority`: MUST_HAVE or GOOD_TO_HAVE
- `timeline`: SHORT_TERM (0-5y), MEDIUM_TERM (5-10y), or LONG_TERM (10y+)
- `isEnabled`: Include in calculations (true/false)

#### FinancialPlanData
- `snapshotTimestamp`: Unix timestamp (milliseconds) when snapshot was created
- Used for year-over-year comparison and versioning

## Roadmap / Upcoming Features

### Planned Enhancements

#### 1. Enhanced Color Coding & Visual Indicators
- **Goal**: Make financial health immediately visible through color indicators
- **Scope**: Apply color coding across all screens (Dashboard, Goals, Analysis, Portfolio, Contributions)
- **Features**:
  - Color-coded cards and borders based on funding status (green = funded, yellow = partial, red = unfunded)
  - Visual progress bars for goal funding percentage
  - Chart color zones based on corpus trajectory thresholds
  - Performance-based color coding for investments (high/medium/low XIRR)
  - Status badges and icons (✓ ✗ ⚠) throughout the UI

#### 2. Minimum SIP Calculator (Actionable Insights)
- **Goal**: Show exactly how much additional SIP is needed to get back on track
- **Scope**: Calculate minimum monthly SIP required to fund all unfunded goals
- **Features**:
  - "Action Required" card on Dashboard when off-track
  - Display minimum monthly SIP amount with target investment category
  - Per-goal SIP recommendations
  - "Add Recommended SIP" quick action button
  - Reverse annuity calculation based on shortfall and time horizon

#### 3. Rich Export Metadata
- **Goal**: Add comprehensive metadata to exported JSON snapshots
- **Scope**: Enhance export/import functionality with versioning and context
- **Features**:
  - Timestamp and date formatting (auto-populated)
  - User-provided snapshot labels (e.g., "Annual Review 2025")
  - Optional user notes field
  - Snapshot metrics captured at export (corpus, goals funded/unfunded)
  - App version tracking
  - Import preview with metadata before accepting
  - Smart filename generation: `financial_plan_YYYY-MM-DD.json`

#### 4. Year-over-Year Comparison
- **Goal**: Load multiple snapshots and visualize financial progress over time
- **Scope**: Compare historical snapshots to track improvement/degradation
- **Features**:
  - Load comparison snapshot in Analysis screen
  - Side-by-side corpus chart visualization (current vs historical)
  - Delta metrics:
    - Corpus growth (absolute & percentage change)
    - Goal progress (new goals, completed goals, funding improvements)
    - Net worth analysis (investment growth vs new contributions)
    - Retirement readiness trend
  - Goal-by-goal comparison showing funding improvement/degradation
  - Comparison summary dashboard
  - Toggle between "Current" | "Comparison" | "Side-by-Side" views

#### 5. Shortfall Recovery Tips
- **Goal**: Provide actionable suggestions when there's a shortage of funds
- **Scope**: Analysis screen shows a collapsible card with recovery strategies
- **Features**:
  - Automatically displayed when total shortfall exists
  - Collapsible/expandable card to save screen space
  - Personalized tips based on the specific situation:
    - Shift to aggressive investments (e.g., increase equity allocation)
    - Push back optional (GOOD_TO_HAVE) goals by a few years to allow compounding
    - Increase monthly SIP contributions
    - Reduce target amounts for flexible goals
    - Extend retirement age to allow more accumulation time
  - Each tip includes estimated impact on shortfall
  - Quick action links to relevant screens (e.g., "Review Goals" → Goals screen)

#### 6. Information & Support Sections
- **Goal**: Provide comprehensive user information and guidance
- **Scope**: New dedicated sections accessible from main navigation or settings
- **Features**:
  - **FAQ Section**:
    - Common questions about calculations and assumptions
    - Explanation of financial terms (XIRR, corpus, inflation adjustment, etc.)
    - How-to guides for common tasks
    - Troubleshooting tips
  - **Upcoming Features**:
    - Roadmap preview with planned enhancements
    - Timeline for upcoming releases
    - User-requested features under consideration
    - Beta feature opt-in
  - **Contact Section**:
    - Feedback form or email contact
    - Bug reporting guidelines
    - Feature request submission
    - Community/support links
  - **Disclaimer**:
    - Financial planning disclaimer (not professional advice)
    - Calculation assumptions and limitations
    - Risk warnings
    - Privacy policy (data stored locally)
    - Terms of use

## Deployment

Ready to deploy your Financial Planner to production?

### Quick Start (Free Hosting)
See [DEPLOYMENT.md](DEPLOYMENT.md) for basic deployment to free hosting platforms.

### Custom Domain Setup
Want to use your own domain (e.g., `financialplanner.com`)? See [CUSTOM_DOMAIN_DEPLOYMENT.md](CUSTOM_DOMAIN_DEPLOYMENT.md) for detailed guides:

- **Platform comparisons** with custom domain support
- **Step-by-step DNS configuration** for each platform
- **Domain registrar recommendations** (from $10/year)
- **Subdomain vs apex domain** setup
- **SSL/HTTPS** certificate configuration (free & automatic)
- **Complete cost breakdown** ($10-15/year total)

**Recommended:** Cloudflare Pages + Cloudflare Registrar for easiest setup and best performance.

All hosting platforms are **100% FREE** - you only pay for the domain name (~$10-15/year).

## Documentation

See [CLAUDE.md](CLAUDE.md) for detailed technical documentation, including:
- Complete feature list
- Acceptance criteria
- Implementation progress
- Architecture details
- Development guidelines

## License

This project is private and not licensed for public use.
