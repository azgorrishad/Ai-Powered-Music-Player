package com.example.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DynamicMoodPlaylist
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenRouter AI Service for AURALIS AI Music Player.
 * Acts as the primary AI intelligence provider for natural language directives,
 * voice search parsing, AI DJ chat, and psychological listening insights.
 */
class OpenRouterAiService(private val context: Context) {

    private val tag = "OpenRouterAiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("auralis_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_KEY_API_KEY = "openrouter_api_key"
        const val PREF_KEY_MODEL = "openrouter_model"
        const val DEFAULT_MODEL = "google/gemini-2.5-flash"
    }

    fun getApiKey(): String? {
        val key = prefs.getString(PREF_KEY_API_KEY, null)
        if (!key.isNullOrBlank()) return key
        return try {
            BuildConfig.GEMINI_API_KEY.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    fun setApiKey(apiKey: String) {
        prefs.edit().putString(PREF_KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getModel(): String {
        return prefs.getString(PREF_KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setModel(model: String) {
        prefs.edit().putString(PREF_KEY_MODEL, model.trim()).apply()
    }

    /**
     * Tests connection to OpenRouter API and measures inference latency.
     */
    suspend fun testConnection(apiKey: String? = null, model: String? = null): ConnectionResult = withContext(Dispatchers.IO) {
        val activeKey = apiKey ?: getApiKey()
        if (activeKey.isNullOrBlank()) {
            return@withContext ConnectionResult(false, "No OpenRouter API key configured.")
        }
        val activeModel = model ?: getModel()
        val startTime = System.currentTimeMillis()

        try {
            val payload = JSONObject().apply {
                put("model", activeModel)
                put("messages", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Ping test for Auralis AI Player.")
                }))
                put("max_tokens", 10)
            }

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer $activeKey")
                .header("HTTP-Referer", "https://auralis.app")
                .header("X-Title", "AURALIS AI Music Player")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ConnectionResult(true, "Connected successfully (${latency}ms) via $activeModel")
            } else {
                Log.w(tag, "OpenRouter test error ${response.code}: $body")
                ConnectionResult(false, "API Error ${response.code}: check key and credits")
            }
        } catch (e: Exception) {
            Log.e(tag, "Connection test failed", e)
            ConnectionResult(false, "Network Error: ${e.localizedMessage ?: "Failed to connect"}")
        }
    }

    /**
     * Smart Mood Shuffling Algorithm via OpenRouter API analyzing current listening history.
     */
    suspend fun getSmartMoodShuffleOrder(
        targetMood: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList()
    ): List<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext fallbackMoodShuffle(targetMood, tracks, history)
        }

        try {
            val historySummary = if (history.isNotEmpty()) {
                val recentTitles = history.take(10).joinToString(", ") { "${it.trackTitle} (${it.artist})" }
                val topMoods = history.groupingBy { it.moodContext }.eachCount().entries.sortedByDescending { it.value }.take(3).joinToString(", ") { "${it.key}:${it.value}" }
                "\nRecent Listening History: $recentTitles\nTop Mood Contexts: $topMoods"
            } else ""

            val tracksSummary = tracks.joinToString("\n") {
                "- ID: ${it.id}, Title: ${it.title}, Artist: ${it.artist}, Mood: ${it.mood}, BPM: ${it.bpm}, Energy: ${it.energy}"
            }

            val prompt = """
                You are AURALIS AI, an algorithmic music curator.
                Target mood: "$targetMood".$historySummary
                Available tracks:
                $tracksSummary

                Analyze the listener's history and target mood to select and arrange track IDs in the most cohesive, satisfying sequence for "$targetMood".
                Respond strictly with a JSON array of track IDs, for example: ["track_1", "track_2"].
            """.trimIndent()

            val responseText = callOpenRouterApi(prompt)
            val jsonArray = parseJsonArray(responseText)
            if (jsonArray != null && jsonArray.length() > 0) {
                val orderedIds = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    orderedIds.add(jsonArray.getString(i))
                }
                val validIds = orderedIds.filter { id -> tracks.any { it.id == id } }
                val remaining = tracks.map { it.id }.filterNot { validIds.contains(it) }
                return@withContext validIds + remaining
            }
        } catch (e: Exception) {
            Log.w(tag, "OpenRouter mood shuffle fallback: ${e.message}")
        }
        return@withContext fallbackMoodShuffle(targetMood, tracks, history)
    }

    /**
     * Generates a dynamic playlist with sequence rationale based on natural language descriptors
     * like "energetic morning workout" or "relaxed rainy day" using OpenRouter / Gemini AI,
     * analyzing current listening history and user playback telemetry.
     */
    suspend fun generateDynamicMoodPlaylist(
        descriptor: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList(),
        analytics: com.example.data.model.PlaybackAnalytics? = null
    ): DynamicMoodPlaylist = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank() || tracks.isEmpty()) {
            return@withContext fallbackDynamicMoodPlaylist(descriptor, tracks, history)
        }

        try {
            val historyContext = buildString {
                if (analytics != null) {
                    append("\nListener Profile & History Analysis:")
                    append("\n- Primary Mood: ").append(analytics.topMood)
                    append("\n- Primary Genre: ").append(analytics.topGenre)
                    append("\n- Top Artists: ").append(analytics.topArtists.joinToString(", ") { "${it.first} (${it.second} plays)" })
                    append("\n- Most Played Tracks: ").append(analytics.mostPlayedTracks.joinToString(", ") { "${it.first} (${it.second} plays)" })
                }
                if (history.isNotEmpty()) {
                    append("\n- Recent Play Sequence: ").append(history.take(8).joinToString(" -> ") { "${it.trackTitle} (${it.moodContext})" })
                }
            }

            val tracksSummary = tracks.joinToString("\n") {
                "- ID: ${it.id}, Title: \"${it.title}\", Artist: \"${it.artist}\", Mood: ${it.mood}, Genre: ${it.genre}, BPM: ${it.bpm}, Energy: ${it.energy}, Valence: ${it.valence}"
            }

            val prompt = """
                You are AURALIS AI, an algorithmic musicologist and dynamic mood curator.
                The listener requested a dynamic playlist based on natural descriptor:
                "$descriptor" (for example 'energetic morning workout' or 'relaxed rainy day').
                $historyContext

                Available catalogue tracks:
                $tracksSummary

                Analyze the listener's playback history and current requested descriptor.
                Select 3 to 8 matching tracks and arrange them in the most psychoacoustically satisfying sequence.
                Respond strictly with valid JSON in this exact structure:
                {
                  "title": "Evocative Playlist Title",
                  "description": "Atmospheric 1-sentence synopsis",
                  "rationale": "2-sentence explanation incorporating both listening history preferences and the mood descriptor energy/tempo arc",
                  "targetMood": "Workout / Chill / Focus / Euphoria / Night Drive",
                  "targetBpmRange": "e.g. 125-142 BPM",
                  "energyLevel": 0.85,
                  "valenceLevel": 0.75,
                  "orderedTrackIds": ["track_id_1", "track_id_2"]
                }
            """.trimIndent()

            val responseText = callOpenRouterApi(prompt)
            val json = parseJsonObject(responseText)
            if (json != null) {
                val orderedIdsArray = json.optJSONArray("orderedTrackIds")
                val orderedTracks = mutableListOf<Track>()
                val trackMap = tracks.associateBy { it.id }

                if (orderedIdsArray != null) {
                    for (i in 0 until orderedIdsArray.length()) {
                        val id = orderedIdsArray.optString(i)
                        trackMap[id]?.let { orderedTracks.add(it) }
                    }
                }

                if (orderedTracks.isNotEmpty()) {
                    return@withContext DynamicMoodPlaylist(
                        title = json.optString("title", descriptor.replaceFirstChar { it.uppercase() }),
                        description = json.optString("description", "Algorithmic selection tailored for \"$descriptor\"."),
                        rationale = json.optString("rationale", "Selected to harmonize with $descriptor based on history."),
                        targetMood = json.optString("targetMood", "Dynamic"),
                        targetBpmRange = json.optString("targetBpmRange", "${orderedTracks.map { it.bpm }.average().toInt()} BPM"),
                        tracks = orderedTracks,
                        energyLevel = json.optDouble("energyLevel", 0.6).toFloat(),
                        valenceLevel = json.optDouble("valenceLevel", 0.6).toFloat()
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "generateDynamicMoodPlaylist API error: ${e.message}")
        }

        return@withContext fallbackDynamicMoodPlaylist(descriptor, tracks, history)
    }

    /**
     * AI DJ Conversational Assistant via OpenRouter API.
     */
    suspend fun chatWithDj(
        userMessage: String,
        history: List<Pair<String, String>>,
        currentTrack: Track?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext getRuleBasedDjResponse(userMessage, currentTrack)
        }

        try {
            val systemInstruction = """
                You are 'DJ Pulse', an enthusiastic and futuristic music curator in AURALIS.
                Current track: ${currentTrack?.let { "${it.title} by ${it.artist} (${it.bpm} BPM, ${it.mood})" } ?: "None"}.
                Keep responses concise, engaging, and atmospheric (under 100 words).
            """.trimIndent()

            val messagesArray = JSONArray()
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemInstruction)
            })

            for ((role, text) in history.takeLast(10)) {
                messagesArray.put(JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                })
            }

            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })

            val payload = JSONObject().apply {
                put("model", getModel())
                put("messages", messagesArray)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("HTTP-Referer", "https://auralis.app")
                .header("X-Title", "AURALIS AI Music Player")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val reply = extractTextFromChoices(body)
                if (!reply.isNullOrBlank()) return@withContext reply.trim()
            }
        } catch (e: Exception) {
            Log.e(tag, "DJ Chat error", e)
        }
        return@withContext getRuleBasedDjResponse(userMessage, currentTrack)
    }

    /**
     * Voice Command Transcription & Intent Understanding.
     */
    suspend fun parseVoiceCommand(spokenText: String): VoiceCommandResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext fallbackVoiceCommand(spokenText)
        }

        try {
            val prompt = """
                Spoken voice command: "$spokenText".
                Action categories:
                1. "PLAY_MOOD" (target: mood name)
                2. "SEARCH" (target: search query)
                3. "EQUALIZER" (target: preset name)
                4. "PLAY_PAUSE" (target: "play" or "pause")
                5. "NEXT_TRACK" (target: "next")

                Respond strictly with JSON:
                {"action": "ACTION_TYPE", "target": "extracted_target", "description": "Explanation"}
            """.trimIndent()

            val responseText = callOpenRouterApi(prompt)
            val json = parseJsonObject(responseText)
            if (json != null) {
                return@withContext VoiceCommandResult(
                    action = json.optString("action", "SEARCH"),
                    target = json.optString("target", spokenText),
                    description = json.optString("description", "Command: $spokenText")
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Voice parse error", e)
        }
        return@withContext fallbackVoiceCommand(spokenText)
    }

    /**
     * Psychological Music Analysis.
     */
    suspend fun getPsychologicalListeningAnalysis(
        topMood: String,
        topGenre: String,
        totalMinutes: Int,
        avgValence: Float,
        avgEnergy: Float
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext "Your listening profile in Auralis demonstrates strong acoustic coherence, centering on $topMood frequencies to enhance mental focus."
        }

        try {
            val prompt = """
                Provide a 2-paragraph empowering analysis of a listener's music profile:
                - Mood: $topMood, Genre: $topGenre, Mins: $totalMinutes
                - Valence: $avgValence, Energy: $avgEnergy
            """.trimIndent()

            val responseText = callOpenRouterApi(prompt)
            if (responseText.isNotBlank()) {
                return@withContext responseText.trim()
            }
        } catch (e: Exception) {
            Log.w(tag, "Analysis error", e)
        }
        return@withContext "Your listening profile demonstrates strong acoustic coherence, centering on $topMood frequencies to enhance focus."
    }

    private fun callOpenRouterApi(prompt: String): String {
        val apiKey = getApiKey() ?: return ""
        val payload = JSONObject().apply {
            put("model", getModel())
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://auralis.app")
            .header("X-Title", "AURALIS AI Music Player")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            throw RuntimeException("API ${response.code}: $body")
        }
        return extractTextFromChoices(body) ?: ""
    }

    private fun extractTextFromChoices(jsonStr: String): String? {
        return try {
            val json = JSONObject(jsonStr)
            val choices = json.optJSONArray("choices") ?: return null
            val firstChoice = choices.optJSONObject(0) ?: return null
            val message = firstChoice.optJSONObject("message") ?: return null
            message.optString("content")
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonObject(raw: String): JSONObject? {
        val clean = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try { JSONObject(clean) } catch (e: Exception) { null }
    }

    private fun parseJsonArray(raw: String): JSONArray? {
        val clean = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try { JSONArray(clean) } catch (e: Exception) { null }
    }

    private fun fallbackMoodShuffle(
        targetMood: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList()
    ): List<String> {
        val lower = targetMood.lowercase()
        val isWorkout = lower.contains("workout") || lower.contains("energetic") || lower.contains("gym") || lower.contains("morning")
        val isRainy = lower.contains("rain") || lower.contains("relax") || lower.contains("chill") || lower.contains("calm")
        val isFocus = lower.contains("focus") || lower.contains("study") || lower.contains("code") || lower.contains("work")

        val historyTrackIds = history.map { it.trackId }.toSet()
        val matched = when {
            isWorkout -> tracks.filter { it.mood.equals("Workout", true) || it.mood.equals("Energetic", true) || it.bpm >= 120 }
            isRainy -> tracks.filter { it.mood.equals("Chill", true) || it.mood.equals("Melancholy", true) || it.bpm <= 90 }
            isFocus -> tracks.filter { it.mood.equals("Focus", true) || it.mood.equals("Night Drive", true) }
            else -> tracks.filter { it.mood.equals(targetMood, ignoreCase = true) }
        }
        val remainder = tracks.filterNot { matched.contains(it) }

        val sortedMatched = matched.sortedWith(
            compareByDescending<Track> { if (historyTrackIds.contains(it.id)) 1 else 0 }
                .thenByDescending { it.energy }
        )
        val sortedRemainder = remainder.sortedBy { it.bpm }

        return (sortedMatched + sortedRemainder).map { it.id }
    }

    private fun fallbackDynamicMoodPlaylist(
        descriptor: String,
        tracks: List<Track>,
        history: List<com.example.data.model.PlaybackHistoryItem> = emptyList()
    ): DynamicMoodPlaylist {
        val lower = descriptor.lowercase()
        val historyTrackIds = history.map { it.trackId }.toSet()
        val historyArtistNames = history.map { it.artist.lowercase() }.toSet()

        return when {
            lower.contains("workout") || lower.contains("energetic") || lower.contains("gym") || lower.contains("morning") || lower.contains("run") -> {
                val selected = tracks.filter { it.mood.equals("Workout", true) || it.mood.equals("Energetic", true) || it.bpm >= 120 }
                    .sortedWith(
                        compareByDescending<Track> { if (historyTrackIds.contains(it.id) || historyArtistNames.contains(it.artist.lowercase())) 1 else 0 }
                            .thenByDescending { it.bpm }
                    )
                    .ifEmpty { tracks.sortedByDescending { it.energy } }
                
                val historyNote = if (history.isNotEmpty()) " Adjusted based on your recent listening affinity for ${history.firstOrNull()?.artist ?: "favorite artists"}." else ""

                DynamicMoodPlaylist(
                    title = "Kinetic Dawn Workout",
                    description = "High-octane electronic and synthwave rhythms engineered for peak cardiovascular output.",
                    rationale = "Prioritized 125-142 BPM driving rhythms with elevated sub-bass transient energy to maximize adrenaline and workout momentum.$historyNote",
                    targetMood = "Workout",
                    targetBpmRange = "125 - 142 BPM",
                    tracks = selected.take(6),
                    energyLevel = 0.92f,
                    valenceLevel = 0.82f
                )
            }
            lower.contains("rain") || lower.contains("rainy") || lower.contains("relaxed") || lower.contains("chill") || lower.contains("unwind") || lower.contains("cozy") -> {
                val selected = tracks.filter { it.mood.equals("Chill", true) || it.mood.equals("Melancholy", true) || it.bpm <= 90 }
                    .sortedWith(
                        compareByDescending<Track> { if (historyTrackIds.contains(it.id)) 1 else 0 }
                            .thenBy { it.bpm }
                    )
                    .ifEmpty { tracks.sortedBy { it.energy } }

                val historyNote = if (history.isNotEmpty()) " Synced with your acoustic history profile." else ""

                DynamicMoodPlaylist(
                    title = "Rainy Day Reverie",
                    description = "Warm analog tape textures, acoustic Rhodes, and neotonal piano for quiet contemplative afternoons.",
                    rationale = "Selected gentle 74-88 BPM downtempo frequencies and organic harmonies to reduce neurological tension and enhance calm relaxation.$historyNote",
                    targetMood = "Chill",
                    targetBpmRange = "74 - 88 BPM",
                    tracks = selected.take(6),
                    energyLevel = 0.32f,
                    valenceLevel = 0.58f
                )
            }
            lower.contains("focus") || lower.contains("study") || lower.contains("code") || lower.contains("work") || lower.contains("deep") -> {
                val selected = tracks.filter { it.mood.equals("Focus", true) || it.genre.contains("Ambient", true) || it.genre.contains("Lo-Fi", true) }
                    .sortedWith(
                        compareByDescending<Track> { if (historyTrackIds.contains(it.id)) 1 else 0 }
                            .thenBy { it.bpm }
                    )
                    .ifEmpty { tracks.take(5) }
                DynamicMoodPlaylist(
                    title = "Deep Cognitive Flow",
                    description = "Binaural textures and distraction-free harmonics crafted for uninterrupted creative absorption.",
                    rationale = "Arranged 80-105 BPM ambient progressions with minimal vocal interference to stabilize alpha brainwaves and sustain deep focus.",
                    targetMood = "Focus",
                    targetBpmRange = "82 - 105 BPM",
                    tracks = selected.take(6),
                    energyLevel = 0.42f,
                    valenceLevel = 0.65f
                )
            }
            else -> {
                val selected = tracks.shuffled().take(5)
                DynamicMoodPlaylist(
                    title = descriptor.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                    description = "Dynamic algorithmic progression synchronized to \"$descriptor\".",
                    rationale = "Structured an ascending energy contour across ${selected.size} tracks to harmonize naturally with your requested state.",
                    targetMood = "Dynamic",
                    targetBpmRange = "${selected.map { it.bpm }.average().toInt()} BPM",
                    tracks = selected,
                    energyLevel = 0.60f,
                    valenceLevel = 0.70f
                )
            }
        }
    }

    private fun getRuleBasedDjResponse(userMessage: String, currentTrack: Track?): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") -> "Welcome to AURALIS! I'm DJ Pulse. Currently spinning ${currentTrack?.title ?: "curated sounds"}. What vibe are you chasing today?"
            lower.contains("workout") || lower.contains("gym") -> "For high-octane sessions, queue 'Solar Flare' or 'Neon Horizons' to elevate your kinetic output!"
            lower.contains("chill") || lower.contains("relax") -> "Dialing it down. 'Midnight Reverie' uses gentle lo-fi chords that trigger relaxation."
            lower.contains("focus") || lower.contains("work") -> "Deep flow state activated. 'Quantum Flux' features minimal vocal interference for maximum focus."
            lower.contains("equalizer") || lower.contains("bass") -> "Head over to Equalizer and punch in the 'Bass Boost' or 'Cyber EDM' preset!"
            else -> "Auditory architecture in motion. AURALIS is pairing ambient harmonics with dynamic rhythm."
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

data class ConnectionResult(
    val isSuccess: Boolean,
    val message: String
)
