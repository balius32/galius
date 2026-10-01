package com.balius.galius.feature.media.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.balius.galius.R
import com.balius.galius.common.ui.TagChip
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.CardSurface
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.SheetShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaTagPickerSheet(
    categories: List<CategoryWithTags>,
    assignedTagIds: Set<String>,
    onToggleTag: (tagId: String, currentlyAssigned: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val typography = GaliusThemeTokens.typography
    val available = categories.filter { it.tags.isNotEmpty() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardSurface,
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
            Text(
                text = stringResource(R.string.media_pick_tag_title),
                style = typography.headlineSm,
                color = Color.White,
            )
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
                            val assigned = tag.id in assignedTagIds
                            TagChip(
                                label = tag.name,
                                colorKey = tag.colorKey,
                                selected = assigned,
                                onClick = { onToggleTag(tag.id, assigned) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun MediaTagPickerContentPreview() {
    GaliusTheme {
        Column(
            modifier = Modifier.padding(GaliusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.media_pick_tag_title),
                color = Color.White,
                style = GaliusThemeTokens.typography.headlineSm,
            )
            TagChip(
                label = "Beach",
                colorKey = TagColorKey.Sky,
                selected = true,
                onClick = {},
            )
        }
    }
}
