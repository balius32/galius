package com.balius.galius.feature.media.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface MediaBrowseSource {
    @Serializable
    data object Library : MediaBrowseSource

    @Serializable
    data object LibraryVideosOnly : MediaBrowseSource

    @Serializable
    data class Category(val categoryId: String) : MediaBrowseSource

    @Serializable
    data class Tag(val tagId: String) : MediaBrowseSource
}
