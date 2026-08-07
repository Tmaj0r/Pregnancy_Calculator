package com.tom.pregnancy_calculator.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class PregnancyViewModel : ViewModel() {
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set
    
    var selectedMethod by mutableStateOf("LMP")
        private set
    
    var showDatePicker by mutableStateOf(false)

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
        showDatePicker = false
    }

    fun onMethodSelected(method: String) {
        selectedMethod = method
    }
    
    fun toggleDatePicker(show: Boolean) {
        showDatePicker = show
    }

    val methods = listOf("LMP", "OPK", "TVOR/IUI", "Day 3", "Day 5")
}
