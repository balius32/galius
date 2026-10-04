package com.balius.galius.feature.media.domain.repository

import android.net.Uri

interface MediaShareUriProvider {
    fun uriForFilePath(absolutePath: String): Uri?
}
