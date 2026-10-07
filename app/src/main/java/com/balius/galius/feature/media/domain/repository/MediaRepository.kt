package com.balius.galius.feature.media.domain.repository

import android.net.Uri
import com.balius.galius.feature.media.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun observeAll(): Flow<List<MediaItem>>

    fun observeByTagId(tagId: String): Flow<List<MediaItem>>

    /** Media that has every tag in [tagIds] (AND). Empty set yields empty list. */
    fun observeByAllTagIds(tagIds: Set<String>): Flow<List<MediaItem>>

    fun observeByCategoryId(categoryId: String): Flow<List<MediaItem>>

    suspend fun importFromUri(uri: Uri): MediaItem

    /**
     * Writes vault files back to MediaStore (best-effort original folder),
     * then deletes vault copies and DB rows.
     */
    suspend fun restoreAndRemove(ids: List<String>): RestoreRemoveResult
}

data class RestoreRemoveResult(
    val restoredCount: Int,
    val failedCount: Int,
)
