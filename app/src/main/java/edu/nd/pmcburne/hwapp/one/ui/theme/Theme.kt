package edu.nd.pmcburne.hwapp.one.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// for the required theme of the app
private val RhythmicColorPaletteDark = darkColorScheme(
    primary = RhythmicRed,
    onPrimary = RhythmicWhite,
    secondary = RhythmicDrkGrey,
    onSecondary = RhythmicWhite,
    tertiary = RhythmicRed2,
    background = RhythmicBlackB,
    onBackground = RhythmicWhite,
    surface = RhythmicBlackC,
    onSurface = RhythmicWhite,
    surfaceVariant = RhythmicDrkGrey,
    onSurfaceVariant = RhythmicWhiteB,
    outline = RhythmicGrey
)

private val RhythmicColorPaletteLight = lightColorScheme(
    primary = RhythmicRed,
    onPrimary = RhythmicWhite,
    secondary = RhythmicDrkGrey,
    onSecondary = RhythmicWhite,
    tertiary = RhythmicRed2,
    background = RhythmicBlackB,
    onBackground = RhythmicWhite,
    surface = RhythmicBlackC,
    onSurface = RhythmicWhite,
    surfaceVariant = RhythmicDrkGrey,
    onSurfaceVariant = RhythmicWhiteB,
    outline = RhythmicGrey
)

@Composable
fun RhythmicStartingTheme(
    rhythmicDark: Boolean = true,
    rhythmicColour: Boolean = false,
    rhythmicContent: @Composable () -> Unit
) {
    val rhythmicPalette = if (rhythmicDark) RhythmicColorPaletteDark else RhythmicColorPaletteLight

    MaterialTheme(
        colorScheme = rhythmicPalette,
        typography = RhythmicGraphy,
        content = rhythmicContent
    )
}