package com.example.data

import kotlinx.coroutines.flow.Flow

class SkyroRepository(private val skyroDao: SkyroDao) {

    // ── Local Room Flows ──────────────────────────────────────────────────────
    val cartItems: Flow<List<CartItem>>         = skyroDao.getCartItems()
    val allOrders: Flow<List<DeliveryOrder>>    = skyroDao.getAllOrders()
    val activeOrders: Flow<List<DeliveryOrder>> = skyroDao.getActiveOrders()
    val userPreferences: Flow<UserPreference?>  = skyroDao.getUserPreferenceFlow()

    // ── Cart (local Room) ─────────────────────────────────────────────────────
    suspend fun addCartItem(item: CartItem)          { skyroDao.insertCartItem(item) }
    suspend fun updateCartItem(item: CartItem)       { skyroDao.updateCartItem(item) }
    suspend fun deleteCartItem(itemId: String)       { skyroDao.deleteCartItem(itemId) }
    suspend fun clearCart()                          { skyroDao.clearCart() }

    // ── Orders (local Room) ───────────────────────────────────────────────────
    suspend fun placeOrder(order: DeliveryOrder)     { skyroDao.insertOrder(order) }
    suspend fun updateOrderStatus(orderId: String, status: String, eta: Int, droneId: String = "") {
        skyroDao.updateOrderStatus(orderId, status, eta, droneId)
    }

    // ── User Preferences (local Room) ─────────────────────────────────────────
    suspend fun updateThemePreference(themeMode: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(existing.copy(themeMode = themeMode))
    }

    suspend fun updateAddress(address: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(existing.copy(address = address))
    }

    suspend fun updateAwsApiUrl(url: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(existing.copy(awsApiUrl = url))
        SkyroRetrofitClient.invalidate()
    }

    suspend fun loginUser(userName: String, phoneNumber: String, userEmail: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(
            existing.copy(
                isLoggedIn = true,
                userName = userName,
                phoneNumber = phoneNumber,
                userEmail = userEmail
            )
        )
    }

    suspend fun updateUserProfile(userName: String, phoneNumber: String, userEmail: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(existing.copy(userName = userName, phoneNumber = phoneNumber, userEmail = userEmail))
    }

    suspend fun logoutUser() {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(
            existing.copy(
                isLoggedIn = false,
                userName = "Guest Pilot",
                phoneNumber = "",
                userEmail = "saisankaryandamuri@gmail.com"
            )
        )
    }

    suspend fun saveDeliveryLocation(locationId: String, locationName: String) {
        val existing = skyroDao.getUserPreference() ?: UserPreference()
        skyroDao.insertUserPreference(
            existing.copy(
                selectedDeliveryLocationId = locationId,
                selectedDeliveryLocationName = locationName
            )
        )
    }

    suspend fun initDefaultPrefsIfNeeded() {
        if (skyroDao.getUserPreference() == null) {
            skyroDao.insertUserPreference(UserPreference())
        }
    }

    // ── Remote API calls ──────────────────────────────────────────────────────

