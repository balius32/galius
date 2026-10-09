package com.balius.galius.feature.media.presentation.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Forward5
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.Replay5
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import coil3.compose.AsyncImage
import com.balius.galius.R
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.media3.common.MediaItem as ExoMediaItem

private const val DoubleTapSeekMs = 10_000L
private const val HoldSeekStepMs = 2_000L
private const val HoldSeekStepSeconds = 2
private val HoldDragDpPerStep = 48.dp
private val SeekIconSize = 28.dp

private data class PlaybackSnapshot(
    val player: ExoPlayer,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val isBuffering: Boolean,
)

@Composable
fun VideoPlayerRoute(
    startMediaId: String,
    source: MediaBrowseSource,
    onBack: () -> Unit,
    viewModel: VideoPlayerViewModel = koinViewModel(
        key = "player-$startMediaId-$source",
    ) {
        parametersOf(startMediaId, source)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VideoPlayerScreen(
        state = state,
        onBack = onBack,
        onPageChanged = { viewModel.onIntent(VideoPlayerIntent.PageChanged(it)) },
        onToggleChrome = { viewModel.onIntent(VideoPlayerIntent.ToggleChrome) },
        onToggleResize = { viewModel.onIntent(VideoPlayerIntent.ToggleResize) },
        onShowDoubleTapSeek = { forward ->
            viewModel.onIntent(VideoPlayerIntent.ShowDoubleTapSeek(forward))
        },
        onClearDoubleTapSeek = { viewModel.onIntent(VideoPlayerIntent.ClearDoubleTapSeek) },
        onHoldSeekChanged = { active, forward, seconds ->
            viewModel.onIntent(VideoPlayerIntent.HoldSeekChanged(active, forward, seconds))
        },
        resumePositionFor = viewModel::resumePositionFor,
        resumePlayWhenReadyFor = viewModel::resumePlayWhenReadyFor,
        onPlaybackSample = viewModel::onPlaybackSample,
    )
}

@Composable
fun VideoPlayerScreen(
    state: VideoPlayerState,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleChrome: () -> Unit,
    onToggleResize: () -> Unit,
    onShowDoubleTapSeek: (forward: Boolean) -> Unit,
    onClearDoubleTapSeek: () -> Unit,
    onHoldSeekChanged: (active: Boolean, forward: Boolean, seconds: Int) -> Unit,
    resumePositionFor: (String) -> Long,
    resumePlayWhenReadyFor: (String) -> Boolean,
    onPlaybackSample: (mediaId: String, positionMs: Long, playWhenReady: Boolean, ready: Boolean) -> Unit,
) {
    val pageCount = state.items.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount },
    )
    val onPageChangedState = rememberUpdatedState(onPageChanged)
    val contentScale = if (state.resizeToFill) ContentScale.Crop else ContentScale.Fit
    val settledItem = state.items.getOrNull(pagerState.settledPage)
    val playback = rememberPlayback(
        item = settledItem,
        resumePositionMs = settledItem?.id?.let(resumePositionFor) ?: 0L,
        startWhenReady = settledItem?.id?.let(resumePlayWhenReadyFor) ?: true,
        onPlaybackSample = onPlaybackSample,
    )

    LaunchedEffect(state.items.map { it.id }) {
        if (state.items.isEmpty()) return@LaunchedEffect
        val target = state.currentIndex.coerceIn(0, state.items.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState, state.items.size) {
        if (state.items.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChangedState.value(page)
            }
    }

    LaunchedEffect(state.seekOverlaySeconds, state.holdSeekActive) {
        if (state.seekOverlaySeconds != null && !state.holdSeekActive) {
            delay(700)
            onClearDoubleTapSeek()
        }
    }

    val currentItem = state.items.getOrNull(pagerState.currentPage)
    val positionLabel = if (state.items.size > 1) {
        stringResource(
            R.string.viewer_position,
            (pagerState.currentPage + 1).coerceAtMost(state.items.size),
            state.items.size,
        )
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (state.items.isEmpty()) {
            Text(
                text = stringResource(R.string.search_no_media_in_filter),
                color = GaliusThemeTokens.colors.metadataDescription,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            VideoPager(
                state = state,
                pagerState = pagerState,
                contentScale = contentScale,
                playback = playback,
                onToggleChrome = onToggleChrome,
                onShowDoubleTapSeek = onShowDoubleTapSeek,
                onHoldSeekChanged = onHoldSeekChanged,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (state.chromeVisible) {
            PlayerTopChrome(
                title = currentItem?.displayName,
                positionLabel = positionLabel,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            playback?.let { snapshot ->
                PlayerControls(
                    isPlaying = snapshot.isPlaying,
                    positionMs = snapshot.positionMs,
                    durationMs = snapshot.durationMs,
                    resizeToFill = state.resizeToFill,
                    onPlayPause = { snapshot.player.togglePlay() },
                    onSeek = { target -> snapshot.player.seekTo(target) },
                    onToggleResize = onToggleResize,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                )
            }
        }
    }
}

@Composable
private fun rememberPlayback(
    item: MediaItem?,
    resumePositionMs: Long,
    startWhenReady: Boolean,
    onPlaybackSample: (mediaId: String, positionMs: Long, playWhenReady: Boolean, ready: Boolean) -> Unit,
): PlaybackSnapshot? {
    val context = LocalContext.current
    val itemId = item?.id
    val player = remember(itemId) {
        val current = item ?: return@remember null
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(Uri.fromFile(File(current.filePath))))
            if (resumePositionMs > 0L) seekTo(resumePositionMs)
            prepare()
            playWhenReady = startWhenReady
        }
    }
    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    var isPlaying by remember(itemId) { mutableStateOf(startWhenReady) }
    var positionMs by remember(itemId) { mutableLongStateOf(resumePositionMs) }
    var durationMs by remember(itemId) { mutableLongStateOf(-1L) }
    var isBuffering by remember(itemId) { mutableStateOf(false) }

    DisposableEffect(player) {
        if (player == null) {
            onDispose { }
        } else {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    durationMs = player.duration.takeIf { it > 0L } ?: -1L
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
            }
            player.addListener(listener)
            isPlaying = player.isPlaying
            durationMs = player.duration.takeIf { it > 0L } ?: -1L
            isBuffering = player.playbackState == Player.STATE_BUFFERING
            onDispose { player.removeListener(listener) }
        }
    }

    LaunchedEffect(player, itemId) {
        if (player == null || itemId == null) return@LaunchedEffect
        while (true) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            durationMs = player.duration.takeIf { it > 0L } ?: -1L
            onPlaybackSample(
                itemId,
                positionMs,
                player.playWhenReady,
                player.playbackState == Player.STATE_READY,
            )
            delay(250)
        }
    }

    val active = player ?: return null
    return PlaybackSnapshot(
        player = active,
        isPlaying = isPlaying,
        positionMs = positionMs,
        durationMs = durationMs,
        isBuffering = isBuffering,
    )
}

