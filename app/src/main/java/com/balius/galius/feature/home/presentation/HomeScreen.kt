package com.balius.galius.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.common.ui.GalliusTopBar
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.presentation.components.MediaDetailsSheet
import com.balius.galius.feature.media.presentation.components.MediaTagPickerSheet
import com.balius.galius.feature.media.presentation.components.MediaThumbCard
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.OnPrimaryContainer
import com.balius.galius.ui.theme.PillShape
import com.balius.galius.ui.theme.Primary
import com.balius.galius.ui.theme.PrimaryContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    onOpenSearch: () -> Unit,
    onImportClick: () -> Unit,
    onOpenViewer: (mediaId: String, source: MediaBrowseSource) -> Unit,
    contentBottomPadding: Dp,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onOpenSearch = onOpenSearch,
            onImportClick = onImportClick,
            onToggleVideosOnly = { viewModel.onIntent(HomeIntent.ToggleVideosOnly) },
            onLongPressItem = { viewModel.onIntent(HomeIntent.LongPressItem(it)) },
            onToggleItem = { viewModel.onIntent(HomeIntent.ToggleItemSelection(it)) },
            onOpenMedia = { mediaId ->
                val source = if (state.videosOnly) {
                    MediaBrowseSource.LibraryVideosOnly
                } else {
                    MediaBrowseSource.Library
                }
                onOpenViewer(mediaId, source)
            },
            onClearSelection = { viewModel.onIntent(HomeIntent.ClearSelection) },
            onRemoveSelected = { viewModel.onIntent(HomeIntent.RemoveSelected) },
            contentBottomPadding = contentBottomPadding,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentBottomPadding + GaliusSpacing.sm),
        )
        state.detailsItem?.let { item ->
            MediaDetailsSheet(
                item = item,
                assignedTags = state.detailsAssignedTags,
                onDismiss = { viewModel.onIntent(HomeIntent.CloseDetails) },
                onOpenTagPicker = { viewModel.onIntent(HomeIntent.OpenTagPicker) },
                onRemoveTag = { viewModel.onIntent(HomeIntent.RemoveTagFromDetails(it)) },
            )
        }
        if (state.showTagPicker && state.detailsItem != null) {
            MediaTagPickerSheet(
                categories = state.allCategories,
                assignedTagIds = state.detailsAssignedTags.map { it.id }.toSet(),
                onToggleTag = { tagId, currentlyAssigned ->
                    if (currentlyAssigned) {
                        viewModel.onIntent(HomeIntent.RemoveTagFromDetails(tagId))
                    } else {
                        viewModel.onIntent(HomeIntent.AddTagToDetails(tagId))
                    }
                },
                onDismiss = { viewModel.onIntent(HomeIntent.CloseTagPicker) },
            )
        }
    }
}

@Composable
fun HomeScreen(
    state: HomeState,
    onOpenSearch: () -> Unit,
    onImportClick: () -> Unit,
    onToggleVideosOnly: () -> Unit,
    onLongPressItem: (String) -> Unit,
    onToggleItem: (String) -> Unit,
    onOpenMedia: (String) -> Unit,
    onClearSelection: () -> Unit,
    onRemoveSelected: () -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val fabClearance = 72.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GalliusTopBar(
                onSearchClick = onOpenSearch,
                selectionCount = state.selectedCount,
                isRemoving = state.isRemoving,
                onClearSelection = onClearSelection,
                onRemoveClick = onRemoveSelected,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = GaliusSpacing.margin,
                    end = GaliusSpacing.margin,
                    top = GaliusSpacing.sm,
                    bottom = contentBottomPadding + fabClearance,
                ),
                horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
                verticalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilterChipRow(
                        videosOnly = state.videosOnly,
                        onToggleVideosOnly = onToggleVideosOnly,
                    )
                }

                when {
                    state.isLoading && state.items.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(GaliusSpacing.xxl),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                    }

                    state.isEmpty -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyVault(
                                onImportClick = onImportClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = GaliusSpacing.xl),
                            )
                        }
                    }

                    else -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(R.string.home_section_recent_hits),
                                style = typography.headlineSm,
                                color = colors.metadataDescription,
                                modifier = Modifier.padding(vertical = GaliusSpacing.sm),
                            )
                        }
                        items(
                            items = state.visibleItems,
                            key = { it.id },
                        ) { item ->
                            MediaThumbCard(
                                item = item,
                                selected = item.id in state.selectedIds,
                                selectionMode = state.selectionMode,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if (state.selectionMode) {
                                            onToggleItem(item.id)
                                        } else {
                                            onOpenMedia(item.id)
                                        }
                                    },
                                    onLongClick = { onLongPressItem(item.id) },
                                ),
                            )
                        }
                    }
                }
            }
        }

        if (!state.selectionMode) {
            FloatingActionButton(
                onClick = onImportClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = GaliusSpacing.margin,
                        bottom = contentBottomPadding + GaliusSpacing.sm,
                    ),
                containerColor = PrimaryContainer,
                contentColor = OnPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.home_fab_import_cd),
                )
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    videosOnly: Boolean,
    onToggleVideosOnly: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.xs + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (videosOnly) {
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
                onClear = onToggleVideosOnly,
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .border(1.dp, GhostBorder.copy(alpha = 0.5f), PillShape)
                    .background(GaliusThemeTokens.colors.card.copy(alpha = 0.6f))
                    .clickable(onClick = onToggleVideosOnly)
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
                        text = stringResource(R.string.search_filter_videos_only),
                        style = typography.labelPill,
                        color = GaliusThemeTokens.colors.tagNeutral,
                    )
                }
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
    onClear: () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(container)
            .border(1.dp, border, PillShape)
            .clickable(onClick = onClear)
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
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
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
private fun HomeScreenEmptyPreview() {
    GaliusTheme {
        HomeScreen(
            state = HomeState(isLoading = false, items = emptyList()),
            onOpenSearch = {},
            onImportClick = {},
            onToggleVideosOnly = {},
            onLongPressItem = {},
            onToggleItem = {},
            onOpenMedia = {},
            onClearSelection = {},
            onRemoveSelected = {},
            contentBottomPadding = 96.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 800)
@Composable
private fun HomeScreenFilledPreview() {
    GaliusTheme {
        HomeScreen(
            state = HomeState(
                isLoading = false,
                selectionMode = true,
                selectedIds = setOf("1"),
                items = listOf(
                    MediaItem(
                        id = "1",
                        filePath = "",
                        displayName = "One",
                        mimeType = "image/jpeg",
                        type = MediaType.Photo,
                        createdAtMillis = 1L,
                    ),
                    MediaItem(
                        id = "2",
                        filePath = "",
                        displayName = "Two",
                        mimeType = "video/mp4",
                        type = MediaType.Video,
                        createdAtMillis = 2L,
                    ),
                ),
            ),
            onOpenSearch = {},
            onImportClick = {},
            onToggleVideosOnly = {},
            onLongPressItem = {},
            onToggleItem = {},
            onOpenMedia = {},
            onClearSelection = {},
            onRemoveSelected = {},
            contentBottomPadding = 96.dp,
        )
    }
}
