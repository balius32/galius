package com.balius.galius.feature.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.balius.galius.R
import com.balius.galius.feature.settings.presentation.AppPinError
import com.balius.galius.feature.settings.presentation.AppPinMode
import com.balius.galius.feature.settings.presentation.AppPinPrompt
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.SheetShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPinSheet(
    prompt: AppPinPrompt,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confirming = prompt.firstPin != null
    val title = stringResource(
        when {
            confirming -> R.string.app_lock_confirm_pin_title
            prompt.mode == AppPinMode.Disable || prompt.mode == AppPinMode.ChangeCurrent ->
                R.string.app_lock_current_pin_title
            else -> R.string.app_lock_set_pin_title
        },
    )
    val subtitle = stringResource(
        when {
            confirming -> R.string.app_lock_confirm_pin_subtitle
            prompt.mode == AppPinMode.Disable || prompt.mode == AppPinMode.ChangeCurrent ->
                R.string.app_lock_current_pin_subtitle
            else -> R.string.app_lock_set_pin_subtitle
        },
    )
    val error = when (prompt.error) {
        AppPinError.TooShort -> stringResource(R.string.app_lock_pin_too_short)
        AppPinError.Mismatch -> stringResource(R.string.app_lock_pin_mismatch)
        AppPinError.Wrong -> stringResource(R.string.app_lock_wrong_pin)
        null -> null
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GaliusSpacing.md)
                .padding(bottom = GaliusSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = GaliusThemeTokens.typography.headlineMd,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = GaliusThemeTokens.typography.bodySm,
                color = GaliusThemeTokens.colors.metadataDescription,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = GaliusSpacing.sm, bottom = GaliusSpacing.lg),
            )
            AppPinKeypad(
                pinLength = prompt.draft.length,
                error = error,
                onDigit = onDigit,
                onDelete = onDelete,
                onSubmit = onSubmit,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun AppPinSheetPreview() {
    GaliusTheme(darkTheme = true) {
        AppPinKeypad(
            pinLength = 2,
            error = null,
            onDigit = {},
            onDelete = {},
            onSubmit = {},
            modifier = Modifier.padding(GaliusSpacing.md),
        )
    }
}
