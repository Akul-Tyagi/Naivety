import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

// app/src/main/java/com/example/naivety/ui/screens/BookDetailScreen.kt

@Composable
fun BookDetailScreen(
    book: OpenLibraryBook,
    onBackClick: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val viewModel: BookDetailViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(onClick = { /* Save book */ }) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = Color.White
                )
            }
        }

        // Book Cover with blur background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Blurred background
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 20.dp)
            )

            // Book cover
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
            )
        }

        // Book details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Text(
                text = book.author,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Text(
                text = book.publishedYear.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating section
            RatingSection(
                rating = book.rating,
                onRatingChanged = { /* Update rating */ }
            )

            // Comments section
            CommentSection(
                bookId = book.key,
                viewModel = viewModel
            )
        }
    }
}