// app/src/main/java/com/example/naivety/repository/BrowseRepository.kt
package com.example.naivety.repository

import androidx.paging.PagingData
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.models.OpenLibraryBookDetail
import kotlinx.coroutines.flow.Flow

interface BrowseRepository {
    suspend fun searchBooks(query: String): List<OpenLibraryBook>
    suspend fun getBookDetails(workId: String): OpenLibraryBookDetail
    suspend fun getTrendingBooks(): List<OpenLibraryBook>
    fun getRecommendedBooks(query: String): Flow<PagingData<OpenLibraryBook>>
    suspend fun getBooksByPage(query: String, page: Int, limit: Int = 20): List<OpenLibraryBook>
}