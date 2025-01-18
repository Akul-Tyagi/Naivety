import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.naivety.R
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.navigation.Destinations
import com.example.naivety.viewmodels.BookComment
import com.example.naivety.viewmodels.BookDetailViewModel

@Composable
fun BookDetailScreen(
    book: OpenLibraryBook,
    onBackPressed: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val bookDetails by viewModel.bookDetails.collectAsState()
    val userRating by viewModel.userRating.collectAsState()
    val bookComments by viewModel.bookComments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var isLiked by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf("Browse") }

    BackHandler {
        // Set the flag before navigating back
        navController.previousBackStackEntry?.savedStateHandle?.set("fromBookDetail", true)
        onBackPressed()
    }

    LaunchedEffect(book.key) {
        viewModel.loadBookDetails(book.key)
    }

    var isContentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isContentVisible = true
    }

    val customFont = FontFamily(
        Font(R.font.fsb)
    )
    val customFontt = FontFamily(
        Font(R.font.sonder)
    )
    val customFonttt = FontFamily(
        Font(R.font.fsbcd)
    )
    val customFontttt = FontFamily(
        Font(R.font.eubergine)
    )
    val customFonttttt = FontFamily(
        Font(R.font.montserratblackitalic)
    )
    val customFontttttt = FontFamily(
        Font(R.font.montserratblack)
    )
    val customFonttttttt = FontFamily(
        Font(R.font.montserratextrabold)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Blurred Background
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = fadeIn(animationSpec = tween(1000))
            ) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(268.dp)
                        .blur(radius = 16.dp)
                        .alpha(0.34f)
                )
            }
        }

        // Main Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section with Book Cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = book.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(width = 160.dp, height = 300.dp)
                                .shadow(16.dp)
                        )
                    }

                // Book Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = customFont),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .animateContentSize(),
                    )

                    Text(
                        text = "by: ${book.author}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = customFontt),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(bottom = 4.dp),
                    )

                    Row(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Published: ${book.publishedYear}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFonttttttt),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(26.dp))
                        Text(
                            text = "${bookDetails?.pageCount ?: "Pages unavailable"}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFonttttttt),
                            color = Color.Gray
                        )
                    }
                }

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (!bookDetails?.subjects.isNullOrEmpty()) {
                            SectionTitle(text = "Genres", customFont = customFont)
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(bookDetails?.subjects?.take(5) ?: emptyList()) { genre ->
                                    Card(
                                        modifier = Modifier.padding(4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1A1A1A)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = genre,
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFontttttt),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        // Description Section
                        SectionTitle(text = "Synopsis", customFont = customFont)
                        Text(
                            text = bookDetails?.getDescription() ?: "No description available",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFontttt),
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                            lineHeight = 20.sp,
                            fontSize = 21.sp
                        )

                        // Rating Section
                        SectionTitle(text = "Rating", customFont = customFont)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Average Rating: ${String.format("%.1f", bookDetails?.ratings_average ?: 0f)} Out Of ${bookDetails?.ratings_count ?: 0} ratings",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = customFonttttt),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RatingBar(
                                rating = userRating,
                                onRatingChanged = viewModel::updateRating
                            )
                        }

                        // Comments Section
                        SectionTitle(text = "Comments", customFont = customFont)
                        CommentsList(
                            comments = bookComments,
                            onDeleteComment = viewModel::deleteComment,
                            customFont = customFont
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Back Button (Left side)
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Like Button (Right side)
                IconButton(
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) Color.Red else Color.White
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = "Error: $error",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Loading Indicator overlay
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                color = Color(0xFF8E42FF)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, customFont: FontFamily) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = customFont),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
private fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(5) { index ->
            IconButton(
                onClick = { onRatingChanged(index + 1f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Rate ${index + 1}",
                    tint = Color(0xFF8E42FF)
                )
            }
        }
    }
}

@Composable
private fun CommentsList(
    comments: List<BookComment>,
    onDeleteComment: (String) -> Unit,
    customFont: FontFamily
) {
    if (comments.isEmpty()) {
        Text(
            text = "No comments yet. Be the first to share your thoughts!",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 300.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onDelete = { onDeleteComment(comment.id) },
                    customFont = customFont
                )
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: BookComment,
    onDelete: () -> Unit,
    customFont: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User ${comment.userId}",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = customFont),
                    color = Color.White
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete comment",
                        tint = Color.Gray
                    )
                }
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}