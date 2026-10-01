package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository

class CreateCategoryUseCase(
    private val repository: TaxonomyRepository,
) {
    suspend operator fun invoke(name: String, colorKey: TagColorKey): Category =
        repository.createCategory(name, colorKey)
}
