package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalculationRecord
import com.example.data.CalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RowData(
    val quantity: String = "",
    val price: String = ""
) {
    val amount: Double
        get() {
            val q = quantity.toDoubleOrNull() ?: 0.0
            val p = price.toDoubleOrNull() ?: 0.0
            return q * p
        }
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CalculationRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalculationRepository(db.calculationDao())
    }

    // Window 1 Inputs
    val totalReceivedProduct = MutableStateFlow("")
    val totalUnsoldProduct = MutableStateFlow("")

    // Window 2 Inputs (14 rows)
    val rows = MutableStateFlow(List(14) { RowData() })

    // Window 3 Inputs
    val totalPayment = MutableStateFlow("")

    // General note for identification of calculations
    val notes = MutableStateFlow("Cage-Weyne B72")
    val isEditingRecordId = MutableStateFlow<Int?>(null)

    // Appearance State
    val isDarkMode = MutableStateFlow(false)

    // Search query for previous calculations
    val searchQuery = MutableStateFlow("")

    // Window 1 Output: Total Product Sold = Total Received - Total Unsold
    val totalProductSold: StateFlow<Int> = combine(totalReceivedProduct, totalUnsoldProduct) { received, unsold ->
        val rec = received.toIntOrNull() ?: 0
        val uns = unsold.toIntOrNull() ?: 0
        (rec - uns).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Window 2 Output: Sum of column 1
    val totalQuantitySold: StateFlow<Int> = rows.map { rowList ->
        rowList.sumOf { it.quantity.toIntOrNull() ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Window 2 Output: Sum of column 3 (Grand Total Sales)
    val grandTotalSales: StateFlow<Double> = rows.map { rowList ->
        rowList.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Window 3 Output: Row 2 (Commission 10%)
    val commission: StateFlow<Double> = grandTotalSales.map { grandTotal ->
        grandTotal * 0.10
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Window 3 Output: Row 3 (Net Total = Grand Total - Commission)
    val netTotal: StateFlow<Double> = grandTotalSales.map { grandTotal ->
        grandTotal * 0.90 // Total minus 10%
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Window 3 Output: Row 5 (Net Balance = Net Total - Total Payment)
    val netBalance: StateFlow<Double> = combine(netTotal, totalPayment) { netVal, payStr ->
        val pay = payStr.toDoubleOrNull() ?: 0.0
        netVal - pay
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // History Records Flow
    val allRecords: StateFlow<List<CalculationRecord>> = repository.allCalculations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Records (for search)
    val filteredRecords: StateFlow<List<CalculationRecord>> = combine(allRecords, searchQuery) { records, query ->
        if (query.isBlank()) {
            records
        } else {
            val lowerCaseQuery = query.lowercase()
            records.filter { record ->
                val dateStr = formatDate(record.timestamp).lowercase()
                val noteStr = record.notes.lowercase()
                dateStr.contains(lowerCaseQuery) || noteStr.contains(lowerCaseQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard metrics calculated reactively over all stored database calculations
    val dashboardTotalSales: StateFlow<Double> = allRecords.map { list ->
        list.sumOf { it.grandTotalSales }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardTotalCommission: StateFlow<Double> = allRecords.map { list ->
        list.sumOf { it.commission }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardTotalNetIncome: StateFlow<Double> = allRecords.map { list ->
        list.sumOf { it.netTotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardTotalBalance: StateFlow<Double> = allRecords.map { list ->
        list.sumOf { it.netBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Actions
    fun updateRow(index: Int, quantity: String, price: String) {
        val currentList = rows.value.toMutableList()
        currentList[index] = RowData(quantity, price)
        rows.value = currentList
    }

    fun clearCalculator() {
        totalReceivedProduct.value = ""
        totalUnsoldProduct.value = ""
        rows.value = List(14) { RowData() }
        totalPayment.value = ""
        notes.value = ""
        isEditingRecordId.value = null
    }

    fun saveRecord(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val qSerialized = rows.value.joinToString(",") { it.quantity }
            val pSerialized = rows.value.joinToString(",") { it.price }

            val record = CalculationRecord(
                id = isEditingRecordId.value ?: 0,
                timestamp = System.currentTimeMillis(),
                receivedProduct = totalReceivedProduct.value.toIntOrNull() ?: 0,
                unsoldProduct = totalUnsoldProduct.value.toIntOrNull() ?: 0,
                productSold = totalProductSold.value,
                quantitiesSerialized = qSerialized,
                pricesSerialized = pSerialized,
                totalQuantitySold = totalQuantitySold.value,
                grandTotalSales = grandTotalSales.value,
                commission = commission.value,
                netTotal = netTotal.value,
                totalPayment = totalPayment.value.toDoubleOrNull() ?: 0.0,
                netBalance = netBalance.value,
                notes = notes.value.trim()
            )

            repository.insert(record)
            clearCalculator()
            onSuccess()
        }
    }

    fun loadRecord(record: CalculationRecord) {
        isEditingRecordId.value = record.id
        totalReceivedProduct.value = record.receivedProduct.toString()
        totalUnsoldProduct.value = record.unsoldProduct.toString()
        notes.value = record.notes

        val quantities = record.getQuantities()
        val prices = record.getPrices()
        val rowList = List(14) { i ->
            val q = if (quantities[i] == 0) "" else quantities[i].toString()
            val p = if (prices[i] == 0.0) "" else prices[i].toString()
            RowData(q, p)
        }
        rows.value = rowList
        totalPayment.value = if (record.totalPayment == 0.0) "" else record.totalPayment.toString()
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (isEditingRecordId.value == id) {
                clearCalculator()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
