// app/src/main/java/com/example/naivety/repository/BrowseRepository.kt
package com.example.naivety.repository

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

@Singleton
class BrowseRepository @Inject constructor(
    private val api: OpenLibraryApi
) {
    companion object {
        const val PAGE_SIZE = 20
    }

    fun getRecommendedBooks(query: String = ""): Flow<PagingData<OpenLibraryBook>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 2,
                initialLoadSize = PAGE_SIZE
            )
        ) {
            BookPagingSource(api, query)
        }.flow
    }

    suspend fun getBookDetails(bookKey: String): OpenLibraryBookDetail {
        val details = api.getBookDetails(bookKey)
        try {
            val ratings = api.getBookRatings(bookKey)
            return details.copy(
                ratings_average = ratings.summary.average,
                ratings_count = ratings.summary.count
            )
        } catch (e: Exception) {
            return details
        }
    }
}