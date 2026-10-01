package com.balius.galius.feature.media.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns

/**
 * Converts Photo Picker / grant URIs into MediaStore content URIs that
 * [MediaStore.createDeleteRequest] accepts.
 */
class MediaStoreDeleteUriResolver(
    private val context: Context,
) {
    /**
     * @return only URIs that look like deletable MediaStore rows. Empty if none resolve.
     */
    fun resolveForDelete(uris: List<Uri>): List<Uri> =
        uris.mapNotNull { uri ->
            runCatching { resolveOne(uri) }.getOrNull()
        }.distinct()

    private fun resolveOne(uri: Uri): Uri? {
        if (isDeletableMediaStoreUri(uri)) return uri

        resolveFromPickerMediaId(uri)?.let { return it }

        val displayName = queryDisplayName(uri) ?: return null
        val size = querySize(uri)
        return findMediaStoreUri(displayName, size)
    }

    /**
     * Photo Picker URIs often look like:
     * `content://media/picker/.../media/{id}` — last segment is MediaStore _ID.
     */
    private fun resolveFromPickerMediaId(uri: Uri): Uri? {
        val mediaId = uri.lastPathSegment?.toLongOrNull() ?: return null
        val candidates = listOf(
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId),
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId),
        )
        return candidates.firstOrNull { existsInMediaStore(it) }
    }

    private fun existsInMediaStore(uri: Uri): Boolean =
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns._ID),
                null,
                null,
                null,
            )?.use { it.moveToFirst() } == true
        }.getOrDefault(false)

    private fun isDeletableMediaStoreUri(uri: Uri): Boolean {
        if (uri.authority != MediaStore.AUTHORITY) return false
        val path = uri.path.orEmpty()
        // Picker grant URIs also use authority "media" — they are NOT deletable as-is.
        if (path.contains("/picker")) return false
        return path.contains("/external/") || path.contains("/internal/")
    }

    private fun findMediaStoreUri(displayName: String, size: Long?): Uri? {
        val collections = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        )
        for (collection in collections) {
            val match = queryCollection(collection, displayName, size)
            if (match != null) return match
        }
        return null
    }

    private fun queryCollection(collection: Uri, displayName: String, size: Long?): Uri? {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection: String
        val args: Array<String>
        if (size != null && size >= 0L) {
            selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.SIZE}=?"
            args = arrayOf(displayName, size.toString())
        } else {
            selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            args = arrayOf(displayName)
        }
        return context.contentResolver.query(
            collection,
            projection,
            selection,
            args,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            ContentUris.withAppendedId(collection, id)
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index < 0) return null
            return cursor.getString(index)
        }
        return null
    }

    private fun querySize(uri: Uri): Long? {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index < 0) return null
            return cursor.getLong(index)
        }
        return null
    }
}
