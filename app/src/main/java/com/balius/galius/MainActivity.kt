package com.balius.galius

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.common.ui.SyncSystemBarAppearance
import com.balius.galius.core.navigation.GaliusNavHost
import com.balius.galius.feature.settings.domain.AppLockAuthResult
import com.balius.galius.feature.settings.domain.AppLockAuthenticator
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.usecase.ObserveSettingsPreferencesUseCase
import com.balius.galius.feature.settings.presentation.AppLockScreen
import com.balius.galius.ui.theme.CanvasBase
import com.balius.galius.ui.theme.GaliusTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val observeSettingsPreferences: ObserveSettingsPreferencesUseCase by inject()
    private val appLockAuthenticator: AppLockAuthenticator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaliusAppRoot(
                observeSettingsPreferences = observeSettingsPreferences,
                appLockAuthenticator = appLockAuthenticator,
            )
        }
    }
}

@Composable
fun GaliusAppRoot(
    observeSettingsPreferences: ObserveSettingsPreferencesUseCase,
    appLockAuthenticator: AppLockAuthenticator,
    modifier: Modifier = Modifier,
) {
    var preferences by remember { mutableStateOf<SettingsPreferences?>(null) }

    LaunchedEffect(observeSettingsPreferences) {
        // Block UI until first DataStore emission so lock is never skipped.
        preferences = observeSettingsPreferences().first()
        observeSettingsPreferences().collect { preferences = it }
    }

    val prefs = preferences
    if (prefs == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CanvasBase),
        )
        return
    }

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (prefs.themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    GaliusTheme(
        darkTheme = darkTheme,
        accent = prefs.accent,
    ) {
        SyncSystemBarAppearance(darkTheme = darkTheme)
        AppLockGate(
            appLockEnabled = prefs.appLockEnabled,
            authenticator = appLockAuthenticator,
            modifier = modifier,
        )
    }
}

@Composable
private fun AppLockGate(
    appLockEnabled: Boolean,
    authenticator: AppLockAuthenticator,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Never rememberSaveable — process death must require unlock again.
    var unlocked by remember { mutableStateOf(!appLockEnabled) }
    var authenticating by remember { mutableStateOf(false) }

    val lockTitle = stringResource(R.string.app_lock_title)
    val lockSubtitle = stringResource(R.string.app_lock_subtitle)

    fun showMessage(messageRes: Int) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    fun requestUnlock() {
        val host = activity ?: run {
            showMessage(R.string.app_lock_error)
            return
        }
        if (authenticating || unlocked || !appLockEnabled) return
        authenticating = true
        authenticator.authenticate(
            activity = host,
            title = lockTitle,
            subtitle = lockSubtitle,
        ) { result ->
            authenticating = false
            when (result) {
                AppLockAuthResult.Success -> unlocked = true
                AppLockAuthResult.Canceled -> Unit
                AppLockAuthResult.Unavailable -> showMessage(R.string.app_lock_unavailable)
                AppLockAuthResult.Error -> showMessage(R.string.app_lock_error)
            }
        }
    }

    val latestRequestUnlock by rememberUpdatedState(newValue = ::requestUnlock)
    val latestAppLockEnabled by rememberUpdatedState(appLockEnabled)
    val latestUnlocked by rememberUpdatedState(unlocked)
    val latestAuthenticating by rememberUpdatedState(authenticating)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Re-lock when leaving app (not during biometric prompt).
                    if (latestAppLockEnabled && !latestAuthenticating) {
                        unlocked = false
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (latestAppLockEnabled && !latestUnlocked && !latestAuthenticating) {
                        latestRequestUnlock()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(appLockEnabled) {
        if (!appLockEnabled) {
            unlocked = true
        } else {
            // Force lock whenever preference is on (cold start + toggle on).
            unlocked = false
            requestUnlock()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (!appLockEnabled || unlocked) {
            GaliusNavHost(modifier = Modifier.fillMaxSize())
        } else {
            AppLockScreen(onUnlockClick = { requestUnlock() })
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
@Composable
private fun GaliusAppRootPreview() {
    GaliusTheme(darkTheme = true, accent = AccentOption.ElectricIndigo) {
        GaliusNavHost(modifier = Modifier.fillMaxSize())
    }
}
