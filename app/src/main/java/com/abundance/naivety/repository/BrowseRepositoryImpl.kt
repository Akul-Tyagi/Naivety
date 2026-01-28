package com.abundance.naivety.repository

import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowseRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi,
    private val bookCache: BookCache,
    private val listsRepository: ListsRepository
) : BrowseRepository {

    // Loading state for bestsellers (can be exposed if needed by UI)
    private val _isLoadingBestsellers = MutableStateFlow(false)

    override suspend fun getBooksByPage(query: String, page: Int, limit: Int): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            // Cache check with proper key
            val cacheKey = if (query.isBlank()) "bestsellers_$page" else "search_${query}_$page"
            bookCache.getPagedBooks(cacheKey)?.let {
                Log.d("BrowseRepository", "Returning ${it.size} books for page $page from cache")
                return@withContext it
            }

            try {
                // Page 1 is handled differently only if query is blank
                if (query.isBlank() && page == 1) {
                    return@withContext getBestsellerBooks()
                }

                // For search queries and subsequent bestseller pages
                val effectiveQuery = if (query.isBlank()) "bestseller" else query
                Log.d("BrowseRepository", "Fetching ${if(query.isBlank()) "bestsellers" else "search results"} page $page...")

                val response = if (query.isBlank()) {
                    // For bestsellers, use dedicated method with page parameter
                    api.getBestsellerBooks(page = page)
                } else {
                    // For search, use regular search
                    api.searchBooks(effectiveQuery, limit, page)
                }

                if (response.isSuccessful) {
                    val books = response.body()?.docs?.mapNotNull { doc ->
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
                    } ?: emptyList()

                    // Cache the results with appropriate key
                    bookCache.cachePagedBooks(cacheKey, books)
                    return@withContext books
                } else {
                    // Throw HttpException for server errors so ViewModel can handle them
                    Log.e("BrowseRepository", "API error: ${response.code()} ${response.message()}")
                    throw HttpException(response)
                }
            } catch (e: Exception) {
                Log.e("BrowseRepository", "Error fetching page $page: ${e.message}")
                return@withContext emptyList()
            }
        }

    // Rename this method to avoid the override conflict
    suspend fun searchBooksWithPagination(query: String, page: Int = 1, limit: Int = 20): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            // Check for cached results first
            val cacheKey = "search_${query}_$page"
            bookCache.getPagedBooks(cacheKey)?.let {
                Log.d("BrowseRepository", "Returning ${it.size} search results for '$query' page $page from cache")
                return@withContext it
            }

            try {
                Log.d("BrowseRepository", "Searching for '$query' page $page")
                val response = api.searchBooks(
                    query = query,
                    limit = limit,
                    page = page,
                    fields = "key,title,author_name,first_publish_year,cover_i",
                    sort = "readinglog"
                )

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        val books = searchResponse.docs.mapNotNull { doc ->
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

                        Log.d("BrowseRepository", "Found ${books.size} books for '$query' page $page")
                        bookCache.cacheBooks(books)
                        bookCache.cachePagedBooks(cacheKey, books)
                        return@withContext books
                    }
                } else {
                    Log.e("BrowseRepository", "Search API error: ${response.code()} ${response.message()}")
                }
                return@withContext emptyList()
            } catch (e: Exception) {
                Log.e("BrowseRepository", "Error searching books: ${e.message}")
                return@withContext emptyList()
            }
        }

    // Rename this method from getTrendingBooks to getBestsellerBooks
    override suspend fun getBestsellerBooks(): List<OpenLibraryBook> =
        withContext(Dispatchers.IO) {
            try {
                _isLoadingBestsellers.value = true

                // Check cache first
                bookCache.getPagedBooks("bestsellers_1")?.let {
                    Log.d("BrowseRepository", "Using cached bestseller books")
                    _isLoadingBestsellers.value = false
                    return@withContext it
                }

                Log.d("BrowseRepository", "Fetching bestseller books...")

                // Use the dedicated method with proper parameters
                val response = api.getBestsellerBooks()

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        val books = searchResponse.docs.mapNotNull { doc ->
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

                        Log.d("BrowseRepository", "Got ${books.size} bestseller books")
                        bookCache.cacheBooks(books)
                        bookCache.cachePagedBooks("bestsellers_1", books)
                        _isLoadingBestsellers.value = false
                        return@withContext books
                    }
                } else {
                    Log.e("BrowseRepository", "Bestsellers API error: ${response.code()} ${response.message()}")
                    _isLoadingBestsellers.value = false
                    throw HttpException(response)
                }

                _isLoadingBestsellers.value = false
                return@withContext emptyList()
            } catch (e: Exception) {
                Log.e("BrowseRepository", "Error in getBestsellerBooks: ${e.message}")
                _isLoadingBestsellers.value = false
                return@withContext emptyList()
            }
        }


    // Fix the searchBooks method to prevent infinite recursion
    override suspend fun searchBooks(query: String): List<OpenLibraryBook> =
        searchBooksWithPagination(query)


    // In BrowseRepositoryImpl.kt, add:
    override fun isBookInAnyList(bookKey: String): Flow<Boolean> {
        return listsRepository.isBookInAnyList(bookKey)
    }

    override suspend fun getBookDetails(workId: String): OpenLibraryBookDetail =
        withContext(Dispatchers.IO) {
            // Check cache first
            bookCache.getBookDetail(workId)?.let {
                return@withContext it
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
                    Log.w("BrowseRepository", "Failed to fetch ratings: ${e.message}")
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

                // Cache the result
                bookCache.cacheBookDetail(workId, bookDetail)

                return@withContext bookDetail
            } catch (e: IOException) {
                // Try returning cached data on network error
                bookCache.getBookDetail(workId)?.let { return@withContext it }
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