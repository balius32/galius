package com.balius.galius.feature.media.presentation.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
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
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.balius.galius.R
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusThemeTokens
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.media3.common.MediaItem as ExoMediaItem

private const val DoubleTapSeekMs = 10_000L
private const val HoldSeekStepMs = 2_000L
private const val HoldSeekStepSeconds = 2
private val HoldDragDpPerStep = 48.dp

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
        onShowDoubleTapSeek = { forward ->
            viewModel.onIntent(VideoPlayerIntent.ShowDoubleTapSeek(forward))
        },
        onClearDoubleTapSeek = { viewModel.onIntent(VideoPlayerIntent.ClearDoubleTapSeek) },
        onHoldSeekChanged = { active, forward, seconds ->
            viewModel.onIntent(VideoPlayerIntent.HoldSeekChanged(active, forward, seconds))
        },
    )
}

@Composable
fun VideoPlayerScreen(
    state: VideoPlayerState,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleChrome: () -> Unit,
    onShowDoubleTapSeek: (forward: Boolean) -> Unit,
    onClearDoubleTapSeek: () -> Unit,
    onHoldSeekChanged: (active: Boolean, forward: Boolean, seconds: Int) -> Unit,
) {
    val pageCount = state.items.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pageCount },
    )
    val onPageChangedState = rememberUpdatedState(onPageChanged)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBase),
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
                userScrollEnabled = false,
                beyondViewportPageCount = 0,
                key = { page -> state.items.getOrNull(page)?.id ?: page },
            ) { page ->
                val item = state.items[page]
                val isCurrent = page == pagerState.settledPage
                if (isCurrent) {
                    ActiveVideoPlayerPage(
                        item = item,
                        chromeVisible = state.chromeVisible,
                        seekOverlaySeconds = state.seekOverlaySeconds,
                        seekOverlayForward = state.seekOverlayForward,
                        holdSeekActive = state.holdSeekActive,
                        onToggleChrome = onToggleChrome,
                        onShowDoubleTapSeek = onShowDoubleTapSeek,
                        onHoldSeekChanged = onHoldSeekChanged,
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            }
        }

        if (state.chromeVisible) {
            PlayerTopChrome(
                positionLabel = if (state.items.isEmpty()) {
                    null
                } else {
                    stringResource(
                        R.string.viewer_position,
                        (pagerState.currentPage + 1).coerceAtMost(state.items.size),
                        state.items.size,
                    )
                },
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun PlayerTopChrome(
    positionLabel: String?,
    onBack: () -> Unit,
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
                contentDescription = stringResource(R.string.player_close_cd),
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
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ActiveVideoPlayerPage(
    item: MediaItem,
    chromeVisible: Boolean,
    seekOverlaySeconds: Int?,
    seekOverlayForward: Boolean,
    holdSeekActive: Boolean,
    onToggleChrome: () -> Unit,
    onShowDoubleTapSeek: (forward: Boolean) -> Unit,
    onHoldSeekChanged: (active: Boolean, forward: Boolean, seconds: Int) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val player = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(Uri.fromFile(File(item.filePath))))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(item.id) {
        onDispose { player.release() }
    }

    var isPlaying by remember(item.id) { mutableStateOf(true) }
    var positionMs by remember(item.id) { mutableLongStateOf(0L) }
    var durationMs by remember(item.id) { mutableLongStateOf(0L) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = player.duration.coerceAtLeast(0L)
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player) {
        while (true) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            durationMs = player.duration.coerceAtLeast(0L)
            delay(250)
        }
    }

    fun seekBy(deltaMs: Long) {
        val dur = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (player.currentPosition + deltaMs).coerceIn(0L, dur)
        player.seekTo(target)
    }

    val onToggleChromeState = rememberUpdatedState(onToggleChrome)
    val onShowDoubleTapSeekState = rememberUpdatedState(onShowDoubleTapSeek)
    val onHoldSeekChangedState = rememberUpdatedState(onHoldSeekChanged)
    val dragThresholdPx = with(density) { HoldDragDpPerStep.toPx() }
    val viewConfiguration = LocalViewConfiguration.current
    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                }
            },
            update = { view ->
                if (view.player !== player) {
                    view.player = player
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(item.id, dragThresholdPx, doubleTapTimeout, longPressTimeout, touchSlop) {
                    var lastTapTime = 0L
                    var lastTapOffset = Offset.Zero

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val downTime = System.currentTimeMillis()
                        val downPosition = down.position
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

                        if (dragged) return@awaitEachGesture

                        val now = System.currentTimeMillis()
                        val isDoubleTap = now - lastTapTime <= doubleTapTimeout &&
                            (downPosition - lastTapOffset).getDistance() < touchSlop * 2
                        if (isDoubleTap) {
                            val forward = downPosition.x >= size.width / 2f
                            seekBy(if (forward) DoubleTapSeekMs else -DoubleTapSeekMs)
                            onShowDoubleTapSeekState.value(forward)
                            lastTapTime = 0L
                        } else {
                            lastTapTime = downTime
                            lastTapOffset = downPosition
                            onToggleChromeState.value()
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

        if (chromeVisible) {
            PlayerControls(
                isPlaying = isPlaying,
                positionMs = positionMs,
                durationMs = durationMs,
                onPlayPause = {
                    if (player.isPlaying) player.pause() else player.play()
                },
                onSeek = { target -> player.seekTo(target) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            )
        }
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
        if (!forward) {
            SeekIndicatorChip(
                forward = false,
                seconds = seconds,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(start = GaliusSpacing.lg),
                alpha = alpha,
            )
        } else {
            SeekIndicatorChip(
                forward = true,
                seconds = seconds,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = GaliusSpacing.lg),
                alpha = alpha,
            )
        }
    }
}

@Composable
private fun SeekIndicatorChip(
    forward: Boolean,
    seconds: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = alpha), CircleShape)
                .padding(GaliusSpacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (forward) Icons.Outlined.Forward10 else Icons.Outlined.Replay10,
                    contentDescription = stringResource(
                        if (forward) {
                            if (seconds <= 2) {
                                R.string.player_seek_forward_2_cd
                            } else {
                                R.string.viewer_seek_forward_cd
                            }
                        } else {
                            if (seconds <= 2) {
                                R.string.player_seek_back_2_cd
                            } else {
                                R.string.viewer_seek_back_cd
                            }
                        },
                    ),
                    tint = AccentCyan,
                    modifier = Modifier.size(36.dp),
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
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = GaliusSpacing.md, vertical = GaliusSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { value ->
                if (durationMs > 0) onSeek((value * durationMs).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(
                    R.string.viewer_time_position,
                    formatPlayerTime(positionMs),
                    formatPlayerTime(durationMs),
                ),
                style = GaliusThemeTokens.typography.bodySm,
                color = Color.White,
            )
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentCyan.copy(alpha = 0.2f), CircleShape),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(R.string.viewer_play_pause_cd),
                    tint = AccentCyan,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

private fun formatPlayerTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
