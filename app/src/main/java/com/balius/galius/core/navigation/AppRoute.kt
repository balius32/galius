package com.balius.galius.core.navigation

import androidx.navigation3.runtime.NavKey
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import kotlinx.serialization.Serializable

@Serializable
data object ManageTagsRoute : NavKey

@Serializable
data class MediaViewerRoute(
    val startMediaId: String,
    val source: MediaBrowseSource,
) : NavKey
