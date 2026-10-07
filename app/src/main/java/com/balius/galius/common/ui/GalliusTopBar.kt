package com.balius.galius.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.Primary

@Composable
fun GalliusTopBar(
    title: String = stringResource(R.string.app_name),
    modifier: Modifier = Modifier,
    selectionCount: Int = 0,
    isRemoving: Boolean = false,
    onClearSelection: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    filterActiveCount: Int = 0,
    onFilterClick: (() -> Unit)? = null,
) {
    if (selectionCount > 0) {
        SelectionTopBar(
            selectionCount = selectionCount,
            isRemoving = isRemoving,
            onClearSelection = onClearSelection ?: {},
            onShareClick = onShareClick ?: {},
            onRemoveClick = onRemoveClick ?: {},
            modifier = modifier,
        )
    } else {
        BrandTopBar(
            title = title,
            filterActiveCount = filterActiveCount,
            onFilterClick = onFilterClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun BrandTopBar(
    title: String,
    filterActiveCount: Int,
    onFilterClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = GaliusSpacing.margin)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = typography.headlineLgMobile.copy(
                    color = onSurface,
                    fontWeight = FontWeight.Bold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            )
            if (onFilterClick != null) {
                Box {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = stringResource(R.string.search_filter_cd),
                            tint = onSurface,
                        )
                    }
                    if (filterActiveCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(8.dp)
                                .background(Primary, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionTopBar(
    selectionCount: Int,
    isRemoving: Boolean,
    onClearSelection: () -> Unit,
    onShareClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.canvas),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = GaliusSpacing.sm)
                .height(56.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.home_share_cd),
                        tint = onSurface,
                    )
                }
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
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GalliusTopBarPreview() {
    GaliusTheme {
        GalliusTopBar()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GalliusTopBarSelectionPreview() {
    GaliusTheme {
        GalliusTopBar(
            selectionCount = 2,
            onClearSelection = {},
            onShareClick = {},
            onRemoveClick = {},
        )
    }
}
