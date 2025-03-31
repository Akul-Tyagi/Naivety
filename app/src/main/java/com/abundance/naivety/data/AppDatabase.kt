package com.abundance.naivety.data

import android.content.Context
import androidx.room.*
import com.abundance.naivety.models.Book
import kotlinx.coroutines.flow.Flow
import java.sql.Date
import com.abundance.naivety.data.List as UserList // Rename to avoid conflict with kotlin.collections.List

@Database(
    entities = [Book::class, Bookmark::class, UserList::class, BookListCrossRef::class, SavedBook::class, ReadingDay::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun bookDao(): BookDao
    abstract fun listDao(): ListDao
    abstract fun bookListDao(): BookListDao
    abstract fun savedBookDao(): SavedBookDao
    abstract fun readingDayDao(): ReadingDayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naivety_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface SavedBookDao {
    @Query("SELECT * FROM saved_books WHERE bookKey IN (:bookKeys)")
    fun getBooksByKeys(bookKeys: kotlin.collections.List<String>): Flow<kotlin.collections.List<SavedBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: SavedBook)

    @Query("SELECT * FROM saved_books WHERE bookKey = :bookKey")
    suspend fun getBookByKey(bookKey: String): SavedBook?
}

@Dao
interface ListDao {
    @Query("SELECT * FROM lists ORDER BY ordinal ASC")
    fun getAllLists(): Flow<kotlin.collections.List<UserList>>

    @Query("SELECT MAX(ordinal) FROM lists")
    suspend fun getMaxOrdinal(): Int?

    @Transaction
    suspend fun updateListOrder(lists: kotlin.collections.List<UserList>) {
        lists.forEachIndexed { index, list ->
            updateListOrdinal(list.id, index)
        }
    }

    @Query("UPDATE lists SET ordinal = :ordinal WHERE id = :listId")
    suspend fun updateListOrdinal(listId: String, ordinal: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: UserList)

    @Update
    suspend fun updateList(list: UserList)

    @Delete
    suspend fun deleteList(list: UserList)

    @Query("SELECT COUNT(*) FROM lists")
    suspend fun getListCount(): Int
}

@Dao
interface BookListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookToList(crossRef: BookListCrossRef)

    @Delete
    suspend fun removeBookFromList(crossRef: BookListCrossRef)

    @Query("SELECT * FROM book_list_cross_ref WHERE listId = :listId")
    fun getBooksInList(listId: String): Flow<kotlin.collections.List<BookListCrossRef>>

    @Query("SELECT listId FROM book_list_cross_ref WHERE bookKey = :bookKey")
    fun getListsForBook(bookKey: String): Flow<kotlin.collections.List<String>>

    @Query("DELETE FROM book_list_cross_ref WHERE bookKey = :bookKey AND listId = :listId")
    suspend fun removeBookFromListById(bookKey: String, listId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM book_list_cross_ref WHERE bookKey = :bookKey AND listId = :listId)")
    suspend fun isBookInList(bookKey: String, listId: String): Boolean
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<kotlin.collections.List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): Book?

    @Query("UPDATE books SET lastReadPage = :page, lastReadPosition = :position WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: String, page: Int, position: Float)
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}