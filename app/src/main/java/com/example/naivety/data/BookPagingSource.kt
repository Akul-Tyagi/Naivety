// app/src/main/java/com/example/naivety/data/BookPagingSource.kt
package com.example.naivety.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi
import com.example.naivety.repository.BrowseRepository

class BookPagingSource(
    private val api: OpenLibraryApi,
    private val query: String = ""
) : PagingSource<Int, OpenLibraryBook>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OpenLibraryBook> {
        return try {
            val page = params.key ?: 1

            val response = if (query.isBlank()) {
                api.getTrendingBooks(page)
            } else {
                api.searchBooks(query, page)
            }

            val books = response.works.map { work ->
                OpenLibraryBook(
                    key = work.key,
                    title = work.title,
                    coverUrl = work.cover_i?.let {
                        "https://covers.openlibrary.org/b/id/$it-L.jpg"
                    } ?: "",
                    author = work.author_name?.firstOrNull() ?: "Unknown Author",
                    publishedYear = work.first_publish_year ?: 0,
                    description = ""
                )
            }

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