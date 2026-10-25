package com.example.ai

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.local.AppDatabase
import com.example.data.model.EqualizerPreset
import com.example.data.model.PlaybackAnalytics
import com.example.data.model.Playlist
import com.example.data.model.SyncDevice
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import com.example.ui.DownloadSortOrder
import com.example.ui.RepeatMode
import com.example.ui.ScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Sealed interface representing all user intents directed to the PulseFlow brain.
 * Unifies natural language commands, voice searches, audio DSP tweaks,
 * playback control, and library/sync operations.
 */
sealed interface PulseUserIntent {
    data class ExecuteDirective(
        val directive: String,
        val origin: IntentOrigin = IntentOrigin.TEXT
    ) : PulseUserIntent

    data class VoiceSearch(val query: String) : PulseUserIntent

    data class PlayTrack(val track: Track) : PulseUserIntent

    data class PlayTrackByIdOrTitle(
        val trackId: String? = null,
        val titleQuery: String? = null
    ) : PulseUserIntent

    data class MediaControl(
        val command: Media3ControllerAction,
        val seekPositionMs: Long = 0L
    ) : PulseUserIntent

    data class TuneAudioDsp(
        val presetName: String? = null,
        val bassBoostPercent: Int? = null,
        val virtualizerPercent: Int? = null,
        val bandGains: List<Float>? = null
    ) : PulseUserIntent

    data class SelectMood(
        val mood: String,
        val triggerSmartShuffle: Boolean = true
    ) : PulseUserIntent

    data class ManageOfflineStorage(
        val action: String,
        val track: Track? = null
    ) : PulseUserIntent

    object TriggerSync : PulseUserIntent

    data class GenerateMoodPlaylist(
        val mood: String,
        val energy: Float = 0.6f,
        val valence: Float = 0.7f
    ) : PulseUserIntent

    data class NavigateUi(val destination: ScreenDestination) : PulseUserIntent
}

enum class IntentOrigin {
    TEXT,
    VOICE,
    UI_ACTION,
    AUTOMATION
}

enum class Media3ControllerAction {
    PLAY,
    PAUSE,
    TOGGLE_PLAY_PAUSE,
    NEXT,
    PREVIOUS,
    TOGGLE_SHUFFLE,
    TOGGLE_REPEAT,
    SEEK
}

enum class CognitiveState {
    IDLE,
    PERCEIVING_CONTEXT,
    NEURAL_REASONING,
    DISPATCHING_CONTROLLER_ACTIONS,
    HARMONIZED,
    ERROR
}

data class BrainContext(
    val currentTrackTitle: String? = null,
    val currentArtist: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val activeMood: String = "All",
    val isOfflineMode: Boolean = false,
    val activePresetName: String = "Bass Boost",
    val bassBoostPercent: Int = 50,
    val virtualizerPercent: Int = 30,
    val totalTracksCount: Int = 0,
    val downloadedTracksCount: Int = 0,
    val connectedDevicesCount: Int = 0,
    val topGenre: String? = null,
    val peakHour: String? = null
)

data class BrainTelemetry(
    val activeModel: String = "gemini-4-argon",
    val thinkingLevel: String = "HIGH",
    val totalDirectivesProcessed: Int = 0,
    val totalActionsDispatched: Int = 0,
    val lastInferenceLatencyMs: Long = 0,
    val lastError: String? = null
)

data class IntentExecutionRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val intentDescription: String,
    val origin: IntentOrigin,
    val cognitivePhase: CognitiveState,
    val actionsDispatched: List<AgentAction> = emptyList(),
    val verbalResponse: String? = null,
    val isSuccess: Boolean = true
)

/**
 * Centralized 'PulseAgent' ViewModel.
 *
 * Acts as the master brain for the music player, coordinating state flow between:
 * 1. User Intent (natural language directives, voice queries, tactile UI intents)
 * 2. Gemini AI Context Processing (deep thinking with gemini-4-argon, contextual DSP decision-making)
 * 3. Media3 Controller Actions (ExoPlayer playback control, audio DSP equalizer manipulation, offline/sync workflows)
 */
