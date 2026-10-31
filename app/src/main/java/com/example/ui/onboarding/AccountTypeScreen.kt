package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.OrangePrimary

@Composable
fun AccountTypeScreen(onAccountSelected: (UserRole) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Choose Account Type",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Create an account as a buyer, seller or delivery partner.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        AccountOptionCard(
            title = "Buyer",
            description = "Order food and get it delivered to your location.",
            icon = "🛒",
            onClick = { onAccountSelected(UserRole.BUYER) }
        )

        AccountOptionCard(
            title = "Seller",
            description = "List your food items and grow your business.",
            icon = "🍳",
            onClick = { onAccountSelected(UserRole.SELLER) }
        )

        AccountOptionCard(
            title = "Delivery Partner",
            description = "Deliver orders and earn money.",
            icon = "🛵",
            onClick = { onAccountSelected(UserRole.DELIVERY) }
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = { /* Navigate to Login */ }) {
            Text("Already have an account? Log In", color = OrangePrimary)
        }
    }
}

@Composable
fun AccountOptionCard(title: String, description: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(OrangePrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
