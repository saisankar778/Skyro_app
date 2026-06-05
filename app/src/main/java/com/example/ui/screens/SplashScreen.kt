package com.example.ui.screens

import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class to represent each university block
data class CampusBlock(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val infoTag: String,
    val gradient: List<Color>,
    val latitude: Double,
    val longitude: Double
)

val campusBlocks = listOf(
    CampusBlock(
        id = "sr_block",
        name = "SR Block",
        description = "Main Academic & Admin Quad",
        iconEmoji = "🏫",
        infoTag = "SRM AP central tower dronepad",
        gradient = listOf(Color(0xFFFC8019), Color(0xFFFFB347)),
        latitude = 16.462635294684286,
        longitude = 80.50647168669644
    ),
    CampusBlock(
        id = "c_block",
        name = "C Block",
        description = "Research & Classroom Center",
        iconEmoji = "🏢",
        infoTag = "C Block drone landing pad",
        gradient = listOf(Color(0xFF7B2FFF), Color(0xFFEC4899)),
        latitude = 16.461646855350896,
        longitude = 80.50569336570064
    ),
    CampusBlock(
        id = "admin_block",
        name = "Admin Block",
        description = "University Administrative Headquarters",
        iconEmoji = "🏛️",
        infoTag = "Admin Block drone delivery bay",
        gradient = listOf(Color(0xFF00D4FF), Color(0xFF0D1B4B)),
        latitude = 16.464874583335895,
        longitude = 80.50791898212552
    ),
    CampusBlock(
        id = "yamuna_hostel",
        name = "Yamuna Hostel",
        description = "Student Housing & Dining Complex",
        iconEmoji = "🏨",
        infoTag = "Yamuna Hostel lawn drop zone",
        gradient = listOf(Color(0xFF00B0FF), Color(0xFF00E676)),
        latitude = 16.466254271237375,
        longitude = 80.50757917761362
    ),
    CampusBlock(
        id = "v_and_g_hostels",
        name = "V & G Hostels",
        description = "Hostel Blocks V and G Residential Wing",
        iconEmoji = "🏢",
        infoTag = "V & G Hostels main gate drop bin",
        gradient = listOf(Color(0xFFFF5252), Color(0xFFFF7A00)),
        latitude = 16.463886777402795,
        longitude = 80.50665800799868
    )
)

