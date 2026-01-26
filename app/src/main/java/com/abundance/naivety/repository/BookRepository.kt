package com.abundance.naivety.repository

import android.util.Log
import com.abundance.naivety.data.AppDatabase
import com.abundance.naivety.models.Book
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    val bookDao = appDatabase.bookDao()
    val bookmarkDao = appDatabase.bookmarkDao()

    suspend fun updateReadingProgress(bookId: Long, page: Int, position: Float) {
        bookDao.updateReadingProgress(bookId, page, position)
    }

    suspend fun addBook(book: Book) {
        Log.d("BookRepository", "Inserting book: ${book.id}")
        bookDao.insertBook(book)
        Log.d("BookRepository", "Book inserted successfully")
    }
}