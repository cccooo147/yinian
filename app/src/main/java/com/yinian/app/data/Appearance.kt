package com.yinian.app.data

import androidx.compose.runtime.Immutable

const val DEFAULT_HEADLINE = "把想法，\n做成一件事。"

enum class PaletteId {
    PAPER,
    MINERAL,
    MOON,
    PINE,
    LOTUS,
    TEA,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

@Immutable
data class Appearance(
    val palette: PaletteId = PaletteId.PAPER,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val headline: String = DEFAULT_HEADLINE,
    val homeImagePath: String? = null,
    /** Labels the user invented with "自设状态", offered again next time. */
    val customStatuses: List<String> = emptyList(),
)
