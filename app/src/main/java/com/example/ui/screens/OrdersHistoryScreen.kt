package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.DeliveryOrder
import com.example.ui.SkyroViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.GlassCard
import com.example.ui.components.SkyroPageFooter
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@Composable
fun OrdersHistoryScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onTrackClick: () -> Unit
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val serverOrders by viewModel.serverOrders.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val isNightTheme = (appThemeMode == "NIGHT")
    val isSunnyTheme = (appThemeMode == "SUNNY" || appThemeMode == "SKYRO_PRESENT")

    // Fetch server orders whenever screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchServerOrderHistory()
    }
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Active, 1 = Past, 2 = Server
    
    val backgroundBrush = SkyroColors.getThemeBackgroundBrush(appThemeMode)
    val textColor = SkyroColors.getThemeTextColor(appThemeMode)
    val secondaryTextColor = SkyroColors.getThemeSecondaryTextColor(appThemeMode)
    val themeColor = if (isNightTheme) SkyroColors.CyanGlow else SkyroColors.Sunrise

    val activeList = allOrders.filter { it.status != "DELIVERED" }
    val pastList = allOrders.filter { it.status == "DELIVERED" }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(selectedRoute = "order_history", onRouteSelected = { route ->
                if (route != "order_history") {
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
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            if (isNightTheme) {
                com.example.ui.components.CustomStarfield()
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header Display Title
                Text(
                    text = "Flight Logs & History",
                    color = textColor,
                    style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Navigation internal Tab selectors
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = themeColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = themeColor
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Active (${activeList.size})",
                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) textColor else textColor.copy(alpha = 0.5f)
                        )
                    }
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Past (${pastList.size})",
                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) textColor else textColor.copy(alpha = 0.5f)
                        )
                    }
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.fetchServerOrderHistory()
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Server (${serverOrders.size})",
                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 2) themeColor else textColor.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server Orders Tab
                if (selectedTab == 2) {
                    if (serverOrders.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🌐", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Server Orders Yet",
                                style = SkyroTypography.H2.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Orders submitted to the backend will appear here. Make sure your API URL is set in Profile settings.",
                                style = SkyroTypography.Caption,
                                color = secondaryTextColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(serverOrders) { ord ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth().testTag("server_order_card_${ord.id}"),
                                    isNight = isNightTheme,
                                    appThemeMode = appThemeMode,
                                    borderRadius = 18.dp,
                                    elevation = 3.dp
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ord.id.take(16) + if (ord.id.length > 16) "…" else "",
                                                style = SkyroTypography.Caption.copy(fontWeight = FontWeight.ExtraBold),
                                                color = themeColor
                                            )
                                            val statusColor = when (ord.status) {
                                                "Delivered" -> Color(0xFF2E7D32)
                                                "En Route", "Cooking" -> SkyroColors.Amber
                                                "Placed", "Accepted" -> if (isNightTheme) SkyroColors.CyanGlow else SkyroColors.Sunrise
                                                else -> secondaryTextColor
                                            }
                                            Text(
                                                text = ord.status,
                                                style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Black),
                                                color = statusColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ord.items.joinToString { "${it.name} x${it.quantity}" },
                                            style = SkyroTypography.Caption,
                                            color = secondaryTextColor,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📍 ${ord.deliveryLocationId.take(8)}",
                                                style = SkyroTypography.Caption,
                                                color = secondaryTextColor
                                            )
                                            Text(
                                                text = "₹${ord.total.toInt()}",
                                                style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Black),
                                                color = themeColor
                                            )
                                        }
                                    }
                                }
                            }
                            item { SkyroPageFooter(isNight = isNightTheme) }
                        }
                    }
                } else {

                // Local orders tabs (Active + Past)
                val currentDisplayList = if (selectedTab == 0) activeList else pastList

                if (currentDisplayList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📥",
                            fontSize = 72.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Flight Records",
                            style = SkyroTypography.H2.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedTab == 0) "No drone delivery caskets currently cruising in flights lanes. Order some delicious dishes now!" else "You haven't ordered any meals yet. C'mon and order!",
                            style = SkyroTypography.Caption,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        SkyroPageFooter(isNight = isNightTheme)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentDisplayList) { ord ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.startTrackingOrder(ord.orderId)
                                    }
                                    .testTag("flight_history_card_${ord.orderId}"),
                                isNight = isNightTheme,
                                appThemeMode = appThemeMode,
                                borderRadius = 18.dp,
                                elevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.0f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ord.orderId,
                                                style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.ExtraBold),
                                                color = textColor
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Status Badge
                                            val badgeBg = when (ord.status) {
                                                "PREPARING" -> SkyroColors.Amber.copy(alpha = 0.2f)
                                                "DISPATCHED", "IN_FLIGHT" -> SkyroColors.CyanGlow.copy(alpha = 0.2f)
                                                else -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            }
                                            val badgeTextClr = when (ord.status) {
                                                "PREPARING" -> SkyroColors.Amber
                                                "DISPATCHED", "IN_FLIGHT" -> if (isSunnyTheme) Color(0xFF0369A1) else SkyroColors.CyanGlow
                                                else -> Color(0xFF2E7D32)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeBg, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = ord.status,
                                                    color = badgeTextClr,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ord.restaurantName,
                                            style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                            color = textColor
                                        )
                                        Text(
                                            text = ord.itemsSummary,
                                            style = SkyroTypography.Caption,
                                            color = secondaryTextColor,
                                            maxLines = 1
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${ord.totalPrice.toInt()}",
                                            style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                            color = if (isSunnyTheme) Color(0xFFD84B16) else themeColor
                                        )
                                        
                                        if (ord.status == "DELIVERED") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.triggerQuickReorder(ord) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSunnyTheme) SkyroColors.Sunrise.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f),
                                                    contentColor = if (isSunnyTheme) SkyroColors.Sunrise else Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Reorder", style = SkyroTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Track Live",
                                                color = if (isSunnyTheme) Color(0xFFD84B16) else themeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { onTrackClick() }
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
                } // end local tabs
            }
        }
    }
}
