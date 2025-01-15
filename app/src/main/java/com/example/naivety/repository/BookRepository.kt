package com.example.naivety.repository

import com.example.naivety.data.AppDatabase
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    val bookDao = appDatabase.bookDao()
    val bookmarkDao = appDatabase.bookmarkDao()

    suspend fun updateReadingProgress(bookId: String, page: Int, position: Float) {
        bookDao.updateReadingProgress(bookId, page, position)
    }
}