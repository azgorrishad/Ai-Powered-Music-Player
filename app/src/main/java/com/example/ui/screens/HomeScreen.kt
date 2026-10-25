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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.DynamicMoodPlaylist
import com.example.data.model.PlaybackAnalytics
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.AuralisArtwork
import com.example.ui.components.AuralisFilterPill
import com.example.ui.components.AuralisSectionHeader
import com.example.ui.components.AuralisTokens
import com.example.ui.components.AuralisTrackRow
import com.example.ui.theme.BrushedSilver
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily

/**
 * Editorial-quality Music Home Screen for AURALIS.
 * Artwork-led visual hierarchy, clean typography, and natural AI mood strategy.
 */
@Composable
fun HomeScreen(
    tracks: List<Track>,
    playlists: List<Playlist>,
    analytics: PlaybackAnalytics,
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    onSmartShuffleClicked: (String) -> Unit,
    onTrackClicked: (Track) -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onOpenAiDj: () -> Unit,
    onOpenAiMaestro: () -> Unit,
    onOpenOfflineManager: () -> Unit,
    onOpenSyncHub: () -> Unit,
    onOpenPlaylistGenerator: () -> Unit,
    onOpenSettings: () -> Unit = {},
    isGeneratingMoodPlaylist: Boolean = false,
    activeDynamicMoodReport: DynamicMoodPlaylist? = null,
    onGenerateDynamicMoodPlaylist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val moods = listOf("Chill", "Focus", "Energetic", "Workout", "Night Drive", "Euphoria", "Melancholy")
    var customDescriptorInput by remember { mutableStateOf("") }

    val featuredTrack = tracks.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidianNoir)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Quiet editorial brand header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 18.dp, top = 20.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AURALIS",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = 2.8.sp,
                        color = PlatinumWhite
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "YOUR MUSIC. YOUR MOMENT.",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        color = MutedSlate
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onOpenVoiceSearch,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF17191B))
                            .border(1.dp, DarkSurfaceBorder, CircleShape)
                            .testTag("home_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = BrushedSilver,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF17191B))
                            .border(1.dp, DarkSurfaceBorder, CircleShape)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = BrushedSilver,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // 2. Featured track: Large, beautiful album artwork
        if (featuredTrack != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 4.dp)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(22.dp))
                        .clickable { onTrackClicked(featuredTrack) }
                        .testTag("featured_track_hero")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "A TRACK TO START WITH",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuralisArtwork(
                                model = featuredTrack.artworkUrl,
                                contentDescription = "Artwork for ${featuredTrack.title}",
                                size = 84.dp,
                                cornerRadius = 14.dp,
                                showHiResBadge = false
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = featuredTrack.title,
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.2).sp,
                                    color = PlatinumWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = featuredTrack.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ImperialGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play ${featuredTrack.title}",
                                            tint = DeepObsidianNoir,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PLAY TRACK",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = BrushedSilver
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Mood-Based Mix Generator (Quiet, intuitive, and natural)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                    .testTag("dynamic_mood_shuffler_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MOOD-BASED MIX",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Describe the moment. Find your sound.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrushedSilver
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customDescriptorInput,
                        onValueChange = { customDescriptorInput = it },
                        placeholder = {
                            Text(
                                text = "What do you want your music to feel like?",
                                fontSize = 12.sp,
                                color = DarkCharcoal
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_descriptor_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImperialGold,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = Color(0xFF090A0C),
                            unfocusedContainerColor = Color(0xFF090A0C),
                            focusedTextColor = PlatinumWhite,
                            unfocusedTextColor = PlatinumWhite
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (isGeneratingMoodPlaylist) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ImperialGold,
                                    strokeWidth = 2.dp
                                )
                            } else if (customDescriptorInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        onGenerateDynamicMoodPlaylist(customDescriptorInput)
                                    },
                                    modifier = Modifier.testTag("mood_descriptor_submit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Generate",
                                        tint = ImperialGold
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prompt Inspiration Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val prompts = listOf(
                            "Deep focus cafe",
                            "Late night drive",
                            "Energetic morning workout",
                            "Relaxed rainy day"
                        )

                        items(prompts) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF17191D))
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        customDescriptorInput = prompt
                                        onGenerateDynamicMoodPlaylist(prompt)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("quick_chip_${prompt.replace(" ", "_")}")
                            ) {
                                Text(
                                    text = prompt,
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = BrushedSilver
                                )
                            }
                        }
                    }

                    // Dynamic Result Card
                    if (activeDynamicMoodReport != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF090A0C))
                                .border(1.dp, ImperialGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("dynamic_mood_result_banner")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeDynamicMoodReport.title,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ImperialGold
                                    )
                                    Text(
                                        text = activeDynamicMoodReport.rationale,
                                        fontSize = 11.sp,
                                        color = MutedSlate,
                                        lineHeight = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = {
                                        if (activeDynamicMoodReport.tracks.isNotEmpty()) {
                                            onTrackClicked(activeDynamicMoodReport.tracks.first())
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ImperialGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("play_dynamic_report_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = DeepObsidianNoir,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PLAY",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DeepObsidianNoir
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Mood Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                AuralisSectionHeader(
                    overline = "MOOD SELECTION",
                    title = "Target Listening Mood"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(moods) { mood ->
                        val isSelected = selectedMood.equals(mood, ignoreCase = true)
                        AuralisFilterPill(
                            label = mood,
                            isSelected = isSelected,
                            onClick = {
                                onMoodSelected(mood)
                                onSmartShuffleClicked(mood)
                            },
                            modifier = Modifier.testTag("mood_chip_$mood")
                        )
                    }
                }
            }
        }

        // 5. Curated Playlists
        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                AuralisSectionHeader(
                    overline = "COLLECTIONS",
                    title = "Curated Playlists",
                    actionText = "Studio Generator",
                    onAction = onOpenPlaylistGenerator
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(playlists) { playlist ->
                        Card(
                            shape = RoundedCornerShape(AuralisTokens.RadiusMedium),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
                            modifier = Modifier
                                .width(150.dp)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusMedium))
                                .clickable { onPlaylistClicked(playlist) }
                                .testTag("playlist_card_${playlist.id}")
                        ) {
                            Column {
                                AsyncImage(
                                    model = playlist.coverUrl,
                                    contentDescription = playlist.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                )
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = playlist.title,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = PlatinumWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = playlist.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MutedSlate,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Recommended Tracks
        item {
            AuralisSectionHeader(
                overline = "RECOMMENDED",
                title = "Tracks for You",
                actionText = "${tracks.size} tracks",
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        items(tracks) { track ->
            AuralisTrackRow(
                track = track,
                isCurrent = false,
                isPlaying = false,
                onClick = { onTrackClicked(track) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDownload = { onDownloadTrack(track) }
            )
        }

        // 7. Quiet Telemetry
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111214))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "LISTENING TELEMETRY",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${analytics.totalMinutesListened} mins listened • ${analytics.listeningStreakDays}-day streak",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = PlatinumWhite
                        )
                    }

                    Text(
                        text = analytics.topMood.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGold
                    )
                }
            }
        }
    }
}
