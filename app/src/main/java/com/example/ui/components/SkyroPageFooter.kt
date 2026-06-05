package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SkyroPageFooter(
    isNight: Boolean,
    modifier: Modifier = Modifier,
    showExtended: Boolean = false
) {
    if (!showExtended) {
        Spacer(modifier = Modifier.height(16.dp))
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 56.dp, start = 28.dp, end = 28.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sky's\nthe start",
            color = if (isNight) Color.White.copy(alpha = 0.35f) else Color(0xFF888888),
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 66.sp,
            letterSpacing = (-2).sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Build with ",
                color = if (isNight) Color.White.copy(alpha = 0.5f) else Color(0xFF888888),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
            Text(
                text = "♥︎",
                color = Color(0xFFEF4444),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )
            Text(
                text = " in SRM University, AP",
                color = if (isNight) Color.White.copy(alpha = 0.5f) else Color(0xFF888888),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        }
    }
}
