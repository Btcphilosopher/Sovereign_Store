package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerEmail: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val carrier: String,
    val address: String,
    val trackingNumber: String,
    val status: String, // "Pending", "Paid", "Shipped", "Delivered"
    val timestamp: Long = System.currentTimeMillis(),
    val itemsDetails: String // List of purchased items
)

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("UPDATE orders SET trackingNumber = :trackingNumber WHERE id = :id")
    suspend fun updateTrackingNumber(id: Int, trackingNumber: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("SELECT SUM(totalAmount) FROM orders WHERE status != 'Pending'")
    suspend fun getTotalEarnings(): Double?

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
}
