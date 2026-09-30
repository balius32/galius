package com.balius.galius.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.common.ui.GalliusTopBar
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.PillShape
import com.balius.galius.ui.theme.Primary
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onOpenSearch: () -> Unit,
    contentBottomPadding: Dp,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onOpenSearch = onOpenSearch,
        onImportClick = { /* wired in media feature */ },
        contentBottomPadding = contentBottomPadding,
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onOpenSearch: () -> Unit,
    onImportClick: () -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBase),
    ) {
        GalliusTopBar(onSearchClick = onOpenSearch)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = contentBottomPadding),
        ) {
            FilterChipRow(
                modifier = Modifier.padding(
                    start = GaliusSpacing.margin,
                    end = GaliusSpacing.margin,
                    top = GaliusSpacing.sm,
                ),
            )
            if (state.isEmpty) {
                EmptyVault(
                    onImportClick = onImportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(GaliusSpacing.xl),
                )
            } else {
                Text(
                    text = stringResource(R.string.home_section_recent_hits),
                    style = typography.headlineSm,
                    color = colors.metadataDescription,
                    modifier = Modifier.padding(GaliusSpacing.margin),
                )
            }
        }
    }
}

@Composable
private fun FilterChipRow(modifier: Modifier = Modifier) {
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.xs + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActiveFilterChip(
            label = stringResource(R.string.search_filter_videos_only),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(12.dp),
                )
            },
            tint = AccentCyan,
            container = AccentCyan.copy(alpha = 0.15f),
            border = AccentCyan.copy(alpha = 0.35f),
        )
        Box(
            modifier = Modifier
                .clip(PillShape)
                .border(1.dp, GhostBorder.copy(alpha = 0.5f), PillShape)
                .background(GaliusThemeTokens.colors.card.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.search_add_filter),
                    tint = GaliusThemeTokens.colors.tagNeutral,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = stringResource(R.string.action_add),
                    style = typography.labelPill,
                    color = GaliusThemeTokens.colors.tagNeutral,
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(container)
            .border(1.dp, border, PillShape)
            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        leadingIcon?.invoke()
        Text(text = label, style = typography.labelPill, color = tint)
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.action_close),
            tint = tint,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun EmptyVault(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(GaliusSpacing.xxl))
        Text(
            text = stringResource(R.string.home_empty_title),
            style = typography.headlineMd,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.sm))
        Text(
            text = stringResource(R.string.home_empty_body),
            style = typography.bodyMd,
            color = colors.metadataDescription,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
        TextButton(onClick = onImportClick) {
            Text(
                text = stringResource(R.string.home_import_cta),
                style = typography.bodyMd,
                color = Primary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    GaliusTheme {
        HomeScreen(
            state = HomeState(isEmpty = true),
            onOpenSearch = {},
            onImportClick = {},
            contentBottomPadding = 96.dp,
        )
    }
}
