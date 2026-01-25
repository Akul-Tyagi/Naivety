package com.abundance.naivety.epub

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var currentPublication: Publication? = null
    private val httpClient = DefaultHttpClient()
    private val assetRetriever = AssetRetriever(context.contentResolver, httpClient)

    private val publicationParser = DefaultPublicationParser(
        context = context,
        httpClient = httpClient,
        assetRetriever = assetRetriever,
        pdfFactory = null
    )

    private val publicationOpener = PublicationOpener(
        publicationParser = publicationParser,
        contentProtections = emptyList()
    )

    suspend fun openEpub(uri: Uri): Result<Publication> = withContext(Dispatchers.IO) {
        try {
            closeCurrentPublication()

            val url = uri.toUrl() as? AbsoluteUrl
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid URI"))

            val assetResult = assetRetriever.retrieve(url)
            val asset = assetResult.getOrElse { error ->
                return@withContext Result.failure(Exception("Failed to retrieve asset: $error"))
            }

            val publicationResult = publicationOpener.open(asset, allowUserInteraction = false)
            val publication = publicationResult.getOrElse { error ->
                return@withContext Result.failure(Exception("Failed to open publication: $error"))
            }

            currentPublication = publication
            Result.success(publication)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPageCount(publication: Publication): Int {
        return publication.readingOrder.size
    }

    fun closeCurrentPublication() {
        currentPublication?.close()
        currentPublication = null
    }
}
