package com.balius.galius.feature.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.feature.settings.domain.AppPinRules
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens

@Composable
fun AppPinKeypad(
    pinLength: Int,
    error: String?,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.lg),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pinLength.coerceAtMost(AppPinRules.MAX_LENGTH)) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                )
            }
        }
        Text(
            text = error.orEmpty(),
            style = GaliusThemeTokens.typography.bodySm,
            color = scheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        PinPad(
            onDigit = onDigit,
            onDelete = onDelete,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun PinPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
) {
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.lg)) {
                row.forEach { digit ->
                    PinKey(label = digit.toString(), onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.lg)) {
            IconButton(onClick = onDelete, modifier = Modifier.size(72.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = stringResource(R.string.app_lock_delete_cd),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            PinKey(label = "0", onClick = { onDigit(0) })
            IconButton(onClick = onSubmit, modifier = Modifier.size(72.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.app_lock_submit_cd),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    label: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val digitCd = stringResource(R.string.app_lock_digit_cd, label.toInt())
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(scheme.surfaceContainer)
            .semantics { contentDescription = digitCd },
    ) {
        Text(
            text = label,
            style = GaliusThemeTokens.typography.headlineMd,
            color = scheme.onSurface,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun AppPinKeypadPreview() {
    GaliusTheme(darkTheme = true) {
        AppPinKeypad(
            pinLength = 4,
            error = null,
            onDigit = {},
            onDelete = {},
            onSubmit = {},
            modifier = Modifier.padding(GaliusSpacing.md),
        )
    }
}
