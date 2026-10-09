package com.balius.galius.feature.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.balius.galius.R
import com.balius.galius.common.ui.TagChip
import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.SheetShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchFilterSheet(
    categories: List<CategoryWithTags>,
    selectedTagIds: Set<String>,
    onToggleTag: (String) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val available = categories.filter { it.tags.isNotEmpty() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        shape = SheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GaliusSpacing.md)
                .padding(bottom = GaliusSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.search_filter_title),
                    style = typography.headlineSm,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(
                    onClick = onClearFilters,
                    enabled = selectedTagIds.isNotEmpty(),
                ) {
                    Text(text = stringResource(R.string.search_filter_clear))
                }
            }
            if (selectedTagIds.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.search_filter_active_count, selectedTagIds.size),
                    style = typography.bodyMd,
                    color = GaliusThemeTokens.colors.metadataDescription,
                )
            }
            if (available.isEmpty()) {
                Text(
                    text = stringResource(R.string.media_no_tags_available),
                    style = typography.bodyMd,
                    color = GaliusThemeTokens.colors.metadataDescription,
                )
            } else {
                available.forEach { group ->
                    Text(
                        text = group.category.name,
                        style = typography.labelPill,
                        color = GaliusThemeTokens.colors.metadataCaption,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                    ) {
                        group.tags.forEach { tag ->
                            TagChip(
                                label = stringResource(R.string.manage_tag_hash, tag.name),
                                colorKey = tag.colorKey,
                                selected = tag.id in selectedTagIds,
                                onClick = { onToggleTag(tag.id) },
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(R.string.action_close))
            }
        }
    }
}

@Preview
@Composable
private fun SearchFilterSheetPreview() {
    GaliusTheme {
        SearchFilterSheet(
            categories = listOf(
                CategoryWithTags(
                    category = Category("1", "Locations", TagColorKey.Indigo, 0L),
                    tags = listOf(
                        Tag("t1", "1", "BigSur", TagColorKey.Lavender, 0L),
                        Tag("t2", "1", "Coast", TagColorKey.Mint, 0L),
                    ),
                ),
            ),
            selectedTagIds = setOf("t1"),
            onToggleTag = {},
            onClearFilters = {},
            onDismiss = {},
        )
    }
}
