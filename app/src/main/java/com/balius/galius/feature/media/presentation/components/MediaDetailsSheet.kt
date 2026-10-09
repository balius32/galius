package com.balius.galius.feature.media.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.common.ui.TagChip
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.SheetShape
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaDetailsSheet(
    item: MediaItem,
    assignedTags: List<Tag>,
    onDismiss: () -> Unit,
    onOpenTagPicker: () -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = GaliusThemeTokens.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.card,
        shape = SheetShape,
    ) {
        DetailsSheetBody(
            item = item,
            assignedTags = assignedTags,
            onAddTagClick = onOpenTagPicker,
            onRemoveTag = onRemoveTag,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSheetBody(
    item: MediaItem,
    assignedTags: List<Tag>,
    onAddTagClick: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val dateLabel = remember(item.createdAtMillis) {
        DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
            .format(Date(item.createdAtMillis))
    }
    val formatLabel = remember(item.mimeType, item.type) {
        formatFromMime(item.mimeType, item.type)
    }
    val typeLabel = when (item.type) {
        MediaType.Photo -> stringResource(R.string.media_type_photo)
        MediaType.Video -> stringResource(R.string.media_type_video)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GaliusSpacing.md + 4.dp)
            .padding(bottom = GaliusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md + 4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = item.displayName,
                style = typography.headlineMd,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = typeLabel,
                style = typography.bodyLg,
                color = colors.metadataDescription,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GaliusRadius.lg))
                .background(colors.elevated)
                .border(1.dp, colors.ghostBorder, RoundedCornerShape(GaliusRadius.lg))
                .padding(GaliusSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.media_date_added),
                    style = typography.bodySm,
                    color = colors.metadataCaption,
                )
                Text(
                    text = dateLabel,
                    style = typography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.media_format),
                    style = typography.bodySm,
                    color = colors.metadataCaption,
                )
                Text(
                    text = formatLabel,
                    style = typography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                ) {
                    Text(
                        text = stringResource(R.string.tags_label),
                        style = typography.labelPill,
                        color = colors.metadataCaption,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(GaliusRadius.full))
                            .background(colors.elevated)
                            .border(1.dp, colors.ghostBorder, RoundedCornerShape(GaliusRadius.full))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.media_tags_assigned, assignedTags.size),
                            style = typography.labelPill,
                            color = colors.metadataDescription,
                        )
                    }
                }
                if (assignedTags.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.media_tags_hint),
                        style = typography.labelPill,
                        color = colors.metadataCaption,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
            ) {
                assignedTags.forEach { tag ->
                    TagChip(
                        label = tag.name,
                        colorKey = tag.colorKey,
                        onClick = { onRemoveTag(tag.id) },
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(GaliusRadius.md))
                        .background(colors.elevated.copy(alpha = 0.70f))
                        .border(1.dp, colors.ghostBorder.copy(alpha = 0.5f), RoundedCornerShape(GaliusRadius.md))
                        .clickable(onClick = onAddTagClick)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.media_add_tag),
                        style = typography.bodyMd,
                        color = colors.metadataCaption,
                    )
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(GaliusRadius.lg),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentIndigo,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(
                text = stringResource(R.string.action_close),
                style = typography.bodyLg,
            )
        }
    }
}

private fun formatFromMime(mimeType: String, type: MediaType): String {
    val subtype = mimeType.substringAfter('/', missingDelimiterValue = "")
        .uppercase(Locale.US)
        .takeIf { it.isNotBlank() && it != "*" }
    return when {
        subtype != null -> subtype
        type == MediaType.Video -> "VIDEO"
        else -> "IMAGE"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 500)
@Composable
private fun MediaDetailsSheetPreview() {
    GaliusTheme {
        DetailsSheetBody(
            item = MediaItem(
                id = "1",
                filePath = "",
                displayName = "Screenshot_20260921_135208_Chrome",
                mimeType = "image/jpeg",
                type = MediaType.Photo,
                createdAtMillis = System.currentTimeMillis(),
            ),
            assignedTags = listOf(
                Tag("t1", "c1", "Beach", TagColorKey.Sky, 0L),
                Tag("t2", "c1", "Family", TagColorKey.Rose, 0L),
            ),
            onAddTagClick = {},
            onRemoveTag = {},
            onDismiss = {},
        )
    }
}
