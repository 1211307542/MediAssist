package com.example.mediassist.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_history")
data class MedicationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationName: String,
    val dosageAmount: String,
    val startDate: String,
    val duration: String,
    val timeInMillis: Long
) {
    fun getRemainingDays(): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(startDate, formatter)
            val durationInt = duration.toIntOrNull() ?: 0
            val end = start.plusDays(durationInt.toLong())
            val today = LocalDate.now()

            when {
                today.isBefore(start) -> durationInt
                today.isAfter(end) -> 0
                else -> ChronoUnit.DAYS.between(today, end).toInt()
            }
        } catch (e: Exception) {
            -1
        }
    }

    fun getReadableTime(): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(timeInMillis))
    }

    fun getStatus(): String {
        return when (val remaining = getRemainingDays()) {
            -1 -> "Unknown"
            0 -> "Completed"
            else -> "In Progress ($remaining days left)"
        }
    }
}
