package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.repository.MediaRepository
import com.balius.galius.feature.media.domain.repository.RestoreRemoveResult

class RestoreAndRemoveMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(ids: List<String>): RestoreRemoveResult =
        mediaRepository.restoreAndRemove(ids)
}
