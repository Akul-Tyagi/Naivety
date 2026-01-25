package com.abundance.naivety.utils

import android.content.Context
import android.net.Uri

enum class BookFileType {
    PDF,
    EPUB,
    UNKNOWN
}

object FileTypeUtils {

    fun getFileType(context: Context, uri: Uri): BookFileType {
        val mimeType = context.contentResolver.getType(uri)
        val fileName = getFileName(context, uri)?.lowercase() ?: ""

        return when {
            mimeType == "application/epub+zip" || fileName.endsWith(".epub") -> BookFileType.EPUB
            mimeType == "application/pdf" || fileName.endsWith(".pdf") -> BookFileType.PDF
            else -> BookFileType.UNKNOWN
        }
    }

    fun getFileType(filePath: String): BookFileType {
        val path = filePath.lowercase()
        return when {
            path.endsWith(".epub") -> BookFileType.EPUB
            path.endsWith(".pdf") -> BookFileType.PDF
            else -> BookFileType.UNKNOWN
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }
}
