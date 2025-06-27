package com.example.mediassist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mediassist.data.model.ReminderTime

@Dao
interface ReminderTimeDao {
    @Query("SELECT * FROM reminder_times WHERE reminderId = :reminderId")
    suspend fun getReminderTimes(reminderId: Int): List<ReminderTime>

    @Insert
    suspend fun insertReminderTime(reminderTime: ReminderTime)

    @Insert
    suspend fun insertReminderTimes(times: List<ReminderTime>)

    @Query("DELETE FROM reminder_times WHERE reminderId = :reminderId")
    suspend fun deleteReminderTimesByReminderId(reminderId: Int)
}
