package com.abundance.naivety.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val filePath: String,
    val thumbnailPath: String,
    val lastReadPage: Int = 0,
    val lastReadPosition: Float = 0f,
    val dateAdded: Long = System.currentTimeMillis(),
    val author: String? = null,
    val totalPages: Int = 0,
    val fileSize: Long = 0L,
    val lastModified: Long = System.currentTimeMillis()
)