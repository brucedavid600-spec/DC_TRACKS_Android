package com.example.dctracks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dctracks.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val status: String,
    val notes: String,
    val distance: String,
    val pace: String,
    val location: String,
    val date: String
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        type = type,
        status = status,
        notes = notes,
        distance = distance,
        pace = pace,
        location = location,
        date = date
    )
}

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    title = title,
    type = type,
    status = status,
    notes = notes,
    distance = distance,
    pace = pace,
    location = location,
    date = date
)
