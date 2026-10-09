package com.balius.galius.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balius.galius.R
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.ui.GalliusTopBar
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.feature.settings.domain.AppLockAuthResult
import com.balius.galius.feature.settings.domain.AppLockAuthenticator
import com.balius.galius.feature.settings.domain.model.LibraryStorageStats
import com.balius.galius.feature.settings.presentation.components.AccentSwatchRow
import com.balius.galius.feature.settings.presentation.components.AppPinSheet
import com.balius.galius.feature.settings.presentation.components.StorageAllocationBar
import com.balius.galius.feature.settings.presentation.components.ThemeModeSegmentedControl
import com.balius.galius.ui.theme.GaliusRadius
import com.balius.galius.ui.theme.GaliusSpacing
import com.balius.galius.ui.theme.GaliusTheme
import com.balius.galius.ui.theme.GaliusThemeTokens
import com.balius.galius.ui.theme.PillShape
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.GlobalContext

@Composable
fun SettingsRoute(
    onManageTagsClick: () -> Unit,
    contentBottomPadding: Dp,
    viewModel: SettingsViewModel = koinViewModel(),
    appLockAuthenticator: AppLockAuthenticator = remember {
        GlobalContext.get().get()
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lockTitle = stringResource(R.string.app_lock_title)
    val lockSubtitle = stringResource(R.string.settings_biometric_unlock_subtitle)
    val notNow = stringResource(R.string.app_lock_not_now)
    val biometricAvailable = appLockAuthenticator.canAuthenticate(biometricOnly = true)

    fun showMessage(messageRes: Int) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    LaunchedEffect(state.promptBiometricAfterPin) {
        if (!state.promptBiometricAfterPin) return@LaunchedEffect
        val host = activity
        if (host != null && biometricAvailable) {
            appLockAuthenticator.authenticate(
                activity = host,
                title = lockTitle,
                subtitle = lockSubtitle,
                biometricOnly = true,
                negativeButtonText = notNow,
            ) { result ->
                if (result == AppLockAuthResult.Success) {
                    viewModel.onIntent(SettingsIntent.SetBiometricUnlock(true))
                }
                viewModel.onIntent(SettingsIntent.BiometricPromptHandled)
            }
        } else {
            viewModel.onIntent(SettingsIntent.BiometricPromptHandled)
        }
    }

    BoxWithSnackbar(snackbarHostState = snackbarHostState) {
        SettingsScreen(
            state = state,
            biometricAvailable = biometricAvailable,
            onThemeModeSelected = { viewModel.onIntent(SettingsIntent.ThemeModeSelected(it)) },
            onAccentSelected = { viewModel.onIntent(SettingsIntent.AccentSelected(it)) },
            onAppLockCheckedChange = { enabled ->
                viewModel.onIntent(
                    if (enabled) SettingsIntent.BeginEnablePin else SettingsIntent.BeginDisablePin,
                )
            },
            onChangePinClick = { viewModel.onIntent(SettingsIntent.BeginChangePin) },
            onBiometricCheckedChange = { enabled ->
                if (!enabled) {
                    viewModel.onIntent(SettingsIntent.SetBiometricUnlock(false))
                } else {
                    val host = activity
                    if (host == null || !biometricAvailable) {
                        showMessage(R.string.app_lock_unavailable)
                    } else {
                        appLockAuthenticator.authenticate(
                            activity = host,
                            title = lockTitle,
                            subtitle = lockSubtitle,
                            biometricOnly = true,
                            negativeButtonText = notNow,
                        ) { result ->
                            when (result) {
                                AppLockAuthResult.Success ->
                                    viewModel.onIntent(SettingsIntent.SetBiometricUnlock(true))
                                AppLockAuthResult.Canceled -> Unit
                                AppLockAuthResult.Unavailable ->
                                    showMessage(R.string.app_lock_unavailable)
                                AppLockAuthResult.Error ->
                                    showMessage(R.string.app_lock_enable_failed)
                            }
                        }
                    }
                }
            },
            onPinDigit = { viewModel.onIntent(SettingsIntent.PinDigit(it)) },
            onPinDelete = { viewModel.onIntent(SettingsIntent.PinDelete) },
            onPinSubmit = { viewModel.onIntent(SettingsIntent.PinSubmit) },
            onPinDismiss = { viewModel.onIntent(SettingsIntent.PinDismiss) },
            onManageTagsClick = onManageTagsClick,
            contentBottomPadding = contentBottomPadding,
        )
    }
}

@Composable
private fun BoxWithSnackbar(
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        content()
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    biometricAvailable: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onAccentSelected: (AccentOption) -> Unit,
    onAppLockCheckedChange: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onBiometricCheckedChange: (Boolean) -> Unit,
    onPinDigit: (Int) -> Unit,
    onPinDelete: () -> Unit,
    onPinSubmit: () -> Unit,
    onPinDismiss: () -> Unit,
    onManageTagsClick: () -> Unit,
    contentBottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val typography = GaliusThemeTokens.typography
    val colors = GaliusThemeTokens.colors
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""
    }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        GalliusTopBar(title = stringResource(R.string.settings_title))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = contentBottomPadding)
                .padding(horizontal = GaliusSpacing.margin),
            verticalArrangement = Arrangement.spacedBy(GaliusSpacing.lg),
        ) {
        SettingsSection(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            title = stringResource(R.string.settings_appearance),
        ) {
            SettingsCard {
                ThemeModeSegmentedControl(
                    selected = state.themeMode,
                    onSelected = onThemeModeSelected,
                )
                Spacer(modifier = Modifier.height(GaliusSpacing.md))
                AccentSwatchRow(
                    selected = state.accent,
                    onSelected = onAccentSelected,
                )
            }
        }

        SettingsSection(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            title = stringResource(R.string.settings_security),
            trailing = {
                Text(
                    text = stringResource(R.string.settings_zero_cloud),
                    style = typography.labelPill,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(scheme.surfaceBright)
                        .padding(horizontal = GaliusSpacing.sm, vertical = 2.dp),
                )
            },
        ) {
            SettingsCard {
                BiometricLockRow(
                    title = stringResource(R.string.settings_app_lock),
                    subtitle = stringResource(R.string.settings_app_lock_subtitle),
                    checked = state.appLockEnabled,
                    onCheckedChange = onAppLockCheckedChange,
                )
                if (state.appLockEnabled) {
                    Spacer(modifier = Modifier.height(GaliusSpacing.md))
                    SettingsNavRow(
                        title = stringResource(R.string.settings_change_pin),
                        onClick = onChangePinClick,
                    )
                    if (biometricAvailable) {
                        Spacer(modifier = Modifier.height(GaliusSpacing.sm))
                        BiometricLockRow(
                            title = stringResource(R.string.settings_biometric_unlock),
                            subtitle = stringResource(R.string.settings_biometric_unlock_subtitle),
                            checked = state.biometricUnlockEnabled,
                            onCheckedChange = onBiometricCheckedChange,
                        )
                    }
                }
            }
        }

        SettingsSection(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Storage,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            title = stringResource(R.string.settings_library),
            trailing = {
                Text(
                    text = stringResource(
                        R.string.settings_storage_total,
                        StorageSizeFormatter.format(state.storage.totalBytes),
                    ),
                    style = typography.labelNumeric,
                    color = scheme.onSurfaceVariant,
                )
            },
        ) {
            SettingsCard {
                StorageAllocationBar(stats = state.storage)
                Spacer(modifier = Modifier.height(GaliusSpacing.lg))
                SettingsNavRow(
                    title = stringResource(R.string.settings_manage_tags),
                    onClick = onManageTagsClick,
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_version, versionName),
            style = typography.bodySm,
            color = colors.metadataCaption,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = GaliusSpacing.lg),
        )
        }
    }
        state.pinPrompt?.let { prompt ->
            AppPinSheet(
                prompt = prompt,
                onDigit = onPinDigit,
                onDelete = onPinDelete,
                onSubmit = onPinSubmit,
                onDismiss = onPinDismiss,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    icon: @Composable () -> Unit,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GaliusSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                Text(
                    text = title,
                    style = GaliusThemeTokens.typography.labelPill,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            trailing?.invoke()
        }
        content()
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GaliusRadius.xl))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(GaliusSpacing.md),
    ) {
        content()
    }
}

