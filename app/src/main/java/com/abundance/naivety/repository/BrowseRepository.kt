// app/src/main/java/com/abundance/naivety/repository/BrowseRepository.kt
package com.abundance.naivety.repository

import androidx.paging.PagingData
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.models.OpenLibraryBookDetail
import kotlinx.coroutines.flow.Flow

interface BrowseRepository {
    suspend fun searchBooks(query: String): List<OpenLibraryBook>
    suspend fun getBookDetails(workId: String): OpenLibraryBookDetail
    suspend fun getTrendingBooks(): List<OpenLibraryBook>
    fun getRecommendedBooks(query: String): Flow<PagingData<OpenLibraryBook>>
    suspend fun getBooksByPage(query: String, page: Int, limit: Int = 20): List<OpenLibraryBook>
}