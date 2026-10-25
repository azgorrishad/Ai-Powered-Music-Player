package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local audio metadata and offline file management.
 */
@Dao
interface AudioMetadataDao {

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
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, downloadDate = :downloadDate, localFilePath = :localPath, fileSizeBytes = :fileSizeBytes WHERE id = :id")
    suspend fun setDownloaded(
        id: String,
        isDownloaded: Boolean,
        downloadDate: Long = System.currentTimeMillis(),
        localPath: String? = null,
        fileSizeBytes: Long = (8.5 * 1024 * 1024).toLong()
    )

    @Query("UPDATE tracks SET localFilePath = :localFilePath, localArtworkPath = :localArtworkPath WHERE id = :id")
    suspend fun updateLocalFilePaths(id: String, localFilePath: String?, localArtworkPath: String?)

    @Query("UPDATE tracks SET bitrateKbps = :bitrate, sampleRateHz = :sampleRate, audioFormat = :format WHERE id = :id")
    suspend fun updateAudioSpecs(id: String, bitrate: Int, sampleRate: Int, format: String)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordTrackPlayed(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isDownloaded = 0, localFilePath = NULL WHERE isDownloaded = 1")
    suspend fun clearAllDownloadedTracks()

    @Query("SELECT COUNT(*) FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracksCount(): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM tracks WHERE isDownloaded = 1")
    suspend fun getTotalDownloadedStorageBytes(): Long?

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
