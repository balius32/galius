package com.balius.galius.feature.media.data.local

import android.webkit.MimeTypeMap
import com.balius.galius.feature.media.domain.model.MediaType

/** MediaStore rejects wildcard MIME types (e.g. image slash star) on insert. */
object MediaMime {
    fun normalize(
        mimeType: String?,
        mediaType: MediaType,
        displayName: String? = null,
        relativePath: String? = null,
    ): String {
        val raw = mimeType?.trim().orEmpty()
        if (raw.isNotEmpty() && !raw.endsWith("/*") && raw.contains('/')) {
            return raw
        }
        guessFromName(displayName)?.let { return it }
        guessFromName(relativePath)?.let { return it }
        return when (mediaType) {
            MediaType.Video -> "video/mp4"
            MediaType.Photo -> "image/jpeg"
        }
    }

    fun toMediaType(mimeType: String?): MediaType =
        if (mimeType.orEmpty().startsWith("video/")) MediaType.Video else MediaType.Photo

    private fun guessFromName(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val ext = name.substringAfterLast('.', missingDelimiterValue = "")
            .trim()
            .lowercase()
            .takeIf { it.isNotEmpty() && !it.contains('/') }
            ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }
}
