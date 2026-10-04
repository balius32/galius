package com.balius.galius.feature.tags.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.common.ui.tagChipColors
import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.InputShape
import com.balius.galius.ui.theme.PillShape
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageTagsRoute(
    onBack: () -> Unit,
    contentBottomPadding: Dp,
    viewModel: ManageTagsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ManageTagsEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ManageTagsScreen(
            state = state,
            onBack = onBack,
            onCategoryNameChange = { viewModel.onIntent(ManageTagsIntent.CategoryNameChanged(it)) },
            onCategoryColorChange = { viewModel.onIntent(ManageTagsIntent.CategoryColorChanged(it)) },
            onSaveCategory = { viewModel.onIntent(ManageTagsIntent.SaveCategory) },
            onOpenAddTag = { viewModel.onIntent(ManageTagsIntent.OpenAddTag(it)) },
            onDismissAddTag = { viewModel.onIntent(ManageTagsIntent.DismissAddTag) },
            onTagDraftChange = { viewModel.onIntent(ManageTagsIntent.TagDraftChanged(it)) },
            onSaveTag = { viewModel.onIntent(ManageTagsIntent.SaveTag) },
            onDeleteTag = { viewModel.onIntent(ManageTagsIntent.DeleteTag(it)) },
            contentBottomPadding = contentBottomPadding,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = contentBottomPadding + GaliusSpacing.sm),
        )
    }
}

@Composable
fun ManageTagsScreen(
    state: ManageTagsState,
    onBack: () -> Unit,
    onCategoryNameChange: (String) -> Unit,
    onCategoryColorChange: (TagColorKey) -> Unit,
    onSaveCategory: () -> Unit,
    onOpenAddTag: (String) -> Unit,
    onDismissAddTag: () -> Unit,
    onTagDraftChange: (String) -> Unit,
    onSaveTag: () -> Unit,
    onDeleteTag: (String) -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
            .padding(horizontal = GaliusSpacing.margin)
            .padding(bottom = contentBottomPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(GaliusSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_close),
                    tint = scheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.tags_label),
                style = typography.headlineLgMobile,
                color = scheme.onSurface,
            )
        }

        CreateCategoryCard(
            name = state.categoryName,
            selectedColor = state.categoryColor,
            isSaving = state.isSaving,
            onNameChange = onCategoryNameChange,
            onColorChange = onCategoryColorChange,
            onSave = onSaveCategory,
        )

        if (state.categories.isEmpty()) {
            Text(
                text = stringResource(R.string.categories_empty),
                style = typography.bodyMd,
                color = colors.metadataDescription,
            )
        } else {
            state.categories.forEach { item ->
                CategoryManageCard(
                    item = item,
                    isAddingTag = state.addTagCategoryId == item.category.id,
                    tagDraft = if (state.addTagCategoryId == item.category.id) {
                        state.tagDraftName
                    } else {
                        ""
                    },
                    isSaving = state.isSaving,
                    onAddTag = { onOpenAddTag(item.category.id) },
                    onDismissAddTag = onDismissAddTag,
                    onTagDraftChange = onTagDraftChange,
                    onSaveTag = onSaveTag,
                    onDeleteTag = onDeleteTag,
                )
            }
        }
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
    }
}

