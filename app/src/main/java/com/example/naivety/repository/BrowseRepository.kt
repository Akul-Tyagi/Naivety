// app/src/main/java/com/example/naivety/repository/BrowseRepository.kt
package com.example.naivety.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.models.OpenLibraryBookDetail
import com.example.naivety.network.OpenLibraryApi
import com.example.naivety.data.BookPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BrowseRepository"

@Singleton
class BrowseRepository @Inject constructor(
    private val api: OpenLibraryApi
) {
    companion object {
        const val PAGE_SIZE = 20
        private const val DEFAULT_COVER_URL = ""
    }

    fun getRecommendedBooks(query: String = ""): Flow<PagingData<OpenLibraryBook>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = PAGE_SIZE,
            )
        ) {
            BookPagingSource(api, query)
        }.flow
    }

    suspend fun getBookDetails(bookKey: String): OpenLibraryBookDetail {
        return try {
            val details = api.getBookDetails(bookKey)
            try {
                // Get ratings
                val ratings = api.getBookRatings(bookKey)

                // Get editions to find page count
                val editions = api.getBookEditions(bookKey)
                val pageCount = editions.entries
                    .firstOrNull { it.number_of_pages != null }
                    ?.number_of_pages

                details.copy(
                    ratings_average = ratings.summary.average,
                    ratings_count = ratings.summary.count,
                    number_of_pages = pageCount ?: 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting additional book data for $bookKey: ${e.message}")
                details
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting book details for $bookKey: ${e.message}")
            throw e
        }
    }

    suspend fun searchBooks(query: String): List<OpenLibraryBook> {
        return try {
            Log.d(TAG, "Searching books with query: $query")
            val response = api.searchBooks(
                query = query,
                page = 1,
                limit = PAGE_SIZE
            )

            response.docs?.mapNotNull { doc ->
                try {
                    OpenLibraryBook(
                        key = doc.key ?: return@mapNotNull null,
                        title = doc.title ?: return@mapNotNull null,
                        coverUrl = doc.cover_i?.let {
                            "https://covers.openlibrary.org/b/id/$it-L.jpg"
                        } ?: DEFAULT_COVER_URL,
                        author = doc.author_name?.firstOrNull() ?: "Unknown Author",
                        publishedYear = doc.first_publish_year ?: 0,
                        description = ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping book doc: ${e.message}")
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching books: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTrendingBooks(page: Int = 1): List<OpenLibraryBook> {
        return try {
            Log.d(TAG, "Getting trending books page: $page")
            val response = api.getTrendingBooks(
                page = page,
                limit = PAGE_SIZE
            )

            response.works?.mapNotNull { work ->
                try {
                    OpenLibraryBook(
                        key = work.key ?: return@mapNotNull null,
                        title = work.title ?: return@mapNotNull null,
                        coverUrl = work.cover_i?.let {
                            "https://covers.openlibrary.org/b/id/$it-L.jpg"
                        } ?: DEFAULT_COVER_URL,
                        author = work.author_name?.firstOrNull() ?: "Unknown Author",
                        publishedYear = work.first_publish_year ?: 0,
                        description = ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping trending book: ${e.message}")
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting trending books: ${e.message}")
            emptyList()
        }
    }

    private fun handleApiError(e: Exception, operation: String): Nothing {
        Log.e(TAG, "Error during $operation: ${e.message}")
        throw e
    }

    private fun createCoverUrl(coverId: Long?): String {
        return coverId?.let {
            "https://covers.openlibrary.org/b/id/$it-L.jpg"
        } ?: DEFAULT_COVER_URL
    }
}