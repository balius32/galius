package com.balius.galius.feature.media.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.balius.galius.feature.media.domain.repository.MediaShareUriProvider

class MediaShareUriFactory(
    private val context: Context,
    private val vaultFileStore: VaultFileStore,
) : MediaShareUriProvider {
    override fun uriForFilePath(absolutePath: String): Uri? {
        val file = vaultFileStore.shareableFile(absolutePath) ?: return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
