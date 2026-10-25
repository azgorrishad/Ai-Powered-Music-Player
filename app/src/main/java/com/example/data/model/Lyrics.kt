package com.example.data.model

/**
 * Single line of time-stamped lyric.
 */
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

/**
 * Complete lyrics container for a track, supporting synchronized LRC data
 * as well as plain text fallback.
 */
data class TrackLyrics(
    val trackId: String,
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val plainLyrics: String? = null,
    val source: String = "Synchronized LRC"
) {
    /**
     * Determines current active line index based on playback position in milliseconds.
     */
    fun getActiveLineIndex(positionMs: Long): Int {
        if (!isSynced || lines.isEmpty()) return -1
        for (i in lines.indices.reversed()) {
            if (positionMs >= lines[i].timestampMs) {
                return i
            }
        }
        return 0
    }
}
