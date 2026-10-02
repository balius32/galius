package com.balius.galius.feature.media.domain.usecase

import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveBrowseMediaUseCase(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(source: MediaBrowseSource): Flow<List<MediaItem>> = when (source) {
        MediaBrowseSource.Library -> mediaRepository.observeAll()
        MediaBrowseSource.LibraryVideosOnly -> mediaRepository.observeAll().map { items ->
            items.filter { it.type == MediaType.Video }
        }
        is MediaBrowseSource.Category -> mediaRepository.observeByCategoryId(source.categoryId)
        is MediaBrowseSource.Tag -> mediaRepository.observeByTagId(source.tagId)
    }
}
