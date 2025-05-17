package com.abundance.naivety.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.abundance.naivety.R
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.viewmodels.ListsViewModel
import kotlinx.coroutines.launch
import com.abundance.naivety.ui.components.CreateNewListDialog

@Composable
fun ListSelectionDialog(
    book: OpenLibraryBook,
    onDismiss: () -> Unit,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val lists by viewModel.lists.collectAsState()
    val bookLists by viewModel.getListsForBook(book.key).collectAsState(initial = emptyList())
    val alinsaFont = FontFamily(Font(R.font.nektar))
    var selectedListIds by remember { mutableStateOf(bookLists.toSet()) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookLists) {
        selectedListIds = bookLists.toSet()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Lists",
                        fontFamily = alinsaFont,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Book Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = book.title,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.author,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Lists
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(lists) { list ->
                        ListItem(
                            name = list.name,
                            isSelected = selectedListIds.contains(list.id),
                            onToggle = {
                                scope.launch {
                                    val isInList = selectedListIds.contains(list.id)
                                    if (isInList) {
                                        // Remove book from list (this part is fine)
                                        viewModel.removeBookFromList(book.key, list.id)
                                        selectedListIds = selectedListIds - list.id
                                    } else {
                                        // Add book to list with complete details
                                        viewModel.addBookWithDetailsToList(book, list.id)
                                        selectedListIds = selectedListIds + list.id
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create New List Button
                TextButton(
                    onClick = { showCreateListDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New List",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = alinsaFont
                        )
                    }
                }
            }
        }
    }

    if (showCreateListDialog) {
        CreateNewListDialog(
            onConfirm = { name ->
                viewModel.createNewListWithName(name)
                showCreateListDialog = false
            },
            onDismiss = { showCreateListDialog = false }
        )
    }
}

@Composable
private fun ListItem(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}