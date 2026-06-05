package com.example.ui

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiCategory
import com.example.data.ApiLocation
import com.example.data.ApiOrder
import com.example.data.AppDatabase
import com.example.data.CartItem
import com.example.data.DeliveryOrder
import com.example.data.NetworkConfig

import com.example.data.SkyroRepository
import com.example.data.UserPreference
import com.example.data.Restaurant
import com.example.data.Dish
import com.example.data.SkyroRetrofitClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SkyroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SkyroRepository

    // ── Restaurants & Dishes ─────────────────────────────────────────────────
    private val _restaurantsList = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurantsList: StateFlow<List<Restaurant>> = _restaurantsList.asStateFlow()

    private val _dishesList = MutableStateFlow<List<Dish>>(emptyList())
    val dishesList: StateFlow<List<Dish>> = _dishesList.asStateFlow()

    // Dishes per restaurant (loaded on demand when restaurant detail opens)
    private val _currentRestaurantDishes = MutableStateFlow<List<Dish>>(emptyList())
    val currentRestaurantDishes: StateFlow<List<Dish>> = _currentRestaurantDishes.asStateFlow()

    // Drone telemetry WebSocket
    private var droneWebSocket: WebSocket? = null
    private val _droneLocationsMap = MutableStateFlow<Map<String, LatLng>>(emptyMap())
    val droneLocationsMap: StateFlow<Map<String, LatLng>> = _droneLocationsMap.asStateFlow()
    private var currentDroneId: String? = null

    // ── Food Categories ──────────────────────────────────────────────────────
    private val _categories = MutableStateFlow<List<ApiCategory>>(emptyList())
    val categories: StateFlow<List<ApiCategory>> = _categories.asStateFlow()

    // ── Connection state ─────────────────────────────────────────────────────
    private val _awsConnectionState = MutableStateFlow("Connecting...")
    val awsConnectionState: StateFlow<String> = _awsConnectionState.asStateFlow()

    // ── Delivery Locations ───────────────────────────────────────────────────
    private val _deliveryLocations = MutableStateFlow<List<ApiLocation>>(emptyList())
    val deliveryLocations: StateFlow<List<ApiLocation>> = _deliveryLocations.asStateFlow()

    private val _selectedDropLocationId = MutableStateFlow("")
    val selectedDropLocationId: StateFlow<String> = _selectedDropLocationId.asStateFlow()

    private val _selectedDropLocationName = MutableStateFlow("Select Delivery Location")
    val selectedDropLocationName: StateFlow<String> = _selectedDropLocationName.asStateFlow()

    // ── Server Order History ──────────────────────────────────────────────────
    private val _serverOrders = MutableStateFlow<List<ApiOrder>>(emptyList())
    val serverOrders: StateFlow<List<ApiOrder>> = _serverOrders.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _orderPlacedMessage = MutableStateFlow<String?>(null)
    val orderPlacedMessage: StateFlow<String?> = _orderPlacedMessage.asStateFlow()

    // ── WebSocket real-time feed ──────────────────────────────────────────────
    private var webSocket: WebSocket? = null
    private var wsBaseUrl: String = ""

    private val _wsConnected = MutableStateFlow(false)
    val wsConnected: StateFlow<Boolean> = _wsConnected.asStateFlow()

    // Persistent OkHttpClients to prevent garbage collection of connection pool
    private val ordersOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // no timeout on reads (keep-alive)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader(NetworkConfig.NGROK_HEADER_NAME, NetworkConfig.NGROK_HEADER_VALUE)
                    .build()
            )
        }
        .build()

    private val droneOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader(NetworkConfig.NGROK_HEADER_NAME, NetworkConfig.NGROK_HEADER_VALUE)
                    .build()
            )
        }
        .build()

    // ── Payment Method ────────────────────────────────────────────────────────
    private val _selectedPaymentMethod = MutableStateFlow("COD")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    fun selectPaymentMethod(method: String) { _selectedPaymentMethod.value = method }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SkyroRepository(database.skyroDao())

        viewModelScope.launch { repository.initDefaultPrefsIfNeeded() }

        // React to user preferences changes — connect to backend automatically
        viewModelScope.launch {
            repository.userPreferences.collect { prefs ->
                if (prefs != null) {
                    // Restore saved delivery location
                    if (prefs.selectedDeliveryLocationId.isNotEmpty()) {
                        _selectedDropLocationId.value = prefs.selectedDeliveryLocationId
                        _selectedDropLocationName.value = prefs.selectedDeliveryLocationName
                    }

                    // Determine which base URL to use
                    val baseUrl = prefs.awsApiUrl.trim().ifEmpty {
                        NetworkConfig.DEFAULT_BASE_URL
                    }

                    if (!baseUrl.contains("YOUR_EC2_IP")) {
                        fetchAllFromBackend(baseUrl)
                    } else {
                        _restaurantsList.value = emptyList()
                        _dishesList.value      = emptyList()
                        _awsConnectionState.value = "⚠ Set API URL in Profile → API Settings"
                    }

                    val mode = prefs.themeMode
                    _appThemeMode.value = when (mode) {
                        "SUNNY", "SKYRO_PRESENT", "NIGHT" -> mode
                        "SUNSET"                          -> "SUNNY"
                        else                              -> "SKYRO_PRESENT"
                    }
                    _isNightTheme.value = (_appThemeMode.value == "NIGHT")
                }
            }
        }
    }

    /** Fetch restaurants, menu, categories, and delivery locations from the backend. */
    fun fetchAllFromBackend(baseUrl: String) {
        startWebSocket(baseUrl)
        viewModelScope.launch {
            _awsConnectionState.value = "Connecting..."
            try {
                // Restaurants
                val remoteRestaurants = repository.fetchRestaurants(baseUrl)
                if (remoteRestaurants.isNotEmpty()) {
                    _restaurantsList.value = remoteRestaurants
                    _awsConnectionState.value = "✅ Connected (${remoteRestaurants.size} restaurants)"
                } else {
                    _restaurantsList.value = emptyList()
                    _awsConnectionState.value = "⚠ No canteens found in database"
                }

                // All menu items (cached locally for search)
                val remoteMenuItems = repository.fetchMenuItems(baseUrl, restaurantId = "")
                if (remoteMenuItems.isNotEmpty()) {
                    _dishesList.value = remoteMenuItems
                }

                // Food categories
                val cats = repository.fetchCategories(baseUrl)
                if (cats.isNotEmpty()) {
                    _categories.value = cats
                }

                // Delivery drop locations
                val locations = repository.fetchDeliveryLocations(baseUrl)
                _deliveryLocations.value = locations
                if (_selectedDropLocationId.value.isEmpty() && locations.isNotEmpty()) {
                    selectDeliveryLocation(locations.first().id, locations.first().name)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _awsConnectionState.value = "❌ Offline: ${e.localizedMessage ?: "Timeout"}"
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket — receives order_created / order_updated events from backend
    // ─────────────────────────────────────────────────────────────────────────

    private fun startWebSocket(baseUrl: String) {
        // Avoid duplicate sockets for the same URL
        if (wsBaseUrl == baseUrl && webSocket != null) return
        webSocket?.cancel()
        wsBaseUrl = baseUrl

        val wsUrl = baseUrl
            .trim()
            .trimEnd('/')
            .replace("https://", "wss://")
            .replace("http://", "ws://") + "/ws"

        println("SkyroViewModel: Connecting to Orders WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader(NetworkConfig.NGROK_HEADER_NAME, NetworkConfig.NGROK_HEADER_VALUE)
            .build()
        webSocket = ordersOkHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("SkyroViewModel: Orders WebSocket connected successfully.")
                _wsConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleWebSocketMessage(bytes.utf8())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("SkyroViewModel: Orders WebSocket connection failed: ${t.message}")
                _wsConnected.value = false
                this@SkyroViewModel.webSocket = null // Ensure retry isn't locked out
                // Auto-reconnect after 5 seconds
                viewModelScope.launch {
                    delay(5_000)
                    startWebSocket(wsBaseUrl)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("SkyroViewModel: Orders WebSocket closed. Reconnecting...")
                _wsConnected.value = false
                this@SkyroViewModel.webSocket = null
                // Auto-reconnect after 5 seconds
                viewModelScope.launch {
                    delay(5_000)
                    startWebSocket(wsBaseUrl)
                }
            }
        })
    }

    private fun handleWebSocketMessage(text: String) {
        println("SkyroViewModel: Received WebSocket Message: $text")
        try {
            val json = JSONObject(text)
            val event = json.optString("event")
            val orderJson = json.optJSONObject("order") ?: return

            val orderId = orderJson.optString("id")
            val newStatus = orderJson.optString("status")
            val droneId = orderJson.optString("droneId").ifEmpty { orderJson.optString("assigned_drone_id") }

            println("SkyroViewModel: Parsed order event: $event, orderId: $orderId, status: $newStatus, droneId: $droneId")

            when (event) {
                "order_created", "order_updated", "order_assigned" -> {
                    // Sync status to local Room DB
                    viewModelScope.launch {
                        var localOrder: DeliveryOrder? = null
                        // Retry up to 10 times with 200ms delay to handle race conditions where WS message arrives before DB insert
                        for (attempt in 1..10) {
                            val allOrdersList = repository.allOrders.first()
                            localOrder = allOrdersList.find { 
                                it.serverOrderId.equals(orderId, ignoreCase = true) ||
                                it.serverOrderId.replace("ORD-", "").equals(orderId.replace("ORD-", ""), ignoreCase = true)
                            }
                            if (localOrder != null) {
                                break
                            }
                            println("SkyroViewModel: DB race condition check - attempt $attempt: order $orderId not found in Room yet. Retrying...")
                            delay(200)
                        }

                        if (localOrder != null && newStatus.isNotEmpty()) {
                            val mappedStatus = mapServerStatusToLocal(newStatus)
                            println("SkyroViewModel: Syncing status to local Room DB. Local orderId: ${localOrder.orderId}, Status: $mappedStatus, Drone ID: $droneId")
                            repository.updateOrderStatus(
                                orderId  = localOrder.orderId,
                                status   = mappedStatus,
                                eta      = orderJson.optInt("etaMinutes", localOrder.etaMinutes),
                                droneId  = droneId
                            )
                        } else {
                            println("SkyroViewModel: No matching local order found for serverOrderId: $orderId after retries.")
                        }
                    }

                    // Refresh server orders list
                    fetchServerOrderHistory()
                }
            }
        } catch (e: Exception) {
            println("SkyroViewModel: Error parsing WebSocket message: ${e.message}")
            e.printStackTrace()
        }
    }

    // ── Drone Telemetry Tracking via WebSocket ──────────────────────────────
    fun startDroneTracking(droneId: String) {
        if (currentDroneId == droneId && droneWebSocket != null) return
        droneWebSocket?.cancel()
        currentDroneId = droneId

        val ordersUrl = wsBaseUrl.ifEmpty {
            val prefs = userPreferences.value
            prefs?.awsApiUrl?.trim()?.ifEmpty { NetworkConfig.DEFAULT_BASE_URL }
                ?: NetworkConfig.DEFAULT_BASE_URL
        }

        val droneBaseUrl = when {
            ordersUrl.contains("ngrok-free.app", ignoreCase = true) -> {
                NetworkConfig.DEFAULT_DRONE_BASE_URL
            }
            ordersUrl.contains(":8000") -> {
                ordersUrl.replace(":8000", ":8080")
            }
            else -> {
                val portRegex = ":\\d+$".toRegex()
                if (ordersUrl.contains(portRegex)) {
                    ordersUrl.replace(portRegex, ":8080")
                } else {
                    ordersUrl.trimEnd('/') + ":8080"
                }
            }
        }

        val wsDroneBase = droneBaseUrl
            .trim()
            .trimEnd('/')
            .replace("https://", "wss://")
            .replace("http://", "ws://")
        val droneWsUrl = "$wsDroneBase/ws"

        println("SkyroViewModel: Connecting to Drone WebSocket: $droneWsUrl")

        val request = Request.Builder()
            .url(droneWsUrl)
            .build()

        droneWebSocket = droneOkHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("SkyroViewModel: Drone WebSocket connected successfully.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    if (type == "status_update") {
                        val drones = json.optJSONObject("drones")
                        if (drones != null) {
                            val newMap = mutableMapOf<String, LatLng>()
                            val keys = drones.keys()
                            while (keys.hasNext()) {
                                val dId = keys.next()
                                val droneData = drones.optJSONObject(dId)
                                if (droneData != null) {
                                    val location = droneData.optJSONObject("location")
                                    if (location != null) {
                                        val lat = location.optDouble("lat")
                                        val lon = location.optDouble("lon")
                                        if (!lat.isNaN() && !lon.isNaN()) {
                                            newMap[dId] = LatLng(lat, lon)
                                        }
                                    }
                                }
                            }
                            _droneLocationsMap.value = newMap
                        }
                    }
                } catch (e: Exception) {
                    println("SkyroViewModel: Error parsing telemetry WebSocket: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("SkyroViewModel: Drone WebSocket connection failed: ${t.message}")
                this@SkyroViewModel.droneWebSocket = null 
                // Reconnect after 3 seconds if still tracking the same drone
                viewModelScope.launch {
                    delay(3000)
                    if (currentDroneId == droneId) {
                        startDroneTracking(droneId)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("SkyroViewModel: Drone WebSocket closed.")
                this@SkyroViewModel.droneWebSocket = null
            }
        })
    }

    fun stopDroneTracking() {
        currentDroneId = null
        droneWebSocket?.cancel()
        droneWebSocket = null
        _droneLocationsMap.value = emptyMap()
    }

    /** Maps backend "En Route" / "Cooking" etc. → local Room status strings */
    private fun mapServerStatusToLocal(serverStatus: String): String = when (serverStatus) {
        "Placed"           -> "PREPARING"
        "Accepted"         -> "ACCEPTED"
        "Cooking"          -> "COOKING"
        "Ready for Launch" -> "READY_FOR_LAUNCH"
        "En Route"         -> "IN_FLIGHT"
        "Delivered"        -> "DELIVERED"
        "Declined"         -> "DECLINED"
        "Failed"           -> "FAILED"
        else               -> serverStatus
    }

    /** Load menu items for a specific restaurant (called when restaurant detail opens) */
    fun loadMenuForRestaurant(restaurantId: String) {
        viewModelScope.launch {
            val prefs = repository.userPreferences.first()
            val baseUrl = prefs?.awsApiUrl?.trim()?.ifEmpty { NetworkConfig.DEFAULT_BASE_URL }
                ?: NetworkConfig.DEFAULT_BASE_URL
            if (baseUrl.contains("YOUR_EC2_IP")) {
                _currentRestaurantDishes.value = emptyList()
                return@launch
            }
            try {
                val items = repository.fetchMenuItems(baseUrl, restaurantId)
                _currentRestaurantDishes.value = if (items.isNotEmpty()) items
                    else _dishesList.value.filter { it.restaurantId == restaurantId }
            } catch (e: Exception) {
                _currentRestaurantDishes.value = _dishesList.value.filter { it.restaurantId == restaurantId }
            }
        }
    }

    /** Load server order history */
    fun fetchServerOrderHistory() {
        viewModelScope.launch {
            val prefs = repository.userPreferences.first()
            val baseUrl = prefs?.awsApiUrl?.trim()?.ifEmpty { NetworkConfig.DEFAULT_BASE_URL }
                ?: NetworkConfig.DEFAULT_BASE_URL
            if (baseUrl.contains("YOUR_EC2_IP")) return@launch
            try {
                val orders = repository.fetchOrderHistory(baseUrl)
                _serverOrders.value = orders
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Observe Room Flows ───────────────────────────────────────────────────
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<DeliveryOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<DeliveryOrder>> = repository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreference?> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreference())

    // ── Theme ────────────────────────────────────────────────────────────────
    private val _appThemeMode = MutableStateFlow("SKYRO_PRESENT")
    val appThemeMode: StateFlow<String> = _appThemeMode.asStateFlow()

    private val _isNightTheme = MutableStateFlow(checkIsNightHour())
    val isNightTheme: StateFlow<Boolean> = _isNightTheme.asStateFlow()

    // ── Cinematic Launch Sequence ─────────────────────────────────────────────
    private val _cinematicSequenceFrame = MutableStateFlow(0)
    val cinematicSequenceFrame: StateFlow<Int> = _cinematicSequenceFrame.asStateFlow()

    // ── Navigation ───────────────────────────────────────────────────────────
    private val _currentNavRoute = MutableStateFlow("splash")
    val currentNavRoute: StateFlow<String> = _currentNavRoute.asStateFlow()

    private val navigationStack = mutableListOf<String>()

    // ── Tracked Order ────────────────────────────────────────────────────────
    private val _trackedOrderId = MutableStateFlow<String?>(null)
    val trackedOrderId: StateFlow<String?> = _trackedOrderId.asStateFlow()

    fun startTrackingOrder(orderId: String) {
        _trackedOrderId.value = orderId
        _currentNavRoute.value = "live_tracking"
    }

    var hasCompletedHomeIntro: Boolean = false

    // ── Favorites ────────────────────────────────────────────────────────────
    private val _favoriteRestaurantNames = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRestaurantNames: StateFlow<Set<String>> = _favoriteRestaurantNames.asStateFlow()

    private val _favoriteDishNames = MutableStateFlow<Set<String>>(emptySet())
    val favoriteDishNames: StateFlow<Set<String>> = _favoriteDishNames.asStateFlow()

    fun toggleFavoriteRestaurant(name: String) {
        _favoriteRestaurantNames.value = _favoriteRestaurantNames.value.let {
            if (it.contains(name)) it - name else it + name
        }
    }

    fun toggleFavoriteDish(name: String) {
        _favoriteDishNames.value = _favoriteDishNames.value.let {
            if (it.contains(name)) it - name else it + name
        }
    }

    // ── Restaurant Detail ─────────────────────────────────────────────────────
    private val _selectedRestaurantId = MutableStateFlow<String?>(null)
    val selectedRestaurantId: StateFlow<String?> = _selectedRestaurantId.asStateFlow()

    // ── Search & Category ─────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("🍕 Pizza")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private fun checkIsNightHour(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 19 || hour < 5
    }

    fun toggleTheme() {
        _isNightTheme.value = !_isNightTheme.value
        viewModelScope.launch {
            repository.updateThemePreference(if (_isNightTheme.value) "NIGHT" else "SUNNY")
        }
    }

    fun selectTheme(theme: String) {
        _appThemeMode.value = theme
        _isNightTheme.value = (theme == "NIGHT")
        viewModelScope.launch { repository.updateThemePreference(theme) }
    }

    fun navigateTo(route: String) {
        val current = _currentNavRoute.value
        if (current != route) {
            if (current != "splash" && current != "onboarding" && current != "login" && current != "cinematic_sequence") {
                if (route == "home") {
                    navigationStack.clear()
                } else {
                    if (navigationStack.lastOrNull() != current) navigationStack.add(current)
                }
            }
        }
        _currentNavRoute.value = route
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            _currentNavRoute.value = navigationStack.removeAt(navigationStack.lastIndex)
            return true
        }
        val current = _currentNavRoute.value
        if (current != "home" && current != "splash" && current != "onboarding" && current != "login") {
            _currentNavRoute.value = "home"
            return true
        }
        return false
    }

    // ── Auth (simple local-only) ──────────────────────────────────────────────
    fun login(userName: String, phoneNumber: String, userEmail: String) {
        viewModelScope.launch {
            repository.loginUser(userName, phoneNumber, userEmail)
            navigateTo("home")
        }
    }

    fun updateUserProfile(userName: String, phoneNumber: String, userEmail: String) {
        viewModelScope.launch { repository.updateUserProfile(userName, phoneNumber, userEmail) }
    }

    fun updateAddress(address: String) {
        viewModelScope.launch { repository.updateAddress(address) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            navigateTo("login")
        }
    }

    // ── Restaurant Detail ─────────────────────────────────────────────────────
    fun showRestaurantDetails(restaurantId: String) {
        _selectedRestaurantId.value = restaurantId
        loadMenuForRestaurant(restaurantId)
        navigateTo("restaurant_detail")
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectCategory(category: String) { _selectedCategory.value = category }

    // ── Delivery Location ─────────────────────────────────────────────────────
    fun selectDeliveryLocation(locationId: String, locationName: String) {
        _selectedDropLocationId.value   = locationId
        _selectedDropLocationName.value = locationName
        viewModelScope.launch { repository.saveDeliveryLocation(locationId, locationName) }
    }

    // ── Cart Operations ───────────────────────────────────────────────────────
    fun addDishToCart(dishName: String, price: Double, restaurantName: String,
                      restaurantId: String = "", menuItemId: String = "") {
        viewModelScope.launch {
            val items = cartItems.value
            val id = "$restaurantName:$dishName"
            val existing = items.find { it.id == id }
            if (existing != null) {
                repository.addCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                repository.addCartItem(
                    CartItem(
                        id             = id,
                        name           = dishName,
                        price          = price,
                        quantity       = 1,
                        restaurantName = restaurantName,
                        restaurantId   = restaurantId,
                        menuItemId     = menuItemId
                    )
                )
            }
        }
    }

    fun removeDishFromCart(dishName: String, restaurantName: String) {
        viewModelScope.launch {
            val id = "$restaurantName:$dishName"
            val existing = cartItems.value.find { it.id == id }
            if (existing != null) {
                if (existing.quantity > 1) repository.addCartItem(existing.copy(quantity = existing.quantity - 1))
                else repository.deleteCartItem(id)
            }
        }
    }

    fun removeCartItemDirect(id: String) {
        viewModelScope.launch { repository.deleteCartItem(id) }
    }

    fun clearCart() {
        viewModelScope.launch { repository.clearCart() }
    }

    // ── Checkout — Submit order to backend then navigate ──────────────────────
    fun checkoutAndLaunchCinematic(paymentMethod: String = "COD") {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val restaurantName = items.first().restaurantName
            val restaurantId   = items.first().restaurantId
            val totalPrice     = items.sumOf { it.price * it.quantity } + 15.0 + 20.0 + 5.0
            val itemsSummaryStr = items.joinToString { "${it.name} x${it.quantity}" }

            val prefs = repository.userPreferences.first()
            val userName = prefs?.userName ?: "Guest Pilot"
            val baseUrl  = prefs?.awsApiUrl?.trim()?.ifEmpty { NetworkConfig.DEFAULT_BASE_URL }
                ?: NetworkConfig.DEFAULT_BASE_URL

            val dropLocationId   = _selectedDropLocationId.value
            val dropLocationName = _selectedDropLocationName.value

            val localOrderId = "SKY-${(1000..9999).random()}"

            _isPlacingOrder.value = true

            var serverOrderId = ""
            if (!baseUrl.contains("YOUR_EC2_IP") && dropLocationId.isNotEmpty()) {
                serverOrderId = repository.submitOrderToBackend(
                    baseUrl          = baseUrl,
                    cartItems        = items,
                    restaurantId     = restaurantId,
                    restaurantName   = restaurantName,
                    dropLocationId   = dropLocationId,
                    dropLocationName = dropLocationName,
                    userIdentifier   = userName,
                    paymentMethod    = paymentMethod
                ) ?: ""
            }

            val newOrder = DeliveryOrder(
                orderId              = localOrderId,
                restaurantName       = restaurantName,
                itemsSummary         = itemsSummaryStr,
                totalPrice           = totalPrice,
                droneId              = "SKY-042",
                etaMinutes           = 8,
                status               = "PREPARING",
                serverOrderId        = serverOrderId,
                deliveryLocationName = dropLocationName
            )

            repository.placeOrder(newOrder)
            clearCart()

            _isPlacingOrder.value  = false
            _trackedOrderId.value  = newOrder.orderId
            _orderPlacedMessage.value = if (serverOrderId.isNotEmpty())
                "Order placed! Server ID: ${serverOrderId.take(8)}…"
            else
                "Order saved locally. Connect to server to sync."

            _currentNavRoute.value = "live_tracking"
        }
    }

    fun clearOrderPlacedMessage() { _orderPlacedMessage.value = null }

    private fun startMovieSequence(orderId: String) {
        val handler = Handler(Looper.getMainLooper())
        _cinematicSequenceFrame.value = 1
        _currentNavRoute.value = "cinematic_sequence"

        handler.postDelayed({
            _cinematicSequenceFrame.value = 2
            viewModelScope.launch { repository.updateOrderStatus(orderId, "DISPATCHED", 8) }
        }, 1500)

        handler.postDelayed({
            _cinematicSequenceFrame.value = 3
            viewModelScope.launch { repository.updateOrderStatus(orderId, "IN_FLIGHT", 7) }
        }, 2200)

        handler.postDelayed({ _cinematicSequenceFrame.value = 4 }, 3500)

        handler.postDelayed({
            _cinematicSequenceFrame.value = 0
            _currentNavRoute.value = "live_tracking"
        }, 4000)
    }

    fun triggerQuickReorder(pastOrder: DeliveryOrder) {
        viewModelScope.launch {
            val newOrder = DeliveryOrder(
                orderId              = "SKY-${(1000..9999).random()}",
                restaurantName       = pastOrder.restaurantName,
                itemsSummary         = pastOrder.itemsSummary,
                totalPrice           = pastOrder.totalPrice,
                droneId              = "SKY-042",
                etaMinutes           = 8,
                status               = "PREPARING",
                deliveryLocationName = pastOrder.deliveryLocationName
            )
            repository.placeOrder(newOrder)
            _trackedOrderId.value = newOrder.orderId
            navigateTo("order_history")
        }
    }

    fun saveAwsApiUrl(url: String) {
        viewModelScope.launch {
            repository.updateAwsApiUrl(url)
            SkyroRetrofitClient.invalidate()
            webSocket?.cancel()
            webSocket = null
            wsBaseUrl = ""
            fetchAllFromBackend(url.ifEmpty { NetworkConfig.DEFAULT_BASE_URL })
        }
    }

    /** Legacy compat — called by profile screen URL save */
    fun fetchFromAwsPostgres(url: String) {
        fetchAllFromBackend(url)
    }

    fun updateOrderStatusPublic(orderId: String, status: String, etaMinutes: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, etaMinutes)

            // Also sync to server if we have a server order ID
            val order = allOrders.value.find { it.orderId == orderId }
            if (order?.serverOrderId?.isNotEmpty() == true) {
                val prefs = repository.userPreferences.first()
                val baseUrl = prefs?.awsApiUrl?.trim()?.ifEmpty { NetworkConfig.DEFAULT_BASE_URL }
                    ?: NetworkConfig.DEFAULT_BASE_URL
                repository.updateOrderStatusOnServer(baseUrl, order.serverOrderId, status)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.cancel()
        droneWebSocket?.cancel()
    }
}
