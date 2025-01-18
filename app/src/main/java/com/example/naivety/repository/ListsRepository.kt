package com.example.naivety.repository

import com.example.naivety.data.AppDatabase
import com.example.naivety.data.List
import com.example.naivety.data.BookListCrossRef
import com.example.naivety.data.SavedBook
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListsRepository @Inject constructor(
    private val database: AppDatabase,
    private val api: OpenLibraryApi
) {
    private val listDao = database.listDao()
    private val bookListDao = database.bookListDao()
    private val savedBookDao = database.savedBookDao()

    fun getAllLists(): Flow<List<List>> = listDao.getAllLists()

    suspend fun getListCount(): Int = listDao.getListCount()

    suspend fun insertList(list: List) = listDao.insertList(list)

    suspend fun updateList(list: List) = listDao.updateList(list)

    suspend fun deleteList(list: List) = listDao.deleteList(list)

    suspend fun updateListOrder(lists: kotlin.collections.List<List>) {
        listDao.updateListOrder(lists)
    }

    suspend fun removeBookFromList(bookKey: String, listId: String) =
        bookListDao.removeBookFromListById(bookKey, listId)

    suspend fun addBookToList(crossRef: BookListCrossRef) {
        bookListDao.addBookToList(crossRef)
        // Ensure book details are saved
        ensureBookIsSaved(crossRef.bookKey)
    }
    private suspend fun ensureBookIsSaved(bookKey: String) {
        if (savedBookDao.getBookByKey(bookKey) == null) {
            try {
                val bookDetails = api.getBookDetails(bookKey.removePrefix("/works/"))
                savedBookDao.insertBook(SavedBook(
                    bookKey = bookKey,
                    title = bookDetails.title,
                    author = bookDetails.authors?.firstOrNull()?.name ?: "Unknown Author",
                    coverUrl = bookDetails.covers?.firstOrNull()?.let {
                        "https://covers.openlibrary.org/b/id/$it-L.jpg"
                    } ?: "",
                    publishedYear = bookDetails.first_publish_year ?: 0,
                    description = bookDetails.getDescription()
                ))
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun getBooksInList(listId: String): Flow<List<OpenLibraryBook>> {
        return bookListDao.getBooksInList(listId)
            .flatMapLatest { crossRefs ->
                if (crossRefs.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    savedBookDao.getBooksByKeys(crossRefs.map { it.bookKey })
                        .map { savedBooks ->
                            savedBooks.map { saved ->
                                OpenLibraryBook(
                                    key = saved.bookKey,
                                    title = saved.title,
                                    author = saved.author,
                                    coverUrl = saved.coverUrl,
                                    publishedYear = saved.publishedYear,
                                    description = saved.description
                                )
                            }
                        }
                }
            }
    }

    fun getListsForBook(bookKey: String): Flow<List<String>> =
        bookListDao.getListsForBook(bookKey)

    suspend fun isBookInList(bookKey: String, listId: String): Boolean =
        bookListDao.isBookInList(bookKey, listId)
}