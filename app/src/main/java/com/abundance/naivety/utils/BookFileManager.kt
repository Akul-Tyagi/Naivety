package com.abundance.naivety.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

/**
 * Manages book file storage by copying files to internal storage.
 * This ensures books remain accessible even after app updates or reinstalls,
 * as content URIs lose permissions but internal files are always accessible.
 */
object BookFileManager {

    private const val TAG = "BookFileManager"
    private const val BOOKS_DIR = "books"
    private const val PDF_DIR = "pdf"
    private const val EPUB_DIR = "epub"

    /**
     * Copy a book file from a content URI to internal storage.
     * Returns the path to the copied file, or null if copy failed.
     */
    fun copyBookToInternalStorage(
        context: Context,
        sourceUri: Uri,
        fileType: BookFileType,
        originalFileName: String
    ): String? {
        return try {
            val subDir = when (fileType) {
                BookFileType.PDF -> PDF_DIR
                BookFileType.EPUB -> EPUB_DIR
                else -> PDF_DIR
            }

            val booksDir = File(context.filesDir, BOOKS_DIR)
            val typeDir = File(booksDir, subDir)
            if (!typeDir.exists()) {
                typeDir.mkdirs()
            }

            val uniqueId = generateUniqueId(sourceUri.toString(), System.currentTimeMillis())
            val extension = when (fileType) {
                BookFileType.PDF -> ".pdf"
                BookFileType.EPUB -> ".epub"
                else -> ""
            }

            val sanitizedName = sanitizeFileName(originalFileName.substringBeforeLast('.'))
            val targetFileName = "${sanitizedName}_${uniqueId}$extension"
            val targetFile = File(typeDir, targetFileName)

            if (targetFile.exists()) {
                Log.d(TAG, "File already exists: ${targetFile.absolutePath}")
                return targetFile.absolutePath
            }

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            } ?: throw IOException("Could not open input stream for URI: $sourceUri")

            Log.d(TAG, "Successfully copied book to: ${targetFile.absolutePath}")
            targetFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy book to internal storage", e)
            null
        }
    }

    /**
     * Delete a book file from internal storage.
     */
    fun deleteBookFile(filePath: String): Boolean {
        return try {
            if (isInternalBookFile(filePath)) {
                val file = File(filePath)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "Deleted book file: $filePath, success: $deleted")
                    deleted
                } else false
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting book file: $filePath", e)
            false
        }
    }

    /**
     * Check if a file path points to an internal book file (vs a content URI).
     */
    fun isInternalBookFile(filePath: String): Boolean {
        return filePath.startsWith("/") &&
               (filePath.contains("/$BOOKS_DIR/$PDF_DIR/") ||
                filePath.contains("/$BOOKS_DIR/$EPUB_DIR/"))
    }

    /**
     * Check if a path is a content URI.
     */
    fun isContentUri(path: String): Boolean {
        return path.startsWith("content://")
    }

    /**
     * Check if a book file exists and is accessible.
     */
    fun isFileAccessible(filePath: String): Boolean {
        return try {
            if (isContentUri(filePath)) {
                false // Content URIs need separate permission check
            } else {
                val file = File(filePath)
                file.exists() && file.canRead()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get Uri for a file path (internal or content URI).
     */
    fun getFileUri(filePath: String): Uri {
        return if (isContentUri(filePath)) {
            Uri.parse(filePath)
        } else {
            Uri.fromFile(File(filePath))
        }
    }

    private fun generateUniqueId(uri: String, timestamp: Long): String {
        val input = "$uri$timestamp"
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.take(8).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            timestamp.toString(16).takeLast(8)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(50)
    }
}
