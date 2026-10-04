package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository

class AddMediaToCategoryUseCase(
    private val repository: TaxonomyRepository,
) {
    suspend operator fun invoke(categoryId: String, mediaIds: Collection<String>) {
        if (mediaIds.isEmpty()) return
        repository.addMediaToCategory(categoryId, mediaIds)
    }
}
