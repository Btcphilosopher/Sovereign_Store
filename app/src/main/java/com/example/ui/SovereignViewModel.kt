package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.Order
import com.example.data.Product
import com.example.data.SovereignDatabase
import com.example.data.SovereignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppRole {
    CUSTOMER, MERCHANT
}

sealed class Screen {
    object Storefront : Screen()
    object Basket : Screen()
    data class Checkout(val total: Double) : Screen()
    object AdminDashboard : Screen()
    object AdminOrders : Screen()
    object AdminProducts : Screen()
    object AdminSettings : Screen()
}

class SovereignViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SovereignRepository

    init {
        val database = SovereignDatabase.getDatabase(application)
        repository = SovereignRepository(database)
        seedInitialDataIfNecessary()
    }

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Storefront)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // UI filters
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCollection = MutableStateFlow("All")
    val selectedCollection: StateFlow<String> = _selectedCollection.asStateFlow()

    // Active product detail
    private val _activeProduct = MutableStateFlow<Product?>(null)
    val activeProduct: StateFlow<Product?> = _activeProduct.asStateFlow()

    // Base flows
    val productsFlow: FlowWrapper<List<Product>> = FlowWrapper(repository.allProducts)
    val cartItemsFlow: FlowWrapper<List<CartItem>> = FlowWrapper(repository.cartItems)
    val ordersFlow: FlowWrapper<List<Order>> = FlowWrapper(repository.allOrders)
    val settingsFlow: FlowWrapper<List<com.example.data.Setting>> = FlowWrapper(repository.allSettings)

    // Derived states
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<com.example.data.Setting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Cart Items with full Product details
    val cartWithProducts: StateFlow<List<CartProduct>> = combine(cartItems, products) { cart, prods ->
        cart.mapNotNull { item ->
            val product = prods.find { it.id == item.productId }
            product?.let { CartProduct(item, it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(products, _selectedCategory, _searchQuery, _selectedCollection) { prods, cat, query, coll ->
        prods.filter { prod ->
            val matchCat = cat == "All" || prod.category.equals(cat, ignoreCase = true)
            val matchColl = coll == "All" || prod.collection.equals(coll, ignoreCase = true)
            val matchQuery = query.isEmpty() || prod.title.contains(query, ignoreCase = true) || prod.description.contains(query, ignoreCase = true)
            matchCat && matchColl && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Merchant Analytics
    private val _totalEarnings = MutableStateFlow(0.0)
    val totalEarnings: StateFlow<Double> = _totalEarnings.asStateFlow()

    init {
        // Observe orders to recalculate earnings
        viewModelScope.launch {
            repository.allOrders.collect { orderList ->
                val paidTotal = orderList.filter { it.status != "Pending" }.sumOf { it.totalAmount }
                _totalEarnings.value = paidTotal
            }
        }
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
        // Redirect screen logically
        if (role == AppRole.MERCHANT) {
            _currentScreen.value = Screen.AdminDashboard
        } else {
            _currentScreen.value = Screen.Storefront
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectCollection(collection: String) {
        _selectedCollection.value = collection
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showProductDetails(product: Product?) {
        _activeProduct.value = product
    }

    // --- Product operations ---
    fun addProduct(title: String, description: String, price: Double, category: String, stock: Int, variants: String, collection: String) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    stock = stock,
                    variants = variants,
                    collection = collection
                )
            )
        }
    }

    fun updateStock(productId: Int, newStock: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, newStock)
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProductById(productId)
        }
    }

    // --- Cart operations ---
    fun addToCart(product: Product, quantity: Int, variant: String) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.productId == product.id && it.selectedVariant == variant }
            if (existing != null) {
                repository.updateQuantity(existing.id, existing.quantity + quantity)
            } else {
                repository.addToCart(CartItem(productId = product.id, quantity = quantity, selectedVariant = variant))
            }
        }
    }

    fun updateCartQuantity(cartItemId: Int, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                repository.removeFromCart(cartItemId)
            } else {
                repository.updateQuantity(cartItemId, quantity)
            }
        }
    }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // --- Checkout / Order operations ---
    fun submitCheckout(
        customerName: String,
        customerEmail: String,
        address: String,
        paymentMethod: String,
        carrier: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val items = cartWithProducts.value
            if (items.isEmpty()) return@launch

            val total = items.sumOf { it.cartItem.quantity * it.product.price }
            val itemsDetailsStr = items.joinToString("\n") { 
                "${it.cartItem.quantity}x ${it.product.title} (${it.cartItem.selectedVariant}) - $${it.product.price * it.cartItem.quantity}" 
            }

            // Mock tracking number logic
            val tracking = "SVG-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"

            val newOrder = Order(
                customerName = customerName,
                customerEmail = customerEmail,
                totalAmount = total,
                paymentMethod = paymentMethod,
                carrier = carrier,
                address = address,
                trackingNumber = tracking,
                status = "Paid", // Set as paid initially for direct simulated checkouts
                itemsDetails = itemsDetailsStr
            )

            repository.insertOrder(newOrder)
            
            // Adjust inventory stock
            for (item in items) {
                val remStock = (item.product.stock - item.cartItem.quantity).coerceAtLeast(0)
                repository.updateStock(item.product.id, remStock)
            }

            repository.clearCart()
            onSuccess()
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun updateOrderTracking(orderId: Int, trackingNumber: String) {
        viewModelScope.launch {
            repository.updateTrackingNumber(orderId, trackingNumber)
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrderById(orderId)
        }
    }

    // --- Settings and CMS Configuration ---
    fun saveConfigValue(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
        }
    }

    private fun seedInitialDataIfNecessary() {
        viewModelScope.launch {
            if (repository.getProductCount() == 0) {
                val initialProducts = listOf(
                    Product(
                        title = "Sovereign Node Mini",
                        description = "Compact self-hosted Linux micro-server. Pre-installed with Sovereign platform suite, zero-config configuration scripts, local PostgreSQL, and Docker runtimes.",
                        price = 249.00,
                        category = "Electronics",
                        stock = 15,
                        variants = "16GB RAM,32GB RAM",
                        collection = "Featured"
                    ),
                    Product(
                        title = "Hardware Storage Vault",
                        description = "Military-grade hardware security key with offline credential management, fully air-gapped backups, and cryptographic signing engines.",
                        price = 129.00,
                        category = "Electronics",
                        stock = 25,
                        variants = "Gold Edition,Slate Edition",
                        collection = "Featured"
                    ),
                    Product(
                        title = "Sovereign Developer Tee",
                        description = "Super heavyweight 100% carded organic cotton tee. Styled with high-contrast sovereign infrastructure terminal line-art blueprints.",
                        price = 35.00,
                        category = "Apparel",
                        stock = 50,
                        variants = "S,M,L,XL",
                        collection = "All"
                    ),
                    Product(
                        title = "Distributed Architecture Guide",
                        description = "Direct digital masterclass course covering reverse-proxies, Docker orchestration, off-grid self-hosting strategies, and multi-region database replication.",
                        price = 49.00,
                        category = "Digital Goods",
                        stock = 999,
                        variants = "PDF,Epub+Videos",
                        collection = "New Arrivals"
                    ),
                    Product(
                        title = "Autonomous Shipping Model",
                        description = "Scale concept layout model of the Sovereign Autonomous delivery drone representing secure parcel handovers. Handcrafted in sleek walnut wood and carbon steel.",
                        price = 89.00,
                        category = "Home",
                        stock = 8,
                        variants = "Walnut Wood,Polished Brass",
                        collection = "All"
                    )
                )
                repository.insertProducts(initialProducts)

                // Seed default settings
                repository.saveSetting("tax_rate", "12.5")
                repository.saveSetting("shipping_rate", "9.99")
                repository.saveSetting("store_title", "Sovereign Store")
                repository.saveSetting("store_subtitle", "Sovereign, privacy-respecting self-hosted e-commerce ecosystem")
            }
        }
    }
}

data class CartProduct(
    val cartItem: CartItem,
    val product: Product
)

// Helper class to expose flow cleanly in Compose
class FlowWrapper<T>(private val flow: kotlinx.coroutines.flow.Flow<T>) : kotlinx.coroutines.flow.Flow<T> by flow
