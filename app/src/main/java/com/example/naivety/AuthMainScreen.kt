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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AuthMainScreen(
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    signInWithGoogle: () -> Unit,
    navigateToMainScreen: () -> Unit
) {
    var isSignIn by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            navigateToMainScreen()
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
        Text(
            text = if (isSignIn) "Sign In" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF8E42FF),
                fontFamily = FontFamily(Font(R.font.sonder, FontWeight.Normal)),
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!isSignIn) {
            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sign In/Up Button
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isSignIn) {
                        signIn(auth, email, password) { result ->
                            message = result
                            if (result == "Sign in successful") {
                                navigateToMainScreen()
                            }
                        }
                    } else {
                        signUp(auth, email, password) { result ->
                            message = result
                            if (result == "Sign up successful") {
                                navigateToMainScreen()
                            }
                        }
                    }
                } else {
                    message = "Email and Password cannot be empty"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111111),
                contentColor = Color(0xFF8E42FF)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = if (isSignIn) "Sign In" else "Sign Up",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.alinsa))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign In Button
        Button(
            onClick = signInWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign in with Google",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.alinsa))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Sign In/Up
        TextButton(
            onClick = { isSignIn = !isSignIn },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSignIn) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                color = Color(0xFF8E42FF),
                fontFamily = FontFamily(Font(R.font.alinsa))
            )
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.alinsa))
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.Gray,
            errorTextColor = Color.Red,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            cursorColor = Color.White,
            errorCursorColor = Color.Red,
            selectionColors = TextSelectionColors(
                handleColor = Color.White,
                backgroundColor = Color.Gray.copy(alpha = 0.4f)
            ),
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.DarkGray,
            disabledIndicatorColor = Color.Gray,
            errorIndicatorColor = Color.Red,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.DarkGray,
            disabledLeadingIconColor = Color.Gray,
            errorLeadingIconColor = Color.Red,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.DarkGray,
            disabledTrailingIconColor = Color.Gray,
            errorTrailingIconColor = Color.Red,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.DarkGray,
            disabledLabelColor = Color.Gray,
            errorLabelColor = Color.Red,
            focusedPlaceholderColor = Color.White,
            unfocusedPlaceholderColor = Color.DarkGray,
            disabledPlaceholderColor = Color.Gray,
            errorPlaceholderColor = Color.Red,
            focusedSupportingTextColor = Color.White,
            unfocusedSupportingTextColor = Color.DarkGray,
            disabledSupportingTextColor = Color.Gray,
            errorSupportingTextColor = Color.Red,
            focusedPrefixColor = Color.White,
            unfocusedPrefixColor = Color.DarkGray,
            disabledPrefixColor = Color.Gray,
            errorPrefixColor = Color.Red,
            focusedSuffixColor = Color.White,
            unfocusedSuffixColor = Color.DarkGray,
            disabledSuffixColor = Color.Gray,
            errorSuffixColor = Color.Red
        ),
        singleLine = true
    )
}

fun signIn(auth: FirebaseAuth, email: String, password: String, onResult: (String) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("Sign in successful")
            } else {
                onResult("Sign in failed: ${task.exception?.message}")
            }
        }
}

fun signUp(auth: FirebaseAuth, email: String, password: String, onResult: (String) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("Sign up successful")
            } else {
                onResult("Sign up failed: ${task.exception?.message}")
            }
        }
}