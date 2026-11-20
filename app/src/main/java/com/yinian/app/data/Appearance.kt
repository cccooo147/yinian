package com.yinian.app.data

import androidx.compose.runtime.Immutable

const val DEFAULT_HEADLINE = "把想法，\n做成一件事。"
const val DEFAULT_SLOGAN = "想法不用完整，先记下来。"

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

/** How much air sits between the headline block and the first card. */
enum class TopGap(val dpAmount: Int) {
    SMALL(40),
    MEDIUM(300),
    LARGE(420),
}

/** How light the see-through strip under a card's cover is. */
enum class CardVeil(val percent: Int) {
    LIGHT(6),
    SOFT(12),
    MEDIUM(20),
    DEEP(30),
}

/** Blur radius the header image falls back to while the list scrolls. */
enum class BlurLevel(val radius: Int) {
    LIGHT(10),
    MEDIUM(18),
    HEAVY(28),
    MAX(40),
}

/** How far the header image is blurred before the list is scrolled at all. */
enum class InitialBlur(val amount: Float) {
    OFF(0f),
    LIGHT(0.35f),
    FULL(1f),
}

/** How strongly the card's title and step line are shadowed. */
enum class TextShadowLevel(val blur: Float, val alpha: Float) {
    NONE(0f, 0f),
    SOFT(14f, 0.30f),
    STRONG(26f, 0.55f),
}

/** How far the list scrolls before the header image is fully blurred. */
enum class BlurDistance(val dpAmount: Int) {
    SHORT(60),
    MEDIUM(120),
    LONG(220),
}

@Immutable
data class Appearance(
    val palette: PaletteId = PaletteId.PAPER,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val headline: String = DEFAULT_HEADLINE,
    /** Empty string hides the line under the headline. */
    val slogan: String = DEFAULT_SLOGAN,
    val showStats: Boolean = true,
    val homeImagePath: String? = null,
    /** Labels the user invented with "自设状态", offered again next time. */
    val customStatuses: List<String> = emptyList(),
    // Card & header feel. Defaults are the values the design was tuned with.
    val topGap: TopGap = TopGap.MEDIUM,
    val cardVeil: CardVeil = CardVeil.SOFT,
    val cardGlass: Boolean = true,
    val textShadow: TextShadowLevel = TextShadowLevel.SOFT,
    val heroBlur: Boolean = true,
    val initialBlur: InitialBlur = InitialBlur.OFF,
    val blurLevel: BlurLevel = BlurLevel.MEDIUM,
    val blurDistance: BlurDistance = BlurDistance.MEDIUM,
)
