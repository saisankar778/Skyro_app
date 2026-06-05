package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

// Represents a star with normalized coordinates (0f to 1f), size, and phase offset
private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float
)

@Composable
fun CustomStarfield(modifier: Modifier = Modifier) {
    // Generate static metadata so stars don't move randomly on every recomposition
    val stars = remember {
        val r = Random(42) // Constant seed for stable star layout
        List(80) {
            Star(
                x = r.nextFloat(),
                y = r.nextFloat(),
                radius = r.nextFloat() * 2f + 1f, // 1 to 3 dp
                speed = r.nextFloat() * 1.5f + 0.5f, // speed modifier
                phase = r.nextFloat() * Math.PI.toFloat() * 2f // random animation offset
            )
        }
    }

    // Infinite pulse driver
    val infiniteTransition = rememberInfiniteTransition(label = "StarfieldTwinkle")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Math.PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StarfieldTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (star in stars) {
            // Compute animated opacity using the individual phase and speed
            val pulse = (Math.sin((time * star.speed + star.phase).toDouble()).toFloat() + 1f) / 2f
            // Range of opacity: 0.15 to 0.95
            val opacity = 0.15f + (pulse * 0.8f)

            drawCircle(
                color = Color.White.copy(alpha = opacity),
                radius = star.radius,
                center = Offset(star.x * width, star.y * height)
            )
        }
    }
}
