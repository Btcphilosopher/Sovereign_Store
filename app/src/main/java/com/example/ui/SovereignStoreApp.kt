package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import java.util.UUID
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CartItem
import com.example.data.Order
import com.example.data.Product
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SovereignStoreApp(viewModel: SovereignViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeProduct by viewModel.activeProduct.collectAsState()

    // Retrieve CMS settings
    val storeTitle = settings.find { it.key == "store_title" }?.value ?: "Sovereign Store"
    val storeSubtitle = settings.find { it.key == "store_subtitle" }?.value ?: "Sovereign e-commerce platform"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = if (currentRole == AppRole.CUSTOMER) "INDEPENDENT NETWORK" else "INFRASTRUCTURE ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val parts = storeTitle.split(" ")
                            val firstWord = parts.firstOrNull() ?: "Sovereign"
                            val restOfWords = parts.drop(1).joinToString(" ")
                            Text(
                                text = "$firstWord ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White
                            )
                            if (restOfWords.isNotEmpty()) {
                                Text(
                                    text = restOfWords,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Elegant security shield avatar matching HTML with gradient + white border
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1), // ImmersiveIndigoMuted
                                        Color(0xFFA855F7)  // ImmersivePurple
                                    )
                                )
                            )
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(percent = 50))
                            .clickable {
                                val nextRole = if (currentRole == AppRole.CUSTOMER) AppRole.MERCHANT else AppRole.CUSTOMER
                                viewModel.setRole(nextRole)
                            }
                            .testTag("role_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentRole == AppRole.CUSTOMER) Icons.Default.Shield else Icons.Default.AdminPanelSettings,
                            contentDescription = "Toggle Mode",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (currentRole == AppRole.CUSTOMER) {
                NavigationBar(
                    containerColor = Color(0xFF0A0A0B),
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(0.dp)),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Storefront,
                        onClick = { viewModel.navigateTo(Screen.Storefront) },
                        icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Shop") },
                        label = { Text("Store") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_store_button")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Basket,
                        onClick = { viewModel.navigateTo(Screen.Basket) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartWithProducts.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(
                                                text = cartWithProducts.sumOf { it.cartItem.quantity }.toString(),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Basket")
                            }
                        },
                        label = { Text("Basket") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_basket_button")
                    )
                }
            } else {
                NavigationBar(
                    containerColor = Color(0xFF0A0A0B),
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(0.dp)),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.AdminDashboard,
                        onClick = { viewModel.navigateTo(Screen.AdminDashboard) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                        label = { Text("Overview") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_admin_overview")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.AdminProducts,
                        onClick = { viewModel.navigateTo(Screen.AdminProducts) },
                        icon = { Icon(Icons.Default.List, contentDescription = "Products") },
                        label = { Text("Products") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_admin_products")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.AdminOrders,
                        onClick = { viewModel.navigateTo(Screen.AdminOrders) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_admin_orders")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.AdminSettings,
                        onClick = { viewModel.navigateTo(Screen.AdminSettings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Config") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_admin_settings")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val screen = currentScreen) {
                is Screen.Storefront -> StorefrontScreen(viewModel)
                is Screen.Basket -> BasketScreen(viewModel)
                is Screen.Checkout -> CheckoutScreen(viewModel, screen.total)
                is Screen.AdminDashboard -> AdminDashboardScreen(viewModel)
                is Screen.AdminProducts -> AdminProductsScreen(viewModel)
                is Screen.AdminOrders -> AdminOrdersScreen(viewModel)
                is Screen.AdminSettings -> AdminSettingsScreen(viewModel)
            }

            // Product Detail Dialog Modal
            if (activeProduct != null) {
                ProductDetailModal(
                    product = activeProduct!!,
                    onDismiss = { viewModel.showProductDetails(null) },
                    onAddToBasket = { qty, variant ->
                        viewModel.addToCart(activeProduct!!, qty, variant)
                        viewModel.showProductDetails(null)
                    }
                )
            }
        }
    }
}

@Composable
fun StorefrontScreen(viewModel: SovereignViewModel) {
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCollection by viewModel.selectedCollection.collectAsState()

    val categories = listOf("All", "Electronics", "Apparel", "Digital Goods", "Home")
    val collections = listOf("All", "Featured", "New Arrivals")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search our sovereign collection...", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Banner block
        HeroBannerBlock()

        Spacer(modifier = Modifier.height(16.dp))

        // Categories selector
        Text("Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("category_chip_$cat")
                )
            }
        }

        // Collections filter row
        Text("Collections", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            collections.forEach { coll ->
                val isSelected = selectedCollection == coll
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCollection(coll) },
                    label = { Text(coll) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("collection_chip_$coll")
                )
            }
        }

        // Product grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "No Products",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No sovereign products found.",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductGridCard(product = product, onClick = { viewModel.showProductDetails(product) })
                }
            }
        }
    }
}

