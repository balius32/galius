package com.balius.galius.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val GaliusDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
)

@Immutable
data class GaliusExtendedColors(
    val canvas: Color = CanvasBase,
    val card: Color = CardSurface,
    val elevated: Color = ElevatedSurface,
    val accentIndigo: Color = AccentIndigo,
    val accentCyan: Color = AccentCyan,
    val ghostBorder: Color = GhostBorder,
    val ghostBorderSubtle: Color = GhostBorderSubtle,
    val ghostBorderStrong: Color = GhostBorderStrong,
    val ghostFill: Color = GhostFill,
    val glassNav: Color = GlassNavBackground,
    val searchField: Color = SearchFieldBackground,
    val metadataDescription: Color = MetadataDescription,
    val metadataCaption: Color = MetadataCaption,
    val focusHalo: Color = FocusHalo,
    val tagMint: Color = TagMint,
    val tagMintContainer: Color = TagMintContainer,
    val tagAmber: Color = TagAmber,
    val tagAmberContainer: Color = TagAmberContainer,
    val tagLavender: Color = TagLavender,
    val tagLavenderContainer: Color = TagLavenderContainer,
    val tagSky: Color = TagSky,
    val tagSkyContainer: Color = TagSkyContainer,
    val tagRose: Color = TagRose,
    val tagRoseContainer: Color = TagRoseContainer,
    val tagNeutral: Color = TagNeutral,
    val tagNeutralContainer: Color = TagNeutralContainer,
    val mediaScrim: Brush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, ScrimBottom),
    ),
)

val LocalGaliusColors = staticCompositionLocalOf { GaliusExtendedColors() }
val LocalGaliusTypography = staticCompositionLocalOf { GaliusExtendedTypography }

object GaliusThemeTokens {
    val colors: GaliusExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGaliusColors.current

    val typography: GaliusTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGaliusTypography.current

    val spacing: GaliusSpacing
        @Composable
        @ReadOnlyComposable
        get() = GaliusSpacing
}

/**
 * Gallius dark media catalog theme.
 * Brand is dark-only; dynamic/system light schemes are disabled to protect tokens.
 */
@Composable
fun GaliusTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalGaliusColors provides GaliusExtendedColors(),
        LocalGaliusTypography provides GaliusExtendedTypography,
    ) {
        MaterialTheme(
            colorScheme = GaliusDarkColorScheme,
            typography = Typography,
            shapes = GaliusShapes,
            content = content,
        )
    }
}
