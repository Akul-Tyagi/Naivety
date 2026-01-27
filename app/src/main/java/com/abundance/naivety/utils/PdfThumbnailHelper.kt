package com.abundance.naivety.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfThumbnailHelper {
    suspend fun generateThumbnail(
        context: Context,
        uri: Uri,
        width: Int = 400,
        height: Int = 600
    ): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)

                pdfRenderer.openPage(0).use { page ->
                    // Create bitmap with desired dimensions
                    val bitmap = Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    )

                    // Render the page
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    // Save bitmap to persistent storage (not cache which can be cleared)
                    val thumbnailsDir = File(context.filesDir, "thumbnails")
                    if (!thumbnailsDir.exists()) thumbnailsDir.mkdirs()
                    val fileName = "thumbnail_${System.currentTimeMillis()}.jpg"
                    val thumbnailFile = File(thumbnailsDir, fileName)

                    FileOutputStream(thumbnailFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    return@withContext thumbnailFile.absolutePath
                }
            } ?: throw IllegalStateException("Could not open PDF file")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun deleteThumbnail(context: Context, path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}