package com.balius.galius.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.common.model.AccentOption
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.accentPalette

@Composable
fun AccentSwatchRow(
    selected: AccentOption,
    onSelected: (AccentOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_luminescence_accent),
                style = typography.bodySm,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = accentLabel(selected),
                style = typography.labelNumeric,
                color = colors.accentCyan,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccentOption.entries.forEach { option ->
                val palette = accentPalette(option)
                val isSelected = option == selected
                val label = accentLabel(option)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.swatch)
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, scheme.onSurface.copy(alpha = 0.35f), CircleShape)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onSelected(option) }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun accentLabel(option: AccentOption): String = stringResource(
    when (option) {
        AccentOption.ElectricIndigo -> R.string.settings_accent_indigo
        AccentOption.ElectricCyan -> R.string.settings_accent_cyan
        AccentOption.SolarAmber -> R.string.settings_accent_amber
        AccentOption.DeepJade -> R.string.settings_accent_jade
    },
)
