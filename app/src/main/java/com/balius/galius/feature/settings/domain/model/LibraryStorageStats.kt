package com.balius.galius.feature.settings.domain.model

data class LibraryStorageStats(
    val photoBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val dbBytes: Long = 0L,
    val itemCount: Int = 0,
) {
    val totalBytes: Long get() = photoBytes + videoBytes + dbBytes
}
