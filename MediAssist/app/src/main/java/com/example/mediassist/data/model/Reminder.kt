package com.example.mediassist.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationName: String,
    val dosageAmount: String,
    val startDate: String,
    val duration: String
) : Parcelable
