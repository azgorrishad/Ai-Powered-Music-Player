package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.EqualizerPreset
import com.example.data.model.PlaybackHistoryItem
import com.example.data.model.Playlist
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.SyncDevice

@Database(
    entities = [
        Track::class,
        Playlist::class,
        PlaybackHistoryItem::class,
        EqualizerPreset::class,
        SyncDevice::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun audioMetadataDao(): AudioMetadataDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulseflow_music_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default showcase tracks, playlists, and EQ presets
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).seedInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val dao = musicDao()

        // Default studio equalizer presets
        dao.insertPresets(EqualizerPreset.DEFAULT_PRESETS)

        // Seeded showcase streaming tracks (reliable audio streams)
        val showcaseTracks = listOf(
            Track(
                id = "track_neon_horizons",
                title = "Neon Horizons",
                artist = "Aether Void",
                album = "Cyber Drive 2088",
                durationMs = 214000L,
                audioUrl = "https://raw.githubusercontent.com/rafaelreis-hotmart/Audio-Sample-files/master/sample.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop",
                genre = "Synthwave",
                mood = "Energetic",
                bpm = 128,
                valence = 0.85f,
                energy = 0.90f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_midnight_reverie",
                title = "Midnight Reverie",
                artist = "Komorebi Beats",
                album = "Rainy Rooftops",
                durationMs = 178000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop",
                genre = "Lo-Fi Hip-Hop",
                mood = "Chill",
                bpm = 82,
                valence = 0.65f,
                energy = 0.35f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_quantum_flux",
                title = "Quantum Flux",
                artist = "Neural Resonance",
                album = "Binaural Horizons",
                durationMs = 245000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=500&auto=format&fit=crop",
                genre = "Ambient Focus",
                mood = "Focus",
                bpm = 105,
                valence = 0.50f,
                energy = 0.40f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_solar_flare",
                title = "Solar Flare",
                artist = "Hyperion Core",
                album = "Bass Protocol",
                durationMs = 198000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop",
                genre = "Cyber Bass",
                mood = "Workout",
                bpm = 142,
                valence = 0.92f,
                energy = 0.95f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_starlight_sonata",
                title = "Starlight Sonata",
                artist = "Echoes of Orion",
                album = "Nocturne Solitude",
                durationMs = 226000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop",
                genre = "Neo-Classical",
                mood = "Melancholy",
                bpm = 74,
                valence = 0.25f,
                energy = 0.28f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_electric_sunset",
                title = "Electric Sunset",
                artist = "Prism Skyline",
                album = "Endless Golden Hour",
                durationMs = 205000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop",
                genre = "Indie Electronic",
                mood = "Euphoria",
                bpm = 122,
                valence = 0.88f,
                energy = 0.78f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            ),
            Track(
                id = "track_tokyo_rain",
                title = "Tokyo Rain",
                artist = "Shibuya Midnight",
                album = "Neon Reflections",
                durationMs = 189000L,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop",
                genre = "Lo-Fi Ambient",
                mood = "Night Drive",
                bpm = 88,
                valence = 0.60f,
                energy = 0.45f,
                isFavorite = false,
                playCount = 0,
                isLocal = false,
                isDownloaded = false
            )
        ) + com.example.audio.YouTubeStreamResolver.getCuratedYouTubeStreams()
        dao.insertTracks(showcaseTracks)

        // Seeded Curated Starter Playlists
        val playlists = listOf(
            Playlist(
                id = "pl_deep_focus",
                title = "Deep Flow State",
                description = "Binaural textures & 100-BPM pulses engineered for uninterrupted creative flow.",
                moodCategory = "Focus",
                coverUrl = "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=500&auto=format&fit=crop",
                trackIdsJson = "track_quantum_flux,track_midnight_reverie,track_tokyo_rain",
                isAiGenerated = true
            ),
            Playlist(
                id = "pl_neon_workout",
                title = "Cyber Pulse Workout",
                description = "High-octane electronic and synthwave rhythms designed to push peak endurance.",
                moodCategory = "Workout",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop",
                trackIdsJson = "track_solar_flare,track_neon_horizons,track_electric_sunset",
                isAiGenerated = true
            ),
            Playlist(
                id = "pl_night_drive",
                title = "Midnight Cruising",
                description = "Warm analog synthesizers and smooth basslines for empty highway night drives.",
                moodCategory = "Night Drive",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop",
                trackIdsJson = "track_tokyo_rain,track_neon_horizons,track_midnight_reverie",
                isAiGenerated = true
            ),
            Playlist(
                id = "pl_euphoria_rush",
                title = "Euphoric Vibrations",
                description = "Uplifting melodic harmonies that stimulate dopamine release and elevate spirits.",
                moodCategory = "Euphoria",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop",
                trackIdsJson = "track_electric_sunset,track_neon_horizons,track_solar_flare",
                isAiGenerated = true
            )
        )
        dao.insertPlaylists(playlists)

        // Note: We do NOT insert fake listening history on fresh installations.
        // Genuine playback history is recorded exclusively as the user actually listens.

        // Seed current local device identity in an un-synced initial state
        val initialDevice = SyncDevice(
            id = "dev_this_mobile",
            deviceName = "Auralis Device (This Mobile)",
            deviceType = "mobile",
            lastSyncTime = 0L,
            syncCode = "",
            isCurrentDevice = true,
            syncStatus = "Unpaired"
        )
        dao.insertSyncDevices(listOf(initialDevice))
    }
}
