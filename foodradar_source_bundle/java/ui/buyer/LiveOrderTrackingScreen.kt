package com.example.ui.buyer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangePrimaryDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveOrderTrackingScreen(
    order: Order,
    onSimulateAdvance: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Order Tracking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "ID: ${order.orderId} • ${order.restaurantName}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Quick Simulation Button to fast-forward courier on the map
                    if (order.status == OrderStatus.ON_THE_WAY) {
                        Button(
                            onClick = onSimulateAdvance,
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .padding(end = 8.dp)
                                .testTag("simulate_advance_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Simulate",
                                tint = OrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate GPS", fontSize = 11.sp, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("live_order_tracking_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Canvas Delivery Map
            item {
                InteractiveDeliveryMapCard(
                    order = order,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            }

            // ETA & Status Header Card
            item {
                EtaAndStatusCard(order = order)
            }

            // Secure Handover PIN Card
            item {
                SecurityPinCard(pin = order.deliveryPin)
            }

            // Courier Contact Card (If courier is assigned)
            if (order.courierName != null) {
                item {
                    CourierInfoCard(order = order)
                }
            }

            // Order Status Stepper Timeline
            item {
                OrderStatusStepper(currentStatus = order.status)
            }

            // Order Receipt & Escrow Details
            item {
                OrderReceiptSummaryCard(order = order)
            }
        }
    }
}

@Composable
private fun InteractiveDeliveryMapCard(
    order: Order,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Card(
        modifier = modifier.testTag("interactive_delivery_map"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (w <= 10f || h <= 10f) return@Canvas

                // Draw decorative city grid streets
                val streetColor = Color(0xFF263248)
                val riverColor = Color(0xFF1E293B)

                // Background park / water polygon
                drawRect(
                    color = riverColor,
                    topLeft = Offset(0f, h * 0.45f),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.15f)
                )

                // Grid street lines
                for (y in 30..h.toInt() step 50) {
                    drawLine(
                        color = streetColor,
                        start = Offset(0f, y.toFloat()),
                        end = Offset(w, y.toFloat()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                for (x in 30..w.toInt() step 60) {
                    drawLine(
                        color = streetColor,
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), h),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Route coordinates
                // Restaurant pin: (w * 0.20f, h * 0.75f)
                // Customer home pin: (w * 0.82f, h * 0.25f)
                val restX = w * 0.20f
                val restY = h * 0.75f
                val homeX = w * 0.82f
                val homeY = h * 0.25f

                // Curved delivery route
                val path = Path().apply {
                    moveTo(restX, restY)
                    cubicTo(
                        w * 0.35f, h * 0.30f,
                        w * 0.60f, h * 0.70f,
                        homeX, homeY
                    )
                }

                // Draw road path background
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )

                // Draw completed active path
                drawPath(
                    path = path,
                    color = OrangePrimary,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Restaurant Marker
                drawCircle(color = OrangePrimaryDark, radius = 12.dp.toPx(), center = Offset(restX, restY))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(restX, restY))

                // Customer Home Marker
                drawCircle(color = EmeraldSuccess, radius = 12.dp.toPx(), center = Offset(homeX, homeY))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(homeX, homeY))

                // Courier Marker interpolation along route
                val progress = if (order.status == OrderStatus.DELIVERED) {
                    1.0f
                } else if (order.status == OrderStatus.ON_THE_WAY) {
                    order.courierProgress.coerceIn(0.05f, 0.95f)
                } else {
                    0.05f
                }

                // Parametric cubic curve estimation for courier position
                val t = progress
                val cx = (1 - t) * (1 - t) * (1 - t) * restX +
                        3 * (1 - t) * (1 - t) * t * (w * 0.35f) +
                        3 * (1 - t) * t * t * (w * 0.60f) +
                        t * t * t * homeX

                val cy = (1 - t) * (1 - t) * (1 - t) * restY +
                        3 * (1 - t) * (1 - t) * t * (h * 0.30f) +
                        3 * (1 - t) * t * t * (h * 0.70f) +
                        t * t * t * homeY

                // Pulse radar ring around courier
                if (order.status == OrderStatus.ON_THE_WAY) {
                    drawCircle(
                        color = OrangePrimary.copy(alpha = pulseAlpha),
                        radius = (pulseRadius).dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Courier marker center dot
                drawCircle(
                    color = Color.White,
                    radius = 11.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = OrangePrimary,
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }

            // Map Overlays (Badges)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = Slate900.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS Live Telemetry",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = Slate900.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Text(
                        text = if (order.status == OrderStatus.DELIVERED) "Completed" else "Speed: 24 km/h",
                        color = OrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Legend at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapLegendItem(color = OrangePrimaryDark, label = "Kitchen")
                MapLegendItem(color = OrangePrimary, label = "Courier")
                MapLegendItem(color = EmeraldSuccess, label = "Your Home")
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Slate200, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EtaAndStatusCard(order: Order) {
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
                        text = if (order.status == OrderStatus.DELIVERED)
                            "Delivered Successfully!"
                        else
                            "Estimated Delivery",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Text(
                        text = if (order.status == OrderStatus.DELIVERED)
                            "Food Arrived"
                        else
                            "${order.etaMinutes} Mins",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (order.status == OrderStatus.DELIVERED) EmeraldSuccess else OrangePrimary
                    )
                }

                Surface(
                    color = if (order.status == OrderStatus.DELIVERED)
                        EmeraldSuccess.copy(alpha = 0.15f)
                    else
                        OrangePrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = if (order.status == OrderStatus.DELIVERED) EmeraldSuccess else OrangePrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Drop-off at: ${order.deliveryAddress}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SecurityPinCard(pin: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "PIN",
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delivery Security PIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Share this 4-digit code with the courier upon receiving your meal to safely disburse payment.",
                    color = Slate400,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = OrangePrimary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = pin,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CourierInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = "Courier",
                        tint = OrangePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = order.courierName ?: "Courier",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${order.courierVehicle} • 4.9 ★",
                        color = Slate600,
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Slate100,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Slate800,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    color = OrangePrimary.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusStepper(currentStatus: OrderStatus) {
    val steps = listOf(
        OrderStatus.PLACED,
        OrderStatus.CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.READY_FOR_PICKUP,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Progress",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            steps.forEachIndexed { index, step ->
                val isCompleted = currentStatus.stepIndex >= step.stepIndex
                val isCurrent = currentStatus.stepIndex == step.stepIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> OrangePrimary
                                    isCompleted -> EmeraldSuccess
                                    else -> Slate200
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) Color.White else Slate600,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = step.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isCurrent) OrangePrimary else if (isCompleted) MaterialTheme.colorScheme.onSurface else Slate400
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .width(2.dp)
                            .height(18.dp)
                            .background(if (currentStatus.stepIndex > step.stepIndex) EmeraldSuccess else Slate200)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderReceiptSummaryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Receipt",
                        tint = Slate600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Order Receipt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text(
                    text = "Total $${String.format("%.2f", order.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OrangePrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.menuItem.name}",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Text(
                        text = "$${String.format("%.2f", item.totalPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Slate200
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Escrow Transaction Ref", fontSize = 11.sp, color = Slate600)
                Text(
                    text = order.transactionId,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val Slate100 = Color(0xFFF1F5F9)
