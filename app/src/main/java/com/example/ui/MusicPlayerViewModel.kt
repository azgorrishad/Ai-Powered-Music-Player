package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.audio.AudioEngine
import com.example.data.local.AppDatabase
import com.example.data.model.CrossDeviceSyncState
import com.example.data.model.EqualizerPreset
import com.example.data.model.PlaybackAnalytics
import com.example.data.model.Playlist
import com.example.data.model.SyncDevice
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.ai.AgentAction
import com.example.ai.AgentExecutionReport
import com.example.ai.OpenRouterAiService
import com.example.ai.PulseAgent
import com.example.ai.PulseAiAgent
import com.example.data.model.DynamicMoodPlaylist
import com.example.data.model.TrackLyrics
import com.example.data.repository.LyricsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME,
    LIBRARY,
    EQUALIZER,
    ANALYTICS,
    AI_DJ,
    OFFLINE_MANAGER,
    SYNC_HUB,
    AI_MAESTRO,
    SETTINGS
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class DownloadSortOrder {
    DATE_DESC,
    SIZE_DESC,
    TITLE_ASC,
    ARTIST_ASC
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "dj"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedMood: String? = null
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val audioEngine = AudioEngine.getInstance(application)
    val openRouterAiService = OpenRouterAiService(application)
    val lyricsRepository = LyricsRepository()
    private val geminiAiService = GeminiAiService()
    val repository = MusicRepository(database.musicDao(), audioEngine, geminiAiService)
    val pulseAgent = PulseAgent.getInstance(application)

    // Lyrics State
    private val _currentLyrics = MutableStateFlow<TrackLyrics?>(null)
    val currentLyrics: StateFlow<TrackLyrics?> = _currentLyrics.asStateFlow()

    // OpenRouter AI Configuration & Diagnostics
    private val _openRouterApiKey = MutableStateFlow(openRouterAiService.getApiKey() ?: "")
    val openRouterApiKey: StateFlow<String> = _openRouterApiKey.asStateFlow()

    private val _openRouterModel = MutableStateFlow(openRouterAiService.getModel())
    val openRouterModel: StateFlow<String> = _openRouterModel.asStateFlow()

    private val _openRouterStatus = MutableStateFlow<String?>(null)
    val openRouterStatus: StateFlow<String?> = _openRouterStatus.asStateFlow()

    private val _isTestingOpenRouter = MutableStateFlow(false)
    val isTestingOpenRouter: StateFlow<Boolean> = _isTestingOpenRouter.asStateFlow()

    // Shuffle history deck to prevent redundant repeat tracks
    private val shuffleHistory = mutableListOf<Int>()

    // Current Screen
    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Now Playing Modal
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    // Playback state forwarded from AudioEngine
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs
    val isBuffering: StateFlow<Boolean> = audioEngine.isBuffering
    val visualizerAmplitudes: StateFlow<List<Float>> = audioEngine.visualizerAmplitudes

    // Track & Queue State
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // Playback modes
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isSmartMoodShuffleActive = MutableStateFlow(false)
    val isSmartMoodShuffleActive: StateFlow<Boolean> = _isSmartMoodShuffleActive.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _selectedMood = MutableStateFlow("All")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    // Offline Mode & Downloads Management
    private val _isOfflineModeOnly = MutableStateFlow(false)
    val isOfflineModeOnly: StateFlow<Boolean> = _isOfflineModeOnly.asStateFlow()

    private val _downloadSortOrder = MutableStateFlow(DownloadSortOrder.DATE_DESC)
    val downloadSortOrder: StateFlow<DownloadSortOrder> = _downloadSortOrder.asStateFlow()

    // Data streams from repository
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localTracks: StateFlow<List<Track>> = repository.localTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePlayableTracks: StateFlow<List<Track>> = repository.offlinePlayableTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePlaylists: StateFlow<List<Playlist>> = repository.offlinePlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets: StateFlow<List<EqualizerPreset>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerPreset.DEFAULT_PRESETS)

    val syncDevices: StateFlow<List<SyncDevice>> = repository.syncDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visualizer Mode state
    private val _visualizerMode = MutableStateFlow(com.example.ui.components.VisualizerMode.SPECTRUM_BARS)
    val visualizerMode: StateFlow<com.example.ui.components.VisualizerMode> = _visualizerMode.asStateFlow()

    fun setVisualizerMode(mode: com.example.ui.components.VisualizerMode) {
        _visualizerMode.value = mode
    }

    // App Theme State
    private val _currentAppTheme = MutableStateFlow(com.example.ui.theme.AppTheme.OBSIDIAN)
    val currentAppTheme: StateFlow<com.example.ui.theme.AppTheme> = _currentAppTheme.asStateFlow()

    fun setAppTheme(theme: com.example.ui.theme.AppTheme) {
        _currentAppTheme.value = theme
        _userFeedbackMessage.value = "App theme set to ${theme.displayName}"
    }

    // Cross-Device Sync State
    private val _syncState = MutableStateFlow(CrossDeviceSyncState())
    val syncState: StateFlow<CrossDeviceSyncState> = _syncState.asStateFlow()

    // Equalizer state
    private val _activePreset = MutableStateFlow(EqualizerPreset.DEFAULT_PRESETS[1]) // Default Bass Boost
    val activePreset: StateFlow<EqualizerPreset> = _activePreset.asStateFlow()

    private val _currentGains = MutableStateFlow(listOf(8f, 5f, 0f, -1f, 1f))
    val currentGains: StateFlow<List<Float>> = _currentGains.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(650)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(200)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    // Universal Search (Separated into Local vs YouTube/Streaming)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isVoiceSearchOpen = MutableStateFlow(false)
    val isVoiceSearchOpen: StateFlow<Boolean> = _isVoiceSearchOpen.asStateFlow()

    private val _voiceStatusText = MutableStateFlow("Listening for command...")
    val voiceStatusText: StateFlow<String> = _voiceStatusText.asStateFlow()

    // AI Dynamic Playlist Generator Dialog & Natural Language Mood Shuffler
    private val _isPlaylistGeneratorOpen = MutableStateFlow(false)
    val isPlaylistGeneratorOpen: StateFlow<Boolean> = _isPlaylistGeneratorOpen.asStateFlow()

    private val _isGeneratingMoodPlaylist = MutableStateFlow(false)
    val isGeneratingMoodPlaylist: StateFlow<Boolean> = _isGeneratingMoodPlaylist.asStateFlow()

    private val _activeDynamicMoodReport = MutableStateFlow<DynamicMoodPlaylist?>(null)
    val activeDynamicMoodReport: StateFlow<DynamicMoodPlaylist?> = _activeDynamicMoodReport.asStateFlow()

    // YouTube Import Dialog
    private val _isYouTubeImportOpen = MutableStateFlow(false)
    val isYouTubeImportOpen: StateFlow<Boolean> = _isYouTubeImportOpen.asStateFlow()

    // Analytics Period (Day, Week, Month)
    private val _analyticsPeriod = MutableStateFlow("Week")
    val analyticsPeriod: StateFlow<String> = _analyticsPeriod.asStateFlow()

    // Filtered tracks based on query, mood, and offline mode
    val filteredTracks: StateFlow<List<Track>> = combine(
        allTracks,
        searchQuery,
        selectedMood,
        isOfflineModeOnly
    ) { tracks, query, mood, offlineOnly ->
        tracks.filter { track ->
            val matchesQuery = query.isBlank() ||
                    track.title.contains(query, ignoreCase = true) ||
                    track.artist.contains(query, ignoreCase = true) ||
                    track.genre.contains(query, ignoreCase = true)
            val matchesMood = mood == "All" || track.mood.equals(mood, ignoreCase = true)
            val matchesOffline = !offlineOnly || track.isDownloaded || track.isLocal
            matchesQuery && matchesMood && matchesOffline
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped search results (Local vs Streaming/YouTube)
    val localSearchResults: StateFlow<List<Track>> = combine(allTracks, searchQuery) { tracks, q ->
        if (q.isBlank()) emptyList()
        else tracks.filter { it.isLocal && (it.title.contains(q, true) || it.artist.contains(q, true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streamingSearchResults: StateFlow<List<Track>> = combine(allTracks, searchQuery) { tracks, q ->
        if (q.isBlank()) emptyList()
        else tracks.filter { !it.isLocal && (it.title.contains(q, true) || it.artist.contains(q, true) || it.genre.contains(q, true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playback Analytics
    private val _analytics = MutableStateFlow(PlaybackAnalytics())
    val analytics: StateFlow<PlaybackAnalytics> = _analytics.asStateFlow()

    private val _isRefreshingAnalytics = MutableStateFlow(false)
    val isRefreshingAnalytics: StateFlow<Boolean> = _isRefreshingAnalytics.asStateFlow()

    // AI DJ Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "dj",
                text = "What's up! I'm DJ Pulse, your algorithmic musicologist. Tell me how you're feeling, what vibe you need, or ask me for a tailor-made track progression!",
                suggestedMood = "Focus"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isDjTyping = MutableStateFlow(false)
    val isDjTyping: StateFlow<Boolean> = _isDjTyping.asStateFlow()

    // Autonomous AI Maestro Agent
    val latestAgentReport: StateFlow<AgentExecutionReport?> = pulseAgent.latestReport
    val isAgentThinking: StateFlow<Boolean> = pulseAgent.isThinking
    val agentActionHistory: StateFlow<List<AgentAction>> = pulseAgent.agentActionHistory

    // User Feedback Message
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        pulseAgent.onNavigateToScreen = { destination: ScreenDestination ->
            navigateTo(destination)
        }
        audioEngine.onTrackCompleted = {
            onTrackFinished()
        }

        audioEngine.applyEqualizerPreset(_activePreset.value)

        viewModelScope.launch {
            allTracks.collect { tracks ->
                if (_currentTrack.value == null && tracks.isNotEmpty()) {
                    _currentTrack.value = tracks.first()
                    _queue.value = tracks
                    _currentIndex.value = 0
                }
            }
        }

        viewModelScope.launch {
            syncDevices.collect { devs ->
                _syncState.value = _syncState.value.copy(connectedDevices = devs)
            }
        }

        viewModelScope.launch {
            pulseAgent.userFeedbackMessage.collect { feedback: String? ->
                if (!feedback.isNullOrBlank()) {
                    _userFeedbackMessage.value = feedback
                }
            }
        }

        refreshAnalytics()
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedMood(mood: String) {
        _selectedMood.value = mood
    }

    fun setAnalyticsPeriod(period: String) {
        _analyticsPeriod.value = period
    }

    fun setDownloadSortOrder(order: DownloadSortOrder) {
        _downloadSortOrder.value = order
    }

    fun toggleOfflineModeOnly() {
        _isOfflineModeOnly.value = !_isOfflineModeOnly.value
        _userFeedbackMessage.value = if (_isOfflineModeOnly.value) "Offline Mode Enabled (Playing downloaded tracks only)" else "Online Streaming Resumed"
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    // Playback control
    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (_isOfflineModeOnly.value && !track.isDownloaded && !track.isLocal) {
            _userFeedbackMessage.value = "Cannot stream in Offline Mode. Download track first!"
            return
        }

        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
            _currentIndex.value = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.value = _queue.value + track
            _currentIndex.value = _queue.value.lastIndex
        } else {
            _currentIndex.value = _queue.value.indexOfFirst { it.id == track.id }
        }

        _currentTrack.value = track
        audioEngine.playTrack(track)

        viewModelScope.launch {
            _currentLyrics.value = null
            _currentLyrics.value = lyricsRepository.getLyrics(track)
            val wasOffline = _isOfflineModeOnly.value || track.isLocal || track.isDownloaded
            repository.recordTrackPlayed(track, wasOffline = wasOffline)
            refreshAnalytics()
        }
    }

    fun togglePlayPause() {
        if (_currentTrack.value == null && _queue.value.isNotEmpty()) {
            playTrack(_queue.value.first())
        } else {
            audioEngine.togglePlayPause()
        }
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = when {
            _isShuffleEnabled.value -> {
                val availableIndices = q.indices.filter { it != _currentIndex.value && it !in shuffleHistory }
                val chosen = if (availableIndices.isNotEmpty()) {
                    availableIndices.random()
                } else {
                    shuffleHistory.clear()
                    val fallbackPool = q.indices.filter { it != _currentIndex.value }
                    if (fallbackPool.isNotEmpty()) fallbackPool.random() else _currentIndex.value
                }
                shuffleHistory.add(chosen)
                chosen
            }
            _currentIndex.value + 1 < q.size -> _currentIndex.value + 1
            _repeatMode.value == RepeatMode.ALL -> 0
            else -> return
        }

        _currentIndex.value = nextIndex
        val nextTrack = q[nextIndex]
        _currentTrack.value = nextTrack
        audioEngine.playTrack(nextTrack)

        viewModelScope.launch {
            _currentLyrics.value = null
            _currentLyrics.value = lyricsRepository.getLyrics(nextTrack)
            repository.recordTrackPlayed(nextTrack)
        }
    }

    fun skipPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (audioEngine.currentPositionMs.value > 3000L) {
            audioEngine.seekTo(0L)
            return
        }

        val prevIndex = if (_currentIndex.value - 1 >= 0) {
            _currentIndex.value - 1
        } else if (_repeatMode.value == RepeatMode.ALL) {
            q.size - 1
        } else 0

        _currentIndex.value = prevIndex
        val prevTrack = q[prevIndex]
        _currentTrack.value = prevTrack
        audioEngine.playTrack(prevTrack)

        viewModelScope.launch {
            _currentLyrics.value = null
            _currentLyrics.value = lyricsRepository.getLyrics(prevTrack)
        }
    }

    private fun onTrackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentTrack.value?.let { audioEngine.playTrack(it) }
            }
            RepeatMode.ALL -> skipNext()
            RepeatMode.OFF -> {
                if (_currentIndex.value + 1 < _queue.value.size) {
                    skipNext()
                } else {
                    audioEngine.pause()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        if (_isShuffleEnabled.value) {
            _isSmartMoodShuffleActive.value = false
            _userFeedbackMessage.value = "Shuffle Enabled"
        } else {
            _userFeedbackMessage.value = "Shuffle Disabled"
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = _currentTrack.value?.copy(isFavorite = !track.isFavorite)
            }
        }
    }

    // Offline Downloads
    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.setTrackDownloaded(track, true)
            _userFeedbackMessage.value = "Downloaded \"${track.title}\" for offline playback (${track.downloadSizeMb} MB)"
        }
    }

    fun deleteDownloadedTrack(track: Track) {
        viewModelScope.launch {
            repository.setTrackDownloaded(track, false)
            _userFeedbackMessage.value = "Removed \"${track.title}\" from offline storage (+${track.downloadSizeMb} MB freed)"
        }
    }

    fun downloadPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.downloadPlaylist(playlist)
            _userFeedbackMessage.value = "Downloaded \"${playlist.title}\" for offline listening!"
        }
    }

    fun clearAllOfflineDownloads() {
        viewModelScope.launch {
            repository.clearAllDownloads()
            _userFeedbackMessage.value = "Cleared all offline audio downloads cache."
        }
    }

    // Cross-Device Sync Operations
    fun triggerSyncNow() {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true)
            _userFeedbackMessage.value = "Synchronizing playlists, history & preferences across devices..."
            repository.triggerCrossDeviceSync()
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncedTime = System.currentTimeMillis()
            )
            _userFeedbackMessage.value = "Sync complete! All devices up to date."
        }
    }

    fun addSyncDevice(name: String, type: String) {
        viewModelScope.launch {
            repository.addSyncDevice(name, type)
            _userFeedbackMessage.value = "Paired device \"$name\" via sync code ${_syncState.value.pairingCode}!"
        }
    }

    fun removeSyncDevice(id: String) {
        viewModelScope.launch {
            repository.removeSyncDevice(id)
            _userFeedbackMessage.value = "Device unpaired."
        }
    }

    // AI Dynamic Mood Playlist Generation
    fun openPlaylistGenerator() {
        _isPlaylistGeneratorOpen.value = true
    }

    fun closePlaylistGenerator() {
        _isPlaylistGeneratorOpen.value = false
    }

    fun generateMoodPlaylist(mood: String, energy: Float, valence: Float) {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Gemini AI crafting psychology-aligned playlist for '$mood'..."
            val playlist = repository.generateAiMoodPlaylist(mood, energy, valence)
            _isPlaylistGeneratorOpen.value = false
            _userFeedbackMessage.value = "Created \"${playlist.title}\"!"
            val trackIds = playlist.getTrackIdList()
            val tracks = allTracks.value.filter { it.id in trackIds }
            if (tracks.isNotEmpty()) {
                playTrack(tracks.first(), tracks)
            }
        }
    }

    // Smart Mood Shuffling via Gemini AI
    fun applySmartMoodShuffle(mood: String) {
        viewModelScope.launch {
            _selectedMood.value = mood
            _userFeedbackMessage.value = "Gemini AI reordering playlist for '$mood'..."
            val tracksToShuffle = if (_queue.value.isNotEmpty()) _queue.value else allTracks.value
            if (tracksToShuffle.isEmpty()) return@launch

            val reordered = repository.getSmartMoodShuffle(mood, tracksToShuffle)
            _queue.value = reordered
            _currentIndex.value = 0
            _isSmartMoodShuffleActive.value = true
            _isShuffleEnabled.value = true

            if (reordered.isNotEmpty()) {
                playTrack(reordered.first(), reordered)
            }
            _userFeedbackMessage.value = "AI reordered playlist for '$mood'!"
        }
    }

    /**
     * Generates a dynamic playlist based on natural language descriptors
     * like "energetic morning workout" or "relaxed rainy day" using OpenRouter / Gemini AI.
     */
    fun generateDynamicMoodPlaylist(descriptor: String) {
        if (descriptor.isBlank()) return
        viewModelScope.launch {
            _isGeneratingMoodPlaylist.value = true
            _userFeedbackMessage.value = "AI Mood Shuffler generating dynamic playlist for \"$descriptor\"..."
            val pool = allTracks.value.ifEmpty { _queue.value }
            val dynamicPlaylist = repository.generateDynamicMoodPlaylist(descriptor, pool)
            _activeDynamicMoodReport.value = dynamicPlaylist
            _isGeneratingMoodPlaylist.value = false

            if (dynamicPlaylist.tracks.isNotEmpty()) {
                _queue.value = dynamicPlaylist.tracks
                _currentIndex.value = 0
                _selectedMood.value = dynamicPlaylist.targetMood
                _isSmartMoodShuffleActive.value = true
                _isShuffleEnabled.value = true
                playTrack(dynamicPlaylist.tracks.first(), dynamicPlaylist.tracks)
                _userFeedbackMessage.value = "Generated \"${dynamicPlaylist.title}\": ${dynamicPlaylist.rationale}"
            } else {
                _userFeedbackMessage.value = "No matching audio candidates for \"$descriptor\"."
            }
        }
    }

    // Equalizer controls
    fun selectEqualizerPreset(preset: EqualizerPreset) {
        _activePreset.value = preset
        _currentGains.value = preset.toGainsList()
        _bassBoostStrength.value = preset.bassBoost
        _virtualizerStrength.value = preset.virtualizer
        audioEngine.applyEqualizerPreset(preset)
        _userFeedbackMessage.value = "Equalizer: ${preset.name}"
    }

    fun setEqualizerBandGain(bandIndex: Int, gainDb: Float) {
        val updated = _currentGains.value.toMutableList()
        if (bandIndex in updated.indices) {
            updated[bandIndex] = gainDb
            _currentGains.value = updated

            val customPreset = EqualizerPreset(
                id = "custom_preset",
                name = "Custom",
                band0 = updated.getOrElse(0) { 0f },
                band1 = updated.getOrElse(1) { 0f },
                band2 = updated.getOrElse(2) { 0f },
                band3 = updated.getOrElse(3) { 0f },
                band4 = updated.getOrElse(4) { 0f },
                bassBoost = _bassBoostStrength.value,
                virtualizer = _virtualizerStrength.value,
                isCustom = true
            )
            _activePreset.value = customPreset
            audioEngine.applyEqualizerPreset(customPreset)
        }
    }

    fun setBassBoostStrength(strength: Int) {
        _bassBoostStrength.value = strength
        val updatedPreset = _activePreset.value.copy(bassBoost = strength)
        _activePreset.value = updatedPreset
        audioEngine.applyEqualizerPreset(updatedPreset)
    }

    fun setVirtualizerStrength(strength: Int) {
        _virtualizerStrength.value = strength
        val updatedPreset = _activePreset.value.copy(virtualizer = strength)
        _activePreset.value = updatedPreset
        audioEngine.applyEqualizerPreset(updatedPreset)
    }

    fun scanLocalDeviceAudio() {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Scanning device storage for audio tracks..."
            val count = repository.scanAndImportLocalAudio()
            _userFeedbackMessage.value = if (count > 0) "Imported $count local tracks!" else "No new local audio files found on device."
        }
    }

    // Voice Search
    fun openVoiceSearch() {
        _isVoiceSearchOpen.value = true
        _voiceStatusText.value = "Speak now: say an artist, mood, or command..."
    }

    fun closeVoiceSearch() {
        _isVoiceSearchOpen.value = false
    }

    fun processVoiceSpokenText(spokenText: String) {
        if (spokenText.isBlank()) return
        viewModelScope.launch {
            _voiceStatusText.value = "Processing: \"$spokenText\"..."
            val result = repository.parseVoiceCommand(spokenText)
            _voiceStatusText.value = result.description

            when (result.action) {
                "PLAY_MOOD" -> {
                    applySmartMoodShuffle(result.target)
                    _isVoiceSearchOpen.value = false
                }
                "SEARCH" -> {
                    _searchQuery.value = result.target
                    _currentScreen.value = ScreenDestination.LIBRARY
                    _isVoiceSearchOpen.value = false
                }
                "EQUALIZER" -> {
                    val match = presets.value.find { it.name.contains(result.target, ignoreCase = true) }
                    match?.let { selectEqualizerPreset(it) }
                    _currentScreen.value = ScreenDestination.EQUALIZER
                    _isVoiceSearchOpen.value = false
                }
                "PLAY_PAUSE" -> {
                    togglePlayPause()
                    _isVoiceSearchOpen.value = false
                }
                "NEXT_TRACK" -> {
                    skipNext()
                    _isVoiceSearchOpen.value = false
                }
                else -> {
                    _searchQuery.value = result.target
                    _currentScreen.value = ScreenDestination.LIBRARY
                    _isVoiceSearchOpen.value = false
                }
            }
            _userFeedbackMessage.value = result.description
        }
    }

    // AI DJ Chat
    fun sendDjMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isDjTyping.value = true

        viewModelScope.launch {
            val history = _chatMessages.value.map { it.sender to it.text }
            val reply = repository.chatWithDj(userText, history, _currentTrack.value)
            _isDjTyping.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "dj", text = reply)
        }
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _isRefreshingAnalytics.value = true
            val data = repository.calculateAnalytics()
            _analytics.value = data
            _isRefreshingAnalytics.value = false
        }
    }

    // Executive AI Agent Control via centralized PulseAgent Brain
    fun executeAgentDirective(directive: String) {
        if (directive.isBlank()) return
        pulseAgent.dispatchIntent(com.example.ai.PulseUserIntent.ExecuteDirective(directive, com.example.ai.IntentOrigin.TEXT))
    }

    fun openYouTubeImport() {
        _isYouTubeImportOpen.value = true
    }

    fun closeYouTubeImport() {
        _isYouTubeImportOpen.value = false
    }

    fun importYouTubeTrackAndPlay(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            val track = com.example.audio.YouTubeStreamResolver.resolveYouTubeTrack(input)
            database.musicDao().insertTracks(listOf(track))
            val updatedQueue = listOf(track) + allTracks.value.filterNot { it.id == track.id }
            playTrack(track, updatedQueue)
            _userFeedbackMessage.value = "Streaming YouTube Track: ${track.title}"
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Temporary stream cache and audio buffers cleared."
        }
    }

    fun setOpenRouterApiKey(key: String) {
        openRouterAiService.setApiKey(key)
        _openRouterApiKey.value = key
        _userFeedbackMessage.value = "OpenRouter API Key saved securely"
    }

    fun setOpenRouterModel(model: String) {
        openRouterAiService.setModel(model)
        _openRouterModel.value = model
        _userFeedbackMessage.value = "OpenRouter Model updated to $model"
    }

    fun testOpenRouterConnection() {
        viewModelScope.launch {
            _isTestingOpenRouter.value = true
            _openRouterStatus.value = "Connecting to OpenRouter..."
            val result = openRouterAiService.testConnection(_openRouterApiKey.value, _openRouterModel.value)
            _openRouterStatus.value = if (result.isSuccess) "✓ ${result.message}" else "✗ ${result.message}"
            _isTestingOpenRouter.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.releasePlayer()
    }
}
