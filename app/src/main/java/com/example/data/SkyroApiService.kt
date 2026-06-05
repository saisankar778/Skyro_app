package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// Skyro API Service — ALL backend-orders endpoints
// Base URL: https://<ngrok-subdomain>.ngrok-free.app  (set via NetworkConfig)
// ─────────────────────────────────────────────────────────────────────────────
interface SkyroApiService {

    // ── Restaurants ──────────────────────────────────────────────────────────
    @GET("api/restaurants")
    suspend fun getRestaurants(): List<ApiRestaurant>

    // ── Menu Items ───────────────────────────────────────────────────────────
    @GET("api/menu-items")
    suspend fun getMenuItems(
        @Query("restaurant_id") restaurantId: String? = null
    ): List<ApiMenuItem>

    // ── Delivery Locations ───────────────────────────────────────────────────
    @GET("api/locations")
    suspend fun getLocations(
        @Query("type") type: String? = "DELIVERY_BLOCK"
    ): List<ApiLocation>

    // ── Orders ───────────────────────────────────────────────────────────────
    @GET("api/orders")
    suspend fun getOrders(): List<ApiOrder>

    @GET("api/orders/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: String): ApiOrder

    @POST("api/orders")
    suspend fun createOrder(@Body order: ApiOrderCreateRequest): ApiOrder

    @PATCH("api/orders/{order_id}")
    suspend fun updateOrderStatus(
        @Path("order_id") orderId: String,
        @Body payload: ApiOrderStatusUpdate
    ): ApiOrder

    // ── Food Categories ──────────────────────────────────────────────────────
    @GET("api/categories")
    suspend fun getCategories(): List<ApiCategory>
}

// ─────────────────────────────────────────────────────────────────────────────
// Retrofit Client — singleton, rebuilds automatically when URL changes.
// Adds the ngrok-skip-browser-warning header to EVERY request so ngrok
// returns JSON instead of its HTML browser-warning page.
// ─────────────────────────────────────────────────────────────────────────────
object SkyroRetrofitClient {
    private var lastUrl: String? = null
    private var cachedService: SkyroApiService? = null

    fun getService(baseUrl: String): SkyroApiService? {
        val sanitized = baseUrl.trim()
        if (sanitized.isEmpty()) return null

        // Ensure proper scheme prefix
        var finalUrl = when {
            sanitized.startsWith("http://") || sanitized.startsWith("https://") -> sanitized
            else -> "https://$sanitized"
        }

        // Retrofit requires a trailing slash on the base URL
        if (!finalUrl.endsWith("/")) {
            finalUrl = "$finalUrl/"
        }

        // Return cached client if URL unchanged
        if (finalUrl == lastUrl && cachedService != null) {
            return cachedService
        }

        return try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // ── ngrok interceptor ─────────────────────────────────────────
            // Without this header, ngrok returns an HTML page (200 status)
            // instead of JSON — Moshi will crash trying to parse it.
            val ngrokInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(NetworkConfig.NGROK_HEADER_NAME, NetworkConfig.NGROK_HEADER_VALUE)
                    .build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(ngrokInterceptor)   // ngrok header — must be first
                .addInterceptor(logging)             // log after headers are applied
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(finalUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            val service = retrofit.create(SkyroApiService::class.java)
            lastUrl = finalUrl
            cachedService = service
            service
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Force rebuild client — call when URL changes in profile settings */
    fun invalidate() {
        lastUrl = null
        cachedService = null
    }
}
