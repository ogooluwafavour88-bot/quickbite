package com.example.ui.buyer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.PaymentMethod
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartItem>,
    appliedPromo: String?,
    onUpdateQuantity: (MenuItem, Int) -> Unit,
    onApplyPromo: (String) -> Boolean,
    onRemovePromo: () -> Unit,
    onPlaceOrder: (PaymentMethod, String, String) -> Order,
    onOrderPlacedNav: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }
    var cardNumber by remember { mutableStateOf("4242 •••• •••• 8492") }
    var cardExpiry by remember { mutableStateOf("08/29") }
    var cardCvv by remember { mutableStateOf("382") }
    var cardHolder by remember { mutableStateOf("EMMA JOHNSON") }
    var deliveryInstructions by remember { mutableStateOf("Leave at door 4B, ring doorbell twice.") }
    var promoInput by remember { mutableStateOf("") }
    var promoError by remember { mutableStateOf<String?>(null) }

    // 3D Secure / OTP Modal State
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("849201") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val deliveryFee = if (cartItems.isEmpty()) 0.0 else 2.49
    val platformFee = if (cartItems.isEmpty()) 0.0 else 1.99
    val discount = when (appliedPromo) {
        "TASTY20" -> subtotal * 0.20
        "FREEDEL" -> deliveryFee
        "QUICK5" -> 5.0
        else -> 0.0
    }
    val finalTotal = (subtotal + deliveryFee + platformFee - discount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Review & Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("256-Bit SSL Escrow Protected", fontSize = 11.sp, color = EmeraldSuccess)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp
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
                            Column {
                                Text("Total Amount Due", fontSize = 12.sp, color = Slate600)
                                Text(
                                    text = "$${String.format("%.2f", finalTotal)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    if (cartItems.isEmpty()) return@Button
                                    if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD) {
                                        showOtpDialog = true
                                    } else {
                                        val placed = onPlaceOrder(
                                            selectedPaymentMethod,
                                            "4242",
                                            "742 Evergreen Terrace, Apt 4B"
                                        )
                                        onOrderPlacedNav(placed.orderId)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("pay_securely_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pay & Secure in Escrow",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Cart is Empty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore tasty meals from top restaurants nearby!",
                        color = Slate600,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Browse Restaurants")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("cart_checkout_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items Section
                item {
                    Text(
                        text = "Selected Dishes (${cartItems.sumOf { it.quantity }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(cartItems) { cartItem ->
                    CartItemRow(
                        cartItem = cartItem,
                        onIncrease = { onUpdateQuantity(cartItem.menuItem, 1) },
                        onDecrease = { onUpdateQuantity(cartItem.menuItem, -1) }
                    )
                }

                // Delivery Location & Instructions
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Delivery Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📍 742 Evergreen Terrace, Apt 4B",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = deliveryInstructions,
                                onValueChange = { deliveryInstructions = it },
                                label = { Text("Driver Dropoff Instructions") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true
                            )
                        }
                    }
                }

                // Promo Voucher Input
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Promo Code / Voucher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = {
                                        promoInput = it
                                        promoError = null
                                    },
                                    placeholder = { Text("Try 'TASTY20' or 'FREEDEL'") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("promo_code_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val success = onApplyPromo(promoInput)
                                        if (!success) {
                                            promoError = "Invalid promo code"
                                        } else {
                                            promoInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Apply")
                                }
                            }

                            if (appliedPromo != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Applied",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Promo '$appliedPromo' Applied!",
                                            color = EmeraldSuccess,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Remove",
                                        color = OrangePrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onRemovePromo() }
                                    )
                                }
                            }

                            if (promoError != null) {
                                Text(
                                    text = promoError ?: "",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Text(
                        text = "Secure Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    PaymentMethodSection(
                        selectedMethod = selectedPaymentMethod,
                        onSelectMethod = { selectedPaymentMethod = it },
                        cardNumber = cardNumber,
                        cardExpiry = cardExpiry,
                        cardCvv = cardCvv,
                        cardHolder = cardHolder,
                        onCardNumberChange = { cardNumber = it },
                        onCardExpiryChange = { cardExpiry = it },
                        onCardCvvChange = { cardCvv = it },
                        onCardHolderChange = { cardHolder = it }
                    )
                }

                // Bill Summary
                item {
                    OrderBillSummary(
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        platformFee = platformFee,
                        discount = discount,
                        finalTotal = finalTotal
                    )
                }

                // Trust & Security Notice
                item {
                    EscrowTrustBanner()
                }
            }
        }
    }

    // 3D Secure / OTP Simulation Dialog
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) showOtpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3D Secure 2.0 Auth",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your card issuer requires 2-factor authentication for total $${String.format("%.2f", finalTotal)}.",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 6-digit SMS OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulated bank code auto-filled for instant testing.",
                        fontSize = 10.sp,
                        color = EmeraldSuccess
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cartItems.isEmpty()) {
                            showOtpDialog = false
                            return@Button
                        }
                        isProcessingPayment = true
                        val placed = onPlaceOrder(
                            PaymentMethod.CREDIT_CARD,
                            "4242",
                            "742 Evergreen Terrace, Apt 4B"
                        )
                        showOtpDialog = false
                        isProcessingPayment = false
                        onOrderPlacedNav(placed.orderId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier.testTag("confirm_otp_button")
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm & Authorize")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOtpDialog = false },
                    enabled = !isProcessingPayment
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(cartItem.menuItem.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = cartItem.menuItem.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (cartItem.selectedCustomizations.isNotEmpty()) {
                        Text(
                            text = cartItem.selectedCustomizations.joinToString(", "),
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", cartItem.menuItem.price)} each",
                        fontSize = 11.sp,
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quantity Control Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (cartItem.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (cartItem.quantity == 1) Color.Red else Slate800,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${cartItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Slate800,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String,
    cardHolder: String,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onCardHolderChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PaymentMethod.values().forEach { method ->
                val isSelected = method == selectedMethod
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { onSelectMethod(method) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectMethod(method) },
                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(method.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = method.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = method.subtitle,
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }
            }

            // Interactive Virtual Card Preview if Credit Card is selected
            AnimatedVisibility(visible = selectedMethod == PaymentMethod.CREDIT_CARD) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Slate900, Slate800, Color(0xFF1A1F36))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Food Radar SecureCard",
                                        color = Slate200,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "VISA",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = cardNumber,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("CARD HOLDER", color = Slate400, fontSize = 9.sp)
                                        Text(
                                            text = cardHolder,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column {
                                        Text("EXPIRES", color = Slate400, fontSize = 9.sp)
                                        Text(
                                            text = cardExpiry,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = onCardExpiryChange,
                            label = { Text("Expires") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = onCardCvvChange,
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderBillSummary(
    subtotal: Double,
    deliveryFee: Double,
    platformFee: Double,
    discount: Double,
    finalTotal: Double
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Payment Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            BillRow(label = "Item Subtotal", amount = "$${String.format("%.2f", subtotal)}")
            BillRow(label = "Standard Delivery Fee", amount = "$${String.format("%.2f", deliveryFee)}")
            BillRow(label = "Platform & Escrow Service Fee", amount = "$${String.format("%.2f", platformFee)}")

            if (discount > 0.0) {
                BillRow(
                    label = "Applied Discount",
                    amount = "-$${String.format("%.2f", discount)}",
                    isDiscount = true
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Slate200
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Payable",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "$${String.format("%.2f", finalTotal)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = OrangePrimary
                )
            }
        }
    }
}

@Composable
private fun BillRow(label: String, amount: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDiscount) EmeraldSuccess else Slate600,
            fontWeight = if (isDiscount) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amount,
            fontSize = 12.sp,
            color = if (isDiscount) EmeraldSuccess else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EscrowTrustBanner() {
    Surface(
        color = EmeraldSuccess.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Escrow Security",
                tint = EmeraldSuccess,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Buyer Protection & Escrow Protocol",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EmeraldSuccess
                )
                Text(
                    text = "Funds are safely held until you confirm delivery using your secret 4-digit PIN. Only then are payouts disbursed to the kitchen & courier.",
                    fontSize = 11.sp,
                    color = Slate700,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
