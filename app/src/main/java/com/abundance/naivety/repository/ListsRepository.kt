package com.abundance.naivety.repository

import com.abundance.naivety.data.AppDatabase
import com.abundance.naivety.data.BookListCrossRef
import com.abundance.naivety.data.SavedBook
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.network.OpenLibraryApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import com.abundance.naivety.data.List as UserList

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

    // In ListsRepository.kt, add this method to save book details directly
    suspend fun addBookWithDetailsToList(book: OpenLibraryBook, listId: String) {
        // First add the book-list relationship
        val crossRef = BookListCrossRef(book.key, listId)
        bookListDao.addBookToList(crossRef)

        // Then save the complete book details
        val savedBook = SavedBook(
            bookKey = book.key,
            title = book.title,
            author = book.author,
            coverUrl = book.coverUrl,
            publishedYear = book.publishedYear,
            description = book.description
        )

        // Only insert if doesn't exist or has incomplete data
        val existingBook = savedBookDao.getBookByKey(book.key)
        if (existingBook == null ||
            existingBook.author == "Unknown" ||
            existingBook.author == "Unknown Author" ||
            existingBook.publishedYear == 0) {
            savedBookDao.insertBook(savedBook)
        }
    }

    private suspend fun ensureBookIsSaved(bookKey: String) {
        // Get existing book data - single query instead of multiple
        val existingBook = savedBookDao.getBookByKey(bookKey)

        // If book exists and has valid author, no need to update
        if (existingBook != null &&
            existingBook.author != "Unknown" &&
            existingBook.author != "Unknown Author") {
            return
        }

        try {
            val cleanBookKey = bookKey.removePrefix("/works/")
            val response = api.getBookDetails(cleanBookKey)

            if (response.isSuccessful) {
                val bookDetails = response.body() ?: return

                // Try multiple ways to get author
                var author = "Unknown Author"

                // Method 1: From authors object
                if (bookDetails.authors?.isNotEmpty() == true) {
                    author = bookDetails.authors.firstOrNull()?.name ?: author
                }

                // Method 2: If still unknown, try search endpoint
                if (author == "Unknown Author") {
                    try {
                        val searchResponse = api.searchBooks("key:$cleanBookKey", 1)
                        if (searchResponse.isSuccessful) {
                            val searchAuthor = searchResponse.body()?.docs?.firstOrNull()?.author_name?.firstOrNull()
                            if (!searchAuthor.isNullOrBlank()) {
                                author = searchAuthor
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with unknown author
                    }
                }

                // Create or update book
                val savedBook = SavedBook(
                    bookKey = bookKey,
                    title = bookDetails.title,
                    author = author,
                    coverUrl = bookDetails.covers?.firstOrNull()?.let {
                        "https://covers.openlibrary.org/b/id/$it-L.jpg"
                    } ?: "",
                    publishedYear = bookDetails.first_publish_year ?: 0,
                    description = bookDetails.getDescription()
                )

                savedBookDao.insertBook(savedBook)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isBookInAnyList(bookKey: String): Flow<Boolean> {
        return database.bookListDao().isBookInAnyList(bookKey)
            .map { count -> count > 0 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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

    suspend fun refreshBookData(bookKey: String) {
        ensureBookIsSaved(bookKey) // This will re-fetch the book data if it exists
    }
}