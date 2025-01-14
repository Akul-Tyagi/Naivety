// app/src/main/java/com/example/naivety/viewmodels/BrowseViewModel.kt

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

    val books = repository
        .getRecommendedBooks()
        .cachedIn(viewModelScope)

    private val _selectedBook = MutableStateFlow<OpenLibraryBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    fun onBookLongPressed(book: OpenLibraryBook) {
        _selectedBook.value = book
    }

    fun clearSelectedBook() {
        _selectedBook.value = null
    }
}