package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a playback history record.
 * Tracks local playback events, listened durations, mood context, and whether
 * playback occurred during offline mode for subsequent cloud sync.
 */
@Entity(
    tableName = "playback_history",
    indices = [
        Index(value = ["trackId"]),
        Index(value = ["timestamp"]),
        Index(value = ["wasOfflinePlayback"])
    ]
)
data class PlaybackHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val trackTitle: String,
    val artist: String,
    val album: String = "Single",
    val artworkUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val listenedDurationMs: Long = 0,
    val totalDurationMs: Long = 0,
    val moodContext: String = "Neutral",
    val completed: Boolean = true,
    
    // Offline Storage & Sync Telemetry
    val wasOfflinePlayback: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val offlineDeviceId: String? = null
)
