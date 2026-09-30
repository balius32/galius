package com.balius.galius.feature.more.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.ui.theme.AccentCyan
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreRoute(
    contentBottomPadding: Dp,
    viewModel: MoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MoreScreen(
        state = state,
        onToggleAppLock = { viewModel.onIntent(MoreIntent.ToggleAppLock(it)) },
        contentBottomPadding = contentBottomPadding,
    )
}

@Composable
fun MoreScreen(
    state: MoreState,
    onToggleAppLock: (Boolean) -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBase)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = contentBottomPadding)
            .padding(horizontal = GaliusSpacing.margin),
    ) {
        Spacer(modifier = Modifier.height(GaliusSpacing.sm))
        Text(
            text = stringResource(R.string.more_title),
            style = typography.headlineLgMobile,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))

        SectionLabel(text = stringResource(R.string.more_security))
        MoreToggleRow(
            title = stringResource(R.string.more_app_lock),
            subtitle = stringResource(R.string.more_app_lock_subtitle),
            checked = state.appLockEnabled,
            onCheckedChange = onToggleAppLock,
        )

        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
        HorizontalDivider(color = GhostBorder)
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))

        SectionLabel(text = stringResource(R.string.more_library))
        MoreNavRow(title = stringResource(R.string.more_manage_categories))
        MoreNavRow(title = stringResource(R.string.more_manage_tags))
        MoreNavRow(title = stringResource(R.string.action_import))

        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
        HorizontalDivider(color = GhostBorder)
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))

        SectionLabel(text = stringResource(R.string.more_about))
        Text(
            text = stringResource(R.string.more_version, "1.0"),
            style = typography.bodySm,
            color = colors.metadataCaption,
            modifier = Modifier.padding(vertical = GaliusSpacing.sm),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = GaliusThemeTokens.typography.labelPill,
        color = AccentCyan,
        modifier = Modifier.padding(bottom = GaliusSpacing.sm),
    )
}

@Composable
private fun MoreToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GaliusSpacing.sm),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.bodyLg, color = Color.White)
            Text(text = subtitle, style = typography.bodySm, color = colors.metadataDescription)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MoreNavRow(title: String) {
    Text(
        text = title,
        style = GaliusThemeTokens.typography.bodyLg,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GaliusSpacing.md),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 800)
@Composable
private fun MoreScreenPreview() {
    GaliusTheme {
        MoreScreen(
            state = MoreState(),
            onToggleAppLock = {},
            contentBottomPadding = 96.dp,
        )
    }
}
