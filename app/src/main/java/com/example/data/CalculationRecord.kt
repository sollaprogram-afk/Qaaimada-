package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val receivedProduct: Int,
    val unsoldProduct: Int,
    val productSold: Int,
    val quantitiesSerialized: String, // comma-separated values (14 values)
    val pricesSerialized: String,     // comma-separated values (14 values)
    val totalQuantitySold: Int,
    val grandTotalSales: Double,
    val commission: Double,
    val netTotal: Double,
    val totalPayment: Double,
    val netBalance: Double,
    val notes: String = ""
) {
    fun getQuantities(): List<Int> {
        if (quantitiesSerialized.isEmpty()) return List(14) { 0 }
        val list = quantitiesSerialized.split(",").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        return if (list.size >= 14) list.take(14) else list + List(14 - list.size) { 0 }
    }

    fun getPrices(): List<Double> {
        if (pricesSerialized.isEmpty()) return List(14) { 0.0 }
        val list = pricesSerialized.split(",").filter { it.isNotEmpty() }.map { it.toDoubleOrNull() ?: 0.0 }
        return if (list.size >= 14) list.take(14) else list + List(14 - list.size) { 0.0 }
    }
}
