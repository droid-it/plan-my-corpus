package ui.screens

import AppState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max

@Composable
fun AnalysisScreen(appState: AppState) {
    val analysis = appState.analysis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Corpus Projection Chart
        if (analysis.yearlyProjections.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Corpus Projection Over Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    CorpusProjectionChart(
                        projections = analysis.yearlyProjections,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Debug: Show calculation breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Calculation Breakdown",
                    style = MaterialTheme.typography.titleMedium
                )

                val currentInvestmentsValue = appState.data.investments.sumOf { it.currentValue }
                Text("Current investments: ₹${formatAmount(currentInvestmentsValue)}")

                val monthlyContributions = appState.data.ongoingContributions.sumOf {
                    when (it.frequency) {
                        model.ContributionFrequency.MONTHLY -> it.amount
                        model.ContributionFrequency.QUARTERLY -> it.amount / 3
                        model.ContributionFrequency.YEARLY -> it.amount / 12
                    }
                }
                Text("Monthly contributions: ₹${formatAmount(monthlyContributions)}")

                val retirementYear = analysis.currentYear +
                    (appState.data.userProfile.retirementAge - appState.data.userProfile.currentAge)
                val yearsToRetirement = retirementYear - analysis.currentYear

                Text("Years to retirement: $yearsToRetirement")
                Text("Total goals: ${appState.data.goals.size}")
                Text("Total goals amount (today): ₹${formatAmount(appState.data.goals.sumOf { it.targetAmount })}")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "At Retirement Year ($retirementYear):",
                    style = MaterialTheme.typography.titleSmall
                )
                Text("Projected corpus: ₹${formatAmount(analysis.corpusHealth.totalCorpusAtRetirement)}")
                Text("Required for goals: ₹${formatAmount(analysis.corpusHealth.totalAllGoalsRequired)}")
                Text("Required for retirement: ₹${formatAmount(analysis.corpusHealth.retirementCorpusRequired)}")
                Text("Total required: ₹${formatAmount(analysis.corpusHealth.totalRequiredIncludingRetirement)}")

                val gap = analysis.corpusHealth.totalRequiredIncludingRetirement - analysis.corpusHealth.totalCorpusAtRetirement
                if (gap > 0) {
                    Text(
                        "Shortfall: ₹${formatAmount(gap)}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                } else {
                    Text(
                        "Surplus: ₹${formatAmount(-gap)}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        Text(
            text = "Goal-by-Goal Breakdown",
            style = MaterialTheme.typography.titleMedium
        )

        // Sort goals by target year (sooner to later)
        val sortedGoalAnalyses = analysis.goalAnalyses.sortedBy { it.goal.targetYear }

        // Calculate retirement year
        val retirementYear = analysis.currentYear +
            (appState.data.userProfile.retirementAge - appState.data.userProfile.currentAge)

        var retirementShown = false

        sortedGoalAnalyses.forEach { goalAnalysis ->
            // Add retirement card before the first goal that's after retirement
            if (!retirementShown && goalAnalysis.goal.targetYear >= retirementYear) {
                RetirementCard(
                    retirementYear = retirementYear,
                    retirementAge = appState.data.userProfile.retirementAge,
                    corpusAtRetirement = analysis.corpusHealth.totalCorpusAtRetirement,
                    retirementCorpusRequired = analysis.corpusHealth.retirementCorpusRequired,
                    canMeetRetirement = analysis.corpusHealth.canMeetRetirement
                )
                retirementShown = true
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (goalAnalysis.isFunded)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    goalAnalysis.goal.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            // Show recurring indicator if frequency is set (even if isRecurring is false due to occurrence expansion)
                            if (goalAnalysis.goal.recurringFrequencyMonths > 0 &&
                                (goalAnalysis.goal.recurringStartAge != null || goalAnalysis.goal.isRecurring)) {
                                Text(
                                    "Recurring occurrence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 28.dp)
                                )
                            }
                        }
                        Text(
                            "Year: ${goalAnalysis.goal.targetYear}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Target (inflated): ₹${formatAmount(goalAnalysis.inflationAdjustedAmount)}")
                    Text("Corpus at goal year: ₹${formatAmount(goalAnalysis.corpusAtGoalYear)}")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (goalAnalysis.isFunded) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (goalAnalysis.isFunded)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (goalAnalysis.isFunded) "Funded"
                            else "Shortfall: ₹${formatAmount(goalAnalysis.shortfall)}"
                        )
                    }
                }
            }
        }

        // If retirement hasn't been shown yet (all goals are before retirement)
        if (!retirementShown) {
            RetirementCard(
                retirementYear = retirementYear,
                retirementAge = appState.data.userProfile.retirementAge,
                corpusAtRetirement = analysis.corpusHealth.totalCorpusAtRetirement,
                retirementCorpusRequired = analysis.corpusHealth.retirementCorpusRequired,
                canMeetRetirement = analysis.corpusHealth.canMeetRetirement
            )
        }
    }
}

