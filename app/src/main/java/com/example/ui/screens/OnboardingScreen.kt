package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val title: String,
    val description: String,
    val illustrationEmoji: String,
    val illustrationAccent: Color
)

@Composable
fun OnboardingScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val slides = listOf(
        OnboardingSlide(
            title = "Order from Anywhere\non Campus",
            description = "Get hot meals delivered directly to your hostel, classroom, library or sports grounds by autonomous drone flight lanes.",
            illustrationEmoji = "🏫",
            illustrationAccent = SkyroColors.Amber
        ),
        OnboardingSlide(
            title = "Track Your Order\nin Real-Time",
            description = "Experience visual aerial GPS mapping with exact vector paths from the campus hub directly to your designated drop point.",
            illustrationEmoji = "🛰️",
            illustrationAccent = SkyroColors.CyanGlow
        ),
        OnboardingSlide(
            title = "Delivered in Under\n10 Minutes",
            description = "Skip the queues. Your fresh warm food is locked in a lightweight thermodynamic payload pod & dropped off safely.",
            illustrationEmoji = "🚁",
            illustrationAccent = SkyroColors.DeepCoral
        )
    )

    val backgroundBrush = if (isNight) {
        Brush.linearGradient(
            colors = listOf(SkyroColors.MidnightNav, SkyroColors.NightPurple, SkyroColors.NightGradientDeep)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(SkyroColors.Sunrise, SkyroColors.Amber, SkyroColors.SkyBlue)
        )
    }

    val themeAccentColor = if (isNight) SkyroColors.CyanGlow else SkyroColors.Sunrise

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Skip Button top right
        if (pagerState.currentPage < 2) {
            TextButton(
                onClick = onFinished,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "SKIP",
                    color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Horizontal Slide Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 120.dp)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Cinematic Interactive Illustration Card
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = slide.illustrationAccent.copy(alpha = 0.2f),
                            spotColor = slide.illustrationAccent.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    slide.illustrationAccent.copy(alpha = 0.3f),
                                    slide.illustrationAccent.copy(alpha = 0.02f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Moving graphic elements based on onboarding theme
                    if (page == 1) {
                        // Drawing path lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = slide.illustrationAccent.copy(alpha = 0.4f),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                            )
                        }
                    }

                    Text(
                        text = slide.illustrationEmoji,
                        fontSize = 90.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Heading with display typography Syne style
                Text(
                    text = slide.title,
                    color = if (isNight) Color.White else Color.Black,
                    style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle body text
                Text(
                    text = slide.description,
                    color = if (isNight) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    style = SkyroTypography.Body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Bottom section containing Dot Indicators & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dots indicator (morph size: 8dp -> 24dp active)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 3) {
                    val isActive = pagerState.currentPage == i
                    val width by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "DotWidthAnimate"
                    )
                    
                    val dotColor = if (isActive) themeAccentColor else Color.White.copy(alpha = 0.3f)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(dotColor)
                    )
                }
            }

            // Get Started or Next Action button
            if (pagerState.currentPage == 2) {
                Button(
                    onClick = onFinished,
                    modifier = Modifier
                        .height(50.dp)
                        .width(160.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = themeAccentColor.copy(alpha = 0.3f),
                            spotColor = themeAccentColor.copy(alpha = 0.5f)
                        )
                        .testTag("get_started_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SkyroColors.Sunrise, SkyroColors.DeepCoral)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.White,
                            style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .width(110.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor)
                ) {
                    Text(
                        text = "Next",
                        color = if (isNight) SkyroColors.MidnightNav else Color.White,
                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
