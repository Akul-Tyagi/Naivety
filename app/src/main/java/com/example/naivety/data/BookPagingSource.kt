// app/src/main/java/com/example/naivety/data/BookPagingSource.kt
package com.example.naivety.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi

class BookPagingSource(
    private val api: OpenLibraryApi
) : PagingSource<Int, OpenLibraryBook>() {
    override fun getRefreshKey(state: PagingState<Int, OpenLibraryBook>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OpenLibraryBook> {
        return try {
            val page = params.key ?: 1
            val response = api.getBooksBySubject("fiction")

            val books = response.works.map { work ->
                OpenLibraryBook(
                    key = work.key,
                    title = work.title,
                    coverUrl = work.cover_id?.let {
                        "https://covers.openlibrary.org/b/id/$it-L.jpg"
                    } ?: "",
                    author = work.authors?.firstOrNull()?.name ?: "Unknown Author",
                    publishedYear = work.first_publish_year ?: 0,
                    description = "" // Will be loaded in detail view
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
}