@Composable
fun RetirementCard(
    retirementYear: Int,
    retirementAge: Int,
    corpusAtRetirement: Double,
    retirementCorpusRequired: Double,
    canMeetRetirement: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canMeetRetirement)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BeachAccess,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Retirement",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            "MILESTONE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
                Text(
                    "Year: $retirementYear",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Required corpus: ₹${formatAmount(retirementCorpusRequired)}")
            Text("Corpus at retirement: ₹${formatAmount(corpusAtRetirement)}")
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (canMeetRetirement) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (canMeetRetirement)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Text(
                    if (canMeetRetirement)
                        "Retirement corpus funded"
                    else
                        "Shortfall: ₹${formatAmount(retirementCorpusRequired - corpusAtRetirement)}"
                )
            }
        }
    }
}

@Composable
fun CorpusProjectionChart(
    projections: List<calculation.YearProjection>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error
    val textMeasurer = rememberTextMeasurer()

    var selectedProjection by remember { mutableStateOf<calculation.YearProjection?>(null) }

    Column(modifier = modifier.height(350.dp)) {
        // Selected value display
        if (selectedProjection != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Year ${selectedProjection!!.year} (Age ${selectedProjection!!.age})",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Corpus: ₹${formatAmount(selectedProjection!!.totalCorpus)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryColor
                    )
                    if (selectedProjection!!.goalsMaturing.isNotEmpty()) {
                        Text(
                            "Goals maturing this year:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        selectedProjection!!.goalsMaturing.forEach { goalAnalysis ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "• ${goalAnalysis.goal.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "₹${formatAmount(goalAnalysis.inflationAdjustedAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryColor
                                )
                            }
                        }
                        val totalGoalAmount = selectedProjection!!.goalsMaturing.sumOf { it.inflationAdjustedAmount }
                        Text(
                            "Total withdrawals: ₹${formatAmount(totalGoalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = errorColor,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(projections) {
                    detectTapGestures { offset ->
                        // Find closest projection to tap
                        val width = size.width
                        val padding = 40f
                        val minYear = projections.first().year
                        val maxYear = projections.last().year
                        val range = maxYear - minYear

                        if (range > 0) {
                            val clickedYear = minYear + ((offset.x - padding) / (width - 2 * padding) * range).toInt()
                            selectedProjection = projections.find { it.year == clickedYear }
                                ?: projections.minByOrNull { kotlin.math.abs(it.year - clickedYear) }
                        }
                    }
                }
        ) {
            if (projections.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val padding = 40f

            // Calculate data bounds
            val minYear = projections.first().year
            val maxYear = projections.last().year
            val maxCorpus = projections.maxOf { it.totalCorpus }
            val minCorpus = 0.0

            // Scale functions
            fun scaleX(year: Int): Float {
                val range = maxYear - minYear
                return if (range > 0) {
                    padding + (year - minYear).toFloat() / range * (width - 2 * padding)
                } else {
                    width / 2
                }
            }

            fun scaleY(corpus: Double): Float {
                val range = maxCorpus - minCorpus
                return if (range > 0) {
                    height - padding - (corpus - minCorpus).toFloat() / range.toFloat() * (height - 2 * padding)
                } else {
                    height / 2
                }
            }

            // Draw axes
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.3f),
                start = Offset(padding, height - padding),
                end = Offset(width - padding, height - padding),
                strokeWidth = 2f
            )
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.3f),
                start = Offset(padding, padding),
                end = Offset(padding, height - padding),
                strokeWidth = 2f
            )

            // Draw axis labels
            val textStyle = TextStyle(
                color = onSurfaceColor.copy(alpha = 0.7f),
                fontSize = 10.sp
            )

            // X-axis label (Year)
            val xAxisLabel = "Year"
            val xLabelLayout = textMeasurer.measure(xAxisLabel, textStyle)
            drawText(
                textLayoutResult = xLabelLayout,
                topLeft = Offset(
                    (width - xLabelLayout.size.width.toFloat()) / 2,
                    height - 10f
                )
            )

            // Y-axis label (Corpus)
            val yAxisLabel = "Corpus (₹ Cr)"
            val yLabelLayout = textMeasurer.measure(yAxisLabel, textStyle)
            drawText(
                textLayoutResult = yLabelLayout,
                topLeft = Offset(
                    5f,
                    (height - yLabelLayout.size.height.toFloat()) / 2
                )
            )

            // Draw X-axis tick labels (sample years)
            val numXTicks = 5
            val yearInterval = (maxYear - minYear) / (numXTicks - 1)
            for (i in 0 until numXTicks) {
                val year = minYear + i * yearInterval
                val x = scaleX(year)
                val yearLabel = year.toString()
                val yearLayout = textMeasurer.measure(yearLabel, textStyle)
                drawText(
                    textLayoutResult = yearLayout,
                    topLeft = Offset(
                        x - yearLayout.size.width.toFloat() / 2,
                        height - padding + 5f
                    )
                )
            }

            // Draw Y-axis tick labels (corpus values in crores)
            val numYTicks = 4
            val corpusInterval = maxCorpus / (numYTicks - 1)
            for (i in 0 until numYTicks) {
                val corpus = i * corpusInterval
                val y = scaleY(corpus)
                // Format in crores with 1 decimal place
                val crores = corpus / 10000000
                val roundedCrores = (crores * 10).toLong() / 10.0
                val corpusLabel = "$roundedCrores"
                val corpusLayout = textMeasurer.measure(corpusLabel, textStyle)
                drawText(
                    textLayoutResult = corpusLayout,
                    topLeft = Offset(
                        padding - corpusLayout.size.width.toFloat() - 5f,
                        y - corpusLayout.size.height.toFloat() / 2
                    )
                )
            }

            // Draw corpus line
            val path = Path()
            projections.forEachIndexed { index, projection ->
                val x = scaleX(projection.year)
                val y = scaleY(projection.totalCorpus)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f)
            )

            // Draw withdrawal indicators for goal years
            projections.forEach { projection ->
                if (projection.goalsMaturing.isNotEmpty()) {
                    val x = scaleX(projection.year)
                    val y = scaleY(projection.totalCorpus)
                    val totalWithdrawal = projection.goalsMaturing.sumOf { it.inflationAdjustedAmount }

                    // Draw a vertical dashed line down from the point
                    drawLine(
                        color = errorColor,
                        start = Offset(x, y),
                        end = Offset(x, height - padding),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                }
            }

            // Draw points for all years
            projections.forEach { projection ->
                val x = scaleX(projection.year)
                val y = scaleY(projection.totalCorpus)

                // Highlight selected projection
                val isSelected = selectedProjection?.year == projection.year

                // Draw larger dots for years with goals
                if (projection.goalsMaturing.isNotEmpty()) {
                    drawCircle(
                        color = secondaryColor,
                        radius = if (isSelected) 12f else 8f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 5f else 3f,
                        center = Offset(x, y)
                    )
                } else {
                    // Small dots for regular years
                    drawCircle(
                        color = primaryColor,
                        radius = if (isSelected) 8f else 4f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
