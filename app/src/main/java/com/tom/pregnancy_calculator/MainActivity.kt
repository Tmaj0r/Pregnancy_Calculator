package com.tom.pregnancy_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // State Management
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedMethod by remember { mutableStateOf("LMP") }
    var showDatePicker by remember { mutableStateOf(false) }

    val methods = listOf("LMP", "OPK", "TVOR/IUI", "Day 3", "Day 5")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Results Area
        selectedDate?.let { date ->
            val effectiveLmp = calculateEffectiveLmp(date, selectedMethod)
            PregnancyResultsDisplay(effectiveLmp, selectedMethod)
        }

        // 2. Method Selector (Dropdown)
        MethodSelector(
            currentMethod = selectedMethod,
            methods = methods,
            onMethodSelected = { selectedMethod = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Date Selection Trigger
        Button(onClick = { showDatePicker = true }) {
            Text("Select Date")
        }

        // 4. Date Picker Dialog
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

    // Calculate Gestation (LMP to Today)
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(effectiveLmp, today)
    val weeks = totalDays / 7
    val days = totalDays % 7

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(fontSize = 15.sp, text = "Calculation Basis: $method", style = MaterialTheme.typography.labelLarge)
        Text(fontSize = 15.sp, text = "Effective LMP: ${effectiveLmp.format(formatter)}", color = MaterialTheme.colorScheme.primary)

        // Gestation Display
        Text(
            fontSize = 15.sp,
            text = "Current Gestation: $weeks Weeks, $days Days",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        milestones.forEach { (label, dayOffset) ->
            val milestoneDate = effectiveLmp.plusDays(dayOffset)
            Text(
                fontSize = 15.sp,
                text = "$label: ${milestoneDate.format(formatter)}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MethodSelector(currentMethod: String, methods: List<String>, onMethodSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Method: $currentMethod")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
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

/**
 * Pure logic function to calculate the effective LMP based on method
 */
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
