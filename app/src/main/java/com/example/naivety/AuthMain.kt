package com.example.naivety

import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthMain(
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    signInWithGoogle: () -> Unit,
    navigateToMainScreen: () -> Unit
) {
    AuthMainScreen(auth, googleSignInClient, signInWithGoogle, navigateToMainScreen)
}