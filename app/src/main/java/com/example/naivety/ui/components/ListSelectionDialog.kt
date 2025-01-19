package com.example.naivety.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naivety.R
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.viewmodels.ListsViewModel
import com.example.naivety.data.List as UserList

@Composable
fun ListSelectionDialog(
    book: OpenLibraryBook,
    onDismiss: () -> Unit,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val lists = viewModel.lists.collectAsState().value
    val selectedListId = viewModel.selectedListId.collectAsState().value
    val alinsaFont = FontFamily(Font(R.font.alinsa))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to List",
                        color = Color.White,
                        fontFamily = alinsaFont,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Lists
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lists) { userList ->
                        ListSelectionItem(
                            listName = userList.name,
                            isSelected = userList.id == selectedListId,
                            onToggle = {
                                if (userList.id == selectedListId) {
                                    viewModel.removeBookFromList(book.key, userList.id)
                                } else {
                                    viewModel.addBookToList(book.key, userList.id)
                                }
                            }
                        )
                    }
                }

                // Add New List Button
                TextButton(
                    onClick = { viewModel.createNewList() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF8E42FF)
                    )
                ) {
                    Text(
                        text = "Create New List",
                        fontFamily = alinsaFont
                    )
                }
            }
        }
    }
}

@Composable
private fun ListSelectionItem(
    listName: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF8E42FF) else Color(0xFF222222))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = listName,
            color = Color.White
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White
            )
        }
    }
}