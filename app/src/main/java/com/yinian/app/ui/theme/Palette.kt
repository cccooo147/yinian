package com.yinian.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.yinian.app.data.CardTint
import com.yinian.app.data.PaletteId
import com.yinian.app.data.StepStatus

/** The "当前进度" pill keeps the same vermilion in every palette, like the prototype. */
val CurrentPill = Color(0xFFCD5238)
val CurrentPillContent = Color(0xFFFFF6F2)

@Immutable
data class AppColors(
    val dark: Boolean,
    val paper: Color,
    val paperDeep: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val hairline: Color,
    val accent: Color,
    val accentSoft: Color,
    val motif: Color,
    val sage: Color,
    val blue: Color,
    val rose: Color,
    val sand: Color,
) {
    fun tint(tint: CardTint): Color = when (tint) {
        CardTint.SAGE -> sage
        CardTint.BLUE -> blue
        CardTint.ROSE -> rose
        CardTint.SAND -> sand
    }
}

@Immutable
data class Palette(
    val id: PaletteId,
    val name: String,
    val light: AppColors,
    val dark: AppColors,
    val swatches: List<Color>,
)

@Immutable
data class StatusTone(
    val background: Color,
    val content: Color,
    val dot: Color,
)

@Immutable
data class StatusPalette(
    val thinking: StatusTone,
    val doing: StatusTone,
    val waiting: StatusTone,
    val done: StatusTone,
    val failed: StatusTone,
) {
    fun of(status: StepStatus): StatusTone = when (status) {
        StepStatus.THINKING -> thinking
        StepStatus.DOING -> doing
        StepStatus.WAITING -> waiting
        StepStatus.DONE -> done
        StepStatus.FAILED -> failed
    }
}

object Palettes {

    private fun colors(
        dark: Boolean,
        paper: Long,
        deep: Long,
        surface: Long,
        ink: Long,
        muted: Long,
        hairline: Long,
        accent: Long,
        accentSoft: Long,
        motif: Long,
        sage: Long,
        blue: Long,
        rose: Long,
        sand: Long,
    ) = AppColors(
        dark = dark,
        paper = Color(paper),
        paperDeep = Color(deep),
        surface = Color(surface),
        ink = Color(ink),
        muted = Color(muted),
        hairline = Color(hairline),
        accent = Color(accent),
        accentSoft = Color(accentSoft),
        motif = Color(motif),
        sage = Color(sage),
        blue = Color(blue),
        rose = Color(rose),
        sand = Color(sand),
    )

