package com.abundance.naivety.models

sealed class SearchState {
    object Idle : SearchState()
    object Searching : SearchState()
    object NoResults : SearchState()
    data class Error(val message: String = "Something went wrong", val isServerError: Boolean = false) : SearchState()
    data class Success(val query: String) : SearchState()
}