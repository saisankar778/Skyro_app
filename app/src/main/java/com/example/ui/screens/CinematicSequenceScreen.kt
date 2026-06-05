package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ExplodingParticle(
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun CinematicSequenceScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false
) {
    val currentFrame by viewModel.cinematicSequenceFrame.collectAsState()

    // -------------------------------------------------------------
    // Stage-specific local animations
    // -------------------------------------------------------------
    
    // Timeline 1: Kitchen Prep Progress bar filling (0.5 to 1.5s)
    val kitchenPrepProgress = remember { Animatable(0f) }
    
    // Timeline 2: White camera flash (opacity 0 -> 0.8 -> 0 in 400ms at 1.5s)
    val flashOpacity = remember { Animatable(0f) }
    
    // Timeline 3: Jitter Camera shake (6 cycles of ±4px at 1.5s to 2.2s)
    val shakeX = remember { Animatable(0f) }
    val shakeY = remember { Animatable(0f) }

    // Timeline 4: Canvas Rocket lift-off (goes from centered 200dp down to -200dp soaring skyward)
    val liftOffDroneY = remember { Animatable(180f) }

    // Timeline 5: Custom Canvas dynamic particle explosion array (20 particles at 2.2s)
    val explosionProgress = remember { Animatable(0f) }
    val particles = remember {
        val r = Random(1234)
        List(20) {
            val angle = r.nextDouble() * 2.0 * Math.PI
            val speed = r.nextFloat() * 180f + 80f
            val radius = r.nextFloat() * 4f + 3f
            val color = if (r.nextBoolean()) SkyroColors.CyanGlow else SkyroColors.Sunrise
            ExplodingParticle(angle, speed, radius, color)
        }
    }

    // Direct timelines coordinate triggers
    LaunchedEffect(currentFrame) {
        when (currentFrame) {
            1 -> {
                // Sizzling progress bars
                kitchenPrepProgress.snapTo(0f)
                kitchenPrepProgress.animateTo(1.0f, animationSpec = tween(1200, easing = EaseOutQuad))
            }
            2 -> {
                // 1. Double rapid exposure cameras flash
                flashOpacity.snapTo(0f)
                flashOpacity.animateTo(0.8f, animationSpec = tween(150, easing = EaseInQuad))
                flashOpacity.animateTo(0f, animationSpec = tween(250, easing = EaseOutQuad))

                // 2. Camera Jitter shake ±4px
                for (cycle in 1..6) {
                    val signX = if (cycle % 2 == 0) 1f else -1f
                    val signY = if (cycle % 3 == 0) 1f else -1f
                    shakeX.animateTo(signX * 6f, animationSpec = tween(50, easing = LinearEasing))
                    shakeY.animateTo(signY * 6f, animationSpec = tween(50, easing = LinearEasing))
                }
                shakeX.animateTo(0f, animationSpec = tween(50))
                shakeY.animateTo(0f, animationSpec = tween(50))
            }
            3 -> {
                // Drone lift-off and particle burst
                liftOffDroneY.snapTo(120f)
                
                // Explode particles outward
                explosionProgress.snapTo(0f)
                
                // Parallel animate rocket launching rocket up and particles out
                launch {
                    liftOffDroneY.animateTo(-400f, animationSpec = tween(1250, easing = EaseInBack))
                }
                launch {
                    explosionProgress.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCirc))
                }
            }
        }
    }

    // Dynamic sky transitions sliding in from bottom
    val skySlideY by animateFloatAsState(
        targetValue = if (currentFrame >= 3) 0f else 1500f,
        animationSpec = tween(1000, easing = EaseInOutBack),
        label = "SkySlide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .offset { IntOffset(shakeX.value.dp.roundToPx(), shakeY.value.dp.roundToPx()) }
            .testTag("cinematic_modal_container")
    ) {
        // Sliding in Sky Gradient Background in later sequence
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, skySlideY.toInt()) }
                .background(
                    Brush.verticalGradient(
                        colors = if (isNight) {
                            listOf(SkyroColors.MidnightNav, SkyroColors.NightPurple, SkyroColors.NightGradientDeep)
                        } else {
                            listOf(SkyroColors.Sunrise, SkyroColors.Amber, SkyroColors.SkyBlue)
                        }
                    )
                )
        )

        // -------------------------------------------------------------
        // SEQUENCE INTERLUDES GRAPHICS
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Central Animated Dynamic Vector Visualizer Box
            Box(
                modifier = Modifier
                    .size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentFrame) {
                    1 -> {
                        // Phase 1 visual: Spinning sizzling plate / kitchen prep
                        ChefKitchenVisualizer()
                    }
                    2, 3 -> {
                        // Phase 2-3 visual: Launching prop drone & wind force lines
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(0, liftOffDroneY.value.dp.roundToPx()) }
                        ) {
                            DroneGraphicElement()
                            
                            // Prop-wash wind stream vectors lines
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawLine(Color.White.copy(alpha = 0.4f), start = c + Offset(-40f, 60f), end = c + Offset(-44f, 150f), strokeWidth = 2.dp.toPx())
                                drawLine(Color.White.copy(alpha = 0.4f), start = c + Offset(40f, 60f), end = c + Offset(44f, 150f), strokeWidth = 2.dp.toPx())
                                drawLine(Color.White.copy(alpha = 0.3f), start = c + Offset(-80f, 50f), end = c + Offset(-88f, 130f), strokeWidth = 1.5f.dp.toPx())
                                drawLine(Color.White.copy(alpha = 0.3f), start = c + Offset(80f, 50f), end = c + Offset(88f, 130f), strokeWidth = 1.5f.dp.toPx())
                            }
                        }

                        // Exploding particle circles painted on canvas centered
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val progress = explosionProgress.value
                            if (progress > 0f) {
                                for (p in particles) {
                                    val distance = p.speed * progress
                                    val currentX = center.x + (cos(p.angle) * distance).toFloat()
                                    val currentY = center.y + (sin(p.angle) * distance).toFloat() + 40f
                                    val fade = 1f - progress

                                    drawCircle(
                                        color = p.color.copy(alpha = fade),
                                        radius = p.radius,
                                        center = Offset(currentX, currentY)
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        // Shrinking flying dot flies away to corner visual
                        Text("🚀", fontSize = 18.sp, modifier = Modifier.scale(0.5f))
                    }
                }
            }

            // Phase Text Morph Messaging
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Cinematic Title (Syne display)
                Text(
                    text = when (currentFrame) {
                        1 -> "Preparing your order..."
                        2 -> "Drone Dispatched 🚁"
                        3 -> "SKYRO SYSTEM IGNITED"
                        else -> "Drone Flying to Campus..."
                    },
                    color = Color.White,
                    style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phase details sub-text
                Text(
                    text = when (currentFrame) {
                        1 -> "Chef is sealing your thermodynamic meal pods."
                        2 -> "Cargo bay locked. Pre-flight clearance complete."
                        3 -> "Takeoff! Ascending autonomous air-corridor lane."
                        else -> "Flight lane locked. ETA is 8 minutes."
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    style = SkyroTypography.Body,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Under progress bars (Phase 1 active)
                if (currentFrame == 1) {
                    LinearProgressIndicator(
                        progress = { kitchenPrepProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SkyroColors.Sunrise,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Full Screen Exposure White Flash Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(flashOpacity.value)
                .background(Color.White)
        )
    }
}

// Chef/Kitchen animated visualizer in Canvas
@Composable
fun ChefKitchenVisualizer() {
    val transition = rememberInfiniteTransition(label = "KitchenPrepLoop")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PlateSpin"
    )

    Box(
        modifier = Modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size / 2f
            
            // Spinning outer glowing dashes representing cooktop heat
            drawArc(
                color = SkyroColors.Amber.copy(alpha = 0.6f),
                startAngle = angle,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
            )

            // Dynamic Sizzling Plate
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.DarkGray, Color.Black)
                ),
                radius = 65.dp.toPx()
            )

            // Covered thermal dome server
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 45.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        Text("🍳", fontSize = 65.sp, textAlign = TextAlign.Center)
    }
}
