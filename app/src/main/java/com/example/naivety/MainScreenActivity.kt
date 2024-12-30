package com.example.naivety

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.naivety.ui.theme.NaivetyTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NaivetyTheme {
                SetSystemBarsColor()
                MainScreenContent()
            }
        }
    }
}

@Composable
fun MainScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main content of the screen
        Text(
            text = "Welcome to the Main Screen!",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF8E42FF)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NaivetyTheme {
        MainScreenContent()
    }
}
