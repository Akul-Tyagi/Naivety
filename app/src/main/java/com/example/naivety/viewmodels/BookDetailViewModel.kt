import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// app/src/main/java/com/example/naivety/viewmodels/BookDetailViewModel.kt
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    // Implementation will be added later
}