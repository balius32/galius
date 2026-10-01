package com.balius.galius.feature.media.domain.usecase

import android.net.Uri
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.repository.MediaRepository

data class ImportResult(
    val imported: List<MediaItem>,
    val sourceUrisForDelete: List<Uri>,
    val failureCount: Int,
)

class ImportMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(uris: List<Uri>): ImportResult {
        if (uris.isEmpty()) {
            return ImportResult(imported = emptyList(), sourceUrisForDelete = emptyList(), failureCount = 0)
        }
        val imported = mutableListOf<MediaItem>()
        val sourceUrisForDelete = mutableListOf<Uri>()
        var failureCount = 0
        uris.forEach { uri ->
            runCatching {
                mediaRepository.importFromUri(uri)
            }.onSuccess { item ->
                imported += item
                sourceUrisForDelete += uri
            }.onFailure {
                failureCount += 1
            }
        }
        return ImportResult(
            imported = imported,
            sourceUrisForDelete = sourceUrisForDelete,
            failureCount = failureCount,
        )
    }
}
