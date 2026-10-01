package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class ObserveLibraryUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<List<MediaItem>> = mediaRepository.observeAll()
}
