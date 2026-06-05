package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AnimatedHeartButton(
    isFavorited: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    testTag: String = "heart_button",
    iconColor: Color? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .scale(scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Custom bouncy scale feedback
                onClick = {
                    onClick()
                    coroutineScope.launch {
                        scale.animateTo(1.4f, animationSpec = tween(90, easing = FastOutSlowInEasing))
                        scale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isFavorited) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorited",
                tint = Color(0xFFEF4444), // Vibrant Red
                modifier = Modifier.size(size)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Not Favorited",
                tint = iconColor ?: Color.Gray.copy(alpha = 0.8f),
                modifier = Modifier.size(size)
            )
        }
    }
}
