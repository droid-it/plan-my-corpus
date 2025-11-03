package ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Format number to Indian Rupee system (lakhs and crores)
 * Examples: 1,00,000 (1L), 10,00,000 (10L), 1,00,00,000 (1Cr)
 */
fun formatIndianCurrency(value: String): String {
    // Remove all non-digit characters
    val digits = value.filter { it.isDigit() }
    if (digits.isEmpty()) return ""

    // Reverse the string to make comma placement easier
    val reversed = digits.reversed()
    val formatted = buildString {
        reversed.forEachIndexed { index, char ->
            if (index == 3 || (index > 3 && (index - 3) % 2 == 0)) {
                append(',')
            }
            append(char)
        }
    }

    return formatted.reversed()
}

/**
 * Remove commas from formatted currency string and convert to clean number string
 */
fun unformatCurrency(value: String): String {
    // Remove commas and any decimal point with trailing zeros
    val cleaned = value.filter { it.isDigit() || it == '.' }
    // If it contains a decimal, convert to Double and back to remove .0
    if ('.' in cleaned) {
        val doubleValue = cleaned.toDoubleOrNull() ?: return value.filter { it.isDigit() }
        // Convert to long to remove decimal part (we only deal with whole rupees)
        return doubleValue.toLong().toString()
    }
    return cleaned
}

@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: @Composable (() -> Unit)? = null,
    prefix: String = "₹"
) {
    var displayValue by remember(value) {
        // Clean the value first to handle "200000.0" -> "200000"
        val cleanedValue = unformatCurrency(value)
        mutableStateOf(if (cleanedValue.isEmpty() || cleanedValue == "0") "" else formatIndianCurrency(cleanedValue))
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = { newValue ->
            // Remove commas and non-digits
            val unformatted = unformatCurrency(newValue)

            // Update the actual value
            onValueChange(unformatted)

            // Format for display
            displayValue = if (unformatted.isEmpty()) "" else formatIndianCurrency(unformatted)
        },
        label = { Text(label) },
        prefix = if (displayValue.isNotEmpty()) { { Text(prefix) } } else null,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
