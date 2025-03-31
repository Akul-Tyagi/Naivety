// app/src/main/java/com/abundance/naivety/viewmodels/AuthViewModel.kt

package com.abundance.naivety.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.R
import com.abundance.naivety.auth.AuthState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val auth = Firebase.auth
    private var googleSignInClient: GoogleSignInClient? = null

    init {
        // Check if user is already signed in
        auth.currentUser?.let {
            _authState.value = AuthState.Success
        }
    }

    fun initGoogleSignIn(context: Context) {
        googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    fun signInWithGoogle(activity: Activity) {
        googleSignInClient?.let { client ->
            _authState.value = AuthState.Loading
            val signInIntent = client.signInIntent
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        } ?: run {
            _authState.value = AuthState.Error("Google Sign In not initialized")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                googleSignInClient?.signOut()?.await()
                _authState.value = AuthState.SignedOut
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign out failed")
            }
        }
    }


    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("no user record") == true ->
                        "No account found with this email"
                    e.message?.contains("password is invalid") == true ->
                        "Incorrect password"
                    else -> "Sign in failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("email address is already in use") == true ->
                        "Email is already registered"
                    e.message?.contains("password is invalid") == true ->
                        "Password must be at least 6 characters"
                    else -> "Sign up failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).await()
                _authState.value = AuthState.Success
            } catch (e: ApiException) {
                // Handle specific Google Sign-In errors
                val errorMessage = when(e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in was cancelled"
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error, please try again"
                    else -> "Google sign in failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Authentication failed: ${e.message}")
            }
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}