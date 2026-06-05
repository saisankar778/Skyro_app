package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography

@Composable
fun DroneEtaBadge(
    modifier: Modifier = Modifier,
    etaMinutes: Int,
    isNight: Boolean = false
) {
    val backgroundColor = if (isNight) {
        SkyroColors.CyanGlow.copy(alpha = 0.15f)
    } else {
        SkyroColors.Amber.copy(alpha = 0.25f)
    }

    val contentColor = if (isNight) {
        SkyroColors.CyanGlow
    } else {
        SkyroColors.Sunrise
    }

    val borderColor = if (isNight) {
        SkyroColors.CyanGlow.copy(alpha = 0.4f)
    } else {
        SkyroColors.Amber.copy(alpha = 0.6f)
    }

    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🚁",
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$etaMinutes min",
            color = contentColor,
            style = SkyroTypography.Caption.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = SkyroTypography.PriceMono.fontFamily
            )
        )
    }
}
