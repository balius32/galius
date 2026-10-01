package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository

class SetMediaTagUseCase(
    private val repository: TaxonomyRepository,
) {
    suspend fun add(mediaId: String, tagId: String) {
        repository.addTagToMedia(mediaId, tagId)
    }

    suspend fun remove(mediaId: String, tagId: String) {
        repository.removeTagFromMedia(mediaId, tagId)
    }
}
