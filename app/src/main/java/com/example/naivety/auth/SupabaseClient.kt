
package com.example.naivety.auth

import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.FlowType

object SupabaseClient {
    const val SUPABASE_URL = "https://enneortpnmxludkyjoeu.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVubmVvcnRwbm14bHVka3lqb2V1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzcyODM2NDQsImV4cCI6MjA1Mjg1OTY0NH0.3q5s3vOk2XfeUssejgYyZLJ8U7j5MqHJ6mPmkNY2ltA"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = "naivety"
            host = "login"
        }
    }
}