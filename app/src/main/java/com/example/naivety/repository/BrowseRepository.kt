import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// app/src/main/java/com/example/naivety/repository/BrowseRepository.kt

class BrowseRepository @Inject constructor(
    private val api: OpenLibraryApi
) {
    fun getRecommendedBooks(): Flow<PagingData<OpenLibraryBook>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {
            BookPagingSource(api)
        }.flow
    }
}