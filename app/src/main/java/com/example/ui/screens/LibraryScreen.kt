package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.AuralisEmptyState
import com.example.ui.components.AuralisFilterPill
import com.example.ui.components.AuralisSearchBar
import com.example.ui.components.AuralisTokens
import com.example.ui.components.AuralisTrackRow
import com.example.ui.theme.BrushedSilver
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily

enum class LibraryTab {
    ALL,
    LOCAL,
    STREAMING,
    FAVORITES,
    DOWNLOADS,
    PLAYLISTS
}

/**
 * Clean, organized personal music library and sound vault for AURALIS.
 */
@Composable
fun LibraryScreen(
    allTracks: List<Track>,
    filteredTracks: List<Track>,
    localTracks: List<Track>,
    favoriteTracks: List<Track>,
    downloadedTracks: List<Track>,
    playlists: List<Playlist>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTrackClicked: (Track) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onScanLocalAudio: () -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onOpenOfflineManager: () -> Unit,
    onOpenSyncHub: () -> Unit,
    onOpenPlaylistGenerator: () -> Unit,
    onOpenYouTubeImport: () -> Unit = {},
    selectedMood: String = "Focus",
    onMoodSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.ALL) }

    val displayedTracks = when (selectedTab) {
        LibraryTab.ALL -> filteredTracks
        LibraryTab.LOCAL -> localTracks.filter { it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
        LibraryTab.STREAMING -> allTracks.filter { !it.isLocal && (it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true)) }
        LibraryTab.FAVORITES -> favoriteTracks.filter { it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
        LibraryTab.DOWNLOADS -> downloadedTracks.filter { it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
        LibraryTab.PLAYLISTS -> emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidianNoir)
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Studio Header & Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(ImperialGold)
                            )
                            Text(
                                text = "AUDIO ARCHIVE",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = ImperialGold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sound Vault",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = PlatinumWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenYouTubeImport,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                                .testTag("library_youtube_import_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.YoutubeSearchedFor,
                                contentDescription = "Import YouTube Stream",
                                tint = NeonPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenPlaylistGenerator,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Playlist Studio",
                                tint = ImperialGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenOfflineManager,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Offline Vault",
                                tint = BrushedSilver,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unified Search Bar
                AuralisSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search titles, artists, albums...",
                    onOpenVoice = onOpenVoiceSearch,
                    modifier = Modifier.testTag("universal_search_bar")
                )
            }
        }

        // Category Filter Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val tabs = listOf(
                    LibraryTab.ALL to "All (${allTracks.size})",
                    LibraryTab.LOCAL to "Local (${localTracks.size})",
                    LibraryTab.STREAMING to "Streams (${allTracks.count { !it.isLocal }})",
                    LibraryTab.FAVORITES to "Favorites (${favoriteTracks.size})",
                    LibraryTab.DOWNLOADS to "Offline (${downloadedTracks.size})",
                    LibraryTab.PLAYLISTS to "Playlists (${playlists.size})"
                )

                items(tabs) { (tab, label) ->
                    val isSelected = selectedTab == tab
                    AuralisFilterPill(
                        label = label,
                        isSelected = isSelected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.testTag("library_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        // Device Storage Scan Action Card
        if (selectedTab == LibraryTab.LOCAL || selectedTab == LibraryTab.ALL) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(AuralisTokens.RadiusMedium))
                        .background(Color(0xFF0F1524))
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusMedium))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(ImperialGold)
                                )
                                Text(
                                    text = "DEVICE STORAGE",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = ImperialGold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Scan Device Audio Storage",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PlatinumWhite
                            )
                            Text(
                                text = "Index lossless FLAC, WAV, ALAC & MP3 files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }

                        Button(
                            onClick = onScanLocalAudio,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImperialGold,
                                contentColor = DeepObsidianNoir
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = DeepObsidianNoir,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Playlists tab view
        if (selectedTab == LibraryTab.PLAYLISTS) {
            if (playlists.isEmpty()) {
                item {
                    AuralisEmptyState(
                        title = "No Playlists Found",
                        description = "Create tailored mood playlists or use AI Playlist Studio to generate bespoke soundscapes.",
                        actionText = "Open Playlist Studio",
                        onAction = onOpenPlaylistGenerator
                    )
                }
            } else {
                items(playlists) { playlist ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(AuralisTokens.RadiusMedium))
                            .background(Color(0xFF0C111C))
                            .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusMedium))
                            .clickable { onPlaylistClicked(playlist) }
                            .padding(12.dp)
                            .testTag("playlist_row_${playlist.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = playlist.coverUrl,
                                    contentDescription = playlist.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.title,
                                    fontFamily = SpaceGroteskFamily,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PlatinumWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = playlist.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Track list view
            if (displayedTracks.isEmpty()) {
                item {
                    AuralisEmptyState(
                        title = if (searchQuery.isNotEmpty()) "No Matching Tracks" else "No Tracks in Category",
                        description = if (searchQuery.isNotEmpty()) "Try adjusting your search query or clear the filter." else "Scan local files or import online streams into your audio vault."
                    )
                }
            } else {
                items(displayedTracks) { track ->
                    AuralisTrackRow(
                        track = track,
                        isCurrent = false,
                        isPlaying = false,
                        onClick = { onTrackClicked(track) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onDownload = { onDownloadTrack(track) }
                    )
                }
            }
        }
    }
}
