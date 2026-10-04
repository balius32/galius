package com.balius.galius.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.feature.settings.domain.model.LibraryStorageStats
import com.balius.galius.feature.settings.presentation.StorageSizeFormatter
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.PillShape

@Composable
fun StorageAllocationBar(
    stats: LibraryStorageStats,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val scheme = MaterialTheme.colorScheme
    val storageColors = GaliusThemeTokens.colors
    val total = stats.totalBytes.coerceAtLeast(1L)
    val photoWeight = (stats.photoBytes.toFloat() / total).coerceIn(0f, 1f)
    val videoWeight = (stats.videoBytes.toFloat() / total).coerceIn(0f, 1f)
    val dbWeight = (stats.dbBytes.toFloat() / total).coerceIn(0f, 1f)
    val photoLabel = StorageSizeFormatter.format(stats.photoBytes)
    val videoLabel = StorageSizeFormatter.format(stats.videoBytes)
    val dbLabel = StorageSizeFormatter.format(stats.dbBytes)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_storage_allocation),
                style = typography.bodySm,
                color = scheme.onSurface,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.settings_items_indexed,
                    stats.itemCount,
                    stats.itemCount,
                ),
                style = typography.labelNumeric,
                color = scheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(GaliusSpacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(PillShape)
                .background(scheme.surfaceContainerLowest)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (photoWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(photoWeight.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp))
                        .background(storageColors.storagePhoto),
                )
            }
            if (videoWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(videoWeight.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .background(storageColors.storageVideo),
                )
            }
            if (dbWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(dbWeight.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp))
                        .background(storageColors.storageDb),
                )
            }
            if (stats.totalBytes == 0L) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
        Spacer(modifier = Modifier.height(GaliusSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendItem(
                color = storageColors.storagePhoto,
                label = stringResource(R.string.settings_storage_photos, photoLabel),
            )
            LegendItem(
                color = storageColors.storageVideo,
                label = stringResource(R.string.settings_storage_videos, videoLabel),
            )
            LegendItem(
                color = storageColors.storageDb,
                label = stringResource(R.string.settings_storage_db, dbLabel),
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = GaliusThemeTokens.typography.bodySm,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
