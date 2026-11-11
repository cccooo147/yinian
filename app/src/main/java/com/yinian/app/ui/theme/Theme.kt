package com.yinian.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yinian.app.data.Appearance
import com.yinian.app.data.ThemeMode

val LocalAppColors = staticCompositionLocalOf { Palettes.all.first().light }
val LocalStatusPalette = staticCompositionLocalOf { statusPalette(false) }
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun YinianTheme(
    appearance: Appearance,
    systemDark: Boolean,
    content: @Composable () -> Unit,
) {
    val dark = when (appearance.mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = Palettes.of(appearance.palette)
    val colors = if (dark) palette.dark else palette.light
    val status = statusPalette(dark)
    val scheme = if (dark) darkColorScheme(
        primary = colors.accent,
        onPrimary = Color(0xFF1A0E0A),
        primaryContainer = colors.accentSoft,
        onPrimaryContainer = colors.ink,
        secondary = colors.motif,
        onSecondary = colors.ink,
        background = colors.paper,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.paperDeep,
        onSurfaceVariant = colors.muted,
        outline = colors.hairline,
        outlineVariant = colors.hairline.copy(alpha = 0.6f),
        inverseSurface = colors.ink,
        inverseOnSurface = colors.paper,
        scrim = Color.Black,
    ) else lightColorScheme(
        primary = colors.accent,
        onPrimary = Color.White,
        primaryContainer = colors.accentSoft,
        onPrimaryContainer = colors.ink,
        secondary = colors.motif,
        onSecondary = colors.ink,
        background = colors.paper,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.paperDeep,
        onSurfaceVariant = colors.muted,
        outline = colors.hairline,
        outlineVariant = colors.hairline.copy(alpha = 0.6f),
        inverseSurface = colors.ink,
        inverseOnSurface = colors.paper,
        scrim = Color.Black,
    )

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalStatusPalette provides status,
        LocalDarkTheme provides dark,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = YinianTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(9.dp),
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(22.dp),
                large = RoundedCornerShape(32.dp),
                extraLarge = RoundedCornerShape(38.dp),
            ),
            content = content,
        )
    }
}
