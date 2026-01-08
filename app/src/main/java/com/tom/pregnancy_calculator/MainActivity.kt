package com.tom.pregnancy_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tom.pregnancy_calculator.ui.theme.Pregnancy_CalculatorTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pregnancy_CalculatorTheme {
                PregnancyCalculatorScreen()
            }
        }
    }
}

@Composable
fun PregnancyCalculatorScreen() {
    // --- STATE MANAGEMENT ---
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedMethod by remember { mutableStateOf("LMP") }
    var showDatePicker by remember { mutableStateOf(false) }

    val methods = listOf("LMP", "OPK", "TVOR/IUI", "Day 3", "Day 5")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- UI COMPONENTS AT THE BOTTOM ---
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            selectedDate?.let { date ->
                val effectiveLmp = calculateEffectiveLmp(date, selectedMethod)
                PregnancyResultsDisplay(effectiveLmp, selectedMethod)
            }

            MethodSelector(
                currentMethod = selectedMethod,
                methods = methods,
                onMethodSelected = { selectedMethod = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { showDatePicker = true }) {
                Text("Select Date") // Removed manual font size
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showDatePicker) {
            PregnancyDatePickerDialog(
                onDateSelected = {
                    selectedDate = it
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

fun calculateEffectiveLmp(inputDate: LocalDate, method: String): LocalDate {
    val adjustment = when (method) {
        "OPK" -> -13L
        "TVOR/IUI" -> -14L
        "Day 3" -> -17L
        "Day 5" -> -19L
        else -> 0L
    }
    return inputDate.plusDays(adjustment)
}

@Composable
fun PregnancyResultsDisplay(effectiveLmp: LocalDate, method: String) {
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    val milestones = listOf(
        "Week Four" to 28L,
        "Week Five + Two Days" to 37L,
        "Week Seven" to 49L,
        "Week Nine" to 63L,
        "Week Eleven" to 77L,
        "Estimated Due Date" to 280L
    )

    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(effectiveLmp, today)
    val weeks = totalDays / 7
    val days = totalDays % 7

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AutoSizeText(
            text = "Calculation Basis: $method",
            style = MaterialTheme.typography.labelLarge
        )
        AutoSizeText(
            text = "Effective LMP: ${effectiveLmp.format(formatter)}",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary)
        )

        AutoSizeText(
            text = "Current Gestation: $weeks Weeks, $days Days",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        milestones.forEach { (label, dayOffset) ->
            val milestoneDate = effectiveLmp.plusDays(dayOffset)
            AutoSizeText(
                text = "$label: ${milestoneDate.format(formatter)}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MethodSelector(
    currentMethod: String,
    methods: List<String>,
    onMethodSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Method: $currentMethod") // Manual size removed
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) }, // Manual size removed
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDatePickerDialog(onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    onDateSelected(date)
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    var scaledFontSize by remember(text) { mutableStateOf(style.fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        softWrap = false,
        // Apply the original style but override the font size
        style = style.copy(fontSize = scaledFontSize),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                // If it overflows, shrink the font size
                scaledFontSize *= 0.9f
            } else {
                // Once it fits, mark it ready to draw
                readyToDraw = true
            }
        },
        // Use the original style's color but control visibility with alpha
        color = style.color.copy(alpha = if (readyToDraw) 1f else 0f)
    )
}