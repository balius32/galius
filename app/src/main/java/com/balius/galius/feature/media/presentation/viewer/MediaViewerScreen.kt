package com.balius.galius.feature.media.presentation.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.balius.galius.R
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.presentation.components.MediaDetailsSheet
import com.balius.galius.feature.media.presentation.components.MediaTagPickerSheet
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusThemeTokens
import java.io.File
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MediaViewerRoute(
    startMediaId: String,
    source: MediaBrowseSource,
    onBack: () -> Unit,
    onOpenVideoPlayer: (mediaId: String) -> Unit,
    viewModel: MediaViewerViewModel = koinViewModel(
        key = "viewer-$startMediaId-$source",
    ) {
        parametersOf(startMediaId, source)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MediaViewerScreen(
        state = state,
        onBack = onBack,
        onPageChanged = { viewModel.onIntent(MediaViewerIntent.PageChanged(it)) },
        onToggleChrome = { viewModel.onIntent(MediaViewerIntent.ToggleChrome) },
        onOpenDetails = { viewModel.onIntent(MediaViewerIntent.OpenDetails) },
        onCloseDetails = { viewModel.onIntent(MediaViewerIntent.CloseDetails) },
        onOpenTagPicker = { viewModel.onIntent(MediaViewerIntent.OpenTagPicker) },
        onCloseTagPicker = { viewModel.onIntent(MediaViewerIntent.CloseTagPicker) },
        onAddTag = { viewModel.onIntent(MediaViewerIntent.AddTag(it)) },
        onRemoveTag = { viewModel.onIntent(MediaViewerIntent.RemoveTag(it)) },
        onOpenVideoPlayer = onOpenVideoPlayer,
    )
}

@Composable
fun MediaViewerScreen(
    state: MediaViewerState,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleChrome: () -> Unit,
    onOpenDetails: () -> Unit,
    onCloseDetails: () -> Unit,
    onOpenTagPicker: () -> Unit,
    onCloseTagPicker: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onOpenVideoPlayer: (mediaId: String) -> Unit,
) {
    val pageCount = state.items.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount },
    )
    var isPageZoomed by remember { mutableStateOf(false) }
    val onPageChangedState = rememberUpdatedState(onPageChanged)

    // Jump once when items first arrive (or list identity changes after empty).
    LaunchedEffect(state.items.map { it.id }) {
        if (state.items.isEmpty()) return@LaunchedEffect
        val target = state.currentIndex.coerceIn(0, state.items.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    // Pager is source of truth for swipes — push index to VM without scrolling back.
    LaunchedEffect(pagerState, state.items.size) {
        if (state.items.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChangedState.value(page)
            }
    }

    // Clear zoom lock when page settles so swipe never stays disabled.
    LaunchedEffect(pagerState.settledPage) {
        isPageZoomed = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GaliusThemeTokens.colors.canvas),
    ) {
        if (state.items.isEmpty()) {
            Text(
                text = stringResource(R.string.search_no_media_in_filter),
                color = GaliusThemeTokens.colors.metadataDescription,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isPageZoomed,
                beyondViewportPageCount = 1,
                key = { page -> state.items.getOrNull(page)?.id ?: page },
            ) { page ->
                val item = state.items[page]
                val isCurrent = page == pagerState.settledPage
                when (item.type) {
                    MediaType.Photo -> ZoomablePhotoPage(
                        item = item,
                        onTap = onToggleChrome,
                        onZoomChanged = { zoomed ->
                            if (isCurrent) isPageZoomed = zoomed
                        },
                    )
                    MediaType.Video -> VideoPreviewPage(
                        item = item,
                        onToggleChrome = onToggleChrome,
                    )
                }
            }
        }

        if (state.chromeVisible) {
            val current = state.items.getOrNull(pagerState.currentPage)
            ViewerTopChrome(
                positionLabel = if (state.items.isEmpty()) {
                    null
                } else {
                    stringResource(
                        R.string.viewer_position,
                        (pagerState.currentPage + 1).coerceAtMost(state.items.size),
                        state.items.size,
                    )
                },
                showOpenVideoPlayer = current?.type == MediaType.Video,
                onBack = onBack,
                onOpenVideoPlayer = {
                    current?.takeIf { it.type == MediaType.Video }?.let { onOpenVideoPlayer(it.id) }
                },
                onOpenDetails = onOpenDetails,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        state.currentItem?.takeIf { state.showDetails }?.let { item ->
            MediaDetailsSheet(
                item = item,
                assignedTags = state.detailsAssignedTags,
                onDismiss = onCloseDetails,
                onOpenTagPicker = onOpenTagPicker,
                onRemoveTag = onRemoveTag,
            )
        }
        if (state.showTagPicker && state.currentItem != null) {
            MediaTagPickerSheet(
                categories = state.allCategories,
                assignedTagIds = state.detailsAssignedTags.map { it.id }.toSet(),
                onToggleTag = { tagId, assigned ->
                    if (assigned) onRemoveTag(tagId) else onAddTag(tagId)
                },
                onDismiss = onCloseTagPicker,
            )
        }
    }
}

@Composable
private fun ViewerTopChrome(
    positionLabel: String?,
    showOpenVideoPlayer: Boolean,
    onBack: () -> Unit,
    onOpenVideoPlayer: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = GaliusSpacing.sm, vertical = GaliusSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.viewer_close_cd),
                tint = Color.White,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (positionLabel != null) {
            Text(
                text = positionLabel,
                style = GaliusThemeTokens.typography.bodyMd,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (showOpenVideoPlayer) {
            IconButton(onClick = onOpenVideoPlayer) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.viewer_open_video_player_cd),
                    tint = Color.White,
                )
            }
        }
        IconButton(onClick = onOpenDetails) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.viewer_info_cd),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun ZoomablePhotoPage(
    item: MediaItem,
    onTap: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
) {
    var scale by remember(item.id) { mutableFloatStateOf(1f) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    val zoomed = scale > 1.01f
    val onZoomChangedState = rememberUpdatedState(onZoomChanged)

    LaunchedEffect(item.id, zoomed) {
        onZoomChangedState.value(zoomed)
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val next = (scale * zoomChange).coerceIn(1f, 4f)
        scale = next
        offset = if (next > 1.01f) offset + panChange else Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.id) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                    onTap = { onTap() },
                )
            }
            // Only steal gestures when zoomed — otherwise pager swipe freezes.
            .transformable(
                state = transformState,
                enabled = zoomed,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = File(item.filePath),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

@Composable
private fun VideoPreviewPage(
    item: MediaItem,
    onToggleChrome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleChrome),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = File(item.filePath),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
