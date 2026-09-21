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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Surface
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
            Track("1", "City Sprint", "Running", "In Progress", "Warm-up lap before the main route.", "3.4 mi"),
            Track("2", "Forest Loop", "Hiking", "Planned", "Trail review and scenic pass.", "5.1 mi"),
            Track("3", "Night Ride", "Cycling", "Completed", "Strong pace and steady cadence.", "12.7 mi"),
            Track("4", "Harbor Walk", "Walking", "Completed", "Recovery walk after training.", "2.2 mi"),
            Track("5", "Hill Repeats", "Running", "Planned", "Speed blocks and recovery intervals.", "4.0 mi")
        )
    }

    var activeScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(TrackFilter.All) }

    val visibleTracks = remember(tracks, query, selectedTab) {
        tracks.filter { track ->
            val matchesFilter = when (selectedTab) {
                TrackFilter.All -> true
                TrackFilter.Planned -> track.status == "Planned"
                TrackFilter.InProgress -> track.status == "In Progress"
                TrackFilter.Completed -> track.status == "Completed"
            }
            val matchesQuery = query.isBlank() || track.title.contains(query, ignoreCase = true) ||
                track.trackType.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    when (val screen = activeScreen) {
        AppScreen.Home -> HomeScreen(
            tracks = visibleTracks,
            query = query,
            selectedTab = selectedTab,
            onQueryChange = { query = it },
            onTabChange = { selectedTab = it },
            onAddTrack = { activeScreen = AppScreen.Form(null) },
            onEditTrack = { track -> activeScreen = AppScreen.Form(track) },
            onDeleteTrack = { track -> tracks.remove(track) }
        )

        is AppScreen.Form -> TrackFormScreen(
            initialTrack = screen.track,
            onSave = { updatedTrack ->
                if (screen.track == null) {
                    tracks.add(updatedTrack)
                } else {
                    val index = tracks.indexOfFirst { it.id == updatedTrack.id }
                    if (index >= 0) tracks[index] = updatedTrack
                }
                activeScreen = AppScreen.Home
            },
            onCancel = { activeScreen = AppScreen.Home }
        )
    }
}

@Composable
private fun HomeScreen(
    tracks: List<Track>,
    query: String,
    selectedTab: TrackFilter,
    onQueryChange: (String) -> Unit,
    onTabChange: (TrackFilter) -> Unit,
    onAddTrack: () -> Unit,
    onEditTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit
) {
    val totalTracks = tracks.size
    val completedCount = tracks.count { it.status == "Completed" }
    val plannedCount = tracks.count { it.status == "Planned" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DC TRACKS") }
            )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryRow(
                totalTracks = totalTracks,
                completedCount = completedCount,
                plannedCount = plannedCount
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search tracks") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            TrackFilterTabs(
                selectedTab = selectedTab,
                onTabChange = onTabChange
            )

            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tracks match your filter.")
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
private fun SummaryRow(
    totalTracks: Int,
    completedCount: Int,
    plannedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(title = "Total", value = totalTracks.toString(), icon = Icons.Default.Star)
        SummaryCard(title = "Done", value = completedCount.toString(), icon = Icons.Default.CheckCircle)
        SummaryCard(title = "Planned", value = plannedCount.toString(), icon = Icons.Default.PlayArrow)
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
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
                Spacer(modifier = Modifier.width(6.dp))
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
private fun TrackFilterTabs(
    selectedTab: TrackFilter,
    onTabChange: (TrackFilter) -> Unit
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        TrackFilter.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabChange(tab) },
                text = { Text(tab.label) }
            )
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
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = track.trackType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                StatusBadge(status = track.status)
            }

            Text(
                text = track.distance,
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
private fun DCTracksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
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
    var distance by remember(initialTrack?.id) { mutableStateOf(initialTrack?.distance ?: "0.0 mi") }

    val isValid = title.isNotBlank() && trackType.isNotBlank()

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
                value = distance,
                onValueChange = { distance = it },
                label = { Text("Distance") },
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
                            notes = notes.trim(),
                            distance = distance.trim().ifBlank { "0.0 mi" }
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

data class Track(
    val id: String,
    val title: String,
    val trackType: String,
    val status: String,
    val notes: String,
    val distance: String
)

private enum class TrackFilter(val label: String) {
    All("All"),
    Planned("Planned"),
    InProgress("In Progress"),
    Completed("Completed")
}

private sealed class AppScreen {
    data object Home : AppScreen()
    data class Form(val track: Track?) : AppScreen()
}
