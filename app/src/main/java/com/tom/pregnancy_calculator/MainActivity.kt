package com.tom.pregnancy_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tom.pregnancy_calculator.ui.theme.Pregnancy_CalculatorTheme
import java.time.LocalDate

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDatePicker() {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val methods = listOf("LMP", "OPK", "TVOR", "D3", "D5")
    var selectedMethod by remember { mutableStateOf(methods[0]) }

    // Map the method to the day adjustment
    val adjustment = when (selectedMethod) {
        "OPK" -> -13L
        "TVOR" -> -14L
        "D3" -> -17L
        "D5" -> -19L
        else -> 0L // LMP is the baseline
    }

    // Milestones definition (Condensed)
    val milestones = listOf(
        "Week Four" to 28L,
        "Week Five" to 35L,
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
            // Adjust the date based on the method chosen
            val adjustedLmp = date.plusDays(adjustment)

            Text(text = "Calculation Basis: $selectedMethod", style = MaterialTheme.typography.labelLarge)
            Text(text = "Effective LMP: $adjustedLmp", color = MaterialTheme.colorScheme.primary)

            milestones.forEach { (label, days) ->
                Text(
                    text = "$label: ${adjustedLmp.plusDays(days)}",
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
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()

                        }
                        showDatePicker = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}
