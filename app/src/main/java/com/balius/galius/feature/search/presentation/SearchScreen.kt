package com.balius.galius.feature.search.presentation

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
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
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.CardSurface
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    contentBottomPadding: Dp,
    onOpenViewer: (mediaId: String, source: MediaBrowseSource) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        contentBottomPadding = contentBottomPadding,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onCategorySelected: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onOpenMedia: (String) -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val selectedCategory = state.selectedCategory

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBase)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = GaliusSpacing.margin),
        contentPadding = PaddingValues(bottom = contentBottomPadding + GaliusSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.gutter),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md)) {
                Spacer(modifier = Modifier.height(GaliusSpacing.sm))
                Text(
                    text = stringResource(R.string.nav_search),
                    style = typography.headlineLgMobile,
                    color = Color.White,
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

@Composable
private fun SectionHeader(
    title: String,
    hint: String,
) {
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = typography.labelPill,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = hint,
            style = typography.bodySm,
            color = AccentCyan,
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
    val accent = tagChipColors(item.category.colorKey)
    val shape = RoundedCornerShape(GaliusRadius.lg)
    Column(
        modifier = Modifier
            .width(168.dp)
            .clip(shape)
            .background(CardSurface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) AccentCyan else GhostBorder,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(GaliusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(GaliusRadius.md))
                .background(accent.container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderSpecial,
                contentDescription = null,
                tint = if (selected) AccentCyan else accent.content,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = item.category.name,
            style = typography.headlineSm,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pluralStringResource(
                R.plurals.manage_tags_count,
                item.tags.size,
                item.tags.size,
            ),
            style = typography.bodySm,
            color = GaliusThemeTokens.colors.metadataCaption,
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
            contentBottomPadding = 96.dp,
        )
    }
}
