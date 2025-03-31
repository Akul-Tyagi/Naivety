package com.abundance.naivety.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.abundance.naivety.data.BookPagingSource
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.OpenLibraryApi
import com.abundance.naivety.network.models.OpenLibraryBookDetail
import com.abundance.naivety.utils.BookCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowseRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi,
    private val bookCache: BookCache
) : BrowseRepository {

    // In-memory cache for book details
    private val bookDetailsCache = ConcurrentHashMap<String, OpenLibraryBookDetail>()

    // Cache timeout in milliseconds (10 minutes)
    private val cacheTimeout = 10 * 60 * 1000

    // Cache entries with timestamp
    private val cacheTimes = ConcurrentHashMap<String, Long>()

    // app/src/main/java/com/abundance/naivety/repository/BrowseRepositoryImpl.kt
    override suspend fun getBooksByPage(query: String, page: Int, limit: Int): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            try {
                // We need to simplify the approach due to type inference issues
                if (query.isBlank()) {
                    // Get trending books
                    return@withContext getTrendingBooks()
                } else {
                    // Search with pagination
                    return@withContext searchBooks(query)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList<OpenLibraryBook>()
            }
        }

    override suspend fun searchBooks(query: String): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.searchBooks(query)
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        val books = searchResponse.docs.mapNotNull { doc ->
                            // Safely extract properties
                            val key = doc.key ?: return@mapNotNull null
                            val title = doc.title ?: return@mapNotNull null

                            OpenLibraryBook(
                                key = key,
                                title = title,
                                author = doc.author_name?.firstOrNull() ?: "Unknown",
                                publishedYear = doc.first_publish_year ?: 0,
                                coverUrl = doc.cover_i?.let { coverId ->
                                    "https://covers.openlibrary.org/b/id/$coverId-L.jpg"
                                } ?: "",
                                description = ""
                            )
                        }

                        // Cache books
                        bookCache.cacheBooks(books)
                        return@withContext books
                    }
                }
                return@withContext emptyList<OpenLibraryBook>()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList<OpenLibraryBook>()
            }
        }

    override suspend fun getTrendingBooks(): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTrendingBooks()
                if (response.isSuccessful) {
                    val trendingResponse = response.body()
                    if (trendingResponse != null) {
                        val books = trendingResponse.works.mapNotNull { work ->
                            // Safely extract properties
                            val key = work.key ?: return@mapNotNull null
                            val title = work.title ?: return@mapNotNull null

                            OpenLibraryBook(
                                key = key,
                                title = title,
                                author = work.author_name?.firstOrNull() ?: "Unknown",
                                publishedYear = work.first_publish_year ?: 0,
                                coverUrl = work.cover_i?.let { coverId ->
                                    "https://covers.openlibrary.org/b/id/$coverId-L.jpg"
                                } ?: "",
                                description = ""
                            )
                        }

                        // Cache books
                        bookCache.cacheBooks(books)
                        return@withContext books
                    }
                }
                return@withContext emptyList<OpenLibraryBook>()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList<OpenLibraryBook>()
            }
        }

    override suspend fun getBookDetails(workId: String): OpenLibraryBookDetail =
        withContext(Dispatchers.IO) {
            // Check if cache is valid
            val cachedDetails = bookDetailsCache[workId]
            val cacheTime = cacheTimes[workId] ?: 0L
            val now = System.currentTimeMillis()

            bookCache.getBookDetail(workId)?.let {
                return@withContext it
            }

            if (cachedDetails != null && (now - cacheTime < cacheTimeout)) {
                return@withContext cachedDetails
            }

            try {
                // Fetch book details
                val detailsResponse = api.getBookDetails(workId)
                if (!detailsResponse.isSuccessful) {
                    throw HttpException(detailsResponse)
                }

                val bookDetailResponse = detailsResponse.body() ?: throw Exception("Empty response body")

                // Fetch ratings separately
                var averageRating = 0f
                var ratingsCount = 0
                try {
                    val ratingsResponse = api.getBookRatings(workId)
                    if (ratingsResponse.isSuccessful) {
                        val ratings = ratingsResponse.body()
                        averageRating = ratings?.summary?.average ?: 0f
                        ratingsCount = ratings?.summary?.count ?: 0
                    }
                } catch (e: Exception) {
                    // Log but continue even if ratings fail
                    e.printStackTrace()
                }

                // Create book detail with ratings data
                val bookDetail = OpenLibraryBookDetail(
                    key = bookDetailResponse.key ?: "",
                    title = bookDetailResponse.title ?: "",
                    description = bookDetailResponse.description,
                    authors = bookDetailResponse.authors,
                    first_publish_year = bookDetailResponse.first_publish_year ?: 0,
                    covers = bookDetailResponse.covers ?: emptyList(),
                    averageRating = averageRating,
                    ratings_count = ratingsCount,
                    subjects = bookDetailResponse.subjects,
                    number_of_pages = bookDetailResponse.number_of_pages
                )

                // Update cache
                bookDetailsCache[workId] = bookDetail
                cacheTimes[workId] = now
                bookCache.cacheBookDetail(workId, bookDetail)

                return@withContext bookDetail
            } catch (e: IOException) {
                if (cachedDetails != null) {
                    return@withContext cachedDetails
                }
                throw e
            }
        }
    // Add this method to BrowseRepositoryImpl class
    override fun getRecommendedBooks(query: String): Flow<PagingData<OpenLibraryBook>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            ),
            pagingSourceFactory = { BookPagingSource(api, query) }
        ).flow
    }
}