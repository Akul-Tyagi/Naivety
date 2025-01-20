// app/src/main/java/com/example/naivety/auth/SupabaseAuth.kt

package com.example.naivety.auth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Discord
import io.github.jan.supabase.auth.providers.Twitter
import io.github.jan.supabase.auth.providers.LinkedIn
import io.github.jan.supabase.auth.providers.Facebook

object SupabaseAuth {
    private const val SUPABASE_URL = "https://enneortpnmxludkyjoeu.supabase.co"
    private const val SUPABASE_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVubmVvcnRwbm14bHVka3lqb2V1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzcyODM2NDQsImV4cCI6MjA1Mjg1OTY0NH0.3q5s3vOk2XfeUssejgYyZLJ8U7j5MqHJ6mPmkNY2ltA"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            // Configure deep links for OAuth
            scheme = "naivety"
            host = "login"
        }
    }
}