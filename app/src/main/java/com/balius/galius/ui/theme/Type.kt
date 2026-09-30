package com.balius.galius.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Gallius typeface.
 *
 * Design specifies Inter. Until Inter files are added under `res/font`,
 * we use [FontFamily.SansSerif] (closest system match on Android).
 * Drop Inter weights into `res/font` and wire [FontFamily] here when ready.
 */
val GalliusFontFamily: FontFamily = FontFamily.SansSerif

@Immutable
data class GaliusTypography(
    val displayHero: TextStyle,
    val headlineLg: TextStyle,
    val headlineLgMobile: TextStyle,
    val headlineMd: TextStyle,
    val headlineSm: TextStyle,
    val bodyLg: TextStyle,
    val bodyMd: TextStyle,
    val bodySm: TextStyle,
    val labelNumeric: TextStyle,
    val labelPill: TextStyle,
)

val GaliusExtendedTypography = GaliusTypography(
    displayHero = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.68).sp, // -0.035em
    ),
    headlineLg = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.8).sp, // -0.025em
    ),
    headlineLgMobile = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.52).sp, // -0.02em
    ),
    headlineMd = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.44).sp, // -0.02em
    ),
    headlineSm = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.27).sp, // -0.015em
    ),
    bodyLg = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.16).sp, // -0.01em
    ),
    bodyMd = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.07).sp, // -0.005em
    ),
    bodySm = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelNumeric = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.24.sp, // 0.02em
        // Prefer tabular figures when Inter is bundled:
        // fontFeatureSettings = "tnum, cv05, cv11"
    ),
    labelPill = TextStyle(
        fontFamily = GalliusFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.33.sp, // 0.03em
    ),
)

/** Material3 typography mapped from Gallius tokens. */
val Typography = Typography(
    displayLarge = GaliusExtendedTypography.displayHero,
    displayMedium = GaliusExtendedTypography.headlineLg,
    displaySmall = GaliusExtendedTypography.headlineLgMobile,
    headlineLarge = GaliusExtendedTypography.headlineLg,
    headlineMedium = GaliusExtendedTypography.headlineMd,
    headlineSmall = GaliusExtendedTypography.headlineSm,
    titleLarge = GaliusExtendedTypography.headlineMd,
    titleMedium = GaliusExtendedTypography.headlineSm,
    titleSmall = GaliusExtendedTypography.bodyMd.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = GaliusExtendedTypography.bodyLg,
    bodyMedium = GaliusExtendedTypography.bodyMd,
    bodySmall = GaliusExtendedTypography.bodySm,
    labelLarge = GaliusExtendedTypography.bodyMd.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = GaliusExtendedTypography.labelNumeric,
    labelSmall = GaliusExtendedTypography.labelPill,
)
