package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EqualizerPreset
import com.example.data.model.PlaybackHistoryItem
import com.example.data.model.Playlist
import com.example.data.model.SyncDevice
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Unified Master DAO for the PulseFlow music player.
 * Provides high-speed Room operations for audio metadata, playlist definitions,
 * playback history, audio DSP presets, and cross-device sync.
 */
@Dao
interface MusicDao {

    // ---------------------------------------------------------
    // 1. Audio Metadata & Local / Offline Storage
    // ---------------------------------------------------------
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isLocal = 1 ORDER BY title ASC")
    fun getLocalTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY downloadDate DESC")
    fun getDownloadedTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 OR isLocal = 1 OR localFilePath IS NOT NULL ORDER BY title ASC")
    fun getOfflinePlayableTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE mood = :mood ORDER BY playCount DESC")
    fun getTracksByMood(mood: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY playCount DESC")
    fun getTracksByGenre(genre: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): Track?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, downloadDate = :downloadDate WHERE id = :id")
    suspend fun setDownloaded(id: String, isDownloaded: Boolean, downloadDate: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, downloadDate = :downloadDate, localFilePath = :localPath, fileSizeBytes = :fileSizeBytes WHERE id = :id")
    suspend fun setDownloadedWithSpecs(
        id: String,
        isDownloaded: Boolean,
        downloadDate: Long = System.currentTimeMillis(),
        localPath: String? = null,
        fileSizeBytes: Long = (8.5 * 1024 * 1024).toLong()
    )

    @Query("UPDATE tracks SET localFilePath = :localFilePath, localArtworkPath = :localArtworkPath WHERE id = :id")
    suspend fun updateLocalFilePaths(id: String, localFilePath: String?, localArtworkPath: String?)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isDownloaded = 0, localFilePath = NULL WHERE isDownloaded = 1")
    suspend fun clearAllDownloadedTracks()

    @Query("SELECT COUNT(*) FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracksCount(): Flow<Int>

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    // ---------------------------------------------------------
    // 2. Local Playlist Definitions
    // ---------------------------------------------------------
    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isOfflineAvailable = 1 ORDER BY createdAt DESC")
    fun getOfflinePlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE moodCategory = :mood ORDER BY createdAt DESC")
    fun getPlaylistsByMood(mood: String): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<Playlist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("UPDATE playlists SET trackIdsJson = :trackIdsJson, totalTracksCount = :totalCount, totalDurationMs = :durationMs, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePlaylistTracks(
        id: String,
        trackIdsJson: String,
        totalCount: Int,
        durationMs: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE playlists SET isOfflineAvailable = :isOffline, offlineSyncTimestamp = :syncTime WHERE id = :id")
    suspend fun setPlaylistOfflineAvailable(
        id: String,
        isOffline: Boolean,
        syncTime: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    // ---------------------------------------------------------
    // 3. Playback History & Analytics
    // ---------------------------------------------------------
    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history WHERE wasOfflinePlayback = 1 ORDER BY timestamp DESC")
    fun getOfflinePlaybackHistory(): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history")
    suspend fun getAllHistory(): List<PlaybackHistoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: PlaybackHistoryItem)

    @Query("DELETE FROM playback_history")
    suspend fun clearAllHistory()

    // ---------------------------------------------------------
    // 4. Equalizer Presets
    // ---------------------------------------------------------
    @Query("SELECT * FROM equalizer_presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<EqualizerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<EqualizerPreset>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPreset)

    // ---------------------------------------------------------
    // 5. Cross-Device Sync Devices
    // ---------------------------------------------------------
    @Query("SELECT * FROM sync_devices ORDER BY lastSyncTime DESC")
    fun getAllSyncDevices(): Flow<List<SyncDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncDevices(devices: List<SyncDevice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncDevice(device: SyncDevice)

    @Query("DELETE FROM sync_devices WHERE id = :id")
    suspend fun deleteSyncDevice(id: String)

    @Query("UPDATE sync_devices SET lastSyncTime = :timestamp, syncStatus = 'Synced'")
    suspend fun markAllDevicesSynced(timestamp: Long = System.currentTimeMillis())
}
