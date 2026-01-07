package com.tom.pregnancy_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tom.pregnancy_calculator.ui.theme.Pregnancy_CalculatorTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pregnancy_CalculatorTheme {
                PregnancyDatePicker()
            }
        }
    }
}
//TO-DO Add gestation. How many days are there from LMP to current date.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDatePicker() {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Formatter for Month, Day, Year
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val methods = listOf("LMP", "OPK", "TVOR/IUI", "Day 3", "Day 5")
    var selectedMethod by remember { mutableStateOf(methods[0]) }

    // Map the method to the day adjustment
    val adjustment = when (selectedMethod) {
        "OPK" -> -13L
        "TVOR/IUI" -> -14L
        "Day 3" -> -17L
        "Day 5" -> -19L
        else -> 0L
    }

    val milestones = listOf(
        "Week Four" to 28L,
        "Week Five + Two Days" to 37L,
        "Week Seven" to 49L,
        "Week Nine" to 63L,
        "Week Eleven" to 77L,
        "Estimated Due Date" to 280L
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Display Results
        selectedDate?.let { date ->
            val adjustedLmp = date.plusDays(adjustment)

            Text(text = "Calculation Basis: $selectedMethod", style = MaterialTheme.typography.labelLarge)

            // Applied Formatter here
            Text(text = "Effective LMP: ${adjustedLmp.format(formatter)}", color = MaterialTheme.colorScheme.primary)

            milestones.forEach { (label, days) ->
                val milestoneDate = adjustedLmp.plusDays(days)
                Text(
                    // Applied Formatter here
                    text = "$label: ${milestoneDate.format(formatter)}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Dropdown Selector
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            OutlinedButton(onClick = { expanded = true }) {
                Text("Method: $selectedMethod")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                methods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            selectedMethod = method
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Date Picker Button
        Button(onClick = { showDatePicker = true }) {
            Text("Select Date")
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // FIX: Use ZoneOffset.UTC to prevent the "one day off" bug
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}
