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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.GhostFill
import com.balius.galius.ui.theme.Outline
import com.balius.galius.ui.theme.PillShape

/**
 * Floating frosted glass bottom bar — Stitch gallius catalog chrome.
 * Tabs: Home · Search · More.
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
                .background(Color(0xD914161C))
                .border(1.dp, GhostBorder, PillShape)
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
                    selectedContentColor = colors.accentCyan,
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
    selectedContentColor: Color,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val label = stringResource(destination.labelRes)
    val contentDescription = stringResource(destination.contentDescriptionRes)

    Column(
        modifier = modifier
            .height(44.dp)
            .clip(PillShape)
            .then(
                if (selected) {
                    Modifier
                        .background(GhostFill)
                        .border(1.dp, GhostBorder, PillShape)
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
            tint = if (selected) selectedContentColor else Outline,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = typography.labelPill,
            color = if (selected) Color.White else Outline,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AccentCyan),
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
