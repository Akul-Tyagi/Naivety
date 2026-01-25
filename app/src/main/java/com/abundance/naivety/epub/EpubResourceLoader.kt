package com.abundance.naivety.epub

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url

/**
 * Handles loading and preprocessing EPUB resources (images, CSS, etc.)
 */
object EpubResourceLoader {

    private const val TAG = "EpubResourceLoader"

    // Common image directories in EPUBs
    private val IMAGE_DIRECTORIES = listOf(
        "images", "Images", "IMAGES",
        "img", "Img", "IMG",
        "image", "Image", "IMAGE",
        "media", "Media", "MEDIA",
        "graphics", "Graphics",
        "figures", "Figures",
        "pics", "Pics",
        "OEBPS/images", "OEBPS/Images",
        "OPS/images", "OPS/Images"
    )

    /**
     * Guess MIME type from file bytes (magic numbers)
     */
    fun guessMimeTypeFromBytes(bytes: ByteArray): String {
        if (bytes.size < 4) return "application/octet-stream"

        return when {
            // JPEG: FF D8 FF
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
            // PNG: 89 50 4E 47
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() -> "image/png"
            // GIF: 47 49 46 38
            bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte() -> "image/gif"
            // WebP: 52 49 46 46 ... 57 45 42 50
            bytes.size >= 12 && bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() -> "image/webp"
            // SVG: starts with < (text/xml)
            bytes[0] == '<'.code.toByte() -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }

    /**
     * Guess MIME type from file extension
     */
    fun guessMimeTypeFromExtension(urlString: String): String {
        return when {
            urlString.endsWith(".css", true) -> "text/css"
            urlString.endsWith(".jpg", true) || urlString.endsWith(".jpeg", true) -> "image/jpeg"
            urlString.endsWith(".png", true) -> "image/png"
            urlString.endsWith(".gif", true) -> "image/gif"
            urlString.endsWith(".svg", true) -> "image/svg+xml"
            urlString.endsWith(".webp", true) -> "image/webp"
            urlString.endsWith(".js", true) -> "application/javascript"
            urlString.endsWith(".html", true) || urlString.endsWith(".xhtml", true) -> "text/html"
            urlString.endsWith(".ttf", true) || urlString.endsWith(".otf", true) -> "font/ttf"
            urlString.endsWith(".woff", true) -> "font/woff"
            urlString.endsWith(".woff2", true) -> "font/woff2"
            else -> "application/octet-stream"
        }
    }

    /**
     * Build list of possible paths to try for a resource
     */
    fun buildPossiblePaths(urlString: String): List<String> {
        val possiblePaths = mutableListOf<String>()

        possiblePaths.add(urlString)

        val path = urlString
            .removePrefix("file://")
            .removePrefix("/")
            .substringAfter("://")
            .substringAfter("/")

        if (path.isNotEmpty() && path != urlString) {
            possiblePaths.add(path)
        }

        val filename = path.substringAfterLast("/")
        if (filename.isNotEmpty() && filename != path) {
            possiblePaths.add(filename)

            // Add common image directory paths
            for (dir in IMAGE_DIRECTORIES) {
                possiblePaths.add("$dir/$filename")
            }
        }

        return possiblePaths.distinct()
    }

    /**
     * Try to load a resource from the publication
     */
    suspend fun loadResource(publication: Publication, possiblePaths: List<String>): Pair<ByteArray, String>? {
        for (possiblePath in possiblePaths) {
            val url = Url(possiblePath)
            if (url != null) {
                val resource = publication.get(url)
                val bytes = resource?.read()?.getOrNull()
                if (bytes != null && bytes.isNotEmpty()) {
                    return Pair(bytes, possiblePath)
                }
            }
        }
        return null
    }

    /**
     * Preprocess EPUB content - fix image paths and embed images as base64
     */
    suspend fun preprocessEpubContent(
        content: String,
        publication: Publication,
        currentHref: String
    ): String {
        return withContext(Dispatchers.IO) {
            var processedContent = content

            // Find all img tags and process them
            val imgRegex = Regex("""<img[^>]*src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
            val matches = imgRegex.findAll(content)

            for (match in matches) {
                val originalTag = match.value
                val imagePath = match.groupValues[1]

                // Skip data URLs
                if (imagePath.startsWith("data:")) continue

                // Try to load and embed image
                val base64Image = loadImageAsBase64(publication, imagePath, currentHref)
                if (base64Image != null) {
                    val mimeType = when {
                        imagePath.endsWith(".png", true) -> "image/png"
                        imagePath.endsWith(".gif", true) -> "image/gif"
                        imagePath.endsWith(".svg", true) -> "image/svg+xml"
                        imagePath.endsWith(".webp", true) -> "image/webp"
                        else -> "image/jpeg"
                    }
                    val newTag = originalTag.replace(imagePath, "data:$mimeType;base64,$base64Image")
                    processedContent = processedContent.replace(originalTag, newTag)
                    Log.d(TAG, "Embedded img: $imagePath")
                }
            }

            // Process SVG image tags
            val svgImageRegex = Regex("""<image[^>]*xlink:href=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
            val svgMatches = svgImageRegex.findAll(processedContent)

            for (match in svgMatches) {
                val originalTag = match.value
                val imagePath = match.groupValues[1]

                if (imagePath.startsWith("data:")) continue

                val base64Image = loadImageAsBase64(publication, imagePath, currentHref)
                if (base64Image != null) {
                    val mimeType = when {
                        imagePath.endsWith(".png", true) -> "image/png"
                        imagePath.endsWith(".gif", true) -> "image/gif"
                        else -> "image/jpeg"
                    }
                    val newTag = originalTag.replace(imagePath, "data:$mimeType;base64,$base64Image")
                    processedContent = processedContent.replace(originalTag, newTag)
                    Log.d(TAG, "Embedded SVG image: $imagePath")
                }
            }

            processedContent
        }
    }

    /**
     * Load an image and convert to base64
     */
    suspend fun loadImageAsBase64(
        publication: Publication,
        imagePath: String,
        currentHref: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val pathsToTry = mutableListOf<String>()

                // Original path
                pathsToTry.add(imagePath)

                // Resolve relative path
                if (imagePath.startsWith("../")) {
                    val currentDir = currentHref.substringBeforeLast("/")
                    var resolved = imagePath
                    var dir = currentDir

                    while (resolved.startsWith("../") && dir.contains("/")) {
                        dir = dir.substringBeforeLast("/")
                        resolved = resolved.removePrefix("../")
                    }

                    if (resolved.startsWith("../")) {
                        resolved = resolved.removePrefix("../")
                    }

                    if (dir.isNotEmpty()) {
                        pathsToTry.add("$dir/$resolved")
                    }
                    pathsToTry.add(resolved)
                }

                // Just filename
                val filename = imagePath.substringAfterLast("/")
                pathsToTry.add(filename)

                // With image directories
                for (dir in IMAGE_DIRECTORIES) {
                    pathsToTry.add("$dir/$filename")
                }

                // Try each path
                for (path in pathsToTry.distinct()) {
                    val url = Url(path)
                    if (url != null) {
                        val resource = publication.get(url)
                        val bytes = resource?.read()?.getOrNull()
                        if (bytes != null && bytes.isNotEmpty()) {
                            return@withContext android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        }
                    }
                }

                null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: $imagePath", e)
                null
            }
        }
    }
}
