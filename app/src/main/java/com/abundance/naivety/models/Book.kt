package com.abundance.naivety.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abundance.naivety.utils.BookFileType

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val filePath: String,
    val thumbnailPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastOpened: Long? = null,
    val lastReadPage: Int = 0,
    val lastReadPosition: Float = 0f,
    val totalPages: Int = 0,
    val fileType: String = BookFileType.PDF.name // Add this field
) {
    fun getBookFileType(): BookFileType {
        return try {
            BookFileType.valueOf(fileType)
        } catch (e: Exception) {
            BookFileType.PDF
        }
    }
}
