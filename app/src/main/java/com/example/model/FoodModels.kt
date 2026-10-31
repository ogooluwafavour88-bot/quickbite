package com.example.model

enum class UserRole(val label: String, val badge: String) {
    BUYER("Foodie (Buyer)", "Buyer"),
    SELLER("Kitchen (Seller)", "Restaurant"),
    DELIVERY("Rider (Courier)", "Delivery"),
    ADMIN("Admin Panel", "Super Admin")
}

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
    PLACED("Order Placed", 0),
    CONFIRMED("Restaurant Confirmed", 1),
    PREPARING("Kitchen Preparing", 2),
    READY_FOR_PICKUP("Ready for Courier", 3),
    ON_THE_WAY("Out for Delivery", 4),
    DELIVERED("Delivered & Completed", 5),
    CANCELLED("Cancelled & Refunded", -1)
}

enum class PaymentMethod(val title: String, val subtitle: String, val icon: String) {
    CREDIT_CARD("Credit / Debit Card", "Instant 256-bit TLS encrypted checkout", "💳"),
    DIGITAL_WALLET("Food Radar Wallet", "Instant 1-tap biometric balance", "⚡"),
    UPI_INSTANT("Direct UPI / Banking", "Google Pay, PhonePe, Net Banking", "🏦"),
    ESCROW_COD("Escrow Cash on Delivery", "Secure release upon 4-digit PIN", "🛡️")
}

enum class TransactionStatus(val label: String) {
    HELD_IN_ESCROW("Funds Secured in Escrow"),
    DISBURSED("Disbursed to Partners"),
    REFUNDED("Refund Processed")
}

data class TransactionRecord(
    val id: String,
    val orderId: String,
    val buyerName: String,
    val sellerName: String,
    val courierName: String?,
    val grossAmount: Double,
    val platformFee: Double,
    val sellerPayout: Double,
    val courierPayout: Double,
    val paymentMethod: PaymentMethod,
    val maskedAccount: String,
    val status: TransactionStatus,
    val timestamp: Long,
    val securityDigest: String = "AES-GCM-256 Verified TLS 1.3"
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val rating: Double,
    val calories: Int,
    val prepTime: String,
    val isAvailable: Boolean = true,
    val badge: String? = null,
    val iconEmoji: String = "🍔"
)

data class Restaurant(
    val id: String,
    val name: String,
    val tagline: String,
    val cuisine: String,
    val rating: Double,
    val reviewCount: Int,
    val deliveryTime: String,
    val deliveryFee: Double,
    val distanceKm: Double,
    val address: String,
    val iconEmoji: String = "🏪"
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedCustomizations: List<String> = emptyList(),
    val specialInstructions: String = ""
) {
    val totalPrice: Double
        get() = menuItem.price * quantity
}

data class Order(
    val orderId: String,
    val buyerName: String,
    val buyerPhone: String,
    val deliveryAddress: String,
    val restaurantId: String,
    val restaurantName: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val discount: Double,
    val total: Double,
    val status: OrderStatus,
    val deliveryPin: String,
    val courierName: String? = null,
    val courierPhone: String? = null,
    val courierVehicle: String? = null,
    val courierProgress: Float = 0f, // 0.0 to 1.0
    val etaMinutes: Int = 22,
    val placedTime: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val transactionId: String = ""
)

fun Double.formatPrice(): String {
    val safeVal = if (this.isNaN() || this.isInfinite()) 0.0 else this
    return String.format(java.util.Locale.US, "%.2f", safeVal)
}
