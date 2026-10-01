package com.balius.galius.feature.media.presentation.components

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.balius.galius.R
import com.balius.galius.common.ui.TagChip
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.AccentIndigo
import com.balius.galius.ui.theme.CardSurface
import com.balius.galius.ui.theme.ElevatedSurface
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.TagMint
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val SheetBackground = Color(0xFF151722)
private val MetaCardBackground = Color(0xFF202330)
private val SurfaceDark = Color(0xFF242735)
private val DetailsSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailsSheet(
    item: MediaItem,
    assignedTags: List<Tag>,
    onDismiss: () -> Unit,
    onOpenTagPicker: () -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    var favorited by remember(item.id) { mutableStateOf(false) }
    var resolutionLabel by remember(item.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(item.id, item.filePath) {
        resolutionLabel = if (item.type == MediaType.Photo) {
            readImageResolution(item.filePath)
        } else {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            AsyncImage(
                model = File(item.filePath),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.50f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GaliusSpacing.md, vertical = GaliusSpacing.sm),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f)),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                resolutionLabel?.let { label ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = GaliusSpacing.md)
                            .padding(bottom = GaliusSpacing.sm)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.70f))
                            .border(1.dp, GhostBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = label,
                            style = GaliusThemeTokens.typography.labelPill,
                            color = TagMint,
                        )
                    }
                }

                DetailsSheetBody(
                    item = item,
                    favorited = favorited,
                    assignedTags = assignedTags,
                    onFavoriteToggle = { favorited = !favorited },
                    onAddTagClick = onOpenTagPicker,
                    onRemoveTag = onRemoveTag,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSheetBody(
    item: MediaItem,
    favorited: Boolean,
    assignedTags: List<Tag>,
    onFavoriteToggle: () -> Unit,
    onAddTagClick: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val typography = GaliusThemeTokens.typography
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
            .clip(DetailsSheetShape)
            .background(SheetBackground)
            .border(1.dp, GhostBorder, DetailsSheetShape)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = GaliusSpacing.md + 4.dp)
            .padding(top = GaliusSpacing.lg, bottom = GaliusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md + 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = GaliusSpacing.sm)) {
                Text(
                    text = item.displayName,
                    style = typography.headlineMd,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = typeLabel,
                    style = typography.bodyLg,
                    color = Color(0xFF8F96A3),
                )
            }
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark.copy(alpha = 0.90f))
                    .border(1.dp, GhostBorder, CircleShape),
            ) {
                Icon(
                    imageVector = if (favorited) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.action_favorite),
                    tint = if (favorited) Color(0xFFEF4444) else Color.White,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MetaCardBackground)
                .border(1.dp, GhostBorder, RoundedCornerShape(16.dp))
                .padding(GaliusSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.media_date_added),
                    style = typography.bodySm,
                    color = Color(0xFF7D8494),
                )
                Text(
                    text = dateLabel,
                    style = typography.bodyMd,
                    color = Color.White,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.media_format),
                    style = typography.bodySm,
                    color = Color(0xFF7D8494),
                )
                Text(
                    text = formatLabel,
                    style = typography.bodyMd,
                    color = Color.White,
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
                        color = Color(0xFF7D8494),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ElevatedSurface)
                            .border(1.dp, GhostBorder, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.media_tags_assigned, assignedTags.size),
                            style = typography.labelPill,
                            color = Color(0xFF9CA3AF),
                        )
                    }
                }
                if (assignedTags.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.media_tags_hint),
                        style = typography.labelPill,
                        color = Color(0xFF6B7280),
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardSurface.copy(alpha = 0.70f))
                        .border(1.dp, GhostBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddTagClick)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.media_add_tag),
                        style = typography.bodyMd,
                        color = Color(0xFF656B7C),
                    )
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentIndigo,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.action_close),
                style = typography.bodyLg,
            )
        }
    }
}

private fun readImageResolution(path: String): String? =
    runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        if (options.outWidth > 0 && options.outHeight > 0) {
            "${options.outWidth}x${options.outHeight}"
        } else {
            null
        }
    }.getOrNull()

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

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 800)
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
            favorited = false,
            assignedTags = listOf(
                Tag("t1", "c1", "Beach", TagColorKey.Sky, 0L),
                Tag("t2", "c1", "Family", TagColorKey.Rose, 0L),
            ),
            onFavoriteToggle = {},
            onAddTagClick = {},
            onRemoveTag = {},
            onDismiss = {},
        )
    }
}
