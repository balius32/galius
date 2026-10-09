package com.balius.galius.feature.settings.domain

import androidx.fragment.app.FragmentActivity

sealed interface AppLockAuthResult {
    data object Success : AppLockAuthResult
    data object Canceled : AppLockAuthResult
    data object Unavailable : AppLockAuthResult
    data object Error : AppLockAuthResult
}

interface AppLockAuthenticator {
    fun canAuthenticate(biometricOnly: Boolean = false): Boolean

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        biometricOnly: Boolean = false,
        negativeButtonText: String? = null,
        onResult: (AppLockAuthResult) -> Unit,
    )
}
