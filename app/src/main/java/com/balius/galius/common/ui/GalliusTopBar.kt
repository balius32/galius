package com.balius.galius.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.Primary

@Composable
fun GalliusTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectionCount: Int = 0,
    isRemoving: Boolean = false,
    onClearSelection: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
) {
    if (selectionCount > 0) {
        SelectionTopBar(
            selectionCount = selectionCount,
            isRemoving = isRemoving,
            onClearSelection = onClearSelection ?: {},
            onRemoveClick = onRemoveClick ?: {},
            modifier = modifier,
        )
    } else {
        BrandTopBar(
            onSearchClick = onSearchClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun BrandTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors

    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas.copy(alpha = 0.8f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = GaliusSpacing.margin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_gallius_logo),
                contentDescription = stringResource(R.string.brand_logo_cd),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(GaliusSpacing.sm + 2.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = typography.headlineSm.copy(color = onSurface),
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

@Composable
private fun SelectionTopBar(
    selectionCount: Int,
    isRemoving: Boolean,
    onClearSelection: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = GaliusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClearSelection, enabled = !isRemoving) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = onSurface,
                )
            }
            Text(
                text = stringResource(R.string.home_selection_count, selectionCount),
                style = typography.headlineSm.copy(color = onSurface),
            )
        }
        if (isRemoving) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = GaliusSpacing.sm)
                    .size(22.dp),
                color = Primary,
                strokeWidth = 2.dp,
            )
        } else {
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.home_remove_restore_cd),
                    tint = colors.tagRose,
                )
            }
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

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GalliusTopBarSelectionPreview() {
    GaliusTheme {
        GalliusTopBar(
            onSearchClick = {},
            selectionCount = 2,
            onClearSelection = {},
            onRemoveClick = {},
        )
    }
}
