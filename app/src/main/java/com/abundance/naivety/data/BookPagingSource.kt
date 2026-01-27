package com.abundance.naivety.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.OpenLibraryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookPagingSource(
    private val api: OpenLibraryApi,
    private val query: String = ""
) : PagingSource<Int, OpenLibraryBook>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OpenLibraryBook> {
        return try {
            val page = params.key ?: 1

            // Use withContext to ensure network call is on IO thread
            val books = withContext(Dispatchers.IO) {
                val response = if (query.isBlank()) {
                    // For bestsellers/trending
                    api.getBestsellerBooks(page = page, limit = params.loadSize)
                } else {
                    // For search queries
                    api.searchBooks(query, params.loadSize, page)
                }

                if (response.isSuccessful && response.body() != null) {
                    val docs = response.body()?.docs ?: emptyList()
                    docs.mapNotNull { doc ->
                        if (doc.key != null && doc.title != null) {
                            OpenLibraryBook(
                                key = doc.key,
                                title = doc.title,
                                author = doc.author_name?.firstOrNull() ?: "Unknown",
                                publishedYear = doc.first_publish_year ?: 0,
                                coverUrl = doc.cover_i?.let {
                                    "https://covers.openlibrary.org/b/id/$it-L.jpg"
                                } ?: "",
                                description = ""
                            )
                        } else null
                    }
                } else emptyList()
            }

            LoadResult.Page(
                data = books,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (books.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("BookPagingSource", "Error loading page: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, OpenLibraryBook>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}