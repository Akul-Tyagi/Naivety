// app/src/main/java/com/example/naivety/auth/SocialProviders.kt
package com.example.naivety.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.*
import io.github.jan.supabase.auth.providers.OAuthProvider

data class SocialProvider(
    val name: String,
    val icon: ImageVector,
    val provider: OAuthProvider,
    val backgroundColor: Color,
    val contentColor: Color
)

val socialProviders = listOf(
    SocialProvider(
        name = "Google",
        icon = FontAwesome.Icon.faw_google,
        provider = Google,
        backgroundColor = Color.White,
        contentColor = Color.Black
    ),
    SocialProvider(
        name = "GitHub",
        icon = FontAwesome.Icon.faw_github,
        provider = Github,
        backgroundColor = Color(0xFF24292E),
        contentColor = Color.White
    ),
    SocialProvider(
        name = "Facebook",
        icon = FontAwesome.Icon.faw_facebook_f,
        provider = Facebook,
        backgroundColor = Color.White,
        contentColor = Color.Black
    ),
    SocialProvider(
        name = "LinkedIn",
        icon = FontAwesome.Icon.faw_linkedin_in,
        provider = LinkedIn,
        backgroundColor = Color.White,
        contentColor = Color.Black
    ),
    SocialProvider(
        name = "Discord",
        icon = FontAwesome.Icon.faw_discord,
        provider = Discord,
        backgroundColor = Color.White,
        contentColor = Color.Black
    ),
    // Add other providers similarly
)