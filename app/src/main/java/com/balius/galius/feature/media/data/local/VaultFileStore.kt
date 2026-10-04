package com.balius.galius.feature.media.data.local

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID

class VaultFileStore(
    private val context: Context,
) {
    fun absolutePath(relativePath: String): String =
        File(context.noBackupFilesDir, relativePath).absolutePath

    fun vaultMediaRoot(): File = File(context.noBackupFilesDir, MEDIA_DIR_RELATIVE)

    /**
     * Returns the file only if it exists under the vault media directory.
     */
    fun shareableFile(absolutePath: String): File? {
        val file = File(absolutePath)
        if (!file.exists()) return null
        val root = vaultMediaRoot().canonicalFile
        val canonical = file.canonicalFile
        val prefix = root.path + File.separator
        if (!canonical.path.startsWith(prefix)) return null
        return canonical
    }

    fun delete(relativePath: String): Boolean {
        val file = File(context.noBackupFilesDir, relativePath)
        return !file.exists() || file.delete()
    }

    fun copyFromUri(uri: Uri, mimeType: String?): String {
        val extension = guessExtension(uri, mimeType)
        val fileName = "${UUID.randomUUID()}$extension"
        val relativePath = "$MEDIA_DIR_RELATIVE/$fileName"
        val destination = File(context.noBackupFilesDir, relativePath)
        destination.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open media stream")
        return relativePath
    }

    private fun guessExtension(uri: Uri, mimeType: String?): String {
        val fromMime = mimeType
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.takeIf { it.isNotBlank() }
            ?.let { ".$it" }
        if (fromMime != null) return fromMime

        val path = uri.lastPathSegment.orEmpty()
        val dot = path.lastIndexOf('.')
        if (dot in 0 until path.lastIndex) {
            return path.substring(dot)
        }
        return if (mimeType?.startsWith("video/") == true) ".mp4" else ".jpg"
    }

    private companion object {
        const val MEDIA_DIR_RELATIVE = "vault/media"
    }
}
