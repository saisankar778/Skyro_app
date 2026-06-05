package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.GlassCard
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun ProfileScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()

    // Dynamic collapsing scroll state calculations for the main list
    val lazyListState = rememberLazyListState()
    val firstVisibleIndex = lazyListState.firstVisibleItemIndex
    val firstVisibleOffset = lazyListState.firstVisibleItemScrollOffset

    // Smoothly calculate normalized collapsing progress (from 0 = fully expanded to 1 = fully collapsed)
    val collapseProgress = if (firstVisibleIndex > 0) {
        1f
    } else {
        (firstVisibleOffset.toFloat() / 240f).coerceIn(0f, 1f)
    }

    // Theme responsive palettes
    val backgroundBrush = getThemeBackgroundBrush(appThemeMode)
    val cardBackground = getThemeCardBg(appThemeMode)
    val borderBrush = getThemeBorderBrush(appThemeMode)
    val textColor = getThemeTextColor(appThemeMode)
    val secondaryTextColor = getThemeSecondaryTextColor(appThemeMode)
    val orangeBrand = Color(0xFFFC8019)

    // Sub-page state router holding current selected sub-page identifier
    var activeSubPage by remember { mutableStateOf<String?>(null) }
    // Setting for allowing contact
    var allowContact by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            // Keep the persistent bottom bar unless on a sub-page
            if (activeSubPage == null) {
                BottomNavBar(
                    selectedRoute = "profile",
                    onRouteSelected = { route ->
                        if (route != "profile") {
                            viewModel.navigateTo(route)
                        }
                    },
                    isNight = (appThemeMode == "NIGHT")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // Background ambient stars for Starry Night Theme
            if (appThemeMode == "NIGHT") {
                com.example.ui.components.CustomStarfield()
            }

            AnimatedContent(
                targetState = activeSubPage,
                transitionSpec = {
                    if (targetState == null) {
                        // Slide out to the right (going back)
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    } else {
                        // Slide in from the right (going into a sub-page)
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "ProfileNavigationAnim"
            ) { subPage ->
                if (subPage == null) {
                    // -------------------------------------------------------------
                    // MAIN PROFILE LIST SCREEN (Default View)
                    // -------------------------------------------------------------
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.statusBars),
                            contentPadding = PaddingValues(top = 175.dp, bottom = 120.dp, start = 12.dp, end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Theme Switcher Section
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .then(
                                            if (appThemeMode != "SKYRO_PRESENT") {
                                                Modifier.border(1.dp, borderBrush, RoundedCornerShape(20.dp))
                                            } else Modifier
                                        )
                                        .background(cardBackground)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "SELECT APP THEME",
                                        color = textColor,
                                        style = SkyroTypography.H2.copy(fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ThemeOptionButton(
                                            name = "SUNNY",
                                            emoji = "☀️",
                                            active = (appThemeMode == "SUNNY"),
                                            modifier = Modifier.weight(1f),
                                            cardBg = cardBackground,
                                            activeBg = Color(0xFFD97706),
                                            textColor = textColor,
                                            onClick = { viewModel.selectTheme("SUNNY") }
                                        )
                                        ThemeOptionButton(
                                            name = "PRESENT",
                                            emoji = "🌅",
                                            active = (appThemeMode == "SKYRO_PRESENT"),
                                            modifier = Modifier.weight(1f),
                                            cardBg = cardBackground,
                                            activeBg = orangeBrand,
                                            textColor = textColor,
                                            onClick = { viewModel.selectTheme("SKYRO_PRESENT") }
                                        )
                                        ThemeOptionButton(
                                            name = "NIGHT",
                                            emoji = "🌌",
                                            active = (appThemeMode == "NIGHT"),
                                            modifier = Modifier.weight(1f),
                                            cardBg = cardBackground,
                                            activeBg = Color(0xFF7B2FFF),
                                            textColor = textColor,
                                            onClick = { viewModel.selectTheme("NIGHT") }
                                        )
                                    }
                                }
                            }

                            // 3 Grid Action Buttons Row (Saved Addresses, Payments, Refunds)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GridPillCard(
                                        emoji = "📍",
                                        title = "Saved\nAddresses",
                                        appThemeMode = appThemeMode,
                                        textColor = textColor,
                                        modifier = Modifier.weight(1f),
                                        onClick = { activeSubPage = "address" }
                                    )
                                    GridPillCard(
                                        emoji = "💳",
                                        title = "Payment\nModes",
                                        appThemeMode = appThemeMode,
                                        textColor = textColor,
                                        modifier = Modifier.weight(1f),
                                        onClick = { activeSubPage = "payments" }
                                    )
                                    GridPillCard(
                                        emoji = "🔄",
                                        title = "My\nRefunds",
                                        appThemeMode = appThemeMode,
                                        textColor = textColor,
                                        modifier = Modifier.weight(1f),
                                        onClick = { activeSubPage = "refunds" }
                                    )
                                }
                            }

                            // Navigation Settings card list
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .then(
                                            if (appThemeMode != "SKYRO_PRESENT") {
                                                Modifier.border(1.dp, borderBrush, RoundedCornerShape(20.dp))
                                            } else Modifier
                                        )
                                        .background(cardBackground)
                                ) {
                                    ProfileSubOptionRow(
                                        icon = "📄",
                                        title = "Account Statements",
                                        textColor = textColor,
                                        secondaryTextColor = secondaryTextColor,
                                        onClick = { activeSubPage = "statements" }
                                    )
                                    Divider(color = secondaryTextColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                                    ProfileSubOptionRow(
                                        icon = "🎓",
                                        title = "Student Rewards",
                                        textColor = textColor,
                                        secondaryTextColor = secondaryTextColor,
                                        onClick = { activeSubPage = "student_rewards" }
                                    )
                                    Divider(color = secondaryTextColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                                    ProfileSubOptionRow(
                                        icon = "❤️",
                                        title = "Favourites",
                                        textColor = textColor,
                                        secondaryTextColor = secondaryTextColor,
                                        onClick = { activeSubPage = "favourites" }
                                    )
                                    Divider(color = secondaryTextColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                                    ProfileSubOptionRow(
                                        icon = "💬",
                                        title = "Allow restaurants to contact you",
                                        textColor = textColor,
                                        secondaryTextColor = secondaryTextColor,
                                        showSwitch = true,
                                        checked = allowContact,
                                        onCheckedChange = { allowContact = it }
                                    )
                                }
                            }

                            // Standard app link controls & Log Out
                            item {
                                Button(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .height(48.dp)
                                        .testTag("profile_logout_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = orangeBrand),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Log Out",
                                        color = Color.White,
                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }

                            // App version labels matching mockup bottom
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "App version 4.108.1 (1745)",
                                        color = secondaryTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Dynamic restoration of standard quote and creator AP footers
                            item {
                                SkyroPageFooter(isNight = (appThemeMode == "NIGHT"), showExtended = true)
                            }
                        }

                        // -------------------------------------------------------------
                        // Overlying Collapsing Header Layout (On main profile only)
                        // -------------------------------------------------------------
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(max(56f, 175f * (1f - collapseProgress)).dp + 64.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (appThemeMode == "NIGHT") {
                                            listOf(SkyroColors.MidnightNav, SkyroColors.MidnightNav.copy(alpha = 0.85f), Color.Transparent)
                                        } else {
                                            listOf(Color(0xFFFFF0E6), Color(0xFFFFF0E6).copy(alpha = 0.95f), Color.Transparent)
                                        }
                                    )
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                // Top Bar controls (Back, title, Help, Menu)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.navigateBack() },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Navigate back",
                                            tint = if (appThemeMode == "NIGHT") Color.White else Color.Black
                                        )
                                    }

                                    // Collapsed morph header: displays minimized user details & title on scroll
                                    if (collapseProgress > 0.4f) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 12.dp)
                                                .alpha((collapseProgress - 0.4f) * 1.6f)
                                        ) {
                                            Text(
                                                text = preferences?.userName ?: "Sai Sankar",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (appThemeMode == "NIGHT") Color.White else Color.Black
                                            )
                                            Text(
                                                text = if (preferences?.phoneNumber?.isNotEmpty() == true) "+91 - ${preferences?.phoneNumber}" else "+91 - 8897191269",
                                                fontSize = 10.sp,
                                                color = secondaryTextColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Help Button with border
                                        Box(
                                            modifier = Modifier
                                                .border(
                                                    width = 1.dp,
                                                    color = if (appThemeMode == "NIGHT") Color.White.copy(alpha = 0.4f) else orangeBrand.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable { /* trigger help support fallback */ }
                                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "Help",
                                                color = if (appThemeMode == "NIGHT") Color.White else orangeBrand,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Menu options",
                                            tint = if (appThemeMode == "NIGHT") Color.White else Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Large expanded User info fields (Fades & translates nicely)
                                if (collapseProgress < 0.9f) {
                                    val fadeAlpha = (1f - collapseProgress * 1.25f).coerceIn(0f, 1f)
                                    val slideOffset = (collapseProgress * -50f).dp

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 64.dp)
                                            .offset(y = slideOffset)
                                            .alpha(fadeAlpha)
                                            .clickable { activeSubPage = "edit_profile" },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = preferences?.userName ?: "Sai Sankar",
                                                color = if (appThemeMode == "NIGHT") Color.White else Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 24.sp,
                                                letterSpacing = (-0.5).sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (preferences?.phoneNumber?.isNotEmpty() == true) "+91 - ${preferences?.phoneNumber}" else "+91 - 8897191269",
                                                color = secondaryTextColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = preferences?.userEmail ?: "saisankaryandamuri@gmail.com",
                                                color = secondaryTextColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text("✏️", fontSize = 10.sp)
                                                Text(
                                                    text = "Tap to Edit Profile",
                                                    color = orangeBrand,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        // User's helicopter avatar picture as present design
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .border(
                                                    width = 1.dp,
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(orangeBrand, Color.Transparent)
                                                    ),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🚁",
                                                fontSize = 32.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // -------------------------------------------------------------
                    // DYNAMIC SUB-PAGES COMPOSABLE RENDERER
                    // -------------------------------------------------------------
                    ProfileSubPageContent(
                        pageId = subPage,
                        appThemeMode = appThemeMode,
                        backgroundBrush = backgroundBrush,
                        cardBackground = cardBackground,
                        borderBrush = borderBrush,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        orangeBrand = orangeBrand,
                        preferences = preferences,
                        viewModel = viewModel,
                        onBack = { activeSubPage = null }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DYNAMIC SUB-PAGES LAYOUT CONTENT RENDERER
// -------------------------------------------------------------
@Composable
fun ProfileSubPageContent(
    pageId: String,
    appThemeMode: String,
    backgroundBrush: Brush,
    cardBackground: Color,
    borderBrush: Brush,
    textColor: Color,
    secondaryTextColor: Color,
    orangeBrand: Color,
    preferences: com.example.data.UserPreference?,
    viewModel: SkyroViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isNight = (appThemeMode == "NIGHT")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // High fidelity Sub-Page Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back to Profile list",
                    tint = if (isNight) Color.White else Color.Black
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = when (pageId) {
                    "address" -> "Saved Addresses"
                    "payments" -> "Payment Modes"
                    "refunds" -> "My Refunds"
                    "statements" -> "Account Statements"
                    "student_rewards" -> "Student Rewards"
                    "favourites" -> "Favourites"
                    "edit_profile" -> "Edit Profile"
                    else -> "Sub-Page"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (isNight) Color.White else Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        // Sub Page list content body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (pageId) {
                // -------------------------------------------------------------
                // 1. SAVED ADDRESSES PAGE
                // -------------------------------------------------------------
                "address" -> {
                    Text(
                        text = "YOUR DELIVERY LOCATIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Address 1: Locked to SRM AP Campus (Default & Active!)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SrmCampusBuildingIcon(
                                    address = preferences?.address ?: "SRM AP Campus",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SRM AP Campus",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .background(orangeBrand, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Siri Maha Mani, SRM University AP, Neerukonda, Mangalagiri, Andhra Pradesh 522240",
                                color = textColor.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = secondaryTextColor.copy(alpha = 0.1f), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LOCKED SECURE RESIDENT HOSTEL ADDRESS",
                                color = orangeBrand,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Address 2: Work Campus
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💼", fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Work / Tech Park",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Block C, 4th Floor, SRM Research Tower, Amaravati, Andhra Pradesh 522240",
                                color = textColor.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Address 3: Home Setup
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏠", fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Home (Hyderabad)",
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Plot 42, Jubilee Hills, near Metro Station, Hyderabad, Telangana 500033",
                                color = textColor.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Interactive Add Address Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, orangeBrand.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .background(cardBackground)
                            .clickable { /* Trigger quick add mockup */ }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ ADD NEW ADDRESS",
                            color = orangeBrand,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // -------------------------------------------------------------
                // 2. PAYMENT MODES PAGE
                // -------------------------------------------------------------
                "payments" -> {
                    Text(
                        text = "SAVED UPI & CREDIT CARDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // UPI Modes
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Direct UPI Accounts",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // GPay option
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🧩", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Google Pay (Active)", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("saisankar@okaxis", color = secondaryTextColor, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(orangeBrand, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = secondaryTextColor.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // PhonePe Option
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔮", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("PhonePe UPI", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("8897191269@ybl", color = secondaryTextColor, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Card Payment Mode
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Credit & Debit Cards",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.DarkGray, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("VISA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("HDFC Bank Infinia Card", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("•••• •••• •••• 9910", color = secondaryTextColor, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. MY REFUNDS PAGE
                // -------------------------------------------------------------
                "refunds" -> {
                    Text(
                        text = "LAST 30 DAYS WALLET REFUNDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Refund Item 1
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Refund for order #SK99217",
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "₹120.00",
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Reason: Item missing from deliveries at SRM AP Hostel",
                                color = secondaryTextColor,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Visual Step Progress Tracker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Initiated", color = secondaryTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Processed", color = secondaryTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Credited to UPI", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Refund Item 2
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Refund for order #SK99104",
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "₹240.00",
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Reason: Delayed Order Delivery time exceedance",
                                color = secondaryTextColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 4. ACCOUNT STATEMENTS PAGE
                // -------------------------------------------------------------
                "statements" -> {
                    Text(
                        text = "DETAILED EXPORT & DIGITAL STATEMENTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Select Export Period",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Month selections
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(orangeBrand, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Feb 2026", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Jan 2026", color = textColor, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Dec 2025", color = textColor, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { /* send email action */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = orangeBrand)
                            ) {
                                Text("Email Statement (PDF)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 5. STUDENT REWARDS PAGE
                // -------------------------------------------------------------
                "student_rewards" -> {
                    Text(
                        text = "SRM UNIVERSITY AP CAMPUS CLUB EXCLUSIVES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // SRM Free Delivery Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE0F2FE), CircleShape)
                                        .padding(6.dp)
                                ) {
                                    Text("🎓", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SRM ELITE CLUB MEMBERSHIP",
                                        color = textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Flat 50% Off & Free Hostel delivery",
                                        color = secondaryTextColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Coupon Code: SRMELITE",
                                color = orangeBrand,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "*Valid exclusively at all outlets delivering inside SRM AP Campus borders.",
                                color = secondaryTextColor,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Night Canteen Pass Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🌌 Midnight Fuel canteen Pass",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Free delivery charge from 11:00 PM to 2:00 AM daily for SRM hostelers. Unlock exam season study fuels!",
                                color = secondaryTextColor,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 6. FAVOURITES PAGE
                // -------------------------------------------------------------
                "favourites" -> {
                    Text(
                        text = "YOUR SAVED RESTAURANTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Restaurant Favourites 1: Domino's Pizza
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(orangeBrand.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍕", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Domino's Pizza", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text("Fast Food, Pizza  •  4.5 ⭐ (50+ orders)", color = secondaryTextColor, fontSize = 11.sp)
                                Text("Delivering to SRM AP: 25 mins", color = orangeBrand, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "❤️",
                                fontSize = 18.sp,
                                modifier = Modifier.clickable { /* Toggle representation */ }
                            )
                        }
                    }

                    // Restaurant Favourites 2: Big Bowl
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(orangeBrand.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍛", fontSize = 24.sp)
                             }
                             Spacer(modifier = Modifier.width(12.dp))
                             Column(modifier = Modifier.weight(1f)) {
                                 Text("Big Bowl", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                 Text("Rice Bowls, Chinese  •  4.3 ⭐", color = secondaryTextColor, fontSize = 11.sp)
                                 Text("Delivering to SRM AP: 20 mins", color = orangeBrand, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                             }
                             Text(
                                 text = "❤️",
                                 fontSize = 18.sp,
                                 modifier = Modifier.clickable { /* Toggle representation */ }
                             )
                        }
                    }
                }

                "edit_profile" -> {
                    Text(
                        text = "EDIT YOUR DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = orangeBrand,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    var editName by remember { mutableStateOf(preferences?.userName ?: "Sai Sankar") }
                    var editPhone by remember { mutableStateOf(preferences?.phoneNumber ?: "8897191269") }
                    var editEmail by remember { mutableStateOf(preferences?.userEmail ?: "saisankaryandamuri@gmail.com") }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        appThemeMode = appThemeMode
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Full Name
                            TextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("FULL NAME", style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = orangeBrand,
                                    unfocusedIndicatorColor = textColor.copy(alpha = 0.15f),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = orangeBrand,
                                    unfocusedLabelColor = secondaryTextColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Google Email
                            TextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("GOOGLE ACCOUNT EMAIL", style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = orangeBrand,
                                    unfocusedIndicatorColor = textColor.copy(alpha = 0.15f),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = orangeBrand,
                                    unfocusedLabelColor = secondaryTextColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Phone Number
                            TextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("MOBILE PHONE NUMBER", style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = orangeBrand,
                                    unfocusedIndicatorColor = textColor.copy(alpha = 0.15f),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = orangeBrand,
                                    unfocusedLabelColor = secondaryTextColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var isSaving by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    Button(
                        onClick = {
                            isSaving = true
                            coroutineScope.launch {
                                delay(1200)
                                viewModel.updateUserProfile(editName, editPhone, editEmail)
                                isSaving = false
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = orangeBrand
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Save Profile Changes",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Standard footers applied inside sub-pages for design excellence and restoring quotes
            SkyroPageFooter(isNight = isNight, showExtended = false)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// Component Helpers
// -------------------------------------------------------------

@Composable
fun GridPillCard(
    emoji: String,
    title: String,
    appThemeMode: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val cardBg = getThemeCardBg(appThemeMode)
    val borderBrush = getThemeBorderBrush(appThemeMode)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (appThemeMode != "SKYRO_PRESENT") {
                    Modifier.border(1.dp, borderBrush, RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(1.dp, Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                }
            )
            .background(cardBg)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 13.sp
        )
    }
}

@Composable
fun ProfileSubOptionRow(
    icon: String,
    title: String,
    textColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit = {},
    showSwitch: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (showSwitch) {
                    onCheckedChange(!checked)
                } else {
                    onClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (showSwitch) {
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFFC8019)
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Edit details option",
                tint = secondaryTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ThemeOptionButton(
    name: String,
    emoji: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    cardBg: Color,
    activeBg: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) activeBg else Color.Black.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (active) Color.White.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                color = if (active) Color.White else textColor.copy(alpha = 0.75f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------------------------------------------------
// Theming Helpers
// -------------------------------------------------------------

internal fun getThemeBackgroundBrush(mode: String): Brush {
    return when (mode) {
        "SUNNY" -> {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFAED), // Very soft sunny warm gold top
                    Color(0xFFFFEBD6), // Warm apricot blend
                    Color(0xFFFFF0E1)  // Peachy cream base
                )
            )
        }
        "NIGHT" -> {
            Brush.linearGradient(
                colors = listOf(
                    SkyroColors.MidnightNav,
                    SkyroColors.NightPurple,
                    SkyroColors.NightGradientDeep
                )
            )
        }
        else -> { // "SKYRO_PRESENT"
            // Use the present home theme signature gradient (vibrant orange to teal gradient)
            Brush.linearGradient(
                colors = listOf(
                    SkyroColors.Sunrise,
                    SkyroColors.Amber,
                    SkyroColors.SkyBlue
                )
            )
        }
    }
}

internal fun getThemeCardBg(mode: String): Color {
    return when (mode) {
        "NIGHT" -> Color(0x33FFFFFF)               // Translucent glowing glass dark
        else -> Color.White                         // Flat professional solid white
    }
}

internal fun getThemeBorderBrush(mode: String): Brush {
    return when (mode) {
        "SUNNY" -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))
        "NIGHT" -> Brush.verticalGradient(listOf(SkyroColors.CyanGlow.copy(alpha = 0.35f), Color.Transparent))
        else -> Brush.verticalGradient(listOf(Color.LightGray.copy(alpha = 0.12f), Color.LightGray.copy(alpha = 0.05f)))
    }
}

internal fun getThemeTextColor(mode: String): Color {
    return when (mode) {
        "NIGHT" -> Color.White
        "SUNNY" -> Color(0xFF452712) // Rich hot amber coffee color for maximum contrast and sun warmth
        else -> Color(0xFF1E293B)   // Jet black/slate dark gray
    }
}

internal fun getThemeSecondaryTextColor(mode: String): Color {
    return when (mode) {
        "NIGHT" -> Color.White.copy(alpha = 0.6f)
        "SUNNY" -> Color(0xFF7A4826) // Softened milk chocolate brown
        else -> Color.Gray
    }
}
