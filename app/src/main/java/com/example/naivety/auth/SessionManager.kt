import com.example.naivety.auth.SupabaseAuth
import io.github.jan.supabase.auth.auth

// app/src/main/java/com/example/naivety/auth/SessionManager.kt
object SessionManager {
    private val supabase = SupabaseAuth.client

    suspend fun getCurrentSession() = supabase.auth.currentSession

    suspend fun getCurrentUser() = supabase.auth.currentUser

    suspend fun isAuthenticated() = supabase.auth.currentSession != null

    suspend fun signOut() = supabase.auth.signOut()

    fun observeAuthState() = supabase.auth.sessionStatus
}