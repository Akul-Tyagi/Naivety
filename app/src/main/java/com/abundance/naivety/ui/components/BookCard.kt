import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.abundance.naivety.models.OpenLibraryBook
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.abundance.naivety.ui.components.ListSelectionDialog

// app/src/main/java/com/abundance/naivety/ui/components/BookCard.kt
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCard(
    book: OpenLibraryBook,
    onClick: () -> Unit,
    isLiked: Boolean = false,
    onLikeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showListsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(book.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f/3f)
            )
        }

        // Modified like button overlay to show dialog
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(32.dp)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), CircleShape)
                .clickable {
                    showListsDialog = true  // Show dialog on click
                    onLikeToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Move dialog outside of Box
    if (showListsDialog) {
        ListSelectionDialog(
            book = book,
            onDismiss = { showListsDialog = false }
        )
    }
}