package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String, // String ID e.g., "RestaurantName:DishName"
    val name: String,
    val price: Double,
    val quantity: Int,
    val restaurantName: String,
    val restaurantId: String = "",  // backend restaurant UUID — needed for order submission
    val menuItemId: String = ""     // backend menu item UUID — stored for order items
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
    @PrimaryKey val orderId: String, // e.g., "SKY-2847" or backend ORD-<ts>
    val restaurantName: String,
    val itemsSummary: String,
    val totalPrice: Double,
    val droneId: String,
    val etaMinutes: Int,
    val status: String,              // "PREPARING", "DISPATCHED", "IN_FLIGHT", "DELIVERED"
    val timestamp: Long = System.currentTimeMillis(),
    val serverOrderId: String = "",  // actual ID from backend (for status polling)
    val deliveryLocationName: String = "" // display name of selected delivery drop
)

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val id: Int = 1,
    val address: String = "📍 SRM AP Campus",
    val themeMode: String = "AUTO",           // "AUTO", "SUNSET", "NIGHT"
    val isLoggedIn: Boolean = false,
    val userName: String = "Guest Pilot",
    val phoneNumber: String = "",
    val awsApiUrl: String = "",               // custom override (if empty, uses NetworkConfig.DEFAULT_BASE_URL)
    val userEmail: String = "saisankaryandamuri@gmail.com",
    val selectedDeliveryLocationId: String = "",    // UUID of selected drop location
    val selectedDeliveryLocationName: String = ""   // human-readable name (e.g. "SR Block")
)
