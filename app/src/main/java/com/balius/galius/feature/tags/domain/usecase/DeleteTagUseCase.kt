package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository

class DeleteTagUseCase(
    private val repository: TaxonomyRepository,
) {
    suspend operator fun invoke(tagId: String) {
        repository.deleteTag(tagId)
    }
}
