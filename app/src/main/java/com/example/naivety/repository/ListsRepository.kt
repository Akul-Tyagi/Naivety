package com.example.naivety.repository

import com.example.naivety.data.AppDatabase
import com.example.naivety.data.BookListCrossRef
import com.example.naivety.data.SavedBook
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.network.OpenLibraryApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import com.example.naivety.data.List as UserList

@Singleton
class ListsRepository @Inject constructor(
    private val database: AppDatabase,
    private val api: OpenLibraryApi
) {
    private val listDao = database.listDao()
    private val bookListDao = database.bookListDao()
    private val savedBookDao = database.savedBookDao()

    fun getAllLists(): Flow<kotlin.collections.List<UserList>> = listDao.getAllLists()

    suspend fun getListCount(): Int = listDao.getListCount()

    suspend fun insertList(list: UserList) = listDao.insertList(list)

    suspend fun updateList(list: UserList) = listDao.updateList(list)

    suspend fun deleteList(list: UserList) = listDao.deleteList(list)

    suspend fun updateListOrder(lists: kotlin.collections.List<UserList>) {
        listDao.updateListOrder(lists)
    }

    suspend fun removeBookFromList(bookKey: String, listId: String) =
        bookListDao.removeBookFromListById(bookKey, listId)

    suspend fun addBookToList(crossRef: BookListCrossRef) {
        bookListDao.addBookToList(crossRef)
        ensureBookIsSaved(crossRef.bookKey)
    }
    private suspend fun ensureBookIsSaved(bookKey: String) {
        if (savedBookDao.getBookByKey(bookKey) == null) {
            try {
                val response = api.getBookDetails(bookKey.removePrefix("/works/"))
                if (response.isSuccessful) {
                    val bookDetails = response.body()
                    if (bookDetails != null) {
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
                    }
                }
            } catch (e: Exception) {
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