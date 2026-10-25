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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Waves
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
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.VinylGold

/**
 * Signature Now Playing screen for AURALIS.
 * Luxury master console featuring adaptive vinyl artwork, real-time waveform,
 * synchronized lyrics, and tactile playback controls.
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

    // Smooth subtle vinyl rotation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )
    val currentRotation = if (isPlaying) rotation else 0f

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }
    var stageMode by remember { mutableStateOf("VINYL") } // "VINYL", "LYRICS", "SPECTRUM"

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
                .padding(horizontal = 22.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B3D))
            )

            // Master Header Bar
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
                        .background(DarkSurface)
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                        .testTag("collapse_now_playing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = ImperialGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) ImperialGold else DarkCharcoal)
                        )
                        Text(
                            text = "AURALIS MASTER CONSOLE",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = ImperialGold
                        )
                    }
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                            .testTag("eq_shortcut_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Open Sound Lab EQ",
                            tint = ChampagneGold,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage Mode Switcher (Vinyl Art, Synced Lyrics, Audio Spectrum)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(AuralisTokens.RadiusPill))
                    .background(Color(0xFF0C101A))
                    .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusPill))
                    .padding(3.dp)
            ) {
                val isVinyl = stageMode == "VINYL"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isVinyl) Color(0xFF1B2336) else Color.Transparent)
                        .border(
                            width = AuralisTokens.BorderHairline,
                            color = if (isVinyl) ImperialGold.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { stageMode = "VINYL" }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VINYL STAGE",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isVinyl) ImperialGold else DarkCharcoal
                    )
                }

                val isLyrics = stageMode == "LYRICS"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isLyrics) Color(0xFF1B2336) else Color.Transparent)
                        .border(
                            width = AuralisTokens.BorderHairline,
                            color = if (isLyrics) ChampagneGold.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { stageMode = "LYRICS" }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LYRICS",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isLyrics) ChampagneGold else DarkCharcoal
                    )
                }

                val isSpectrum = stageMode == "SPECTRUM"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSpectrum) Color(0xFF1B2336) else Color.Transparent)
                        .border(
                            width = AuralisTokens.BorderHairline,
                            color = if (isSpectrum) BrushedSilver.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { stageMode = "SPECTRUM" }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SPECTRUM",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isSpectrum) BrushedSilver else DarkCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Visual Stage
            if (stageMode == "LYRICS") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(AuralisTokens.RadiusLarge))
                        .background(Color(0xFF0A0E18))
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(AuralisTokens.RadiusLarge))
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
                    height = 280.dp,
                    showControls = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Vinyl Disc Artwork with Brushed Metallic Chamfer Outer Edge & Micro-Grooves
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(280.dp)
                        .padding(6.dp)
                ) {
                    // Subtle ambient back-glow
                    Box(
                        modifier = Modifier
                            .size(266.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        ImperialGold.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Vinyl Disc outer metallic chamfer
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(256.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF090D15))
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        ImperialGold.copy(alpha = 0.7f),
                                        Color(0xFF161E2E),
                                        ChampagneGold.copy(alpha = 0.8f),
                                        Color(0xFF0C101A),
                                        ImperialGold.copy(alpha = 0.7f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(currentRotation)
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadius = size.width / 2f

                            val grooveFractions = listOf(0.94f, 0.88f, 0.82f, 0.76f, 0.70f, 0.64f, 0.58f)
                            for (fraction in grooveFractions) {
                                drawCircle(
                                    color = Color(0xFF141A28),
                                    radius = maxRadius * fraction,
                                    center = center,
                                    style = Stroke(width = 1f)
                                )
                            }
                        }

                        // Center Label with Track Cover Art and Gold Bezel
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(118.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            ImperialGold,
                                            ChampagneGold,
                                            Color(0xFF1B2436),
                                            ImperialGold
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .rotate(currentRotation)
                        ) {
                            AsyncImage(
                                model = track.artworkUrl,
                                contentDescription = "Cover for ${track.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Heavy Spindle Brass Hole
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF070A12))
                                    .border(1.5.dp, ImperialGold, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Track Title, Artist, & Favorite Heart
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
                            fontSize = 22.sp
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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (track.isFavorite) NeonPink.copy(alpha = 0.15f) else DarkSurface)
                        .border(AuralisTokens.BorderHairline, if (track.isFavorite) NeonPink.copy(alpha = 0.5f) else DarkSurfaceBorder, CircleShape)
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

            // Audiophile Badges row (Hi-Res Master, BPM, Mood)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF121826))
                        .border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "HI-RES",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialGold
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(ImperialGold)
                        )
                        Text(
                            text = "24-BIT / 96kHz",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrushedSilver
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF101624))
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${track.bpm} BPM",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF151222))
                        .border(AuralisTokens.BorderHairline, LuminousViolet.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = track.mood.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuminousViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thin, elegant Slider & Duration Readouts
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
                    inactiveTrackColor = Color(0xFF1B2336)
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
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = ImperialGold
                )
                Text(
                    text = formatTimeMs(durationMs),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    color = MutedSlate
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balanced Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Button (With Smart Mood Shuffle badge)
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isShuffleEnabled) Color(0xFF131D2E) else Color.Transparent)
                        .then(
                            if (isShuffleEnabled) Modifier.border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.5f), CircleShape)
                            else Modifier
                        )
                        .testTag("shuffle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) ImperialGold else DarkCharcoal,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSmartMoodShuffle) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Smart Mood Active",
                                tint = ImperialGold,
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                // Previous Track Button
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                        .testTag("skip_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = PlatinumWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Tactile Center Play/Pause Knob in Champagne Gold
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ImperialGold, ChampagneGold, VinylGold)
                            )
                        )
                        .border(1.5.dp, Color(0xFFFFF2C6), CircleShape)
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
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Next Track Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(AuralisTokens.BorderHairline, DarkSurfaceBorder, CircleShape)
                        .testTag("skip_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = PlatinumWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (repeatMode != RepeatMode.OFF) Color(0xFF131D2E) else Color.Transparent)
                        .then(
                            if (repeatMode != RepeatMode.OFF) Modifier.border(AuralisTokens.BorderHairline, ImperialGold.copy(alpha = 0.5f), CircleShape)
                            else Modifier
                        )
                        .testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat: $repeatMode",
                        tint = if (repeatMode != RepeatMode.OFF) ImperialGold else DarkCharcoal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Gemini AI Dynamic Mood Selector
            GeminiMoodSelectorDropdown(
                selectedMood = selectedMood,
                onMoodSelected = onMoodSelected,
                labelPrefix = "Gemini Mood Strategy"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // DSP Sound Architecture Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DSP SOUND ARCHITECTURE",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = ImperialGold
                )
                Text(
                    text = activePreset.name.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedSlate
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPresets) { preset ->
                    val isSelected = activePreset.id == preset.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AuralisTokens.RadiusSmall))
                            .background(if (isSelected) Color(0xFF1B2438) else Color(0xFF0F1524))
                            .border(
                                AuralisTokens.BorderHairline,
                                if (isSelected) ImperialGold else DarkSurfaceBorder,
                                RoundedCornerShape(AuralisTokens.RadiusSmall)
                            )
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = preset.name,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ImperialGold else MutedSlate
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
