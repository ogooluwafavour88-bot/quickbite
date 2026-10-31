package com.example.ui.courier

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangePrimaryDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun CourierDashboardScreen(
    orders: List<Order>,
    onAcceptDelivery: (String) -> Unit,
    onStartDelivery: (String) -> Unit,
    onAdvanceGps: (String) -> Unit,
    onCompleteWithPin: (String, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isOnline by remember { mutableStateOf(true) }
    var selectedOrderForPin by remember { mutableStateOf<Order?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }

    // Active order currently being delivered by courier
    val activeDelivery = orders.firstOrNull {
        (it.status == OrderStatus.ON_THE_WAY || it.status == OrderStatus.READY_FOR_PICKUP) &&
                it.courierName != null
    }

    // Orders available to accept
    val availableOrders = orders.filter {
        (it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PREPARING || it.status == OrderStatus.READY_FOR_PICKUP) &&
                it.courierName == null
    }

    val completedDeliveriesCount = orders.count { it.status == OrderStatus.DELIVERED }
    val todayEarnings = completedDeliveriesCount * 6.50 + 42.00

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("courier_dashboard_screen")
    ) {
        // Courier Header & Earnings Bar
        Surface(
            color = Slate900,
            shadowElevation = 4.dp
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Alex Rodriguez",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Electric Scooter #42 • 4.9 ★",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            color = if (isOnline) EmeraldSuccess else Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourierStatChip(
                        label = "Shift Earnings",
                        value = "$${String.format("%.2f", todayEarnings)}",
                        modifier = Modifier.weight(1f)
                    )
                    CourierStatChip(
                        label = "Trips Finished",
                        value = "$completedDeliveriesCount Drops",
                        modifier = Modifier.weight(1f)
                    )
                    CourierStatChip(
                        label = "Escrow Rate",
                        value = "100% Payout",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ACTIVE TRIP SECTION
            if (activeDelivery != null) {
                item {
                    Text(
                        text = "🚀 Active Delivery in Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }

                item {
                    ActiveDeliveryControlCard(
                        order = activeDelivery,
                        onStartDelivery = { onStartDelivery(activeDelivery.orderId) },
                        onAdvanceGps = { onAdvanceGps(activeDelivery.orderId) },
                        onOpenPinDialog = {
                            selectedOrderForPin = activeDelivery
                            enteredPin = ""
                            pinErrorMessage = null
                        }
                    )
                }
            }

            // AVAILABLE TRIPS NEARBY
            item {
                Text(
                    text = "Available Delivery Dispatches (${availableOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (availableOrders.isEmpty() && activeDelivery == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛵", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Available Delivery Requests",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Switch to Buyer mode to create a new order, or check Kitchen.",
                                color = Slate600,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(availableOrders) { order ->
                    AvailableOrderCard(
                        order = order,
                        onAccept = { onAcceptDelivery(order.orderId) }
                    )
                }
            }
        }
    }

    // Delivery PIN Handover Verification Modal
    if (selectedOrderForPin != null) {
        val targetOrder = selectedOrderForPin ?: return@if
        AlertDialog(
            onDismissRequest = { selectedOrderForPin = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Handover PIN Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Ask customer ${targetOrder.buyerName} for their 4-digit security PIN to complete handover and release escrow payment.",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinErrorMessage = null
                        },
                        label = { Text("4-Digit Customer PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("courier_pin_input"),
                        singleLine = true
                    )

                    // Helper note for testing
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hint for testing: Customer PIN is ${targetOrder.deliveryPin}",
                        fontSize = 10.sp,
                        color = EmeraldSuccess
                    )

                    if (pinErrorMessage != null) {
                        Text(
                            text = pinErrorMessage ?: "",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onCompleteWithPin(targetOrder.orderId, enteredPin)
                        if (success) {
                            selectedOrderForPin = null
                        } else {
                            pinErrorMessage = "Incorrect PIN! Please ask the customer."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier.testTag("verify_and_complete_button")
                ) {
                    Text("Verify & Release Escrow")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForPin = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CourierStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Slate400, fontSize = 9.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActiveDeliveryControlCard(
    order: Order,
    onStartDelivery: () -> Unit,
    onAdvanceGps: () -> Unit,
    onOpenPinDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = OrangePrimary
                )
                Surface(
                    color = if (order.status == OrderStatus.ON_THE_WAY)
                        EmeraldSuccess.copy(alpha = 0.15f)
                    else
                        OrangePrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == OrderStatus.ON_THE_WAY) EmeraldSuccess else OrangePrimaryDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup & Dropoff Route Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = OrangePrimaryDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Pickup From:", fontSize = 10.sp, color = Slate400)
                    Text(order.restaurantName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Dropoff Customer:", fontSize = 10.sp, color = Slate400)
                    Text("${order.buyerName} • ${order.deliveryAddress}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

            // Dynamic Action Button based on delivery phase
            if (order.status == OrderStatus.READY_FOR_PICKUP) {
                Button(
                    onClick = onStartDelivery,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("courier_start_trip_button")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Picked Up & Start Navigation", fontWeight = FontWeight.Bold)
                }
            } else if (order.status == OrderStatus.ON_THE_WAY) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAdvanceGps,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("courier_advance_gps_button")
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fast GPS (+25%)", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenPinDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("courier_handover_pin_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Handover PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Current Telemetry Progress: ${(order.courierProgress * 100).toInt()}% • ~${order.etaMinutes} mins remaining",
                        fontSize = 11.sp,
                        color = Slate600,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailableOrderCard(
    order: Order,
    onAccept: () -> Unit
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
                Column {
                    Text(
                        text = order.restaurantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "To: ${order.deliveryAddress}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }

                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+$5.50 Payout",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("courier_accept_job_${order.orderId}")
            ) {
                Text("Accept Delivery Dispatch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
