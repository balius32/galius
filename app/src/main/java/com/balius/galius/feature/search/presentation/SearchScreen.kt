package com.balius.galius.feature.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.common.ui.TagChip
import com.balius.galius.common.ui.tagChipColors
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.presentation.components.MediaThumbCard
import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.OnPrimaryContainer
import com.balius.galius.ui.theme.PrimaryContainer
import com.balius.galius.ui.theme.SheetShape
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    contentBottomPadding: Dp,
    onOpenViewer: (mediaId: String, source: MediaBrowseSource) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    BackHandler(enabled = state.addMediaPickerVisible) {
        viewModel.onIntent(SearchIntent.DismissAddMediaPicker)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            state = state,
            onCategorySelected = { viewModel.onIntent(SearchIntent.CategorySelected(it)) },
            onTagSelected = { viewModel.onIntent(SearchIntent.TagSelected(it)) },
            onOpenMedia = { mediaId ->
                val source = when {
                    state.selectedTagId != null -> MediaBrowseSource.Tag(state.selectedTagId!!)
                    state.selectedCategoryId != null -> MediaBrowseSource.Category(state.selectedCategoryId!!)
                    else -> return@SearchScreen
                }
                onOpenViewer(mediaId, source)
            },
            onOpenAddMediaPicker = { viewModel.onIntent(SearchIntent.OpenAddMediaPicker) },
            onDismissAddMediaPicker = { viewModel.onIntent(SearchIntent.DismissAddMediaPicker) },
            onTogglePickerSelection = { viewModel.onIntent(SearchIntent.TogglePickerSelection(it)) },
            onConfirmAddToCategory = { viewModel.onIntent(SearchIntent.ConfirmAddToCategory) },
            contentBottomPadding = contentBottomPadding,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentBottomPadding + GaliusSpacing.sm),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onCategorySelected: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onOpenMedia: (String) -> Unit,
    onOpenAddMediaPicker: () -> Unit,
    onDismissAddMediaPicker: () -> Unit,
    onTogglePickerSelection: (String) -> Unit,
    onConfirmAddToCategory: () -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = GaliusThemeTokens.colors
    val selectedCategory = state.selectedCategory

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        SearchBrowseContent(
            state = state,
            onCategorySelected = onCategorySelected,
            onTagSelected = onTagSelected,
            onOpenMedia = onOpenMedia,
            contentBottomPadding = contentBottomPadding,
        )

        if (state.addMediaPickerVisible && selectedCategory != null) {
            SearchAddMediaPickerSheet(
                categoryName = selectedCategory.category.name,
                items = state.libraryItems,
                selectedIds = state.pickerSelectedIds,
                isSaving = state.isAddingToCategory,
                onDismiss = onDismissAddMediaPicker,
                onToggleSelection = onTogglePickerSelection,
                onConfirm = onConfirmAddToCategory,
            )
        }

        if (selectedCategory != null && !state.addMediaPickerVisible) {
            FloatingActionButton(
                onClick = onOpenAddMediaPicker,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = GaliusSpacing.margin,
                        bottom = contentBottomPadding + GaliusSpacing.lg,
                    ),
                shape = CircleShape,
                containerColor = PrimaryContainer,
                contentColor = OnPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.search_add_to_category_fab_cd),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchBrowseContent(
    state: SearchState,
    onCategorySelected: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onOpenMedia: (String) -> Unit,
    contentBottomPadding: Dp,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val selectedCategory = state.selectedCategory
    val fabClearance = if (selectedCategory != null) 88.dp else 0.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = GaliusSpacing.margin),
        contentPadding = PaddingValues(bottom = contentBottomPadding + GaliusSpacing.lg + fabClearance),
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md)) {
                Spacer(modifier = Modifier.height(GaliusSpacing.sm))
                Text(
                    text = stringResource(R.string.nav_search),
                    style = typography.headlineLgMobile,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                SectionHeader(
                    title = stringResource(R.string.search_select_category),
                    hint = stringResource(R.string.search_select_category_hint),
                )

                if (state.categories.isEmpty()) {
                    Text(
                        text = stringResource(R.string.categories_empty),
                        style = typography.bodyMd,
                        color = colors.metadataDescription,
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                    ) {
                        items(state.categories, key = { it.category.id }) { item ->
                            CategoryBrowseCard(
                                item = item,
                                selected = item.category.id == state.selectedCategoryId,
                                onClick = { onCategorySelected(item.category.id) },
                            )
                        }
                    }
                }

                if (selectedCategory != null) {
                    SectionHeader(
                        title = stringResource(
                            R.string.search_tags_in_category,
                            selectedCategory.category.name,
                        ),
                        hint = stringResource(R.string.search_click_tag_hint),
                    )
                    if (selectedCategory.tags.isEmpty()) {
                        Text(
                            text = stringResource(R.string.category_empty_tags),
                            style = typography.bodyMd,
                            color = colors.metadataDescription,
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                        ) {
                            selectedCategory.tags.forEach { tag ->
                                TagChip(
                                    label = stringResource(R.string.manage_tag_hash, tag.name),
                                    colorKey = tag.colorKey,
                                    selected = tag.id == state.selectedTagId,
                                    onClick = { onTagSelected(tag.id) },
                                )
                            }
                        }
                    }

                    val resultsTitle = state.selectedTagId?.let { tagId ->
                        val tagName = selectedCategory.tags.find { it.id == tagId }?.name.orEmpty()
                        stringResource(R.string.search_files_with_tag, tagName)
                    } ?: stringResource(
                        R.string.search_files_in_category,
                        selectedCategory.category.name,
                    )
                    SectionHeader(
                        title = resultsTitle,
                        hint = stringResource(R.string.search_items_count, state.results.size),
                    )
                } else if (state.categories.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.search_pick_category_body),
                        style = typography.bodyMd,
                        color = colors.metadataDescription,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }

        if (selectedCategory != null && state.results.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.search_no_media_in_filter),
                    style = typography.bodyMd,
                    color = colors.metadataDescription,
                    modifier = Modifier.padding(vertical = GaliusSpacing.md),
                )
            }
        }

        items(state.results, key = { it.id }) { item ->
            MediaThumbCard(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMedia(item.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAddMediaPickerSheet(
    categoryName: String,
    items: List<MediaItem>,
    selectedIds: Set<String>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onToggleSelection: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        shape = SheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = GaliusSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = scheme.onSurface,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.search_add_to_category_title, categoryName),
                        style = typography.headlineSm,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selectedIds.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.home_selection_count, selectedIds.size),
                            style = typography.bodySm,
                            color = colors.metadataCaption,
                        )
                    }
                }
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = GaliusSpacing.sm)
                            .size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    TextButton(
                        onClick = onConfirm,
                        enabled = selectedIds.isNotEmpty(),
                    ) {
                        Text(text = stringResource(R.string.search_add_media_confirm))
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = GaliusSpacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_empty_body),
                        style = typography.bodyMd,
                        color = colors.metadataDescription,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = GaliusSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
                ) {
                    items(items, key = { it.id }) { item ->
                        MediaThumbCard(
                            item = item,
                            selected = item.id in selectedIds,
                            selectionMode = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSelection(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    hint: String,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = typography.labelPill,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = hint,
            style = typography.bodySm,
            color = colors.metadataCaption,
        )
    }
}

@Composable
private fun CategoryBrowseCard(
    item: CategoryWithTags,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val accent = tagChipColors(item.category.colorKey)
    val shape = RoundedCornerShape(GaliusRadius.lg)

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(shape)
            .background(colors.card)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) AccentCyan else colors.ghostBorder,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = GaliusSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(GaliusRadius.md))
                .background(accent.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderSpecial,
                contentDescription = null,
                tint = if (selected) AccentCyan else accent.content,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = item.category.name,
            style = typography.headlineSm,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun SearchScreenPreview() {
    GaliusTheme {
        SearchScreen(
            state = SearchState(
                categories = listOf(
                    CategoryWithTags(
                        category = Category("1", "Locations", TagColorKey.Indigo, 0L),
                        tags = listOf(Tag("t1", "1", "BigSur", TagColorKey.Lavender, 0L)),
                    ),
                ),
                selectedCategoryId = "1",
                selectedTagId = "t1",
                results = listOf(
                    MediaItem(
                        id = "m1",
                        filePath = "",
                        displayName = "IMG_001.jpg",
                        mimeType = "image/jpeg",
                        type = MediaType.Photo,
                        createdAtMillis = 0L,
                    ),
                ),
            ),
            onCategorySelected = {},
            onTagSelected = {},
            onOpenMedia = {},
            onOpenAddMediaPicker = {},
            onDismissAddMediaPicker = {},
            onTogglePickerSelection = {},
            onConfirmAddToCategory = {},
            contentBottomPadding = 96.dp,
        )
    }
}