@Composable
private fun CreateCategoryCard(
    name: String,
    selectedColor: TagColorKey,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onColorChange: (TagColorKey) -> Unit,
    onSave: () -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(GaliusRadius.lg)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ghostBorder, shape)
            .padding(GaliusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(GaliusRadius.default))
                    .background(colors.accentIndigo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderSpecial,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.create_category),
                    style = typography.headlineSm,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.manage_create_category_subtitle),
                    style = typography.bodySm,
                    color = colors.metadataDescription,
                )
            }
        }

        Text(
            text = stringResource(R.string.manage_category_title_label),
            style = typography.labelPill,
            color = colors.metadataCaption,
        )
        DraftField(
            value = name,
            hint = stringResource(R.string.manage_category_name_hint),
            onValueChange = onNameChange,
        )

        Text(
            text = stringResource(R.string.manage_accent_label),
            style = typography.labelPill,
            color = colors.metadataCaption,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
        ) {
            TagColorKey.categoryAccents.forEach { key ->
                AccentSwatch(
                    colorKey = key,
                    selected = key == selectedColor,
                    onClick = { onColorChange(key) },
                )
            }
        }

        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentIndigo,
                contentColor = scheme.onPrimaryContainer,
            ),
            shape = RoundedCornerShape(GaliusRadius.md),
        ) {
            Text(text = stringResource(R.string.action_save))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryManageCard(
    item: CategoryWithTags,
    isAddingTag: Boolean,
    tagDraft: String,
    isSaving: Boolean,
    onAddTag: () -> Unit,
    onDismissAddTag: () -> Unit,
    onTagDraftChange: (String) -> Unit,
    onSaveTag: () -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val accent = tagChipColors(item.category.colorKey)
    val shape = RoundedCornerShape(GaliusRadius.lg)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .border(1.dp, colors.ghostBorder, shape)
            .padding(GaliusSpacing.md),
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(GaliusRadius.md))
                    .background(accent.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderSpecial,
                    contentDescription = null,
                    tint = accent.content,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                ) {
                    Text(
                        text = item.category.name,
                        style = typography.headlineSm,
                        color = scheme.onSurface,
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent.content),
                    )
                }
                Text(
                    text = pluralStringResource(
                        R.plurals.manage_tags_count,
                        item.tags.size,
                        item.tags.size,
                    ),
                    style = typography.bodySm,
                    color = colors.metadataCaption,
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
        ) {
            item.tags.forEach { tag ->
                ManageTagChip(
                    name = tag.name,
                    colorKey = item.category.colorKey,
                    onRemove = { onDeleteTag(tag.id) },
                )
            }
            Box(
                modifier = Modifier
                    .height(GaliusSpacing.tagPillHeight)
                    .clip(PillShape)
                    .background(colors.elevated)
                    .border(1.dp, colors.ghostBorder, PillShape)
                    .clickable(onClick = onAddTag)
                    .padding(horizontal = GaliusSpacing.sm + 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = accent.content,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.manage_add_tag),
                        style = typography.labelPill,
                        color = scheme.onSurface,
                    )
                }
            }
        }

        if (isAddingTag) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DraftField(
                    value = tagDraft,
                    hint = stringResource(R.string.tag_name_hint),
                    onValueChange = onTagDraftChange,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onSaveTag, enabled = !isSaving) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.action_save),
                        tint = colors.accentIndigo,
                    )
                }
                IconButton(onClick = onDismissAddTag) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = scheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageTagChip(
    name: String,
    colorKey: TagColorKey,
    onRemove: () -> Unit,
) {
    val accent = tagChipColors(colorKey)
    val outline = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .height(GaliusSpacing.tagPillHeight)
            .clip(PillShape)
            .background(accent.container)
            .padding(start = GaliusSpacing.sm + 2.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.manage_tag_hash, name),
            style = GaliusThemeTokens.typography.labelPill,
            color = accent.content,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.action_delete),
                tint = outline,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    colorKey: TagColorKey,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = tagChipColors(colorKey)
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accent.content)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = scheme.onSurface,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DraftField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = typography.bodyMd.copy(color = scheme.onSurface),
        cursorBrush = SolidColor(colors.accentIndigo),
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            }
            .height(48.dp)
            .clip(InputShape)
            .background(colors.elevated)
            .border(1.dp, colors.ghostBorder, InputShape)
            .padding(horizontal = GaliusSpacing.md, vertical = GaliusSpacing.sm),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(text = hint, style = typography.bodyMd, color = scheme.outline)
                }
                inner()
            }
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 900)
@Composable
private fun ManageTagsScreenPreviewDark() {
    GaliusTheme(darkTheme = true) {
        ManageTagsScreenPreviewContent()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F6, heightDp = 900)
@Composable
private fun ManageTagsScreenPreviewLight() {
    GaliusTheme(darkTheme = false) {
        ManageTagsScreenPreviewContent()
    }
}

@Composable
private fun ManageTagsScreenPreviewContent() {
    ManageTagsScreen(
        state = ManageTagsState(
            categories = listOf(
                CategoryWithTags(
                    category = Category("1", "People", TagColorKey.Indigo, 0L),
                    tags = listOf(
                        Tag("t1", "1", "Family", TagColorKey.Indigo, 0L),
                        Tag("t2", "1", "Team", TagColorKey.Indigo, 0L),
                    ),
                ),
            ),
        ),
        onBack = {},
        onCategoryNameChange = {},
        onCategoryColorChange = {},
        onSaveCategory = {},
        onOpenAddTag = {},
        onDismissAddTag = {},
        onTagDraftChange = {},
        onSaveTag = {},
        onDeleteTag = {},
        contentBottomPadding = 96.dp,
    )
}
