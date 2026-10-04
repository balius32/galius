package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaSharePackage
import com.balius.galius.feature.media.domain.repository.MediaShareUriProvider

class PrepareMediaShareUseCase(
    private val mediaShareUriProvider: MediaShareUriProvider,
) {
    operator fun invoke(items: List<MediaItem>): MediaSharePackage? {
        if (items.isEmpty()) return null
        val shared = items.mapNotNull { item ->
            mediaShareUriProvider.uriForFilePath(item.filePath)?.let { uri -> item to uri }
        }
        if (shared.isEmpty()) return null
        val uris = shared.map { it.second }
        val sharedItems = shared.map { it.first }
        val mimeType = when {
            sharedItems.size == 1 -> sharedItems.single().mimeType
            sharedItems.map { it.mimeType }.distinct().size == 1 -> sharedItems.first().mimeType
            else -> "*/*"
        }
        return MediaSharePackage(uris = uris, mimeType = mimeType)
    }
}
