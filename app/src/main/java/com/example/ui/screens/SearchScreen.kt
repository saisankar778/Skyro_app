package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val dishesList by viewModel.dishesList.collectAsState()
    val restaurantsList by viewModel.restaurantsList.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNightTheme = (appThemeMode == "NIGHT")
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")
    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    // Smooth transition for search query pulsing suggestions
    var searchIndicatorIndex by remember { mutableStateOf(0) }
    val searchTerms = listOf("EatRight", "Biryani", "Burgers", "Hot Pizzas", "Masala Dosa")
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            searchIndicatorIndex = (searchIndicatorIndex + 1) % searchTerms.size
        }
    }

    val themeColor = if (isNightTheme) SkyroColors.CyanGlow else Color(0xFFFC8019)

    // Filter dishes based on typing search query (case-insensitive fuzzy match)
    val filteredDishes = remember(searchQuery, dishesList) {
        if (searchQuery.trim().isEmpty()) {
            emptyList()
        } else {
            dishesList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Is input active and empty
    val isQueryEmpty = searchQuery.trim().isEmpty()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // -------------------------------------------------------------
                // Dynamic/Warm Rounded Top Header Panel (Matches the Swiggy image)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(SkyroColors.getThemeCardBg(appThemeMode))
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column {
                        // Back Arrow + Header title row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.navigateBack() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("search_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Go Back",
                                    tint = if (isNightTheme) Color.White else Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Search for dishes & restaurants",
                                style = SkyroTypography.H2.copy(fontWeight = FontWeight.Bold),
                                color = if (isNightTheme) Color.White else if (isSunnyTheme) textColor else Color.DarkGray,
                                fontSize = 17.sp
                            )
                        }

                        // Rounded high-fidelity Search input field with orange microphone icon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSunnyTheme) Color(0xFFFFFAED) else if (isNightTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text input edit
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                textStyle = if (isNightTheme) {
                                    SkyroTypography.Body.copy(color = Color.White, fontSize = 15.sp)
                                } else {
                                    SkyroTypography.Body.copy(color = if (isSunnyTheme) textColor else Color.Black, fontSize = 15.sp)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_text_input_field"),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Search for '",
                                                color = Color.Gray.copy(alpha = 0.8f),
                                                fontSize = 15.sp
                                            )
                                            AnimatedContent(
                                                targetState = searchTerms[searchIndicatorIndex],
                                                transitionSpec = {
                                                    slideInVertically { h -> h } + fadeIn() togetherWith
                                                            slideOutVertically { h -> -h } + fadeOut()
                                                },
                                                label = "PlaceholderText"
                                            ) { term ->
                                                Text(
                                                    text = term,
                                                    color = Color.Gray.copy(alpha = 0.8f),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "'",
                                                color = Color.Gray.copy(alpha = 0.8f),
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                    innerTextField()
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Custom split line
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(Color.LightGray.copy(alpha = 0.8f))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Red/Orange/Peach Microphone vector 🎤
                            Text(
                                text = "🎤",
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.updateSearchQuery("Biryani") // simulate speech trigger
                                    }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // Results & Dimmed Suggestions Area Below Top Sheet
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Background search items list (visible but content dimmed if query is empty)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 1. Horizontal filters pills row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            val filters = listOf("Filter ⚙️", "Sort by ⌵", "99 Store", "Offers", "Ratings 4.0+")
                            items(filters) { fill ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            if (isNightTheme) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(SkyroColors.getThemeCardBg(appThemeMode))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = fill,
                                        color = if (isNightTheme) Color.White else if (isSunnyTheme) textColor else Color.DarkGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                 }
                            }
                        }

                        // 2. Main Search results or Mock backdrop matching user's image search suggestions
                        if (isQueryEmpty) {
                            // Empty/resting state showing mock canteens
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Domino's Pizza card
                                item {
                                    MockRestaurantDisplayCard(
                                        name = "Domino's Pizza",
                                        rating = "4.3 (13K+)",
                                        location = "Acharya Ranga Nagar, 1.9 km",
                                        cuisines = "Pizzas, Italian • ₹400 for two",
                                        emoji = "🍕🥨",
                                        isNight = isNightTheme,
                                        appThemeMode = appThemeMode
                                    )
                                }

                                // Chinese Wok card
                                item {
                                    MockDishDisplayCard(
                                        restaurantName = "Chinese Wok",
                                        rating = "4.0 (1.0K+)",
                                        location = "Gurunanak colony, 2.6 km",
                                        cuisines = "Chinese, Asian • ₹250 for two",
                                        dishName = "Schezwan Hakka Noodles",
                                        tag = "Buy 1 get 1",
                                        emoji = "🍜🥡",
                                        isNight = isNightTheme,
                                        appThemeMode = appThemeMode
                                    )
                                }

                                item {
                                    SkyroPageFooter(isNight = isNightTheme)
                                }
                            }
                        } else {
                            // Active search matches
                            if (filteredDishes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "No dishes match \"$searchQuery\" inside our database.",
                                            color = if (isNightTheme) Color.White.copy(alpha = 0.6f) else secondaryTextColor,
                                            style = SkyroTypography.Body,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        SkyroPageFooter(isNight = isNightTheme)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp)
                                ) {
                                    items(filteredDishes, key = { it.id }) { dish ->
                                        val restaurant = restaurantsList.firstOrNull { it.id == dish.restaurantId }
                                        val restaurantName = restaurant?.name ?: "Spice Garden"
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(SkyroColors.getThemeCardBg(appThemeMode))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                                .padding(12.dp)
                                                .testTag("search_matching_item_${dish.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .border(1.dp, if (dish.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626), RoundedCornerShape(2.dp))
                                                                .padding(2.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (dish.isVeg) Color(0xFF16A34A) else Color(0xFFDC2626))
                                                            )
                                                        }
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
                                                        color = if (isNightTheme) Color.White else textColor
                                                    )
                                                    Text(
                                                        text = "₹${dish.price.toInt()}",
                                                        style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Black, fontSize = 13.sp),
                                                        color = if (isSunnyTheme) Color(0xFFD84B16) else themeColor
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                val itemQuantity = cartItems.find { it.id == "$restaurantName:${dish.name}" }?.quantity ?: 0
                                                if (itemQuantity > 0) {
                                                    Row(
                                                        modifier = Modifier
                                                            .height(36.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color(0xFF16A34A))
                                                            .padding(horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        IconButton(
                                                            onClick = { viewModel.addDishToCart(dish.name, dish.price, restaurantName) },
                                                            modifier = Modifier.size(28.dp).testTag("search_increase_${dish.id}")
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
                                                            modifier = Modifier.size(28.dp).testTag("search_decrease_${dish.id}")
                                                        ) {
                                                            Text("–", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            viewModel.addDishToCart(dish.name, dish.price, restaurantName)
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF16A34A)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                        modifier = Modifier.testTag("search_add_btn_${dish.id}")
                                                    ) {
                                                        Text(
                                                            text = "+ Add Item",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White
                                                        )
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

                    // -------------------------------------------------------------
                    // Pure High Fidelity Dim/Scrim Overlay (Screenshot look & feel)
                    // -------------------------------------------------------------
                    if (isQueryEmpty) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.52f)) // Authentic dark grey mask exactly like image
                                .clickable { /* Prevents click-through inside list while dimmed */ }
                        )
                    }
                }
            }
        }
    }
}

