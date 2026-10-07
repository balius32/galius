package com.balius.galius.feature.media.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.balius.galius.core.database.MediaDao
import com.balius.galius.core.database.MediaEntity
import com.balius.galius.feature.media.data.local.MediaMime
import com.balius.galius.feature.media.data.local.MediaStoreDeleteUriResolver
import com.balius.galius.feature.media.data.local.MediaStoreRestorer
import com.balius.galius.feature.media.data.local.VaultFileStore
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.repository.MediaRepository
import com.balius.galius.feature.media.domain.repository.RestoreRemoveResult
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val context: Context,
    private val mediaDao: MediaDao,
    private val vaultFileStore: VaultFileStore,
    private val deleteUriResolver: MediaStoreDeleteUriResolver,
    private val mediaStoreRestorer: MediaStoreRestorer,
) : MediaRepository {

    override fun observeAll(): Flow<List<MediaItem>> =
        mediaDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeByTagId(tagId: String): Flow<List<MediaItem>> =
        mediaDao.observeByTagId(tagId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeByAllTagIds(tagIds: Set<String>): Flow<List<MediaItem>> {
        if (tagIds.isEmpty()) return flowOf(emptyList())
        val list = tagIds.toList()
        return mediaDao.observeByAllTagIds(list, list.size).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeByCategoryId(categoryId: String): Flow<List<MediaItem>> =
        mediaDao.observeByCategoryId(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun importFromUri(uri: Uri): MediaItem = withContext(Dispatchers.IO) {
        val rawMime = context.contentResolver.getType(uri)
        val displayName = queryDisplayName(uri) ?: "media_${System.currentTimeMillis()}"
        val mediaType = MediaMime.toMediaType(rawMime)
        val normalizedMime = MediaMime.normalize(
            mimeType = rawMime,
            mediaType = mediaType,
            displayName = displayName,
        )
        val mediaStoreUri = deleteUriResolver.resolveForDelete(listOf(uri)).firstOrNull() ?: uri
        val originalRelativePath = queryRelativePath(mediaStoreUri) ?: queryRelativePath(uri)
        val relativePath = vaultFileStore.copyFromUri(uri, normalizedMime)
        val entity = MediaEntity(
            id = UUID.randomUUID().toString(),
            relativePath = relativePath,
            displayName = displayName,
            mimeType = normalizedMime,
            mediaType = mediaType.name,
            createdAtMillis = System.currentTimeMillis(),
            originalRelativePath = originalRelativePath,
        )
        mediaDao.insert(entity)
        entity.toDomain()
    }

    override suspend fun restoreAndRemove(ids: List<String>): RestoreRemoveResult =
        withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext RestoreRemoveResult(0, 0)
            val entities = mediaDao.getByIds(ids)
            var restored = 0
            var failed = 0
            val removedIds = mutableListOf<String>()
            entities.forEach { entity ->
                runCatching {
                    val absolute = vaultFileStore.absolutePath(entity.relativePath)
                    val mediaType = runCatching { MediaType.valueOf(entity.mediaType) }
                        .getOrDefault(MediaType.Photo)
                    mediaStoreRestorer.restoreFile(
                        vaultAbsolutePath = absolute,
                        displayName = entity.displayName,
                        mimeType = MediaMime.normalize(
                            mimeType = entity.mimeType,
                            mediaType = mediaType,
                            displayName = entity.displayName,
                            relativePath = entity.relativePath,
                        ),
                        mediaType = mediaType,
                        originalRelativePath = entity.originalRelativePath,
                    )
                    vaultFileStore.delete(entity.relativePath)
                    removedIds += entity.id
                    restored += 1
                }.onFailure { error ->
                    Log.e(TAG, "restoreAndRemove failed for ${entity.id} (${entity.displayName})", error)
                    failed += 1
                }
            }
            if (removedIds.isNotEmpty()) {
                mediaDao.deleteByIds(removedIds)
            }
            RestoreRemoveResult(restoredCount = restored, failedCount = failed)
        }

    private fun MediaEntity.toDomain(): MediaItem =
        MediaItem(
            id = id,
            filePath = vaultFileStore.absolutePath(relativePath),
            displayName = displayName,
            mimeType = mimeType,
            type = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.Photo),
            createdAtMillis = createdAtMillis,
            originalRelativePath = originalRelativePath,
        )

    private fun queryDisplayName(uri: Uri): String? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index < 0) return null
            return it.getString(index)
        }
    }

    private fun queryRelativePath(uri: Uri): String? =
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.RELATIVE_PATH),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                if (index < 0) return@use null
                cursor.getString(index)
            }
        }.getOrNull()

    private companion object {
        const val TAG = "MediaRepositoryImpl"
    }
}
