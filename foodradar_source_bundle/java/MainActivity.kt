package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.FoodDeliveryRepository
import com.example.model.OrderStatus
import com.example.model.Restaurant
import com.example.model.UserRole
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.buyer.BuyerHomeScreen
import com.example.ui.buyer.CartAndCheckoutScreen
import com.example.ui.buyer.LiveOrderTrackingScreen
import com.example.ui.buyer.RestaurantDetailScreen
import com.example.ui.components.RoleSelectorBar
import com.example.ui.courier.CourierDashboardScreen
import com.example.ui.seller.SellerDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class BuyerDestination {
    HOME,
    RESTAURANT_DETAIL,
    CART_CHECKOUT,
    ORDER_TRACKING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FoodRadarApp()
            }
        }
    }
}

@Composable
fun FoodRadarApp() {
    val currentRole by FoodDeliveryRepository.currentRole.collectAsState()
    val restaurants by FoodDeliveryRepository.restaurants.collectAsState()
    val menuItems by FoodDeliveryRepository.menuItems.collectAsState()
    val cartItems by FoodDeliveryRepository.cart.collectAsState()
    val appliedPromo by FoodDeliveryRepository.appliedPromo.collectAsState()
    val orders by FoodDeliveryRepository.orders.collectAsState()
    val transactions by FoodDeliveryRepository.transactions.collectAsState()
    val activeTrackingOrderId by FoodDeliveryRepository.activeTrackingOrderId.collectAsState()

    var buyerDestination by rememberSaveable { mutableStateOf(BuyerDestination.HOME) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen to real-time events across roles (Kitchen accepts, courier moves, escrow releases)
    LaunchedEffect(Unit) {
        FoodDeliveryRepository.notifications.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Role pending counts for badges
    val pendingSellerCount = orders.count {
        it.status == OrderStatus.PLACED || it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PREPARING
    }
    val pendingCourierCount = orders.count {
        it.status == OrderStatus.READY_FOR_PICKUP || (it.status == OrderStatus.ON_THE_WAY && it.courierName != null)
    }
    val activeBuyerOrdersCount = orders.count {
        it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED
    }

    // Back handling for Buyer sub-destinations
    BackHandler(enabled = currentRole == UserRole.BUYER && buyerDestination != BuyerDestination.HOME) {
        buyerDestination = BuyerDestination.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                RoleSelectorBar(
                    selectedRole = currentRole,
                    onRoleSelected = { newRole ->
                        FoodDeliveryRepository.switchRole(newRole)
                    },
                    pendingSellerCount = pendingSellerCount,
                    pendingCourierCount = pendingCourierCount,
                    activeOrderCount = activeBuyerOrdersCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.BUYER -> {
                    when (buyerDestination) {
                        BuyerDestination.HOME -> {
                            BuyerHomeScreen(
                                restaurants = restaurants,
                                menuItems = menuItems,
                                cartItems = cartItems,
                                activeOrders = orders,
                                onSelectRestaurant = { rest ->
                                    selectedRestaurant = rest
                                    buyerDestination = BuyerDestination.RESTAURANT_DETAIL
                                },
                                onAddToCart = { item ->
                                    FoodDeliveryRepository.addToCart(item)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added ${item.name} to cart")
                                    }
                                },
                                onViewCart = {
                                    buyerDestination = BuyerDestination.CART_CHECKOUT
                                },
                                onTrackOrder = { orderId ->
                                    FoodDeliveryRepository.setActiveTrackingOrder(orderId)
                                    buyerDestination = BuyerDestination.ORDER_TRACKING
                                }
                            )
                        }

                        BuyerDestination.RESTAURANT_DETAIL -> {
                            val currentRestaurant = selectedRestaurant ?: restaurants.firstOrNull()
                            if (currentRestaurant != null) {
                                RestaurantDetailScreen(
                                    restaurant = currentRestaurant,
                                    menuItems = menuItems,
                                    cartItems = cartItems,
                                    onAddToCart = { item, customizations ->
                                        FoodDeliveryRepository.addToCart(item, customizations)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${item.name} to cart")
                                        }
                                    },
                                    onViewCart = {
                                        buyerDestination = BuyerDestination.CART_CHECKOUT
                                    },
                                    onBack = {
                                        buyerDestination = BuyerDestination.HOME
                                    }
                                )
                            } else {
                                buyerDestination = BuyerDestination.HOME
                            }
                        }

                        BuyerDestination.CART_CHECKOUT -> {
                            CartAndCheckoutScreen(
                                cartItems = cartItems,
                                appliedPromo = appliedPromo,
                                onUpdateQuantity = { item, delta ->
                                    FoodDeliveryRepository.updateCartItemQuantity(item, delta)
                                },
                                onApplyPromo = { code ->
                                    FoodDeliveryRepository.applyPromo(code)
                                },
                                onRemovePromo = {
                                    FoodDeliveryRepository.removePromo()
                                },
                                onPlaceOrder = { method, cardLast4, address ->
                                    FoodDeliveryRepository.placeOrder(method, cardLast4, address)
                                },
                                onOrderPlacedNav = { orderId ->
                                    FoodDeliveryRepository.setActiveTrackingOrder(orderId)
                                    buyerDestination = BuyerDestination.ORDER_TRACKING
                                },
                                onBack = {
                                    buyerDestination = BuyerDestination.HOME
                                }
                            )
                        }

                        BuyerDestination.ORDER_TRACKING -> {
                            val trackingOrder = orders.firstOrNull { it.orderId == activeTrackingOrderId }
                                ?: orders.firstOrNull()

                            if (trackingOrder != null) {
                                LiveOrderTrackingScreen(
                                    order = trackingOrder,
                                    onSimulateAdvance = {
                                        FoodDeliveryRepository.simulateCourierAdvance(trackingOrder.orderId, 0.25f)
                                    },
                                    onBack = {
                                        buyerDestination = BuyerDestination.HOME
                                    }
                                )
                            } else {
                                BuyerHomeScreen(
                                    restaurants = restaurants,
                                    menuItems = menuItems,
                                    cartItems = cartItems,
                                    activeOrders = orders,
                                    onSelectRestaurant = { rest ->
                                        selectedRestaurant = rest
                                        buyerDestination = BuyerDestination.RESTAURANT_DETAIL
                                    },
                                    onAddToCart = { item -> FoodDeliveryRepository.addToCart(item) },
                                    onViewCart = { buyerDestination = BuyerDestination.CART_CHECKOUT },
                                    onTrackOrder = { orderId ->
                                        FoodDeliveryRepository.setActiveTrackingOrder(orderId)
                                        buyerDestination = BuyerDestination.ORDER_TRACKING
                                    }
                                )
                            }
                        }
                    }
                }

                UserRole.SELLER -> {
                    SellerDashboardScreen(
                        orders = orders,
                        menuItems = menuItems,
                        onAcceptOrder = { orderId -> FoodDeliveryRepository.sellerConfirmOrder(orderId) },
                        onStartCooking = { orderId -> FoodDeliveryRepository.sellerStartPreparing(orderId) },
                        onMarkReady = { orderId -> FoodDeliveryRepository.sellerMarkReadyForPickup(orderId) },
                        onToggleStock = { itemId, isStock ->
                            FoodDeliveryRepository.toggleMenuItemAvailability(itemId, isStock)
                        },
                        onAddMenuItem = { newItem ->
                            FoodDeliveryRepository.addMenuItem(newItem)
                        }
                    )
                }

                UserRole.DELIVERY -> {
                    CourierDashboardScreen(
                        orders = orders,
                        onAcceptDelivery = { orderId -> FoodDeliveryRepository.courierAcceptDelivery(orderId) },
                        onStartDelivery = { orderId -> FoodDeliveryRepository.courierStartDelivery(orderId) },
                        onAdvanceGps = { orderId -> FoodDeliveryRepository.simulateCourierAdvance(orderId, 0.25f) },
                        onCompleteWithPin = { orderId, pin ->
                            FoodDeliveryRepository.completeDeliveryWithPin(orderId, pin)
                        }
                    )
                }

                UserRole.ADMIN -> {
                    AdminPanelScreen(
                        orders = orders,
                        transactions = transactions,
                        onDisburseEscrow = { txnId -> FoodDeliveryRepository.adminReleaseEscrow(txnId) },
                        onCancelAndRefund = { orderId -> FoodDeliveryRepository.adminCancelAndRefund(orderId) }
                    )
                }
            }
        }
    }
}
