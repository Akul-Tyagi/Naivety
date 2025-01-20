package com.example.naivety.ui.screens

import BookCard
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.naivety.data.List as UserList

@Composable
fun ListsScreen(
    viewModel: ListsViewModel = hiltViewModel(),
    onNavigateToRead: (String) -> Unit,
    onBookClick: (OpenLibraryBook) -> Unit
) {
    val lists = viewModel.lists.collectAsState().value
    val selectedListId = viewModel.selectedListId.collectAsState().value
    var showEditDialog by remember { mutableStateOf<UserList?>(null) }
    val alinsaFont = FontFamily(Font(R.font.alinsa))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
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
                    onEdit = { showEditDialog = list }
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

        // Books grid for selected list
        selectedListId?.let { listId ->
            val booksInList = viewModel.loadBooksForList(listId).collectAsState(initial = emptyList()).value

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
                        count = booksInList.size,
                        key = { index -> booksInList[index].key }
                    ) { index ->
                        val book = booksInList[index]
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) },
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

    // Edit dialog
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

@Composable
private fun ListChip(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clickable(onClick = onSelected)
            .background(
                if (isSelected) Color(0xFF8E42FF) else Color(0xFF1A1A1A),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit list name",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}