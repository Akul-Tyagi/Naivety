package com.example.naivety.models

import java.util.UUID

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val filePath: String,
    val thumbnailPath: String,
    val lastReadPage: Int = 0,
    val lastReadPosition: Float = 0f,
    val dateAdded: Long = System.currentTimeMillis()
)