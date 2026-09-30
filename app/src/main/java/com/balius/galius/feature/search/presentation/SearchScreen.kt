package com.balius.galius.feature.search.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.ui.theme.AccentIndigo
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.Outline
import com.balius.galius.ui.theme.PillShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    contentBottomPadding: Dp,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onQueryChange = { viewModel.onIntent(SearchIntent.QueryChanged(it)) },
        contentBottomPadding = contentBottomPadding,
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    onQueryChange: (String) -> Unit,
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
            text = stringResource(R.string.nav_search),
            style = typography.headlineLgMobile,
            color = androidx.compose.ui.graphics.Color.White,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.md))
        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = GaliusSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.search_empty_title),
                style = typography.headlineSm,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(GaliusSpacing.sm))
            Text(
                text = stringResource(R.string.search_empty_body),
                style = typography.bodyMd,
                color = colors.metadataDescription,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val focused = query.isNotEmpty()

    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(GaliusSpacing.searchBarHeight)
            .background(colors.searchField, PillShape)
            .border(
                width = 1.dp,
                color = if (focused) AccentIndigo else GhostBorder,
                shape = PillShape,
            )
            .padding(horizontal = GaliusSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = stringResource(R.string.action_search),
            tint = Outline,
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typography.bodyMd.copy(color = androidx.compose.ui.graphics.Color.White),
            cursorBrush = SolidColor(AccentIndigo),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = typography.bodyMd,
                        color = Outline,
                    )
                }
                inner()
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 800)
@Composable
private fun SearchScreenPreview() {
    GaliusTheme {
        SearchScreen(
            state = SearchState(),
            onQueryChange = {},
            contentBottomPadding = 96.dp,
        )
    }
}
