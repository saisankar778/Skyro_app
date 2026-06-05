package com.example.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.data.FeaturedRestData

import com.example.ui.SkyroViewModel
import com.example.ui.components.AnimatedHeartButton
import com.example.ui.components.BottomNavBar
import com.example.ui.components.DroneEtaBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.RestaurantCard
import com.example.ui.components.DishCard
import com.example.ui.components.SkyroPageFooter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// Location imports
import android.location.Location
import android.location.LocationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onCartClick: () -> Unit,
    onTrackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        locationPermissionGranted = fineGranted || coarseGranted
    }

    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNight = (appThemeMode == "NIGHT")
    
    val activeCartItems by viewModel.cartItems.collectAsState()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val userPreference by viewModel.userPreferences.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val restaurantsList by viewModel.restaurantsList.collectAsState()
    val dishesList by viewModel.dishesList.collectAsState()
    val awsConnectionState by viewModel.awsConnectionState.collectAsState()
    val favoriteRestaurantNames by viewModel.favoriteRestaurantNames.collectAsState()
    val deliveryLocations by viewModel.deliveryLocations.collectAsState()
    val categoriesList by viewModel.categories.collectAsState()
    val selectedDropLocationId by viewModel.selectedDropLocationId.collectAsState()
    val selectedDropLocationName by viewModel.selectedDropLocationName.collectAsState()

    // Interactive vegetarian selector state
    var showVegOnly by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Optimizations to prevent scrolling lag / glitches on frame swaps
    val bestSellerDishes = remember(dishesList) {
        dishesList.filter { it.isBestSeller }
    }
    val recentOrders = remember(allOrders) {
        allOrders.take(2)
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

    // -------------------------------------------------------------
    // WOW #1 State Management: Drone Drop Entrance (Only runs on very first home load)
    // -------------------------------------------------------------
    val isIntroAlreadySeen = viewModel.hasCompletedHomeIntro
    var animationStage by remember { mutableStateOf(if (isIntroAlreadySeen) 2 else 0) } // 0 = drop, 1 = hovering-idle, 2 = flight-to-corner & final view
    
    // Dynamic Bottom Bar visibility based on scroll direction
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val scrollTracking = remember {
        object {
            var lastIndex = 0
            var lastOffset = 0
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val prevIndex = scrollTracking.lastIndex
                val prevOffset = scrollTracking.lastOffset
                
                if (index > prevIndex) {
                    if (isBottomBarVisible) isBottomBarVisible = false
                } else if (index < prevIndex) {
                    if (!isBottomBarVisible) isBottomBarVisible = true
                } else {
                    if (offset > prevOffset + 15) {
                        if (isBottomBarVisible) isBottomBarVisible = false
                    } else if (offset < prevOffset - 15) {
                        if (!isBottomBarVisible) isBottomBarVisible = true
                    }
                }
                scrollTracking.lastIndex = index
                scrollTracking.lastOffset = offset
            }
    }
    
    // Y position drops from -200dp to 80dp
    val droneY = remember { Animatable(-200f) }
    
    // Infinite bobbing transition
    val infiniteTransition = rememberInfiniteTransition(label = "DroneBob")
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bobbing"
    )

    // Staggered contents reveal states
    var revealHeader by remember { mutableStateOf(isIntroAlreadySeen) }
    var revealSearch by remember { mutableStateOf(isIntroAlreadySeen) }
    var revealBanners by remember { mutableStateOf(isIntroAlreadySeen) }
    var revealCategories by remember { mutableStateOf(isIntroAlreadySeen) }
    var revealActiveOrder by remember { mutableStateOf(isIntroAlreadySeen) }
    var revealMainLists by remember { mutableStateOf(isIntroAlreadySeen) }

    LaunchedEffect(Unit) {
        if (!isIntroAlreadySeen) {
            // Step 1: Animate drop from top with Overshoot effect
            delay(300)
            droneY.animateTo(
                targetValue = 80f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            animationStage = 1 // Drone enters hover mode

            // Step 2: Content faded-in staggering 150ms apart
            delay(800)
            revealHeader = true
            delay(150)
            revealSearch = true
            delay(150)
            revealBanners = true
            delay(150)
            revealCategories = true
            delay(150)
            revealActiveOrder = true
            delay(150)
            revealMainLists = true
            
            // Step 3: Fly away up-right to header icon
            delay(1500)
            animationStage = 2
            viewModel.hasCompletedHomeIntro = true
        } else {
            // Instantly fully visible
            animationStage = 2
            revealHeader = true
            revealSearch = true
            revealBanners = true
            revealCategories = true
            revealActiveOrder = true
            revealMainLists = true
        }
    }

    // Drone flight path interpolation from center (x: screenCenter, y: 80) to top-right (x: topRight, y: statusBars)
    val droneHeaderScale by animateFloatAsState(
        targetValue = if (animationStage == 2) 0.35f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "DroneHeaderScale"
    )

    val droneHeaderY by animateFloatAsState(
        targetValue = if (animationStage == 2) 12f else droneY.value + bobbingOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "DroneHeaderY"
    )

    val droneHeaderXOffset by animateFloatAsState(
        targetValue = if (animationStage == 2) 75f else 0f, // Interpolates from center to right-half offset
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "DroneHeaderXOffset"
    )

    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = (animationStage == 2) && isBottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                BottomNavBar(selectedRoute = "home", onRouteSelected = { route ->
                    if (route != "home") {
                        viewModel.navigateTo(route)
                    }
                }, isNight = isNight)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // Underlay blinking starfield for Night Theme
            if (isNight) {
                com.example.ui.components.CustomStarfield()
            }

            // Central scrolling dashboard
            if (animationStage == 2) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 95.dp), // Safe clearance for top appbar
                    contentPadding = PaddingValues(bottom = 125.dp, start = 0.dp, end = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    
                    // -------------------------------------------------------------
                    // 2. High fidelity white Search bar + Vegetarian Switch Filter
                    // -------------------------------------------------------------
                    item {
                        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                            SwiggySearchBarRow(
                                isNight = isNight,
                                onSearchClick = { viewModel.navigateTo("search_screen") },
                                showVegOnly = showVegOnly,
                                onVegToggle = { showVegOnly = it }
                            )
                        }
                    }

                    // -------------------------------------------------------------
                    // 3. Auto-scrolling Match Day Promo cricket banner
                    // -------------------------------------------------------------
                    item {
                        SwiggyCricketPromoBanner()
                    }

                    // -------------------------------------------------------------
                    // 5. Unified super.money partner offer separator banner
                    // -------------------------------------------------------------
                    item {
                        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                            SwiggySuperOfferDivider()
                        }
                    }

                    // -------------------------------------------------------------
                    // Active Order tracker card removed from home screen (on-nav wave tracking active instead)
                    // -------------------------------------------------------------

                    // -------------------------------------------------------------
                    // 9. Circular Cuisines Section (Specials, South Indian, Biryani)
                    // -------------------------------------------------------------
                    item {
                        SwiggyCircularCuisines(categoriesList = categoriesList, isNight = isNight) { cat ->
                            viewModel.selectCategory(cat)
                        }
                    }



                    // -------------------------------------------------------------
                    // 11. Large featured bakery/cake card (Ak Bakers & more)
                    // -------------------------------------------------------------
                    featuredRestaurants.forEach { resData ->
                        item {
                            val isFavorited = favoriteRestaurantNames.contains(resData.name)
                            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                SwiggyFeaturedRestaurantCard(
                                    data = resData,
                                    isNight = isNight,
                                    appThemeMode = appThemeMode,
                                    isFavorited = isFavorited,
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



                    item {
                        SkyroPageFooter(isNight = isNight, showExtended = true)
                    }
                }
            }

            // Custom high fidelity Floating Header bar (Active only in Stage 2)
            AnimatedVisibility(
                visible = revealHeader,
                enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left profile details & location pill — shows the selected delivery block
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showLocationDialog = true }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                                .testTag("home_location_selector")
                        ) {
                            // Use the selected delivery location name (from the fetched API locations)
                            // instead of the raw user address string
                            val displayName = if (selectedDropLocationName.isNotEmpty() &&
                                selectedDropLocationName != "Select Delivery Location"
                            ) {
                                selectedDropLocationName
                            } else {
                                (userPreference?.address ?: "SRM AP Campus")
                                    .let { if (it.contains("JNTU")) "SRM AP Campus" else it }
                            }
                            // Pass the actual selected location name to the building icon
                            // so block detection is accurate
                            SrmCampusBuildingIcon(address = displayName, modifier = Modifier.padding(end = 6.dp))
                            val cleanedDisplay = displayName.replace("📍", "").replace("SRM AP ", "").trim()
                            Text(
                                text = cleanedDisplay,
                                style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Dropdown address select",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Right action buttons spacer: cart with animation bounce badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cart badge bounce animate
                        val badgeScale by animateFloatAsState(
                            targetValue = if (activeCartItems.isNotEmpty()) 1.2f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "BadgeScale"
                        )

                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .scale(badgeScale)
                                .testTag("cart_navigation_badge")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Cart page",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (activeCartItems.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.TopEnd)
                                            .background(SkyroColors.DeepCoral, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${activeCartItems.sumOf { it.quantity }}",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // WOW #1 Drone Animation Layer - Removed for a cleaner look
            // -------------------------------------------------------------
        }
    }

    if (showLocationDialog) {
        // Pre-select the currently chosen delivery location (from ViewModel)
        var selectedLocId by remember { mutableStateOf(selectedDropLocationId) }
        var selectedLocName by remember { mutableStateOf(selectedDropLocationName) }
        var customAddress by remember { mutableStateOf("") }
        
        val displayPresets = remember(deliveryLocations) {
            if (deliveryLocations.isEmpty()) {
                listOf(
                    Triple("", "SR Block", "🏫"),
                    Triple("", "C Block", "🏢"),
                    Triple("", "Admin Block", "🏛️"),
                    Triple("", "Yamuna Hostel", "🏨"),
                    Triple("", "V & G Hostels", "🏢")
                )
            } else {
                deliveryLocations.map { loc ->
                    val emoji = when {
                        loc.name.contains("SR", ignoreCase = true) && loc.name.contains("Block", ignoreCase = true) -> "🏫"
                        loc.name.contains("C", ignoreCase = true) && loc.name.contains("Block", ignoreCase = true) -> "🏢"
                        loc.name.contains("Admin", ignoreCase = true) -> "🏛️"
                        loc.name.contains("Yamuna", ignoreCase = true) -> "🏨"
                        loc.name.contains("V", ignoreCase = true) && (loc.name.contains("G", ignoreCase = true) || loc.name.contains("Hostel", ignoreCase = true)) -> "🏢"
                        else -> "📍"
                    }
                    Triple(loc.id, loc.name, emoji)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (customAddress.isNotBlank()) {
                            // Custom address typed — update the user address string only
                            viewModel.updateAddress(customAddress)
                        } else if (selectedLocId.isNotEmpty()) {
                            // A fetched delivery location was picked — set it properly
                            viewModel.selectDeliveryLocation(selectedLocId, selectedLocName)
                            viewModel.updateAddress("📍 $selectedLocName")
                        }
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyroColors.DeepCoral
                    )
                ) {
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel", color = if (isNight) Color.White else Color.Black)
                }
            },
            title = {
                Text(
                    "Select Delivery Block",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = if (isNight) Color.White else Color.Black
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Pick your delivery drop zone on campus:",
                        fontSize = 12.sp,
                        color = (if (isNight) Color.LightGray else Color.DarkGray).copy(alpha = 0.8f)
                    )

                    var isDetectingLocation by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = {
                            if (!locationPermissionGranted) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                coroutineScope.launch {
                                    isDetectingLocation = true
                                    val userLocation = getDeviceLocation(context, locationManager)
                                    if (userLocation != null && deliveryLocations.isNotEmpty()) {
                                        val nearest = deliveryLocations.minByOrNull { loc ->
                                            getDistance(userLocation.latitude, userLocation.longitude, loc.latitude, loc.longitude)
                                        }
                                        if (nearest != null) {
                                            selectedLocId = nearest.id
                                            selectedLocName = nearest.name
                                            customAddress = ""
                                            viewModel.selectDeliveryLocation(nearest.id, nearest.name)
                                        }
                                    }
                                    isDetectingLocation = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                            contentColor = if (isNight) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isDetectingLocation) {
                                CircularProgressIndicator(
                                    color = SkyroColors.DeepCoral,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Detecting closest block...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = SkyroColors.DeepCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Detect Nearest Block", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        displayPresets.forEach { (locId, presetName, emoji) ->
                            val isSelected = locId.isNotEmpty() && locId == selectedLocId && customAddress.isBlank()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) SkyroColors.DeepCoral.copy(alpha = 0.15f)
                                        else (if (isNight) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SkyroColors.DeepCoral
                                        else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedLocId = locId
                                        selectedLocName = presetName
                                        customAddress = ""
                                        // Immediately update the ViewModel so the header reflects it
                                        if (locId.isNotEmpty()) {
                                            viewModel.selectDeliveryLocation(locId, presetName)
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = presetName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isNight) Color.White else Color.Black
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("✓", color = SkyroColors.DeepCoral, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = customAddress,
                        onValueChange = { customAddress = it },
                        label = { Text("Or Type Custom Address", fontSize = 11.sp) },
                        placeholder = { Text("E.g. JNTU Hostel / Sector 4", fontSize = 12.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = if (isNight) Color.White else Color.Black),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isNight) Color.White else Color.Black,
                            unfocusedTextColor = if (isNight) Color.White else Color.Black,
                            focusedBorderColor = SkyroColors.DeepCoral,
                            unfocusedBorderColor = (if (isNight) Color.White else Color.Black).copy(alpha = 0.2f)
                        )
                    )
                }
            },
            containerColor = if (isNight) Color(0xFF1E1E2C) else Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// Complete Vector Drone drawing in Compose Canvas to replicate Lottie look with zero dynamic load risks
@Composable
fun DroneGraphicElement() {
    Box(
        modifier = Modifier
            .size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.22f

            // Spanning landing gear legs
            drawLine(
                color = Color.LightGray,
                start = center + androidx.compose.ui.geometry.Offset(-30f, 35f),
                end = center + androidx.compose.ui.geometry.Offset(-40f, 70f),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color.LightGray,
                start = center + androidx.compose.ui.geometry.Offset(30f, 35f),
                end = center + androidx.compose.ui.geometry.Offset(40f, 70f),
                strokeWidth = 3.dp.toPx()
            )
            // Landing balance bars
            drawLine(
                color = Color.DarkGray,
                start = center + androidx.compose.ui.geometry.Offset(-55f, 70f),
                end = center + androidx.compose.ui.geometry.Offset(55f, 70f),
                strokeWidth = 4.dp.toPx()
            )

            // Spanning motorized wing arms
            drawLine(
                color = Color.White,
                start = center - androidx.compose.ui.geometry.Offset(70f, 40f),
                end = center + androidx.compose.ui.geometry.Offset(70f, 40f),
                strokeWidth = 5.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = center - androidx.compose.ui.geometry.Offset(-70f, 40f),
                end = center + androidx.compose.ui.geometry.Offset(-70f, 40f),
                strokeWidth = 5.dp.toPx()
            )

            // Rotating spinning blade visual effect with dashed arcs
            val circleStyle = Stroke(width = 2.dp.toPx())
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = center - androidx.compose.ui.geometry.Offset(70f, 40f), style = circleStyle)
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = center + androidx.compose.ui.geometry.Offset(70f, 40f), style = circleStyle)
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = center - androidx.compose.ui.geometry.Offset(-70f, 40f), style = circleStyle)
            drawCircle(Color.White.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = center + androidx.compose.ui.geometry.Offset(-70f, 40f), style = circleStyle)

            // Drone central fuselage core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.LightGray)
                ),
                radius = baseRadius
            )

            // Cyber-themed glow status dome
            drawCircle(
                color = SkyroColors.CyanGlow,
                radius = baseRadius * 0.45f,
                center = center - androidx.compose.ui.geometry.Offset(0f, 10f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromoBannersSection(isNight: Boolean) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutine = rememberCoroutineScope()

    // Banners auto scrolling
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val banners = listOf(
        PromoBanner(
            headline = "🚁 FREE DELIVERY ON\nFIRST ORDER",
            subCopy = "Order from any campus canteen or bistro.",
            gradient = listOf(SkyroColors.Sunrise, SkyroColors.Amber)
        ),
        PromoBanner(
            headline = "⚡ EXPRESS DRONE FLIGHT\n8 MINUTED ETA",
            subCopy = "Propelled flight delivers warm and fresh.",
            gradient = listOf(SkyroColors.DeepCoral, SkyroColors.GoldenHour)
        ),
        PromoBanner(
            headline = "🔥 30% OFF ABOVE\n₹299 ON SPICE GARDEN",
            subCopy = "Satisfy midnight dynamic biryani cravings.",
            gradient = listOf(SkyroColors.VioletPulse, Color(0xFFE52D27))
        )
    )

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(banner.gradient))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        text = banner.headline,
                        color = Color.White,
                        style = SkyroTypography.H2.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 22.sp,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.subCopy,
                        color = Color.White.copy(alpha = 0.85f),
                        style = SkyroTypography.Caption
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        // Auto Smooth Indicator Dots (worm-style selection highlight)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until 3) {
                val isActive = pagerState.currentPage == i
                val dotWidth by animateDpAsState(
                    targetValue = if (isActive) 20.dp else 6.dp, 
                    label = "WormIndicatorAnimate"
                )
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .height(6.dp)
                        .width(dotWidth)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise)
                )
            }
        }
    }
}

