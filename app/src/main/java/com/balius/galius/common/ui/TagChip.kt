package com.balius.galius.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.ui.theme.AccentIndigo
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.GhostBorder
import com.balius.galius.ui.theme.PillShape

data class TagChipColors(
    val content: Color,
    val container: Color,
)

@Composable
fun tagChipColors(colorKey: TagColorKey): TagChipColors {
    val colors = GaliusThemeTokens.colors
    return when (colorKey) {
        TagColorKey.Indigo -> TagChipColors(AccentIndigo, AccentIndigo.copy(alpha = 0.14f))
        TagColorKey.Mint -> TagChipColors(colors.tagMint, colors.tagMintContainer)
        TagColorKey.Amber -> TagChipColors(colors.tagAmber, colors.tagAmberContainer)
        TagColorKey.Lavender -> TagChipColors(colors.tagLavender, colors.tagLavenderContainer)
        TagColorKey.Sky -> TagChipColors(colors.tagSky, colors.tagSkyContainer)
        TagColorKey.Rose -> TagChipColors(colors.tagRose, colors.tagRoseContainer)
        TagColorKey.Neutral -> TagChipColors(colors.tagNeutral, colors.tagNeutralContainer)
    }
}

@Composable
fun TagChip(
    label: String,
    colorKey: TagColorKey,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val chipColors = tagChipColors(colorKey)
    val typography = GaliusThemeTokens.typography
    val height = if (compact) GaliusSpacing.tagPillHeightCompact else GaliusSpacing.tagPillHeight
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(PillShape)
            .background(chipColors.container)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) chipColors.content else GhostBorder,
                shape = PillShape,
            )
            .then(clickableModifier)
            .padding(horizontal = GaliusSpacing.sm + 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = typography.labelPill,
            color = chipColors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun TagChipPreview() {
    GaliusTheme {
        TagChip(label = "Travel", colorKey = TagColorKey.Sky, onClick = {})
    }
}