@Composable
private fun VideoPager(
    state: VideoPlayerState,
    pagerState: PagerState,
    contentScale: ContentScale,
    playback: PlaybackSnapshot?,
    onToggleChrome: () -> Unit,
    onShowDoubleTapSeek: (forward: Boolean) -> Unit,
    onHoldSeekChanged: (active: Boolean, forward: Boolean, seconds: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = !state.holdSeekActive,
        beyondViewportPageCount = 0,
        key = { page -> state.items.getOrNull(page)?.id ?: page },
    ) { page ->
        val item = state.items[page]
        val isSettled = page == pagerState.settledPage
        if (isSettled && playback != null) {
            ActiveVideoPage(
                item = item,
                playback = playback,
                contentScale = contentScale,
                seekOverlaySeconds = state.seekOverlaySeconds,
                seekOverlayForward = state.seekOverlayForward,
                holdSeekActive = state.holdSeekActive,
                onToggleChrome = onToggleChrome,
                onShowDoubleTapSeek = onShowDoubleTapSeek,
                onHoldSeekChanged = onHoldSeekChanged,
            )
        } else {
            AsyncImage(
                model = File(item.filePath),
                contentDescription = item.displayName,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ActiveVideoPage(
    item: MediaItem,
    playback: PlaybackSnapshot,
    contentScale: ContentScale,
    seekOverlaySeconds: Int?,
    seekOverlayForward: Boolean,
    holdSeekActive: Boolean,
    onToggleChrome: () -> Unit,
    onShowDoubleTapSeek: (forward: Boolean) -> Unit,
    onHoldSeekChanged: (active: Boolean, forward: Boolean, seconds: Int) -> Unit,
) {
    val player = playback.player
    val density = LocalDensity.current
    val onToggleChromeState = rememberUpdatedState(onToggleChrome)
    val onShowDoubleTapSeekState = rememberUpdatedState(onShowDoubleTapSeek)
    val onHoldSeekChangedState = rememberUpdatedState(onHoldSeekChanged)
    val dragThresholdPx = with(density) { HoldDragDpPerStep.toPx() }
    val viewConfiguration = LocalViewConfiguration.current
    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop
    val bufferingDescription = stringResource(R.string.player_buffering_cd)

    fun seekBy(deltaMs: Long) {
        val dur = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (player.currentPosition + deltaMs).coerceIn(0L, dur)
        player.seekTo(target)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = item.displayName },
    ) {
        ContentFrame(
            player = player,
            modifier = Modifier.fillMaxSize(),
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            contentScale = contentScale,
            shutter = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                )
            },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    item.id,
                    dragThresholdPx,
                    doubleTapTimeout,
                    longPressTimeout,
                    touchSlop,
                ) {
                    var lastUpTime = 0L
                    var lastUpOffset = Offset.Zero
                    var singleTapJob: Job? = null
                    val inputScope = this
                    coroutineScope {
                        val scope = this
                        inputScope.awaitEachGesture {
                            val down = awaitFirstDown()
                            val downTime = System.currentTimeMillis()
                            val downPosition = down.position
                            val isDoubleTap = lastUpTime != 0L &&
                                downTime - lastUpTime <= doubleTapTimeout &&
                                (downPosition - lastUpOffset).getDistance() < touchSlop * 2
                            if (isDoubleTap) {
                                singleTapJob?.cancel()
                                singleTapJob = null
                            }
                            var totalDx = 0f
                            var appliedSteps = 0
                            var dragged = false
                            var pointerUp = false

                            val timedOut = withTimeoutOrNull(longPressTimeout) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: run {
                                            pointerUp = true
                                            return@withTimeoutOrNull false
                                        }
                                    if (change.changedToUp()) {
                                        pointerUp = true
                                        return@withTimeoutOrNull false
                                    }
                                    val dx = change.position.x - downPosition.x
                                    val dy = change.position.y - downPosition.y
                                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                                        dragged = true
                                        return@withTimeoutOrNull false
                                    }
                                }
                            } == null

                            if (timedOut && !dragged && !pointerUp) {
                                onHoldSeekChangedState.value(true, true, HoldSeekStepSeconds)
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUp()) {
                                        onHoldSeekChangedState.value(false, true, 0)
                                        break
                                    }
                                    val dx = change.positionChange().x
                                    if (dx != 0f) {
                                        change.consume()
                                        totalDx += dx
                                        val steps = (totalDx / dragThresholdPx).roundToInt()
                                        val deltaSteps = steps - appliedSteps
                                        if (deltaSteps != 0) {
                                            seekBy(deltaSteps * HoldSeekStepMs)
                                            appliedSteps = steps
                                            val seconds = abs(steps) * HoldSeekStepSeconds
                                            onHoldSeekChangedState.value(
                                                true,
                                                steps >= 0,
                                                seconds.coerceAtLeast(HoldSeekStepSeconds),
                                            )
                                        } else {
                                            onHoldSeekChangedState.value(
                                                true,
                                                totalDx >= 0f,
                                                HoldSeekStepSeconds,
                                            )
                                        }
                                    }
                                }
                                return@awaitEachGesture
                            }

                            if (!pointerUp) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUp()) break
                                    val dx = change.position.x - downPosition.x
                                    val dy = change.position.y - downPosition.y
                                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                                        dragged = true
                                    }
                                }
                            }

                            if (dragged) {
                                if (isDoubleTap) lastUpTime = 0L
                                return@awaitEachGesture
                            }

                            if (isDoubleTap) {
                                val forward = downPosition.x >= size.width / 2f
                                seekBy(if (forward) DoubleTapSeekMs else -DoubleTapSeekMs)
                                onShowDoubleTapSeekState.value(forward)
                                lastUpTime = 0L
                            } else {
                                lastUpTime = System.currentTimeMillis()
                                lastUpOffset = downPosition
                                singleTapJob?.cancel()
                                singleTapJob = scope.launch {
                                    delay(doubleTapTimeout)
                                    onToggleChromeState.value()
                                }
                            }
                        }
                    }
                },
        )

        SeekSideIndicators(
            seconds = seekOverlaySeconds,
            forward = seekOverlayForward,
            persistent = holdSeekActive,
            modifier = Modifier.fillMaxSize(),
        )

        if (playback.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .semantics { contentDescription = bufferingDescription },
                color = GaliusThemeTokens.colors.accentCyan,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
private fun PlayerTopChrome(
    title: String?,
    positionLabel: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = GaliusSpacing.xs, vertical = GaliusSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.player_close_cd),
                tint = Color.White,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = GaliusThemeTokens.typography.bodyMd,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (positionLabel != null) {
                Text(
                    text = positionLabel,
                    style = GaliusThemeTokens.typography.bodySm,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun SeekSideIndicators(
    seconds: Int?,
    forward: Boolean,
    persistent: Boolean,
    modifier: Modifier = Modifier,
) {
    if (seconds == null) return
    val alpha = if (persistent) 0.85f else 0.75f
    Box(modifier = modifier) {
        SeekIndicatorChip(
            forward = forward,
            seconds = seconds,
            alpha = alpha,
            modifier = Modifier
                .align(if (forward) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = GaliusSpacing.lg),
        )
    }
}

@Composable
private fun SeekIndicatorChip(
    forward: Boolean,
    seconds: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = alpha), CircleShape)
            .padding(GaliusSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = seekIcon(forward, seconds),
                contentDescription = seekContentDescription(forward, seconds),
                tint = AccentCyan,
                modifier = Modifier.size(SeekIconSize),
            )
            Text(
                text = stringResource(
                    if (forward) {
                        R.string.player_seek_forward_hint
                    } else {
                        R.string.player_seek_back_hint
                    },
                    seconds,
                ),
                style = GaliusThemeTokens.typography.bodyMd,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun seekContentDescription(forward: Boolean, seconds: Int): String {
    return when {
        seconds == 10 && forward -> stringResource(R.string.viewer_seek_forward_cd)
        seconds == 10 && !forward -> stringResource(R.string.viewer_seek_back_cd)
        seconds <= 2 && forward -> stringResource(R.string.player_seek_forward_2_cd)
        seconds <= 2 && !forward -> stringResource(R.string.player_seek_back_2_cd)
        forward -> stringResource(R.string.player_seek_forward_seconds_cd, seconds)
        else -> stringResource(R.string.player_seek_back_seconds_cd, seconds)
    }
}

private fun seekIcon(forward: Boolean, seconds: Int): ImageVector = when {
    seconds == 10 && forward -> Icons.Outlined.Forward10
    seconds == 10 && !forward -> Icons.Outlined.Replay10
    seconds == 5 && forward -> Icons.Outlined.Forward5
    seconds == 5 && !forward -> Icons.Outlined.Replay5
    forward -> Icons.Outlined.FastForward
    else -> Icons.Outlined.FastRewind
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    resizeToFill: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleResize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0L) positionMs.toFloat() / durationMs.toFloat() else 0f
    val durationLabel = if (durationMs > 0L) {
        formatPlayerTime(durationMs)
    } else {
        stringResource(R.string.player_duration_unknown)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GaliusSpacing.md, vertical = GaliusSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatPlayerTime(positionMs),
                style = GaliusThemeTokens.typography.bodySm,
                color = Color.White,
            )
            Slider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { value ->
                    if (durationMs > 0L) onSeek((value * durationMs).toLong())
                },
                enabled = durationMs > 0L,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = GaliusSpacing.sm),
                colors = SliderDefaults.colors(
                    thumbColor = AccentCyan,
                    activeTrackColor = AccentCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                ),
            )
            Text(
                text = durationLabel,
                style = GaliusThemeTokens.typography.bodySm,
                color = Color.White,
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.viewer_play_pause_cd),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            IconButton(
                onClick = onToggleResize,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    imageVector = if (resizeToFill) {
                        Icons.Outlined.FitScreen
                    } else {
                        Icons.Outlined.Fullscreen
                    },
                    contentDescription = stringResource(
                        if (resizeToFill) {
                            R.string.player_fit_screen_cd
                        } else {
                            R.string.player_fill_screen_cd
                        },
                    ),
                    tint = Color.White,
                )
            }
        }
    }
}

