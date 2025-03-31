package com.abundance.naivety

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abundance.naivety.auth.AuthState
import com.abundance.naivety.navigation.Destinations
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.abundance.naivety.ui.theme.NaivetyTheme
import com.abundance.naivety.utils.PreferencesManager
import com.abundance.naivety.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize Google Sign In
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        // Initialize Google Sign In in ViewModel
        viewModel.initGoogleSignIn(this)

        // Check if user is already authenticated
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainScreenActivity::class.java))
            finish()
            return
        }

        setContent {
            val isDarkTheme by (applicationContext as NaivetyApplication)
                .userPreferencesRepository.isDarkTheme.collectAsState(initial = true)

            NaivetyTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val authState by viewModel.authState.collectAsState()

                // Handle authentication state
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Success -> {
                            PreferencesManager.setFirstTimeLoginDone(this@AuthActivity)
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

                NavHost(
                    navController = navController,
                    startDestination = if (PreferencesManager.isFirstTime(this@AuthActivity)) {
                        Destinations.Walkthrough.route
                    } else {
                        Destinations.Auth.route
                    }
                ) {
                    composable(Destinations.Walkthrough.route) {
                        WalkthroughScreen(
                            onFinish = {
                                PreferencesManager.setFirstTimeDone(this@AuthActivity)
                                navController.navigate(Destinations.Auth.route) {
                                    popUpTo(Destinations.Walkthrough.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Destinations.Auth.route) {
                        AuthMainScreen(
                            viewModel = viewModel,
                            activity = this@AuthActivity
                        )
                    }
                }
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