// app/src/main/java/com/example/naivety/data/BookPagingSource.kt
package com.example.naivety.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi
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
            val response = withContext(Dispatchers.IO) {
                if (query.isBlank()) {
                    api.getTrendingBooks(page, params.loadSize)
                } else {
                    api.searchBooks(query, page, params.loadSize)
                }
            }

            // Log the response for debugging

            val books = when {
                query.isBlank() && response.works != null -> {
                    response.works.mapNotNull { work ->
                        try {
                            OpenLibraryBook(
                                key = work.key ?: return@mapNotNull null,
                                title = work.title ?: return@mapNotNull null,
                                coverUrl = work.cover_i?.let {
                                    "https://covers.openlibrary.org/b/id/$it-L.jpg"
                                } ?: "",
                                author = work.author_name?.firstOrNull() ?: "Unknown Author",
                                publishedYear = work.first_publish_year ?: 0,
                                description = ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                else -> emptyList()
            }

            // Log the results

            LoadResult.Page(
                data = books,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (books.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
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