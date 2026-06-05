package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.data.DeliveryOrder
import com.example.ui.SkyroViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sin

private val restaurantCoordinates = mapOf(
    "Dominos" to LatLng(16.463084574257913, 80.5084325541339),
    "US Pizza" to LatLng(16.46277461846416, 80.50822267128899),
    "US_Pizza" to LatLng(16.46277461846416, 80.50822267128899),
    "Chat & Chill" to LatLng(16.462954675830282, 80.50807783200786),
    "Chat_and_Chill" to LatLng(16.462954675830282, 80.50807783200786),
    "Paradise" to LatLng(16.46286593329217, 80.50807313814228),
    "Total Fresh" to LatLng(16.463118656500374, 80.50826089276595),
    "Total_Fresh" to LatLng(16.463118656500374, 80.50826089276595),
    "Baskin Robbins" to LatLng(16.463022197299473, 80.50831923080973),
    "Baskin_Robins" to LatLng(16.463022197299473, 80.50831923080973),
    "Nescafe" to LatLng(16.46288008065604, 80.50844663573295)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val trackedOrderId by viewModel.trackedOrderId.collectAsState()
    val deliveryLocations by viewModel.deliveryLocations.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    // Find the current order being tracked, fallback to first active, fallback to database first, or generic
    val order = allOrders.find { it.orderId == trackedOrderId }
        ?: activeOrders.firstOrNull()
        ?: allOrders.firstOrNull()
        ?: remember {
            DeliveryOrder(
                orderId = "SKY-8392",
                restaurantName = "Spice Garden - Campus Hub",
                itemsSummary = "Loaded Cheese Fries x1, Smoky BBQ Burger x1",
                totalPrice = 389.0,
                droneId = "SKY-042",
                etaMinutes = 10,
                status = "PREPARING"
            )
        }

    // Dynamic brand colors depending on day/night mode
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    val orangeBrand = Color(0xFFFC8019)
    val themeColor = if (isNight) SkyroColors.CyanGlow else orangeBrand
    val surfaceColor = if (isNight) SkyroColors.NightPurple else SkyroColors.WarmCream

    val backgroundBrush = if (isNight) {
        Brush.linearGradient(
            colors = listOf(SkyroColors.MidnightNav, SkyroColors.NightPurple, SkyroColors.NightGradientDeep)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(SkyroColors.Sunrise, SkyroColors.Amber, SkyroColors.SkyBlue)
        )
    }

    // -------------------------------------------------------------
    // SEQUENTIAL LIFECYCLE AND SMOOTH GPS FLIGHT DRIVERS
    // -------------------------------------------------------------
    // Automatic precursor transitions to simulate external API vendor console updates
    // -------------------------------------------------------------
    // REAL-TIME TELEMETRY TRACKING & GEOLOCATION
    // -------------------------------------------------------------
    val droneLocationsMap by viewModel.droneLocationsMap.collectAsState()
    val droneLocation = droneLocationsMap[order.droneId]

    // Start/Stop tracking based on active order flight state
    DisposableEffect(order.droneId, order.status) {
        val isFlightState = order.status == "IN_FLIGHT" || order.status == "DISPATCHED" || order.status == "READY_FOR_LAUNCH"
        if (isFlightState && order.droneId.isNotEmpty()) {
            viewModel.startDroneTracking(order.droneId)
        } else {
            viewModel.stopDroneTracking()
        }
        onDispose {
            viewModel.stopDroneTracking()
        }
    }

    // Distance calculation helper (Haversine formula)
    fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371e3 // Earth's radius in meters
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // Reference coordinates
    val restaurantLatLng = restaurantCoordinates[order.restaurantName] ?: LatLng(16.463084, 80.508432)
    val deliveryLatLng = remember(order.deliveryLocationName, deliveryLocations) {
        val cleanName = order.deliveryLocationName.replace("📍 SRM AP ", "").replace("_", " ").trim()
        val found = deliveryLocations.find { 
            it.name.replace("_", " ").equals(cleanName, ignoreCase = true) ||
            it.name.replace("_", " ").replace(" & ", " and ").equals(cleanName.replace(" & ", " and "), ignoreCase = true)
        }
        if (found != null) {
            LatLng(found.latitude, found.longitude)
        } else {
            LatLng(16.462635, 80.506471) // Fallback to SR Block
        }
    }
    val totalDistance = calculateDistance(restaurantLatLng, deliveryLatLng)

    // Current coordinates
    val currentDroneLocation = droneLocation
    val droneLatLng = currentDroneLocation ?: restaurantLatLng

    val remainingDistance = if (currentDroneLocation != null) {
        calculateDistance(droneLatLng, deliveryLatLng)
    } else {
        totalDistance
    }

    // flightProgress is the percentage of path traversed
    val flightProgress = if (totalDistance > 0.0) {
        (1.0f - (remainingDistance / totalDistance).toFloat()).coerceIn(0f, 1f)
    } else {
        0.0f
    }

    // Speed: 5.0 m/s groundspeed. totalSecsLeft = remainingDistance / 5.0
    val totalSecsLeft = (remainingDistance / 5.0).toInt().coerceAtLeast(0)

    // Local internal simulation variables
    var enteredKeypadPin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var isPinSuccess by remember { mutableStateOf(false) }
    var triggerUnlockBurst by remember { mutableStateOf(false) }

    // Derive 4-digit code directly from order numbers (e.g. SKY-2847 -> 2847)
    val lockerCode = remember(order.orderId) {
        val digits = order.orderId.filter { it.isDigit() }
        if (digits.length >= 4) digits.take(4) else "8103"
    }

    // Dynamic animators for radar bobbing and landing pins
    val infiniteTransition = rememberInfiniteTransition(label = "RadarBlink")
    val radarPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarScale"
    )
    val radarPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAlpha"
    )
    val droneBobbingY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DroneBobOffset"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hangar Tracking Console",
                            style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                            color = Color.White
                        )
                        Text(
                            text = "Order: ${order.orderId} · ${order.restaurantName}",
                            style = SkyroTypography.Caption.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("track_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Return to history",
                            tint = Color.White
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
                .padding(innerPadding)
        ) {
            if (isNight) {
                com.example.ui.components.CustomStarfield()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // -------------------------------------------------------------
                // ACTIVE LIFECYCLE STATE DISPATCHER
                // -------------------------------------------------------------
                when (order.status) {
                    "PREPARING" -> {
                        // STATE 1: WAITING FOR ACCURACY / ORDER ACCEPTANCE
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("vendor_terminal_accept"),
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 24.dp,
                            elevation = 10.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(themeColor.copy(alpha = 0.1f), CircleShape)
                                        .border(2.dp, themeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📝", fontSize = 48.sp)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "TRANSMITTING TICKET TO KITCHEN",
                                    style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, color = themeColor),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "The kitchen at ${order.restaurantName} is receiving your order request. Synchronizing with chef console...",
                                    style = SkyroTypography.Body,
                                    color = if (isNight) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = themeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Awaiting chef acceptance (simulated link)...",
                                    style = SkyroTypography.Caption,
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor
                                )
                            }
                        }
                    }

                    "ACCEPTED" -> {
                        // STATE 2: ORDER ACCEPTED, READY TO COOK
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("vendor_terminal_cooking_prep"),
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 24.dp,
                            elevation = 10.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(themeColor.copy(alpha = 0.1f), CircleShape)
                                        .border(2.dp, themeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🤝", fontSize = 48.sp)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "ORDER ACCEPTED BY CHEF",
                                    style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, color = themeColor),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Chef accepted! Thermal storage caskets are locked and ingredients are being assembled.",
                                    style = SkyroTypography.Body,
                                    color = if (isNight) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = themeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Preparing kitchen workspace...",
                                    style = SkyroTypography.Caption,
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor
                                )
                            }
                        }
                    }

                    "COOKING" -> {
                        // STATE 3: COOKING SIZZLING HOT
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("vendor_terminal_cooking_active"),
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 24.dp,
                            elevation = 10.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val cookingScale by infiniteTransition.animateFloat(
                                    initialValue = 0.95f,
                                    targetValue = 1.05f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = EaseInOutSine),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "CookingPulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(cookingScale)
                                        .background(themeColor.copy(alpha = 0.1f), CircleShape)
                                        .border(2.dp, themeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔥", fontSize = 48.sp)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "KITCHEN ACTIVELY COOKING",
                                    style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, color = themeColor),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your food is being sizzled inside sealed heating vessels. Ingredients: ${order.itemsSummary}",
                                    style = SkyroTypography.Body,
                                    color = if (isNight) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = themeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Baking and sealing thermal storage pod...",
                                    style = SkyroTypography.Caption,
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor
                                )
                            }
                        }
                    }

                    "READY_FOR_LAUNCH" -> {
                        // STATE 4: READY ON CATAPULT SYSTEM
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .testTag("vendor_terminal_ready_launch"),
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 24.dp,
                            elevation = 10.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .background(themeColor.copy(alpha = 0.1f), CircleShape)
                                        .border(2.5.dp, themeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Hangar Catapult",
                                        tint = themeColor,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "LAUNCHPAD ARMED & SECURED",
                                    style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, color = themeColor),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Drone Hangar compartment loaded. Flight route locks approved for drone casket ${order.droneId}.",
                                    style = SkyroTypography.Body,
                                    color = if (isNight) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = themeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Arming electric flight motors & authorizing airspace takeoff...",
                                    style = SkyroTypography.Caption,
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor
                                )
                            }
                        }
                    }

                    "IN_FLIGHT", "DISPATCHED", "APPROACHING" -> {
                        // STATE 5: LAUNCH COMPLETE — GOOGLE MAPS + DRONE ANIMATION OVERLAY
                        Text(
                            text = "⚡ Real-time Aerial Corridor Route Map",
                            color = Color.White.copy(alpha = 0.9f),
                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                .testTag("flight_corridor_canvas_map")
                        ) {
                            // ── 1. Real Google Map Base ──────────────────────────────────────
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(
                                    LatLng(16.4628, 80.5074), 16.5f
                                )
                            }

                            // Animate camera to follow the live drone coordinates
                            LaunchedEffect(droneLocation) {
                                val loc = droneLocation
                                if (loc != null) {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            LatLng(loc.latitude, loc.longitude), 17.5f
                                        ),
                                        durationMs = 800
                                    )
                                }
                            }

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    mapType = if (isNight) MapType.NORMAL else MapType.SATELLITE,
                                    isMyLocationEnabled = false
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    compassEnabled = false,
                                    myLocationButtonEnabled = false
                                )
                            ) {
                                // Restaurant marker (orange)
                                Marker(
                                    state = MarkerState(position = restaurantLatLng),
                                    title = order.restaurantName,
                                    snippet = "Pickup point"
                                )
                                // Delivery zone marker (blue)
                                Marker(
                                    state = MarkerState(position = deliveryLatLng),
                                    title = order.deliveryLocationName.ifEmpty { "Delivery Zone" },
                                    snippet = "Drop point"
                                )
                                // Flight path polyline
                                Polyline(
                                    points = listOf(restaurantLatLng, deliveryLatLng),
                                    color = if (isNight) Color(0xFF00FFFF) else Color(0xFFFC8019),
                                    width = 8f
                                )

                                // Live Drone Marker
                                val headingDeg = remember(droneLocation) {
                                    val derivDx = deliveryLatLng.longitude - restaurantLatLng.longitude
                                    val derivDy = deliveryLatLng.latitude - restaurantLatLng.latitude
                                    Math.toDegrees(atan2(derivDy, derivDx)).toFloat()
                                }

                                MarkerComposable(
                                    state = MarkerState(position = droneLatLng),
                                    anchor = Offset(0.5f, 0.5f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .rotate(headingDeg + 90f)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.d_drone),
                                            contentDescription = "Live Drone Location",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // Telemetry HUD overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🌪️ Wind Corridor: Stable (NE 4.1kts) · Altitude: 42m",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // HIGH FIDELITY REAL-TIME ETA AND ACTIVE FLIGHT PROGRESS TIMELINE
                        val secFmt = "%02d".format(totalSecsLeft % 60)
                        val minFmt = totalSecsLeft / 60
                        val etaMsg = if (order.status in listOf("ARRIVED", "DELIVERED") || totalSecsLeft <= 0) "Touchdown!" else "${minFmt}m ${secFmt}s left"

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.updateOrderStatusPublic(order.orderId, "ARRIVED", 0)
                                },
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 16.dp,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ SECURE FLIGHT TELEMETRY: ACTIVE",
                                        color = themeColor,
                                        style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black, fontSize = 9.sp)
                                    )
                                    Text(
                                        text = "ETA: $etaMsg",
                                        color = if (isNight) Color.White else textColor,
                                        style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    )
                                }

                                // Smooth timeline progress track
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(28.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(flightProgress)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(themeColor)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Kitchen hub
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (flightProgress >= 0.0f) Color(0xFF16A34A) else Color.Gray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🍳", fontSize = 10.sp)
                                        }

                                        // 2. Transitting drone
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (flightProgress > 0.05f) themeColor else Color.Gray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🛸", fontSize = 10.sp)
                                        }

                                        // 3. Vending locker
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (flightProgress >= 0.99f) themeColor else Color.Gray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎯", fontSize = 10.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Depot Hangar", color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor, fontSize = 8.sp)
                                    Text("Drone In Air Corridor", color = themeColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    Text("Locker #03", color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor, fontSize = 8.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📡 TAP TELEMETRY TO INITIATE MANPOWER LANDING / SUDDEN TOUCHDOWN",
                                        color = themeColor.copy(alpha = 0.85f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                            }
                        }
                    }

                    "ARRIVED", "DELIVERED" -> {
                        // STATE 6: DRONE LANDED! LOCKER CABINET QR CODE + RETRIEVAL DIGITAL KEYPAD OPEN
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .testTag("secure_pickup_dashboard"),
                            isNight = isNight,
                            appThemeMode = appThemeMode,
                            borderRadius = 22.dp,
                            elevation = 14.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Cabin assignment
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "📍 TOUCHDOWN: APPARATUS C-LOCKER #03",
                                        style = SkyroTypography.Caption.copy(fontWeight = FontWeight.ExtraBold, color = themeColor),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Enter Code At Vending Station",
                                        style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black),
                                        color = if (isNight) Color.White else textColor
                                    )
                                }

                                // High visibility OTP and visual PIN entry screen feedback
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .background(if (isNight) Color.Black.copy(alpha = 0.6f) else Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                        .border(
                                            1.5.dp,
                                            if (isPinError) Color.Red else if (isPinSuccess) Color.Green else (if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f)),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "PIN UNLOCK PASSCODE: $lockerCode",
                                            color = themeColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Render entered pin digits
                                            val pinString = enteredKeypadPin.padEnd(4, '_').chunked(1).joinToString("  ")
                                            Text(
                                                text = pinString,
                                                color = if (isPinError) Color.Red else if (isPinSuccess) Color.Green else (if (isNight) Color.White else textColor),
                                                fontSize = 24.sp,
                                                style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Black)
                                            )
                                        }
                                    }
                                }

                                // Interactive numeric keypad (4x3 layout)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val btnColor = if (isNight) Color.White.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.25f)
                                    val keypadLayout = listOf(
                                        listOf("1", "2", "3"),
                                        listOf("4", "5", "6"),
                                        listOf("7", "8", "9"),
                                        listOf("⌫", "0", "✓")
                                    )

                                    keypadLayout.forEach { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            row.forEach { digit ->
                                                val isSpecial = digit == "⌫" || digit == "✓"
                                                val keyBgColor = if (isSpecial) {
                                                    if (digit == "✓") Color(0xFF16A34A).copy(alpha = 0.25f)
                                                    else Color(0xFFDC2626).copy(alpha = 0.15f)
                                                } else btnColor

                                                val keyBorderClr = if (isSpecial) {
                                                    if (digit == "✓") Color(0xFF16A34A)
                                                    else Color(0xFFDC2626)
                                                } else if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(46.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(keyBgColor)
                                                        .border(1.dp, keyBorderClr, RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            if (isPinSuccess) return@clickable
                                                            when (digit) {
                                                                "⌫" -> {
                                                                    if (enteredKeypadPin.isNotEmpty()) {
                                                                        enteredKeypadPin = enteredKeypadPin.dropLast(1)
                                                                        isPinError = false
                                                                    }
                                                                }
                                                                "✓" -> {
                                                                    if (enteredKeypadPin == lockerCode) {
                                                                        isPinSuccess = true
                                                                        isPinError = false
                                                                        coroutineScope.launch {
                                                                            triggerUnlockBurst = true
                                                                            delay(500)
                                                                            isPinSuccess = false
                                                                            viewModel.updateOrderStatusPublic(order.orderId, "DELIVERED", 0)
                                                                            isPinSuccess = false
                                                                        }
                                                                    } else {
                                                                        isPinError = true
                                                                        enteredKeypadPin = ""
                                                                    }
                                                                }
                                                                else -> {
                                                                    if (enteredKeypadPin.length < 4) {
                                                                        enteredKeypadPin += digit
                                                                        isPinError = false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        .testTag("keypad_btn_$digit"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = digit,
                                                        color = if (isSpecial) {
                                                            if (digit == "✓") Color(0xFF16A34A) else Color(0xFFDC2626)
                                                        } else {
                                                            if (isNight) Color.White else textColor
                                                        },
                                                        style = SkyroTypography.H2.copy(fontSize = 15.sp, fontWeight = FontWeight.Black)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        isPinSuccess = true
                                        isPinError = false
                                        coroutineScope.launch {
                                            triggerUnlockBurst = true
                                            delay(500)
                                            viewModel.updateOrderStatusPublic(order.orderId, "DELIVERED", 0)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("use_express_unlocking_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Express Unlocking",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Use Express Unlocking ⚡",
                                        style = SkyroTypography.H2.copy(fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Vault indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text("📦 STATION: SMART CABINET 3", color = if (isNight) Color.White.copy(alpha = 0.4f) else secondaryTextColor, fontSize = 9.sp)
                                    Text("🔑 AUTOMATIC UNLOCK READY", color = themeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // BOTTOM CONTROL SHEET / FORCE SIMULATE CORRIDOR BUTTONS
                // -------------------------------------------------------------
                if (order.status !in listOf("IN_FLIGHT", "DISPATCHED", "APPROACHING")) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("visual_tracking_panel"),
                        isNight = isNight,
                        appThemeMode = appThemeMode,
                        borderRadius = 20.dp,
                        elevation = 12.dp
                    ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val labelText = when (order.status) {
                                    "PREPARING" -> "STEP 1: COOKING PREPARATION"
                                    "ACCEPTED" -> "STEP 2: TICKET CONFIRMED"
                                    "COOKING" -> "STEP 3: SIZZILING HEAT COOKING"
                                    "READY_FOR_LAUNCH" -> "STEP 4: CARGO HANGAR ARMED"
                                    "IN_FLIGHT" -> "STEP 5: VISUAL FLIGHT TRACKING"
                                    else -> "DEPOSIT RECOVERY AUTHENTICATION"
                                }
                                Text(
                                    text = labelText,
                                    color = themeColor,
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black, letterSpacing = 0.8.sp),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Drone Hangar compartment: ${order.droneId}",
                                    color = if (isNight) Color.White else textColor,
                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ETA:",
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (order.status in listOf("ARRIVED", "DELIVERED")) "QR" else "0${order.etaMinutes}m",
                                        color = if (isNight) Color.White else textColor,
                                        style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (order.status in listOf("IN_FLIGHT", "DISPATCHED", "APPROACHING")) {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatusPublic(order.orderId, "ARRIVED", 0)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("track_advance_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Skip Flight / Force Touchdown 🛬",
                                        color = Color.White,
                                        style = SkyroTypography.H2.copy(fontSize = 12.sp, color = Color.White)
                                    )
                                }
                            } else if (order.status == "ARRIVED") {
                                Button(
                                    onClick = {
                                        // Auto typing correct pin simulator
                                        enteredKeypadPin = lockerCode
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("auto_inject_pin_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isNight) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Autofill station PIN 📟",
                                        color = if (isNight) Color.White else textColor,
                                        style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else if (order.status == "DELIVERED") {
                                Button(
                                    onClick = {
                                        isPinSuccess = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("claim_simulated_meal_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Open locker cupboard drawer #03 📂",
                                        color = Color.White,
                                        style = SkyroTypography.H2.copy(fontSize = 12.sp)
                                    )
                                }
                            } else {
                                // Fallback info text
                                Text(
                                    text = "Perform simulated actions above to progress the kitchen life-cycle sequentially to flight catapult system.",
                                    color = if (isNight) Color.White.copy(alpha = 0.5f) else secondaryTextColor,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }

    // Congratulations Smart Locker Unlock Dialog — shows 4-digit code prominently
    if (isPinSuccess || order.status == "DELIVERED") {
        AlertDialog(
            onDismissRequest = {
                coroutineScope.launch {
                    viewModel.updateOrderStatusPublic(order.orderId, "DELIVERED", 0)
                    onBack()
                }
            },
            title = {
                Text(
                    text = "🔓 LOCKER UNLOCKED!",
                    fontWeight = FontWeight.Black,
                    color = orangeBrand,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your pick-up code:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    // Large 4-digit code display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFC8019).copy(alpha = 0.12f), Color(0xFFFC8019).copy(alpha = 0.06f))
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .border(2.dp, orangeBrand, RoundedCornerShape(14.dp))
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lockerCode,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black,
                            color = orangeBrand,
                            letterSpacing = 10.sp,
                            style = SkyroTypography.PriceMono
                        )
                    }
                    Text(
                        text = "Enter this code at Cabinet #03 to collect your order from ${order.restaurantName}.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.updateOrderStatusPublic(order.orderId, "DELIVERED", 0)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeBrand)
                ) {
                    Text("Done ⚡", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// Custom Grid QR code matrix drawer
@Composable
fun CanvasQrCode(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val cells = 9
        val cs = w / cells

        if (cs > 4f) {
            drawRect(color, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(cs * 3, cs * 3))
            drawRect(Color.Transparent, topLeft = Offset(cs, cs), size = androidx.compose.ui.geometry.Size(cs, cs))

            drawRect(color, topLeft = Offset(cs * 6, 0f), size = androidx.compose.ui.geometry.Size(cs * 3, cs * 3))
            drawRect(Color.Transparent, topLeft = Offset(cs * 7, cs), size = androidx.compose.ui.geometry.Size(cs, cs))

            drawRect(color, topLeft = Offset(0f, cs * 6), size = androidx.compose.ui.geometry.Size(cs * 3, cs * 3))
            drawRect(Color.Transparent, topLeft = Offset(cs, cs * 7), size = androidx.compose.ui.geometry.Size(cs, cs))

            val layout = listOf(
                listOf(0,0,0,0,1,0,0,0,0),
                listOf(0,0,0,1,0,1,0,0,0),
                listOf(0,0,0,0,1,1,0,0,0),
                listOf(1,0,1,1,0,0,1,0,1),
                listOf(0,1,0,0,1,0,0,1,0),
                listOf(1,1,1,0,1,1,0,1,1),
                listOf(0,0,0,1,0,1,0,0,0),
                listOf(0,0,0,0,1,1,0,0,0),
                listOf(0,0,0,1,0,1,1,0,0)
            )

            for (r in 0 until cells) {
                for (c in 0 until cells) {
                    if (r < 3 && c < 3) continue
                    if (r < 3 && c >= 6) continue
                    if (r >= 6 && c < 3) continue

                    if (r < layout.size && c < layout[r].size && layout[r][c] == 1) {
                        val pixelSize = cs - 4f
                        if (pixelSize > 0f) {
                            drawRect(
                                color = color,
                                topLeft = Offset(c * cs + 2f, r * cs + 2f),
                                size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Parallax clouds vectors drawing helper
@Composable
fun CloudsPainter(
    modifier: Modifier = Modifier,
    offsetScroll: Float,
    cloudOpacity: Float,
    waveFrequency: Float,
    amplitude: Float,
    yAnchor: Float
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val path = Path()
        val baseLine = h * yAnchor

        path.moveTo(0f, baseLine)
        
        var x = 0f
        while (x <= w + 40f) {
            val calcY = baseLine + sin((x + offsetScroll) * waveFrequency) * amplitude
            path.lineTo(x, calcY.toFloat())
            x += 18f
        }
        
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = cloudOpacity),
                    Color.White.copy(alpha = 0.01f)
                )
            )
        )
    }
}
