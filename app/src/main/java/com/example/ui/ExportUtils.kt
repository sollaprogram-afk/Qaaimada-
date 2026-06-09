package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.CalculationRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun generateTextReport(record: CalculationRecord): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = sdf.format(Date(record.timestamp))
        val builder = StringBuilder()

        builder.append("========================================\n")
        builder.append("          QAAIMADA CALCULATOR           \n")
        builder.append("========================================\n")
        builder.append("Taariikhda: $dateString\n")
        if (record.notes.isNotEmpty()) {
            builder.append("Xogta kale / Notes: ${record.notes}\n")
        }
        builder.append("----------------------------------------\n\n")

        builder.append("DAAQADA 1: KOOBIDDA ALAABTA (PRODUCT SUMMARY)\n")
        builder.append("----------------------------------------\n")
        builder.append(String.format(Locale.US, "%-25s %12d\n", "Alaabta La Helay:", record.receivedProduct))
        builder.append(String.format(Locale.US, "%-25s %12d\n", "Alaabta Hadhay (Unsold):", record.unsoldProduct))
        builder.append(String.format(Locale.US, "%-25s %12d\n", "Alaabta La Iibiyay (Sold):", record.productSold))
        builder.append("----------------------------------------\n\n")

        builder.append("DAAQADA 2: XISAABINTA IIBKA (SALES CALCULATOR)\n")
        builder.append("----------------------------------------\n")
        builder.append(String.format(Locale.US, "%-6s | %-12s | %-15s\n", "Tiro", "Qiimaha S.", "Wadarta S."))
        builder.append("----------------------------------------\n")

        val quantities = record.getQuantities()
        val prices = record.getPrices()
        var linesEmpty = true
        for (i in 0 until 14) {
            val q = quantities[i]
            val p = prices[i]
            if (q > 0 || p > 0.0) {
                builder.append(String.format(Locale.US, "%-6d | %-12.2f | %-15.2f\n", q, p, q * p))
                linesEmpty = false
            }
        }
        if (linesEmpty) {
            builder.append(" [Ma jiraan safaf la galiyay]\n")
        }
        builder.append("----------------------------------------\n")
        builder.append(String.format(Locale.US, "%-6d | %-12s | %-15.2f SLSH\n", record.totalQuantitySold, "WADAR GUUD", record.grandTotalSales))
        builder.append("----------------------------------------\n\n")

        builder.append("DAAQADA 3: KHIDMADDA & HARAAGA (COMMISSION & BALANCE)\n")
        builder.append("----------------------------------------\n")
        builder.append(String.format(Locale.US, "%-25s %12.2f SLSH\n", "Wadarta Guud:", record.grandTotalSales))
        builder.append(String.format(Locale.US, "%-25s %12.2f SLSH\n", "Khidmadda / Comm (10%):", record.commission))
        builder.append(String.format(Locale.US, "%-25s %12.2f SLSH\n", "Wadarta Safiga (Net):", record.netTotal))
        builder.append(String.format(Locale.US, "%-25s %12.2f SLSH\n", "Lacagta La Bixiyay:", record.totalPayment))
        builder.append(String.format(Locale.US, "%-25s %12.2f SLSH\n", "Haraaga Safiga ah:", record.netBalance))
        builder.append("========================================\n")
        builder.append("   Farxad Noogu Habee Xisaabaadkaaga!   \n")
        builder.append("========================================\n")

        return builder.toString()
    }

    fun generateCsvContent(record: CalculationRecord): String {
        val builder = StringBuilder()
        builder.append("QAAIMADA CALCULATOR,Xisaab-Xidheedka\n")
        builder.append("Taariikhda,${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(record.timestamp))}\n")
        builder.append("Notes,${record.notes}\n\n")

        builder.append("DAAQADA 1: KOOBIDDA ALAABTA\n")
        builder.append("Alaabta La Helay,${record.receivedProduct}\n")
        builder.append("Alaabta Hadhay,${record.unsoldProduct}\n")
        builder.append("Alaabta La Iibiyay,${record.productSold}\n\n")

        builder.append("DAAQADA 2: LINES-KA IIBKA\n")
        builder.append("Tirada,Qiimaha xabbadii (SLSH),Wadarta wadarta (SLSH)\n")
        val quantities = record.getQuantities()
        val prices = record.getPrices()
        for (i in 0 until 14) {
            val q = quantities[i]
            val p = prices[i]
            if (q > 0 || p > 0.0) {
                builder.append("$q,$p,${q * p}\n")
            }
        }
        builder.append("Tirada Guud: ${record.totalQuantitySold},,Iibka Guud: ${record.grandTotalSales}\n\n")

        builder.append("DAAQADA 3: KHIDMADDA & HARAAGA\n")
        builder.append("Wadarta Guud,${record.grandTotalSales}\n")
        builder.append("Khidmadda Commission (10%),${record.commission}\n")
        builder.append("Wadarta Safiga ah,${record.netTotal}\n")
        builder.append("Lacagta La Bixiyay,${record.totalPayment}\n")
        builder.append("Haraaga,${record.netBalance}\n")

        return builder.toString()
    }

    fun shareTextReport(context: Context, record: CalculationRecord) {
        val text = generateTextReport(record)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Warbixinta Qaaimada Calculator")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "La wadaag (Share PDF/Text)"))
    }

    fun shareCsvFile(context: Context, record: CalculationRecord) {
        try {
            val csvContent = generateCsvContent(record)
            val filename = "Qaaimada_${record.id}_${record.timestamp}.csv"
            val file = File(context.cacheDir, filename)
            file.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Qaaimada Calculator - Excel Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "U dhoof Excel (CSV)"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cilad ayaa ku dhacday dhoofinta CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