private data class PromoBanner(
    val headline: String,
    val subCopy: String,
    val gradient: List<Color>
)

@Composable
fun CategoryRowSection(
    selected: String,
    onSelected: (String) -> Unit,
    isNight: Boolean
) {
    val categories = listOf("🍕 Pizza", "🍔 Burgers", "🍱 Biryani", "🥗 Healthy", "☕ Café", "🍜 Noodles", "🍦 Desserts")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { cat ->
            val isChosen = selected == cat
            val cardBg = if (isChosen) {
                Brush.linearGradient(listOf(SkyroColors.Sunrise, SkyroColors.DeepCoral))
            } else {
                null
            }

            val sizeScale by animateFloatAsState(
                targetValue = if (isChosen) 1.05f else 1.0f,
                label = "CategoryPillScale"
            )

            Box(
                modifier = Modifier
                    .scale(sizeScale)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isChosen && cardBg != null) {
                            Modifier.background(cardBg)
                        } else {
                            Modifier.background(if (isNight) SkyroColors.GlassDark else SkyroColors.GlassWhite)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isChosen) Color.Transparent else (if (isNight) SkyroColors.GlassBorderDark else SkyroColors.GlassBorderLight),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelected(cat) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cat,
                    color = Color.White,
                    style = SkyroTypography.Body.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
fun MainActiveOrderCard(
    order: DeliveryOrder,
    isNight: Boolean,
    onTrackClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .testTag("home_active_order_card"),
        isNight = isNight,
        borderRadius = 20.dp,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE DRONE DELIVERY",
                        color = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise,
                        style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${order.restaurantName} is 1.2km away",
                    color = Color.White,
                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "ETA ${order.etaMinutes} min · Status: ${order.status}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = SkyroTypography.Caption.copy(fontFamily = SkyroTypography.PriceMono.fontFamily)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Modern 4-step progress bar
                val activeStep = when (order.status) {
                    "PREPARING" -> 1
                    "DISPATCHED" -> 2
                    "IN_FLIGHT" -> 3
                    else -> 4
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (step in 1..4) {
                        val barColor = if (step <= activeStep) {
                            if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onTrackClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise
                )
            ) {
                Text("Track", style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun SrmCampusBuildingIcon(
    address: String = "SRM AP SR Block",
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Determine which block matches the active address
    val blockId = when {
        address.contains("Yamuna", ignoreCase = true) -> "yamuna_hostel"
        address.contains("V & G", ignoreCase = true) || address.contains("V_and_G", ignoreCase = true) || address.contains("V and G", ignoreCase = true) -> "v_and_g_hostels"
        address.contains("Admin", ignoreCase = true) -> "admin_block"
        address.contains("C Block", ignoreCase = true) || address.contains("C_Block", ignoreCase = true) -> "c_block"
        else -> "sr_block"
    }

    val gradientColors = when (blockId) {
        "c_block" -> listOf(Color(0xFF7B2FFF), Color(0xFFEC4899))
        "admin_block" -> listOf(Color(0xFF00D4FF), Color(0xFF0D1B4B))
        "yamuna_hostel" -> listOf(Color(0xFF00B0FF), Color(0xFF00E676))
        "v_and_g_hostels" -> listOf(Color(0xFFFF5252), Color(0xFFFF7A00))
        else -> listOf(Color(0xFFFC8019), Color(0xFFFFB347)) // sr_block
    }

    val emoji = when (blockId) {
        "c_block" -> "🏢"
        "admin_block" -> "🏛️"
        "yamuna_hostel" -> "🏨"
        "v_and_g_hostels" -> "🏢"
        else -> "🏫" // sr_block
    }

    // Attempt to decode the asset image asynchronously with graceful fallback
    val painter = remember(blockId) {
        try {
            context.assets.open("$blockId.png").use { stream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    androidx.compose.ui.graphics.painter.BitmapPainter(bitmap.asImageBitmap())
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = "Campus Block",
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            Text(
                text = emoji,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Custom subtle looping/rotating interactive Skyro Logo component for app header branding
@Composable
fun HeaderSkyroLogo(isNight: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // High fidelity elegant letter mark
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    Brush.linearGradient(
                        colors = if (isNight) {
                            listOf(SkyroColors.CyanGlow, SkyroColors.NightPurple)
                        } else {
                            listOf(SkyroColors.DeepCoral, SkyroColors.Amber)
                        }
                    ),
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Branding typography with a futuristic drop shadow glow matching night/day themes
        Text(
            text = "SKYRO",
            color = Color.White,
            style = SkyroTypography.Display.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = (if (isNight) SkyroColors.CyanGlow else SkyroColors.DeepCoral).copy(alpha = 0.5f),
                    blurRadius = 8f
                )
            )
        )
    }
}

// -------------------------------------------------------------
// Swiggy-themed Polished Composable Widgets matching Screenshots
// -------------------------------------------------------------

@Composable
fun SwiggyCategoryHeader(isNight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Food (Selected/Active)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(95.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.35f))
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("🍔", fontSize = 34.sp)
                Text("Food", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Instamart (with "25 mins" dynamic badge sticker above)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(95.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.35f))
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-4).dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("25 mins", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxSize().padding(bottom = 0.dp)
                ) {
                    Text("🛍️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Instamart", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 3. Dineout
        Box(
            modifier = Modifier
                .weight(1f)
                .height(95.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.35f))
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("🛎️", fontSize = 28.sp)
                Text("Dineout", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // 4. Giftables
        Box(
            modifier = Modifier
                .weight(1f)
                .height(95.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.35f))
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("🎁", fontSize = 28.sp)
                Text("Giftables", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SwiggySearchBarRow(
    isNight: Boolean,
    onSearchClick: () -> Unit,
    showVegOnly: Boolean,
    onVegToggle: (Boolean) -> Unit
) {
    var searchIndicatorIndex by remember { mutableStateOf(0) }
    val searchTerms = listOf("Sweets", "Biryani", "Burgers", "Hot Pizzas", "Masala Dosa")
    LaunchedEffect(Unit) {
        while (true) {
            delay(2800)
            searchIndicatorIndex = (searchIndicatorIndex + 1) % searchTerms.size
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search bar card
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onSearchClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search for ",
                        color = Color.Gray.copy(alpha = 0.8f),
                        style = SkyroTypography.Body,
                        fontSize = 14.sp
                    )
                    AnimatedContent(
                        targetState = searchTerms[searchIndicatorIndex],
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "SearchTextScroll"
                    ) { term ->
                        Text(
                            text = "'$term'",
                            color = Color.Gray.copy(alpha = 0.8f),
                            style = SkyroTypography.Body,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Divider and Mic icon exactly matching screenshots
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color.LightGray.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                // Custom high-performance drawn Microphone vector icon
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(16.dp, 22.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val col = Color(0xFFFF521C) // Vibrant orange mic color
                    
                    // 1. Microphone body (rounded capsule)
                    val capsuleWidth = w * 0.48f
                    val capsuleHeight = h * 0.55f
                    val capsuleLeft = (w - capsuleWidth) / 2
                    val capsuleTop = h * 0.05f
                    drawRoundRect(
                        color = col,
                        topLeft = androidx.compose.ui.geometry.Offset(capsuleLeft, capsuleTop),
                        size = androidx.compose.ui.geometry.Size(capsuleWidth, capsuleHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(capsuleWidth / 2, capsuleWidth / 2)
                    )
                    
                    // 2. Semicircle cradle
                    val strokeWidth = w * 0.10f
                    val cradleRadius = w * 0.38f
                    drawArc(
                        color = col,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(w / 2 - cradleRadius, h * 0.20f),
                        size = androidx.compose.ui.geometry.Size(cradleRadius * 2, cradleRadius * 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    
                    // 3. Vertical stem/stand
                    drawLine(
                        color = col,
                        start = androidx.compose.ui.geometry.Offset(w / 2, h * 0.62f),
                        end = androidx.compose.ui.geometry.Offset(w / 2, h * 0.88f),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // VEG filter card exactly matching screenshots
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onVegToggle(!showVegOnly) }
                .padding(vertical = 4.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "VEG",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                if (showVegOnly) {
                    // State ON (Image 1): Green active track, with white thumb containing plant detail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFF0FAF59)) // Vibrant green in toggle track
                            .padding(2.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0FAF59)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                } else {
                    // State OFF (Image 2): Green Indian Veg mark on the left, grey switch on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Trademark green box + dot symbol
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(1.2.dp, Color(0xFF0FAF59), RoundedCornerShape(3.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0FAF59))
                            )
                        }
                        
                        // Switch (grey track, thumb on left)
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(13.dp)
                                .clip(RoundedCornerShape(6.5.dp))
                                .background(Color(0xFFE2E2E9))
                                .padding(1.5.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class CricketMatchBannerData(
    val team1: String,
    val team2: String,
    val team1Logo: String,
    val team2Logo: String,
    val gradientColors: List<Color>,
    val discountText: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwiggyCricketPromoBanner() {
    val matches = remember {
        listOf(
            CricketMatchBannerData(
                team1 = "KOL",
                team2 = "DEL",
                team1Logo = "🟣 🏏",
                team2Logo = "🔵 🏏",
                gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81)),
                discountText = "FLAT ₹200 OFF"
            ),
            CricketMatchBannerData(
                team1 = "CSK",
                team2 = "MI",
                team1Logo = "🟡 🏏",
                team2Logo = "🔵 🏏",
                gradientColors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF92400E)),
                discountText = "FLAT ₹200 OFF"
            ),
            CricketMatchBannerData(
                team1 = "RCB",
                team2 = "SRH",
                team1Logo = "🔴 🏏",
                team2Logo = "🟠 🏏",
                gradientColors = listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF991B1B)),
                discountText = "FLAT ₹200 OFF"
            ),
            CricketMatchBannerData(
                team1 = "GT",
                team2 = "RR",
                team1Logo = "🔵 🏏",
                team2Logo = "💖 🏏",
                gradientColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77)),
                discountText = "FLAT ₹200 OFF"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { matches.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            val nextPage = (pagerState.currentPage + 1) % matches.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 14.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val match = matches[page]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = match.gradientColors
                        )
                    )
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(match.team1Logo, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = match.team1,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VS",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = match.team2,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(match.team2Logo, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Order Now »",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = match.discountText,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SwiggyFourCardDeals() {
    val items = listOf(
        DealCardData("Get 66% OFF\nFor 10 Mins", "🏏 SKYRO\nSIXES", Color(0xFF0284C7), Color(0xFF0369A1)),
        DealCardData("Irresistible\nDeals", "🟢 MIN\n50% OFF", Color(0xFF059669), Color(0xFF047857)),
        DealCardData("Win ₹100\nFree Cash", "🪙 FREE\nCASH", Color(0xFF4F46E5), Color(0xFF4338CA)),
        DealCardData("Delightful\nDeals", "🎟️ FLAT\n₹200 OFF", Color(0xFFD97706), Color(0xFFB45309))
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .height(145.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(item.colorStart, item.colorEnd)
                        )
                    )
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 14.sp
                    )
                    
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private data class DealCardData(
    val title: String,
    val badge: String,
    val colorStart: Color,
    val colorEnd: Color
)

@Composable
fun SwiggySuperOfferDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SUPER OFFER WITH ",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "✨ super.money",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun SwiggyBrandPromosRow(isNight: Boolean, onCartAdd: (String, Double) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Taco Bell Card
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isNight) Color(0xFF1E293B) else Color.White)
                .clickable { onCartAdd("Loaded Cheese Fries", 99.0) }
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7C3AED), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("TACO BELL", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Get 66% OFF\nFor 10 Mins",
                        color = if (isNight) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 15.sp
                    )
                    Text("Crispy taco goodness", color = Color.Gray, fontSize = 9.sp)
                }
                
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🌮🥤", fontSize = 34.sp)
                }
            }
        }

        // 2. Wow! Momo Card
        Box(
            modifier = Modifier
                .weight(1.1f)
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(
                    if (isNight) {
                        Modifier.background(Color(0xFF1F2937))
                    } else {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                            )
                        )
                    }
                )
                .clickable { onCartAdd("Veg Momos (6pc)", 89.0) }
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDC2626), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("WOW! MOMO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Get items at\njust ₹59*",
                        color = Color(0xFF78350F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 16.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF78350F))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ORDER NOW", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                Column(
                    modifier = Modifier.weight(0.8f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🥟🍙", fontSize = 30.sp)
                }
            }
        }
    }
}

@Composable
fun Swiggy99StoreSection(isNight: Boolean, onAddClick: (String, Double, String) -> Unit) {
    val items = listOf(
        StoreItemData("Plain Dosa", 80.0, 49.0, 4.5, "1.6K+", "Srinivasa Fast ...", "🥞"),
        StoreItemData("Rava Dosa", 119.0, 99.0, 4.5, "2.9K+", "Bengaluru Bh ...", "🧇"),
        StoreItemData("Veg Momos (6pc)", 89.0, 59.0, 4.4, "4.2K+", "Dragon Noodles", "🥟"),
        StoreItemData("Loaded Fries", 139.0, 99.0, 4.4, "332", "The Burger Lab", "🍟")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isNight) Color(0xFF1E1E2E) else Color.White)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "99 store",
                    color = if (isNight) SkyroColors.CyanGlow else Color(0xFF1E293B),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("✅", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Meals at ₹99 + Free Delivery",
                    color = if (isNight) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "View All >",
                color = if (isNight) SkyroColors.CyanGlow else Color(0xFFFC8019),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .width(135.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isNight) Color(0xFF2E2E3E) else Color(0xFFF8FAFC))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNight) Color(0xFF1E1E2E) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 42.sp)
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                                    .clickable { onAddClick(item.name, item.price, item.restaurant) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(1.dp))
                                    .padding(1.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF16A34A)))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${item.rating} (${item.reviews})",
                                    color = if (isNight) Color.White else Color.DarkGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = item.name,
                            color = if (isNight) Color.White else Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "₹${item.originalPrice.toInt()}",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF08A), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "₹${item.price.toInt()}",
                                    color = Color(0xFF854D0E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = item.restaurant,
                            color = Color.Gray,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class StoreItemData(
    val name: String,
    val originalPrice: Double,
    val price: Double,
    val rating: Double,
    val reviews: String,
    val restaurant: String,
    val emoji: String
)

@Composable
fun SwiggyCashOfferBanner(isNight: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8))
                )
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Use your ₹15 Free Cash",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "on order above ₹99",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FREE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text("₹15", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SwiggyCircularCuisines(
    categoriesList: List<com.example.data.ApiCategory>,
    isNight: Boolean,
    onSelect: (String) -> Unit
) {
    val items = remember(categoriesList) {
        if (categoriesList.isEmpty()) {
            listOf(
                CuisineItem("Specials", "🎂"),
                CuisineItem("South Indian", "🥞"),
                CuisineItem("Biryani", "🍱"),
                CuisineItem("North Indian", "🥘"),
                CuisineItem("Dessert", "🍰")
            )
        } else {
            categoriesList
                .filter { it.name != "All" }
                .map { cat ->
                    val emoji = when (cat.name) {
                        "Pizza" -> "🍕"
                        "Biryani" -> "🍱"
                        "Snacks" -> "🌯"
                        "Beverages" -> "🥤"
                        "Desserts", "Dessert", "Desserts & Bakes" -> "🍰"
                        "Healthy" -> "🥗"
                        "Coffee" -> "☕"
                        "Ice Cream" -> "🍦"
                        "Specials" -> "🎂"
                        "South Indian" -> "🥞"
                        "North Indian" -> "🥘"
                        else -> cat.emoji.ifEmpty { "🍽️" }
                    }
                    CuisineItem(cat.name, emoji)
                }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                text = "What's on your mind?",
                color = if (isNight) Color.White else Color.Black,
                style = SkyroTypography.H2.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { onSelect(item.name) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.emoji, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.name,
                        color = if (isNight) Color.White else Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class CuisineItem(val name: String, val emoji: String)

@Composable
fun SwiggyFiltersPillsRow(isNight: Boolean) {
    val filters = listOf("Filter ⚙️", "Sort by ⌵", "99 Store", "Offers", "Ratings 4.0+")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        items(filters) { fill ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isNight) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .background(if (isNight) Color(0xFF1E293B) else Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = fill,
                    color = if (isNight) Color.White else Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun SwiggyFeaturedRestaurantCard(
    data: FeaturedRestData,
    isNight: Boolean,
    appThemeMode: String,
    isFavorited: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    val cardBg = SkyroColors.getThemeCardBg(appThemeMode)
    val borderBrush = SkyroColors.getThemeBorderBrush(appThemeMode)
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (appThemeMode != "SKYRO_PRESENT") {
                    Modifier.border(1.dp, borderBrush, RoundedCornerShape(18.dp))
                } else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.linearGradient(
                            colors = data.gradientColors
                        )
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(data.emoji, fontSize = 64.sp)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0xFFEA580C), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = data.offer,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(28.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedHeartButton(
                        isFavorited = isFavorited,
                        onClick = onFavoriteToggle,
                        size = 18.dp,
                        testTag = "featured_heart_${data.name}"
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = data.time,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.name,
                        color = if (isNight) Color.White else textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("★", color = Color.White, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${data.rating} ${data.reviews} • ${data.distance}",
                        color = if (isNight) Color.White.copy(alpha = 0.8f) else secondaryTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${data.cuisines} • ${data.costForTwo}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SwiggyDessertItemSuggestions(
    isNight: Boolean,
    appThemeMode: String,
    activeCartItems: List<com.example.data.CartItem>,
    onAddClick: (String, Double, String) -> Unit,
    onRemoveClick: (String, String) -> Unit
) {
    val items = listOf(
        DessertCardData("Ak Bakers", "Strawberry Shake", 99.0, 4.1, "45-50 mins", "Desserts, Bakery, Sna...", "🥤🍓"),
        DessertCardData("Ak Bakers", "Choco Cheesecake", 149.0, 5.0, "45-50 mins", "Desserts, Beverages, ...", "🍰🍫"),
        DessertCardData("Siddu Falooda", "Falooda Special", 125.0, 3.8, "60-70 mins", "Ice Cream, Juices, ...", "🍨🍹")
    )

    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val cardBg = SkyroColors.getThemeCardBg(appThemeMode)
    val borderBrush = SkyroColors.getThemeBorderBrush(appThemeMode)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "Because you viewed Ak Bakers",
            color = if (isNight) Color.White else textColor,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (appThemeMode != "SKYRO_PRESENT") {
                                Modifier.border(1.dp, borderBrush, RoundedCornerShape(14.dp))
                            } else {
                                Modifier.border(1.dp, Color.Gray.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            }
                        )
                        .background(cardBg)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNight) Color(0xFF111827) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 38.sp)
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(Color(0xFFEA580C), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("ITEMS @ ₹99", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
                            }

                            val quantity = activeCartItems.find { it.name == item.dishName }?.quantity ?: 0

                            if (quantity == 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                        .clickable { onAddClick(item.dishName, item.price, item.name) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(Color(0xFF16A34A))
                                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(13.dp))
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "–",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .clickable { onRemoveClick(item.dishName, item.name) }
                                            .padding(horizontal = 3.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$quantity",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .clickable { onAddClick(item.dishName, item.price, item.name) }
                                            .padding(horizontal = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.name,
                            color = if (isNight) Color.White else Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${item.rating} • ${item.eta}",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = item.cuisine,
                            color = Color.Gray,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class DessertCardData(
    val name: String,
    val dishName: String,
    val price: Double,
    val rating: Double,
    val eta: String,
    val cuisine: String,
    val emoji: String
)
