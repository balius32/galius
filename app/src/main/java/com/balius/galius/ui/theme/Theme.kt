package com.balius.galius.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.balius.galius.common.model.AccentOption

private val GaliusDarkBaseScheme = darkColorScheme(
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

private val GaliusLightBaseScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerLowest = LightSurfaceContainerLowest,
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
    val storagePhoto: Color = AccentIndigo,
    val storageVideo: Color = AccentCyan,
    val storageDb: Color = TagSky,
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

data class AccentPalette(
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val storagePhoto: Color,
    val storageVideo: Color,
    val storageDb: Color,
    val accent: Color,
    val focusHalo: Color,
    val swatch: Color,
)

fun accentPalette(option: AccentOption): AccentPalette = when (option) {
    AccentOption.ElectricIndigo -> AccentPalette(
        primaryContainer = Color(0xFF5856D6),
        onPrimaryContainer = Color(0xFFE7E4FF),
        primary = Color(0xFFC2C1FF),
        onPrimary = Color(0xFF1C0B9F),
        primaryLight = Color(0xFF4F4CCD),
        onPrimaryLight = Color(0xFFFFFFFF),
        storagePhoto = Color(0xFF5856D6),
        storageVideo = Color(0xFF00C7BE),
        storageDb = Color(0xFF64D2FF),
        accent = Color(0xFF5856D6),
        focusHalo = Color(0x735856D6),
        swatch = Color(0xFF5856D6),
    )
    AccentOption.ElectricCyan -> AccentPalette(
        primaryContainer = Color(0xFF00C7BE),
        onPrimaryContainer = Color(0xFF003734),
        primary = Color(0xFF61F9EF),
        onPrimary = Color(0xFF003734),
        primaryLight = Color(0xFF006A65),
        onPrimaryLight = Color(0xFFFFFFFF),
        storagePhoto = Color(0xFF00C7BE),
        storageVideo = Color(0xFF5856D6),
        storageDb = Color(0xFFBF5AF2),
        accent = Color(0xFF00C7BE),
        focusHalo = Color(0x7300C7BE),
        swatch = Color(0xFF00C7BE),
    )
    AccentOption.SolarAmber -> AccentPalette(
        primaryContainer = Color(0xFFFF9F0A),
        onPrimaryContainer = Color(0xFF3B2200),
        primary = Color(0xFFFFCC70),
        onPrimary = Color(0xFF3B2200),
        primaryLight = Color(0xFFC77A00),
        onPrimaryLight = Color(0xFFFFFFFF),
        storagePhoto = Color(0xFFFF9F0A),
        storageVideo = Color(0xFFFF375F),
        storageDb = Color(0xFF64D2FF),
        accent = Color(0xFFFF9F0A),
        focusHalo = Color(0x73FF9F0A),
        swatch = Color(0xFFFF9F0A),
    )
    AccentOption.DeepJade -> AccentPalette(
        primaryContainer = Color(0xFF30D158),
        onPrimaryContainer = Color(0xFF003910),
        primary = Color(0xFF6FE887),
        onPrimary = Color(0xFF003910),
        primaryLight = Color(0xFF1B8A3A),
        onPrimaryLight = Color(0xFFFFFFFF),
        storagePhoto = Color(0xFF30D158),
        storageVideo = Color(0xFF00C7BE),
        storageDb = Color(0xFF5856D6),
        accent = Color(0xFF30D158),
        focusHalo = Color(0x7330D158),
        swatch = Color(0xFF30D158),
    )
}

private fun ColorScheme.withAccent(palette: AccentPalette, darkTheme: Boolean): ColorScheme =
    if (darkTheme) {
        copy(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            surfaceTint = palette.accent,
        )
    } else {
        copy(
            primary = palette.primaryLight,
            onPrimary = palette.onPrimaryLight,
            primaryContainer = palette.primaryContainer,
            onPrimaryContainer = palette.onPrimaryContainer,
            surfaceTint = palette.accent,
        )
    }

private fun darkExtended(palette: AccentPalette): GaliusExtendedColors =
    GaliusExtendedColors(
        accentIndigo = palette.accent,
        accentCyan = palette.accent,
        focusHalo = palette.focusHalo,
        storagePhoto = palette.storagePhoto,
        storageVideo = palette.storageVideo,
        storageDb = palette.storageDb,
    )

private fun lightExtended(palette: AccentPalette): GaliusExtendedColors =
    GaliusExtendedColors(
        canvas = LightCanvasBase,
        card = LightCardSurface,
        elevated = LightElevatedSurface,
        accentIndigo = palette.accent,
        accentCyan = palette.accent,
        ghostBorder = LightGhostBorder,
        ghostBorderSubtle = LightGhostBorderSubtle,
        ghostBorderStrong = LightGhostBorderStrong,
        ghostFill = LightGhostFill,
        glassNav = LightGlassNavBackground,
        searchField = LightSearchFieldBackground,
        metadataDescription = LightMetadataDescription,
        metadataCaption = LightMetadataCaption,
        focusHalo = palette.focusHalo,
        storagePhoto = palette.storagePhoto,
        storageVideo = palette.storageVideo,
        storageDb = palette.storageDb,
        tagNeutralContainer = LightTagNeutralContainer,
        mediaScrim = Brush.verticalGradient(
            colors = listOf(Color.Transparent, LightScrimBottom),
        ),
    )

/**
 * Gallius media catalog theme.
 * Supports dark, light, and accent remapping from Settings.
 */
@Composable
fun GaliusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: AccentOption = AccentOption.ElectricIndigo,
    content: @Composable () -> Unit,
) {
    val palette = remember(accent) { accentPalette(accent) }
    val colorScheme = remember(darkTheme, accent) {
        val base = if (darkTheme) GaliusDarkBaseScheme else GaliusLightBaseScheme
        base.withAccent(palette, darkTheme)
    }
    val extended = remember(darkTheme, accent) {
        if (darkTheme) darkExtended(palette) else lightExtended(palette)
    }

    CompositionLocalProvider(
        LocalGaliusColors provides extended,
        LocalGaliusTypography provides GaliusExtendedTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = GaliusShapes,
            content = content,
        )
    }
}
