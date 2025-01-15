// BrowseViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    private val _selectedBook = MutableStateFlow<OpenLibraryBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    val books = repository.getRecommendedBooks()
        .cachedIn(viewModelScope)

    fun onBookLongPressed(book: OpenLibraryBook) {
        _selectedBook.value = book
    }

    fun clearSelectedBook() {
        _selectedBook.value = null
    }
}