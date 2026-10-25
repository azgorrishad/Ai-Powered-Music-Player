package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing audio track metadata.
 * Stores local file descriptors, audio technical specs (bitrate, sample rate, format),
 * psychological mood taxonomy, and offline download caching states.
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["isDownloaded"]),
        Index(value = ["isLocal"]),
        Index(value = ["mood"]),
        Index(value = ["genre"])
    ]
)
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "Single",
    val durationMs: Long,
    val audioUrl: String,
    val artworkUrl: String,
    val genre: String = "Electronic",
    val mood: String = "Focus", // "Chill", "Energetic", "Focus", "Melancholy", "Euphoria", "Night Drive", "Workout"
    val bpm: Int = 120,
    val valence: Float = 0.5f, // 0.0 (sad/moody) to 1.0 (joyous/euphoric)
    val energy: Float = 0.5f,  // 0.0 (calm/chill) to 1.0 (high tempo/workout)
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val isLocal: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadSizeMb: Double = 8.5,
    val downloadDate: Long = 0L,
    val source: String = "cloud", // "local", "youtube", "cloud"
    
    // Offline Storage & Technical Audio Metadata
    val localFilePath: String? = null,
    val localArtworkPath: String? = null,
    val bitrateKbps: Int = 320,
    val sampleRateHz: Int = 44100,
    val fileSizeBytes: Long = (8.5 * 1024 * 1024).toLong(),
    val audioFormat: String = "audio/mpeg",
    val lastPlayedTimestamp: Long = 0L
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    /**
     * Resolves the playable audio URI - prioritizes local offline file if present.
     */
    val effectiveAudioUri: String
        get() = localFilePath ?: audioUrl

    /**
     * Indicates whether this track is playable completely offline without active internet.
     */
    val isAvailableOffline: Boolean
        get() = isDownloaded || isLocal || !localFilePath.isNullOrBlank()

    /**
     * Formats audio specs for audiophile display (e.g. "320 kbps • 44.1 kHz MP3").
     */
    val audioSpecsFormatted: String
        get() = "$bitrateKbps kbps • ${sampleRateHz / 1000.0} kHz ${audioFormat.substringAfterLast("/").uppercase()}"
}
