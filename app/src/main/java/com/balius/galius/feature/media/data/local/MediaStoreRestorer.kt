package com.balius.galius.feature.media.data.local

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.balius.galius.feature.media.domain.model.MediaType
import java.io.File

/**
 * Writes a vault file into shared MediaStore storage, preferring the original
 * [RELATIVE_PATH] (e.g. Download/) when known.
 */
class MediaStoreRestorer(
    private val context: Context,
) {
    fun restoreFile(
        vaultAbsolutePath: String,
        displayName: String,
        mimeType: String,
        mediaType: MediaType,
        originalRelativePath: String?,
    ): Uri {
        val vaultFile = File(vaultAbsolutePath)
        if (!vaultFile.exists() || !vaultFile.isFile) {
            error("Vault file missing: $vaultAbsolutePath")
        }
        val relativePath = normalizeRelativePath(originalRelativePath, mediaType)
        val normalizedMime = MediaMime.normalize(
            mimeType = mimeType,
            mediaType = mediaType,
            displayName = displayName,
            relativePath = relativePath,
        )
        val collection = collectionFor(mediaType, relativePath)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, normalizedMime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(collection, values)
            ?: error("Unable to create MediaStore entry for $displayName ($normalizedMime → $relativePath)")
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                vaultFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Unable to open output stream for $uri")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val pending = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, pending, null, null)
            }
            return uri
        } catch (t: Throwable) {
            Log.e(TAG, "restoreFile failed for $displayName", t)
            context.contentResolver.delete(uri, null, null)
            throw t
        }
    }

    private fun collectionFor(mediaType: MediaType, relativePath: String): Uri {
        val volume = MediaStore.VOLUME_EXTERNAL_PRIMARY
        val isDownload = relativePath.contains("Download", ignoreCase = true)
        return when {
            isDownload && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Downloads.getContentUri(volume)
            mediaType == MediaType.Video ->
                MediaStore.Video.Media.getContentUri(volume)
            else ->
                MediaStore.Images.Media.getContentUri(volume)
        }
    }

    private fun normalizeRelativePath(original: String?, mediaType: MediaType): String {
        val cleaned = original
            ?.trim()
            ?.trimStart('/')
            ?.takeIf { it.isNotBlank() }
        val path = cleaned ?: defaultRelativePath(mediaType)
        return if (path.endsWith("/")) path else "$path/"
    }

    private fun defaultRelativePath(mediaType: MediaType): String =
        when (mediaType) {
            MediaType.Video -> "${Environment.DIRECTORY_MOVIES}/Gallius/"
            MediaType.Photo -> "${Environment.DIRECTORY_PICTURES}/Gallius/"
        }

    private companion object {
        const val TAG = "MediaStoreRestorer"
    }
}
