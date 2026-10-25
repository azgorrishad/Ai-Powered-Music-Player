from pathlib import Path
import shutil
import re
import sys

ROOT = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path.cwd()
MAIN = ROOT / 'app/src/main/java/com/example/MainActivity.kt'
HOME = ROOT / 'app/src/main/java/com/example/ui/screens/HomeScreen.kt'
COMP = ROOT / 'app/src/main/java/com/example/ui/components/AuralisComponents.kt'
MINI = ROOT / 'app/src/main/java/com/example/ui/components/MiniPlayer.kt'
COLOR = ROOT / 'app/src/main/java/com/example/ui/theme/Color.kt'
THEME = ROOT / 'app/src/main/java/com/example/ui/theme/Theme.kt'

required = [MAIN, HOME, COMP, MINI, COLOR, THEME]
missing = [str(p) for p in required if not p.exists()]
if missing:
    print('ERROR: This does not look like the AURALIS project root. Missing:')
    print('\n'.join(missing))
    print('Run: python apply_auralis_ui_overhaul.py /path/to/Ai-Powered-Music-Player')
    raise SystemExit(2)

backup_dir = ROOT / 'ui_overhaul_backup'
backup_dir.mkdir(exist_ok=True)
for p in required:
    relative = p.relative_to(ROOT)
    backup_path = backup_dir / relative
    backup_path.parent.mkdir(parents=True, exist_ok=True)
    if not backup_path.exists():
        shutil.copy2(p, backup_path)

# 1. Calmer, consistent color palette. Keep the theme names and existing public symbols intact.
COLOR.write_text('''package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// AURALIS core palette: warm monochrome, graphite surfaces, and restrained champagne.
val DeepObsidianNoir = Color(0xFF090A0C)
val DarkBackground = Color(0xFF090A0C)
val DarkSurface = Color(0xFF111214)
val DarkSurfaceVariant = Color(0xFF181A1D)
val DarkSurfaceHighlight = Color(0xFF222428)
val DarkSurfaceBorder = Color(0xFF282A2D)

// Accent colors are intentionally softened. Reserve gold for meaningful focus and playback.
val ImperialGold = Color(0xFFC8A86B)
val ChampagneGold = Color(0xFFDCC28F)
val VinylGold = Color(0xFFB79658)
val ElectricCyan = Color(0xFF719AA3)
val LuminousViolet = Color(0xFF8C83A5)
val NeonPink = Color(0xFFAB7787)
val EmeraldGreen = Color(0xFF5C9B82)
val AudiophileAmber = Color(0xFFC39A60)

// Neutral metallic accents and text.
val PlatinumWhite = Color(0xFFF3F1EA)
val BrushedSilver = Color(0xFFC1C0BA)
val MutedSlate = Color(0xFF92938F)
val DarkCharcoal = Color(0xFF5D605D)

val TextPrimaryDark = Color(0xFFF3F1EA)
val TextSecondaryDark = Color(0xFFA0A19C)
val TextTertiaryDark = Color(0xFF696B68)

// Light-theme reference palette.
val LightBackground = Color(0xFFF8F8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0EC)
val LightSurfaceHighlight = Color(0xFFE2E2DC)
val TextPrimaryLight = Color(0xFF171816)
val TextSecondaryLight = Color(0xFF555752)
val TextTertiaryLight = Color(0xFF888B84)
''', encoding='utf-8')

# 2. Make the shared section headings editorial rather than badge-heavy.
comp = COMP.read_text(encoding='utf-8')
start = comp.find('@Composable\nfun AuralisSectionHeader(')
end = comp.find('/**\n * Tactile Album / Track Artwork', start)
if start < 0 or end < 0:
    raise RuntimeError('Could not safely locate AuralisSectionHeader in AuralisComponents.kt')
