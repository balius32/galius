package com.balius.galius.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Gallius shape tokens (8pt-friendly radii from design system). */
object GaliusRadius {
    val sm = 4.dp // 0.25rem
    val default = 8.dp // 0.5rem — inputs
    val md = 12.dp // 0.75rem
    val lg = 16.dp // 1rem — media cards
    val xl = 24.dp // 1.5rem — sheets / floating nav
    val full = 9999.dp // pills
}

val GaliusShapes = Shapes(
    extraSmall = RoundedCornerShape(GaliusRadius.sm),
    small = RoundedCornerShape(GaliusRadius.default),
    medium = RoundedCornerShape(GaliusRadius.md),
    large = RoundedCornerShape(GaliusRadius.lg),
    extraLarge = RoundedCornerShape(GaliusRadius.xl),
)

val MediaCardShape = RoundedCornerShape(GaliusRadius.lg)
val SheetShape = RoundedCornerShape(GaliusRadius.xl)
val PillShape = RoundedCornerShape(GaliusRadius.full)
val InputShape = RoundedCornerShape(GaliusRadius.default)
val CheckboxShape = RoundedCornerShape(4.dp)
val AvatarShape = CircleShape
