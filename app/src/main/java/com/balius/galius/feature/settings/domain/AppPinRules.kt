package com.balius.galius.feature.settings.domain

object AppPinRules {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 8

    fun isValid(pin: String): Boolean =
        pin.length in MIN_LENGTH..MAX_LENGTH && pin.all { it.isDigit() }
}
