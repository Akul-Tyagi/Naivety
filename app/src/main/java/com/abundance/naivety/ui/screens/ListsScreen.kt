package com.abundance.naivety.ui.screens

import com.abundance.naivety.ui.components.BookCard
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abundance.naivety.R
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.viewmodels.ListsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.abundance.naivety.ui.components.EditListDialog
import com.abundance.naivety.ui.theme.NaivetyPurple
import com.abundance.naivety.data.List as UserList
import com.abundance.naivety.ui.components.CreateNewListDialog

@Composable
fun ListsScreen(
    viewModel: ListsViewModel = hiltViewModel(),
    onNavigateToRead: (String) -> Unit,
    onBookClick: (OpenLibraryBook) -> Unit
) {
    val lists = viewModel.lists.collectAsState().value
    val selectedListId = viewModel.selectedListId.collectAsState().value
    var showEditDialog by remember { mutableStateOf<UserList?>(null) }
    val alinsaFont = FontFamily(Font(R.font.nektar))
    val alinsaFontt = FontFamily(Font(R.font.montserratblack))
    var showCreateListDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "My Lists",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = alinsaFont,
            modifier = Modifier.padding(bottom = 8.dp, start = 16.dp),
            color = NaivetyPurple
        )

        // Improved Lists Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 70.dp), // Space for floating add button
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(lists) { list ->
                    ElegantListCard(
                        name = list.name,
                        isSelected = list.id == selectedListId,
                        onSelected = { viewModel.selectList(list.id) },
                        onEdit = { showEditDialog = list }
                    )
                }
            }

            // Floating add button
            FloatingActionButton(
                onClick = { showCreateListDialog = true }, // Show dialog instead of directly creating
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new list",
                )
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

        // Books grid for selected list
        selectedListId?.let { listId ->
            val booksInList =
                viewModel.loadBooksForList(listId).collectAsState(initial = emptyList()).value

            if (booksInList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books in this list yet...\n\nAdd some books through Browse section to get started!",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontFamily = alinsaFontt,
                        fontSize = 15.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(25.dp)
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
            onDelete = {  // Add this line
                viewModel.deleteList(list.id)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }
}

@Composable
private fun ElegantListCard(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 90.dp)
            .clickable(onClick = onSelected),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit list name",
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}