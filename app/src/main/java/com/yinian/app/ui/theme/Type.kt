package com.yinian.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private fun clean(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

val YinianTypography = Typography(
    displayLarge = clean(Serif, FontWeight.SemiBold, 46, 56, -1.2f),
    displayMedium = clean(Serif, FontWeight.SemiBold, 38, 48, -0.9f),
    displaySmall = clean(Serif, FontWeight.SemiBold, 32, 42, -0.6f),
    headlineLarge = clean(Serif, FontWeight.SemiBold, 30, 40, -0.4f),
    headlineMedium = clean(Serif, FontWeight.SemiBold, 26, 35, -0.3f),
    headlineSmall = clean(Serif, FontWeight.SemiBold, 22, 30, -0.2f),
    titleLarge = clean(Sans, FontWeight.SemiBold, 20, 28, 0f),
    titleMedium = clean(Sans, FontWeight.Medium, 16, 24, 0f),
    titleSmall = clean(Sans, FontWeight.Medium, 14, 20, 0.1f),
    bodyLarge = clean(Sans, FontWeight.Normal, 16, 26, 0.1f),
    bodyMedium = clean(Sans, FontWeight.Normal, 14, 22, 0.1f),
    bodySmall = clean(Sans, FontWeight.Normal, 12, 18, 0.15f),
    labelLarge = clean(Sans, FontWeight.Medium, 14, 20, 0.2f),
    labelMedium = clean(Sans, FontWeight.Medium, 12, 16, 0.4f),
    labelSmall = clean(Sans, FontWeight.Medium, 10, 14, 1.1f),
)
