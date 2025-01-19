package com.example.naivety

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naivety.navigation.Destinations
import com.example.naivety.viewmodels.AuthState
import com.example.naivety.viewmodels.AuthViewModel
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.OAuthProvider

@Composable
fun AuthMainScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    var isSignIn by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = if (isSignIn) "Sign In" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF8E42FF),
                fontFamily = FontFamily(Font(R.font.sonder))
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email & Password Fields
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In/Up Button
        Button(
            onClick = {
                if (isSignIn) {
                    viewModel.signInWithEmail(email, password)
                } else {
                    viewModel.signUpWithEmail(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8E42FF)
            )
        ) {
            Text(if (isSignIn) "Sign In" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OAuth Providers
        Text(
            text = "Or continue with",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OAuth Buttons
        OAuthButtonsRow(
            onProviderClick = { provider ->
                viewModel.signInWithProvider(provider)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Sign In/Up
        TextButton(
            onClick = { isSignIn = !isSignIn }
        ) {
            Text(
                text = if (isSignIn) "Need an account? Sign Up" else "Have an account? Sign In",
                color = Color(0xFF8E42FF)
            )
        }

        // Error Message
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SocialAuthButtons(
    onProviderClick: (OAuthProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SocialAuthProviders.providers.forEach { provider ->
            Button(
                onClick = { onProviderClick(provider.provider) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = provider.backgroundColor,
                    contentColor = provider.contentColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = provider.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Continue with ${provider.name}",
                        fontFamily = FontFamily(Font(R.font.alinsa))
                    )
                }
            }
        }
    }
}

@Composable
private fun OAuthButtonsRow(
    onProviderClick: (OAuthProvider) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OAuthProviderButton(
            icon = Icons.Default.Google,
            provider = Google,
            onClick = onProviderClick
        )
        OAuthProviderButton(
            icon = Icons.Default.GitHub,
            provider = Github,
            onClick = onProviderClick
        )
        // Add other provider buttons similarly
    }
}

@Composable
fun AuthRequiredScreen(
    content: @Composable () -> Unit
) {
    val navController = LocalNavController.current
    val sessionManager = remember { SessionManager }
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAuthenticated = sessionManager.isAuthenticated()
        if (!isAuthenticated) {
            navController.navigate(Destinations.Auth.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    if (isAuthenticated) {
        content()
    }
}