import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.naivety.R
import com.example.naivety.ui.theme.NaivetyPurple

// app/src/main/java/com/example/naivety/ui/components/pdf/SettingItem.kt

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    fontFamily: FontFamily = FontFamily(Font(R.font.fsb)),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NaivetyPurple
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = fontFamily,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            content()
        }
    }
}