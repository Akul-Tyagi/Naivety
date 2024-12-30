package com.example.naivety

import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen(
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    signInWithGoogle: () -> Unit,
    navigateToMainScreen: () -> Unit
) {
    AuthScreen(auth, googleSignInClient, signInWithGoogle, navigateToMainScreen)
}