package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FoodDeliveryRepository
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Food Radar app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Food Radar", appName)
    }

    @Test
    fun `order lifecycle and escrow release with pin verification works correctly`() {
        // 1. Add item to cart and place order
        val menuItem = FoodDeliveryRepository.menuItems.value.first()
        FoodDeliveryRepository.addToCart(menuItem)
        val placedOrder = FoodDeliveryRepository.placeOrder(PaymentMethod.CREDIT_CARD, "4242")

        assertNotNull(placedOrder)
        assertEquals(OrderStatus.PLACED, placedOrder.status)

        // Verify transaction was recorded with HELD_IN_ESCROW status
        val txn = FoodDeliveryRepository.transactions.value.firstOrNull { it.orderId == placedOrder.orderId }
        assertNotNull(txn)
        assertEquals(TransactionStatus.HELD_IN_ESCROW, txn?.status)

        // 2. Kitchen confirms & prepares
        FoodDeliveryRepository.sellerConfirmOrder(placedOrder.orderId)
        var currentOrder = FoodDeliveryRepository.orders.value.first { it.orderId == placedOrder.orderId }
        assertEquals(OrderStatus.CONFIRMED, currentOrder.status)

        FoodDeliveryRepository.sellerStartPreparing(placedOrder.orderId)
        currentOrder = FoodDeliveryRepository.orders.value.first { it.orderId == placedOrder.orderId }
        assertEquals(OrderStatus.PREPARING, currentOrder.status)

        FoodDeliveryRepository.sellerMarkReadyForPickup(placedOrder.orderId)
        currentOrder = FoodDeliveryRepository.orders.value.first { it.orderId == placedOrder.orderId }
        assertEquals(OrderStatus.READY_FOR_PICKUP, currentOrder.status)

        // 3. Courier accepts and starts delivery
        FoodDeliveryRepository.courierAcceptDelivery(placedOrder.orderId, "Alex Rodriguez")
        FoodDeliveryRepository.courierStartDelivery(placedOrder.orderId)
        currentOrder = FoodDeliveryRepository.orders.value.first { it.orderId == placedOrder.orderId }
        assertEquals(OrderStatus.ON_THE_WAY, currentOrder.status)

        // 4. Test wrong PIN fails
        val wrongPinResult = FoodDeliveryRepository.completeDeliveryWithPin(placedOrder.orderId, "0000")
        assertFalse(wrongPinResult)

        // 5. Test correct PIN succeeds and releases escrow
        val correctPinResult = FoodDeliveryRepository.completeDeliveryWithPin(placedOrder.orderId, placedOrder.deliveryPin)
        assertTrue(correctPinResult)

        currentOrder = FoodDeliveryRepository.orders.value.first { it.orderId == placedOrder.orderId }
        assertEquals(OrderStatus.DELIVERED, currentOrder.status)

        val updatedTxn = FoodDeliveryRepository.transactions.value.first { it.orderId == placedOrder.orderId }
        assertEquals(TransactionStatus.DISBURSED, updatedTxn.status)
    }
}
