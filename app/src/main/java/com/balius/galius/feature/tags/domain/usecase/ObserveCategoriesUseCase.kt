package com.balius.galius.feature.tags.domain.usecase

import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(
    private val repository: TaxonomyRepository,
) {
    operator fun invoke(): Flow<List<CategoryWithTags>> = repository.observeCategoriesWithTags()
}