// Simple Helper composables to mirror exact UI screenshot lists
@Composable
fun MockRestaurantDisplayCard(
    name: String,
    rating: String,
    location: String,
    cuisines: String,
    emoji: String,
    isNight: Boolean,
    appThemeMode: String = "SKYRO_PRESENT"
) {
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SkyroColors.getThemeCardBg(appThemeMode))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = name,
                    style = SkyroTypography.H2,
                    color = if (isNight) Color.White else textColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rating,
                        fontSize = 12.sp,
                        color = if (isNight) Color.LightGray else secondaryTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = location,
                    fontSize = 11.sp,
                    color = if (isSunnyTheme) secondaryTextColor.copy(alpha = 0.8f) else Color.Gray
                )
                Text(
                    text = cuisines,
                    fontSize = 11.sp,
                    color = if (isSunnyTheme) secondaryTextColor.copy(alpha = 0.8f) else Color.Gray
                )
            }
            Text(emoji, fontSize = 42.sp)
        }
    }
}

@Composable
fun MockDishDisplayCard(
    restaurantName: String,
    rating: String,
    location: String,
    cuisines: String,
    dishName: String,
    tag: String,
    emoji: String,
    isNight: Boolean,
    appThemeMode: String = "SKYRO_PRESENT"
) {
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SkyroColors.getThemeCardBg(appThemeMode))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isNight) Color(0xFF0F172A) else if (isSunnyTheme) Color(0xFFFFFAED) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 44.sp)
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color(0xFFEA580C), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(tag, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = restaurantName,
                style = SkyroTypography.H2,
                color = if (isNight) Color.White else textColor,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rating,
                    fontSize = 12.sp,
                    color = if (isNight) Color.LightGray else secondaryTextColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("• $location", fontSize = 11.sp, color = if (isSunnyTheme) secondaryTextColor.copy(alpha = 0.8f) else Color.Gray)
            }
            Text(
                text = cuisines,
                fontSize = 11.sp,
                color = if (isSunnyTheme) secondaryTextColor.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}
