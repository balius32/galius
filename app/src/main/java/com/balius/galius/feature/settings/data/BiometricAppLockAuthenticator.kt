package com.balius.galius.feature.settings.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.balius.galius.feature.settings.domain.AppLockAuthResult
import com.balius.galius.feature.settings.domain.AppLockAuthenticator

class BiometricAppLockAuthenticator(
    private val context: Context,
) : AppLockAuthenticator {

    private val authenticators: Int =
        Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL

    override fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            // Some devices only report weak biometric + device credential.
            else -> manager.canAuthenticate(
                Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL,
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    override fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onResult: (AppLockAuthResult) -> Unit,
    ) {
        if (!canAuthenticate()) {
            onResult(AppLockAuthResult.Unavailable)
            return
        }

        val allowed = resolveAuthenticators()
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    onResult(AppLockAuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED,
                            -> AppLockAuthResult.Canceled
                            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
                            BiometricPrompt.ERROR_HW_UNAVAILABLE,
                            BiometricPrompt.ERROR_NO_BIOMETRICS,
                            -> AppLockAuthResult.Unavailable
                            else -> AppLockAuthResult.Error
                        },
                    )
                }

                override fun onAuthenticationFailed() {
                    // Keep prompt open for another attempt; no terminal result.
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowed)
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun resolveAuthenticators(): Int {
        val manager = BiometricManager.from(context)
        return if (
            manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            authenticators
        } else {
            Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        }
    }
}
