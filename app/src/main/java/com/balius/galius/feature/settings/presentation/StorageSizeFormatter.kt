package com.balius.galius.feature.settings.presentation

import java.util.Locale

object StorageSizeFormatter {
    fun format(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = -1
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex++
        }
        val pattern = if (value >= 100 || unitIndex <= 0) "%.0f %s" else "%.1f %s"
        return String.format(Locale.US, pattern, value, units[unitIndex])
    }
}
