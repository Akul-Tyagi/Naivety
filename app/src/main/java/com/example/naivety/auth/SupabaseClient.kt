// app/src/main/java/com/example/naivety/auth/SupabaseClient.kt
package com.example.naivety.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://enneortpnmxludkyjoeu.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVubmVvcnRwbm14bHVka3lqb2V1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzcyODM2NDQsImV4cCI6MjA1Mjg1OTY0NH0.3q5s3vOk2XfeUssejgYyZLJ8U7j5MqHJ6mPmkNY2ltA"
    ) {
        install(Auth) {
            scheme = "naivety"
            host = "login"
        }
        install(Postgrest)
    }
}