package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.VinylGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

enum class ImmersiveVisualizerMode(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    REACTIVE_WAVEFORM("Waveform", Icons.Default.Waves),
    SPECTRUM_SPECTRUM("Spectrum", Icons.Default.GraphicEq),
    CIRCULAR_ORBIT("Orbit", Icons.Default.Radio)
}

/**
 * ImmersiveVisualizer composable that uses the current audio playback state
 * to render a real-time reactive waveform or frequency spectrum with audiophile telemetry HUD.
 */
@Composable
fun ImmersiveVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    track: Track? = null,
    modifier: Modifier = Modifier,
    height: Dp = 230.dp,
    initialMode: ImmersiveVisualizerMode = ImmersiveVisualizerMode.REACTIVE_WAVEFORM,
    showControls: Boolean = true
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var isExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "immersive_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Calculate real-time audio telemetry
    val peakAmp = remember(amplitudes) {
        if (amplitudes.isEmpty()) 0.2f
        else amplitudes.maxOrNull()?.coerceIn(0.05f, 1.0f) ?: 0.2f
    }

    val dbFSRating = remember(peakAmp) {
        val db = 20 * log10(peakAmp.coerceAtLeast(0.001f))
        "%.1f dBFS".format(db)
    }

    val dominantBand = remember(amplitudes) {
        if (amplitudes.isEmpty()) "MIDRANGE 1 kHz"
        else {
            val maxIndex = amplitudes.indices.maxByOrNull { amplitudes[it] } ?: 0
            when {
                maxIndex < amplitudes.size * 0.2 -> "SUB-BASS 60 Hz"
                maxIndex < amplitudes.size * 0.4 -> "PUNCH BASS 230 Hz"
                maxIndex < amplitudes.size * 0.7 -> "VOCAL MID 1.2 kHz"
                else -> "AIR TREBLE 14 kHz"
            }
        }
    }

    val activeHeight = if (isExpanded) 360.dp else height

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D15)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF1E283D), RoundedCornerShape(20.dp))
            .testTag("immersive_visualizer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Telemetry HUD Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) ElectricCyan else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "DSP ENGINE ACTIVE" else "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = if (isPlaying) ElectricCyan else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF161F30))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "24-BIT / 96 kHz",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = VinylGold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dbFSRating,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (peakAmp > 0.85f) NeonPink else ElectricCyan
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp).testTag("visualizer_expand_toggle")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Expand Visualizer",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Visualizer Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(activeHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0D121F), Color(0xFF07090F))
                        )
                    )
                    .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Cycle modes on tap
                        val next = (selectedMode.ordinal + 1) % ImmersiveVisualizerMode.entries.size
                        selectedMode = ImmersiveVisualizerMode.entries[next]
                    }
                    .testTag("immersive_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val safeAmps = if (amplitudes.isEmpty()) List(28) { 0.15f } else amplitudes

                    when (selectedMode) {
                        ImmersiveVisualizerMode.REACTIVE_WAVEFORM -> {
                            drawImmersiveWaveform(
                                amplitudes = safeAmps,
                                isPlaying = isPlaying,
                                phase = phase,
                                primaryColor = ElectricCyan,
                                secondaryColor = LuminousViolet,
                                accentColor = NeonPink
                            )
                        }
                        ImmersiveVisualizerMode.SPECTRUM_SPECTRUM -> {
                            drawImmersiveSpectrum(
                                amplitudes = safeAmps,
                                isPlaying = isPlaying,
                                phase = phase,
                                primaryColor = ElectricCyan,
                                secondaryColor = LuminousViolet,
                                accentColor = VinylGold
                            )
                        }
                        ImmersiveVisualizerMode.CIRCULAR_ORBIT -> {
                            drawImmersiveCircularOrbit(
                                amplitudes = safeAmps,
                                isPlaying = isPlaying,
                                phase = phase,
                                primaryColor = ElectricCyan,
                                secondaryColor = LuminousViolet,
                                accentColor = NeonPink
                            )
                        }
                    }
                }

                // Overlay dominant band badge at bottom center
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x990A0E17))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DOMINANT: $dominantBand",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            if (showControls) {
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Mode Selector Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImmersiveVisualizerMode.entries.forEach { mode ->
                        val isSelected = mode == selectedMode
                        val bg by animateColorAsState(
                            if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent,
                            label = "modeBg"
                        )
                        val textColor by animateColorAsState(
                            if (isSelected) ElectricCyan else Color.White.copy(alpha = 0.5f),
                            label = "modeText"
                        )

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ElectricCyan else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("visualizer_mode_${mode.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = mode.displayName,
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mode.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1. Reactive Waveform: Multi-layer Bézier spline with translucent glow underbelly & phase animation.
 */
private fun DrawScope.drawImmersiveWaveform(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val w = size.width
    val h = size.height
    val centerY = h / 2f
    val count = amplitudes.size.coerceAtLeast(16)
    val stepX = w / (count - 1).toFloat()

    // Layer 1: Translucent Filled Ribbon (Cyan to Violet)
    val ribbonPath = Path()
    val fillPath = Path()

    ribbonPath.moveTo(0f, centerY)
    fillPath.moveTo(0f, h)
    fillPath.lineTo(0f, centerY)

    for (i in 0 until count) {
        val amp = amplitudes.getOrElse(i) { 0.2f }
        val waveMod = if (isPlaying) sin(phase + i * 0.4f) * (h * 0.12f) else 0f
        val y = centerY - (amp * (h * 0.42f)) + waveMod
        val x = i * stepX

        if (i == 0) {
            ribbonPath.moveTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevAmp = amplitudes.getOrElse(i - 1) { 0.2f }
            val prevMod = if (isPlaying) sin(phase + (i - 1) * 0.4f) * (h * 0.12f) else 0f
            val prevY = centerY - (prevAmp * (h * 0.42f)) + prevMod
            val cx = (prevX + x) / 2f
            ribbonPath.quadraticTo(prevX, prevY, cx, (prevY + y) / 2f)
            fillPath.lineTo(x, y)
        }
    }
    fillPath.lineTo(w, h)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.35f),
                secondaryColor.copy(alpha = 0.12f),
                Color.Transparent
            )
        )
    )

    drawPath(
        path = ribbonPath,
        brush = Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, accentColor)
        ),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Layer 2: Counter-Wave Phase Ribbon in Neon Accent
    val counterPath = Path()
    for (i in 0 until count) {
        val amp = amplitudes.getOrElse(count - 1 - i) { 0.15f }
        val waveMod = if (isPlaying) cos(phase * 1.3f + i * 0.5f) * (h * 0.08f) else 0f
        val y = centerY + (amp * (h * 0.35f)) - waveMod
        val x = i * stepX

        if (i == 0) {
            counterPath.moveTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevAmp = amplitudes.getOrElse(count - i) { 0.15f }
            val prevMod = if (isPlaying) cos(phase * 1.3f + (i - 1) * 0.5f) * (h * 0.08f) else 0f
            val prevY = centerY + (prevAmp * (h * 0.35f)) - prevMod
            val cx = (prevX + x) / 2f
            counterPath.quadraticTo(prevX, prevY, cx, (prevY + y) / 2f)
        }
    }

    drawPath(
        path = counterPath,
        brush = Brush.horizontalGradient(
            colors = listOf(accentColor.copy(alpha = 0.9f), secondaryColor.copy(alpha = 0.7f))
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * 2. Immersive Frequency Spectrum: 28-32 vibrant gradient bars with floating peak needles & reflection.
 */
private fun DrawScope.drawImmersiveSpectrum(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val totalWidth = size.width
    val canvasHeight = size.height
    val count = amplitudes.size.coerceIn(24, 32)
    val barSpacing = totalWidth / (count * 1.48f)
    val barWidth = barSpacing * 0.76f
    val startX = (totalWidth - (count * (barWidth + barSpacing) - barSpacing)) / 2f

    val gradient = Brush.verticalGradient(
        colors = listOf(
            accentColor,
            secondaryColor,
            primaryColor
        )
    )

    val groundY = canvasHeight * 0.75f

    for (i in 0 until count) {
        val rawAmp = amplitudes.getOrElse(i) { 0.15f }
        val oscillation = if (isPlaying) sin(phase + i * 0.35f) * 0.04f else 0f
        val amp = (rawAmp + oscillation).coerceIn(0.06f, 1.0f)

        val barH = groundY * amp * 0.9f
        val x = startX + i * (barWidth + barSpacing)
        val y = groundY - barH

        // Draw main frequency bar upward
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(x, y),
            size = Size(barWidth, barH),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Peak Hold Needle Dot
        if (isPlaying && amp > 0.3f) {
            val peakY = (y - 5.dp.toPx()).coerceAtLeast(3.dp.toPx())
            drawCircle(
                color = accentColor,
                radius = barWidth * 0.45f,
                center = Offset(x + barWidth / 2f, peakY)
            )
        }

        // Glass Floor Reflection (Subtle mirrored bar fading down)
        val reflectionH = barH * 0.32f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                startY = groundY,
                endY = groundY + reflectionH
            ),
            topLeft = Offset(x, groundY + 2.dp.toPx()),
            size = Size(barWidth, reflectionH),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )
    }

    // Ground reflection separation line
    drawLine(
        color = Color.White.copy(alpha = 0.15f),
        start = Offset(0f, groundY),
        end = Offset(totalWidth, groundY),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * 3. Circular Radial Orbit: 360-degree frequency needles with pulsing core.
 */
private fun DrawScope.drawImmersiveCircularOrbit(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.height.coerceAtMost(size.width) / 2f) - 10.dp.toPx()
    val bassEnergy = amplitudes.take(4).average().toFloat().coerceIn(0.1f, 1f)
    val baseRadius = maxRadius * 0.42f + (if (isPlaying) bassEnergy * 10.dp.toPx() else 0f)

    // Pulsing core halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondaryColor.copy(alpha = 0.35f * bassEnergy), Color.Transparent),
            center = center,
            radius = baseRadius * 1.5f
        ),
        radius = baseRadius * 1.5f,
        center = center
    )

    // Core circle
    drawCircle(
        color = Color(0xFF101726),
        radius = baseRadius,
        center = center
    )

    drawCircle(
        color = primaryColor.copy(alpha = 0.7f),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    val count = amplitudes.size.coerceIn(24, 48)
    val angleStep = (2f * PI.toFloat()) / count

    for (i in 0 until count) {
        val angle = i * angleStep + (if (isPlaying) phase * 0.25f else 0f)
        val amp = amplitudes.getOrElse(i % amplitudes.size) { 0.2f }
        val needleLength = (maxRadius - baseRadius) * amp * 0.92f

        val innerX = center.x + baseRadius * cos(angle)
        val innerY = center.y + baseRadius * sin(angle)
        val outerX = center.x + (baseRadius + needleLength) * cos(angle)
        val outerY = center.y + (baseRadius + needleLength) * sin(angle)

        val color = if (i % 3 == 0) accentColor else if (i % 2 == 0) primaryColor else secondaryColor

        drawLine(
            color = color.copy(alpha = 0.85f),
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        if (amp > 0.4f && isPlaying) {
            drawCircle(
                color = color,
                radius = 2.dp.toPx(),
                center = Offset(outerX, outerY)
            )
        }
    }
}
