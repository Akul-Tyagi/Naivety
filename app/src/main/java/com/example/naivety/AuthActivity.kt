package com.example.naivety

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.naivety.auth.AuthState
import com.example.naivety.navigation.Destinations
import com.example.naivety.navigation.NavGraph
import com.example.naivety.ui.screens.MainScreen
import com.example.naivety.ui.theme.TransparentSystemBars
import com.example.naivety.utils.PreferencesManager
import com.example.naivety.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                val navController = rememberNavController()
                val authState by viewModel.authState.collectAsState()

                // Handle authentication state
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            // Navigate to main screen
                            startActivity(Intent(this@AuthActivity, MainScreenActivity::class.java))
                            finish()
                        }
                        is AuthState.Error -> {
                            Toast.makeText(
                                this@AuthActivity,
                                (authState as AuthState.Error).message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {}
                    }
                }

                NavGraph(
                    navController = navController,
                    startDestination = if (PreferencesManager.isFirstTime(this@AuthActivity)) {
                        Destinations.Walkthrough.route
                    } else {
                        Destinations.Auth.route
                    }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthViewModel.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            viewModel.handleGoogleSignInResult(task)
        }
    }
}