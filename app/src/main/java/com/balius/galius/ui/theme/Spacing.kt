package com.balius.galius.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gallius spacing — absolute 8pt rhythm (base 8px).
 */
object GaliusSpacing {
    val xs: Dp = 4.dp // 0.25rem
    val sm: Dp = 8.dp // 0.5rem
    val md: Dp = 16.dp // 1rem — gutters
    val lg: Dp = 24.dp // 1.5rem
    val xl: Dp = 32.dp // 2rem
    val xxl: Dp = 48.dp // 3rem

    val gutter: Dp = md
    val margin: Dp = md
    val gutterDesktop: Dp = lg
    val marginDesktop: Dp = xl

    /** Floating bottom nav / decks above canvas. */
    val floatingBottom: Dp = md

    val navRailCompact: Dp = 72.dp
    val navRailExpanded: Dp = 240.dp

    val searchBarHeight: Dp = 44.dp
    val primaryButtonHeight: Dp = 40.dp
    val tagPillHeight: Dp = 28.dp
    val tagPillHeightCompact: Dp = 24.dp
    val checkboxSize: Dp = 18.dp
    val navIconHit: Dp = 36.dp
}