    /** Fetch restaurants from backend and map to local [Restaurant] model. */
    suspend fun fetchRestaurants(baseUrl: String): List<Restaurant> {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return emptyList()
        return try {
            service.getRestaurants().mapIndexed { index, apiRest ->
                apiRest.toRestaurant(gradientIndex = index % 8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Fetch menu items from backend for a specific restaurant and map to [Dish]. */
    suspend fun fetchMenuItems(baseUrl: String, restaurantId: String): List<Dish> {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return emptyList()
        return try {
            val id = restaurantId.ifEmpty { null }
            service.getMenuItems(id).map { it.toDish() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Fetch all available delivery drop locations from backend. */
    suspend fun fetchDeliveryLocations(baseUrl: String): List<ApiLocation> {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return emptyList()
        return try {
            service.getLocations(type = null) // fetch all, filter client-side
                .filter { it.type == "DELIVERY_BLOCK" && it.isActive }
                .map { it.copy(name = it.name.replace("_", " ").replace(" and ", " & ")) } // "SR_Block" → "SR Block", "V_and_G_Hostels" → "V & G Hostels"
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Fetch food categories from backend. Falls back to empty list on failure. */
    suspend fun fetchCategories(baseUrl: String): List<ApiCategory> {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return emptyList()
        return try {
            service.getCategories()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Submit a new order to the backend.
     * Returns the backend order ID on success, null on failure.
     */
    suspend fun submitOrderToBackend(
        baseUrl: String,
        cartItems: List<CartItem>,
        restaurantId: String,
        restaurantName: String,
        dropLocationId: String,
        dropLocationName: String,
        userIdentifier: String,
        paymentMethod: String = "COD"
    ): String? {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return null
        return try {
            val total = cartItems.sumOf { it.price * it.quantity } + 15.0 + 20.0 + 5.0
            val orderItems = cartItems.map { item ->
                ApiOrderItemRequest(
                    id           = item.menuItemId.ifEmpty { item.id },
                    name         = item.name,
                    price        = item.price,
                    quantity     = item.quantity,
                    restaurantId = item.restaurantId.ifEmpty { restaurantId }
                )
            }
            val request = ApiOrderCreateRequest(
                user               = userIdentifier,
                restaurantId       = restaurantId,
                items              = orderItems,
                total              = total,
                deliveryLocationId = dropLocationId,
                status             = "Placed",
                paymentMethod      = paymentMethod
            )
            val response = service.createOrder(request)
            response.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Update an order's status on the backend (PATCH /api/orders/{id}).
     * Used by LiveTracking to sync status changes to the server.
     */
    suspend fun updateOrderStatusOnServer(
        baseUrl: String,
        orderId: String,
        status: String
    ): Boolean {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return false
        return try {
            // Only update if we have a valid server order ID (not a local SKY-XXXX id)
            if (orderId.startsWith("SKY-") && !orderId.startsWith("ORD-")) return false
            
            val mappedStatus = when (status) {
                "PREPARING", "Placed" -> "Placed"
                "ACCEPTED", "Accepted" -> "Accepted"
                "COOKING", "Cooking" -> "Cooking"
                "READY_FOR_LAUNCH", "Ready for Launch" -> "Ready for Launch"
                "IN_FLIGHT", "En Route" -> "En Route"
                "ARRIVED", "DELIVERED", "Delivered" -> "Delivered"
                "DECLINED", "Declined" -> "Declined"
                "FAILED", "Failed" -> "Failed"
                else -> status
            }
            
            service.updateOrderStatus(orderId, ApiOrderStatusUpdate(status = mappedStatus))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /** Fetch order history from backend. */
    suspend fun fetchOrderHistory(baseUrl: String): List<ApiOrder> {
        val service = SkyroRetrofitClient.getService(baseUrl) ?: return emptyList()
        return try {
            service.getOrders()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension mapping functions: API models → local UI models
// ─────────────────────────────────────────────────────────────────────────────

fun ApiRestaurant.toRestaurant(gradientIndex: Int): Restaurant = Restaurant(
    id              = id,
    name            = name,
    cuisine         = cuisine ?: "Multi-Cuisine",
    rating          = rating,
    etaMin          = deliveryTimeMin,
    deliveryFee     = 20.0,
    avgCost         = priceForTwo.toDouble() / 2,
    isDroneEligible = true,
    promoBadge      = offer,
    gradientIndex   = gradientIndex,
    imageUrl        = imageUrl
)

fun ApiMenuItem.toDish(): Dish = Dish(
    id           = id,
    name         = name,
    description  = description ?: "",
    price        = price,
    isVeg        = isVeg,          // ✅ Now uses real DB value
    isBestSeller = false,
    restaurantId = restaurantId,
    imageUrl     = imageUrl,
    weightGrams  = weightGrams     // ✅ Now uses real DB value
)
