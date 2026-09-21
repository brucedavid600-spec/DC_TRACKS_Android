package com.example.dctracks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DCTracksTheme {
                DCTracksApp()
            }
        }
    }
}

@Composable
private fun DCTracksApp() {
    var message by remember { mutableStateOf("Your tracks will appear here.") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DC TRACKS") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to DC TRACKS",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
            )
            Button(onClick = { message = "Ready to add your first track." }) {
                Text("Get started")
            }
        }
    }
}

@Composable
private fun DCTracksTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
