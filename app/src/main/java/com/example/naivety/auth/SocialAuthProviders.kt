
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.LinkedIn
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.Twitter

// app/src/main/java/com/example/naivety/auth/SocialAuthProviders.kt
object SocialAuthProviders {
    val providers = listOf(
        SocialProvider(
            name = "Google",
            icon = Icons.Default.Google,
            provider = Google,
            backgroundColor = Color.White,
            contentColor = Color.Black
        ),
        SocialProvider(
            name = "GitHub",
            icon = Icons.Default.Code,
            provider = Github,
            backgroundColor = Color(0xFF24292E),
            contentColor = Color.White
        ),
        SocialProvider(
            name = "LinkedIn",
            icon = Icons.Default.LinkedIn,
            provider = LinkedIn,
            backgroundColor = Color(0xFF0077B5),
            contentColor = Color.White
        ),
        SocialProvider(
            name = "Twitter",
            icon = Icons.Default.Twitter,
            provider = Twitter,
            backgroundColor = Color(0xFF1DA1F2),
            contentColor = Color.White
        ),
        SocialProvider(
            name = "Facebook",
            icon = Icons.Default.Facebook,
            provider = Facebook,
            backgroundColor = Color(0xFF1877F2),
            contentColor = Color.White
        )
    )
}

data class SocialProvider(
    val name: String,
    val icon: ImageVector,
    val provider: OAuthProvider,
    val backgroundColor: Color,
    val contentColor: Color
)