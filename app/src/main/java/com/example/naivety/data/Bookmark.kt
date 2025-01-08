import androidx.room.Entity
import androidx.room.PrimaryKey

// app/src/main/java/com/example/naivety/data/Bookmark.kt

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val page: Int
)