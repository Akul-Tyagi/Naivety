// app/src/main/java/com/abundance/naivety/ui/screens/SearchResultsScreen.kt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.abundance.naivety.R
import com.abundance.naivety.models.OpenLibraryBook
import android.app.Activity
import com.abundance.naivety.ads.AdManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultsScreen(
    books: List<OpenLibraryBook>,
    onBookClick: (OpenLibraryBook) -> Unit,
    onBackPress: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp),
                    fontStyle = FontStyle(R.font.montserratblack)
                )
            }

            // Results
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(
                        items = books,
                        key = { it.key }
                    ) { book ->
                        BookCard(
                            book = book,
                            onClick = {
                                // Show ad before navigating to book details
                                val activity = (context as? Activity)
                                if (activity != null) {
                                    AdManager.showRewardedAd(
                                        activity = activity,
                                        onAdClosed = {
                                            onBookClick(book)
                                        },
                                        onAdFailedToShow = {
                                            onBookClick(book)
                                        }
                                    )
                                } else {
                                    onBookClick(book)
                                }
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}