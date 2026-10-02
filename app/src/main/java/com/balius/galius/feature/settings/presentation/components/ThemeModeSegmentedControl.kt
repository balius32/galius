package com.balius.galius.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.PillShape

@Composable
fun ThemeModeSegmentedControl(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm)) {
        Text(
            text = stringResource(R.string.settings_interface_mode),
            style = typography.bodySm,
            color = scheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(scheme.surfaceContainerLowest)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThemeMode.entries.forEach { mode ->
                val selectedMode = mode == selected
                val label = when (mode) {
                    ThemeMode.System -> stringResource(R.string.settings_theme_system)
                    ThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
                    ThemeMode.Light -> stringResource(R.string.settings_theme_light)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(PillShape)
                        .background(
                            if (selectedMode) scheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                        )
                        .clickable { onSelected(mode) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = typography.bodySm,
                        fontWeight = if (selectedMode) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            selectedMode -> scheme.onPrimaryContainer
                            else -> scheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}
