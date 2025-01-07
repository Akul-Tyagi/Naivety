package com.example.naivety

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.ui.theme.TransparentSystemBars

class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NaivetyTheme {
                TransparentSystemBars()
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))
    val fsFont = FontFamily(Font(R.font.fsb))
    var selectedSection by remember { mutableStateOf("Library") }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Branding and Sections Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),

            ) {
                Box(
                    modifier = Modifier
                    .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                // App Branding
                Text(
                    text = "Naivety",
                    modifier = Modifier
                        .padding(7.dp)
                        .padding(top = 28.dp),
                    fontFamily = sonderFont,
                    fontSize = 24.sp,
                    color = Color(0xFF8E42FF),
                    textAlign = TextAlign.Center
                )
                }

                // Sections Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Library and Add button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { selectedSection = "Library" }
                        ) {
                            Text(
                                text = "Library",
                                fontFamily = alinsaFont,
                                color = if (selectedSection == "Library")
                                    Color(0xFF8E42FF) else Color.White
                            )
                        }
                        IconButton(onClick = { /* Add new section */ }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Section",
                                tint = Color.White
                            )
                        }
                    }

                    // Right side - Sort and Search
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Open search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Navigation Bar
            NavigationBar(
                modifier = Modifier.background(Color.Black),
                containerColor = Color.Black
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Coffee, "Lists") },
                    label = { Text("Lists", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = true,
                    onClick = { /* Navigate to Lists */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MenuBook, "Read") },
                    label = { Text("Read", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = false,
                    onClick = { /* Navigate to Read */ }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = false,
                    onClick = { /* Add new content */ },
                    label = null
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, "Browse") },
                    label = { Text("Browse", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = false,
                    onClick = { /* Navigate to Browse */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreVert, "More") },
                    label = { Text("More", fontFamily = alinsaFont, color = Color(0xFF8E42FF)) },
                    selected = false,
                    onClick = { /* Navigate to More */ }
                )
            }
        }
    ) { paddingValues ->
        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Search for books and add them to your list to start reading",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = fsFont,
               lineHeight = 20.sp,
                modifier = Modifier.padding(32.dp)
            )
        }

        // Sort Menu
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false },
            modifier = Modifier.background(Color(0xFF1A1A1A))
        ) {
            DropdownMenuItem(
                text = { Text("Recently Added", color = Color.White) },
                onClick = { showSortMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Title: A to Z", color = Color.White) },
                onClick = { showSortMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Author", color = Color.White) },
                onClick = { showSortMenu = false }
            )
        }
    }
}