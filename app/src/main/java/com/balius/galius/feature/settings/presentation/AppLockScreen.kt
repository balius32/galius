package com.balius.galius.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balius.galius.R
import com.balius.galius.feature.settings.presentation.components.AppPinKeypad
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens

@Composable
fun AppLockScreen(
    pinLength: Int,
    wrongPin: Boolean,
    biometricAvailable: Boolean,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GaliusThemeTokens.colors
    val typography = GaliusThemeTokens.typography
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .padding(GaliusSpacing.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(GaliusSpacing.xxl))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(GaliusRadius.xl))
                .background(scheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
        Text(
            text = stringResource(R.string.app_lock_title),
            style = typography.headlineMd,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.sm))
        Text(
            text = stringResource(R.string.app_lock_subtitle),
            style = typography.bodyMd,
            color = colors.metadataDescription,
            textAlign = TextAlign.Center,
        )
        if (biometricAvailable) {
            Spacer(modifier = Modifier.height(GaliusSpacing.md))
            IconButton(onClick = onBiometricClick) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = stringResource(R.string.app_lock_biometric_cd),
                    tint = scheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        AppPinKeypad(
            pinLength = pinLength,
            error = if (wrongPin) stringResource(R.string.app_lock_wrong_pin) else null,
            onDigit = onDigit,
            onDelete = onDelete,
            onSubmit = onSubmit,
        )
        Spacer(modifier = Modifier.height(GaliusSpacing.lg))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun AppLockScreenPreview() {
    GaliusTheme(darkTheme = true) {
        AppLockScreen(
            pinLength = 2,
            wrongPin = false,
            biometricAvailable = true,
            onDigit = {},
            onDelete = {},
            onSubmit = {},
            onBiometricClick = {},
        )
    }
}
