package com.example.naivety

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.naivety.ui.theme.NaivetyTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            NaivetyTheme {
                SetSystemBarsColor()
                var showWalkthrough by remember { mutableStateOf(true) }

                if (showWalkthrough) {
                    WalkthroughScreen(onFinish = { showWalkthrough = false })
                } else {
                    MainScreen(auth, googleSignInClient, ::signInWithGoogle, ::navigateToMainScreen)
                }
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result?.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("MainActivity", "Google sign-in successful")
                firebaseAuthWithGoogle(account)
            } else {
                Log.d("MainActivity", "Google sign-in failed: account is null")
            }
        } catch (e: ApiException) {
            Log.e("MainActivity", "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Firebase authentication with Google successful")
                    navigateToMainScreen()
                } else {
                    Log.e("MainActivity", "Firebase authentication with Google failed", task.exception)
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun navigateToMainScreen() {
        Log.d("MainActivity", "Navigating to main screen")
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun SetSystemBarsColor() {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Black,
        darkIcons = false
    )
}