package com.abundance.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.abundance.naivety.data.BookListCrossRef
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.repository.ListsRepository
import com.abundance.naivety.data.List as UserList

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val repository: ListsRepository
) : ViewModel() {
    fun clearSelectedList() {
        _selectedListId.value = null
    }

    private val _lists = MutableStateFlow<kotlin.collections.List<UserList>>(emptyList())
    val lists = _lists.asStateFlow()

    private val _selectedListId = MutableStateFlow<String?>(null)
    val selectedListId = _selectedListId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadLists()
        createDefaultListIfNeeded()
    }

    private fun loadLists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllLists().collect { listItems: kotlin.collections.List<UserList> ->
                    _lists.value = listItems
                    if (_selectedListId.value == null && listItems.isNotEmpty()) {
                        _selectedListId.value = listItems.first().id
                    }
                }
            } finally {
                _isLoading.value = false
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
            val newList = UserList(name = newListName)
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

    fun loadBooksForList(listId: String): Flow<kotlin.collections.List<OpenLibraryBook>> {
        return repository.getBooksInList(listId)
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

    fun reorderLists(newOrder: kotlin.collections.List<UserList>) {
        viewModelScope.launch {
            try {
                repository.updateListOrder(newOrder)
            } catch (e: Exception) {
                _error.value = "Failed to reorder lists"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getListsForBook(bookKey: String): Flow<List<String>> {
        return repository.getListsForBook(bookKey)
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