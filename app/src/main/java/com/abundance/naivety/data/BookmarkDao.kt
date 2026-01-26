package com.abundance.naivety.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: Bookmark)

    @Delete
    suspend fun removeBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY page ASC")
    fun getBookmarksFlow(bookId: Long): Flow<kotlin.collections.List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY page ASC")
    suspend fun getBookmarks(bookId: Long): kotlin.collections.List<Bookmark>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND page = :page LIMIT 1")
    suspend fun getBookmarkAtPage(bookId: Long, page: Int): Bookmark?

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteAllBookmarksForBook(bookId: Long)

    @Transaction
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE bookId = :bookId AND page = :page)")
    suspend fun isPageBookmarked(bookId: Long, page: Int): Boolean

    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
}