package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkyroDao {
    // Cart operations
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Update
    suspend fun updateCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteCartItem(itemId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Order operations
    @Query("SELECT * FROM delivery_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<DeliveryOrder>>

    @Query("SELECT * FROM delivery_orders WHERE status != 'DELIVERED' ORDER BY timestamp DESC")
    fun getActiveOrders(): Flow<List<DeliveryOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: DeliveryOrder)

    @Query("UPDATE delivery_orders SET status = :status, etaMinutes = :eta, droneId = CASE WHEN :droneId = '' THEN droneId ELSE :droneId END WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, eta: Int, droneId: String)

    // User preferences
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferenceFlow(): Flow<UserPreference?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreference(): UserPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreference(preference: UserPreference)
}
