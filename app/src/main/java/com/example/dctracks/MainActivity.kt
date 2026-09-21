package com.example.dctracks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dctracks.data.AppDatabase
import com.example.dctracks.data.TrackRepository
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TracksApp() } }
    }
}

@Composable
private fun TracksApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { TrackRepository(AppDatabase.getInstance(context).trackDao()) }
    val scope = rememberCoroutineScope()
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var editing by remember { mutableStateOf<Track?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    suspend fun refresh() { tracks = repository.getAllTracks() }
    LaunchedEffect(Unit) {
        refresh()
        if (tracks.isEmpty()) {
            repository.insertOrUpdate(Track("sample", "City Sprint", "Running", "Planned", "Starter track", "3.4 mi", "7:12 /mi", "Downtown", "Today"))
            refresh()
        }
    }

    if (showForm) {
        TrackForm(editing, onCancel = { showForm = false; editing = null }) { track ->
            scope.launch { repository.insertOrUpdate(track); refresh(); showForm = false; editing = null }
        }
        return
    }

    val visible = tracks.filter { it.title.contains(search, true) || it.type.contains(search, true) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("DC TRACKS") }) },
        floatingActionButton = { FloatingActionButton(onClick = { editing = null; showForm = true }) { Icon(Icons.Default.Add, "Add") } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Your tracks", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(search, { search = it }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.id }) { track ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(track.title, style = MaterialTheme.typography.titleLarge)
                            Text("${track.type} • ${track.distance} • ${track.status}")
                            Text(track.location)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { editing = track; showForm = true }) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = { scope.launch { repository.deleteTrack(track); refresh() } }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackForm(existing: Track?, onCancel: () -> Unit, onSave: (Track) -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "Planned") }
    var distance by remember { mutableStateOf(existing?.distance ?: "0.0 mi") }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    Scaffold(topBar = { TopAppBar(title = { Text(if (existing == null) "Add track" else "Edit track") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(type, { type = it }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(status, { status = it }, label = { Text("Status") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(distance, { distance = it }, label = { Text("Distance") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(enabled = title.isNotBlank() && type.isNotBlank(), onClick = {
                    onSave(Track(existing?.id ?: UUID.randomUUID().toString(), title.trim(), type.trim(), status.trim(), notes.trim(), distance.trim(), existing?.pace ?: "0:00 /mi", location.trim(), existing?.date ?: "Today"))
                }, modifier = Modifier.padding(start = 8.dp)) { Text("Save") }
            }
        }
    }
}

data class Track(val id: String, val title: String, val type: String, val status: String, val notes: String, val distance: String, val pace: String, val location: String, val date: String)