@Composable
fun HeroBannerBlock() {
    val context = LocalContext.current
    val heroImgFile = File(context.filesDir, "../app/src/main/res/drawable/img_hero_banner.jpg")
    val hasHeroImage = true // Set to true as we just generated and verified it!

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw background image
            if (hasHeroImage) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Sovereign Store banner background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark semi-transparent gradient tint overlay to ensure text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Banner content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "OFF-GRID COMMERCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "Sovereign Storefront Platform",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    "Own your platform, own your checkout, secure your database.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ProductGridCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Simulated Product Image using appropriate category icons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (product.category.lowercase()) {
                    "electronics" -> Icons.Default.Dns
                    "apparel" -> Icons.Default.Checkroom
                    "digital goods" -> Icons.Default.MenuBook
                    "home" -> Icons.Default.Home
                    else -> Icons.Default.ShoppingBag
                }
                Icon(
                    imageVector = icon,
                    contentDescription = product.title,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Stock label overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            if (product.stock > 0) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (product.stock > 0) "${product.stock} in stock" else "Out of stock",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Description block
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Quick view details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailModal(
    product: Product,
    onDismiss: () -> Unit,
    onAddToBasket: (quantity: Int, variant: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val variantOptions = remember(product) {
        if (product.variants.isNotEmpty()) product.variants.split(",") else emptyList()
    }
    var selectedVariant by remember { mutableStateOf(variantOptions.firstOrNull() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                .testTag("product_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Category Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close details")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title & Price
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Product Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Variant selection
                if (variantOptions.isNotEmpty()) {
                    Text(
                        text = "Select Variation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        variantOptions.forEach { opt ->
                            val isSelected = selectedVariant == opt
                            OutlinedButton(
                                onClick = { selectedVariant = opt },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.testTag("variant_button_$opt")
                            ) {
                                Text(opt, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Quantity Selectors
                Text(
                    text = "Quantity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .testTag("qty_minus")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                    }
                    Text(
                        text = quantity.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (quantity < product.stock) quantity++ },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .testTag("qty_plus")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Add to Basket Button
                Button(
                    onClick = { onAddToBasket(quantity, selectedVariant) },
                    enabled = product.stock > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_to_basket_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = if (product.stock > 0) "Add to Basket" else "Out of Stock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BasketScreen(viewModel: SovereignViewModel) {
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val totalAmount = cartWithProducts.sumOf { it.cartItem.quantity * it.product.price }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Basket",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (cartWithProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Empty Basket",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your sovereign shopping basket is empty.",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Storefront) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Browse Catalog")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartWithProducts) { cartProd ->
                    BasketItemRow(cartProd = cartProd, viewModel = viewModel)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.secondary)
                        Text("$${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Simulated Taxes", color = MaterialTheme.colorScheme.secondary)
                        Text("$${String.format("%.2f", totalAmount * 0.125)}", fontWeight = FontWeight.Bold) // 12.5% tax
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Flat Carrier Shipping", color = MaterialTheme.colorScheme.secondary)
                        Text("$9.99", fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Sovereign Bill", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            text = "$${String.format("%.2f", totalAmount * 1.125 + 9.99)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.Checkout(totalAmount * 1.125 + 9.99)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_trigger"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Proceed to Secure Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BasketItemRow(cartProd: CartProduct, viewModel: SovereignViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon representation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (cartProd.product.category.lowercase()) {
                    "electronics" -> Icons.Default.Dns
                    "apparel" -> Icons.Default.Checkroom
                    "digital goods" -> Icons.Default.MenuBook
                    "home" -> Icons.Default.Home
                    else -> Icons.Default.ShoppingBag
                }
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cartProd.product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (cartProd.cartItem.selectedVariant.isNotEmpty()) {
                    Text(
                        "Variant: ${cartProd.cartItem.selectedVariant}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    "$${String.format("%.2f", cartProd.product.price)} each",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.updateCartQuantity(cartProd.cartItem.id, cartProd.cartItem.quantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    cartProd.cartItem.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = { viewModel.updateCartQuantity(cartProd.cartItem.id, cartProd.cartItem.quantity + 1) },
                    modifier = Modifier.size(32.dp),
                    enabled = cartProd.cartItem.quantity < cartProd.product.stock
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.removeFromCart(cartProd.cartItem.id) },
                modifier = Modifier.testTag("remove_item_${cartProd.cartItem.id}")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CheckoutScreen(viewModel: SovereignViewModel, total: Double) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("Card Processor") }
    var selectedCarrier by remember { mutableStateOf("Sovereign Express") }
    var isSuccess by remember { mutableStateOf(false) }

    val payments = listOf("Card Processor", "Direct Bank Transfer", "Direct Invoice (BTC)")
    val carriers = listOf("Sovereign Express", "National Postal", "Off-Grid Courier")

    if (isSuccess) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Order Successful",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sovereign Order Placed!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Thank you for supporting self-hosted independent commerce.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Carrier Assigned:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(selectedCarrier, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Status:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("PAID / AUTHED", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tracking Number:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("SVG-${UUID.randomUUID().toString().substring(0,8).uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.navigateTo(Screen.Storefront)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Storefront")
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Basket) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Secure Checkout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Billing Info
            Text("1. Customer & Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Customer Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_name_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Customer Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_email_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Physical Delivery Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("checkout_address_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Methods
            Text("2. Payment Interface (Modular Plugin)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            payments.forEach { pay ->
                val isSelected = selectedPayment == pay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPayment = pay }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedPayment = pay }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(pay, fontWeight = FontWeight.Medium)
                        Text(
                            text = when (pay) {
                                "Card Processor" -> "Simulate secure stripe card gateway"
                                "Direct Bank Transfer" -> "Generate off-grid bank wire details"
                                else -> "Generate BTC Lightning / Node payment addresses"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Shipping
            Text("3. Sovereign Shipping Carrier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            carriers.forEach { carrier ->
                val isSelected = selectedCarrier == carrier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCarrier = carrier }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedCarrier = carrier }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(carrier, fontWeight = FontWeight.Medium)
                        Text(
                            text = when (carrier) {
                                "Sovereign Express" -> "Priority next-day autonomous delivery"
                                "National Postal" -> "Slower secure standard postal carrier tracking"
                                else -> "Private air-gapped courier dispatch with extreme privacy"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Place Order Button
            Button(
                onClick = {
                    viewModel.submitCheckout(
                        customerName = name,
                        customerEmail = email,
                        address = address,
                        paymentMethod = selectedPayment,
                        carrier = selectedCarrier,
                        onSuccess = { isSuccess = true }
                    )
                },
                enabled = name.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("place_order_button")
            ) {
                Text("Confirm Sovereign Bill ($${String.format("%.2f", total)})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: SovereignViewModel) {
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Snapshot Card matching Immersive UI html precisely
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background radial blur highlight representation
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(128.dp)
                        .offset(x = 64.dp, y = (-64).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(percent = 50)
                        )
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "REVENUE SNAPSHOT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", totalEarnings)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 28.sp,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                        }

                        // Trend Chip
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF10B981).copy(alpha = 0.15f),
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+14.2%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ledger/Chart bars representation from HTML
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val chartHeights = listOf(0.40f, 0.60f, 0.30f, 0.80f, 0.50f, 0.90f, 0.70f, 0.45f)
                        chartHeights.forEachIndexed { index, heightFraction ->
                            val barColor = if (index == 3 || index == 6) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.05f)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightFraction)
                                    .background(
                                        barColor,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Grid Actions (Manage Products & Active Orders)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Manage Products Button Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.AdminProducts) }
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage Products",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Active Orders Button Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.AdminOrders) }
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Orders",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Node Health monitor section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NODE HEALTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pulsing status dot
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(50))
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), RoundedCornerShape(50))
                            )
                        }
                        Text(
                            text = "Operational",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CPU Usage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CPU Usage", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("24%", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = 0.24f,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PostgreSQL status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PostgreSQL", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Optimal", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = 0.88f,
                            color = Color(0xFF10B981),
                            trackColor = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsScreen(viewModel: SovereignViewModel) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Product Inventory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
                Text("Manage your self-hosted catalog stock.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Inventory is currently empty.", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { prod ->
                    AdminProductRow(product = prod, viewModel = viewModel)
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { t, d, p, c, s, v, col ->
                viewModel.addProduct(t, d, p, c, s, v, col)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AdminProductRow(product: Product, viewModel: SovereignViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Category: ${product.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Text("Price: $${String.format("%.2f", product.price)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Stock Remaining: ${product.stock}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Quick Stock Adjustment
                IconButton(
                    onClick = { viewModel.updateStock(product.id, (product.stock - 1).coerceAtLeast(0)) },
                    modifier = Modifier.testTag("stock_minus_${product.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Stock")
                }
                IconButton(
                    onClick = { viewModel.updateStock(product.id, product.stock + 1) },
                    modifier = Modifier.testTag("stock_plus_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Stock")
                }
                IconButton(
                    onClick = { viewModel.deleteProduct(product.id) },
                    modifier = Modifier.testTag("delete_product_${product.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, price: Double, category: String, stock: Int, variants: String, collection: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var stockStr by remember { mutableStateOf("10") }
    var variants by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf("All") }

    val categories = listOf("Electronics", "Apparel", "Digital Goods", "Home")
    val collections = listOf("All", "Featured", "New Arrivals")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Insert New Sovereign Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_product_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("dialog_product_desc"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_product_price"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_product_stock"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category select
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Collection select
                Text("Catalog Collection", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    collections.forEach { col ->
                        FilterChip(
                            selected = collection == col,
                            onClick = { collection = col },
                            label = { Text(col, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = variants,
                    onValueChange = { variants = it },
                    label = { Text("Variants (comma-separated, e.g. S,M,L)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_product_variants"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val stock = stockStr.toIntOrNull() ?: 0
                            onConfirm(title, description, price, category, stock, variants, collection)
                        },
                        enabled = title.isNotEmpty() && description.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_product_confirm")
                    ) {
                        Text("Save Product")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersScreen(viewModel: SovereignViewModel) {
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Fulfillment Center",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif
        )
        Text("Track, invoice, and dispatch pending client orders.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders received yet.", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders) { order ->
                    AdminOrderCard(order = order, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order, viewModel: SovereignViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var tempTracking by remember { mutableStateOf(order.trackingNumber) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order ID: SVG-${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(order.customerName, fontSize = 12.sp)
                    Text(order.customerEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (order.status) {
                                    "Pending" -> MaterialTheme.colorScheme.errorContainer
                                    "Paid" -> MaterialTheme.colorScheme.primaryContainer
                                    "Shipped" -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                "Pending" -> MaterialTheme.colorScheme.onErrorContainer
                                "Paid" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Shipped" -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> Color.White
                            }
                        )
                    }
                }
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                Text("Items Purchased:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(order.itemsDetails, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Shipping Address:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(order.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Payment / Delivery Configuration:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Method: ${order.paymentMethod} • Carrier: ${order.carrier}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Tracking and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempTracking,
                        onValueChange = { tempTracking = it },
                        label = { Text("Tracking #", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = { viewModel.updateOrderTracking(order.id, tempTracking) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update Ref")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.deleteOrder(order.id) }) {
                        Text("Delete Invoice", color = MaterialTheme.colorScheme.error)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status == "Paid") {
                            Button(
                                onClick = { viewModel.updateOrderStatus(order.id, "Shipped") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ship Parcel")
                            }
                        }
                        if (order.status == "Shipped") {
                            Button(
                                onClick = { viewModel.updateOrderStatus(order.id, "Delivered") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mark Delivered")
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap to view billing & fulfillment parameters", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(viewModel: SovereignViewModel) {
    val settings by viewModel.settings.collectAsState()

    var storeTitleInput by remember { mutableStateOf("") }
    var storeSubtitleInput by remember { mutableStateOf("") }
    var taxRateInput by remember { mutableStateOf("") }
    var shippingRateInput by remember { mutableStateOf("") }

    // Synchronize inputs with saved configs
    LaunchedEffect(settings) {
        storeTitleInput = settings.find { it.key == "store_title" }?.value ?: "Sovereign Store"
        storeSubtitleInput = settings.find { it.key == "store_subtitle" }?.value ?: "Privacy-respecting commerce"
        taxRateInput = settings.find { it.key == "tax_rate" }?.value ?: "12.5"
        shippingRateInput = settings.find { it.key == "shipping_rate" }?.value ?: "9.99"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Platform Configuration",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Serif
        )
        Text("Configure self-hosted parameters, taxes, and storefront metadata.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(20.dp))

        // Store CMS Details
        Text("Storefront CMS settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = storeTitleInput,
            onValueChange = {
                storeTitleInput = it
                viewModel.saveConfigValue("store_title", it)
            },
            label = { Text("Store Title (Banner & Header)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("config_title_field"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = storeSubtitleInput,
            onValueChange = {
                storeSubtitleInput = it
                viewModel.saveConfigValue("store_subtitle", it)
            },
            label = { Text("Store Subtitle / Slogan") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("config_subtitle_field"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Financial configurations
        Text("Tax and shipping parameters", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = taxRateInput,
                onValueChange = {
                    taxRateInput = it
                    viewModel.saveConfigValue("tax_rate", it)
                },
                label = { Text("Tax rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("config_tax_field"),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = shippingRateInput,
                onValueChange = {
                    shippingRateInput = it
                    viewModel.saveConfigValue("shipping_rate", it)
                },
                label = { Text("Shipping Flat Rate ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("config_shipping_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Informational System details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Self-Hosted Stack Info", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hosting: Sovereign VPS - Private Cloud Node", fontSize = 12.sp)
                Text("Engine: FastAPI 0.111.0 with Python 3.12", fontSize = 12.sp)
                Text("Persistence: PostgreSQL 16.3 + Redis 7.2 Cache", fontSize = 12.sp)
                Text("API Docs: https://sovereign.local/api/docs", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
