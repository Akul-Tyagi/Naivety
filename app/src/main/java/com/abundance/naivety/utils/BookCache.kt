// app/src/main/java/com/abundance/naivety/utils/BookCache.kt
package com.abundance.naivety.utils

import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.models.OpenLibraryBookDetail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCache @Inject constructor() {
    private val maxCacheSize = 500 // Maximum books to cache
    private val bookCache = mutableMapOf<String, OpenLibraryBook>()
    private val pagedBooksCache = mutableMapOf<String, List<OpenLibraryBook>>()
    private val detailCache = mutableMapOf<String, OpenLibraryBookDetail>()
    private val cacheTimes = mutableMapOf<String, Long>()
    private val cacheTimeout = 10 * 60 * 1000 // 10 minutes

    fun getPagedBooks(key: String): List<OpenLibraryBook>? {
        val cacheTime = cacheTimes[key] ?: 0L
        val now = System.currentTimeMillis()
        return if (now - cacheTime < cacheTimeout) {
            pagedBooksCache[key]
        } else {
            null
        }
    }

    fun cachePagedBooks(key: String, books: List<OpenLibraryBook>) {
        pagedBooksCache[key] = books
        cacheTimes[key] = System.currentTimeMillis()
        books.forEach { cacheBook(it) }
    }

    fun cacheBook(book: OpenLibraryBook) {
        bookCache[book.key] = book
    }

    fun cacheBooks(books: List<OpenLibraryBook>) {
        books.forEach { cacheBook(it) }
        pruneCache()
    }

    private fun pruneCache() {
        if (bookCache.size > maxCacheSize) {
            val oldestEntries = cacheTimes.entries
                .sortedBy { it.value }
                .take(bookCache.size - maxCacheSize / 2)
                .map { it.key }

            oldestEntries.forEach { key ->
                bookCache.remove(key)
                detailCache.remove(key)
                cacheTimes.remove(key)
            }
        }
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