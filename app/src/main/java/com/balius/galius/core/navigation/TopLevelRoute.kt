package com.balius.galius.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.navigation3.runtime.NavKey
import com.balius.galius.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface TopLevelRoute : NavKey {
    @Serializable
    data object Home : TopLevelRoute

    @Serializable
    data object Search : TopLevelRoute

    @Serializable
    data object Settings : TopLevelRoute
}

data class TopLevelDestination(
    val route: TopLevelRoute,
    val labelRes: Int,
    val contentDescriptionRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        route = TopLevelRoute.Home,
        labelRes = R.string.nav_home,
        contentDescriptionRes = R.string.nav_home_cd,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    TopLevelDestination(
        route = TopLevelRoute.Search,
        labelRes = R.string.nav_search,
        contentDescriptionRes = R.string.nav_search_cd,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
    ),
    TopLevelDestination(
        route = TopLevelRoute.Settings,
        labelRes = R.string.nav_settings,
        contentDescriptionRes = R.string.nav_settings_cd,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)
