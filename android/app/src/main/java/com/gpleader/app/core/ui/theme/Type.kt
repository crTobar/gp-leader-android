package com.gpleader.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.gpleader.app.R

// ── Google Fonts provider ─────────────────────────────────────────────────────
// Uses the Compose ui-text-google-fonts library (Compose BOM 2024.09+).
// For production: add the GMS cert to res/values/font_certs.xml.
// Until then, falls back to system fonts — the app runs normally.
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

// ── Font definitions ─────────────────────────────────────────────────────────
private val CormorantGaramondGoogleFont = GoogleFont("Cormorant Garamond")
private val DMSansGoogleFont            = GoogleFont("DM Sans")
private val DMMonoGoogleFont            = GoogleFont("DM Mono")

// ── FontFamily objects ────────────────────────────────────────────────────────
val CormorantGaramond: FontFamily = FontFamily(
    Font(googleFont = CormorantGaramondGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = CormorantGaramondGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = CormorantGaramondGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = CormorantGaramondGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold, style = FontStyle.Italic),
)

val DMSans: FontFamily = FontFamily(
    Font(googleFont = DMSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = DMSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = DMSansGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
)

val DMMono: FontFamily = FontFamily(
    Font(googleFont = DMMonoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
)

// ── Typography scale ──────────────────────────────────────────────────────────
val GpTypography = Typography(
    // Títulos display — Cormorant Garamond serif
    displayLarge = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 40.sp,
        lineHeight = 48.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.SemiBold,
        fontStyle  = FontStyle.Italic,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    // UI labels — DM Sans sans-serif
    titleLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
    ),
    // Datos técnicos / mono — DM Mono
    labelSmall = TextStyle(
        fontFamily    = DMMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 2.sp,
    ),
)
