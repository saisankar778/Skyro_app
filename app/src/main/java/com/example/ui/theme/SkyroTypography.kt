package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

object SkyroTypography {
    val fontProvider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    val poppinsFont = GoogleFont("Poppins")

    val PoppinsFontFamily = FontFamily(
        Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Normal),
        Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Medium),
        Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Bold),
        Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
    )

    val Display = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = 1.sp
    )

    val H1 = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.5.sp
    )

    val H2 = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp
    )

    val Body = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    )

    val PriceMono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp
    )

    val Caption = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    )
}
