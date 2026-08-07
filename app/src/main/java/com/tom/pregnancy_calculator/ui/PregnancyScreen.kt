package com.tom.pregnancy_calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tom.pregnancy_calculator.R
import com.tom.pregnancy_calculator.logic.PregnancyCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyCalculatorScreen(
    viewModel: PregnancyViewModel = viewModel(),
    onOpenSettings: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- SETUP CARD ---
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calculator_setup),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MethodSelector(
                                currentMethod = viewModel.selectedMethod,
                                methods = viewModel.methods,
                                onMethodSelected = { viewModel.onMethodSelected(it) }
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.toggleDatePicker(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.select_date))
                        }
                    }
                }
            }

            // --- RESULTS AND PROGRESS ---
            viewModel.selectedDate?.let { date ->
                val effectiveLmp = PregnancyCalculator.calculateEffectiveLmp(date, viewModel.selectedMethod)
                PregnancyResultsDisplay(effectiveLmp, viewModel.selectedMethod)
            }

            if (viewModel.showDatePicker) {
                PregnancyDatePickerDialog(
                    onDateSelected = { viewModel.onDateSelected(it) },
                    onDismiss = { viewModel.toggleDatePicker(false) }
                )
            }
        }
    }
}

@Composable
fun PregnancyResultsDisplay(effectiveLmp: LocalDate, method: String) {
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    val (weeks, days) = PregnancyCalculator.getGestation(effectiveLmp)
    val milestones = PregnancyCalculator.getMilestones(effectiveLmp)
    
    // Progress calculation (based on 280 days)
    val totalDays = ChronoUnit.DAYS.between(effectiveLmp, LocalDate.now())
    val progress = (totalDays.toFloat() / 280f).coerceIn(0f, 1f)
    val percentage = progress * 100

    // --- MILESTONES CARD ---
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.upcoming_milestones),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            milestones.forEach { (label, date) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = date.format(formatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // --- GESTATION PROGRESS CARD ---
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.gestation_progress),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.current_gestation, weeks, days),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.gestation_percentage, percentage),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // --- INFO CARD ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            InfoRow(label = stringResource(R.string.calculation_basis), value = method)
            InfoRow(label = stringResource(R.string.effective_lmp), value = effectiveLmp.format(formatter))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MethodSelector(
    currentMethod: String,
    methods: List<String>,
    onMethodSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.method_label, currentMethod))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
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
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
