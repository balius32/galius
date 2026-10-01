package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveMediaTagsUseCase(
    private val repository: TaxonomyRepository,
) {
    operator fun invoke(mediaId: String?): Flow<List<Tag>> =
        if (mediaId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            repository.observeTagsForMedia(mediaId)
        }
}
