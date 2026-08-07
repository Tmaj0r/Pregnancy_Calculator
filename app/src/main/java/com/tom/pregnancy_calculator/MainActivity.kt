package com.tom.pregnancy_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tom.pregnancy_calculator.ui.PregnancyCalculatorScreen
import com.tom.pregnancy_calculator.ui.SettingsScreen
import com.tom.pregnancy_calculator.ui.SettingsViewModel
import com.tom.pregnancy_calculator.ui.theme.Pregnancy_CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            
            // Use the preference from ViewModel (defaults to false/light)
            val useDarkMode = settingsViewModel.isDarkMode

            var currentScreen by remember { mutableStateOf("calculator") }

            Pregnancy_CalculatorTheme(darkTheme = useDarkMode) {
                if (currentScreen == "calculator") {
                    PregnancyCalculatorScreen(
                        onOpenSettings = { currentScreen = "settings" }
                    )
                } else {
                    SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                }
            }
        }
    }
}
