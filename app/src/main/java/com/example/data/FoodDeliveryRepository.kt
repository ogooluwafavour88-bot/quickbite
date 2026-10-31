package com.example.data

import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.Restaurant
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object FoodDeliveryRepository {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJobs = ConcurrentHashMap<String, Job>()

    // Notification broadcast for multi-role interactions
    private val _notifications = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val notifications: SharedFlow<String> = _notifications.asSharedFlow()

    // Current Active Role
    private val _currentRole = MutableStateFlow(UserRole.BUYER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    // Restaurants
    private val _restaurants = MutableStateFlow<List<Restaurant>>(
        listOf(
            Restaurant(
                id = "rest_1",
                name = "Flame & Brioche Burgers",
                tagline = "Smoked Wagyu, Brioche Buns & Truffle Fries",
                cuisine = "Artisan Burgers • American",
                rating = 4.8,
                reviewCount = 384,
                deliveryTime = "15-25 min",
                deliveryFee = 2.49,
                distanceKm = 1.3,
                address = "142 Market Street, Downtown",
                iconEmoji = "🍔"
            ),
            Restaurant(
                id = "rest_2",
                name = "Napoli Woodfired Pizza",
                tagline = "Sourdough Crust, San Marzano & Fior di Latte",
                cuisine = "Italian • Woodfired Pizza",
                rating = 4.9,
                reviewCount = 512,
                deliveryTime = "20-35 min",
                deliveryFee = 2.99,
                distanceKm = 2.1,
                address = "88 Little Italy Promenade",
                iconEmoji = "🍕"
            ),
            Restaurant(
                id = "rest_3",
                name = "Umami Ramen & Dumpling Bar",
                tagline = "18-Hour Tonkotsu & Steamed Pork Gyoza",
                cuisine = "Japanese • Ramen • Bowls",
                rating = 4.7,
                reviewCount = 290,
                deliveryTime = "25-35 min",
                deliveryFee = 3.49,
                distanceKm = 3.0,
                address = "55 Sakura Avenue",
                iconEmoji = "🍜"
            ),
            Restaurant(
                id = "rest_4",
                name = "Green Bliss Organic Bowls",
                tagline = "Superfood Salads, Avocado Bowls & Cold-Pressed Juices",
                cuisine = "Healthy • Vegan Friendly",
                rating = 4.6,
                reviewCount = 195,
                deliveryTime = "15-25 min",
                deliveryFee = 1.99,
                distanceKm = 1.0,
                address = "12 Botanical Square",
                iconEmoji = "🥗"
            )
        )
    )
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // Menu Items
    private val _menuItems = MutableStateFlow<List<MenuItem>>(
        listOf(
            MenuItem(
                id = "item_101",
                restaurantId = "rest_1",
                name = "Smoked Truffle Wagyu Burger",
                description = "Double Angus patty, aged cheddar, caramelised onions, black truffle aioli on toasted brioche.",
                price = 14.99,
                category = "Burgers",
                rating = 4.9,
                calories = 780,
                prepTime = "12 min",
                badge = "Bestseller",
                iconEmoji = "🍔"
            ),
            MenuItem(
                id = "item_102",
                restaurantId = "rest_1",
                name = "Crispy Nashville Hot Chicken Burger",
                description = "Golden buttermilk fried thigh, spicy habanero glaze, tangy slaw, house pickles.",
                price = 12.99,
                category = "Burgers",
                rating = 4.8,
                calories = 710,
                prepTime = "10 min",
                badge = "Spicy 🔥",
                iconEmoji = "🍗"
            ),
            MenuItem(
                id = "item_103",
                restaurantId = "rest_1",
                name = "Parmesan Truffle Loaded Fries",
                description = "Hand-cut Idaho potatoes, white truffle oil, grated 24-month parmesan, rosemary sea salt.",
                price = 6.49,
                category = "Sides",
                rating = 4.7,
                calories = 420,
                prepTime = "6 min",
                iconEmoji = "🍟"
            ),
            MenuItem(
                id = "item_104",
                restaurantId = "rest_1",
                name = "Salted Caramel Thick Milkshake",
                description = "Madagascan vanilla bean, house caramel swirl, sea salt crystal garnish.",
                price = 5.99,
                category = "Drinks",
                rating = 4.9,
                calories = 360,
                prepTime = "4 min",
                iconEmoji = "🥤"
            ),
            MenuItem(
                id = "item_201",
                restaurantId = "rest_2",
                name = "Margherita Di Bufala DOP",
                description = "San Marzano tomatoes, fresh buffalo mozzarella, fragrant Genovese basil, EVO oil.",
                price = 15.50,
                category = "Pizza",
                rating = 4.9,
                calories = 820,
                prepTime = "14 min",
                badge = "Chef Choice",
                iconEmoji = "🍕"
            ),
            MenuItem(
                id = "item_202",
                restaurantId = "rest_2",
                name = "Diavola Spicy Salami Pizza",
                description = "Spicy Calabrian soppressata, chili flakes, roasted peppers, molten fior di latte.",
                price = 16.99,
                category = "Pizza",
                rating = 4.8,
                calories = 890,
                prepTime = "14 min",
                badge = "Popular",
                iconEmoji = "🍕"
            ),
            MenuItem(
                id = "item_203",
                restaurantId = "rest_2",
                name = "Classic Italian Tiramisu",
                description = "Espresso soaked savoiardi biscuits, mascarpone cream, dark cocoa dust.",
                price = 7.50,
                category = "Desserts",
                rating = 4.9,
                calories = 430,
                prepTime = "2 min",
                iconEmoji = "🍰"
            ),
            MenuItem(
                id = "item_301",
                restaurantId = "rest_3",
                name = "Rich Tonkotsu Black Garlic Ramen",
                description = "18-hr pork bone broth, tender chashu pork, molten ajitsuke egg, nori, scallions, black garlic oil.",
                price = 16.50,
                category = "Ramen",
                rating = 4.9,
                calories = 850,
                prepTime = "15 min",
                badge = "Top Pick",
                iconEmoji = "🍜"
            ),
            MenuItem(
                id = "item_302",
                restaurantId = "rest_3",
                name = "Pan-Seared Pork & Chive Gyoza (6pcs)",
                description = "Crispy lacy crust, minced pork belly filling, sesame chili dipping glaze.",
                price = 8.50,
                category = "Sides",
                rating = 4.8,
                calories = 380,
                prepTime = "8 min",
                iconEmoji = "🥟"
            ),
            MenuItem(
                id = "item_401",
                restaurantId = "rest_4",
                name = "Golden Harvest Avocado Quinoa Bowl",
                description = "Hass avocado, roasted chickpeas, organic quinoa, wild greens, heirloom tomato, tahini drizzle.",
                price = 13.99,
                category = "Healthy",
                rating = 4.7,
                calories = 490,
                prepTime = "8 min",
                badge = "Healthy 🥑",
                iconEmoji = "🥗"
            ),
            MenuItem(
                id = "item_402",
                restaurantId = "rest_4",
                name = "Cold-Pressed Citrus Glow Booster",
                description = "Fresh Valencia oranges, ginger root, turmeric, crisp green apple.",
                price = 5.50,
                category = "Drinks",
                rating = 4.8,
                calories = 140,
                prepTime = "3 min",
                iconEmoji = "🧃"
            )
        )
    )
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    // Shopping Cart
    private val _cart = MutableStateFlow<List<CartItem>>(
        run {
            val item1 = _menuItems.value.getOrNull(0)
            val item2 = _menuItems.value.getOrNull(2)
            val list = mutableListOf<CartItem>()
            if (item1 != null) {
                list.add(CartItem(menuItem = item1, quantity = 1, selectedCustomizations = listOf("Extra Truffle Aioli", "Gluten-Free Bun")))
            }
            if (item2 != null) {
                list.add(CartItem(menuItem = item2, quantity = 1, selectedCustomizations = listOf("Extra Parmesan")))
            }
            list
        }
    )
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _appliedPromo = MutableStateFlow<String?>("TASTY20")
    val appliedPromo: StateFlow<String?> = _appliedPromo.asStateFlow()

    fun applyPromo(code: String): Boolean {
        val clean = code.trim().uppercase()
        return if (clean == "TASTY20" || clean == "FREEDEL" || clean == "QUICK5") {
            _appliedPromo.value = clean
            true
        } else {
            false
        }
    }

    fun removePromo() {
        _appliedPromo.value = null
    }

    fun addToCart(item: MenuItem, customizations: List<String> = emptyList()) {
        _cart.update { currentList ->
            val existingIndex = currentList.indexOfFirst {
                it.menuItem.id == item.id && it.selectedCustomizations == customizations
            }
            if (existingIndex >= 0) {
                currentList.toMutableList().apply {
                    val old = this[existingIndex]
                    this[existingIndex] = old.copy(quantity = old.quantity + 1)
                }
            } else {
                currentList + CartItem(menuItem = item, quantity = 1, selectedCustomizations = customizations)
            }
        }
    }

    fun updateCartItemQuantity(item: MenuItem, delta: Int) {
        _cart.update { currentList ->
            val index = currentList.indexOfFirst { it.menuItem.id == item.id }
            if (index >= 0) {
                val target = currentList[index]
                val newQty = target.quantity + delta
                if (newQty <= 0) {
                    currentList.filterIndexed { i, _ -> i != index }
                } else {
                    currentList.toMutableList().apply {
                        this[index] = target.copy(quantity = newQty)
                    }
                }
            } else currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // Orders & Transactions
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _activeTrackingOrderId = MutableStateFlow<String?>(null)
    val activeTrackingOrderId: StateFlow<String?> = _activeTrackingOrderId.asStateFlow()

    fun setActiveTrackingOrder(orderId: String?) {
        _activeTrackingOrderId.value = orderId
    }

    init {
        // Seed initial mock order and transaction for immediate exploration
        seedInitialDemoData()
    }

    private fun seedInitialDemoData() {
        val sampleItem1 = _menuItems.value.getOrNull(0) ?: return
        val sampleItem2 = _menuItems.value.getOrNull(2) ?: sampleItem1
        val orderId = "QB-92841"
        val txnId = "TXN-SEC-582910"
        val subtotal = 21.48
        val delFee = 2.49
        val platFee = 1.99
        val disc = 4.30
        val total = subtotal + delFee + platFee - disc

        val initialOrder = Order(
            orderId = orderId,
            buyerName = "Emma Johnson",
            buyerPhone = "+1 (555) 019-2834",
            deliveryAddress = "742 Evergreen Terrace, Apt 4B",
            restaurantId = "rest_1",
            restaurantName = "Flame & Brioche Burgers",
            items = listOf(
                CartItem(sampleItem1, 1, listOf("Extra Truffle Aioli")),
                CartItem(sampleItem2, 1, listOf("Extra Parmesan"))
            ),
            subtotal = subtotal,
            deliveryFee = delFee,
            platformFee = platFee,
            discount = disc,
            total = total,
            status = OrderStatus.ON_THE_WAY,
            deliveryPin = "4829",
            courierName = "Alex Rodriguez",
            courierPhone = "+1 (555) 782-9382",
            courierVehicle = "Electric Niu Scooter #42",
            courierProgress = 0.55f,
            etaMinutes = 8,
            placedTime = System.currentTimeMillis() - 15 * 60 * 1000,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            transactionId = txnId
        )

        val initialTxn = TransactionRecord(
            id = txnId,
            orderId = orderId,
            buyerName = "Emma Johnson",
            sellerName = "Flame & Brioche Burgers",
            courierName = "Alex Rodriguez",
            grossAmount = total,
            platformFee = 3.22,
            sellerPayout = 14.50,
            courierPayout = 3.94,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            maskedAccount = "•••• 4242 (Visa Signature)",
            status = TransactionStatus.HELD_IN_ESCROW,
            timestamp = System.currentTimeMillis() - 15 * 60 * 1000
        )

        _orders.value = listOf(initialOrder)
        _transactions.value = listOf(initialTxn)
        _activeTrackingOrderId.value = orderId
    }

    // Checkout / Place Order Flow with Escrow Security
    fun placeOrder(
        paymentMethod: PaymentMethod,
        cardLast4: String = "4242",
        deliveryAddress: String = "742 Evergreen Terrace, Apt 4B"
    ): Order {
        val currentCart = _cart.value
        val restaurantId = currentCart.firstOrNull()?.menuItem?.restaurantId ?: "rest_1"
        val restaurant = _restaurants.value.firstOrNull { it.id == restaurantId }
            ?: _restaurants.value.firstOrNull()
            ?: Restaurant(
                id = "rest_1",
                name = "Flame & Brioche Burgers",
                tagline = "Artisan Burgers",
                cuisine = "American",
                rating = 4.8,
                reviewCount = 100,
                deliveryTime = "20 min",
                deliveryFee = 2.49,
                distanceKm = 1.0,
                address = "Downtown"
            )

        val subtotal = currentCart.sumOf { it.totalPrice }
        val deliveryFee = restaurant.deliveryFee
        val platformFee = 1.99
        val promoCode = _appliedPromo.value
        val discount = when (promoCode) {
            "TASTY20" -> subtotal * 0.20
            "FREEDEL" -> deliveryFee
            "QUICK5" -> 5.0
            else -> 0.0
        }
        val total = (subtotal + deliveryFee + platformFee - discount).coerceAtLeast(0.0)

        val randomNum = Random.nextInt(10000, 99999)
        val orderId = "FR-$randomNum"
        val txnId = "TXN-SEC-${Random.nextInt(100000, 999999)}"
        val pin = Random.nextInt(1000, 9999).toString()

        val maskedAccount = when (paymentMethod) {
            PaymentMethod.CREDIT_CARD -> "•••• $cardLast4 (Mastercard/Visa)"
            PaymentMethod.DIGITAL_WALLET -> "Food Radar Wallet (Bal: $148.50)"
            PaymentMethod.UPI_INSTANT -> "emma.j@okhdfcbank"
            PaymentMethod.ESCROW_COD -> "Escrow Cash on Delivery (PIN Required)"
        }

        val platformCommission = total * 0.15
        val courierPayout = 3.50 + deliveryFee
        val sellerPayout = (total - platformCommission - courierPayout).coerceAtLeast(0.0)

        val newOrder = Order(
            orderId = orderId,
            buyerName = "Emma Johnson",
            buyerPhone = "+1 (555) 019-2834",
            deliveryAddress = deliveryAddress,
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            items = currentCart,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            discount = discount,
            total = total,
            status = OrderStatus.PLACED,
            deliveryPin = pin,
            courierName = null,
            courierPhone = null,
            courierVehicle = null,
            courierProgress = 0f,
            etaMinutes = 25,
            placedTime = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            transactionId = txnId
        )

        val newTxn = TransactionRecord(
            id = txnId,
            orderId = orderId,
            buyerName = "Emma Johnson",
            sellerName = restaurant.name,
            courierName = null,
            grossAmount = total,
            platformFee = platformCommission,
            sellerPayout = sellerPayout,
            courierPayout = courierPayout,
            paymentMethod = paymentMethod,
            maskedAccount = maskedAccount,
            status = TransactionStatus.HELD_IN_ESCROW,
            timestamp = System.currentTimeMillis()
        )

        _orders.update { listOf(newOrder) + it }
        _transactions.update { listOf(newTxn) + it }
        _activeTrackingOrderId.value = orderId
        clearCart()

        scope.launch {
            _notifications.emit("🎉 Order $orderId placed! Payment held safely in Escrow.")
        }

        return newOrder
    }

    // Seller Kitchen Actions
    fun sellerConfirmOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CONFIRMED)
        scope.launch {
            _notifications.emit("👩‍🍳 Order $orderId confirmed by restaurant!")
        }
    }

    fun sellerStartPreparing(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.PREPARING)
        scope.launch {
            _notifications.emit("🔥 Kitchen started preparing order $orderId!")
        }
    }

    fun sellerMarkReadyForPickup(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.READY_FOR_PICKUP)
        scope.launch {
            _notifications.emit("📦 Order $orderId is packaged & ready for courier!")
        }
    }

    fun toggleMenuItemAvailability(itemId: String, isAvailable: Boolean) {
        _menuItems.update { items ->
            items.map {
                if (it.id == itemId) it.copy(isAvailable = isAvailable) else it
            }
        }
    }

    fun addMenuItem(item: MenuItem) {
        _menuItems.update { listOf(item) + it }
    }

    // Courier Actions
    fun courierAcceptDelivery(
        orderId: String,
        courierName: String = "Alex Rodriguez",
        courierPhone: String = "+1 (555) 782-9382",
        vehicle: String = "Electric Scooter #42"
    ) {
        _orders.update { orders ->
            orders.map {
                if (it.orderId == orderId) {
                    it.copy(
                        courierName = courierName,
                        courierPhone = courierPhone,
                        courierVehicle = vehicle,
                        status = OrderStatus.READY_FOR_PICKUP
                    )
                } else it
            }
        }
        _transactions.update { txns ->
            txns.map {
                if (it.orderId == orderId) it.copy(courierName = courierName) else it
            }
        }
        scope.launch {
            _notifications.emit("🛵 Courier $courierName assigned to order $orderId")
        }
    }

    fun courierStartDelivery(orderId: String) {
        _orders.update { orders ->
            orders.map {
                if (it.orderId == orderId) {
                    it.copy(
                        status = OrderStatus.ON_THE_WAY,
                        courierProgress = 0.05f,
                        etaMinutes = 18
                    )
                } else it
            }
        }
        scope.launch {
            _notifications.emit("🚀 Courier picked up order $orderId and is on the way!")
        }
        startCourierSimulation(orderId)
    }

    fun simulateCourierAdvance(orderId: String, delta: Float = 0.25f) {
        _orders.update { orders ->
            orders.map { order ->
                if (order.orderId == orderId && order.status == OrderStatus.ON_THE_WAY) {
                    val next = (order.courierProgress + delta).coerceAtMost(0.95f)
                    val remainingMins = ((1f - next) * 20).toInt().coerceAtLeast(1)
                    order.copy(courierProgress = next, etaMinutes = remainingMins)
                } else order
            }
        }
    }

    private fun startCourierSimulation(orderId: String) {
        simulationJobs[orderId]?.cancel()
        simulationJobs[orderId] = scope.launch {
            for (step in 1..8) {
                delay(4000)
                simulateCourierAdvance(orderId, 0.12f)
            }
        }
    }

    fun completeDeliveryWithPin(orderId: String, enteredPin: String): Boolean {
        val targetOrder = _orders.value.firstOrNull { it.orderId == orderId } ?: return false
        if (targetOrder.deliveryPin != enteredPin.trim()) {
            return false
        }

        simulationJobs[orderId]?.cancel()
        _orders.update { orders ->
            orders.map {
                if (it.orderId == orderId) {
                    it.copy(
                        status = OrderStatus.DELIVERED,
                        courierProgress = 1.0f,
                        etaMinutes = 0
                    )
                } else it
            }
        }

        // Release Escrow in Transaction Ledger
        _transactions.update { txns ->
            txns.map {
                if (it.orderId == orderId) {
                    it.copy(status = TransactionStatus.DISBURSED)
                } else it
            }
        }

        scope.launch {
            _notifications.emit("✅ Order $orderId successfully delivered! Escrow funds released to Seller & Courier.")
        }
        return true
    }

    // Admin Operations
    fun adminReleaseEscrow(txnId: String) {
        _transactions.update { txns ->
            txns.map {
                if (it.id == txnId) it.copy(status = TransactionStatus.DISBURSED) else it
            }
        }
        scope.launch {
            _notifications.emit("🏦 Admin manual release: Escrow $txnId disbursed.")
        }
    }

    fun adminCancelAndRefund(orderId: String) {
        simulationJobs[orderId]?.cancel()
        _orders.update { orders ->
            orders.map {
                if (it.orderId == orderId) it.copy(status = OrderStatus.CANCELLED) else it
            }
        }
        _transactions.update { txns ->
            txns.map {
                if (it.orderId == orderId) it.copy(status = TransactionStatus.REFUNDED) else it
            }
        }
        scope.launch {
            _notifications.emit("🛑 Order $orderId cancelled & refunded by Admin.")
        }
    }

    private fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { orders ->
            orders.map {
                if (it.orderId == orderId) it.copy(status = newStatus) else it
            }
        }
    }
}
