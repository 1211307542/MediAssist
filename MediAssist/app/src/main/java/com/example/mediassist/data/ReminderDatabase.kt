package com.example.mediassist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mediassist.data.model.Reminder
import com.example.mediassist.data.local.MedicationHistoryDao
import com.example.mediassist.data.model.MedicationHistory
import com.example.mediassist.data.model.ReminderTime
import com.example.mediassist.data.ReminderTimeDao

@Database(
    entities = [Reminder::class, ReminderTime::class, MedicationHistory::class],
    version = 3
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun medicationHistoryDao(): MedicationHistoryDao
    abstract fun reminderTimeDao(): ReminderTimeDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
