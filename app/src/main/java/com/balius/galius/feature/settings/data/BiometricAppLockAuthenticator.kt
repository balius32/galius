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

    private val strongOrCredential: Int =
        Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL

    override fun canAuthenticate(biometricOnly: Boolean): Boolean {
        val manager = BiometricManager.from(context)
        if (biometricOnly) {
            return manager.canAuthenticate(Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS ||
                manager.canAuthenticate(Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }
        return when (manager.canAuthenticate(strongOrCredential)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> manager.canAuthenticate(
                Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL,
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    override fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        biometricOnly: Boolean,
        negativeButtonText: String?,
        onResult: (AppLockAuthResult) -> Unit,
    ) {
        if (!canAuthenticate(biometricOnly)) {
            onResult(AppLockAuthResult.Unavailable)
            return
        }

        val allowed = resolveAuthenticators(biometricOnly)
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
            .apply {
                if (biometricOnly && !negativeButtonText.isNullOrBlank()) {
                    setNegativeButtonText(negativeButtonText)
                }
            }
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun resolveAuthenticators(biometricOnly: Boolean): Int {
        val manager = BiometricManager.from(context)
        if (biometricOnly) {
            return if (
                manager.canAuthenticate(Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
            ) {
                Authenticators.BIOMETRIC_STRONG
            } else {
                Authenticators.BIOMETRIC_WEAK
            }
        }
        return if (
            manager.canAuthenticate(strongOrCredential) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            strongOrCredential
        } else {
            Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        }
    }
}
