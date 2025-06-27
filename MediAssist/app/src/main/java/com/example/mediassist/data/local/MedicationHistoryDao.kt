package com.example.mediassist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediassist.data.model.MedicationHistory

@Dao
interface MedicationHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MedicationHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<MedicationHistory>)

    @Query("SELECT * FROM medication_history ORDER BY startDate DESC")
    suspend fun getAllHistory(): List<MedicationHistory>

}
