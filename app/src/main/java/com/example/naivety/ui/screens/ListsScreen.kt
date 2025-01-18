package com.example.naivety.ui.screens

import BookCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naivety.R
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.viewmodels.ListsViewModel
import androidx.compose.runtime.getValue
import com.example.naivety.ui.components.EditListDialog
import com.example.naivety.ui.components.ReorderableLists
import com.example.naivety.data.AppDatabase

@Composable
fun ListsScreen(
    viewModel: ListsViewModel = hiltViewModel(),
    onNavigateToRead: (String) -> Unit,
    onBookClick: (OpenLibraryBook) -> Unit
) {
    val lists by viewModel.lists.collectAsState()
    val selectedListId by viewModel.selectedListId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showEditDialog by remember { mutableStateOf<List?>(null) }
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ReorderableLists(
            lists = lists,
            selectedListId = selectedListId,
            onListSelect = { viewModel.selectList(it) },
            onListReorder = { viewModel.reorderLists(it) },
            onEditList = { showEditDialog = it },
            modifier = Modifier.fillMaxWidth()
        )
        // Lists header section
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(lists) { list ->
                ListChip(
                    name = list.name,
                    isSelected = list.id == selectedListId,
                    onSelected = { viewModel.selectList(list.id) },
                    onEdit = { /* Show edit dialog */ }
                )
            }

            // Add new list button
            item {
                IconButton(
                    onClick = { viewModel.createNewList() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFF1A1A1A),
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new list",
                        tint = Color(0xFF8E42FF)
                    )
                }
            }
        }

        showEditDialog?.let { list ->
            EditListDialog(
                initialName = list.name,
                onConfirm = { newName ->
                    viewModel.updateListName(list.id, newName)
                    showEditDialog = null
                },
                onDismiss = { showEditDialog = null }
            )
        }
    }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF8E42FF))
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            else -> {
                // Books grid for selected list
                selectedListId?.let { listId ->
                    val booksInList by viewModel.loadBooksForList(listId)
                        .collectAsState(initial = emptyList())

                    if (booksInList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No books in this list yet",
                                color = Color.Gray,
                                fontFamily = alinsaFont
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
                                items = booksInList,
                                key = { it.key }
                            ) { book ->
                                BookCard(
                                    book = book,
                                    onLongPress = { /* Handle long press */ },
                                    onClick = { /* Handle click */ },
                                    isLiked = true,
                                    onLikeToggle = {
                                        viewModel.toggleBookInList(book.key, listId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListChip(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(40.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) Color(0xFF8E42FF) else Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 14.sp
            )
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit list name",
                    tint = Color.White
                )
            }
        }
    }
}