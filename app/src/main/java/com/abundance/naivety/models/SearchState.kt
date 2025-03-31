package com.abundance.naivety.models

sealed class SearchState {
    object Idle : SearchState()
    object Searching : SearchState()
    object NoResults : SearchState()
    object Error : SearchState()
    data class Success(val query: String) : SearchState()
}