    val all: List<Palette> = listOf(
        Palette(
            id = PaletteId.PAPER,
            name = "宣纸",
            swatches = listOf(Color(0xFFF5F0E6), Color(0xFFCD5238), Color(0xFFE9F0E3)),
            light = colors(
                false, 0xFFF5F0E6, 0xFFEDE5D7, 0xFFFFFBF4, 0xFF1D1A16, 0xFF81786A,
                0xFFDED3C2, 0xFFCD5238, 0xFFF1D8CF, 0xFFE7D8CB,
                0xFFE9F0E3, 0xFFE4EDF2, 0xFFF6E7E1, 0xFFF2E9DC,
            ),
            dark = colors(
                true, 0xFF131210, 0xFF0D0C0A, 0xFF1D1B18, 0xFFF1EAD9, 0xFFA89D8C,
                0xFF332F29, 0xFFE8846B, 0xFF3A241E, 0xFF2A2520,
                0xFF20261D, 0xFF1B2229, 0xFF2A1F1B, 0xFF26221A,
            ),
        ),
        Palette(
            id = PaletteId.MINERAL,
            name = "丹青",
            swatches = listOf(Color(0xFFEAF0EE), Color(0xFFB85C45), Color(0xFFE2EDE6)),
            light = colors(
                false, 0xFFEAF0EE, 0xFFDEE8E4, 0xFFF8FBF9, 0xFF1B2925, 0xFF64756E,
                0xFFCBD9D3, 0xFFB85C45, 0xFFEEDAD2, 0xFFD5E4DF,
                0xFFE2EDE6, 0xFFDFEAEF, 0xFFF2E3DE, 0xFFECE8DA,
            ),
            dark = colors(
                true, 0xFF101614, 0xFF0B100E, 0xFF1A211E, 0xFFE7F0EB, 0xFF93A39B,
                0xFF2B3631, 0xFFE08A6E, 0xFF3A2620, 0xFF1E2A25,
                0xFF1C2721, 0xFF19242A, 0xFF2A201C, 0xFF24231B,
            ),
        ),
        Palette(
            id = PaletteId.MOON,
            name = "月白",
            swatches = listOf(Color(0xFFEEF2F6), Color(0xFF5B7C9B), Color(0xFFDFEAF4)),
            light = colors(
                false, 0xFFEEF2F6, 0xFFE1E8EF, 0xFFF9FBFD, 0xFF18212B, 0xFF66717C,
                0xFFD2DCE5, 0xFF5B7C9B, 0xFFDCE8F1, 0xFFDCE7F0,
                0xFFE3ECEA, 0xFFDFEAF4, 0xFFF1E3E8, 0xFFECE8DF,
            ),
            dark = colors(
                true, 0xFF10151B, 0xFF0B1015, 0xFF1A2028, 0xFFE8EFF7, 0xFF95A2B0,
                0xFF2A3440, 0xFF8DB3D1, 0xFF223444, 0xFF1D2833,
                0xFF1A2623, 0xFF1A2734, 0xFF292029, 0xFF24231E,
            ),
        ),
        Palette(
            id = PaletteId.PINE,
            name = "松烟",
            swatches = listOf(Color(0xFFE9EEE6), Color(0xFF5E7B55), Color(0xFFE3ECDD)),
            light = colors(
                false, 0xFFE9EEE6, 0xFFDCE4D9, 0xFFF7FAF5, 0xFF1E2A1F, 0xFF667367,
                0xFFCCD8C8, 0xFF5E7B55, 0xFFDDE8D8, 0xFFD7E3D2,
                0xFFE3ECDD, 0xFFE0E9E4, 0xFFF1E4E0, 0xFFEBE7D8,
            ),
            dark = colors(
                true, 0xFF10150F, 0xFF0B100A, 0xFF1A2117, 0xFFE8F0DF, 0xFF97A38E,
                0xFF2C3827, 0xFF9BB786, 0xFF283825, 0xFF1F2A1C,
                0xFF1D271A, 0xFF1B2522, 0xFF29211D, 0xFF242219,
            ),
        ),
        Palette(
            id = PaletteId.LOTUS,
            name = "藕荷",
            swatches = listOf(Color(0xFFF2EBEE), Color(0xFF9A5F73), Color(0xFFF3E0E7)),
            light = colors(
                false, 0xFFF2EBEE, 0xFFE8DDE2, 0xFFFBF7F9, 0xFF2A2027, 0xFF7A6A73,
                0xFFDCCDD4, 0xFF9A5F73, 0xFFEAD7DF, 0xFFE8D9E0,
                0xFFE8ECE4, 0xFFE3EAF0, 0xFFF3E0E7, 0xFFEEE6DA,
            ),
            dark = colors(
                true, 0xFF171116, 0xFF100B0F, 0xFF211A20, 0xFFF2E7EE, 0xFFB09EA8,
                0xFF3A2C35, 0xFFD79BB2, 0xFF3A2530, 0xFF2B2028,
                0xFF20251F, 0xFF1D252B, 0xFF2C1D25, 0xFF282219,
            ),
        ),
        Palette(
            id = PaletteId.TEA,
            name = "茶烟",
            swatches = listOf(Color(0xFFEFEAE1), Color(0xFF8A6A45), Color(0xFFEFE4D2)),
            light = colors(
                false, 0xFFEFEAE1, 0xFFE5DDD0, 0xFFFAF7F0, 0xFF28221A, 0xFF7B6E5B,
                0xFFD8CCBA, 0xFF8A6A45, 0xFFE7DCCB, 0xFFE2D5C2,
                0xFFE9EBDD, 0xFFE2E8E9, 0xFFF1E1D9, 0xFFEFE4D2,
            ),
            dark = colors(
                true, 0xFF16130F, 0xFF100D0A, 0xFF211D17, 0xFFF2E9DA, 0xFFB2A38D,
                0xFF3A3227, 0xFFD5A66D, 0xFF3A2C1E, 0xFF2B241B,
                0xFF20241B, 0xFF1C2424, 0xFF2C211B, 0xFF2A2418,
            ),
        ),
    )

    fun of(id: PaletteId): Palette = all.firstOrNull { it.id == id } ?: all.first()
}

fun statusPalette(dark: Boolean): StatusPalette = if (dark) {
    StatusPalette(
        thinking = StatusTone(Color(0xFF2B2822), Color(0xFFB8AD9C), Color(0xFFB8AD9C)),
        doing = StatusTone(Color(0xFF1C2A34), Color(0xFF9CC3DE), Color(0xFF9CC3DE)),
        waiting = StatusTone(Color(0xFF3A2E17), Color(0xFFE2B563), Color(0xFFE2B563)),
        done = StatusTone(Color(0xFF1F3528), Color(0xFFA2D2B4), Color(0xFFA2D2B4)),
        failed = StatusTone(Color(0xFF3B211C), Color(0xFFF0A798), Color(0xFFF0A798)),
    )
} else {
    StatusPalette(
        thinking = StatusTone(Color(0xFFF2EEE6), Color(0xFF7A7265), Color(0xFF7A7265)),
        doing = StatusTone(Color(0xFFE4EDF4), Color(0xFF3E6480), Color(0xFF3E6480)),
        waiting = StatusTone(Color(0xFFF8EBCF), Color(0xFFA9711E), Color(0xFFA9711E)),
        done = StatusTone(Color(0xFFE2F0E4), Color(0xFF4F7C5B), Color(0xFF4F7C5B)),
        failed = StatusTone(Color(0xFFF8E1DD), Color(0xFFB44A3A), Color(0xFFB44A3A)),
    )
}
