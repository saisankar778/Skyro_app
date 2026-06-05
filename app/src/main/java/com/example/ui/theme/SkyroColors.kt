package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object SkyroColors {
    val Sunrise = Color(0xFFFF6B35)     // Burnt Orange
    val Amber = Color(0xFFFFB347)       // Amber
    val GoldenHour = Color(0xFFFF9A3C)  // Golden Hour
    val DeepCoral = Color(0xFFFF4E50)   // Deep Coral
    val SkyBlue = Color(0xFF87CEEB)     // Sky Blue
    val MidnightNav = Color(0xFF0A0A1A) // Midnight Nav
    val NightPurple = Color(0xFF1A1A2E) // Night Purple
    val NightGradientDeep = Color(0xFF0D1B4B) // Dark purple-blue depth
    val CyanGlow = Color(0xFF00D4FF)    // Cyan Glow
    val VioletPulse = Color(0xFF7B2FFF) // Violet Pulse
    val StarWhite = Color(0xFFF0F8FF)   // Star White
    val WarmCream = Color(0xFFFFF8F0)   // Warm Cream
    
    // UI Helpers
    val GlassWhite = Color(0x26FFFFFF)  // Colors.white.withOpacity(0.15)
    val GlassDark = Color(0xD91A1A2E)   // #1A1A2E opacity 0.85
    val GlassBorderLight = Color(0x40FFFFFF)
    val GlassBorderDark = Color(0x6600D4FF) // CyanGlow 0.4 opacity

    fun getThemeBackgroundBrush(mode: String): Brush {
        return when (mode) {
            "SUNNY" -> {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFAED), // Very soft sunny warm gold top
                        Color(0xFFFFEBD6), // Warm apricot blend
                        Color(0xFFFFF0E1)  // Peachy cream base
                    )
                )
            }
            "NIGHT" -> {
                Brush.linearGradient(
                    colors = listOf(
                        MidnightNav,
                        NightPurple,
                        NightGradientDeep
                    )
                )
            }
            else -> { // "SKYRO_PRESENT"
                // Use the present home theme signature gradient (vibrant orange to teal gradient)
                Brush.linearGradient(
                    colors = listOf(
                        Sunrise,
                        Amber,
                        SkyBlue
                    )
                )
            }
        }
    }

    fun getThemeCardBg(mode: String): Color {
        return when (mode) {
            "NIGHT" -> GlassDark                      // Beautiful glowing glass dark
            else -> Color.White                       // Solid pristine white for SUNNY and SKYRO_PRESENT
        }
    }

    fun getThemeBorderBrush(mode: String): Brush {
        return when (mode) {
            "SUNNY" -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)))
            "NIGHT" -> Brush.verticalGradient(listOf(CyanGlow.copy(alpha = 0.35f), Color.Transparent))
            else -> Brush.verticalGradient(listOf(Color.LightGray.copy(alpha = 0.12f), Color.LightGray.copy(alpha = 0.05f)))
        }
    }

    fun getThemeTextColor(mode: String): Color {
        return when (mode) {
            "NIGHT" -> Color.White
            "SUNNY" -> Color(0xFF452712) // Rich hot amber coffee color for maximum contrast and sun warmth
            else -> Color(0xFF1E293B)   // Jet black/slate dark gray
        }
    }

    fun getThemeSecondaryTextColor(mode: String): Color {
        return when (mode) {
            "NIGHT" -> Color.White.copy(alpha = 0.6f)
            "SUNNY" -> Color(0xFF7A4826) // Softened milk chocolate brown
            else -> Color.Gray
        }
    }
}
