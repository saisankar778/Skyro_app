package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SkyroColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isNight: Boolean = false,
    appThemeMode: String? = null,
    borderRadius: Dp = 20.dp,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val activeMode = appThemeMode ?: if (isNight) "NIGHT" else "SKYRO_PRESENT"

    val isGlassDark = activeMode == "NIGHT"

    val backgroundColor = if (isGlassDark) {
        SkyroColors.getThemeCardBg(activeMode)
    } else {
        Color.White
    }

    val borderColor = if (isGlassDark) {
        SkyroColors.GlassBorderDark
    } else {
        Color.LightGray.copy(alpha = 0.3f)
    }

    val shadowColor = if (isGlassDark) {
        SkyroColors.CyanGlow
    } else {
        Color.LightGray
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(borderRadius),
                clip = false,
                ambientColor = shadowColor.copy(alpha = if (isGlassDark) 0.2f else 0.3f),
                spotColor = shadowColor.copy(alpha = if (isGlassDark) 0.35f else 0.4f)
            )
            .clip(RoundedCornerShape(borderRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isGlassDark) {
                        listOf(backgroundColor, backgroundColor.copy(alpha = 0.45f))
                    } else {
                        listOf(Color.White, Color.White)
                    }
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(borderRadius)
            )
            .padding(16.dp),
        content = content
    )
}
