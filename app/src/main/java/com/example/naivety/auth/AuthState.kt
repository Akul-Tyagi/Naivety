// app/src/main/java/com/example/naivety/auth/AuthState.kt
package com.example.naivety.auth

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
}