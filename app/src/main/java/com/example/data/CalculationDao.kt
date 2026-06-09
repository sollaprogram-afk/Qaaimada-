package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(record: CalculationRecord): Long

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Int)

    @Query("DELETE FROM calculations")
    suspend fun clearAllCalculations()
}
