import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// app/src/main/java/com/example/naivety/ui/components/RatingSection.kt
@Composable
fun RatingSection(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            IconButton(onClick = { onRatingChanged(index + 1f) }) {
                Icon(
                    imageVector = if (index + 1 <= rating) {
                        Icons.Default.Star
                    } else {
                        Icons.Default.StarOutline
                    },
                    contentDescription = "Star ${index + 1}",
                    tint = Color(0xFF8E42FF)
                )
            }
        }
    }
}

