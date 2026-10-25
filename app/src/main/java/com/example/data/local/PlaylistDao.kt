package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Playlist
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local playlist definitions, track listings, and offline playlist caching.
 */
@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isOfflineAvailable = 1 ORDER BY createdAt DESC")
    fun getOfflinePlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE moodCategory = :mood ORDER BY createdAt DESC")
    fun getPlaylistsByMood(mood: String): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<Playlist>)

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

    @Query("UPDATE playlists SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPlaylistPinned(id: String, isPinned: Boolean)

    @Query("UPDATE playlists SET localCoverPath = :localCoverPath WHERE id = :id")
    suspend fun updateLocalCover(id: String, localCoverPath: String?)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)
}
