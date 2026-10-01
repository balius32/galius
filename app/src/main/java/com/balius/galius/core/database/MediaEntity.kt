package com.balius.galius.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: String,
    val relativePath: String,
    val displayName: String,
    val mimeType: String,
    val mediaType: String,
    val createdAtMillis: Long,
    /** MediaStore RELATIVE_PATH before move, e.g. "Download/" — null if unknown. */
    val originalRelativePath: String? = null,
)