@Composable
fun SplashScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onNavigateNext: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val deliveryLocations by viewModel.deliveryLocations.collectAsState()

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

    // Screen Background Style
    val backgroundBrush = if (isNight) {
        Brush.linearGradient(
            colors = listOf(SkyroColors.MidnightNav, SkyroColors.NightPurple, SkyroColors.NightGradientDeep)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(SkyroColors.Sunrise, SkyroColors.Amber, SkyroColors.WarmCream)
        )
    }

    // Phase Flow tracking: "LOGO" -> "LOCATION_SCANNING" -> "LOCATION_LOCKED" -> "LOCATION_FLIGHT"
    var splashStage by remember { mutableStateOf("LOGO") }

    // -----------------------------------------------------------------
    // PHASE 1: LOGO ANIMATIONS & TAGLINE TYPEWRITER
    // -----------------------------------------------------------------
    var startScaleAnimation by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (startScaleAnimation) 1.0f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    val fullTagline = "delivery from the sky"
    var typedText by remember { mutableStateOf("") }

    // -----------------------------------------------------------------
    // PHASE 2: MARQUEE CYCLE INDEX
    // -----------------------------------------------------------------
    var activeMarqueeIndex by remember { mutableStateOf(0) }
    var locationScannerProgress by remember { mutableStateOf(0f) }
    var activeStatusText by remember { mutableStateOf("Initializing radar beacon...") }

    // To prevent infinite re-picking, freeze the random chosen block once decided
    var pickedBlock by remember { mutableStateOf(campusBlocks[0]) }
    var locationResolved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Run original silhouette logo branding entry
        startScaleAnimation = true
        delay(500)
        for (i in 1..fullTagline.length) {
            typedText = fullTagline.substring(0, i)
            delay(40)
        }
        delay(800)

        // Switch automatically to our custom location loading stage!
        splashStage = "LOCATION_SCANNING"
    }

    // Control the active slot-machine block rotation carousel & progress bar
    LaunchedEffect(splashStage, locationPermissionGranted) {
        if (splashStage == "LOCATION_SCANNING") {
            if (!locationPermissionGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            // 1. Progress loader thread
            launch {
                val totalSteps = 100
                var step = 1
                while (step <= 100) {
                    if (step == 90 && (!locationResolved || deliveryLocations.isEmpty())) {
                        activeStatusText = if (deliveryLocations.isEmpty()) {
                            "Connecting to backend drone hangars..."
                        } else {
                            "Pinpointing nearest campus block..."
                        }
                        delay(200)
                        continue
                    }
                    locationScannerProgress = step / 100f
                    
                    // Seamless loading sub-labels transition based on progress
                    activeStatusText = when {
                        step < 25 -> "Calibrating GPS aerospace coordinates..."
                        step < 50 -> "Pinging SRM AP university wifi cells..."
                        step < 75 -> "Analyzing wind currents over hostel blocks..."
                        else -> "Target airspace locks configured!"
                    }
                    step++
                    delay(32)
                }

                // 2. Lock-on to our chosen random block!
                splashStage = "LOCATION_LOCKED"
            }

            // 2. Fast marquee slide carousel thread
            launch {
                var speedDelay = 150L
                while (splashStage == "LOCATION_SCANNING") {
                    activeMarqueeIndex = (activeMarqueeIndex + 1) % campusBlocks.size
                    delay(speedDelay)
                    // Gradual deceleration curve
                    if (locationScannerProgress > 0.6f) speedDelay = 220L
                    if (locationScannerProgress > 0.85f) speedDelay = 320L
                }
            }

            // 3. Coordinate fetching & nearest calculation thread
            launch {
                val userLocation = if (locationPermissionGranted) {
                    getDeviceLocation(context, locationManager)
                } else null

                if (userLocation != null) {
                    val nearest = campusBlocks.minByOrNull { block ->
                        getDistance(userLocation.latitude, userLocation.longitude, block.latitude, block.longitude)
                    } ?: campusBlocks[0]
                    pickedBlock = nearest
                } else {
                    pickedBlock = campusBlocks[0] // fallback to SR Block
                }
                locationResolved = true
            }
        } else if (splashStage == "LOCATION_LOCKED") {
            // Find index of the genuinely chosen random block, and freeze marquee selection on it!
            activeMarqueeIndex = campusBlocks.indexOf(pickedBlock).coerceAtLeast(0)
            
            // Wait for dramatic locked-in ripple countdown, before flying up!
            delay(1500)
            splashStage = "LOCATION_FLIGHT"
        }
    }

    // -----------------------------------------------------------------
    // PHASE 4: OVER-SHOOT BEZIER FLIGHT FLIGHT POSITION CALCULATIONS
    // -----------------------------------------------------------------
    val flightProgress by animateFloatAsState(
        targetValue = if (splashStage == "LOCATION_FLIGHT") 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f, // Gorgeous overshooting spring ease-out!
            stiffness = Spring.StiffnessLow
        ),
        label = "BadgeFlightProgress",
        finishedListener = {
            if (splashStage == "LOCATION_FLIGHT") {
                val blockName = pickedBlock.name
                viewModel.updateAddress("📍 SRM AP " + blockName)

                // Sync with DB drop location UUID preference
                val dbLoc = deliveryLocations.find { loc ->
                    loc.name.equals(blockName, ignoreCase = true) ||
                    loc.name.replace("_", " ").equals(blockName.replace("_", " "), ignoreCase = true) ||
                    loc.name.replace(" & ", " and ").equals(blockName.replace(" & ", " and "), ignoreCase = true)
                }
                if (dbLoc != null) {
                    viewModel.selectDeliveryLocation(dbLoc.id, dbLoc.name)
                } else if (deliveryLocations.isNotEmpty()) {
                    viewModel.selectDeliveryLocation(deliveryLocations.first().id, deliveryLocations.first().name)
                }

                // 2. Set home page intro done to prevent double animation!
                viewModel.hasCompletedHomeIntro = true
                // 3. Perform seamless scene navigation onwards
                onNavigateNext()
            }
        }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // Left-top destination coordinates to blend with Home Screen top left SRM AP address icon!
        val destX = 16.dp
        val destY = 54.dp

        // Ambient Background space decorations
        Canvas(modifier = Modifier.fillMaxSize().alpha(1f - flightProgress)) {
            // Simple starry dust
            drawCircle(Color.White.copy(alpha = 0.2f), radius = 5f, center = Offset(size.width * 0.15f, size.height * 0.22f))
            drawCircle(Color.White.copy(alpha = 0.25f), radius = 8f, center = Offset(size.width * 0.85f, size.height * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.15f), radius = 6f, center = Offset(size.width * 0.70f, size.height * 0.80f))
            drawCircle(Color.White.copy(alpha = 0.2f), radius = 7f, center = Offset(size.width * 0.25f, size.height * 0.75f))
        }

        // Animated Switch Stages content
        if (splashStage == "LOGO") {
            // Stage A: Iconic branding silhouette
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(logoScale)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2.3f
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)
                        
                        drawCircle(
                            color = Color.White,
                            radius = radius,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.35f
                        )
                        drawLine(
                            color = Color.White,
                            start = centerOffset - Offset(radius * 0.8f, radius * 0.8f),
                            end = centerOffset + Offset(radius * 0.8f, radius * 0.8f),
                            strokeWidth = 4.dp.toPx()
                        )
                        drawLine(
                            color = Color.White,
                            start = centerOffset - Offset(-radius * 0.8f, radius * 0.8f),
                            end = centerOffset + Offset(-radius * 0.8f, radius * 0.8f),
                            strokeWidth = 4.dp.toPx()
                        )
                        drawCircle(
                            color = SkyroColors.Amber,
                            radius = 4.dp.toPx(),
                            center = centerOffset - Offset(radius * 0.8f, radius * 0.8f)
                        )
                        drawCircle(
                            color = SkyroColors.Amber,
                            radius = 4.dp.toPx(),
                            center = centerOffset + Offset(radius * 0.8f, radius * 0.8f)
                        )
                        drawCircle(
                            color = SkyroColors.Amber,
                            radius = 4.dp.toPx(),
                            center = centerOffset - Offset(-radius * 0.8f, radius * 0.8f)
                        )
                        drawCircle(
                            color = SkyroColors.Amber,
                            radius = 4.dp.toPx(),
                            center = centerOffset + Offset(-radius * 0.8f, radius * 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SKYRO",
                    color = Color.White,
                    style = SkyroTypography.Display.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = SkyroColors.DeepCoral.copy(alpha = 0.5f),
                            blurRadius = 15f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$typedText|",
                    color = Color.White.copy(alpha = 0.9f),
                    style = SkyroTypography.Body.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        } else {
            // Stage B/C/D: Location Resolver & Flying Logo
            val currentBlock = if (splashStage == "LOCATION_SCANNING") {
                campusBlocks[activeMarqueeIndex]
            } else {
                pickedBlock
            }

            // Normal layout: Central location box and text layers
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .alpha(1f - flightProgress), // Fade background text layers linearly with flight progress
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large spacer matching absolute fly position of image
                Spacer(modifier = Modifier.height(240.dp))

                Spacer(modifier = Modifier.height(20.dp))

                // Under the image: simple text of block names
                Text(
                    text = currentBlock.name,
                    color = Color.White,
                    style = SkyroTypography.H2.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 0.5.sp,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            blurRadius = 8f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // accompanied by a simple loading icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (splashStage == "LOCATION_SCANNING") {
                        CircularProgressIndicator(
                            color = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Getting your location...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "📍 POSITION SECURED",
                            color = if (isNight) SkyroColors.CyanGlow else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // THE FLYING/MINIMIZING/SLIDING BLOCK IMAGE BADGE
            // -----------------------------------------------------------------
            val startSize = 220.dp
            val stopSize = 24.dp

            val startX = totalWidth / 2 - (startSize / 2)
            val startY = totalHeight / 2 - (startSize / 2) - 100.dp

            val animatedX = lerp(startX, destX, flightProgress)
            val animatedY = lerp(startY, destY, flightProgress)
            val animatedSize = lerp(startSize, stopSize, flightProgress)

            Box(
                modifier = Modifier
                    .offset(x = animatedX, y = animatedY)
                    .size(animatedSize),
                contentAlignment = Alignment.Center
            ) {
                // Try rendering uploaded block image from local assets
                val context = LocalContext.current
                val painter = remember(currentBlock.id) {
                    try {
                        context.assets.open("${currentBlock.id}.png").use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                BitmapPainter(bitmap.asImageBitmap())
                            } else null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = currentBlock.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to beautiful text emoji with transparent background
                    val emojiScale = 1f - 0.4f * flightProgress
                    Text(
                        text = currentBlock.iconEmoji,
                        fontSize = if (animatedSize > 60.dp) 120.sp else 16.sp,
                        modifier = Modifier.scale(emojiScale)
                    )
                }
            }

            // Subtle radar locator rings purely when locked
            if (splashStage == "LOCATION_LOCKED") {
                val infiniteTransition = rememberInfiniteTransition(label = "RadarLockGlow")
                val radarAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "GlowAlpha"
                )
                val radarScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "GlowScale"
                )

                Box(
                    modifier = Modifier
                        .offset(x = startX, y = startY)
                        .size(startSize)
                        .scale(radarScale)
                        .alpha(radarAlpha)
                        .border(
                            BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// Distance calculation helper (Haversine formula)
fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return 6371e3 * c // Distance in meters
}

// Async Fused/LocationManager retrieval helper
suspend fun getDeviceLocation(context: Context, locationManager: LocationManager): Location? = suspendCancellableCoroutine { continuation ->
    try {
        val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        // 1. Check last known locations first
        var bestLastKnown: Location? = null
        val providers = locationManager.getProviders(true)
        val currentTime = System.currentTimeMillis()
        
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider)
            if (loc != null) {
                // If it is very fresh (e.g., less than 30 seconds old), we can use it immediately!
                if (currentTime - loc.time < 30_000) {
                    continuation.resume(loc)
                    return@suspendCancellableCoroutine
                }
                if (bestLastKnown == null || loc.accuracy < bestLastKnown.accuracy) {
                    bestLastKnown = loc
                }
            }
        }

        // 2. Request updates from BOTH Network and GPS providers
        val listeners = mutableListOf<LocationListener>()
        var resolved = false

        fun cleanUp() {
            listeners.forEach { listener ->
                locationManager.removeUpdates(listener)
            }
        }

        fun onLocationReceived(location: Location) {
            if (!resolved) {
                resolved = true
                cleanUp()
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }
        }

        val enabledProviders = providers.filter { 
            it == LocationManager.GPS_PROVIDER || it == LocationManager.NETWORK_PROVIDER 
        }

        if (enabledProviders.isEmpty()) {
            continuation.resume(bestLastKnown)
            return@suspendCancellableCoroutine
        }

        // Setup a handler/timer for timeout (e.g., 4 seconds)
        val handler = android.os.Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!resolved) {
                resolved = true
                cleanUp()
                if (continuation.isActive) {
                    // Fallback to best last known location if we have it
                    continuation.resume(bestLastKnown)
                }
            }
        }
        handler.postDelayed(timeoutRunnable, 4000L)

        enabledProviders.forEach { provider ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationReceived(location)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            listeners.add(listener)
            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        }

        continuation.invokeOnCancellation {
            handler.removeCallbacks(timeoutRunnable)
            cleanUp()
        }

    } catch (e: SecurityException) {
        if (continuation.isActive) continuation.resume(null)
    } catch (e: Exception) {
        if (continuation.isActive) continuation.resume(null)
    }
}
