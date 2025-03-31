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
                if (query.isBlank()) {
                    // For trending, pagination is not supported by API, so we get all and page locally
                    val response = api.getTrendingBooks()
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.works.mapNotNull { work ->
                            if (work.key != null && work.title != null) {
                                OpenLibraryBook(
                                    key = work.key,
                                    title = work.title,
                                    author = work.author_name?.firstOrNull() ?: "Unknown",
                                    publishedYear = work.first_publish_year ?: 0,
                                    coverUrl = work.cover_i?.let {
                                        "https://covers.openlibrary.org/b/id/$it-L.jpg"
                                    } ?: "",
                                    description = ""
                                )
                            } else null
                        }
                    } else emptyList()
                } else {
                    // For search, pagination is supported
                    val response = api.searchBooks(query, params.loadSize, page)
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.docs.mapNotNull { doc ->
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
            }

            // Calculate paging for client-side pagination
            val pageSize = params.loadSize
            val startPos = (page - 1) * pageSize
            val endPos = minOf(startPos + pageSize, books.size)

            val pageData = if (books.isEmpty() || startPos >= books.size) {
                emptyList()
            } else {
                books.subList(startPos, endPos)
            }

            LoadResult.Page(
                data = pageData,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (pageData.isEmpty() || endPos >= books.size) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("BookPagingSource", "Error loading books", e)
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