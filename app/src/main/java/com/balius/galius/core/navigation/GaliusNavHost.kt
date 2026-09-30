package com.balius.galius.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.balius.galius.feature.home.presentation.HomeRoute
import com.balius.galius.feature.more.presentation.MoreRoute
import com.balius.galius.feature.search.presentation.SearchRoute
import com.balius.galius.ui.theme.GaliusSpacing

@Composable
fun GaliusNavHost(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(TopLevelRoute.Home)
    var currentRoute by remember { mutableStateOf<TopLevelRoute>(TopLevelRoute.Home) }

    fun navigateToTopLevel(route: TopLevelRoute) {
        if (currentRoute == route) return
        currentRoute = route
        backStack.clear()
        backStack.add(route)
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                    currentRoute = backStack.lastOrNull() as? TopLevelRoute ?: TopLevelRoute.Home
                }
            },
            modifier = Modifier.fillMaxSize(),
            entryProvider = entryProvider {
                entry<TopLevelRoute.Home> {
                    HomeRoute(
                        onOpenSearch = { navigateToTopLevel(TopLevelRoute.Search) },
                        contentBottomPadding = GaliusSpacing.xxl + GaliusSpacing.xl,
                    )
                }
                entry<TopLevelRoute.Search> {
                    SearchRoute(
                        contentBottomPadding = GaliusSpacing.xxl + GaliusSpacing.xl,
                    )
                }
                entry<TopLevelRoute.More> {
                    MoreRoute(
                        contentBottomPadding = GaliusSpacing.xxl + GaliusSpacing.xl,
                    )
                }
            },
        )

        GaliusBottomBar(
            currentRoute = currentRoute,
            onNavigate = ::navigateToTopLevel,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
