package com.balius.galius.feature.media.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.balius.galius.R
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.MediaCardShape
import java.io.File

/** Shared cell ratio so photo + video keep even gutters in a 2-column grid. */
const val MediaGridThumbAspectRatio = 3f / 4f

@Composable
fun MediaThumbCard(
    item: MediaItem,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectionMode: Boolean = false,
) {
    Box(
        modifier = modifier
            .aspectRatio(MediaGridThumbAspectRatio)
            .clip(MediaCardShape)
            .background(GaliusThemeTokens.colors.card)
            .then(
                if (selected) {
                    Modifier.border(2.dp, AccentCyan, MediaCardShape)
                } else {
                    Modifier
                },
            ),
    ) {
        AsyncImage(
            model = File(item.filePath),
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.type == MediaType.Video) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.action_play),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(GaliusSpacing.sm)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) AccentCyan else Color.Black.copy(alpha = 0.45f),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.home_item_selected_cd),
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun MediaThumbCardPreview() {
    GaliusTheme {
        MediaThumbCard(
            item = MediaItem(
                id = "preview",
                filePath = "",
                displayName = "Preview",
                mimeType = "image/jpeg",
                type = MediaType.Photo,
                createdAtMillis = 0L,
            ),
            selected = true,
            selectionMode = true,
            modifier = Modifier.width(160.dp),
        )
    }
}
