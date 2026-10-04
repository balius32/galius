package com.balius.galius.feature.media.domain.model

import android.net.Uri

data class MediaSharePackage(
    val uris: List<Uri>,
    val mimeType: String,
)
