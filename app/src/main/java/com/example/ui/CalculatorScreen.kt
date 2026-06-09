package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalculationRecord
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe StateFlow states from the ViewModel
    val totalReceived by viewModel.totalReceivedProduct.collectAsStateWithLifecycle()
    val totalUnsold by viewModel.totalUnsoldProduct.collectAsStateWithLifecycle()
    val totalSold by viewModel.totalProductSold.collectAsStateWithLifecycle()

    val rowsList by viewModel.rows.collectAsStateWithLifecycle()
    val totalQtySold by viewModel.totalQuantitySold.collectAsStateWithLifecycle()
    val grandTotalSales by viewModel.grandTotalSales.collectAsStateWithLifecycle()

    val commissionAmount by viewModel.commission.collectAsStateWithLifecycle()
    val netTotalAmount by viewModel.netTotal.collectAsStateWithLifecycle()
    val totalPaymentValue by viewModel.totalPayment.collectAsStateWithLifecycle()
    val netBalanceValue by viewModel.netBalance.collectAsStateWithLifecycle()

    val notesValue by viewModel.notes.collectAsStateWithLifecycle()
    val activeRecordId by viewModel.isEditingRecordId.collectAsStateWithLifecycle()
    val searchQueryText by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isDarkByVM by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Dashboard metrics
    val dashSales by viewModel.dashboardTotalSales.collectAsStateWithLifecycle()
    val dashComm by viewModel.dashboardTotalCommission.collectAsStateWithLifecycle()
    val dashNet by viewModel.dashboardTotalNetIncome.collectAsStateWithLifecycle()
    val dashBal by viewModel.dashboardTotalBalance.collectAsStateWithLifecycle()

    // Local screen tab management
    // Tab 0 = Calculator, Tab 1 = Dashboard, Tab 2 = History
    var activeTab by remember { mutableStateOf(0) }
    val filteredRecordsList by viewModel.filteredRecords.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Qaaimada Calculator",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SOMALILAND SHILLING (SLSH) | CAGE-WEYNE B72",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkByVM) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Wax ka beddel muuqaalka",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = if (isDarkByVM) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Calculate, "Xisaabiyaha") },
                    label = { Text("Xisaabiye", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_calculator")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, "Warbixinta Guud") },
                    label = { Text("Muuqaalka", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.History, "Taariikhda") },
                    label = { Text("Taariikhda", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                0 -> {
                    // Calculator Screen with 14 scrollable table cells and Window 1/2/3
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Edit mode active notification
                            if (activeRecordId != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EditCalendar,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Waxaad ku jirtaa habaynta Record #${activeRecordId}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        TextButton(onClick = { viewModel.clearCalculator() }) {
                                            Text("Ka bax", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        // WINDOW 1: PRODUCT SUMMARY CARD
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isDarkByVM) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("window_1_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 4.dp, height = 16.dp)
                                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                        )
                                        Text(
                                            text = "Window 1: Warbixinta Alaabta",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                    // 2 columns x 3 rows grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Column 1 for Inputs
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CalculatorInputField(
                                                value = totalReceived,
                                                onValueChange = { viewModel.totalReceivedProduct.value = it },
                                                label = "S. Alaabta La Helay",
                                                placeholder = "0 xabbo",
                                                isNumber = true,
                                                tag = "received_input"
                                            )
                                            CalculatorInputField(
                                                value = totalUnsold,
                                                onValueChange = { viewModel.totalUnsoldProduct.value = it },
                                                label = "S. Alaabta Hadhay",
                                                placeholder = "0 xabbo",
                                                isNumber = true,
                                                tag = "unsold_input"
                                            )
                                        }

                                        // Column 2 for Output (Auto calculated)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    color = if (isDarkByVM) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color(0xFFD1E4FF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isDarkByVM) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color(0xFF9ECAFF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp)
                                                .fillMaxHeight(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Alaabta La Iibiyay",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isDarkByVM) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF0061A4),
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$totalSold",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.testTag("product_sold_output")
                                            )
                                            Text(
                                                text = "xabbo guud",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // WINDOW 2: SALES CALCULATION CARD (SPREADSHEET TABLE)
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isDarkByVM) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("window_2_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 4.dp, height = 16.dp)
                                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                        )
                                        Text(
                                            text = "Window 2: Iibka",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Table Headers: Tiro | Qiimo | Wadar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Saf",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.width(32.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Tiro (Qty)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.weight(1.2f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Qiimo (SLSH)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.weight(1.8f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Wadarta Safka",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.weight(2f),
                                            textAlign = TextAlign.End
                                        )
                                    }

                                    // Spanned 14 rows listing
                                    for (index in 0 until 14) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Row Index
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.width(32.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            // Quantity Input Box
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .height(38.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .border(
                                                        width = 0.5.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val rowData = rowsList[index]
                                                BasicTextField(
                                                    value = rowData.quantity,
                                                    onValueChange = { newValue ->
                                                        // Filter for safe integers
                                                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                                            viewModel.updateRow(index, newValue, rowData.price)
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    textStyle = TextStyle(
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("qty_input_${index}")
                                                )
                                                if (rowData.quantity.isEmpty()) {
                                                    Text(
                                                        "0",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Price Input Box
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.8f)
                                                    .height(38.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .border(
                                                        width = 0.5.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val rowData = rowsList[index]
                                                BasicTextField(
                                                    value = rowData.price,
                                                    onValueChange = { newValue ->
                                                        // Safe decimals
                                                        if (newValue.length <= 10 && newValue.all { it.isDigit() || it == '.' }) {
                                                            viewModel.updateRow(index, rowData.quantity, newValue)
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    textStyle = TextStyle(
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("price_input_${index}")
                                                )
                                                if (rowData.price.isEmpty()) {
                                                    Text(
                                                        "0.00",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Automatically calculated Total amount column
                                            Text(
                                                text = formatMoney(rowsList[index].amount),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .weight(2f)
                                                    .testTag("row_total_${index}"),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    }

                                    // ROW 15: TOTAL QUANTITY & GRAND TOTAL SALES
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Wadarta Tirada (Qty)",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "$totalQtySold xabbo",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.testTag("grand_total_qty")
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1.5f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "Wadarta Iibka (Grand Total)",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = formatMoney(grandTotalSales) + " SLSH",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.testTag("grand_total_sales")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // WINDOW 3: COMMISSION & BALANCE CARD
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF001E2F)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("window_3_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 4.dp, height = 16.dp)
                                                .background(Color(0xFFD1E4FF), RoundedCornerShape(2.dp))
                                        )
                                        Text(
                                            text = "Window 3: Xisaabinta & Haraaga",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD1E4FF),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFF1E2D2B).copy(alpha = 0.3f))

                                    // Display totals cleanly
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CalculationSummaryRow(
                                            label = "1. Wadarta Guud ee Iibka (Total)",
                                            value = formatMoney(grandTotalSales) + " SLSH",
                                            isDarkCard = true,
                                            tag = "w3_total"
                                        )
                                        CalculationSummaryRow(
                                            label = "2. Khidmadda Commission (10%)",
                                            value = formatMoney(commissionAmount) + " SLSH",
                                            isHighlight = true,
                                            isDarkCard = true,
                                            tag = "w3_commission"
                                        )
                                        CalculationSummaryRow(
                                            label = "3. Wadarta Safiga ah (Net Total)",
                                            value = formatMoney(netTotalAmount) + " SLSH",
                                            fontWeight = FontWeight.ExtraBold,
                                            isDarkCard = true,
                                            tag = "w3_net"
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Total Payment User Input (Row 4)
                                        CalculatorInputField(
                                            value = totalPaymentValue,
                                            onValueChange = { viewModel.totalPayment.value = it },
                                            label = "4. Bixinta (Payment)",
                                            placeholder = "Qor lacagta lagu bixiyay...",
                                            isNumber = true,
                                            accentColor = Color.White,
                                            isDarkCard = true,
                                            tag = "payment_input"
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Net Balance Display (Row 5 - Auto calculated)
                                        val isBalanceNegative = netBalanceValue > 0.0
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = if (isBalanceNegative) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    width = 1.6.dp,
                                                    color = if (isBalanceNegative) Color(0xFFF87171).copy(alpha = 0.4f) else Color(0xFF34D399).copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "5. Haraaga Safiga ah (Balance)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBalanceNegative) Color(0xFFF87171) else Color(0xFF34D399)
                                            )
                                            Text(
                                                text = formatMoney(netBalanceValue) + " SLSH",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isBalanceNegative) Color(0xFFEF4444) else Color(0xFF34D399),
                                                modifier = Modifier.testTag("net_balance_output")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // NOTES / DESCRIPTIONS ACCORDION
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "WAR-BIXIN KALE (NOTES)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    OutlinedTextField(
                                        value = notesValue,
                                        onValueChange = { viewModel.notes.value = it },
                                        placeholder = { Text("U reeb magac macmiil ama faahfaahin kale halkan...") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("notes_input"),
                                        maxLines = 3,
                                        singleLine = false
                                    )
                                }
                            }
                        }

                        // WORKSPACE ACTION BUTTONS
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveRecord {
                                            keyboardController?.hide()
                                            ToastUtils.show(context, "Xogta si guul leh ayaa loo kaydiyay!")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("save_record_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (activeRecordId == null) "KAYDI XOGTA (SAVE)" else "UPDATE XOGTA (SAVE CHANGES)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.clearCalculator()
                                            ToastUtils.show(context, "calculator-ka dib ayaa loo nadiifiyay!")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("clear_button")
                                    ) {
                                        Icon(Icons.Default.ClearAll, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Nadiifi", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val draftObj = viewModelToDraft(
                                                recId = activeRecordId ?: 0,
                                                receivedStr = totalReceived,
                                                unsoldStr = totalUnsold,
                                                soldVal = totalSold,
                                                rowsL = rowsList,
                                                totalQty = totalQtySold,
                                                grandT = grandTotalSales,
                                                comm = commissionAmount,
                                                net = netTotalAmount,
                                                paymentStr = totalPaymentValue,
                                                bal = netBalanceValue,
                                                notesText = notesValue
                                            )
                                            ExportUtils.shareTextReport(context, draftObj)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(48.dp)
                                            .testTag("share_text_button")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("La Wadaag PDF", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Dashboard stats & Visual summaries
                    DashboardTab(
                        totalSalesVal = dashSales,
                        totalCommVal = dashComm,
                        totalNetVal = dashNet,
                        totalBalVal = dashBal,
                        recordsCount = filteredRecordsList.size
                    )
                }

                2 -> {
                    // History Screen with Searching & sorting
                    HistoryTab(
                        searchQuery = searchQueryText,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        records = filteredRecordsList,
                        onLoad = { record ->
                            viewModel.loadRecord(record)
                            activeTab = 0 // Switch to calculator
                        },
                        onDelete = { record ->
                            viewModel.deleteRecord(record.id)
                            ToastUtils.show(context, "Safka waa la tir-tiray!")
                        },
                        onClearAll = {
                            viewModel.clearAllHistory()
                            ToastUtils.show(context, "Gabi ahaanba taariikhda xisaabaadka waa la tir-tiray!")
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    totalSalesVal: Double,
    totalCommVal: Double,
    totalNetVal: Double,
    totalBalVal: Double,
    recordsCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "DASHBOARD-KA GUUD",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Xisaab-xidheedka guud ee kugu kaydsan database-ka dhexdiisa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        // Metrics Grid layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard(
                    title = "WADARTA GUUD EE IIBKA (TOTAL SALES)",
                    value = formatMoney(totalSalesVal) + " SLSH",
                    icon = Icons.Default.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    iconColor = MaterialTheme.colorScheme.primary,
                    tag = "dash_total_sales"
                )

                DashboardMetricCard(
                    title = "WADARTA KHIDMADDA (COMMISSION)",
                    value = formatMoney(totalCommVal) + " SLSH",
                    icon = Icons.Default.Percent,
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    iconColor = MaterialTheme.colorScheme.secondary,
                    tag = "dash_commission"
                )

                DashboardMetricCard(
                    title = "WADARTA DAKHLIGA SAFKA (NET INCOME)",
                    value = formatMoney(totalNetVal) + " SLSH",
                    icon = Icons.Default.MonetizationOn,
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    iconColor = MaterialTheme.colorScheme.primary,
                    tag = "dash_net_income"
                )

                DashboardMetricCard(
                    title = "HARAAGA GUUD (UNPAID BALANCE)",
                    value = formatMoney(totalBalVal) + " SLSH",
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = if (totalBalVal > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    iconColor = if (totalBalVal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    tag = "dash_balance"
                )
            }
        }

        // Summary Statistics Info
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Diiwaangelinta Noocyada Xisaabaadka",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Waxaa ku kaydsan $recordsCount dhowr maalmood oo calculations ah.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(containerColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(tag)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryTab(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    records: List<CalculationRecord>,
    onLoad: (CalculationRecord) -> Unit,
    onDelete: (CalculationRecord) -> Unit,
    onClearAll: () -> Unit,
    viewModel: CalculatorViewModel
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Header
        Text(
            text = "TAARIIKHDA RECOORDS-KA",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ku raadi taariikhda ama faahfaahinta xisaab kasta oo kaydsan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Ku raadi taariikh ama faahfaahin...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Raadinta") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // History Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${records.size} Records Ayaa la helay",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (records.isNotEmpty()) {
                Text(
                    text = "Tir-tir Dhamaan",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { showDeleteConfirmDialog = true }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Records list
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wax xisaab ah laga helin halkan!",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tijaabi eray kale ama kaydi xisaabta hadda.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onLoad = { onLoad(record) },
                        onDelete = { onDelete(record) },
                        onSharePdf = { ExportUtils.shareTextReport(context, record) },
                        onShareExcel = { ExportUtils.shareCsvFile(context, record) },
                        formattedDate = viewModel.formatDate(record.timestamp)
                    )
                }
            }
        }
    }

    // Confirmation Alert Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Mawad la hubaa?") },
            text = { Text("Tani waxay tir-tiraysaa dhammaan records-ka ku jira taariikhda xisaabaadka! Action-kan dib looma soo celin karo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Haa, Tir-tir dhamaan", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Ka laabo")
                }
            }
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: CalculationRecord,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    onSharePdf: () -> Unit,
    onShareExcel: () -> Unit,
    formattedDate: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_card_${record.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First row: Date + Action buttons (Load and Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Load into Calculator Button
                    IconButton(
                        onClick = onLoad,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("load_button_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Dib u soo geli Calculator-ka",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Record Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_button_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Tirtir record-kan",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Description or note if present
            if (record.notes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Faahfaahin: ${record.notes}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Summarized calculations layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Sold (Alaabta)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${record.productSold} xabbo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Iibka Guud (Total)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatMoney(record.grandTotalSales) + " S.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Haraaga Safiga (Bal)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatMoney(record.netBalance) + " S.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.netBalance > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            // Sharing layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSharePdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share/PDF Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShareExcel,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.BorderOuter, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CalculatorInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isNumber: Boolean,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isDarkCard: Boolean = false,
    tag: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkCard) Color(0xFFBAC8DB) else MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (!isNumber || newValue.all { it.isDigit() || it == '.' }) {
                    onValueChange(newValue)
                }
            },
            placeholder = { 
                Text(
                    text = placeholder, 
                    fontSize = 13.sp,
                    color = if (isDarkCard) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ) 
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isNumber) KeyboardType.Decimal else KeyboardType.Text
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag),
            singleLine = true,
            textStyle = TextStyle(
                color = if (isDarkCard) Color.White else MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkCard) Color.White else accentColor,
                unfocusedBorderColor = if (isDarkCard) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                focusedContainerColor = if (isDarkCard) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                unfocusedContainerColor = if (isDarkCard) Color.White.copy(alpha = 0.05f) else Color.Transparent
            )
        )
    }
}

@Composable
fun CalculationSummaryRow(
    label: String,
    value: String,
    fontWeight: FontWeight = FontWeight.Bold,
    isHighlight: Boolean = false,
    isDarkCard: Boolean = false,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) {
                if (isDarkCard) Color(0xFFF97316) else MaterialTheme.colorScheme.secondary
            } else {
                if (isDarkCard) Color(0xFFBAC8DB) else MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            color = if (isHighlight) {
                if (isDarkCard) Color(0xFFF97316) else MaterialTheme.colorScheme.secondary
            } else {
                if (isDarkCard) Color.White else MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

// Convert all current ViewModel attributes dynamically to a transient CalculationRecord format
private fun viewModelToDraft(
    recId: Int,
    receivedStr: String,
    unsoldStr: String,
    soldVal: Int,
    rowsL: List<RowData>,
    totalQty: Int,
    grandT: Double,
    comm: Double,
    net: Double,
    paymentStr: String,
    bal: Double,
    notesText: String
): CalculationRecord {
    val qSerialized = rowsL.joinToString(",") { it.quantity }
    val pSerialized = rowsL.joinToString(",") { it.price }

    return CalculationRecord(
        id = recId,
        timestamp = System.currentTimeMillis(),
        receivedProduct = receivedStr.toIntOrNull() ?: 0,
        unsoldProduct = unsoldStr.toIntOrNull() ?: 0,
        productSold = soldVal,
        quantitiesSerialized = qSerialized,
        pricesSerialized = pSerialized,
        totalQuantitySold = totalQty,
        grandTotalSales = grandT,
        commission = comm,
        netTotal = net,
        totalPayment = paymentStr.toDoubleOrNull() ?: 0.0,
        netBalance = bal,
        notes = notesText
    )
}

// Helper to format currency numbers cleanly (e.g. 1,000.00)
fun formatMoney(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}

// Toast helper
object ToastUtils {
    fun show(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
