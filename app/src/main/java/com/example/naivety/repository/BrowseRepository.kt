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
    fun getRecommendedBooks(): Flow<PagingData<OpenLibraryBook>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {
            BookPagingSource(api)
        }.flow
    }

    suspend fun getBookDetails(bookKey: String): OpenLibraryBookDetail {
        return api.getBookDetails(bookKey)
    }
}