package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.settings.domain.model.LibraryStorageStats
import com.balius.galius.feature.settings.domain.repository.DatabaseSizeProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveLibraryStorageUseCase(
    private val observeLibraryUseCase: ObserveLibraryUseCase,
    private val databaseSizeProvider: DatabaseSizeProvider,
) {
    operator fun invoke(): Flow<LibraryStorageStats> =
        observeLibraryUseCase().map { items ->
            var photoBytes = 0L
            var videoBytes = 0L
            for (item in items) {
                val size = File(item.filePath).takeIf { it.exists() }?.length() ?: 0L
                when (item.type) {
                    MediaType.Photo -> photoBytes += size
                    MediaType.Video -> videoBytes += size
                }
            }
            LibraryStorageStats(
                photoBytes = photoBytes,
                videoBytes = videoBytes,
                dbBytes = databaseSizeProvider.databaseBytes(),
                itemCount = items.size,
            )
        }.flowOn(Dispatchers.IO)
}
