package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dish

import com.example.ui.SkyroViewModel
import com.example.ui.components.AnimatedHeartButton
import com.example.ui.components.GlassCard
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false, // Ignored, we use the centralized theme state
    onBack: () -> Unit
) {
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNightTheme = (appThemeMode == "NIGHT")
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")
    
    val selectedResId by viewModel.selectedRestaurantId.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val restaurantsList by viewModel.restaurantsList.collectAsState()
    val dishesList by viewModel.dishesList.collectAsState()
    val favoriteDishNames by viewModel.favoriteDishNames.collectAsState()

    val restaurant = remember(selectedResId, restaurantsList) {
        restaurantsList.firstOrNull { it.id == selectedResId }
            ?: restaurantsList.firstOrNull()
            ?: com.example.data.Restaurant(
                id = "",
                name = "Loading...",
                cuisine = "",
                rating = 4.0,
                etaMin = 20,
                deliveryFee = 15.0,
                avgCost = 200.0,
                gradientIndex = 0
            )
    }

    val restaurantId = restaurant.id
    val dishes = remember(restaurantId, dishesList) {
        dishesList.filter { it.restaurantId == restaurantId }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Starters/Cafe", "Main Dishes", "Desserts/Drinks")

    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)
    val cardBg = SkyroColors.getThemeCardBg(appThemeMode)
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)
    val themeColor = if (isNightTheme) SkyroColors.CyanGlow else if (isSunnyTheme) Color(0xFFEA580C) else SkyroColors.Sunrise

    // Customizer bottom sheet state
    var showCustomizerSheet by remember { mutableStateOf(false) }
    var customizerDish by remember { mutableStateOf<Dish?>(null) }
    var selectedCrust by remember { mutableStateOf("Classic Hand Tossed") }
    var selectedSize by remember { mutableStateOf("Regular") }
    var customizerQuantity by remember { mutableStateOf(1) }

    val totalItemsInCart = remember(cartItems) { cartItems.sumOf { it.quantity } }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = restaurant.name,
                            style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black),
                            color = if (isSunnyTheme) textColor else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(if (isSunnyTheme) textColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f), CircleShape)
                                .testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isSunnyTheme) textColor else Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .statusBarsPadding()
                    .padding(top = 90.dp) // Offset for custom floating top appbar
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Restaurant summary info banner
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restaurant_info_panel"),
                            isNight = isNightTheme,
                            borderRadius = 24.dp,
                            elevation = 6.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = restaurant.name,
                                            style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                                            color = if (isSunnyTheme) textColor else Color.White
                                        )
                                        Text(
                                            text = restaurant.cuisine,
                                            style = SkyroTypography.Body,
                                            color = if (isSunnyTheme) secondaryTextColor else Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(SkyroColors.Amber, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating stellar icon",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${restaurant.rating}",
                                                color = Color.White,
                                                style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = if (isSunnyTheme) textColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Stats row: Rating, ETA, Delivery expense, drone badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatBox(label = "DRONE ETA", value = "${restaurant.etaMin} Mins", color = themeColor, isSunny = isSunnyTheme, defaultTextColor = textColor)
                                    StatBox(label = "DELIVERY", value = "₹${restaurant.deliveryFee.toInt()}", color = themeColor, isSunny = isSunnyTheme, defaultTextColor = textColor)
                                    StatBox(label = "SPACING TYPE", value = "Drone Eligible", color = Color(0xFF16A34A), isSunny = isSunnyTheme, defaultTextColor = textColor)
                                }
                            }
                        }
                    }

                    // Food tabs selectors
                    item {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = themeColor,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = themeColor
                                )
                            }
                        ) {
                            tabs.forEachIndexed { idx, label ->
                                Tab(
                                    selected = selectedTabIndex == idx,
                                    onClick = { selectedTabIndex = idx },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                        color = if (selectedTabIndex == idx) (if (isSunnyTheme) textColor else Color.White) else (if (isSunnyTheme) secondaryTextColor else Color.White.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }

                    // Menu items
                    val filteredDishes = when (selectedTabIndex) {
                        0 -> dishes.filter { it.price < 100.0 }
                        1 -> dishes.filter { it.price >= 100.0 && it.price < 170.0 }
                        else -> dishes.filter { it.price >= 170.0 || it.name.contains("Coffee") || it.name.contains("Brew") }
                    }

                    if (filteredDishes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No items in this category yet! Try our Best Sellers.",
                                    color = if (isSunnyTheme) secondaryTextColor else Color.White.copy(alpha = 0.5f),
                                    style = SkyroTypography.Caption,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    items(filteredDishes) { dish ->
                        val cartQuantity = cartItems.firstOrNull { it.id == "${restaurant.name}:${dish.name}" }?.quantity ?: 0

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("menu_dish_item_${dish.id}"),
                            isNight = isNightTheme,
                            borderRadius = 16.dp,
                            elevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Food items text
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (dish.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (dish.isBestSeller) {
                                            Box(
                                                modifier = Modifier
                                                    .background(SkyroColors.DeepCoral, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "BESTSELLER",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        val isDishFavorited = favoriteDishNames.contains(dish.name)
                                        AnimatedHeartButton(
                                            isFavorited = isDishFavorited,
                                            onClick = {
                                                viewModel.toggleFavoriteDish(dish.name)
                                            },
                                            size = 18.dp,
                                            iconColor = if (isSunnyTheme) Color.Gray.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.5f),
                                            testTag = "dish_heart_${dish.id}"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dish.name,
                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSunnyTheme) textColor else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dish.description,
                                        style = SkyroTypography.Caption,
                                        color = if (isSunnyTheme) secondaryTextColor else Color.White.copy(alpha = 0.65f),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "₹${dish.price.toInt()}",
                                        style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isSunnyTheme) textColor else themeColor
                                    )
                                    if (dish.name == "Paneer & Capsicum Pizza Mania") {
                                        Text(
                                            text = "Customisable",
                                            color = if (isSunnyTheme) secondaryTextColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Add & Stepper interactive controller
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSunnyTheme) textColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f))
                                        .border(1.dp, if (isSunnyTheme) textColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = cartQuantity > 0,
                                        transitionSpec = {
                                            scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                                        },
                                        label = "StepperTransition"
                                    ) { hasQuantity ->
                                        if (hasQuantity) {
                                            // Stepper Row (+ / number / -)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.addDishToCart(dish.name, dish.price, restaurant.name) },
                                                    modifier = Modifier.size(32.dp).testTag("increase_stepper_${dish.id}")
                                                ) {
                                                    Text(
                                                        text = "+",
                                                        color = if (isSunnyTheme) textColor else Color.White,
                                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                                        modifier = Modifier.testTag("increase_stepper_text_${dish.id}")
                                                    )
                                                }
                                                
                                                // Number with bounce scale
                                                Text(
                                                    text = "$cartQuantity",
                                                    color = if (isSunnyTheme) textColor else Color.White,
                                                    style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.ExtraBold)
                                                )

                                                IconButton(
                                                    onClick = { viewModel.removeDishFromCart(dish.name, restaurant.name) },
                                                    modifier = Modifier.size(32.dp).testTag("decrease_stepper_${dish.id}")
                                                ) {
                                                    Text(
                                                        text = "−",
                                                        color = if (isSunnyTheme) textColor else Color.White,
                                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                                        modifier = Modifier.testTag("decrease_stepper_text_${dish.id}")
                                                    )
                                                }
                                            }
                                        } else {
                                            // Standard + Add pill button
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        if (dish.name == "Paneer & Capsicum Pizza Mania") {
                                                            customizerDish = dish
                                                            customizerQuantity = 1
                                                            selectedCrust = "Classic Hand Tossed"
                                                            selectedSize = "Regular"
                                                            showCustomizerSheet = true
                                                        } else {
                                                            viewModel.addDishToCart(dish.name, dish.price, restaurant.name)
                                                        }
                                                    }
                                                    .testTag("add_item_base_button_${dish.id}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "+ Add Item",
                                                    color = themeColor,
                                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SkyroPageFooter(isNight = isNightTheme)
                    }
                }
            }
        }

        // Bottom floating View Cart Capsule
        AnimatedVisibility(
            visible = totalItemsInCart > 0,
            enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { 100 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16A34A))
                    .clickable { viewModel.navigateTo("cart") }
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemsWord = if (totalItemsInCart == 1) "item" else "items"
                Text(
                    text = "$totalItemsInCart $itemsWord added",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Cart",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Forward to cart screen",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Customizable Slider (BottomSheet popup overlay)
        if (showCustomizerSheet && customizerDish != null) {
            val dish = customizerDish!!
            
            // Dim underlay background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showCustomizerSheet = false }
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Sliding Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(if (isNightTheme) Color(0xFF1A1A2E) else Color.White)
                        .clickable(enabled = false) {} // Prevent click-through
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .navigationBarsPadding()
                ) {
                    // Overlapping custom "X" dismiss button in header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(1.dp))
                                    .padding(1.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF16A34A)))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dish.name,
                                style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black),
                                color = if (isNightTheme) Color.White else Color(0xFF1F2937)
                            )
                        }

                        IconButton(
                            onClick = { showCustomizerSheet = false },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text(
                                text = "✕",
                                color = if (isNightTheme) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Crust Selection
                    Text(
                        text = "Crust",
                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black),
                        color = if (isNightTheme) Color.White else Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNightTheme) Color(0xFF111827) else Color(0xFFF8FAFC))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Classic Hand Tossed",
                                style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                color = if (isNightTheme) Color.White else Color(0xFF1F2937)
                            )
                            Text(
                                text = "Traditional crust, crispy outside, soft inside",
                                style = SkyroTypography.Caption,
                                color = Color.Gray
                            )
                        }
                        RadioButton(
                            selected = selectedCrust == "Classic Hand Tossed",
                            onClick = { selectedCrust = "Classic Hand Tossed" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEA580C))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Size Selection
                    Text(
                        text = "Size",
                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black),
                        color = if (isNightTheme) Color.White else Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNightTheme) Color(0xFF111827) else Color(0xFFF8FAFC))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Regular",
                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                    color = if (isNightTheme) Color.White else Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹109",
                                    style = SkyroTypography.Caption.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹99",
                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Text(
                                text = "Perfect for single person serving (6 inches)",
                                style = SkyroTypography.Caption,
                                color = Color.Gray
                            )
                        }
                        RadioButton(
                            selected = selectedSize == "Regular",
                            onClick = { selectedSize = "Regular" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEA580C))
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom customizable sheet footer row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Horizontal stepper [- 1 +]
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Gray.copy(alpha = 0.15f))
                                .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "–",
                                color = if (isNightTheme) Color.White else Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { if (customizerQuantity > 1) customizerQuantity-- }
                            )
                            Text(
                                text = "$customizerQuantity",
                                color = if (isNightTheme) Color.White else Color.Black,
                                style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "+",
                                color = if (isNightTheme) Color.White else Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { customizerQuantity++ }
                            )
                        }

                        // Vibrant Green Add Item action button
                        Button(
                            onClick = {
                                repeat(customizerQuantity) {
                                    viewModel.addDishToCart(dish.name, dish.price, restaurant.name)
                                }
                                showCustomizerSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier
                                .height(45.dp)
                                .width(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Add Item  | ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "₹${(dish.price * customizerQuantity).toInt()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹${(109.0 * customizerQuantity).toInt()}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color, isSunny: Boolean = false, defaultTextColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
            color = if (isSunny) defaultTextColor.copy(alpha = 0.62f) else Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = SkyroTypography.Body.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = SkyroTypography.PriceMono.fontFamily
            ),
            color = color
        )
    }
}

// Memory sample helper object to load quickly
private object SampleResData {
    fun getRestaurant(id: String): com.example.data.Restaurant {
        return com.example.data.Restaurant(
            id = id,
            name = "Canteen",
            cuisine = "Food",
            rating = 4.5,
            etaMin = 15,
            deliveryFee = 15.0,
            avgCost = 150.0,
            gradientIndex = 0
        )
    }
}
