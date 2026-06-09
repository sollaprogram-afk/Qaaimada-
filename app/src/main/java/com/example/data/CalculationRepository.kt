package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val allCalculations: Flow<List<CalculationRecord>> = calculationDao.getAllCalculations()

    suspend fun insert(record: CalculationRecord): Long {
        return calculationDao.insertCalculation(record)
    }

    suspend fun deleteById(id: Int) {
        calculationDao.deleteCalculationById(id)
    }

    suspend fun clearAll() {
        calculationDao.clearAllCalculations()
    }
}