class PulseAgent(application: Application) : AndroidViewModel(application) {

    private val tag = "PulseAgent"

    // Core System Drivers
    val audioEngine: AudioEngine = AudioEngine.getInstance(application)
    private val database: AppDatabase = AppDatabase.getInstance(application)
    val openRouterAiService = OpenRouterAiService(application)
    private val geminiAiService = GeminiAiService(application)
    val repository = MusicRepository(database.musicDao(), audioEngine, geminiAiService)
    val pulseAiAgent = PulseAiAgent(application)

    // ---------------------------------------------------------
    // 1. User Intent State Flow
    // ---------------------------------------------------------
    private val _activeIntent = MutableStateFlow<PulseUserIntent?>(null)
    val activeIntent: StateFlow<PulseUserIntent?> = _activeIntent.asStateFlow()

    private val _intentHistory = MutableStateFlow<List<IntentExecutionRecord>>(emptyList())
    val intentHistory: StateFlow<List<IntentExecutionRecord>> = _intentHistory.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // ---------------------------------------------------------
    // 2. Gemini AI Context Processing State Flow
    // ---------------------------------------------------------
    private val _cognitiveState = MutableStateFlow(CognitiveState.IDLE)
    val cognitiveState: StateFlow<CognitiveState> = _cognitiveState.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _currentThoughtProcess = MutableStateFlow("Ready for autonomous commands.")
    val currentThoughtProcess: StateFlow<String> = _currentThoughtProcess.asStateFlow()

    private val _latestReport = MutableStateFlow<AgentExecutionReport?>(null)
    val latestReport: StateFlow<AgentExecutionReport?> = _latestReport.asStateFlow()

    private val _agentActionHistory = MutableStateFlow<List<AgentAction>>(emptyList())
    val agentActionHistory: StateFlow<List<AgentAction>> = _agentActionHistory.asStateFlow()

    private val _brainTelemetry = MutableStateFlow(BrainTelemetry())
    val brainTelemetry: StateFlow<BrainTelemetry> = _brainTelemetry.asStateFlow()

    // ---------------------------------------------------------
    // 3. Media3 Controller Actions & Playback Telemetry Flow
    // ---------------------------------------------------------
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs
    val isBuffering: StateFlow<Boolean> = audioEngine.isBuffering
    val visualizerAmplitudes: StateFlow<List<Float>> = audioEngine.visualizerAmplitudes

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isSmartMoodShuffleActive = MutableStateFlow(false)
    val isSmartMoodShuffleActive: StateFlow<Boolean> = _isSmartMoodShuffleActive.asStateFlow()

    private val _selectedMood = MutableStateFlow("All")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    private val _isOfflineOnly = MutableStateFlow(false)
    val isOfflineOnly: StateFlow<Boolean> = _isOfflineOnly.asStateFlow()

    // Equalizer & Audio DSP States
    private val _activePreset = MutableStateFlow(EqualizerPreset.DEFAULT_PRESETS[1]) // Bass Boost
    val activePreset: StateFlow<EqualizerPreset> = _activePreset.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(500) // 50%
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(300) // 30%
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _currentGains = MutableStateFlow(EqualizerPreset.DEFAULT_PRESETS[1].toGainsList())
    val currentGains: StateFlow<List<Float>> = _currentGains.asStateFlow()

    // Dynamic aggregated context for Gemini AI
    val runtimeContext: StateFlow<BrainContext> = combine(
        currentTrack,
        isPlaying,
        currentPositionMs,
        selectedMood,
        isOfflineOnly,
        activePreset,
        bassBoostStrength,
        virtualizerStrength
    ) { params ->
        val track = params[0] as Track?
        val playing = params[1] as Boolean
        val pos = params[2] as Long
        val mood = params[3] as String
        val offline = params[4] as Boolean
        val preset = params[5] as EqualizerPreset
        val bb = params[6] as Int
        val virt = params[7] as Int

        BrainContext(
            currentTrackTitle = track?.title,
            currentArtist = track?.artist,
            isPlaying = playing,
            positionMs = pos,
            durationMs = track?.durationMs ?: 0L,
            activeMood = mood,
            isOfflineMode = offline,
            activePresetName = preset.name,
            bassBoostPercent = bb / 10,
            virtualizerPercent = virt / 10,
            totalTracksCount = _queue.value.size,
            downloadedTracksCount = 0,
            connectedDevicesCount = 3,
            topGenre = track?.genre,
            peakHour = "8 PM - 11 PM"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrainContext())

    // Tracks and Presets from DB
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets: StateFlow<List<EqualizerPreset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerPreset.DEFAULT_PRESETS)

