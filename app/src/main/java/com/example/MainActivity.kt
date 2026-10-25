package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MusicPlayerViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AiPlaylistGeneratorDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.components.YouTubeImportDialog
import com.example.ui.screens.AiDjChatScreen
import com.example.ui.screens.AiMaestroScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CrossDeviceSyncScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OfflineManagerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MusicPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentAppTheme by viewModel.currentAppTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(appTheme = currentAppTheme) {
                PulseFlowApp(viewModel)
            }
        }
    }
}

@Composable
fun PulseFlowApp(viewModel: MusicPlayerViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progressMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val visualizerAmplitudes by viewModel.visualizerAmplitudes.collectAsStateWithLifecycle()

    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val isSmartMoodShuffle by viewModel.isSmartMoodShuffleActive.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val selectedMood by viewModel.selectedMood.collectAsStateWithLifecycle()

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val localTracks by viewModel.localTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val downloadedTracks by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val activePreset by viewModel.activePreset.collectAsStateWithLifecycle()
    val allPresets by viewModel.presets.collectAsStateWithLifecycle()
    val currentGains by viewModel.currentGains.collectAsStateWithLifecycle()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsStateWithLifecycle()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsStateWithLifecycle()

    val isOfflineOnly by viewModel.isOfflineModeOnly.collectAsStateWithLifecycle()
    val downloadSortOrder by viewModel.downloadSortOrder.collectAsStateWithLifecycle()

    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVoiceSearchOpen by viewModel.isVoiceSearchOpen.collectAsStateWithLifecycle()
    val voiceStatusText by viewModel.voiceStatusText.collectAsStateWithLifecycle()
    val isPlaylistGeneratorOpen by viewModel.isPlaylistGeneratorOpen.collectAsStateWithLifecycle()
    val isYouTubeImportOpen by viewModel.isYouTubeImportOpen.collectAsStateWithLifecycle()

    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val analyticsPeriod by viewModel.analyticsPeriod.collectAsStateWithLifecycle()
    val isRefreshingAnalytics by viewModel.isRefreshingAnalytics.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isDjTyping by viewModel.isDjTyping.collectAsStateWithLifecycle()

    val latestAgentReport by viewModel.latestAgentReport.collectAsStateWithLifecycle()
    val isAgentThinking by viewModel.isAgentThinking.collectAsStateWithLifecycle()
    val agentActionHistory by viewModel.agentActionHistory.collectAsStateWithLifecycle()
    val cognitiveState by viewModel.pulseAgent.cognitiveState.collectAsStateWithLifecycle()
    val brainTelemetry by viewModel.pulseAgent.brainTelemetry.collectAsStateWithLifecycle()
    val visualizerMode by viewModel.visualizerMode.collectAsStateWithLifecycle()
    val currentAppTheme by viewModel.currentAppTheme.collectAsStateWithLifecycle()
    val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
    val openRouterApiKey by viewModel.openRouterApiKey.collectAsStateWithLifecycle()
    val openRouterModel by viewModel.openRouterModel.collectAsStateWithLifecycle()
    val openRouterStatus by viewModel.openRouterStatus.collectAsStateWithLifecycle()
    val isTestingOpenRouter by viewModel.isTestingOpenRouter.collectAsStateWithLifecycle()
    val isGeneratingMoodPlaylist by viewModel.isGeneratingMoodPlaylist.collectAsStateWithLifecycle()
    val activeDynamicMoodReport by viewModel.activeDynamicMoodReport.collectAsStateWithLifecycle()

    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentScreen != ScreenDestination.HOME) {
        viewModel.navigateTo(ScreenDestination.HOME)
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessage()
        }
    }

    // Storage permission launcher for local audio scan
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanLocalDeviceAudio()
        }
    }

    val requestStoragePermissionAndScan: () -> Unit = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Mini Player above Navigation Bar
                AnimatedVisibility(
                    visible = currentTrack != null && !isNowPlayingExpanded,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    MiniPlayer(
                        track = currentTrack,
                        isPlaying = isPlaying,
                        progressMs = progressMs,
                        durationMs = durationMs,
                        visualizerAmplitudes = visualizerAmplitudes,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.skipNext() },
                        onClick = { viewModel.setNowPlayingExpanded(true) }
                    )
                }

                // Luxury Audiophile Navigation Bar
                NavigationBar(
                    containerColor = Color(0xF7070A12),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(
                            width = 0.8.dp,
                            color = com.example.ui.theme.DarkSurfaceBorder,
                            shape = androidx.compose.ui.graphics.RectangleShape
                        )
                        .testTag("main_navigation_bar")
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.ImperialGold,
                        selectedTextColor = com.example.ui.theme.PlatinumWhite,
                        unselectedIconColor = com.example.ui.theme.DarkCharcoal,
                        unselectedTextColor = com.example.ui.theme.DarkCharcoal,
                        indicatorColor = com.example.ui.theme.ImperialGold.copy(alpha = 0.16f)
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.HOME,
                        onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = if (currentScreen == ScreenDestination.HOME) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.LIBRARY,
                        onClick = { viewModel.navigateTo(ScreenDestination.LIBRARY) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library", fontWeight = if (currentScreen == ScreenDestination.LIBRARY) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_library")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.EQUALIZER,
                        onClick = { viewModel.navigateTo(ScreenDestination.EQUALIZER) },
                        icon = { Icon(Icons.Default.Equalizer, contentDescription = "Equalizer") },
                        label = { Text("EQ Lab", fontWeight = if (currentScreen == ScreenDestination.EQUALIZER) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_equalizer")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.ANALYTICS,
                        onClick = { viewModel.navigateTo(ScreenDestination.ANALYTICS) },
                        icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                        label = { Text("Insights", fontWeight = if (currentScreen == ScreenDestination.ANALYTICS) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_analytics")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.AI_MAESTRO,
                        onClick = { viewModel.navigateTo(ScreenDestination.AI_MAESTRO) },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Maestro") },
                        label = { Text("Maestro", fontWeight = if (currentScreen == ScreenDestination.AI_MAESTRO) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_ai_maestro")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.SETTINGS,
                        onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontWeight = if (currentScreen == ScreenDestination.SETTINGS) FontWeight.Bold else FontWeight.Medium, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenDestination.HOME -> {
                    HomeScreen(
                        tracks = allTracks,
                        playlists = playlists,
                        analytics = analytics,
                        selectedMood = selectedMood,
                        onMoodSelected = { viewModel.setSelectedMood(it) },
                        onSmartShuffleClicked = { viewModel.applySmartMoodShuffle(it) },
                        onTrackClicked = { viewModel.playTrack(it, allTracks) },
                        onPlaylistClicked = { pl ->
                            val plTrackIds = pl.getTrackIdList()
                            val plTracks = allTracks.filter { it.id in plTrackIds }
                            if (plTracks.isNotEmpty()) {
                                viewModel.playTrack(plTracks.first(), plTracks)
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onOpenVoiceSearch = { viewModel.openVoiceSearch() },
                        onOpenAiDj = { viewModel.navigateTo(ScreenDestination.AI_DJ) },
                        onOpenAiMaestro = { viewModel.navigateTo(ScreenDestination.AI_MAESTRO) },
                        onOpenOfflineManager = { viewModel.navigateTo(ScreenDestination.OFFLINE_MANAGER) },
                        onOpenSyncHub = { viewModel.navigateTo(ScreenDestination.SYNC_HUB) },
                        onOpenPlaylistGenerator = { viewModel.openPlaylistGenerator() },
                        onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                        isGeneratingMoodPlaylist = isGeneratingMoodPlaylist,
                        activeDynamicMoodReport = activeDynamicMoodReport,
                        onGenerateDynamicMoodPlaylist = { viewModel.generateDynamicMoodPlaylist(it) }
                    )
                }
                ScreenDestination.LIBRARY -> {
                    LibraryScreen(
                        allTracks = allTracks,
                        filteredTracks = filteredTracks,
                        localTracks = localTracks,
                        favoriteTracks = favoriteTracks,
                        downloadedTracks = downloadedTracks,
                        playlists = playlists,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onTrackClicked = { viewModel.playTrack(it, filteredTracks) },
                        onPlaylistClicked = { pl ->
                            val plTrackIds = pl.getTrackIdList()
                            val plTracks = allTracks.filter { it.id in plTrackIds }
                            if (plTracks.isNotEmpty()) {
                                viewModel.playTrack(plTracks.first(), plTracks)
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onScanLocalAudio = requestStoragePermissionAndScan,
                        onOpenVoiceSearch = { viewModel.openVoiceSearch() },
                        onOpenOfflineManager = { viewModel.navigateTo(ScreenDestination.OFFLINE_MANAGER) },
                        onOpenSyncHub = { viewModel.navigateTo(ScreenDestination.SYNC_HUB) },
                        onOpenPlaylistGenerator = { viewModel.openPlaylistGenerator() },
                        onOpenYouTubeImport = { viewModel.openYouTubeImport() },
                        selectedMood = selectedMood,
                        onMoodSelected = { viewModel.applySmartMoodShuffle(it) }
                    )
                }
                ScreenDestination.EQUALIZER -> {
                    EqualizerScreen(
                        activePreset = activePreset,
                        allPresets = allPresets,
                        currentGains = currentGains,
                        bassBoostStrength = bassBoostStrength,
                        virtualizerStrength = virtualizerStrength,
                        onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                        onBandGainChange = { index, gainDb -> viewModel.setEqualizerBandGain(index, gainDb) },
                        onBassBoostChange = { viewModel.setBassBoostStrength(it) },
                        onVirtualizerChange = { viewModel.setVirtualizerStrength(it) }
                    )
                }
                ScreenDestination.ANALYTICS -> {
                    AnalyticsScreen(
                        analytics = analytics,
                        analyticsPeriod = analyticsPeriod,
                        onPeriodSelected = { viewModel.setAnalyticsPeriod(it) },
                        isRefreshing = isRefreshingAnalytics,
                        onRefresh = { viewModel.refreshAnalytics() },
                        onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                    )
                }
                ScreenDestination.SETTINGS -> {
                    SettingsScreen(
                        activePreset = activePreset,
                        isOfflineOnly = isOfflineOnly,
                        visualizerMode = visualizerMode,
                        currentTheme = currentAppTheme,
                        onThemeSelected = { viewModel.setAppTheme(it) },
                        onVisualizerModeChange = { viewModel.setVisualizerMode(it) },
                        onToggleOfflineOnly = { viewModel.toggleOfflineModeOnly() },
                        onOpenInsights = { viewModel.navigateTo(ScreenDestination.ANALYTICS) },
                        onOpenEqualizer = { viewModel.navigateTo(ScreenDestination.EQUALIZER) },
                        onOpenAiMaestro = { viewModel.navigateTo(ScreenDestination.AI_MAESTRO) },
                        onOpenAiDj = { viewModel.navigateTo(ScreenDestination.AI_DJ) },
                        onOpenSyncHub = { viewModel.navigateTo(ScreenDestination.SYNC_HUB) },
                        onOpenYouTubeImport = { viewModel.openYouTubeImport() },
                        onClearCache = { viewModel.clearCache() },
                        openRouterApiKey = openRouterApiKey,
                        openRouterModel = openRouterModel,
                        openRouterStatus = openRouterStatus,
                        isTestingOpenRouter = isTestingOpenRouter,
                        onSaveOpenRouterKey = { viewModel.setOpenRouterApiKey(it) },
                        onSelectOpenRouterModel = { viewModel.setOpenRouterModel(it) },
                        onTestOpenRouterConnection = { viewModel.testOpenRouterConnection() }
                    )
                }
                ScreenDestination.AI_DJ -> {
                    AiDjChatScreen(
                        messages = chatMessages,
                        isDjTyping = isDjTyping,
                        currentTrack = currentTrack,
                        onSendMessage = { viewModel.sendDjMessage(it) },
                        onPlayMoodRequested = { viewModel.applySmartMoodShuffle(it) }
                    )
                }
                ScreenDestination.AI_MAESTRO -> {
                    AiMaestroScreen(
                        currentTrack = currentTrack,
                        latestReport = latestAgentReport,
                        isThinking = isAgentThinking,
                        actionHistory = agentActionHistory,
                        onExecuteDirective = { viewModel.executeAgentDirective(it) },
                        onOpenVoiceSearch = { viewModel.openVoiceSearch() },
                        cognitiveState = cognitiveState,
                        brainTelemetry = brainTelemetry
                    )
                }
                ScreenDestination.OFFLINE_MANAGER -> {
                    OfflineManagerScreen(
                        downloadedTracks = downloadedTracks,
                        playlists = playlists,
                        isOfflineOnly = isOfflineOnly,
                        sortOrder = downloadSortOrder,
                        onToggleOfflineOnly = { viewModel.toggleOfflineModeOnly() },
                        onSortOrderChanged = { viewModel.setDownloadSortOrder(it) },
                        onTrackClicked = { viewModel.playTrack(it, downloadedTracks) },
                        onDeleteDownloadedTrack = { viewModel.deleteDownloadedTrack(it) },
                        onClearAllDownloads = { viewModel.clearAllOfflineDownloads() }
                    )
                }
                ScreenDestination.SYNC_HUB -> {
                    CrossDeviceSyncScreen(
                        syncState = syncState,
                        onTriggerSyncNow = { viewModel.triggerSyncNow() },
                        onAddDevice = { name, type -> viewModel.addSyncDevice(name, type) },
                        onRemoveDevice = { id -> viewModel.removeSyncDevice(id) }
                    )
                }
            }

            // Full Screen Now Playing Sheet Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingSheet(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    visualizerAmplitudes = visualizerAmplitudes,
                    isShuffleEnabled = isShuffleEnabled,
                    isSmartMoodShuffle = isSmartMoodShuffle,
                    repeatMode = repeatMode,
                    activePreset = activePreset,
                    allPresets = allPresets,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrevious = { viewModel.skipPrevious() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.toggleRepeat() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onSelectPreset = { viewModel.selectEqualizerPreset(it) },
                    onOpenEqualizer = {
                        viewModel.setNowPlayingExpanded(false)
                        viewModel.navigateTo(ScreenDestination.EQUALIZER)
                    },
                    onCollapse = { viewModel.setNowPlayingExpanded(false) },
                    visualizerMode = visualizerMode,
                    onVisualizerModeChange = { viewModel.setVisualizerMode(it) },
                    selectedMood = selectedMood,
                    onMoodSelected = { viewModel.applySmartMoodShuffle(it) },
                    lyrics = currentLyrics
                )
            }

            // Voice Command / Search Dialog
            VoiceSearchDialog(
                isOpen = isVoiceSearchOpen,
                statusText = voiceStatusText,
                onDismiss = { viewModel.closeVoiceSearch() },
                onSubmitVoiceQuery = { viewModel.processVoiceSpokenText(it) }
            )

            // AI Dynamic Playlist Generator Dialog
            AiPlaylistGeneratorDialog(
                isOpen = isPlaylistGeneratorOpen,
                onDismiss = { viewModel.closePlaylistGenerator() },
                onGenerate = { mood, energy, valence ->
                    viewModel.generateMoodPlaylist(mood, energy, valence)
                }
            )

            // YouTube Stream Import Dialog
            YouTubeImportDialog(
                isOpen = isYouTubeImportOpen,
                onDismiss = { viewModel.closeYouTubeImport() },
                onImportAndPlay = { input ->
                    viewModel.importYouTubeTrackAndPlay(input)
                }
            )
        }
    }
}
