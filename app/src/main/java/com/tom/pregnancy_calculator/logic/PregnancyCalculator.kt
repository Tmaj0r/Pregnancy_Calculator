package com.tom.pregnancy_calculator.logic

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object PregnancyCalculator {
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

    fun getGestation(effectiveLmp: LocalDate): Pair<Long, Long> {
        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(effectiveLmp, today)
        val weeks = totalDays / 7
        val days = totalDays % 7
        return Pair(weeks, days)
    }

    fun getMilestones(effectiveLmp: LocalDate): List<Pair<String, LocalDate>> {
        val milestones = listOf(
            "Four Weeks" to 28L,
            "Five Weeks + Two Days" to 37L,
            "Seven Weeks" to 49L,
            "Nine Weeks" to 63L,
            "Eleven Weeks" to 77L,
            "Estimated Due Date" to 280L
        )
        return milestones.map { (label, dayOffset) ->
            label to effectiveLmp.plusDays(dayOffset)
        }
    }
}
