// app/src/main/java/com/example/naivety/utils/BookCache.kt
package com.example.naivety.utils

import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.models.OpenLibraryBookDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCache @Inject constructor() {
    private val bookCache = mutableMapOf<String, OpenLibraryBook>()
    private val detailCache = mutableMapOf<String, OpenLibraryBookDetail>()
    private val cacheTimes = mutableMapOf<String, Long>()
    private val cacheTimeout = 10 * 60 * 1000 // 10 minutes

    fun getBook(key: String): OpenLibraryBook? = bookCache[key]

    fun cacheBook(book: OpenLibraryBook) {
        bookCache[book.key] = book
    }

    fun cacheBooks(books: List<OpenLibraryBook>) {
        books.forEach { cacheBook(it) }
    }

    fun getBookDetail(key: String): OpenLibraryBookDetail? {
        val cacheTime = cacheTimes[key] ?: 0L
        val now = System.currentTimeMillis()
        return if (now - cacheTime < cacheTimeout) {
            detailCache[key]
        } else {
            null
        }
    }

    fun cacheBookDetail(key: String, detail: OpenLibraryBookDetail) {
        detailCache[key] = detail
        cacheTimes[key] = System.currentTimeMillis()
    }

    fun clear() {
        bookCache.clear()
        detailCache.clear()
        cacheTimes.clear()
    }
}