package com.example.ai

import android.content.Context
import android.util.Log
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

class PulseAiAgent(private val context: Context? = null) {

    private val tag = "PulseAiAgent"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var openRouterService: OpenRouterAiService? = context?.let { OpenRouterAiService(it) }

    fun setContext(ctx: Context) {
        openRouterService = OpenRouterAiService(ctx)
    }

    suspend fun processUserDirective(
        userPrompt: String,
        currentTrack: Track?,
        availableTracks: List<Track>,
        activeMood: String,
        isOfflineMode: Boolean
    ): AgentExecutionReport = withContext(Dispatchers.IO) {
        val apiKey = openRouterService?.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return@withContext fallbackAgentReasoning(userPrompt, currentTrack, availableTracks)
        }

        try {
            val tracksSummary = availableTracks.take(15).joinToString(", ") {
                "${it.title} (${it.artist}, mood: ${it.mood}, bpm: ${it.bpm})"
            }

            val systemInstruction = """
                You are 'AI Maestro', executive brain for AURALIS AI Music Player.
                Current app state:
                - Playing track: ${currentTrack?.let { "${it.title} by ${it.artist}" } ?: "None"}
                - Active mood: $activeMood
                - Offline mode: $isOfflineMode
                - Sample library tracks: $tracksSummary

                Available Agent Tools:
                1. "SET_EQUALIZER": parameters {"preset": "Bass Boost"|"Cyber EDM"|"Vocal Clarity"|"Acoustic Warmth"|"Rock Stage"|"Chill Lo-Fi"|"Flat", "bassBoostPercent": "0-100", "virtualizerPercent": "0-100"}
                2. "SMART_SHUFFLE": parameters {"mood": "Chill"|"Focus"|"Energetic"|"Workout"|"Night Drive"|"Euphoria"|"Melancholy"}
                3. "PLAY_TRACK": parameters {"trackId": "...", "title": "..."}
                4. "CONTROL_PLAYBACK": parameters {"command": "play"|"pause"|"next"|"prev"|"toggle_repeat"|"toggle_shuffle"}
                5. "MANAGE_DOWNLOADS": parameters {"action": "download_current"|"download_mood"|"clear_cache"}
                6. "SYNC_DEVICES": parameters {"action": "sync_now"}
                7. "NAVIGATE": parameters {"destination": "home"|"library"|"equalizer"|"analytics"|"ai_dj"|"offline"|"sync"|"settings"}
                8. "CREATE_PLAYLIST": parameters {"mood": "...", "energy": "0.1-1.0", "valence": "0.1-1.0"}

                Analyze user prompt: "$userPrompt".
                Respond strictly in JSON format:
                {
                   "thoughtProcess": "Internal reasoning step-by-step",
                   "actions": [
                       {"toolName": "TOOL_NAME", "parameters": {"param1": "value1"}, "description": "Readable description"}
                   ],
                   "agentSpeech": "Short confirmation statement to user"
                }
            """.trimIndent()

            val payload = JSONObject().apply {
                put("model", openRouterService?.getModel() ?: "google/gemini-2.5-flash")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
                put("temperature", 0.3)
            }

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("HTTP-Referer", "https://auralis.app")
                .header("X-Title", "AURALIS AI Music Player")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val rawStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.w(tag, "OpenRouter error ${response.code}: $rawStr")
                return@withContext fallbackAgentReasoning(userPrompt, currentTrack, availableTracks)
            }

            val extracted = extractChoiceContent(rawStr) ?: ""
            val cleanedJson = cleanJsonContent(extracted)
            val json = JSONObject(cleanedJson)

            val thoughts = json.optString("thoughtProcess", "Executing user directive via OpenRouter AI.")
            val speech = json.optString("agentSpeech", "Actions complete, maestro.")
            val actionsArray = json.optJSONArray("actions") ?: JSONArray()

            val actions = mutableListOf<AgentAction>()
            for (i in 0 until actionsArray.length()) {
                val actObj = actionsArray.getJSONObject(i)
                val toolName = actObj.getString("toolName")
                val desc = actObj.optString("description", "Executing $toolName")
                val paramsObj = actObj.optJSONObject("parameters")
                val paramsMap = mutableMapOf<String, String>()
                paramsObj?.keys()?.forEach { k ->
                    paramsMap[k] = paramsObj.getString(k)
                }
                actions.add(AgentAction(toolName, paramsMap, desc))
            }

