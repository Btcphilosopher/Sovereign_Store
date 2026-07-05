package com.example.data

import kotlinx.coroutines.flow.Flow

class SovereignRepository(private val db: SovereignDatabase) {
    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()
    private val settingDao = db.settingDao()

    // --- Products ---
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category)

    fun getProductsByCollection(collection: String): Flow<List<Product>> =
        productDao.getProductsByCollection(collection)

    suspend fun insertProduct(product: Product) =
        productDao.insertProduct(product)

    suspend fun insertProducts(products: List<Product>) =
        productDao.insertProducts(products)

    suspend fun updateStock(id: Int, newStock: Int) =
        productDao.updateStock(id, newStock)

    suspend fun deleteProductById(id: Int) =
        productDao.deleteProductById(id)

    suspend fun getProductCount(): Int =
        productDao.getProductCount()

    // --- Cart ---
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()

    suspend fun addToCart(cartItem: CartItem) =
        cartDao.addToCart(cartItem)

    suspend fun updateQuantity(id: Int, quantity: Int) =
        cartDao.updateQuantity(id, quantity)

    suspend fun removeFromCart(id: Int) =
        cartDao.removeFromCart(id)

    suspend fun clearCart() =
        cartDao.clearCart()

    // --- Orders ---
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun insertOrder(order: Order): Long =
        orderDao.insertOrder(order)

    suspend fun updateOrderStatus(id: Int, status: String) =
        orderDao.updateOrderStatus(id, status)

    suspend fun updateTrackingNumber(id: Int, trackingNumber: String) =
        orderDao.updateTrackingNumber(id, trackingNumber)

    suspend fun deleteOrderById(id: Int) =
        orderDao.deleteOrderById(id)

    suspend fun getTotalEarnings(): Double =
        orderDao.getTotalEarnings() ?: 0.0

    suspend fun getOrderCount(): Int =
        orderDao.getOrderCount()

    // --- Settings ---
    val allSettings: Flow<List<Setting>> = settingDao.getAllSettings()

    suspend fun getSettingValue(key: String): String? =
        settingDao.getSettingValue(key)

    suspend fun saveSetting(key: String, value: String) =
        settingDao.saveSetting(Setting(key, value))

    suspend fun deleteSetting(key: String) =
        settingDao.deleteSetting(key)
}
