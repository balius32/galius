package com.balius.galius.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxonomyDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE categoryId = :categoryId ORDER BY name COLLATE NOCASE ASC")
    fun observeTagsForCategory(categoryId: String): Flow<List<TagEntity>>

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN media_tags mt ON mt.tagId = t.id
        WHERE mt.mediaId = :mediaId
        ORDER BY t.name COLLATE NOCASE ASC
        """,
    )
    fun observeTagsForMedia(mediaId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(entity: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTag(entity: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaTag(entity: MediaTagEntity)

    @Query("DELETE FROM media_tags WHERE mediaId = :mediaId AND tagId = :tagId")
    suspend fun deleteMediaTag(mediaId: String, tagId: String)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: String)

    @Transaction
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategoriesWithTags(): Flow<List<CategoryWithTagsRelation>>
}
