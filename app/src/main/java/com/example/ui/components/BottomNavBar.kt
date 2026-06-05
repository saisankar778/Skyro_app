package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkyroColors
import kotlin.math.sin

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    isNight: Boolean = false
) {
    val navItems = listOf(
        NavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("order_history", "Orders", Icons.Filled.List, Icons.Outlined.List),
        NavItem("saved_items", "Explore", Icons.Filled.Favorite, Icons.Outlined.Favorite), // Stable core icon representing favorites/explore
        NavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val shadowColor = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise
    val barBackground = if (isNight) SkyroColors.NightPurple else SkyroColors.WarmCream
    val activeColor = if (isNight) SkyroColors.CyanGlow else Color(0xFFFC8019) // High visual contrast accents
    val inactiveTrackColor = if (isNight) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)

    // -------------------------------------------------------------
    // Core flight tracking animation loops
    // -------------------------------------------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "CanopyTrackDrone")

    // Progress of drone flying from left (0.05) to right (0.95)
    val progress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CanopyFlightProgress"
    )

    // Running phase shift for back-flowing trailing wake waves
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WakeWavePhase"
    )

    // Micro aerodynamic drone bobbing offset
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DroneAerodynamicBob"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(115.dp) // Custom high comfort design giving canopy corridor height
            .background(Color.Transparent)
    ) {
        // -------------------------------------------------------------
        // DRAWING REQUIREMENT: Unified Canopy Airway Track (Canvas Layer)
        // -------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w < 120.dp.toPx() || h < 20.dp.toPx()) return@Canvas

            val capsuleLeft = 16.dp.toPx()
            val capsuleRight = w - 16.dp.toPx()
            val capsuleTop = 40.dp.toPx() // Height coordinate where navigation capsule begins
            val canopyTop = 14.dp.toPx()  // Peak coordinates where flat airway corridor runs

            // Horizon run bounds
            val startX = capsuleLeft + 28.dp.toPx()
            val endX = capsuleRight - 28.dp.toPx()
            val droneX = startX + progress * (endX - startX)

            // A. Draw trailing active wake path behind drone (flowing sinusoidal waves)
            if (droneX > startX) {
                val wavePath = Path()
                // Left curve joining the nav capsule edge smoothly
                wavePath.moveTo(capsuleLeft, capsuleTop)
                wavePath.cubicTo(
                    capsuleLeft, canopyTop,
                    capsuleLeft + 12.dp.toPx(), canopyTop,
                    startX, canopyTop
                )

                // Sine wave from startX to the current drone pointer
                val waveStep = 6
                val waveFrequency = 0.045f
                val amplitude = 5.dp.toPx()
                val transitionRange = 40.dp.toPx() // Range to transition amplitude smoothly

                for (x in startX.toInt()..droneX.toInt() step waveStep) {
                    val distFromStart = x - startX
                    val distFromEnd = droneX - x
                    
                    // Safe envelope multipliers fading to 0 at the very ends
                    val startFade = (distFromStart / transitionRange).coerceIn(0f, 1f)
                    val endFade = (distFromEnd / transitionRange).coerceIn(0f, 1f)
                    val currentAmplitude = amplitude * startFade * endFade

                    // (x - droneX) locks the wave anchor point to the tail of the drone.
                    // Adding wavePhase causes wave cycles to propagate backwards (leftward) away from the drone!
                    val y = canopyTop + sin((x - droneX) * waveFrequency + wavePhase) * currentAmplitude
                    wavePath.lineTo(x.toFloat(), y)
                }
                
                // Close accurate endpoint
                wavePath.lineTo(droneX, canopyTop)

                drawPath(
                    path = wavePath,
                    color = activeColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                // If drone is near start, just draw the static starting curve
                val curvePath = Path().apply {
                    moveTo(capsuleLeft, capsuleTop)
                    cubicTo(
                        capsuleLeft, canopyTop,
                        capsuleLeft + 12.dp.toPx(), canopyTop,
                        startX, canopyTop
                    )
                }
                drawPath(path = curvePath, color = activeColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }

            // B. Draw inactive smooth runway path ahead of the drone (flat line + right arch)
            val inactivePath = Path()
            inactivePath.moveTo(droneX, canopyTop)
            inactivePath.lineTo(endX, canopyTop)
            inactivePath.cubicTo(
                capsuleRight - 12.dp.toPx(), canopyTop,
                capsuleRight, canopyTop,
                capsuleRight, capsuleTop
            )

            drawPath(
                path = inactivePath,
                color = inactiveTrackColor,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // C. Draw paper-airplane style navigation locator pointing right (matching user's uploaded icon)
            val currentDroneY = canopyTop + bobOffset.dp.toPx()
            val arrowPath = Path().apply {
                // Nose apex pointing right (scaled up)
                moveTo(droneX + 13.dp.toPx(), currentDroneY)
                // Upper wing line (scaled up)
                lineTo(droneX - 10.dp.toPx(), currentDroneY - 10.dp.toPx())
                // Deep inner cleft pointing inwards toward the front nose (scaled up)
                lineTo(droneX - 2.dp.toPx(), currentDroneY)
                // Lower wing line (scaled up)
                lineTo(droneX - 10.dp.toPx(), currentDroneY + 10.dp.toPx())
                close()
            }
            drawPath(path = arrowPath, color = if (isNight) SkyroColors.Sunrise else Color.Black)
        }

        // -------------------------------------------------------------
        // Core Bottom Navigation Capsule (Interior UI)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp) // Keeps precise distance below the canopy line
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = shadowColor.copy(alpha = 0.15f),
                    spotColor = shadowColor.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(barBackground.copy(alpha = 0.95f))
                .border(
                    1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isActive = selectedRoute == item.route

                    // Soft smooth width transition for active tab pills
                    val pillWidth by animateDpAsState(
                        targetValue = if (isActive) 85.dp else 45.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "BottomNavPillWidth"
                    )

                    val accentColor = SkyroColors.Sunrise
                    val pillBackground = if (isActive) accentColor.copy(alpha = 0.15f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(16.dp))
                            .background(pillBackground)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // Disable generic ripple for custom fluid pill transition
                                onClick = { onRouteSelected(item.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isActive) item.activeIcon else item.inactiveIcon,
                                contentDescription = item.label,
                                tint = if (isActive) accentColor else (if (isNight) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.7f)),
                                modifier = Modifier.size(22.dp)
                            )

                            if (isActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.label,
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
