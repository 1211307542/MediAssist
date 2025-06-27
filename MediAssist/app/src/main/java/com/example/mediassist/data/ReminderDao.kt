package com.example.mediassist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.mediassist.data.model.Reminder

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Insert
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder) // ✅ Added for updating reminders

    @Delete
    suspend fun deleteReminder(reminder: Reminder) // ✅ Added for deleting reminders
}
