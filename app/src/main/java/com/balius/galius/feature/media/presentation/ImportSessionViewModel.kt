package com.balius.galius.feature.media.presentation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.feature.media.domain.usecase.ImportMediaUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface ImportEffect {
    data object RequestMediaReadPermission : ImportEffect
    data object LaunchPhotoPicker : ImportEffect
    data class LaunchDeleteRequest(val uris: List<Uri>) : ImportEffect
    data class ShowMessage(
        @StringRes val messageRes: Int,
        @StringRes val actionLabelRes: Int? = null,
        val action: ImportSnackbarAction? = null,
    ) : ImportEffect
}

enum class ImportSnackbarAction {
    RetryDelete,
    RequestPermission,
}

class ImportSessionViewModel(
    private val importMediaUseCase: ImportMediaUseCase,
) : ViewModel() {

    private val _effects = MutableSharedFlow<ImportEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<ImportEffect> = _effects.asSharedFlow()

    private var pendingDeleteUris: List<Uri> = emptyList()
    private var pendingImportFailureCount: Int = 0
    private var awaitingPermissionForDelete: Boolean = false

    fun onImportClick() {
        viewModelScope.launch {
            _effects.emit(ImportEffect.RequestMediaReadPermission)
        }
    }

    fun onMediaPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (awaitingPermissionForDelete) {
                if (granted) {
                    awaitingPermissionForDelete = false
                    emitPendingDelete()
                } else {
                    _effects.emit(
                        ImportEffect.ShowMessage(
                            messageRes = R.string.media_import_permission_rationale,
                            actionLabelRes = R.string.action_retry,
                            action = ImportSnackbarAction.RequestPermission,
                        ),
                    )
                }
                return@launch
            }

            if (granted) {
                _effects.emit(ImportEffect.LaunchPhotoPicker)
            } else {
                _effects.emit(
                    ImportEffect.ShowMessage(
                        messageRes = R.string.media_import_permission_rationale,
                        actionLabelRes = R.string.action_retry,
                        action = ImportSnackbarAction.RequestPermission,
                    ),
                )
            }
        }
    }

    fun onPickerResult(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val result = importMediaUseCase(uris)
            when {
                result.imported.isNotEmpty() -> {
                    pendingImportFailureCount = result.failureCount
                    pendingDeleteUris = result.sourceUrisForDelete
                    if (pendingDeleteUris.isNotEmpty()) {
                        _effects.emit(ImportEffect.LaunchDeleteRequest(pendingDeleteUris))
                    } else {
                        // Nothing to retry — originals were not resolvable to MediaStore rows.
                        _effects.emit(
                            ImportEffect.ShowMessage(messageRes = R.string.media_delete_failed),
                        )
                    }
                }
                else -> _effects.emit(ImportEffect.ShowMessage(R.string.error_generic))
            }
        }
    }

    fun onDeleteNeedsPermission() {
        awaitingPermissionForDelete = true
        viewModelScope.launch {
            _effects.emit(ImportEffect.RequestMediaReadPermission)
        }
    }

    fun onDeleteRequestResult(confirmed: Boolean) {
        viewModelScope.launch {
            if (confirmed) {
                val messageRes = if (pendingImportFailureCount > 0) {
                    R.string.media_import_partial
                } else {
                    R.string.media_private_moved
                }
                _effects.emit(ImportEffect.ShowMessage(messageRes))
                clearPendingDelete()
            } else {
                _effects.emit(
                    ImportEffect.ShowMessage(
                        messageRes = R.string.media_delete_cancelled,
                        actionLabelRes = R.string.action_retry,
                        action = ImportSnackbarAction.RetryDelete,
                    ),
                )
            }
        }
    }

    fun onDeleteRequestLaunchFailed() {
        viewModelScope.launch {
            val canRetry = pendingDeleteUris.isNotEmpty()
            _effects.emit(
                ImportEffect.ShowMessage(
                    messageRes = R.string.media_delete_failed,
                    actionLabelRes = if (canRetry) R.string.action_retry else null,
                    action = if (canRetry) ImportSnackbarAction.RetryDelete else null,
                ),
            )
        }
    }

    fun onSnackbarAction(action: ImportSnackbarAction) {
        when (action) {
            ImportSnackbarAction.RetryDelete -> retryPendingDelete()
            ImportSnackbarAction.RequestPermission -> {
                viewModelScope.launch {
                    _effects.emit(ImportEffect.RequestMediaReadPermission)
                }
            }
        }
    }

    fun retryPendingDelete() {
        if (pendingDeleteUris.isEmpty()) return
        viewModelScope.launch {
            emitPendingDelete()
        }
    }

    private suspend fun emitPendingDelete() {
        if (pendingDeleteUris.isEmpty()) return
        _effects.emit(ImportEffect.LaunchDeleteRequest(pendingDeleteUris))
    }

    private fun clearPendingDelete() {
        pendingDeleteUris = emptyList()
        pendingImportFailureCount = 0
        awaitingPermissionForDelete = false
    }
}
