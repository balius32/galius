package com.balius.galius.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<MediaEntity>>

    @Query(
        """
        SELECT DISTINCT m.* FROM media_items m
        INNER JOIN media_tags mt ON mt.mediaId = m.id
        WHERE mt.tagId = :tagId
        ORDER BY m.createdAtMillis DESC
        """,
    )
    fun observeByTagId(tagId: String): Flow<List<MediaEntity>>

    @Query(
        """
        SELECT DISTINCT m.* FROM media_items m
        INNER JOIN media_categories mc ON mc.mediaId = m.id
        WHERE mc.categoryId = :categoryId
        ORDER BY m.createdAtMillis DESC
        """,
    )
    fun observeByCategoryId(categoryId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MediaEntity)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
