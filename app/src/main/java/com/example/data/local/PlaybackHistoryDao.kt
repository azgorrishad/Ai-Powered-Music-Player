package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlaybackHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local playback history records and offline session analytics.
 */
@Dao
interface PlaybackHistoryDao {

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history WHERE wasOfflinePlayback = 1 ORDER BY timestamp DESC")
    fun getOfflinePlaybackHistory(): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history WHERE trackId = :trackId ORDER BY timestamp DESC")
    fun getHistoryForTrack(trackId: String): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history")
    suspend fun getAllHistory(): List<PlaybackHistoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: PlaybackHistoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<PlaybackHistoryItem>)

    @Query("DELETE FROM playback_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Long)

    @Query("DELETE FROM playback_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM playback_history")
    fun getHistoryCount(): Flow<Int>

    @Query("SELECT trackId FROM playback_history GROUP BY trackId ORDER BY COUNT(trackId) DESC LIMIT :limit")
    suspend fun getTopPlayedTrackIds(limit: Int): List<String>
}
