package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class ObserveMediaByCategoryUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(categoryId: String): Flow<List<MediaItem>> =
        mediaRepository.observeByCategoryId(categoryId)
}
