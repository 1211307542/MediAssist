package com.example.mediassist.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_times",
    foreignKeys = [
        ForeignKey(
            entity = Reminder::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderTime(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val timeInMillis: Long
)
