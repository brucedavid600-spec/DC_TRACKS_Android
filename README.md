package com.example.dctracks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.UUID

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
    val tracks = remember {
        mutableStateListOf(
            Track(
                id = "1",
                title = "City Sprint",
                trackType = "Running",
                status = "Planned",
                notes = "Evening run around the lake."
            ),
            Track(
                id = "2",
                title = "Mountain Loop",
                trackType = "Cycling",
                status = "In Progress",
                notes = "Steady climb and recovery ride."
            ),
            Track(
                id = "3",
                title = "Trail Review",
                trackType = "Hiking",
                status = "Completed",
                notes = "Scouted a new route near the ridge."
            )
        )
    }

    var activeScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var editingTrack by remember { mutableStateOf<Track?>(null) }

    when (val screen = activeScreen) {
        AppScreen.Home -> {
            HomeScreen(
                tracks = tracks,
                onAddTrack = { activeScreen = AppScreen.Form(null) },
                onEditTrack = { track ->
                    editingTrack = track
                    activeScreen = AppScreen.Form(track)
                },
                onDeleteTrack = { track -> tracks.remove(track) }
            )
        }

        is AppScreen.Form -> {
            TrackFormScreen(
                initialTrack = screen.track,
                onSave = { updatedTrack ->
                    if (screen.track == null) {
                        tracks.add(updatedTrack)
                    } else {
                        val index = tracks.indexOfFirst { it.id == updatedTrack.id }
                        if (index >= 0) {
                            tracks[index] = updatedTrack
                        }
                    }
                    activeScreen = AppScreen.Home
                },
                onCancel = { activeScreen = AppScreen.Home }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    tracks: List<Track>,
    onAddTrack: () -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DC TRACKS") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrack) {
                Icon(Icons.Default.Add, contentDescription = "Add track")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Track overview",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tracks yet. Add your first one.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tracks, key = { it.id }) { track ->
                        TrackCard(
                            track = track,
                            onEdit = { onEditTrack(track) },
                            onDelete = { onDeleteTrack(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: Track,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = when (track.status) {
                                "Completed" -> Color(0xFFDCFCE7)
                                "In Progress" -> Color(0xFFDBEAFE)
                                else -> Color(0xFFF3E8FF)
                            },
                            shape = CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = track.status,
                        color = Color(0xFF1F2937),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Text(
                text = track.trackType,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (track.notes.isNotBlank()) {
                Text(
                    text = track.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit track")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete track")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackFormScreen(
    initialTrack: Track?,
    onSave: (Track) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember(initialTrack?.id) { mutableStateOf(initialTrack?.title ?: "") }
    var trackType by remember(initialTrack?.id) { mutableStateOf(initialTrack?.trackType ?: "") }
    var status by remember(initialTrack?.id) { mutableStateOf(initialTrack?.status ?: "Planned") }
    var notes by remember(initialTrack?.id) { mutableStateOf(initialTrack?.notes ?: "") }

    val isValid = title.isNotBlank() && trackType.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialTrack == null) "New track" else "Edit track") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = trackType,
                onValueChange = { trackType = it },
                label = { Text("Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val saved = Track(
                            id = initialTrack?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            trackType = trackType.trim(),
                            status = status.trim().ifBlank { "Planned" },
                            notes = notes.trim()
                        )
                        onSave(saved)
                    },
                    enabled = isValid,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun DCTracksTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

data class Track(
    val id: String,
    val title: String,
    val trackType: String,
    val status: String,
    val notes: String
)

private sealed class AppScreen {
    data object Home : AppScreen()
    data class Form(val track: Track?) : AppScreen()
}
