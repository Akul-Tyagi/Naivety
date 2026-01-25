package com.abundance.naivety.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File
import java.io.FileOutputStream

object EpubThumbnailHelper {

    suspend fun extractThumbnail(publication: Publication, context: Context): Bitmap? {
        return try {
            // Get cover from publication metadata
            val coverLink = publication.linkWithRel("cover")
                ?: publication.readingOrder.firstOrNull()

            coverLink?.let { link ->
                val resource = publication.get(link)
                resource?.read()?.getOrNull()?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
