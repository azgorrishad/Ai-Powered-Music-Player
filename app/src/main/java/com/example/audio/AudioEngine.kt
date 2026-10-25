package com.example.audio

import android.content.ContentUris
import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.EqualizerPreset
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

@OptIn(UnstableApi::class)
class AudioEngine(private val context: Context) {

    private val tag = "PulseFlowAudioEngine"
    private var exoPlayer: ExoPlayer? = null
    private var currentTrack: Track? = null

    // Audio effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    // Coroutine scope for progress ticker & visualizer
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _visualizerAmplitudes = MutableStateFlow(List(24) { 0.15f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private val _appliedPreset = MutableStateFlow<EqualizerPreset?>(null)
    val appliedPreset: StateFlow<EqualizerPreset?> = _appliedPreset.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onTrackCompleted: (() -> Unit)? = null

    init {
        initializeExoPlayer()
        startWaveformAnimation()
    }

    private fun initializeExoPlayer() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setAudioAttributes(audioAttributes, true)
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _isBuffering.value = true
                            }
                            Player.STATE_READY -> {
                                _isBuffering.value = false
                                if (duration > 0) {
                                    _durationMs.value = duration
                                }
                                attachAudioEffects(audioSessionId)
                            }
                            Player.STATE_ENDED -> {
                                _isPlaying.value = false
                                _currentPositionMs.value = 0L
                                onTrackCompleted?.invoke()
                            }
                            Player.STATE_IDLE -> {
                                _isBuffering.value = false
                            }
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startProgressTracker()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(tag, "ExoPlayer error: ${error.errorCodeName} - ${error.message}")
                        _isBuffering.value = false
                        _isPlaying.value = false
                        _errorMessage.value = "Playback error: ${error.localizedMessage ?: "Audio format unsupported"}"
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Media3 ExoPlayer", e)
        }
    }

    fun playTrack(track: Track, startPositionMs: Long = 0L) {
        currentTrack = track
        _isBuffering.value = true
        _errorMessage.value = null

        val player = exoPlayer ?: run {
            initializeExoPlayer()
            exoPlayer
        }

        if (player == null) {
            _errorMessage.value = "Audio engine unavailable"
            return
        }

        try {
            val audioUriString = track.effectiveAudioUri
            val uri = if (audioUriString.startsWith("/")) {
                Uri.fromFile(java.io.File(audioUriString))
            } else {
                Uri.parse(audioUriString)
            }
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(Uri.parse(track.artworkUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            if (startPositionMs > 0) {
                player.seekTo(startPositionMs)
            }
            player.play()
            _isPlaying.value = true
            _durationMs.value = track.durationMs
            startProgressTracker()
        } catch (e: Exception) {
            Log.e(tag, "Failed to load track ${track.title}", e)
            _isBuffering.value = false
            _isPlaying.value = false
            _errorMessage.value = "Failed to start playback: ${e.message}"
        }
    }

    fun pause() {
        exoPlayer?.pause()
        _isPlaying.value = false
    }

    fun resume() {
        exoPlayer?.let {
            it.play()
            _isPlaying.value = true
            startProgressTracker()
        } ?: run {
            currentTrack?.let { playTrack(it, _currentPositionMs.value) }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        exoPlayer?.seekTo(positionMs)
    }

    private fun attachAudioEffects(audioSessionId: Int) {
        try {
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != 0) {
                releaseAudioEffects()
                equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
                bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
                virtualizer = Virtualizer(0, audioSessionId).apply { enabled = true }
            }
        } catch (e: Exception) {
            Log.w(tag, "AudioEffect not supported on this device/emulator: ${e.message}")
        }
    }

    fun applyEqualizerPreset(preset: EqualizerPreset) {
        val eq = equalizer
        if (eq != null) {
            try {
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]
                val range = (maxLevel - minLevel).toFloat()

                val gains = preset.toGainsList()
                val numBands = eq.numberOfBands.toInt().coerceAtMost(gains.size)

                for (i in 0 until numBands) {
                    val gainDb = gains[i].coerceIn(-12f, 12f)
                    val normalized = (gainDb + 12f) / 24f
                    val targetLevel = (minLevel + normalized * range).toInt().toShort()
                    eq.setBandLevel(i.toShort(), targetLevel)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to apply EQ band level: ${e.message}")
            }
        }

        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(preset.bassBoost.coerceIn(0, 1000).toShort())
                }
            }
            virtualizer?.let {
                if (it.strengthSupported) {
                    it.setStrength(preset.virtualizer.coerceIn(0, 1000).toShort())
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to set Bass/Virtualizer effect: ${e.message}")
        }

        _appliedPreset.value = preset
        Log.i(tag, "Media3 Audio Processor updated: preset='${preset.name}', genre='${preset.genre}', bassBoost=${preset.bassBoost}, virtualizer=${preset.virtualizer}")
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                exoPlayer?.let {
                    _currentPositionMs.value = it.currentPosition.coerceAtLeast(0L)
                    if (it.duration > 0) {
                        _durationMs.value = it.duration
                    }
                }
                delay(300)
            }
        }
    }

    private fun startWaveformAnimation() {
        scope.launch {
            var step = 0f
            while (isActive) {
                if (_isPlaying.value) {
                    val energyFactor = currentTrack?.energy ?: 0.6f
                    val bpmFactor = (currentTrack?.bpm ?: 120) / 120f
                    val currentPreset = _appliedPreset.value
                    val gains = currentPreset?.toGainsList() ?: listOf(0f, 0f, 0f, 0f, 0f)

                    val bars = List(24) { index ->
                        // Map 24 bars to 5 frequency bands (0: Sub-bass, 1: Bass, 2: Mid, 3: High-mid, 4: Treble)
                        val bandIndex = (index * 5 / 24).coerceIn(0, 4)
                        val gainDb = gains[bandIndex]
                        val eqFactor = 1.0f + (gainDb / 24f) // +/- 0.5 variation based on EQ

                        val wave1 = abs(sin(step * 0.18f * bpmFactor + index * 0.45f))
                        val wave2 = abs(sin(step * 0.35f + index * 0.2f))
                        val height = ((wave1 * 0.6f + wave2 * 0.4f) * energyFactor * eqFactor).coerceIn(0.08f, 0.98f)
                        height
                    }
                    _visualizerAmplitudes.value = bars
                    step += 1f
                } else {
                    _visualizerAmplitudes.value = List(24) { 0.12f }
                }
                delay(60)
            }
        }
    }

    fun scanLocalAudio(): List<Track> {
        val tracks = mutableListOf<Track>()
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Local Music"
                    val duration = cursor.getLong(durationCol)
                    val sizeBytes = cursor.getLong(sizeCol)
                    val sizeMb = if (sizeBytes > 0) sizeBytes / (1024.0 * 1024.0) else 5.2
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    tracks.add(
                        Track(
                            id = "local_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = if (duration > 0) duration else 180000L,
                            audioUrl = contentUri,
                            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop",
                            genre = "Local Audio",
                            mood = "Chill",
                            bpm = 115,
                            valence = 0.6f,
                            energy = 0.5f,
                            isFavorite = false,
                            playCount = 0,
                            isLocal = true,
                            isDownloaded = true,
                            downloadSizeMb = String.format("%.1f", sizeMb).toDoubleOrNull() ?: 5.2,
                            downloadDate = System.currentTimeMillis(),
                            source = "local"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to scan local media", e)
        }
        return tracks
    }

    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            virtualizer?.release()
            virtualizer = null
        } catch (e: Exception) {
            Log.w(tag, "Error releasing audio effects", e)
        }
    }

    fun releasePlayer() {
        progressJob?.cancel()
        releaseAudioEffects()
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        @Volatile
        private var instance: AudioEngine? = null

        fun getInstance(context: Context): AudioEngine {
            return instance ?: synchronized(this) {
                instance ?: AudioEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

