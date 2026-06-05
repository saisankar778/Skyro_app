package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─────────────────────────────────────────────────────────────────────────────
// API Response Models — exactly matching the backend-orders FastAPI JSON output
// These are separate from Room Entities (which are for local SQLite caching).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Matches GET /api/restaurants response shape from backend.
 */
@JsonClass(generateAdapter = true)
data class ApiRestaurant(
    @Json(name = "id")               val id: String,
    @Json(name = "name")             val name: String,
    @Json(name = "tagline")          val tagline: String? = null,
    @Json(name = "rating")           val rating: Double = 0.0,
    @Json(name = "cuisine")          val cuisine: String? = null,
    @Json(name = "delivery_time_min") val deliveryTimeMin: Int = 20,
    @Json(name = "price_for_two")    val priceForTwo: Int = 300,
    @Json(name = "offer")            val offer: String? = null,
    @Json(name = "image_url")        val imageUrl: String? = null,
    @Json(name = "latitude")         val latitude: Double? = null,
    @Json(name = "longitude")        val longitude: Double? = null,
    @Json(name = "is_active")        val isActive: Boolean = true
)

/**
 * Matches GET /api/menu-items response shape from backend.
 * Now includes is_veg and weight_grams from the migrated DB.
 */
@JsonClass(generateAdapter = true)
data class ApiMenuItem(
    @Json(name = "id")            val id: String,
    @Json(name = "restaurant_id") val restaurantId: String,
    @Json(name = "name")          val name: String,
    @Json(name = "description")   val description: String? = null,
    @Json(name = "price")         val price: Double,
    @Json(name = "category")      val category: String? = null,
    @Json(name = "image_url")     val imageUrl: String? = null,
    @Json(name = "is_available")  val isAvailable: Boolean = true,
    @Json(name = "is_veg")        val isVeg: Boolean = false,       // 🟢 New
    @Json(name = "weight_grams")  val weightGrams: Int = 300         // ⚖️ New
)

/**
 * Matches GET /api/locations response shape.
 * Used to populate the delivery location picker in cart.
 */
@JsonClass(generateAdapter = true)
data class ApiLocation(
    @Json(name = "id")        val id: String,
    @Json(name = "name")      val name: String,
    @Json(name = "type")      val type: String,
    @Json(name = "latitude")  val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "is_active") val isActive: Boolean = true
)

/**
 * Food category from GET /api/categories.
 */
@JsonClass(generateAdapter = true)
data class ApiCategory(
    @Json(name = "id")    val id: String,
    @Json(name = "name")  val name: String,
    @Json(name = "emoji") val emoji: String = "🍽️",
    @Json(name = "sort_order") val sortOrder: Int = 0
)

/**
 * Single order item for POST /api/orders request body.
 */
@JsonClass(generateAdapter = true)
data class ApiOrderItemRequest(
    @Json(name = "id")           val id: String,
    @Json(name = "name")         val name: String,
    @Json(name = "price")        val price: Double,
    @Json(name = "quantity")     val quantity: Int,
    @Json(name = "restaurantId") val restaurantId: String
)

/**
 * Request body for POST /api/orders.
 * payment_method is stored as metadata (COD / UPI / CARD).
 */
@JsonClass(generateAdapter = true)
data class ApiOrderCreateRequest(
    @Json(name = "user")               val user: String,
    @Json(name = "restaurantId")       val restaurantId: String,
    @Json(name = "items")              val items: List<ApiOrderItemRequest>,
    @Json(name = "total")              val total: Double,
    @Json(name = "deliveryLocationId") val deliveryLocationId: String,
    @Json(name = "status")             val status: String = "Placed",
    @Json(name = "payment_method")     val paymentMethod: String? = null
)

/**
 * Request body for PATCH /api/orders/{id}.
 */
@JsonClass(generateAdapter = true)
data class ApiOrderStatusUpdate(
    @Json(name = "status")   val status: String? = null,
    @Json(name = "droneId")  val droneId: String? = null
)

/**
 * Single item in a server-side order response.
 */
@JsonClass(generateAdapter = true)
data class ApiOrderItem(
    @Json(name = "id")           val id: String,
    @Json(name = "name")         val name: String,
    @Json(name = "price")        val price: Double,
    @Json(name = "quantity")     val quantity: Int,
    @Json(name = "restaurantId") val restaurantId: String
)

/**
 * Matches GET /api/orders and POST /api/orders response shape.
 */
@JsonClass(generateAdapter = true)
data class ApiOrder(
    @Json(name = "id")                 val id: String,
    @Json(name = "user")               val user: String,
    @Json(name = "restaurantId")       val restaurantId: String,
    @Json(name = "items")              val items: List<ApiOrderItem>,
    @Json(name = "total")              val total: Double,
    @Json(name = "deliveryLocationId") val deliveryLocationId: String,
    @Json(name = "status")             val status: String,
    @Json(name = "createdAt")          val createdAt: String,
    @Json(name = "droneId")            val droneId: String? = null
)
