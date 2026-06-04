package com.gpleader.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gpleader.app.R

// ── Fuentes variables locales (res/font/) ─────────────────────────────────────

@OptIn(ExperimentalTextApi::class)
val CormorantGaramond: FontFamily = FontFamily(
    Font(resId = R.font.cormorant_garamond, weight = FontWeight.Normal,   style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resId = R.font.cormorant_garamond, weight = FontWeight.Medium,   style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(resId = R.font.cormorant_garamond, weight = FontWeight.SemiBold, style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resId = R.font.cormorant_garamond, weight = FontWeight.Bold,     style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(resId = R.font.cormorant_garamond_italic, weight = FontWeight.Normal,   style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resId = R.font.cormorant_garamond_italic, weight = FontWeight.Medium,   style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(resId = R.font.cormorant_garamond_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resId = R.font.cormorant_garamond_italic, weight = FontWeight.Bold,     style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

@OptIn(ExperimentalTextApi::class)
val DMSans: FontFamily = FontFamily(
    Font(resId = R.font.dm_sans, weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resId = R.font.dm_sans, weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(resId = R.font.dm_sans, weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resId = R.font.dm_sans, weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val DMMono: FontFamily = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium,  FontWeight.Medium),
)

// ── Typography scale ──────────────────────────────────────────────────────────
val GpTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 40.sp,
        lineHeight = 48.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
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
    labelSmall = TextStyle(
        fontFamily    = DMMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 2.sp,
    ),
)
