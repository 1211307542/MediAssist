package com.example.mediassist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mediassist.data.model.Reminder
import com.example.mediassist.data.model.MedicationHistory

@Database(
    entities = [Reminder::class, MedicationHistory::class],
    version = 2
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun getReminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: ReminderDatabase? = null

        fun getInstance(context: Context): ReminderDatabase {
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

