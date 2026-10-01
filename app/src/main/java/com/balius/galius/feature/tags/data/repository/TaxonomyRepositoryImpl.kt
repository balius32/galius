package com.balius.galius.feature.tags.data.repository

import com.balius.galius.core.database.CategoryEntity
import com.balius.galius.core.database.MediaTagEntity
import com.balius.galius.core.database.TagEntity
import com.balius.galius.core.database.TaxonomyDao
import com.balius.galius.feature.tags.domain.model.Category
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaxonomyRepositoryImpl(
    private val taxonomyDao: TaxonomyDao,
) : TaxonomyRepository {

    override fun observeCategoriesWithTags(): Flow<List<CategoryWithTags>> =
        taxonomyDao.observeCategoriesWithTags().map { rows ->
            rows.map { row ->
                CategoryWithTags(
                    category = row.category.toDomain(),
                    tags = row.tags
                        .sortedBy { it.name.lowercase() }
                        .map { it.toDomain() },
                )
            }
        }

    override fun observeTagsForMedia(mediaId: String): Flow<List<Tag>> =
        taxonomyDao.observeTagsForMedia(mediaId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun createCategory(name: String, colorKey: TagColorKey): Category {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name required" }
        val entity = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            colorKey = colorKey.name.lowercase(),
            createdAtMillis = System.currentTimeMillis(),
        )
        taxonomyDao.insertCategory(entity)
        return entity.toDomain()
    }

    override suspend fun createTag(
        categoryId: String,
        name: String,
        colorKey: TagColorKey,
    ): Tag {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Tag name required" }
        require(taxonomyDao.getCategoryById(categoryId) != null) { "Category not found" }
        val entity = TagEntity(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            name = trimmed,
            colorKey = colorKey.name.lowercase(),
            createdAtMillis = System.currentTimeMillis(),
        )
        taxonomyDao.insertTag(entity)
        return entity.toDomain()
    }

    override suspend fun deleteTag(tagId: String) {
        taxonomyDao.deleteTag(tagId)
    }

    override suspend fun addTagToMedia(mediaId: String, tagId: String) {
        taxonomyDao.insertMediaTag(MediaTagEntity(mediaId = mediaId, tagId = tagId))
    }

    override suspend fun removeTagFromMedia(mediaId: String, tagId: String) {
        taxonomyDao.deleteMediaTag(mediaId = mediaId, tagId = tagId)
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        colorKey = TagColorKey.fromStorage(colorKey),
        createdAtMillis = createdAtMillis,
    )

    private fun TagEntity.toDomain() = Tag(
        id = id,
        categoryId = categoryId,
        name = name,
        colorKey = TagColorKey.fromStorage(colorKey),
        createdAtMillis = createdAtMillis,
    )
}
