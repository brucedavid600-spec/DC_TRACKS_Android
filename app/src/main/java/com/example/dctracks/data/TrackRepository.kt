package com.example.dctracks.data

import com.example.dctracks.Track

class TrackRepository(private val dao: TrackDao) {
    suspend fun getAllTracks(): List<Track> = dao.getAll().map { it.toTrack() }
    suspend fun insertOrUpdate(track: Track) = dao.save(track.toEntity())
    suspend fun deleteTrack(track: Track) = dao.delete(track.toEntity())
}
