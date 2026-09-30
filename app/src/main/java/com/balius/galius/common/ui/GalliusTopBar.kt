package com.balius.galius.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.AccentIndigo
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder

@Composable
fun GalliusTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CanvasBase.copy(alpha = 0.8f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = GaliusSpacing.margin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(GaliusRadius.default))
                    .background(AccentIndigo.copy(alpha = 0.35f))
                    .border(1.dp, GhostBorder, RoundedCornerShape(GaliusRadius.default)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentCyan),
                )
            }
            Spacer(modifier = Modifier.width(GaliusSpacing.sm + 2.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = typography.headlineSm.copy(color = Color.White),
            )
            Spacer(modifier = Modifier.width(GaliusSpacing.xs))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.accentCyan),
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.action_search),
                tint = colors.metadataDescription,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GalliusTopBarPreview() {
    GaliusTheme {
        GalliusTopBar(onSearchClick = {})
    }
}
