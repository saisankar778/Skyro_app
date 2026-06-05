package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dish
import com.example.data.Restaurant
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onTap: () -> Unit,
    isNight: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        label = "RestaurantCardScale"
    )

    // Pre-allocated unique aesthetic gradient sets
    val gradientColors = when (restaurant.gradientIndex) {
        0 -> listOf(Color(0xFFFF512F), Color(0xFFDD2476)) // Orange to Pink-red
        1 -> listOf(Color(0xFF4776E6), Color(0xFF8E54E9)) // Electric Purple
        2 -> listOf(Color(0xFF00B4DB), Color(0xFF0083B0)) // Cool Cyan blue
        else -> listOf(Color(0xFF11998e), Color(0xFF38ef7d)) // Green glow
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(animatedScale)
            .testTag("restaurant_card_${restaurant.id}")
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom scale animation handled manually
                onClick = onTap
            ),
        isNight = isNight,
        borderRadius = 24.dp,
        elevation = 6.dp
    ) {
        // Aesthetic Gradient Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
        ) {
            // Visual drone silhouette overlay as branding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            radius = 400f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚁",
                    fontSize = 72.sp,
                    color = Color.White.copy(alpha = 0.15f)
                )
            }

            // Top overlay badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left discount promo badge
                restaurant.promoBadge?.let { promo ->
                    Box(
                        modifier = Modifier
                            .background(SkyroColors.DeepCoral, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = promo,
                            color = Color.White,
                            style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Right ETA Drone Badge
                DroneEtaBadge(etaMinutes = restaurant.etaMin, isNight = isNight)
            }

            // Bottom glass info half
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.0f), Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = restaurant.name,
                        color = Color.White,
                        style = SkyroTypography.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.cuisine,
                            color = Color.White.copy(alpha = 0.8f),
                            style = SkyroTypography.Caption
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Reviews rating",
                                tint = SkyroColors.Amber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${restaurant.rating}",
                                color = Color.White,
                                style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DishCard(
    dish: Dish,
    onAddClick: () -> Unit,
    isNight: Boolean = false
) {
    val gradientColors = if (isNight) {
        listOf(SkyroColors.MidnightNav, SkyroColors.NightGradientDeep)
    } else {
        listOf(SkyroColors.WarmCream, Color.White)
    }

    val shadowColor = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(210.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = shadowColor.copy(alpha = 0.15f),
                spotColor = shadowColor.copy(alpha = 0.25f)
            )
            .testTag("dish_card_${dish.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = gradientColors))
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Food visual placeholder card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SkyroColors.GoldenHour.copy(alpha = 0.4f),
                                SkyroColors.DeepCoral.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (dish.name) {
                        "Chicken Dum Biryani" -> "🍱"
                        "Paneer Tikka Masala" -> "🥘"
                        "Loaded Cheese Fries" -> "🍟"
                        "Smoky BBQ Burger" -> "🍔"
                        "Cold Brew Coffee" -> "☕"
                        "Nutella Hazelnut Waffle" -> "🧇"
                        "Veg Momos (6pc)" -> "🥟"
                        "Schezwan Hakka Noodles" -> "🍜"
                        else -> "🍕"
                    },
                    fontSize = 38.sp
                )

                if (dish.isBestSeller) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(SkyroColors.DeepCoral, RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BEST",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Info Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dish.name,
                    color = if (isNight) Color.White else Color.Black,
                    style = SkyroTypography.Body.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    ),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (dish.isVeg) Color(0xFF4CAF50) else Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (dish.isVeg) "Veg" else "Non-Veg",
                        color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        style = SkyroTypography.Caption.copy(fontSize = 10.sp)
                    )
                }
            }

            // Price / Add action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${dish.price.toInt()}",
                    color = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise,
                    style = SkyroTypography.PriceMono.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                )

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("add_dish_button_${dish.id}"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add to cart",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
