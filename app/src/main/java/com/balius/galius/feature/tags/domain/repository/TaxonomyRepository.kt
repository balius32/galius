package com.balius.galius.feature.tags.domain.repository

import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import kotlinx.coroutines.flow.Flow

interface TaxonomyRepository {
    fun observeCategoriesWithTags(): Flow<List<CategoryWithTags>>
    fun observeTagsForMedia(mediaId: String): Flow<List<Tag>>
    suspend fun createCategory(name: String, colorKey: TagColorKey): Category
    suspend fun createTag(
        categoryId: String,
        name: String,
        colorKey: TagColorKey,
    ): Tag
    suspend fun deleteTag(tagId: String)
    suspend fun addTagToMedia(mediaId: String, tagId: String)
    suspend fun removeTagFromMedia(mediaId: String, tagId: String)
}