new_header = '''@Composable
fun AuralisSectionHeader(
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = overline.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                color = MutedSlate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                    letterSpacing = (-0.25).sp
                ),
                color = PlatinumWhite
            )
        }

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = BrushedSilver,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onAction() }
                    .padding(start = 8.dp, top = 10.dp, end = 2.dp, bottom = 10.dp)
            )
        }
    }
}

'''
comp = comp[:start] + new_header + comp[end:]
# Neutralize the shared card-border brush. The gold gradient remains available for primary actions.
comp = comp.replace('''    val SubtleBorderBrush = Brush.horizontalGradient(
        listOf(
            DarkSurfaceBorder,
            ImperialGold.copy(alpha = 0.35f),
            DarkSurfaceBorder
        )
    )''', '''    val SubtleBorderBrush = Brush.horizontalGradient(
        listOf(
            DarkSurfaceBorder,
            Color(0xFF34363A),
            DarkSurfaceBorder
        )
    )''')
comp = comp.replace('''    val SurfaceGradient = Brush.linearGradient(
        listOf(Color(0xFF111726), Color(0xFF0A0F1A))
    )''', '''    val SurfaceGradient = Brush.linearGradient(
        listOf(Color(0xFF17191C), Color(0xFF101113))
    )''')
COMP.write_text(comp, encoding='utf-8')

# 3. Replace the busy header + small framed banner with a restrained editorial header and real-track hero.
home = HOME.read_text(encoding='utf-8')
start_line = home.find('        // 1. Refined AURALIS Brand Header')
end_line = home.find('        // 3. OpenRouter / Gemini AI Dynamic Mood Strategy Card', start_line)
if start_line < 0 or end_line < 0:
    raise RuntimeError('Could not safely locate the HomeScreen header and featured-track sections')
new_home_block = '''        // 1. Quiet editorial brand header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 18.dp, top = 22.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AURALIS",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        letterSpacing = 2.8.sp,
                        color = PlatinumWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                            .testTag("home_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = BrushedSilver,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF17191B))
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

        // 2. Featured track. Artwork and metadata come from the actual library item.
        if (featuredTrack != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 2.dp)
                        .border(1.dp, Color(0xFF282A2D), RoundedCornerShape(22.dp))
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
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AuralisArtwork(
                                model = featuredTrack.artworkUrl,
                                contentDescription = "Artwork for ${featuredTrack.title}",
                                size = 88.dp,
                                cornerRadius = 16.dp,
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
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = featuredTrack.artist,
                                    fontSize = 12.sp,
                                    color = MutedSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(ImperialGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play ${featuredTrack.title}",
                                            tint = DeepObsidianNoir,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(9.dp))
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

'''
home = home[:start_line] + new_home_block + home[end_line:]
# Make the AI panel's copy less technical and visually over-branded.
home = home.replace('text = "DYNAMIC MOOD STRATEGY"', 'text = "MOOD-BASED MIX"')
home = home.replace('text = "OpenRouter / Gemini Psychoacoustic Shuffler"', 'text = "Describe the moment. Find your sound."')
home = home.replace('text = "e.g. energetic morning workout or rainy day study..."', 'text = "What do you want your music to feel like?"')
home = home.replace('containerColor = Color(0xFF0F1524)', 'containerColor = Color(0xFF121416)')
HOME.write_text(home, encoding='utf-8')

# 4. Redesign the persistent mini-player. Remove fake HI-RES status, glowing borders, and redundant waveform.
MINI.write_text('''package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.Track
import com.example.ui.theme.BrushedSilver
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SpaceGroteskFamily

/** Persistent mini-player with understated artwork, clear controls, and a hairline progress track. */
@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    visualizerAmplitudes: List<Float>,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val progress = if (durationMs > 0L) {
        (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121416)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("mini_player")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = "Cover for ${track.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF1B1D20))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.sp
                        ),
                        color = PlatinumWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ImperialGold)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DeepObsidianNoir,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next track",
                        tint = BrushedSilver,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = ImperialGold,
                trackColor = Color(0xFF292B2E)
            )
        }
    }
}
''', encoding='utf-8')

