package com.example.mediassist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mediassist.data.model.Reminder

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder?
}

