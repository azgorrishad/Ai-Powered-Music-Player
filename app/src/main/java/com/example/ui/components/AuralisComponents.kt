package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
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
 * AURALIS Luxury Design System Tokens & Components.
 * Built on editorial restraint, quiet graphite layering, and warm champagne accents.
 */
object AuralisTokens {
    val RadiusSmall = 8.dp
    val RadiusMedium = 16.dp
    val RadiusLarge = 22.dp
    val RadiusPill = 24.dp

    val BorderHairline = 1.dp
    val BorderSubtle = 1.dp

    val SubtleBorderBrush = Brush.horizontalGradient(
        listOf(
            DarkSurfaceBorder,
            Color(0xFF34363A),
            DarkSurfaceBorder
        )
    )

    val SurfaceGradient = Brush.linearGradient(
        listOf(Color(0xFF16181B), Color(0xFF101113))
    )
}

/**
 * Editorial Section Header with quiet typography and clean alignment.
 */
@Composable
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
                    fontSize = 20.sp,
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
                    .padding(start = 8.dp, top = 8.dp, end = 2.dp, bottom = 8.dp)
            )
        }
    }
}

/**
 * Tactile Album / Track Artwork with understated corner radius and placeholder.
 */
@Composable
fun AuralisArtwork(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    cornerRadius: Dp = 12.dp,
    isPlaying: Boolean = false,
    showHiResBadge: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF141618))
            .border(
                width = 1.dp,
                color = if (isPlaying) ImperialGold.copy(alpha = 0.8f) else DarkSurfaceBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (model != null && model.toString().isNotBlank()) {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = DarkCharcoal,
                modifier = Modifier.size(size * 0.45f)
            )
        }

        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                AuralisMiniWaveform(isPlaying = true, color = ImperialGold)
            }
        }
    }
}

/**
 * Minimalist 4-bar Equalizer indicator for active playback.
 */
@Composable
fun AuralisMiniWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    height: Dp = 14.dp,
    color: Color = ImperialGold
) {
    val transition = rememberInfiniteTransition(label = "mini_eq")
    val heights = List(barCount) { i ->
        if (isPlaying) {
            val anim by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + i * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            anim
        } else {
            0.3f
        }
    }

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { frac ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(height * frac)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Reusable Track Row component with clean typography, generous touch targets, and subtle feedback.
 */
@Composable
fun AuralisTrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null
) {
    val activeBorder = if (isCurrent) ImperialGold.copy(alpha = 0.6f) else DarkSurfaceBorder
    val activeBackground = if (isCurrent) Color(0xFF181A1D) else Color(0xFF111214)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(AuralisTokens.RadiusMedium))
            .background(activeBackground)
            .border(1.dp, activeBorder, RoundedCornerShape(AuralisTokens.RadiusMedium))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("track_row_${track.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AuralisArtwork(
                model = track.artworkUrl,
                contentDescription = track.title,
                size = 46.dp,
                cornerRadius = 10.dp,
                isPlaying = isCurrent && isPlaying,
                showHiResBadge = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 13.5.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isCurrent) ImperialGold else PlatinumWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MutedSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (track.genre.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(DarkCharcoal)
                        )
                        Text(
                            text = track.genre,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                            color = DarkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            if (onDownload != null) {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (track.isDownloaded) Icons.Default.DownloadDone else Icons.Default.CloudDownload,
                        contentDescription = if (track.isDownloaded) "Downloaded" else "Download",
                        tint = if (track.isDownloaded) ImperialGold else MutedSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onToggleFavorite != null) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track.isFavorite) NeonPink else MutedSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Universal Luxury Search Bar with graphite styling.
 */
@Composable
fun AuralisSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onOpenVoice: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 13.sp,
                color = MutedSlate
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = BrushedSilver,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MutedSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (onOpenVoice != null) {
                    IconButton(onClick = onOpenVoice) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice search",
                            tint = BrushedSilver,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(AuralisTokens.RadiusMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ImperialGold,
            unfocusedBorderColor = DarkSurfaceBorder,
            focusedContainerColor = Color(0xFF141619),
            unfocusedContainerColor = Color(0xFF111214),
            focusedTextColor = PlatinumWhite,
            unfocusedTextColor = PlatinumWhite
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Restrained Category Filter Pill.
 */
@Composable
fun AuralisFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val bg = if (isSelected) Color(0xFF1E2125) else Color(0xFF111214)
    val border = if (isSelected) ImperialGold.copy(alpha = 0.7f) else DarkSurfaceBorder
    val textCol = if (isSelected) PlatinumWhite else MutedSlate

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuralisTokens.RadiusSmall))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(AuralisTokens.RadiusSmall))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textCol
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($count)",
                    fontSize = 10.sp,
                    color = textCol.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Surface Card with graphite background and hairline border.
 */
@Composable
fun AuralisSurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = AuralisTokens.RadiusMedium,
    borderColor: Color = DarkSurfaceBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF111214))
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Primary action button in restrained Champagne Gold.
 */
@Composable
fun AuralisPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ImperialGold,
            contentColor = DeepObsidianNoir,
            disabledContainerColor = DarkSurfaceBorder,
            disabledContentColor = DarkCharcoal
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier.height(46.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = DeepObsidianNoir, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
            text = text,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Outlined secondary button.
 */
@Composable
fun AuralisSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PlatinumWhite
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, Color(0xFF383A3E)))
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier.height(46.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = BrushedSilver, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
            text = text,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Tactile icon button with accessible 42-48dp target.
 */
@Composable
fun AuralisIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = PlatinumWhite,
    size: Dp = 42.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF17191C))
            .border(1.dp, DarkSurfaceBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

/**
 * Reusable Settings Row component.
 */
@Composable
fun AuralisSettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuralisTokens.RadiusSmall))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF17191C))
                    .border(1.dp, DarkSurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrushedSilver,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PlatinumWhite
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DarkCharcoal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Calm, professional empty state.
 */
@Composable
fun AuralisEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MusicNote,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF141618))
                .border(1.dp, DarkSurfaceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrushedSilver,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = PlatinumWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedSlate,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            AuralisPrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}
