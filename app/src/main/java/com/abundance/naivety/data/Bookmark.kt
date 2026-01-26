package com.abundance.naivety.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abundance.naivety.models.Book

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookId: Long, // Changed from String to Long to match Book.id type
    val page: Int,
    val title: String? = null,
    val notes: String? = null,
    val dateCreated: Long = System.currentTimeMillis(),
    val colorHex: String = "#FF8E42FF" // Default purple color
)