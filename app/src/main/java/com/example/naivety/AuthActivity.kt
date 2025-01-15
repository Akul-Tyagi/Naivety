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
import androidx.navigation.compose.rememberNavController
import com.example.naivety.navigation.Destinations
import com.example.naivety.navigation.NavGraph
import com.example.naivety.ui.screens.MainScreen
import com.example.naivety.ui.theme.TransparentSystemBars
import com.example.naivety.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateToMainScreen()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                val navController = rememberNavController()

                // Determine start destination based on auth state
                val startDestination = when {
                    PreferencesManager.isFirstTime(this) -> Destinations.Walkthrough.route
                    auth.currentUser == null -> Destinations.Auth.route
                    else -> Destinations.Main.route
                }

                NavGraph(
                    navController = navController,
                    auth = auth,
                    googleSignInClient = googleSignInClient,
                    startDestination = startDestination
                )
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
                Log.d("AuthActivity", "Google sign-in successful")
                firebaseAuthWithGoogle(account)
            } else {
                Log.d("AuthActivity", "Google sign-in failed: account is null")
            }
        } catch (e: ApiException) {
            Log.e("AuthActivity", "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("AuthActivity", "Firebase authentication successful")
                    PreferencesManager.setFirstTimeDone(this)
                    navigateToMainScreen()
                } else {
                    Log.e("AuthActivity", "Firebase authentication failed", task.exception)
                }
            }
    }

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainScreenActivity::class.java))
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