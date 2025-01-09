// app/src/main/java/com/example/naivety/viewmodels/PdfViewerViewModel.kt
package com.example.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.ui.pdf.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PdfViewerViewModel : ViewModel() {
    private val _viewerState = MutableStateFlow(PdfViewerState())
    val viewerState = _viewerState.asStateFlow()

    fun updateLoadingState(isLoading: Boolean) {
        _viewerState.value = _viewerState.value.copy(isLoading = isLoading)
    }

    fun addBookmark(page: Int) {
        viewModelScope.launch {
            // Implement bookmark storage logic using Room database
        }
    }

    fun removeBookmark(page: Int) {
        viewModelScope.launch {
            // Implement bookmark removal logic using Room database
        }
    }

    fun updatePage(bookId: String, page: Int, position: Float) {
        viewModelScope.launch {
            // Update reading progress in database
        }
    }

    fun saveSettings(bookId: String?) {
        viewModelScope.launch {
            // Save settings to SharedPreferences or database
        }
    }

    fun toggleControls() {
        _viewerState.update { currentState ->
            currentState.copy(isControlsVisible = !currentState.isControlsVisible)
        }
    }

    fun updatePageCount(current: Int, total: Int) {
        _viewerState.value = _viewerState.value.copy(
            currentPage = current,
            totalPages = total
        )
    }

    fun updateSettings(settings: PdfSettings) {
        _viewerState.value = _viewerState.value.copy(settings = settings)
    }

    fun updateReadingMode(mode: ReadingMode) {
        _viewerState.value = _viewerState.value.copy(readingMode = mode)
    }

    fun updateRotation(mode: RotationMode) {
        _viewerState.value = _viewerState.value.copy(rotation = mode)
    }

    fun updateBrightness(settings: BrightnessSettings) {
        _viewerState.value = _viewerState.value.copy(brightness = settings)
    }
}