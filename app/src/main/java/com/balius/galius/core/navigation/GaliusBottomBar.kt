package com.balius.galius.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.PillShape

/**
 * Floating frosted glass bottom bar — Stitch gallius catalog chrome.
 * Tabs: Home · Search · Settings.
 */
@Composable
fun GaliusBottomBar(
    currentRoute: TopLevelRoute,
    onNavigate: (TopLevelRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GaliusThemeTokens.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = GaliusSpacing.margin)
            .padding(bottom = GaliusSpacing.floatingBottom),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(colors.glassNav)
                .border(1.dp, colors.ghostBorder, PillShape)
                .padding(GaliusSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            topLevelDestinations.forEach { destination ->
                val selected = destination.route == currentRoute
                BottomBarItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onNavigate(destination.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(destination.labelRes)
    val contentDescription = stringResource(destination.contentDescriptionRes)
    val unselected = scheme.onSurfaceVariant

    Column(
        modifier = modifier
            .height(44.dp)
            .clip(PillShape)
            .then(
                if (selected) {
                    Modifier
                        .background(colors.ghostFill)
                        .border(1.dp, colors.ghostBorder, PillShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = contentDescription,
            tint = if (selected) scheme.primary else unselected,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = typography.labelPill,
            color = if (selected) scheme.onSurface else unselected,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(scheme.primary),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GaliusBottomBarPreview() {
    GaliusTheme {
        GaliusBottomBar(
            currentRoute = TopLevelRoute.Home,
            onNavigate = {},
        )
    }
}
