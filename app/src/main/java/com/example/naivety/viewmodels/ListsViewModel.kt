package com.example.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.data.List
import com.example.naivety.data.BookListCrossRef
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.repository.ListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: ListsRepository
) : ViewModel() {
    private val _lists = MutableStateFlow<List<List>>(emptyList())
    val lists = _lists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedListId = MutableStateFlow<String?>(null)
    val selectedListId = _selectedListId.asStateFlow()

    init {
        loadLists()
        createDefaultListIfNeeded()
    }

    private fun loadLists() {
        viewModelScope.launch {
            repository.getAllLists().collect { lists ->
                _lists.value = lists
                if (_selectedListId.value == null && lists.isNotEmpty()) {
                    _selectedListId.value = lists.first().id
                }
            }
        }
    }

    private fun createDefaultListIfNeeded() {
        viewModelScope.launch {
            if (repository.getListCount() == 0) {
                createNewList("Custom")
            }
        }
    }

    fun createNewList(name: String = "Custom") {
        viewModelScope.launch {
            val count = repository.getListCount()
            val newListName = if (count > 0) "$name${count + 1}" else name
            val newList = List(name = newListName)
            repository.insertList(newList)
        }
    }

    fun updateListName(listId: String, newName: String) {
        viewModelScope.launch {
            _lists.value.find { it.id == listId }?.let { list ->
                repository.updateList(list.copy(name = newName))
            }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            _lists.value.find { it.id == listId }?.let { list ->
                repository.deleteList(list)
            }
        }
    }

    fun selectList(listId: String) {
        _selectedListId.value = listId
    }

    fun addBookToList(bookKey: String, listId: String) {
        viewModelScope.launch {
            repository.addBookToList(BookListCrossRef(bookKey, listId))
        }
    }

    fun removeBookFromList(bookKey: String, listId: String) {
        viewModelScope.launch {
            repository.removeBookFromList(bookKey, listId)
        }
    }

    fun loadBooksForList(listId: String): Flow<List<OpenLibraryBook>> {
        return repository.getBooksInList(listId).map { crossRefs ->
            // Here you'll need to fetch book details from your API
            // This is a placeholder - implement actual book fetching
            crossRefs.mapNotNull { crossRef ->
                // Fetch book details using the bookKey
                // You'll need to implement this in your repository
                repository.getBookDetails(crossRef.bookKey)
            }
        }
    }

    fun toggleBookInList(bookKey: String, listId: String) {
        viewModelScope.launch {
            val isInList = repository.isBookInList(bookKey, listId)
            if (isInList) {
                repository.removeBookFromList(bookKey, listId)
            } else {
                repository.addBookToList(BookListCrossRef(bookKey, listId))
            }
        }
    }

    fun reorderLists(newOrder: kotlin.collections.List<List>) {
        viewModelScope.launch {
            try {
                repository.updateListOrder(newOrder)
            } catch (e: Exception) {
                // Handle error
                _error.value = "Failed to reorder lists"
            }
        }
    }
    init {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                createDefaultListIfNeeded()
                loadLists()
            } catch (e: Exception) {
                _error.value = "Failed to initialize lists"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshLists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                loadLists()
            } catch (e: Exception) {
                _error.value = "Failed to refresh lists"
            } finally {
                _isLoading.value = false
            }
        }
    }
}