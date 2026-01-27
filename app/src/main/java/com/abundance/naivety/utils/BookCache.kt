// app/src/main/java/com/abundance/naivety/utils/BookCache.kt
package com.abundance.naivety.utils

import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.models.OpenLibraryBookDetail
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCache @Inject constructor() {
    private val maxCacheSize = 500 // Maximum books to cache
    private val maxPagedCacheSize = 50 // Maximum paged results to cache

    // Thread-safe collections for concurrent access
    private val bookCache = ConcurrentHashMap<String, OpenLibraryBook>()
    private val pagedBooksCache = ConcurrentHashMap<String, List<OpenLibraryBook>>()
    private val detailCache = ConcurrentHashMap<String, OpenLibraryBookDetail>()
    private val cacheTimes = ConcurrentHashMap<String, Long>()
    private val cacheTimeout = 10 * 60 * 1000L // 10 minutes

    fun getPagedBooks(key: String): List<OpenLibraryBook>? {
        val cacheTime = cacheTimes[key] ?: 0L
        val now = System.currentTimeMillis()
        return if (now - cacheTime < cacheTimeout) {
            pagedBooksCache[key]
        } else {
            // Remove expired entry
            pagedBooksCache.remove(key)
            cacheTimes.remove(key)
            null
        }
    }

    fun cachePagedBooks(key: String, books: List<OpenLibraryBook>) {
        pagedBooksCache[key] = books
        cacheTimes[key] = System.currentTimeMillis()
        books.forEach { cacheBook(it) }
        prunePagedCache()
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
            val keysToRemove = bookCache.keys.take(bookCache.size - maxCacheSize / 2)
            keysToRemove.forEach { key ->
                bookCache.remove(key)
                detailCache.remove(key)
            }
        }
    }

    private fun prunePagedCache() {
        if (pagedBooksCache.size > maxPagedCacheSize) {
            // Remove oldest entries based on cache times
            val oldestEntries = cacheTimes.entries
                .filter { pagedBooksCache.containsKey(it.key) }
                .sortedBy { it.value }
                .take(pagedBooksCache.size - maxPagedCacheSize / 2)
                .map { it.key }

            oldestEntries.forEach { key ->
                pagedBooksCache.remove(key)
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
            detailCache.remove(key)
            null
        }
    }

    fun cacheBookDetail(key: String, detail: OpenLibraryBookDetail) {
        detailCache[key] = detail
        cacheTimes[key] = System.currentTimeMillis()
    }

    fun getBook(key: String): OpenLibraryBook? = bookCache[key]

    fun clear() {
        bookCache.clear()
        pagedBooksCache.clear()
        detailCache.clear()
        cacheTimes.clear()
    }
}