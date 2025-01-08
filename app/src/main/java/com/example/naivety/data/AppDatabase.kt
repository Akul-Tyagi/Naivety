import androidx.room.Database
import androidx.room.RoomDatabase

// app/src/main/java/com/example/naivety/data/AppDatabase.kt

@Database(entities = [Bookmark::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}