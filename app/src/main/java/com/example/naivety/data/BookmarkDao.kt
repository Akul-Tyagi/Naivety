import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// app/src/main/java/com/example/naivety/data/BookmarkDao.kt

@Dao
interface BookmarkDao {
    @Insert
    suspend fun addBookmark(bookmark: Bookmark)

    @Delete
    suspend fun removeBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId")
    suspend fun getBookmarks(bookId: String): List<Bookmark>
}



