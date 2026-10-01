package com.balius.galius.feature.media.domain.model

data class MediaItem(
    val id: String,
    val filePath: String,
    val displayName: String,
    val mimeType: String,
    val type: MediaType,
    val createdAtMillis: Long,
    val originalRelativePath: String? = null,
)

enum class MediaType {
    Photo,
    Video,
}
