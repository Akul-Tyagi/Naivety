//package com.example.naivety.auth
//
//import io.github.jan.supabase.auth.auth
//import io.github.jan.supabase.gotrue.SessionStatus
//
//object SessionManager {
//    private val supabase = SupabaseClient.client
//
//    suspend fun getSession() = supabase.auth.getSession()
//
//    suspend fun getUser() = supabase.auth.getUser()
//
//    suspend fun isAuthenticated() = getSession() != null
//
//    suspend fun signOut() = supabase.auth.signOut()
//
//    fun observeAuthState() = supabase.auth.sessionStatus
//}