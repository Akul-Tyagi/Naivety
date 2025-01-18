// app/src/main/java/com/example/naivety/data/BookPagingSource.kt
package com.example.naivety.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi

class BookPagingSource(
    private val api: OpenLibraryApi,
    private val query: String = ""
) : PagingSource<Int, OpenLibraryBook>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OpenLibraryBook> {
        return try {
            val page = params.key ?: 1
            val limit = params.loadSize

            val response = if (query.isBlank()) {
                api.getTrendingBooks(page, limit)
            } else {
                api.searchBooks(query, page, limit)
            }

            // Log the response for debugging
            Log.d("BookPagingSource", "Response received. Query: $query")

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
                            Log.e("BookPagingSource", "Error mapping work: ${e.message}")
                            null
                        }
                    }
                }
                !query.isBlank() && response.docs != null -> {
                    response.docs.mapNotNull { doc ->
                        try {
                            OpenLibraryBook(
                                key = doc.key ?: return@mapNotNull null,
                                title = doc.title ?: return@mapNotNull null,
                                coverUrl = doc.cover_i?.let {
                                    "https://covers.openlibrary.org/b/id/$it-L.jpg"
                                } ?: "",
                                author = doc.author_name?.firstOrNull() ?: "Unknown Author",
                                publishedYear = doc.first_publish_year ?: 0,
                                description = ""
                            )
                        } catch (e: Exception) {
                            Log.e("BookPagingSource", "Error mapping doc: ${e.message}")
                            null
                        }
                    }
                }
                else -> emptyList()
            }

            // Log the results
            Log.d("BookPagingSource", "Mapped ${books.size} books")

            LoadResult.Page(
                data = books,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (books.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("BookPagingSource", "Error loading books: ${e.message}")
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