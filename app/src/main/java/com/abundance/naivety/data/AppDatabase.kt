package com.abundance.naivety.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abundance.naivety.models.Book
import kotlinx.coroutines.flow.Flow
import java.sql.Date
import com.abundance.naivety.data.List as UserList // Rename to avoid conflict with kotlin.collections.List

@Database(
    entities = [Book::class, Bookmark::class, UserList::class, BookListCrossRef::class, SavedBook::class, ReadingDay::class],
    version = 2,
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

        // Migration from version 1 to version 2
        // v1 = PDF only support, v2 = PDF + EPUB support with updated schema
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add bookType column with default value "PDF" for existing entries in reading_days
                db.execSQL("ALTER TABLE reading_days ADD COLUMN bookType TEXT NOT NULL DEFAULT 'PDF'")
                // Add chaptersRead column with default value 0
                db.execSQL("ALTER TABLE reading_days ADD COLUMN chaptersRead INTEGER NOT NULL DEFAULT 0")

                // Recreate the books table with new schema
                // The old schema had: id (TEXT), title, filePath, fileSize, lastModified, thumbnailPath (NOT NULL), dateAdded, lastReadPage, lastReadPosition, totalPages
                // The new schema has: id (INTEGER auto), title, author, filePath, thumbnailPath (nullable), dateAdded, lastOpened, lastReadPage, lastReadPosition, totalPages, fileType

                // Step 1: Create new table with correct schema
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS books_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        author TEXT,
                        filePath TEXT NOT NULL,
                        thumbnailPath TEXT,
                        dateAdded INTEGER NOT NULL,
                        lastOpened INTEGER,
                        lastReadPage INTEGER NOT NULL,
                        lastReadPosition REAL NOT NULL,
                        totalPages INTEGER NOT NULL,
                        fileType TEXT NOT NULL DEFAULT 'PDF'
                    )
                """.trimIndent())

                // Step 2: Copy data from old table to new table (mapping old columns to new)
                db.execSQL("""
                    INSERT INTO books_new (title, filePath, thumbnailPath, dateAdded, lastReadPage, lastReadPosition, totalPages, fileType)
                    SELECT title, filePath, thumbnailPath, dateAdded, lastReadPage, lastReadPosition, totalPages, 'PDF'
                    FROM books
                """.trimIndent())

                // Step 3: Drop old table
                db.execSQL("DROP TABLE books")

                // Step 4: Rename new table to original name
                db.execSQL("ALTER TABLE books_new RENAME TO books")

                // Step 5: Recreate bookmarks table with correct bookId type (INTEGER to match books.id)
                // First, drop the old bookmarks table (data will be lost, but necessary for schema fix)
                db.execSQL("DROP TABLE IF EXISTS bookmarks")

                // Create new bookmarks table with correct schema
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        bookId INTEGER NOT NULL,
                        page INTEGER NOT NULL,
                        title TEXT,
                        notes TEXT,
                        dateCreated INTEGER NOT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#FF8E42FF',
                        FOREIGN KEY (bookId) REFERENCES books(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create index on bookId for bookmarks
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_bookId ON bookmarks (bookId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naivety_database"
                )
                    .addMigrations(MIGRATION_1_2)
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

    @Query("SELECT COUNT(*) FROM book_list_cross_ref WHERE bookKey = :bookKey")
    fun isBookInAnyList(bookKey: String): Flow<Int>

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
    suspend fun getBookById(bookId: Long): Book?

    @Query("UPDATE books SET lastReadPage = :page, lastReadPosition = :position WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: Long, page: Int, position: Float)
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