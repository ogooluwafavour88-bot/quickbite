package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangePrimaryDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    orders: List<Order>,
    transactions: List<TransactionRecord>,
    onDisburseEscrow: (String) -> Unit,
    onCancelAndRefund: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var txnSearchQuery by remember { mutableStateOf("") }

    val totalGmv = transactions.sumOf { it.grossAmount } + 2840.0
    val totalPlatformCommission = transactions.sumOf { it.platformFee } + 426.0
    val escrowHeld = transactions.filter { it.status == TransactionStatus.HELD_IN_ESCROW }.sumOf { it.grossAmount }
    val totalDisbursed = transactions.filter { it.status == TransactionStatus.DISBURSED }.sumOf { it.grossAmount } + 2414.0

    val filteredTxns = transactions.filter {
        txnSearchQuery.isBlank() ||
                it.id.contains(txnSearchQuery, ignoreCase = true) ||
                it.orderId.contains(txnSearchQuery, ignoreCase = true) ||
                it.buyerName.contains(txnSearchQuery, ignoreCase = true) ||
                it.sellerName.contains(txnSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_panel_screen")
    ) {
        // Platform Command Header
        Surface(
            color = Slate900,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Operations Center",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Multi-Tenant Platform Oversight & Escrow Ledger",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "System Healthy",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        title = "Platform GMV",
                        value = "$${String.format("%.2f", totalGmv)}",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Commission (15%)",
                        value = "$${String.format("%.2f", totalPlatformCommission)}",
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        title = "Escrow Held",
                        value = "$${String.format("%.2f", escrowHeld)}",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tabs: Financial Transaction Ledger vs All Live Operations
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF6366F1)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Transaction Ledger (${transactions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Operations Feed (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            // Transaction History & Audit Ledger Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = txnSearchQuery,
                        onValueChange = { txnSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_txn_search"),
                        placeholder = { Text("Search by TXN ID, Order ID, Buyer, or Restaurant...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Slate400)
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        text = "Cryptographic Payment Audit Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredTxns) { txn ->
                    TransactionAuditCard(
                        txn = txn,
                        onDisburse = { onDisburseEscrow(txn.id) }
                    )
                }
            }
        } else {
            // Live Operations Monitor Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "All System Orders & Handover Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(orders) { order ->
                    AdminOrderMonitorCard(
                        order = order,
                        onCancelOrder = { onCancelAndRefund(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Slate400, fontSize = 9.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransactionAuditCard(
    txn: TransactionRecord,
    onDisburse: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = txn.id,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6366F1)
                    )
                    Text(
                        text = "Ref Order: ${txn.orderId} • ${dateFormat.format(Date(txn.timestamp))}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }

                Surface(
                    color = when (txn.status) {
                        TransactionStatus.HELD_IN_ESCROW -> OrangePrimary.copy(alpha = 0.15f)
                        TransactionStatus.DISBURSED -> EmeraldSuccess.copy(alpha = 0.15f)
                        TransactionStatus.REFUNDED -> Color.Red.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = txn.status.label,
                        color = when (txn.status) {
                            TransactionStatus.HELD_IN_ESCROW -> OrangePrimaryDark
                            TransactionStatus.DISBURSED -> EmeraldSuccess
                            TransactionStatus.REFUNDED -> Color.Red
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gross Amount & Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Gross Billed", fontSize = 10.sp, color = Slate600)
                    Text("$${String.format("%.2f", txn.grossAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Platform Take", fontSize = 10.sp, color = Slate600)
                    Text("$${String.format("%.2f", txn.platformFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                }
                Column {
                    Text("Seller Share", fontSize = 10.sp, color = Slate600)
                    Text("$${String.format("%.2f", txn.sellerPayout)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Courier Pay", fontSize = 10.sp, color = Slate600)
                    Text("$${String.format("%.2f", txn.courierPayout)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Method: ${txn.paymentMethod.title} (${txn.maskedAccount})",
                fontSize = 11.sp,
                color = Slate700
            )
            Text(
                text = "Parties: Buyer: ${txn.buyerName} → Kitchen: ${txn.sellerName}${if (txn.courierName != null) " → Rider: ${txn.courierName}" else ""}",
                fontSize = 11.sp,
                color = Slate600
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(txn.securityDigest, fontSize = 10.sp, color = Slate600, fontFamily = FontFamily.Monospace)
                }

                if (txn.status == TransactionStatus.HELD_IN_ESCROW) {
                    Button(
                        onClick = onDisburse,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Disburse Escrow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOrderMonitorCard(
    order: Order,
    onCancelOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = OrangePrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.status.displayName,
                            color = OrangePrimaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "$${String.format("%.2f", order.total)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kitchen: ${order.restaurantName} • Customer: ${order.buyerName}",
                fontSize = 12.sp,
                color = Slate800
            )
            Text(
                text = "Courier: ${order.courierName ?: "Unassigned"} • Handover PIN: ${order.deliveryPin}",
                fontSize = 11.sp,
                color = Slate600
            )

            if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancelOrder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel & Refund", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