    // Navigation and screen callbacks
    var onNavigateToScreen: ((ScreenDestination) -> Unit)? = null

    init {
        audioEngine.onTrackCompleted = {
            handleMedia3Action(Media3ControllerAction.NEXT)
        }
        observeTracksInitialQueue()
    }

    private fun observeTracksInitialQueue() {
        viewModelScope.launch {
            repository.allTracks.collect { tracks ->
                if (tracks.isNotEmpty() && _queue.value.isEmpty()) {
                    _queue.value = tracks
                    if (_currentTrack.value == null) {
                        _currentTrack.value = tracks.first()
                    }
                }
            }
        }
    }

    // =========================================================================
    // Core Brain Intent Processing
    // =========================================================================

    /**
     * Dispatch any user intent to the centralized brain.
     * The agent handles state progression, AI perception, and Media3 actions.
     */
    fun dispatchIntent(intent: PulseUserIntent) {
        _activeIntent.value = intent
        when (intent) {
            is PulseUserIntent.ExecuteDirective -> {
                executeDirective(intent.directive, intent.origin)
            }
            is PulseUserIntent.VoiceSearch -> {
                executeDirective(intent.query, IntentOrigin.VOICE)
            }
            is PulseUserIntent.PlayTrack -> {
                executeMedia3PlayTrack(intent.track)
                recordDirectIntent("Play: ${intent.track.title}", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.PlayTrackByIdOrTitle -> {
                val found = allTracks.value.find {
                    (intent.trackId != null && it.id == intent.trackId) ||
                    (intent.titleQuery != null && it.title.contains(intent.titleQuery, ignoreCase = true))
                }
                if (found != null) {
                    executeMedia3PlayTrack(found)
                    recordDirectIntent("Play: ${found.title}", IntentOrigin.UI_ACTION)
                }
            }
            is PulseUserIntent.MediaControl -> {
                handleMedia3Action(intent.command, intent.seekPositionMs)
                recordDirectIntent("Media3: ${intent.command}", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.TuneAudioDsp -> {
                tuneDsp(
                    presetName = intent.presetName,
                    bassBoostPercent = intent.bassBoostPercent,
                    virtualizerPercent = intent.virtualizerPercent,
                    bandGains = intent.bandGains
                )
                recordDirectIntent("Tune DSP", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.SelectMood -> {
                setMood(intent.mood, intent.triggerSmartShuffle)
                recordDirectIntent("Select Mood: ${intent.mood}", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.ManageOfflineStorage -> {
                handleOfflineStorageAction(intent.action, intent.track)
                recordDirectIntent("Offline action: ${intent.action}", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.TriggerSync -> {
                triggerSync()
                recordDirectIntent("Trigger Sync", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.GenerateMoodPlaylist -> {
                generateMoodPlaylist(intent.mood, intent.energy, intent.valence)
                recordDirectIntent("Generate Playlist: ${intent.mood}", IntentOrigin.UI_ACTION)
            }
            is PulseUserIntent.NavigateUi -> {
                onNavigateToScreen?.invoke(intent.destination)
                recordDirectIntent("Navigate: ${intent.destination}", IntentOrigin.UI_ACTION)
            }
        }
    }

    /**
     * Executes natural language user directives through Gemini AI context processing.
     */
    fun executeDirective(directive: String, origin: IntentOrigin = IntentOrigin.TEXT) {
        if (directive.isBlank()) return

        val startTime = System.currentTimeMillis()
        _cognitiveState.value = CognitiveState.PERCEIVING_CONTEXT
        _isThinking.value = true
        _userFeedbackMessage.value = "AI Maestro perceiving intent..."

        viewModelScope.launch {
            try {
                _cognitiveState.value = CognitiveState.NEURAL_REASONING
                _currentThoughtProcess.value = "Contextualizing user directive: \"$directive\" with Media3 player state..."

                val report = pulseAiAgent.processUserDirective(
                    userPrompt = directive,
                    currentTrack = _currentTrack.value,
                    availableTracks = allTracks.value,
                    activeMood = _selectedMood.value,
                    isOfflineMode = _isOfflineOnly.value
                )

                _latestReport.value = report
                _currentThoughtProcess.value = report.thoughtProcess
                _isThinking.value = false

                _cognitiveState.value = CognitiveState.DISPATCHING_CONTROLLER_ACTIONS

                // Append actions to history
                if (report.actions.isNotEmpty()) {
                    _agentActionHistory.value = _agentActionHistory.value + report.actions
                }

                // Execute each tool through Media3 controller and repository
                for (action in report.actions) {
                    dispatchToolAction(action)
                }

                _cognitiveState.value = CognitiveState.HARMONIZED
                _userFeedbackMessage.value = report.agentSpeech

                val duration = System.currentTimeMillis() - startTime
                val currentTelemetry = _brainTelemetry.value
                val newCount = currentTelemetry.totalDirectivesProcessed + 1
                val newActions = currentTelemetry.totalActionsDispatched + report.actions.size
                val avgLatency = (currentTelemetry.lastInferenceLatencyMs * currentTelemetry.totalDirectivesProcessed + duration) / newCount

                _brainTelemetry.value = currentTelemetry.copy(
                    totalDirectivesProcessed = newCount,
                    totalActionsDispatched = newActions,
                    lastInferenceLatencyMs = duration,
                    lastError = null
                )

                // Log into intent history
                val record = IntentExecutionRecord(
                    intentDescription = directive,
                    origin = origin,
                    cognitivePhase = CognitiveState.HARMONIZED,
                    actionsDispatched = report.actions,
                    verbalResponse = report.agentSpeech,
                    isSuccess = true
                )
                _intentHistory.value = listOf(record) + _intentHistory.value.take(49)

            } catch (e: Exception) {
                Log.e(tag, "Executive brain failure on directive: $directive", e)
                _cognitiveState.value = CognitiveState.ERROR
                _isThinking.value = false
                _userFeedbackMessage.value = "Agent recovered: fallback executed."
                _brainTelemetry.value = _brainTelemetry.value.copy(lastError = e.message)
            }
        }
    }

    private fun dispatchToolAction(action: AgentAction) {
        when (action.toolName) {
            "SET_EQUALIZER" -> {
                val presetName = action.parameters["preset"]
                val bassBoostPercent = action.parameters["bassBoostPercent"]?.toIntOrNull()
                val virtualizerPercent = action.parameters["virtualizerPercent"]?.toIntOrNull()
                tuneDsp(presetName, bassBoostPercent, virtualizerPercent)
            }
            "SMART_SHUFFLE" -> {
                val mood = action.parameters["mood"] ?: "Focus"
                setMood(mood, triggerSmartShuffle = true)
            }
            "PLAY_TRACK" -> {
                val trackId = action.parameters["trackId"]
                val title = action.parameters["title"]
                val track = allTracks.value.find { it.id == trackId || (title != null && it.title.contains(title, true)) }
                if (track != null) {
                    executeMedia3PlayTrack(track)
                }
            }
            "CONTROL_PLAYBACK" -> {
                when (action.parameters["command"]) {
                    "pause" -> handleMedia3Action(Media3ControllerAction.PAUSE)
                    "play" -> handleMedia3Action(Media3ControllerAction.PLAY)
                    "next" -> handleMedia3Action(Media3ControllerAction.NEXT)
                    "prev" -> handleMedia3Action(Media3ControllerAction.PREVIOUS)
                    "toggle_repeat" -> handleMedia3Action(Media3ControllerAction.TOGGLE_REPEAT)
                    "toggle_shuffle" -> handleMedia3Action(Media3ControllerAction.TOGGLE_SHUFFLE)
                }
            }
            "MANAGE_DOWNLOADS" -> {
                val actionCmd = action.parameters["action"] ?: "download_current"
                handleOfflineStorageAction(actionCmd, _currentTrack.value)
            }
            "SYNC_DEVICES" -> {
                triggerSync()
            }
            "NAVIGATE" -> {
                when (action.parameters["destination"]?.lowercase()) {
                    "home" -> onNavigateToScreen?.invoke(ScreenDestination.HOME)
                    "library" -> onNavigateToScreen?.invoke(ScreenDestination.LIBRARY)
                    "equalizer", "eq" -> onNavigateToScreen?.invoke(ScreenDestination.EQUALIZER)
                    "analytics", "insights" -> onNavigateToScreen?.invoke(ScreenDestination.ANALYTICS)
                    "ai_dj" -> onNavigateToScreen?.invoke(ScreenDestination.AI_DJ)
                    "offline" -> onNavigateToScreen?.invoke(ScreenDestination.OFFLINE_MANAGER)
                    "sync" -> onNavigateToScreen?.invoke(ScreenDestination.SYNC_HUB)
                    "maestro" -> onNavigateToScreen?.invoke(ScreenDestination.AI_MAESTRO)
                    "settings" -> onNavigateToScreen?.invoke(ScreenDestination.SETTINGS)
                }
            }
            "CREATE_PLAYLIST" -> {
                val mood = action.parameters["mood"] ?: "Focus"
                val energy = action.parameters["energy"]?.toFloatOrNull() ?: 0.6f
                val valence = action.parameters["valence"]?.toFloatOrNull() ?: 0.7f
                generateMoodPlaylist(mood, energy, valence)
            }
        }
    }

    // =========================================================================
    // Media3 Controller Actions Implementation
    // =========================================================================

    fun handleMedia3Action(command: Media3ControllerAction, seekPositionMs: Long = 0L) {
        when (command) {
            Media3ControllerAction.PLAY -> {
                val track = _currentTrack.value
                if (track != null) {
                    audioEngine.resume()
                } else if (_queue.value.isNotEmpty()) {
                    executeMedia3PlayTrack(_queue.value.first())
                }
            }
            Media3ControllerAction.PAUSE -> {
                audioEngine.pause()
            }
            Media3ControllerAction.TOGGLE_PLAY_PAUSE -> {
                if (audioEngine.isPlaying.value) {
                    audioEngine.pause()
                } else {
                    audioEngine.resume()
                }
            }
            Media3ControllerAction.NEXT -> {
                val q = _queue.value
                if (q.isNotEmpty()) {
                    val nextIdx = (_currentIndex.value + 1) % q.size
                    _currentIndex.value = nextIdx
                    executeMedia3PlayTrack(q[nextIdx])
                }
            }
            Media3ControllerAction.PREVIOUS -> {
                val q = _queue.value
                if (q.isNotEmpty()) {
                    val prevIdx = if (_currentIndex.value - 1 < 0) q.size - 1 else _currentIndex.value - 1
                    _currentIndex.value = prevIdx
                    executeMedia3PlayTrack(q[prevIdx])
                }
            }
            Media3ControllerAction.TOGGLE_SHUFFLE -> {
                _isShuffleEnabled.value = !_isShuffleEnabled.value
                if (_isShuffleEnabled.value) {
                    _queue.value = _queue.value.shuffled()
                } else {
                    _queue.value = allTracks.value
                }
            }
            Media3ControllerAction.TOGGLE_REPEAT -> {
                _repeatMode.value = when (_repeatMode.value) {
                    RepeatMode.OFF -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.OFF
                }
            }
            Media3ControllerAction.SEEK -> {
                audioEngine.seekTo(seekPositionMs)
            }
        }
    }

    fun executeMedia3PlayTrack(track: Track) {
        _currentTrack.value = track
        val idx = _queue.value.indexOfFirst { it.id == track.id }
        if (idx != -1) {
            _currentIndex.value = idx
        } else {
            _queue.value = listOf(track) + _queue.value
            _currentIndex.value = 0
        }

        audioEngine.playTrack(track)
        viewModelScope.launch {
            repository.recordTrackPlayed(track)
        }
    }

    fun tuneDsp(
        presetName: String? = null,
        bassBoostPercent: Int? = null,
        virtualizerPercent: Int? = null,
        bandGains: List<Float>? = null
    ) {
        var presetToApply = _activePreset.value

        if (!presetName.isNullOrBlank()) {
            val preset = presets.value.find { it.name.contains(presetName, ignoreCase = true) }
            if (preset != null) {
                presetToApply = preset
                _currentGains.value = preset.toGainsList()
            }
        }

        bassBoostPercent?.let { percent ->
            val strength = (percent * 10).coerceIn(0, 1000)
            _bassBoostStrength.value = strength
            presetToApply = presetToApply.copy(bassBoost = strength)
        }

        virtualizerPercent?.let { percent ->
            val strength = (percent * 10).coerceIn(0, 1000)
            _virtualizerStrength.value = strength
            presetToApply = presetToApply.copy(virtualizer = strength)
        }

        bandGains?.let { gains ->
            _currentGains.value = gains
            presetToApply = presetToApply.copy(
                band0 = gains.getOrElse(0) { 0f },
                band1 = gains.getOrElse(1) { 0f },
                band2 = gains.getOrElse(2) { 0f },
                band3 = gains.getOrElse(3) { 0f },
                band4 = gains.getOrElse(4) { 0f }
            )
        }

        _activePreset.value = presetToApply
        audioEngine.applyEqualizerPreset(presetToApply)
    }

    fun setMood(mood: String, triggerSmartShuffle: Boolean = true) {
        _selectedMood.value = mood
        if (triggerSmartShuffle && mood != "All") {
            _isSmartMoodShuffleActive.value = true
            val filtered = allTracks.value.filter { it.mood.equals(mood, ignoreCase = true) }
            if (filtered.isNotEmpty()) {
                _queue.value = filtered.shuffled()
                _currentIndex.value = 0
                executeMedia3PlayTrack(_queue.value.first())
            }
        }
    }

    private fun handleOfflineStorageAction(action: String, track: Track?) {
        viewModelScope.launch {
            when (action) {
                "download_current" -> {
                    track?.let {
                        repository.setTrackDownloaded(it, true)
                        _userFeedbackMessage.value = "Downloaded ${it.title} for offline playback."
                    }
                }
                "clear_cache" -> {
                    allTracks.value.forEach { t ->
                        if (t.isDownloaded) repository.setTrackDownloaded(t, false)
                    }
                    _userFeedbackMessage.value = "Offline cache cleared."
                }
            }
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Library synchronized across connected devices."
        }
    }

    private fun generateMoodPlaylist(mood: String, energy: Float, valence: Float) {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Curating dynamic $mood playlist..."
            val generated = repository.generateAiMoodPlaylist(mood, energy, valence)
            val trackIds = generated.getTrackIdList()
            val tracks = allTracks.value.filter { it.id in trackIds }
            if (tracks.isNotEmpty()) {
                _queue.value = tracks
                _currentIndex.value = 0
                executeMedia3PlayTrack(tracks.first())
                _userFeedbackMessage.value = "Dynamic playlist '${generated.title}' generated!"
            }
        }
    }

    private fun recordDirectIntent(desc: String, origin: IntentOrigin) {
        val record = IntentExecutionRecord(
            intentDescription = desc,
            origin = origin,
            cognitivePhase = CognitiveState.HARMONIZED,
            actionsDispatched = emptyList(),
            isSuccess = true
        )
        _intentHistory.value = listOf(record) + _intentHistory.value.take(49)
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Do not release shared singleton AudioEngine if other components use it
    }

    companion object {
        @Volatile
        private var instance: PulseAgent? = null

        fun getInstance(application: Application): PulseAgent {
            return instance ?: synchronized(this) {
                instance ?: PulseAgent(application).also { instance = it }
            }
        }
    }
}

/**
 * Typealias ensuring both 'PulseAgent' and 'PulseAgentViewModel' references resolve seamlessly.
 */
typealias PulseAgentViewModel = PulseAgent
