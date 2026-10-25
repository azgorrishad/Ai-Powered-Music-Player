package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EqualizerPreset
import com.example.data.model.Track
import com.example.data.model.TrackLyrics
import com.example.ui.RepeatMode
import com.example.ui.theme.BrushedSilver
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily

/**
 * Visual Centerpiece: AURALIS Now Playing Screen.
 * Balanced composition with large album artwork, clear typography,
 * a precise progress bar, and refined tactile controls.
 */
@Composable
fun NowPlayingSheet(
    track: Track?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    visualizerAmplitudes: List<Float>,
    isShuffleEnabled: Boolean,
    isSmartMoodShuffle: Boolean,
    repeatMode: RepeatMode,
    activePreset: EqualizerPreset,
    allPresets: List<EqualizerPreset>,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onOpenEqualizer: () -> Unit,
    onCollapse: () -> Unit,
    visualizerMode: VisualizerMode = VisualizerMode.SPECTRUM_BARS,
    onVisualizerModeChange: (VisualizerMode) -> Unit = {},
    selectedMood: String = "Focus",
    onMoodSelected: (String) -> Unit = {},
    lyrics: TrackLyrics? = null,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onCollapse()
    }

    if (track == null) return

    val scrollState = rememberScrollState()

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }
    var stageMode by remember { mutableStateOf("ARTWORK") } // "ARTWORK", "LYRICS", "SPECTRUM"

    val currentSliderVal = if (isUserScrubbing) scrubPositionMs else progressMs.toFloat()
    val maxSliderVal = durationMs.toFloat().coerceAtLeast(1000f)

    Surface(
        color = DeepObsidianNoir,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232528))
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141618))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("collapse_now_playing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = BrushedSilver,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = MutedSlate
                    )
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrushedSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141618))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("eq_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Open Equalizer",
                        tint = BrushedSilver,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stage Switcher Tabs
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(AuralisTokens.RadiusPill))
                    .background(Color(0xFF121416))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusPill))
                    .padding(3.dp)
            ) {
                val tabs = listOf("ARTWORK" to "ARTWORK", "LYRICS" to "LYRICS", "SPECTRUM" to "SPECTRUM")
                tabs.forEach { (key, label) ->
                    val isSelected = stageMode == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color(0xFF1E2024) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) DarkSurfaceBorder else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { stageMode = key }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 1.sp,
                            color = if (isSelected) PlatinumWhite else MutedSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Visual Stage
            if (stageMode == "LYRICS") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(AuralisTokens.RadiusLarge))
                        .background(Color(0xFF111214))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusLarge))
                ) {
                    SyncedLyricsView(
                        lyrics = lyrics,
                        currentPositionMs = progressMs,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (stageMode == "SPECTRUM") {
                ImmersiveVisualizer(
                    amplitudes = visualizerAmplitudes,
                    isPlaying = isPlaying,
                    currentPositionMs = progressMs,
                    durationMs = durationMs,
                    track = track,
                    height = 290.dp,
                    showControls = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Large, beautiful album artwork
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF15171A))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = "Cover for ${track.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Track Information & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            letterSpacing = (-0.25).sp
                        ),
                        color = PlatinumWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(track) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141618))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("favorite_button")
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "Favorited" else "Not favorited",
                        tint = if (track.isFavorite) NeonPink else MutedSlate,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Precise Progress Bar & Time Readouts
            Slider(
                value = currentSliderVal,
                onValueChange = {
                    isUserScrubbing = true
                    scrubPositionMs = it
                },
                onValueChangeFinished = {
                    onSeekTo(scrubPositionMs.toLong())
                    isUserScrubbing = false
                },
                valueRange = 0f..maxSliderVal,
                colors = SliderDefaults.colors(
                    thumbColor = ImperialGold,
                    activeTrackColor = ImperialGold,
                    inactiveTrackColor = Color(0xFF222428)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scrubber_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimeMs(currentSliderVal.toLong()),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = PlatinumWhite
                )
                Text(
                    text = formatTimeMs(durationMs),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                    color = MutedSlate
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Balanced Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isShuffleEnabled) Color(0xFF1A1C20) else Color.Transparent)
                        .border(1.dp, if (isShuffleEnabled) ImperialGold.copy(alpha = 0.6f) else Color.Transparent, CircleShape)
                        .testTag("shuffle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) ImperialGold else MutedSlate,
                            modifier = Modifier.size(21.dp)
                        )
                        if (isSmartMoodShuffle) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Shuffle Active",
                                tint = ImperialGold,
                                modifier = Modifier
                                    .size(9.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                // Previous Track Button
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141618))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("skip_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = PlatinumWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Primary Play / Pause Control
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ImperialGold)
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("now_playing_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = DeepObsidianNoir,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Next Track Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141618))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("skip_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = PlatinumWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (repeatMode != RepeatMode.OFF) Color(0xFF1A1C20) else Color.Transparent)
                        .border(1.dp, if (repeatMode != RepeatMode.OFF) ImperialGold.copy(alpha = 0.6f) else Color.Transparent, CircleShape)
                        .testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat: $repeatMode",
                        tint = if (repeatMode != RepeatMode.OFF) ImperialGold else MutedSlate,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound Architecture Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EQUALIZER PRESETS",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = MutedSlate
                )
                Text(
                    text = activePreset.name.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = ImperialGold
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPresets) { preset ->
                    val isSelected = activePreset.id == preset.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AuralisTokens.RadiusSmall))
                            .background(if (isSelected) Color(0xFF1E2125) else Color(0xFF111214))
                            .border(
                                1.dp,
                                if (isSelected) ImperialGold.copy(alpha = 0.7f) else DarkSurfaceBorder,
                                RoundedCornerShape(AuralisTokens.RadiusSmall)
                            )
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = preset.name,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) PlatinumWhite else MutedSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatTimeMs(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
