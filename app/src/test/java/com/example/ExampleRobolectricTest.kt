package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EqualizerPreset
import com.example.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AURALIS", appName)
  }

  @Test
  fun `verify default equalizer presets`() {
    val presets = EqualizerPreset.DEFAULT_PRESETS
    assertTrue(presets.isNotEmpty())

    val bassBoost = presets.find { it.name == "Bass Boost" }
    assertTrue(bassBoost != null)
    assertEquals(5, bassBoost?.toGainsList()?.size)
    assertEquals("Bass & Club", bassBoost?.genre)
    assertTrue((bassBoost?.bassBoost ?: 0) >= 600)

    val acoustic = presets.find { it.name == "Acoustic" }
    assertTrue(acoustic != null)
    assertEquals("Acoustic & Folk", acoustic?.genre)
    assertEquals(5, acoustic?.toGainsList()?.size)

    val jazz = presets.find { it.name == "Jazz" }
    assertTrue(jazz != null)
    assertEquals("Jazz & Soul", jazz?.genre)
    assertEquals(5, jazz?.toGainsList()?.size)
    assertTrue((jazz?.virtualizer ?: 0) > 0)

    val edm = presets.find { it.name == "Cyber EDM" }
    assertTrue(edm != null)

    val rock = presets.find { it.name == "Rock Stage" }
    assertTrue(rock != null)
  }

  @Test
  fun `verify AudioEngine applies genre presets and updates media3 audio processor settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val audioEngine = com.example.audio.AudioEngine.getInstance(context)

    val jazzPreset = EqualizerPreset.DEFAULT_PRESETS.first { it.name == "Jazz" }
    audioEngine.applyEqualizerPreset(jazzPreset)

    assertEquals("Jazz", audioEngine.appliedPreset.value?.name)
    assertEquals("Jazz & Soul", audioEngine.appliedPreset.value?.genre)
    assertEquals(jazzPreset.bassBoost, audioEngine.appliedPreset.value?.bassBoost)
    assertEquals(jazzPreset.virtualizer, audioEngine.appliedPreset.value?.virtualizer)
  }

  @Test
  fun `track duration formatting`() {
    val track = Track(
      id = "test_track",
      title = "Test Song",
      artist = "Test Artist",
      durationMs = 185000L,
      audioUrl = "http://example.com/audio.mp3",
      artworkUrl = "http://example.com/art.jpg"
    )
    assertEquals("3:05", track.durationFormatted)
  }

  @Test
  fun `verify agent autonomous directives generation`() {
    val agent = com.example.ai.PulseAiAgent()
    val testTracks = listOf(
      Track(
        id = "t1",
        title = "Neon Beats",
        artist = "Synth Wave",
        durationMs = 200000L,
        audioUrl = "http://example.com/audio1.mp3",
        artworkUrl = "http://example.com/art1.jpg",
        bpm = 128,
        mood = "Workout"
      )
    )
    // Run blocking test on fallback reasoning
    val report = kotlinx.coroutines.runBlocking {
      agent.processUserDirective("pump up the bass and workout", null, testTracks, "Workout", false)
    }
    assertTrue(report.actions.isNotEmpty())
    assertTrue(report.actions.any { it.toolName == "SET_EQUALIZER" })
    assertTrue(report.agentSpeech.isNotBlank())
  }

  @Test
  fun `verify PulseAgent initialization and intent flow`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val pulseAgent = com.example.ai.PulseAgent.getInstance(context)

    // Verify initial state flows
    assertEquals(com.example.ai.CognitiveState.IDLE, pulseAgent.cognitiveState.value)
    assertEquals(false, pulseAgent.isThinking.value)
    assertEquals("gemini-4-argon", pulseAgent.brainTelemetry.value.activeModel)
    assertEquals("HIGH", pulseAgent.brainTelemetry.value.thinkingLevel)

    // Dispatch DSP tuning intent
    pulseAgent.dispatchIntent(
      com.example.ai.PulseUserIntent.TuneAudioDsp(
        presetName = "Bass Boost",
        bassBoostPercent = 80,
        virtualizerPercent = 40
      )
    )

    assertEquals(800, pulseAgent.bassBoostStrength.value)
    assertEquals(400, pulseAgent.virtualizerStrength.value)
    assertEquals(1, pulseAgent.intentHistory.value.size)
    assertEquals(com.example.ai.CognitiveState.HARMONIZED, pulseAgent.intentHistory.value.first().cognitivePhase)
  }

  @Test
  fun `verify refined agent intent directives for club EDM and vocal clarity`() {
    val agent = com.example.ai.PulseAiAgent()
    val reportEdm = kotlinx.coroutines.runBlocking {
      agent.processUserDirective("turn this into a club party with edm", null, emptyList(), "Energetic", false)
    }
    assertTrue(reportEdm.actions.any { it.toolName == "SET_EQUALIZER" && it.parameters["preset"] == "Cyber EDM" })
    assertTrue(reportEdm.actions.any { it.toolName == "SMART_SHUFFLE" })

    val reportVocal = kotlinx.coroutines.runBlocking {
      agent.processUserDirective("enhance the vocal and voice clarity", null, emptyList(), "Focus", false)
    }
    assertTrue(reportVocal.actions.any { it.toolName == "SET_EQUALIZER" && it.parameters["preset"] == "Vocal Clarity" })
  }

  @Test
  fun `verify Room local storage of audio metadata and offline queries`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val dao = db.audioMetadataDao()

    val testTrack = Track(
      id = "local_test_track_1",
      title = "Offline Cyber Resonance",
      artist = "Synthetica",
      album = "Offline EP",
      durationMs = 210000L,
      audioUrl = "http://example.com/audio.mp3",
      artworkUrl = "http://example.com/art.jpg",
      localFilePath = "/data/user/0/com.example/files/offline_track_1.mp3",
      bitrateKbps = 320,
      sampleRateHz = 48000,
      audioFormat = "audio/mpeg",
      isDownloaded = true
    )

    dao.insertTrack(testTrack)

    val fetched = dao.getTrackById("local_test_track_1")
    assertTrue(fetched != null)
    assertEquals("Offline Cyber Resonance", fetched?.title)
    assertEquals("/data/user/0/com.example/files/offline_track_1.mp3", fetched?.localFilePath)
    assertEquals("320 kbps • 48.0 kHz MPEG", fetched?.audioSpecsFormatted)
    assertTrue(fetched?.isAvailableOffline == true)
    assertEquals(fetched?.localFilePath, fetched?.effectiveAudioUri)
  }

  @Test
  fun `verify Room local storage of playlist definitions and offline availability`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val dao = db.playlistDao()

    val testPlaylist = com.example.data.model.Playlist(
      id = "pl_offline_test",
      title = "Airplane Focus List",
      description = "Saved entirely for offline airplane travel",
      moodCategory = "Focus",
      coverUrl = "http://example.com/cover.jpg",
      trackIdsJson = "track_1,track_2,track_3",
      isOfflineAvailable = true,
      totalTracksCount = 3,
      totalDurationMs = 600000L
    )

    dao.insertPlaylist(testPlaylist)

    val fetched = dao.getPlaylistById("pl_offline_test")
    assertTrue(fetched != null)
    assertEquals(3, fetched?.getTrackIdList()?.size)
    assertTrue(fetched?.isOfflineAvailable == true)
    assertEquals("10 min", fetched?.totalDurationFormatted)

    // Update track listing
    dao.updatePlaylistTracks("pl_offline_test", "track_1,track_2,track_3,track_4", 4, 840000L)
    val updated = dao.getPlaylistById("pl_offline_test")
    assertEquals(4, updated?.getTrackIdList()?.size)
    assertEquals(840000L, updated?.totalDurationMs)
  }

  @Test
  fun `verify Room local storage of playback history and offline tracking`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val dao = db.playbackHistoryDao()

    val historyItem = com.example.data.model.PlaybackHistoryItem(
      trackId = "track_offline_hist",
      trackTitle = "Midnight Drive",
      artist = "Vapor Drift",
      album = "Night City",
      artworkUrl = "http://example.com/art.jpg",
      listenedDurationMs = 195000L,
      totalDurationMs = 195000L,
      moodContext = "Night Drive",
      wasOfflinePlayback = true
    )

    dao.insertHistoryItem(historyItem)

    val allHistory = dao.getAllHistory()
    assertTrue(allHistory.any { it.trackId == "track_offline_hist" && it.wasOfflinePlayback })
  }

  @Test
  fun `verify VisualizerMode configurations and values`() {
    val modes = com.example.ui.components.VisualizerMode.entries
    assertEquals(4, modes.size)
    assertTrue(modes.contains(com.example.ui.components.VisualizerMode.SPECTRUM_BARS))
    assertTrue(modes.contains(com.example.ui.components.VisualizerMode.NEON_WAVE))
    assertTrue(modes.contains(com.example.ui.components.VisualizerMode.CIRCULAR_RADIAL))
    assertTrue(modes.contains(com.example.ui.components.VisualizerMode.ENERGY_PARTICLES))
  }

  @Test
  fun `verify AppTheme values and Analog Sound Lab quick presets`() {
    val themes = com.example.ui.theme.AppTheme.values()
    assertEquals(4, themes.size)
    assertTrue(themes.contains(com.example.ui.theme.AppTheme.OBSIDIAN))
    assertTrue(themes.contains(com.example.ui.theme.AppTheme.AURORA))
    assertTrue(themes.contains(com.example.ui.theme.AppTheme.TITANIUM))
    assertTrue(themes.contains(com.example.ui.theme.AppTheme.EMBER))

    val presets = EqualizerPreset.DEFAULT_PRESETS
    assertTrue(presets.any { it.name.contains("Flat", true) || it.name.contains("Studio", true) })
    assertTrue(presets.any { it.name.contains("Acoustic", true) })
    assertTrue(presets.any { it.name.contains("Bass", true) })
  }

  @Test
  fun `verify FiveBandEqualizerControl five common profiles`() {
    val presets = EqualizerPreset.DEFAULT_PRESETS

    val bassBoost = presets.find { it.id == "preset_bass_boost" || it.name == "Bass Boost" }
    assertTrue("Bass Boost preset must exist", bassBoost != null)
    assertTrue(bassBoost!!.band0 >= 8f)

    val vocalClarity = presets.find { it.id == "preset_vocal" || it.name == "Vocal Clarity" }
    assertTrue("Vocal Clarity preset must exist", vocalClarity != null)
    assertTrue(vocalClarity!!.band2 >= 5f) // Mid boost for speech intelligibility

    val rock = presets.find { it.id == "preset_rock" || it.name.contains("Rock", true) }
    assertTrue("Rock preset must exist", rock != null)
    assertTrue(rock!!.band0 > 0f && rock.band4 > 0f) // V-curve

    val jazz = presets.find { it.id == "preset_jazz" || it.name.contains("Jazz", true) }
    assertTrue("Jazz preset must exist", jazz != null)
    assertTrue(jazz!!.virtualizer > 0)

    val flat = presets.find { it.id == "preset_flat" || it.name.contains("Flat", true) }
    assertTrue("Flat preset must exist", flat != null)
    assertEquals(0f, flat!!.band0, 0.01f)
    assertEquals(0f, flat.band1, 0.01f)
    assertEquals(0f, flat.band2, 0.01f)
    assertEquals(0f, flat.band3, 0.01f)
    assertEquals(0f, flat.band4, 0.01f)
  }

  @Test
  fun `verify ImmersiveVisualizer modes and telemetry`() {
    val modes = com.example.ui.components.ImmersiveVisualizerMode.values()
    assertEquals(3, modes.size)
    assertTrue(modes.contains(com.example.ui.components.ImmersiveVisualizerMode.REACTIVE_WAVEFORM))
    assertTrue(modes.contains(com.example.ui.components.ImmersiveVisualizerMode.SPECTRUM_SPECTRUM))
    assertTrue(modes.contains(com.example.ui.components.ImmersiveVisualizerMode.CIRCULAR_ORBIT))
  }

  @Test
  fun `verify OpenRouter and Gemini natural language dynamic mood shuffler for energetic morning workout and relaxed rainy day`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val openRouterService = com.example.ai.OpenRouterAiService(context)
    val testTracks = listOf(
      Track(
        id = "t_run",
        title = "Morning Cardio Sprint",
        artist = "Electro Pulse",
        durationMs = 210000L,
        audioUrl = "http://example.com/run.mp3",
        artworkUrl = "http://example.com/run.jpg",
        bpm = 135,
        energy = 0.9f,
        mood = "Energetic"
      ),
      Track(
        id = "t_rain",
        title = "Raindrop Window Acoustic",
        artist = "Lo-Fi Coffee",
        durationMs = 195000L,
        audioUrl = "http://example.com/rain.mp3",
        artworkUrl = "http://example.com/rain.jpg",
        bpm = 78,
        energy = 0.35f,
        mood = "Chill"
      )
    )

    // 1. Test "energetic morning workout"
    val workoutResult = kotlinx.coroutines.runBlocking {
      openRouterService.generateDynamicMoodPlaylist("energetic morning workout", testTracks)
    }
    assertTrue("Workout title should reflect workout or morning", workoutResult.title.isNotBlank())
    assertEquals("Workout", workoutResult.targetMood)
    assertTrue("BPM range should include 125 and 142", workoutResult.targetBpmRange.contains("125") && workoutResult.targetBpmRange.contains("142"))
    assertTrue("Should select high tempo workout track", workoutResult.tracks.any { it.id == "t_run" })

    // 2. Test "relaxed rainy day"
    val rainyResult = kotlinx.coroutines.runBlocking {
      openRouterService.generateDynamicMoodPlaylist("relaxed rainy day", testTracks)
    }
    assertTrue("Rainy day title should reflect relaxed or rain", rainyResult.title.isNotBlank())
    assertEquals("Chill", rainyResult.targetMood)
    assertTrue("BPM range should include 74 and 88", rainyResult.targetBpmRange.contains("74") && rainyResult.targetBpmRange.contains("88"))
    assertTrue("Should select downtempo acoustic rain track", rainyResult.tracks.any { it.id == "t_rain" })
  }
}

