package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_devices")
data class SyncDevice(
    @PrimaryKey val id: String,
    val deviceName: String,
    val deviceType: String, // "desktop", "mobile", "web"
    val lastSyncTime: Long = System.currentTimeMillis(),
    val syncCode: String = "PULSE-8924-SYNC",
    val isCurrentDevice: Boolean = false,
    val syncStatus: String = "Synced" // "Synced", "Syncing", "Offline"
)

data class CrossDeviceSyncState(
    val pairingCode: String = "PULSE-8924-SYNC",
    val isSyncing: Boolean = false,
    val lastSyncedTime: Long = System.currentTimeMillis() - 120000L,
    val connectedDevices: List<SyncDevice> = emptyList(),
    val autoSyncEnabled: Boolean = true,
    val syncPlaylists: Boolean = true,
    val syncHistory: Boolean = true,
    val syncEqualizer: Boolean = true
) {
    val lastSyncedFormatted: String
        get() {
            val diffMinutes = ((System.currentTimeMillis() - lastSyncedTime) / 60000L).coerceAtLeast(0)
            return if (diffMinutes < 1) "Just now" else "$diffMinutes mins ago"
        }
}
