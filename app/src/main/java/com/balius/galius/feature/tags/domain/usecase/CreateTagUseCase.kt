package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository

class CreateTagUseCase(
    private val repository: TaxonomyRepository,
) {
    suspend operator fun invoke(
        categoryId: String,
        name: String,
        colorKey: TagColorKey,
    ): Tag = repository.createTag(categoryId, name, colorKey)
}
