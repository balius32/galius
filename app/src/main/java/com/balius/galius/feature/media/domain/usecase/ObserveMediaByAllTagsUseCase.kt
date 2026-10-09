package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class ObserveMediaByAllTagsUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(tagIds: Set<String>): Flow<List<MediaItem>> =
        mediaRepository.observeByAllTagIds(tagIds)
}