private fun ExoPlayer.togglePlay() {
    if (isPlaying) pause() else play()
}

private fun formatPlayerTime(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0L))
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 720)
@Composable
private fun VideoPlayerScreenPreview() {
    GaliusTheme(darkTheme = true) {
        VideoPlayerScreen(
            state = VideoPlayerState(
                startMediaId = "preview",
                items = listOf(previewVideo("preview")),
                chromeVisible = true,
                seekOverlaySeconds = 2,
                seekOverlayForward = true,
            ),
            onBack = {},
            onPageChanged = {},
            onToggleChrome = {},
            onToggleResize = {},
            onShowDoubleTapSeek = {},
            onClearDoubleTapSeek = {},
            onHoldSeekChanged = { _, _, _ -> },
            resumePositionFor = { 0L },
            resumePlayWhenReadyFor = { true },
            onPlaybackSample = { _, _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerTopChromePreview() {
    GaliusTheme(darkTheme = true) {
        PlayerTopChrome(
            title = "Preview",
            positionLabel = "2 / 8",
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 360, heightDp = 200)
@Composable
private fun SeekSideIndicatorsPreview() {
    GaliusTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            SeekSideIndicators(
                seconds = 2,
                forward = true,
                persistent = true,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerControlsPreview() {
    GaliusTheme(darkTheme = true) {
        PlayerControls(
            isPlaying = true,
            positionMs = 3_725_000L,
            durationMs = 7_540_000L,
            resizeToFill = false,
            onPlayPause = {},
            onSeek = {},
            onToggleResize = {},
        )
    }
}

private fun previewVideo(id: String): MediaItem = MediaItem(
    id = id,
    filePath = "/preview.mp4",
    displayName = "Preview",
    mimeType = "video/mp4",
    type = MediaType.Video,
    createdAtMillis = 0L,
)
