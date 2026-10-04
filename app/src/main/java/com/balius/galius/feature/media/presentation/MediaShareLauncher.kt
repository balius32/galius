package com.balius.galius.feature.media.presentation

import android.content.ClipData
import android.content.Context
import android.content.Intent
import com.balius.galius.R
import com.balius.galius.feature.media.domain.model.MediaSharePackage

fun Context.launchMediaShare(sharePackage: MediaSharePackage) {
    val intent = if (sharePackage.uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = sharePackage.mimeType
            putExtra(Intent.EXTRA_STREAM, sharePackage.uris.first())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = sharePackage.mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(sharePackage.uris))
        }
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val clip = ClipData.newRawUri(getString(R.string.action_share), sharePackage.uris.first())
    sharePackage.uris.drop(1).forEach { uri ->
        clip.addItem(ClipData.Item(uri))
    }
    intent.clipData = clip
    startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
}