            return@withContext AgentExecutionReport(thoughts, actions, speech)
        } catch (e: Exception) {
            Log.e(tag, "Agent processing error", e)
            return@withContext fallbackAgentReasoning(userPrompt, currentTrack, availableTracks)
        }
    }

    private fun fallbackAgentReasoning(
        prompt: String,
        currentTrack: Track?,
        tracks: List<Track>
    ): AgentExecutionReport {
        val lower = prompt.lowercase()
        val actions = mutableListOf<AgentAction>()
        var thoughts = "Parsing natural language intent via autonomous AURALIS heuristics."
        var speech = "Understood. Executing system parameters now."

        when {
            lower.contains("setting") -> {
                thoughts = "User requested settings menu. Opening Settings screen."
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "settings"), "Navigating to Settings screen"))
                speech = "Settings opened."
            }
            lower.contains("insight") || lower.contains("analytics") || lower.contains("stat") -> {
                thoughts = "User requested listening insights & psychology analytics."
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "analytics"), "Opening Insights & Music Psychology screen"))
                speech = "Listening insights and psychology analytics loaded."
            }
            lower.contains("youtube") -> {
                thoughts = "User requested YouTube streaming or import."
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "library"), "Opening Library to YouTube streams section"))
                speech = "YouTube streams active. Tap 'Import YouTube Link' to play any stream!"
            }
            lower.contains("gym") || lower.contains("workout") -> {
                thoughts = "User preparing for physical training. Reconfiguring audio DSP for bass saturation and queuing high-octane BPM tracks."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Cyber EDM", "bassBoostPercent" to "80"), "Sub-bass boost to 80% with Cyber EDM preset"))
                actions.add(AgentAction("SMART_SHUFFLE", mapOf("mood" to "Workout"), "Reordering queue for Workout tempo progression"))
                speech = "Workout protocol engaged! Bass boost dialed to 80% and high-kinetic beats are rolling."
            }
            lower.contains("focus") || lower.contains("study") || lower.contains("work") -> {
                thoughts = "User entering deep cognitive work. Optimizing acoustic mids and queuing binaural focus flow."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Acoustic Warmth", "bassBoostPercent" to "20"), "Balancing frequencies for long cognitive sessions"))
                actions.add(AgentAction("SMART_SHUFFLE", mapOf("mood" to "Focus"), "Arranging deep focus binaural trajectory"))
                speech = "Deep flow state activated. Frequency harmonics smoothed to eliminate auditory distractions."
            }
            lower.contains("chill") || lower.contains("relax") || lower.contains("sleep") -> {
                thoughts = "User winding down. Applying Chill Lo-Fi preset and queuing low-arousal tracks."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Chill Lo-Fi", "virtualizerPercent" to "50"), "Engaging soft lo-fi curve with spatial virtualizer"))
                actions.add(AgentAction("SMART_SHUFFLE", mapOf("mood" to "Chill"), "Queuing relaxing ambient beats"))
                speech = "Relaxation mode initiated. Soft lo-fi soundstage active."
            }
            lower.contains("bass") -> {
                thoughts = "User requested amplified low end. Maximizing sub-bass DSP."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Bass Boost", "bassBoostPercent" to "90"), "Boosting sub-bass to 90%"))
                speech = "Bass boost pinned to 90%. Feel the sub-harmonics!"
            }
            lower.contains("equalizer") || lower.contains("eq") -> {
                thoughts = "User requested equalizer controls."
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "equalizer"), "Opening 5-Band Studio Equalizer"))
                speech = "Opening Studio Equalizer."
            }
            lower.contains("sync") -> {
                thoughts = "User requested cross-device library harmonization."
                actions.add(AgentAction("SYNC_DEVICES", mapOf("action" to "sync_now"), "Broadcasting library state to connected devices"))
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "sync"), "Opening Cross-Device Sync Hub"))
                speech = "Cross-device sync executed."
            }
            lower.contains("download") || lower.contains("offline") -> {
                thoughts = "User requested offline storage preparation."
                actions.add(AgentAction("MANAGE_DOWNLOADS", mapOf("action" to "download_current"), "Caching current playback tracks to offline storage"))
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "offline"), "Opening Offline Storage Manager"))
                speech = "Offline mode ready. Cached your tracks for offline listening."
            }
            lower.contains("pause") || lower.contains("stop") -> {
                actions.add(AgentAction("CONTROL_PLAYBACK", mapOf("command" to "pause"), "Halting audio playback"))
                speech = "Playback paused."
            }
            lower.contains("next") || lower.contains("skip") -> {
                actions.add(AgentAction("CONTROL_PLAYBACK", mapOf("command" to "next"), "Advancing to next track in queue"))
                speech = "Skipping forward."
            }
            lower.contains("prev") || lower.contains("back") -> {
                actions.add(AgentAction("CONTROL_PLAYBACK", mapOf("command" to "prev"), "Rewinding to previous track"))
                speech = "Playing previous track."
            }
            lower.contains("resume") || (lower.contains("play") && !lower.contains("playlist")) -> {
                actions.add(AgentAction("CONTROL_PLAYBACK", mapOf("command" to "play"), "Resuming audio playback"))
                speech = "Playback resumed."
            }
            lower.contains("playlist") -> {
                thoughts = "User requested playlist generation."
                actions.add(AgentAction("CREATE_PLAYLIST", mapOf("mood" to "Focus", "energy" to "0.6", "valence" to "0.7"), "Generating AI mood playlist"))
                speech = "AI mood playlist crafted."
            }
            lower.contains("vocal") || lower.contains("voice") || lower.contains("podcast") -> {
                thoughts = "User requested enhanced vocal fidelity. Engaging Vocal Clarity preset."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Vocal Clarity", "bassBoostPercent" to "15", "virtualizerPercent" to "20"), "Targeting 1kHz-4kHz dialogue harmonics"))
                speech = "Vocal Clarity active."
            }
            lower.contains("edm") || lower.contains("club") || lower.contains("party") -> {
                thoughts = "User requested party audio equalization. Engaging Cyber EDM profile."
                actions.add(AgentAction("SET_EQUALIZER", mapOf("preset" to "Cyber EDM", "bassBoostPercent" to "75", "virtualizerPercent" to "50"), "Boosting sub-bass and spatial virtualizer"))
                actions.add(AgentAction("SMART_SHUFFLE", mapOf("mood" to "Energetic"), "Arranging high-kinetic track queue"))
                speech = "Cyber EDM club profile engaged!"
            }
            else -> {
                thoughts = "Query processed. Searching library for '$prompt'."
                actions.add(AgentAction("NAVIGATE", mapOf("destination" to "library"), "Searching library"))
                speech = "Searching music library for '$prompt'."
            }
        }

        return AgentExecutionReport(thoughts, actions, speech)
    }

    private fun extractChoiceContent(jsonStr: String): String? {
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

    private fun cleanJsonContent(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
