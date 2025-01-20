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
import com.example.naivety.auth.SupabaseAuth
import com.example.naivety.auth.SupabaseClient
import com.example.naivety.navigation.Destinations
import com.example.naivety.navigation.NavGraph
import com.example.naivety.ui.screens.MainScreen
import com.example.naivety.ui.theme.TransparentSystemBars
import com.example.naivety.utils.PreferencesManager
import com.example.naivety.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.auth
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

                val startDestination = when {
                    PreferencesManager.isFirstTime(this) -> Destinations.Walkthrough
                    else -> Destinations.Auth
                }.route

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                lifecycleScope.launch {
                    try {
                        startActivity(Intent(this@AuthActivity, MainScreenActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@AuthActivity,
                            "Authentication failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}