# 5. Four primary destinations. EQ and Insights remain available from the Settings screen.
main = MAIN.read_text(encoding='utf-8')
start = main.find('                // Luxury Audiophile Navigation Bar')
if start < 0:
    raise RuntimeError('Could not locate the main navigation section in MainActivity.kt')
start = main.rfind('\n', 0, start) + 1
closing = '\n                }\n            }\n        },\n        modifier = Modifier.fillMaxSize()'
end = main.find(closing, start)
if end < 0:
    raise RuntimeError('Could not safely locate the end of the NavigationBar block in MainActivity.kt')
end += len('\n                }')
new_nav = '''                // Four clear primary destinations. Secondary tools live inside Settings.
                NavigationBar(
                    containerColor = Color(0xFF090A0C),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = com.example.ui.theme.DarkSurfaceBorder,
                            shape = androidx.compose.ui.graphics.RectangleShape
                        )
                        .testTag("main_navigation_bar")
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.ImperialGold,
                        selectedTextColor = com.example.ui.theme.PlatinumWhite,
                        unselectedIconColor = com.example.ui.theme.MutedSlate,
                        unselectedTextColor = com.example.ui.theme.MutedSlate,
                        indicatorColor = Color.Transparent
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.HOME,
                        onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = if (currentScreen == ScreenDestination.HOME) FontWeight.SemiBold else FontWeight.Normal, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.LIBRARY,
                        onClick = { viewModel.navigateTo(ScreenDestination.LIBRARY) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library", fontWeight = if (currentScreen == ScreenDestination.LIBRARY) FontWeight.SemiBold else FontWeight.Normal, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_library")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.AI_MAESTRO,
                        onClick = { viewModel.navigateTo(ScreenDestination.AI_MAESTRO) },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Maestro") },
                        label = { Text("Maestro", fontWeight = if (currentScreen == ScreenDestination.AI_MAESTRO) FontWeight.SemiBold else FontWeight.Normal, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_ai_maestro")
                    )

                    NavigationBarItem(
                        selected = currentScreen == ScreenDestination.SETTINGS || currentScreen == ScreenDestination.EQUALIZER || currentScreen == ScreenDestination.ANALYTICS,
                        onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings and tools") },
                        label = { Text("Settings", fontWeight = if (currentScreen == ScreenDestination.SETTINGS) FontWeight.SemiBold else FontWeight.Normal, fontFamily = com.example.ui.theme.SpaceGroteskFamily, fontSize = 10.sp) },
                        colors = navColors,
                        modifier = Modifier.testTag("nav_settings")
                    )
                }'''
main = main[:start] + new_nav + main[end:]
MAIN.write_text(main, encoding='utf-8')

# 6. Update the Obsidian Material theme to match the new neutral surfaces and softer champagne.
theme = THEME.read_text(encoding='utf-8')
theme = theme.replace('Color(0xFFE5B869)', 'Color(0xFFC8A86B)')
theme = theme.replace('Color(0xFFFFE09C)', 'Color(0xFFF0D9A8)')
theme = theme.replace('Color(0xFF07090E)', 'Color(0xFF090A0C)')
theme = theme.replace('Color(0xFF0E131F)', 'Color(0xFF111214)')
theme = theme.replace('Color(0xFF161E2E)', 'Color(0xFF181A1D)')
theme = theme.replace('Color(0xFF222B3D)', 'Color(0xFF282A2D)')
theme = theme.replace('Color(0xFF2E2209)', 'Color(0xFF302819)')
theme = theme.replace('Color(0xFFD4AF37)', 'Color(0xFFB79658)')
THEME.write_text(theme, encoding='utf-8')

print('AURALIS UI overhaul applied.')
print(f'Project root: {ROOT}')
print(f'Backups: {backup_dir}')
print('Changed files:')
for p in [COLOR, COMP, HOME, MINI, MAIN, THEME]:
    print(f'  - {p.relative_to(ROOT)}')
print('\nNext: run ./gradlew assembleDebug from the project root, then inspect Home, Library, Mini Player, Settings, and Now Playing on a device.')
