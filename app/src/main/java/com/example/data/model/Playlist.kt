package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a local or cloud playlist definition.
 * Supports offline storage, track ordering, AI curation metadata, and offline caching.
 */
@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["moodCategory"]),
        Index(value = ["isOfflineAvailable"])
    ]
)
data class Playlist(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val moodCategory: String, // e.g., "Deep Focus", "Euphoric Chill", "Late Night Drive"
    val coverUrl: String,
    val trackIdsJson: String, // Comma-separated or JSON list of track IDs
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false,

    // Offline Storage & Definition Metadata
    val localCoverPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isOfflineAvailable: Boolean = false,
    val totalTracksCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val isPinned: Boolean = false,
    val offlineSyncTimestamp: Long = 0L
) {
    fun getTrackIdList(): List<String> {
        return if (trackIdsJson.isBlank()) emptyList()
        else trackIdsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val totalDurationFormatted: String
        get() {
            val totalMinutes = totalDurationMs / 60000L
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return if (hours > 0) "${hours} hr ${mins} min" else "${mins} min"
        }

    fun copyWithUpdatedTracks(newTrackIds: List<String>, calculatedDurationMs: Long): Playlist {
        return copy(
            trackIdsJson = newTrackIds.joinToString(","),
            totalTracksCount = newTrackIds.size,
            totalDurationMs = calculatedDurationMs,
            updatedAt = System.currentTimeMillis()
        )
    }
}