@Composable
private fun BiometricLockRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = GaliusThemeTokens.colors
    val typography = GaliusThemeTokens.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GaliusSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(GaliusRadius.md))
                .background(scheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = scheme.primary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyMd,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = typography.bodySm,
                color = colors.metadataDescription,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = scheme.primaryContainer,
                checkedThumbColor = scheme.onPrimary,
            ),
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        style = GaliusThemeTokens.typography.bodyLg,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = GaliusSpacing.md),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115, heightDp = 900)
@Composable
private fun SettingsScreenDarkPreview() {
    GaliusTheme(darkTheme = true, accent = AccentOption.ElectricIndigo) {
        SettingsScreen(
            state = SettingsState(
                storage = LibraryStorageStats(
                    photoBytes = 7_200_000_000,
                    videoBytes = 3_200_000_000,
                    dbBytes = 42_000_000,
                    itemCount = 1420,
                ),
            ),
            onThemeModeSelected = {},
            onAccentSelected = {},
            biometricAvailable = true,
            onAppLockCheckedChange = {},
            onChangePinClick = {},
            onBiometricCheckedChange = {},
            onPinDigit = {},
            onPinDelete = {},
            onPinSubmit = {},
            onPinDismiss = {},
            onManageTagsClick = {},
            contentBottomPadding = 96.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F6, heightDp = 900)
@Composable
private fun SettingsScreenLightPreview() {
    GaliusTheme(darkTheme = false, accent = AccentOption.ElectricCyan) {
        SettingsScreen(
            state = SettingsState(
                themeMode = ThemeMode.Light,
                accent = AccentOption.ElectricCyan,
                appLockEnabled = true,
                biometricUnlockEnabled = true,
                storage = LibraryStorageStats(
                    photoBytes = 1_200_000_000,
                    videoBytes = 800_000_000,
                    dbBytes = 12_000_000,
                    itemCount = 42,
                ),
            ),
            onThemeModeSelected = {},
            onAccentSelected = {},
            biometricAvailable = true,
            onAppLockCheckedChange = {},
            onChangePinClick = {},
            onBiometricCheckedChange = {},
            onPinDigit = {},
            onPinDelete = {},
            onPinSubmit = {},
            onPinDismiss = {},
            onManageTagsClick = {},
            contentBottomPadding = 96.dp,
        )
    }
}
