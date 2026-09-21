package com.example.dctracks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
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
                type = "Running",
                status = "In Progress",
                notes = "Warm-up lap before the main route.",
                distance = "3.4 mi",
                pace = "7:12 /mi",
                location = "Downtown Loop",
                date = "Today"
            ),
            Track(
                id = "2",
                title = "Forest Loop",
                type = "Hiking",
                status = "Planned",
                notes = "Trail review and scenic pass.",
                distance = "5.1 mi",
                pace = "24:36 /mi",
                location = "North Ridge",
                date = "Tomorrow"
            ),
            Track(
                id = "3",
                title = "Night Ride",
                type = "Cycling",
                status = "Completed",
                notes = "Strong pace and steady cadence.",
                distance = "12.7 mi",
                pace = "16.5 mph",
                location = "River Trail",
                date = "Yesterday"
            ),
            Track(
                id = "4",
                title = "Harbor Walk",
                type = "Walking",
                status = "Completed",
                notes = "Recovery walk after training.",
                distance = "2.2 mi",
                pace = "18:12 /mi",
                location = "Harbor Promenade",
                date = "2 days ago"
            )
        )
    }

    var activeScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(Filter.All) }

    val filteredTracks = remember(tracks, query, selectedTab) {
        tracks.filter { track ->
            val matchesTab = when (selectedTab) {
                Filter.All -> true
                Filter.Planned -> track.status == "Planned"
                Filter.InProgress -> track.status == "In Progress"
                Filter.Completed -> track.status == "Completed"
            }
            val matchesQuery = query.isBlank() ||
                track.title.contains(query, ignoreCase = true) ||
                track.type.contains(query, ignoreCase = true) ||
                track.location.contains(query, ignoreCase = true)
            matchesTab && matchesQuery
        }
    }

    when (val screen = activeScreen) {
        Screen.Home -> HomeScreen(
            tracks = filteredTracks,
            query = query,
            selectedTab = selectedTab,
            onQueryChange = { query = it },
            onTabChange = { selectedTab = it },
            onAddTrack = { activeScreen = Screen.Form(null) },
            onEditTrack = { track -> activeScreen = Screen.Form(track) },
            onDeleteTrack = { track -> tracks.remove(track) },
            onOpenTrack = { track -> activeScreen = Screen.Detail(track) }
        )

        is Screen.Form -> TrackFormScreen(
            initialTrack = screen.track,
            onSave = { savedTrack ->
                if (screen.track == null) {
                    tracks.add(savedTrack)
                } else {
                    val index = tracks.indexOfFirst { it.id == savedTrack.id }
                    if (index >= 0) tracks[index] = savedTrack
                }
                activeScreen = Screen.Home
            },
            onCancel = { activeScreen = Screen.Home }
        )

        is Screen.Detail -> TrackDetailScreen(
            track = screen.track,
            onBack = { activeScreen = Screen.Home },
            onEdit = { activeScreen = Screen.Form(screen.track) }
        )
    }
}

@Composable
private fun HomeScreen(
    tracks: List<Track>,
    query: String,
    selectedTab: Filter,
    onQueryChange: (String) -> Unit,
    onTabChange: (Filter) -> Unit,
    onAddTrack: () -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onOpenTrack: (Track) -> Unit
) {
    val total = tracks.size
    val completed = tracks.count { it.status == "Completed" }
    val planned = tracks.count { it.status == "Planned" }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DC TRACKS") }) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryRow(total, completed, planned)

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search tracks") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            TrackFilterTabs(selectedTab, onTabChange)

            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tracks match your search.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tracks, key = { it.id }) { track ->
                        TrackCard(
                            track = track,
                            onOpen = { onOpenTrack(track) },
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
private fun SummaryRow(total: Int, completed: Int, planned: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard("Total", total.toString(), Icons.Default.Star)
        SummaryCard("Done", completed.toString(), Icons.Default.CheckCircle)
        SummaryCard("Planned", planned.toString(), Icons.Default.PlayArrow)
    }
}

@Composable
private fun SummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelMedium)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TrackFilterTabs(checked: Filter, onTabChange: (Filter) -> Unit) {
    TabRow(selectedTabIndex = checked.ordinal) {
        Filter.entries.forEach { tab ->
            Tab(
                selected = checked == tab,
                onClick = { onTabChange(tab) },
                text = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun TrackCard(
    track: Track,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                StatusBadge(track.status)
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(track.location)
            }

            Text(
                text = "${track.distance} • ${track.pace} • ${track.date}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )

            if (track.notes.isNotBlank()) {
                Text(
                    text = track.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onOpen) {
                    Text("Open")
                }
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

@Composable
private fun StatusBadge(status: String) {
    val background = when (status) {
        "Completed" -> Color(0xFFDCFCE7)
        "In Progress" -> Color(0xFFDBEAFE)
        else -> Color(0xFFF3E8FF)
    }

    Box(
        modifier = Modifier
            .background(background, CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF1F2937)
        )
    }
}

@Composable
private fun TrackDetailScreen(
    track: Track,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(track.title) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusBadge(track.status)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Type", track.type)
                    DetailRow("Distance", track.distance)
                    DetailRow("Pace", track.pace)
                    DetailRow("Location", track.location)
                    DetailRow("Date", track.date)
                }
            }

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = track.notes.ifBlank { "No notes were added." })

            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Edit track")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
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
    var type by remember(initialTrack?.id) { mutableStateOf(initialTrack?.type ?: "") }
    var status by remember(initialTrack?.id) { mutableStateOf(initialTrack?.status ?: "Planned") }
    var notes by remember(initialTrack?.id) { mutableStateOf(initialTrack?.notes ?: "") }
    var distance by remember(initialTrack?.id) { mutableStateOf(initialTrack?.distance ?: "0.0 mi") }
    var pace by remember(initialTrack?.id) { mutableStateOf(initialTrack?.pace ?: "0:00 /mi") }
    var location by remember(initialTrack?.id) { mutableStateOf(initialTrack?.location ?: "") }
    var date by remember(initialTrack?.id) { mutableStateOf(initialTrack?.date ?: "Today") }

    val isValid = title.isNotBlank() && type.isNotBlank()

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (initialTrack == null) "New track" else "Edit track") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pace, onValueChange = { pace = it }, label = { Text("Pace") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(
                    onClick = {
                        val saved = Track(
                            id = initialTrack?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            type = type.trim(),
                            status = status.trim().ifBlank { "Planned" },
                            notes = notes.trim(),
                            distance = distance.trim().ifBlank { "0.0 mi" },
                            pace = pace.trim().ifBlank { "0:00 /mi" },
                            location = location.trim().ifBlank { "Unknown" },
                            date = date.trim().ifBlank { "Today" }
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
    val type: String,
    val status: String,
    val notes: String,
    val distance: String,
    val pace: String,
    val location: String,
    val date: String
)

enum class Filter(val label: String) {
    All("All"),
    Planned("Planned"),
    InProgress("In Progress"),
    Completed("Completed")
}

private sealed class Screen {
    data object Home : Screen()
    data class Form(val track: Track?) : Screen()
    data class Detail(val track: Track) : Screen()
}

