package com.balius.galius.core.navigation

import android.app.Activity
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.balius.galius.feature.home.presentation.HomeRoute
import com.balius.galius.feature.media.data.local.MediaReadPermission
import com.balius.galius.feature.media.data.local.MediaStoreDeleteUriResolver
import com.balius.galius.feature.media.presentation.ImportEffect
import com.balius.galius.feature.media.presentation.ImportSessionViewModel
import com.balius.galius.feature.more.presentation.MoreRoute
import com.balius.galius.feature.search.presentation.SearchRoute
import com.balius.galius.feature.tags.presentation.ManageTagsRoute
import com.balius.galius.ui.theme.GaliusSpacing
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun GaliusNavHost(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(TopLevelRoute.Home)
    var currentRoute by remember { mutableStateOf<TopLevelRoute>(TopLevelRoute.Home) }
    val importSessionViewModel: ImportSessionViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deleteUriResolver = remember(context) { MediaStoreDeleteUriResolver(context) }
    val showBottomBar = backStack.lastOrNull() is TopLevelRoute

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants.values.any { it }
        importSessionViewModel.onMediaPermissionResult(granted)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        importSessionViewModel.onPickerResult(uris)
    }

    val deleteRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        importSessionViewModel.onDeleteRequestResult(result.resultCode == Activity.RESULT_OK)
    }

    fun launchSystemDelete(uris: List<android.net.Uri>) {
        if (!MediaReadPermission.hasAccess(context)) {
            importSessionViewModel.onDeleteNeedsPermission()
            return
        }
        val deleteUris = deleteUriResolver.resolveForDelete(uris)
        if (deleteUris.isEmpty()) {
            importSessionViewModel.onDeleteRequestLaunchFailed()
            return
        }
        val launched = runCatching {
            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                deleteUris,
            )
            deleteRequestLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build(),
            )
        }.isSuccess
        if (!launched) {
            importSessionViewModel.onDeleteRequestLaunchFailed()
        }
    }

    LaunchedEffect(importSessionViewModel) {
        importSessionViewModel.effects.collect { effect ->
            when (effect) {
                ImportEffect.RequestMediaReadPermission -> {
                    if (MediaReadPermission.hasAccess(context)) {
                        importSessionViewModel.onMediaPermissionResult(granted = true)
                    } else {
                        permissionLauncher.launch(MediaReadPermission.requiredPermissions())
                    }
                }

                ImportEffect.LaunchPhotoPicker -> {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                        ),
                    )
                }

                is ImportEffect.LaunchDeleteRequest -> {
                    launchSystemDelete(effect.uris)
                }

                is ImportEffect.ShowMessage -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = context.getString(effect.messageRes),
                            actionLabel = effect.actionLabelRes?.let { context.getString(it) },
                            duration = if (effect.action != null) {
                                SnackbarDuration.Long
                            } else {
                                SnackbarDuration.Short
                            },
                        )
                        if (result == SnackbarResult.ActionPerformed && effect.action != null) {
                            importSessionViewModel.onSnackbarAction(effect.action)
                        }
                    }
                }
            }
        }
    }

    fun navigateToTopLevel(route: TopLevelRoute) {
        if (currentRoute == route && backStack.size == 1) return
        currentRoute = route
        backStack.clear()
        backStack.add(route)
    }

    val contentBottomPadding = if (showBottomBar) {
        GaliusSpacing.xxl + GaliusSpacing.xl
    } else {
        GaliusSpacing.lg
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                    currentRoute = backStack.lastOrNull() as? TopLevelRoute ?: currentRoute
                }
            },
            modifier = Modifier.fillMaxSize(),
            entryProvider = entryProvider {
                entry<TopLevelRoute.Home> {
                    HomeRoute(
                        onOpenSearch = { navigateToTopLevel(TopLevelRoute.Search) },
                        onImportClick = importSessionViewModel::onImportClick,
                        contentBottomPadding = contentBottomPadding,
                    )
                }
                entry<TopLevelRoute.Search> {
                    SearchRoute(
                        contentBottomPadding = contentBottomPadding,
                    )
                }
                entry<TopLevelRoute.More> {
                    MoreRoute(
                        onImportClick = importSessionViewModel::onImportClick,
                        onManageTagsClick = { backStack.add(ManageTagsRoute) },
                        contentBottomPadding = contentBottomPadding,
                    )
                }
                entry<ManageTagsRoute> {
                    ManageTagsRoute(
                        onBack = { backStack.removeLastOrNull() },
                        contentBottomPadding = contentBottomPadding,
                    )
                }
            },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = GaliusSpacing.xxl + GaliusSpacing.xl),
        )

        if (showBottomBar) {
            GaliusBottomBar(
                currentRoute = currentRoute,
                onNavigate = ::navigateToTopLevel,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
