package com.example.ai

import android.content.Context
import com.example.data.model.Track

/**
 * Compatibility wrapper delegating AI intelligence requests to OpenRouterAiService.
 */
class GeminiAiService(private val context: Context? = null) {

    private var openRouterAiService: OpenRouterAiService? = context?.let { OpenRouterAiService(it) }

    fun setContext(ctx: Context) {
        openRouterAiService = OpenRouterAiService(ctx)
    }

    suspend fun getSmartMoodShuffleOrder(
        targetMood: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList()
    ): List<String> {
        val service = openRouterAiService
        return if (service != null) {
            service.getSmartMoodShuffleOrder(targetMood, tracks, history)
        } else {
            fallbackMoodShuffle(targetMood, tracks)
        }
    }

    suspend fun generateDynamicMoodPlaylist(
        descriptor: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList(),
        analytics: com.example.data.model.PlaybackAnalytics? = null
    ): com.example.data.model.DynamicMoodPlaylist {
        val service = openRouterAiService
        return if (service != null) {
            service.generateDynamicMoodPlaylist(descriptor, tracks, history, analytics)
        } else {
            OpenRouterAiService(context ?: error("No context")).generateDynamicMoodPlaylist(descriptor, tracks, history, analytics)
        }
    }

    suspend fun chatWithDj(
        userMessage: String,
        history: List<Pair<String, String>>,
        currentTrack: Track?
    ): String {
        val service = openRouterAiService
        return if (service != null) {
            service.chatWithDj(userMessage, history, currentTrack)
        } else {
            getRuleBasedDjResponse(userMessage, currentTrack)
        }
    }

    suspend fun parseVoiceCommand(spokenText: String): VoiceCommandResult {
        val service = openRouterAiService
        return if (service != null) {
            service.parseVoiceCommand(spokenText)
        } else {
            fallbackVoiceCommand(spokenText)
        }
    }

    suspend fun getPsychologicalListeningAnalysis(
        topMood: String,
        topGenre: String,
        totalMinutes: Int,
        avgValence: Float,
        avgEnergy: Float
    ): String {
        val service = openRouterAiService
        return if (service != null) {
            service.getPsychologicalListeningAnalysis(topMood, topGenre, totalMinutes, avgValence, avgEnergy)
        } else {
            "Your listening profile in Auralis centers on $topMood frequencies to enhance mental clarity and focus."
        }
    }

    private fun fallbackMoodShuffle(targetMood: String, tracks: List<Track>): List<String> {
        val matchedMood = tracks.filter { it.mood.equals(targetMood, ignoreCase = true) }
        val otherTracks = tracks.filterNot { it.mood.equals(targetMood, ignoreCase = true) }
        return (matchedMood.sortedByDescending { it.playCount } + otherTracks.sortedBy { it.bpm }).map { it.id }
    }

    private fun getRuleBasedDjResponse(userMessage: String, currentTrack: Track?): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> "Welcome to AURALIS! I'm DJ Pulse. Currently spinning ${currentTrack?.title ?: "curated sounds"}. What vibe are you chasing today?"
            lower.contains("workout") || lower.contains("gym") -> "For high-octane sessions, queue 'Solar Flare' or 'Neon Horizons' to maximize kinetic output!"
            lower.contains("chill") || lower.contains("relax") -> "Dialing it down. 'Midnight Reverie' uses gentle lo-fi chords that trigger relaxation."
            lower.contains("focus") || lower.contains("work") -> "Deep flow state activated. 'Quantum Flux' features minimal vocal interference for focus."
            lower.contains("equalizer") || lower.contains("bass") -> "Head over to Equalizer and punch in the 'Bass Boost' or 'Cyber EDM' preset!"
            else -> "AURALIS is pairing ambient harmonics with dynamic rhythm to keep your momentum flowing."
        }
    }

    private fun fallbackVoiceCommand(spokenText: String): VoiceCommandResult {
        val lower = spokenText.lowercase()
        return when {
            lower.contains("pause") || lower.contains("stop") -> VoiceCommandResult("PLAY_PAUSE", "pause", "Pausing audio")
            lower.contains("play") && (lower.contains("resume") || lower.length < 8) -> VoiceCommandResult("PLAY_PAUSE", "play", "Resuming playback")
            lower.contains("next") || lower.contains("skip") -> VoiceCommandResult("NEXT_TRACK", "next", "Skipping to next track")
            lower.contains("workout") -> VoiceCommandResult("PLAY_MOOD", "Workout", "Playing workout tracks")
            lower.contains("chill") -> VoiceCommandResult("PLAY_MOOD", "Chill", "Playing chill tracks")
            lower.contains("focus") -> VoiceCommandResult("PLAY_MOOD", "Focus", "Playing deep focus beats")
            lower.contains("bass") -> VoiceCommandResult("EQUALIZER", "Bass Boost", "Switching to Bass Boost preset")
            else -> VoiceCommandResult("SEARCH", spokenText, "Searching library for '$spokenText'")
        }
    }
}
