// app/src/main/java/com/abundance/naivety/auth/AuthState.kt
package com.abundance.naivety.auth

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
}