package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.data.FeaturedRestData
import com.example.data.Restaurant
import com.example.data.Dish
import com.example.ui.SkyroViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.GlassCard
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@Composable
fun SavedItemsScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false // central state takes priority
) {
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNightTheme = (appThemeMode == "NIGHT")
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")

    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)
    val themeColor = if (isNightTheme) SkyroColors.CyanGlow else if (isSunnyTheme) Color(0xFFEA580C) else SkyroColors.Sunrise

    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    val dishesList by viewModel.dishesList.collectAsState()
    val restaurantsList by viewModel.restaurantsList.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val favoriteRestaurantNames by viewModel.favoriteRestaurantNames.collectAsState()
    val favoriteDishNames by viewModel.favoriteDishNames.collectAsState()

    // Filter favorited dishes based on VM favorite state
    val savedDishes = remember(dishesList, favoriteDishNames) {
        dishesList.filter { favoriteDishNames.contains(it.name) }
    }

    val featuredRestaurants = remember(restaurantsList) {
        restaurantsList.map { res ->
            val emoji = when (res.name) {
                "Ak Bakers" -> "🎂🌹✨"
                "Bhimas Indian Kitchen" -> "🥘🌶️🍛"
                "The Pizza Palace" -> "🍕🧀🥤"
                "Sri Venkateswara Sweets" -> "🍬🍯🥛"
                "Spice Garden" -> "🥘🌶️🍛"
                "The Burger Lab" -> "🍔🍟🥤"
                "Café Nimbus" -> "☕🥐🧁"
                "Dragon Noodles" -> "🍜🥟🔥"
                "Paradise" -> "🥘🌶️🍛"
                "Dominos" -> "🍕🧀🥤"
                "US Pizza" -> "🍕🧀🥤"
                "Chat & Chill" -> "🌭🍟🥤"
                "Total Fresh" -> "🥗🍉🥤"
                "Baskin Robbins" -> "🍦🧁🍩"
                "Nescafe" -> "☕🥐🧁"
                else -> "🍽️✨🍔"
            }
            val gradientColors = when (res.name) {
                "Ak Bakers" -> listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))
                "Bhimas Indian Kitchen" -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                "The Pizza Palace" -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                "Sri Venkateswara Sweets" -> listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
                "Spice Garden" -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                "The Burger Lab" -> listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))
                "Café Nimbus" -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                "Dragon Noodles" -> listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
                "Paradise" -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                "Dominos" -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                "US Pizza" -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                "Chat & Chill" -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                "Total Fresh" -> listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
                "Baskin Robbins" -> listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))
                "Nescafe" -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                else -> {
                    when (res.gradientIndex % 4) {
                        0 -> listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8))
                        1 -> listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                        2 -> listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                        else -> listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
                    }
                }
            }
            val timeStr = "${res.etaMin}-${res.etaMin + 5} MINS"
            val costForTwoStr = "₹${(res.avgCost * 2).toInt()} for two"
            val offerStr = res.promoBadge ?: "★ 15% OFF"
            
            FeaturedRestData(
                name = res.name,
                emoji = emoji,
                offer = offerStr,
                time = timeStr,
                rating = String.format("%.1f", res.rating),
                reviews = when (res.name) {
                    "Ak Bakers" -> "(11)"
                    "Bhimas Indian Kitchen" -> "(120+)"
                    "The Pizza Palace" -> "(85)"
                    "Sri Venkateswara Sweets" -> "(250+)"
                    "Paradise" -> "(500+)"
                    "Dominos" -> "(320+)"
                    else -> "(${ (10..150).random() }+)"
                },
                distance = when (res.name) {
                    "Ak Bakers" -> "Auto Nagar, 0.3 km"
                    "Bhimas Indian Kitchen" -> "SRM AP Hostel Road, 0.8 km"
                    "The Pizza Palace" -> "University Plaza, 1.2 km"
                    "Sri Venkateswara Sweets" -> "AP Junction, 2.0 km"
                    "Paradise" -> "SRM AP Food Court, 0.1 km"
                    "Dominos" -> "Central Mall, 1.5 km"
                    else -> "SRM AP Campus, 0.5 km"
                },
                cuisines = res.cuisine,
                costForTwo = costForTwoStr,
                gradientColors = gradientColors,
                id = res.id
            )
        }
    }

    // Filter favorited restaurants based on VM favorite state
    val savedRestaurants = remember(featuredRestaurants, favoriteRestaurantNames) {
        featuredRestaurants.filter { favoriteRestaurantNames.contains(it.name) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(selectedRoute = "saved_items", onRouteSelected = { route ->
                if (route != "saved_items") {
                    viewModel.navigateTo(route)
                }
            }, isNight = isNightTheme)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            if (isNightTheme) {
                com.example.ui.components.CustomStarfield()
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header Display Title
                Text(
                    text = "Saved Hangers & Favorites",
                    color = if (isNightTheme) Color.White else textColor,
                    style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (savedDishes.isEmpty() && savedRestaurants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💖", fontSize = 54.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Favorites Empty",
                                style = SkyroTypography.H2.copy(fontWeight = FontWeight.Bold),
                                color = if (isNightTheme) Color.White else textColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bookmark your favorite campus meals and flight corridors to reorder with a single click.",
                                style = SkyroTypography.Caption,
                                color = if (isNightTheme) Color.White.copy(alpha = 0.5f) else secondaryTextColor.copy(alpha = 0.82f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            SkyroPageFooter(isNight = isNightTheme)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Section 1: Favorite Restaurants (Big Homepage-style Cards)
                        if (savedRestaurants.isNotEmpty()) {
                            item {
                                Text(
                                    text = "FAVORITE CORRIDORS & RESTAURANTS",
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                    color = if (isNightTheme) SkyroColors.CyanGlow else themeColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(savedRestaurants, key = { "rest_" + it.name }) { resData ->
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    SwiggyFeaturedRestaurantCard(
                                        data = resData,
                                        isNight = isNightTheme,
                                        appThemeMode = appThemeMode,
                                        isFavorited = true,
                                        onFavoriteToggle = {
                                            viewModel.toggleFavoriteRestaurant(resData.name)
                                        }
                                    ) {
                                        val targetId = if (resData.id.isNotEmpty()) resData.id else {
                                            when (resData.name) {
                                                "Ak Bakers" -> "res-bakers"
                                                "Bhimas Indian Kitchen" -> "res-bhimas"
                                                "The Pizza Palace" -> "res-pizza"
                                                "Sri Venkateswara Sweets" -> "res-sweets"
                                                else -> "res-spice"
                                            }
                                        }
                                        viewModel.showRestaurantDetails(targetId)
                                    }
                                }
                            }
                        }

                        // Section 2: Favorite Dishes / Food Items (Normal Food Item Cards)
                        if (savedDishes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "FAVORITE MEAL SAVERS",
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                    color = if (isNightTheme) SkyroColors.CyanGlow else themeColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(savedDishes, key = { "dish_" + it.id }) { dish ->
                                val restaurant = restaurantsList.firstOrNull { it.id == dish.restaurantId }
                                val restaurantName = restaurant?.name ?: "Café Nimbus"

                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("saved_favorite_item_${dish.id}"),
                                    isNight = isNightTheme,
                                    appThemeMode = appThemeMode,
                                    borderRadius = 16.dp,
                                    elevation = 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Favorite,
                                                    contentDescription = "Saved icon tag",
                                                    tint = SkyroColors.DeepCoral,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = restaurantName,
                                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
                                                    color = secondaryTextColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = dish.name,
                                                style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                                color = textColor
                                            )
                                            Text(
                                                text = "₹${dish.price.toInt()}",
                                                style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                                color = themeColor
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        val itemQuantity = cartItems.find { it.id == "$restaurantName:${dish.name}" }?.quantity ?: 0
                                        if (itemQuantity > 0) {
                                            Row(
                                                modifier = Modifier
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(themeColor)
                                                    .padding(horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.addDishToCart(dish.name, dish.price, restaurantName) },
                                                    modifier = Modifier.size(28.dp).testTag("saved_increase_${dish.id}")
                                                ) {
                                                    Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Text(
                                                    text = "$itemQuantity",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.removeDishFromCart(dish.name, restaurantName) },
                                                    modifier = Modifier.size(28.dp).testTag("saved_decrease_${dish.id}")
                                                ) {
                                                    Text("–", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(themeColor)
                                                    .clickable {
                                                        viewModel.addDishToCart(dish.name, dish.price, restaurantName)
                                                    }
                                                    .testTag("saved_item_add_to_cart_button_${dish.id}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = "Quick add saved item to payload",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
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
        }
    }
}
