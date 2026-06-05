package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false, // Ignored, we use the centralized theme state instead
    onBack: () -> Unit
) {
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNightTheme = (appThemeMode == "NIGHT")
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")
    
    val cartItems by viewModel.cartItems.collectAsState()
    val dishesList by viewModel.dishesList.collectAsState()
    val deliveryLocations  by viewModel.deliveryLocations.collectAsState()
    val selectedLocId      by viewModel.selectedDropLocationId.collectAsState()
    val selectedLocName    by viewModel.selectedDropLocationName.collectAsState()
    val isPlacingOrder     by viewModel.isPlacingOrder.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    var showLocationMenu   by remember { mutableStateOf(false) }

    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)
    val cardBg = SkyroColors.getThemeCardBg(appThemeMode)
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)
    val themeColor = if (isNightTheme) SkyroColors.CyanGlow else if (isSunnyTheme) Color(0xFFEA580C) else SkyroColors.Sunrise

    // Pricing splits
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = if (cartItems.isNotEmpty()) 20.0 else 0.0
    val platformFee = if (cartItems.isNotEmpty()) 5.0 else 0.0
    val safetyWrap = if (cartItems.isNotEmpty()) 15.0 else 0.0
    val totalAmount = subtotal + deliveryFee + platformFee + safetyWrap

    // Drone cargo payload — use real weight_grams from DB via dishesList lookup
    val estimatedWeightGrams = cartItems.sumOf { cartItem ->
        val dish = dishesList.find { it.id == cartItem.menuItemId || it.name == cartItem.name }
        (dish?.weightGrams ?: 300) * cartItem.quantity
    }
    val maxPayloadGrams = 1000

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Slide Payload Cart",
                        style = SkyroTypography.H2.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isSunnyTheme) textColor else Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(if (isSunnyTheme) textColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("cart_back_button")
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
                .padding(top = 90.dp)
        ) {
            if (cartItems.isEmpty()) {
                // Empty state card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📦",
                        fontSize = 80.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Payload Empty",
                        style = SkyroTypography.H1.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isSunnyTheme) textColor else if (isNightTheme) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add delicious dynamic drone-eligible meals and beverages from campus canteens to dispatch flight lanes.",
                        style = SkyroTypography.Body,
                        color = if (isSunnyTheme) secondaryTextColor else if (isNightTheme) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Cart lists grouping
                    item {
                        Text(
                            text = "Selected Cargo Items",
                            style = SkyroTypography.H2.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isNightTheme) Color.White else Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(cartItems, key = { it.id }) { item ->
                        // Swipe / Dismissible row visual in compose
                        SwipeDismissCartItem(
                            item = item,
                            isNight = isNightTheme,
                            appThemeMode = appThemeMode,
                            onRemove = { viewModel.removeCartItemDirect(item.id) }
                        )
                    }

                    // Drone weight specs check payload warning limits
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("drone_payload_card"),
                            isNight = isNightTheme,
                            appThemeMode = appThemeMode,
                            borderRadius = 20.dp,
                            elevation = 4.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "📦 DRONE CARGO payload",
                                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black),
                                            color = if (isNightTheme) SkyroColors.CyanGlow else SkyroColors.Sunrise
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$estimatedWeightGrams g / $maxPayloadGrams g max",
                                            style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                            color = textColor
                                        )
                                    }
                                    
                                    val safeRatio = estimatedWeightGrams.toFloat() / maxPayloadGrams.toFloat()
                                    val statusColor = when {
                                        safeRatio < 0.6f -> Color(0xFF4CAF50) // Green safe
                                        safeRatio < 0.9f -> SkyroColors.Amber // Warm amber capacity
                                        else -> SkyroColors.DeepCoral // Red heavy limits
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (safeRatio >= 1.0f) "OVERWEIGHT" else "STABLE LOAD",
                                            color = statusColor,
                                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Cargo weight progress bar
                                val progressFraction = (estimatedWeightGrams.toFloat() / maxPayloadGrams).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (estimatedWeightGrams > maxPayloadGrams) SkyroColors.DeepCoral else themeColor,
                                    trackColor = Color.White.copy(alpha = 0.15f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Estimated flight lane distance: 2.3 km (DGCA Compliant Campus Routing)",
                                    style = SkyroTypography.Caption.copy(fontSize = 11.sp),
                                    color = secondaryTextColor
                                )
                            }
                        }
                    }

                    // Delivery Location Picker
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_location_card"),
                            isNight = isNightTheme,
                            appThemeMode = appThemeMode,
                            borderRadius = 16.dp,
                            elevation = 4.dp
                        ) {
                            Column {
                                Text(
                                    text = "📍 DROP ZONE — Delivery Location",
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black),
                                    color = if (isNightTheme) SkyroColors.CyanGlow else SkyroColors.Sunrise
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (deliveryLocations.isEmpty()) {
                                    Text(
                                        text = "⚠ No delivery zones loaded yet. Check your API connection in Profile settings.",
                                        style = SkyroTypography.Caption,
                                        color = secondaryTextColor
                                    )
                                } else {
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = if (isNightTheme) Color.White.copy(alpha = 0.08f)
                                                            else Color.Black.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 1.5.dp,
                                                    color = themeColor.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { showLocationMenu = true }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                                .testTag("location_picker_row"),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocationOn,
                                                    contentDescription = null,
                                                    tint = themeColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = selectedLocName.ifEmpty { "Select drop location" },
                                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (selectedLocName.isEmpty() || selectedLocName == "Select Delivery Location")
                                                        secondaryTextColor else textColor
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Filled.KeyboardArrowDown,
                                                contentDescription = "Pick location",
                                                tint = secondaryTextColor
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showLocationMenu,
                                            onDismissRequest = { showLocationMenu = false },
                                            modifier = Modifier
                                                .background(
                                                    if (isNightTheme) Color(0xFF1A2035) else Color.White
                                                )
                                        ) {
                                            deliveryLocations.forEach { loc ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = loc.name,
                                                            style = SkyroTypography.Body,
                                                            color = textColor
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.selectDeliveryLocation(loc.id, loc.name)
                                                        showLocationMenu = false
                                                    },
                                                    leadingIcon = {
                                                        Text(
                                                            text = if (loc.id == selectedLocId) "✅" else "📍"
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("location_item_${loc.id}")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Core Payment Breakouts Breakdown
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_breakdown_card"),
                            isNight = isNightTheme,
                            appThemeMode = appThemeMode,
                            borderRadius = 16.dp,
                            elevation = 2.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Payment Breakdown",
                                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black),
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                BillRow(label = "Items Subtotal", amount = subtotal, appThemeMode = appThemeMode)
                                BillRow(label = "Drone Delivery Fee", amount = deliveryFee, appThemeMode = appThemeMode)
                                BillRow(label = "Platform Handling Fee", amount = platformFee, appThemeMode = appThemeMode)
                                BillRow(label = "Thermodynamic Safety Wrap", amount = safetyWrap, appThemeMode = appThemeMode)
                                Divider(color = if (isNightTheme) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Payload Amount",
                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                    Text(
                                        text = "₹${totalAmount.toInt()}",
                                        style = SkyroTypography.H2.copy(fontWeight = FontWeight.ExtraBold),
                                        color = themeColor
                                    )
                                }
                            }
                        }
                    }

                    // Payment Method Selector
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().testTag("payment_method_card"),
                            isNight = isNightTheme,
                            appThemeMode = appThemeMode,
                            borderRadius = 16.dp,
                            elevation = 4.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "💳 PAYMENT METHOD",
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black),
                                    color = if (isNightTheme) SkyroColors.CyanGlow else SkyroColors.Sunrise
                                )
                                val paymentOptions = listOf(
                                    Triple("COD", "💵", "Cash on Delivery"),
                                    Triple("UPI", "📱", "UPI / GPay / PhonePe"),
                                    Triple("CARD", "💳", "Credit / Debit Card")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    paymentOptions.forEach { (code, emoji, label) ->
                                        val isSelected = selectedPaymentMethod == code
                                        val pillColor = if (isSelected) themeColor else (
                                            if (isNightTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(pillColor, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) themeColor else themeColor.copy(alpha = 0.3f),
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                                )
                                                .clickable { viewModel.selectPaymentMethod(code) }
                                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                                .testTag("pay_${code}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(emoji, fontSize = 18.sp)
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else textColor,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Place Flight dispatch CTA
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Warn if no location selected
                        val hasLocation = selectedLocId.isNotEmpty() && selectedLocName != "Select Delivery Location"
                        if (!hasLocation && deliveryLocations.isNotEmpty()) {
                            Text(
                                text = "⚠ Please select a delivery location above before dispatching.",
                                style = SkyroTypography.Caption,
                                color = SkyroColors.Amber,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (!isPlacingOrder) viewModel.checkoutAndLaunchCinematic(selectedPaymentMethod)
                            },
                            enabled = !isPlacingOrder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = themeColor.copy(alpha = 0.3f),
                                    spotColor = themeColor.copy(alpha = 0.5f)
                                )
                                .testTag("place_order_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = if (isPlacingOrder)
                                                listOf(Color.Gray, Color.DarkGray)
                                            else
                                                listOf(SkyroColors.Sunrise, SkyroColors.DeepCoral)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlacingOrder) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Submitting Order...",
                                            color = Color.White,
                                            style = SkyroTypography.H2.copy(fontSize = 15.sp, fontWeight = FontWeight.Black)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "DISPATCH DRONE FLIGHT · ₹${totalAmount.toInt()} · $selectedPaymentMethod",
                                        color = Color.White,
                                        style = SkyroTypography.H2.copy(fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    )
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

@Composable
fun SwipeDismissCartItem(
    item: com.example.data.CartItem,
    isNight: Boolean,
    appThemeMode: String,
    onRemove: () -> Unit
) {
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.id}"),
        isNight = isNight,
        appThemeMode = appThemeMode,
        borderRadius = 16.dp,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = "Payload of ${item.restaurantName}",
                    style = SkyroTypography.Caption,
                    color = secondaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${item.price.toInt()} x ${item.quantity}",
                    style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Bold),
                    color = if (isNight) SkyroColors.CyanGlow else if (appThemeMode == "SUNNY") Color(0xFFEA580C) else SkyroColors.Sunrise
                )
            }
            
            // Inline Delete trigger
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .background(SkyroColors.DeepCoral.copy(alpha = 0.15f), CircleShape)
                    .testTag("delete_item_button_${item.id}"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = SkyroColors.DeepCoral)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Swipe remove"
                )
            }
        }
    }
}

@Composable
fun BillRow(label: String, amount: Double, appThemeMode: String) {
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = SkyroTypography.Caption,
            color = secondaryTextColor
          )
        Text(
            text = "₹${amount.toInt()}",
            style = SkyroTypography.PriceMono.copy(fontSize = 13.sp),
            color = textColor
        )
    }
}
