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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily

/**
 * Editorial-quality Music Home Screen for AURALIS.
 * Clean hierarchy, refined typography, and OpenRouter AI dynamic mood shuffler.
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
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Refined AURALIS Brand Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AURALIS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = PlatinumWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ImperialGold.copy(alpha = 0.15f))
                                    .border(0.6.dp, ImperialGold, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HI-RES DIRECT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = ImperialGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bit-Perfect Lossless Architecture • 24-Bit / 96kHz",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MutedSlate
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Voice Command Shortcut
                        IconButton(
                            onClick = onOpenVoiceSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                                .testTag("home_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = ImperialGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // AI Maestro Agent Master Controller
                        IconButton(
                            onClick = onOpenAiMaestro,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                                .testTag("home_maestro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Maestro Agent",
                                tint = ChampagneGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Settings Entry Point
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
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
        }

        // 2. Continue Listening Featured Hero Banner
        if (featuredTrack != null) {
            item {
                Card(
                    shape = RoundedCornerShape(AuralisTokens.RadiusLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F1524)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(
                            AuralisTokens.BorderHairline,
                            ImperialGold.copy(alpha = 0.35f),
                            RoundedCornerShape(AuralisTokens.RadiusLarge)
                        )
                        .clickable { onTrackClicked(featuredTrack) }
                        .testTag("featured_track_hero")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuralisArtwork(
                            model = featuredTrack.artworkUrl,
                            contentDescription = featuredTrack.title,
                            size = 64.dp,
                            cornerRadius = 14.dp,
                            showHiResBadge = true
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ImperialGold)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "CONTINUE LISTENING",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = ImperialGold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = featuredTrack.title,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PlatinumWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${featuredTrack.artist} • ${featuredTrack.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play Button Knob
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ImperialGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = DeepObsidianNoir,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. OpenRouter / Gemini AI Dynamic Mood Strategy Card
        item {
            Card(
                shape = RoundedCornerShape(AuralisTokens.RadiusLarge),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .border(
                        AuralisTokens.BorderHairline,
                        DarkSurfaceBorder,
                        RoundedCornerShape(AuralisTokens.RadiusLarge)
                    )
                    .testTag("dynamic_mood_shuffler_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF162035))
                                    .border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ImperialGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DYNAMIC MOOD STRATEGY",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp,
                                    color = PlatinumWhite
                                )
                                Text(
                                    text = "OpenRouter / Gemini Psychoacoustic Shuffler",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MutedSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Natural Language Input Field
                    OutlinedTextField(
                        value = customDescriptorInput,
                        onValueChange = { customDescriptorInput = it },
                        placeholder = {
                            Text(
                                text = "e.g. energetic morning workout or rainy day study...",
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
                            focusedContainerColor = Color(0xFF090D16),
                            unfocusedContainerColor = Color(0xFF090D16),
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

                    // Quick Prompt Inspiration Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickChips = listOf(
                            Triple("energetic morning workout", "125-142 BPM", Icons.Default.FitnessCenter),
                            Triple("relaxed rainy day", "74-88 BPM", Icons.Default.WaterDrop),
                            Triple("late night drive", "95-110 BPM", Icons.Default.Bolt),
                            Triple("deep focus cafe", "60-80 BPM", Icons.Default.AutoAwesome)
                        )

                        items(quickChips) { (prompt, bpmRange, icon) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141C2D))
                                    .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        customDescriptorInput = prompt
                                        onGenerateDynamicMoodPlaylist(prompt)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("quick_chip_${prompt.replace(" ", "_")}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = ImperialGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prompt,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrushedSilver
                                    )
                                }
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
                                .background(Color(0xFF090D16))
                                .border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
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
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
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

        // 4. Target Mood State Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)) {
                AuralisSectionHeader(
                    overline = "RESONANCE",
                    title = "Target Mood State"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
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

        // 5. Curated Mood Playlists
        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                AuralisSectionHeader(
                    overline = "COLLECTIONS",
                    title = "Curated Playlists",
                    actionText = "Studio Generator",
                    onAction = onOpenPlaylistGenerator
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    items(playlists) { playlist ->
                        Card(
                            shape = RoundedCornerShape(AuralisTokens.RadiusMedium),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0F1524)
                            ),
                            modifier = Modifier
                                .width(160.dp)
                                .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusMedium))
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
                                        fontWeight = FontWeight.Bold,
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

        // 6. Curated Track Selections
        item {
            AuralisSectionHeader(
                overline = "AUDIO ARCHIVE",
                title = "Recommended Master Tracks",
                actionText = "${tracks.size} Tracks",
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

        // 7. Listening Telemetry Summary Glance
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D121E))
                    .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ACOUSTIC TELEMETRY",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = ImperialGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${analytics.totalMinutesListened} mins listened • ${analytics.listeningStreakDays}-day streak",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PlatinumWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161E2E))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = analytics.topMood.uppercase(),
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGold
                        )
                    }
                }
            }
        }
    }
}
