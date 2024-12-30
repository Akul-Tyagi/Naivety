package com.example.naivety

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.text.selection.TextSelectionColors

@Composable
fun AuthScreen(
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
            // User is already signed in, navigate to the main screen
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
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.DarkGray,
                    disabledTextColor = Color.Gray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    errorCursorColor = Color.Red,
                    textSelectionColors = TextSelectionColors(
                        handleColor = Color.White,
                        backgroundColor = Color.LightGray
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
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.DarkGray,
                disabledTextColor = Color.Gray,
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                errorCursorColor = Color.Red,
                textSelectionColors = TextSelectionColors(
                    handleColor = Color.White,
                    backgroundColor = Color.LightGray
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
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.DarkGray,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.DarkGray,
                disabledTextColor = Color.Gray,
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                errorCursorColor = Color.Red,
                textSelectionColors = TextSelectionColors(
                    handleColor = Color.White,
                    backgroundColor = Color.LightGray
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
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111), contentColor = Color(0xFF8E42FF)),
            shape = MaterialTheme.shapes.large
        ) {
            Text(text = if (isSignIn) "Sign In" else "Sign Up", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = rememberImagePainter("https://www.gstatic.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png"),
            contentDescription = "Google Sign In",
            modifier = Modifier
                .size(48.dp)
                .clickable { signInWithGoogle() }
        )
        TextButton(
            onClick = { isSignIn = !isSignIn },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSignIn) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                color = Color(0xFF8E42FF),
            )
        }
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = Color.Red, fontSize = 14.sp)
        }
    }
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