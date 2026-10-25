package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.ScatterPlot
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.VinylGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visualizer visual modes available for the user to select.
 */
enum class VisualizerMode(val label: String, val icon: ImageVector) {
    SPECTRUM_BARS("Spectrum", Icons.Default.GraphicEq),
    NEON_WAVE("Neon Wave", Icons.Default.Waves),
    CIRCULAR_RADIAL("Radial Pulse", Icons.Default.Radio),
    ENERGY_PARTICLES("Particles", Icons.Default.ScatterPlot)
}

/**
 * High-performance, Canvas-driven audio visualizer reacting in real-time
 * to the playback spectrum amplitudes.
 */
@Composable
fun AudioVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    mode: VisualizerMode = VisualizerMode.SPECTRUM_BARS,
    onModeChange: ((VisualizerMode) -> Unit)? = null,
    showModeSelector: Boolean = false,
    primaryColor: Color = ElectricCyan,
    secondaryColor: Color = LuminousViolet,
    accentColor: Color = NeonPink
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "phase_animation"
    )

    // Smooth overall energy level derived from lower frequency bands
    val bassEnergy = remember(amplitudes) {
        if (amplitudes.isEmpty()) 0.2f
        else amplitudes.take(4).average().toFloat().coerceIn(0.1f, 1f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_visualizer_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Pills (optional interactive header)
        if (showModeSelector && onModeChange != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisualizerMode.entries.forEach { visualMode ->
                    val isSelected = visualMode == mode
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                        label = "pill_color"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        label = "pill_content"
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .clickable { onModeChange(visualMode) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = visualMode.icon,
                            contentDescription = visualMode.label,
                            tint = contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = visualMode.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }

        // Dedicated Canvas rendering the dynamic spectrum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C0F17))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (onModeChange != null) {
                        val nextOrdinal = (mode.ordinal + 1) % VisualizerMode.entries.size
                        onModeChange(VisualizerMode.entries[nextOrdinal])
                    }
                }
                .testTag("audio_visualizer_canvas")
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val effectiveAmplitudes = if (isPlaying) {
                    amplitudes.ifEmpty { List(24) { 0.2f } }
                } else {
                    List(amplitudes.size.coerceAtLeast(24)) { 0.08f }
                }

                when (mode) {
                    VisualizerMode.SPECTRUM_BARS -> {
                        drawSpectrumBars(
                            amplitudes = effectiveAmplitudes,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            accentColor = accentColor,
                            isPlaying = isPlaying,
                            phase = phase
                        )
                    }
                    VisualizerMode.NEON_WAVE -> {
                        drawNeonWave(
                            amplitudes = effectiveAmplitudes,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            accentColor = accentColor,
                            phase = phase,
                            isPlaying = isPlaying
                        )
                    }
                    VisualizerMode.CIRCULAR_RADIAL -> {
                        drawRadialPulse(
                            amplitudes = effectiveAmplitudes,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            accentColor = accentColor,
                            phase = phase,
                            isPlaying = isPlaying,
                            bassEnergy = bassEnergy
                        )
                    }
                    VisualizerMode.ENERGY_PARTICLES -> {
                        drawEnergyParticles(
                            amplitudes = effectiveAmplitudes,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            accentColor = accentColor,
                            phase = phase,
                            isPlaying = isPlaying,
                            bassEnergy = bassEnergy
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. SPECTRUM BARS: Studio rack style equalizer with mirrored bars and decaying peak dots.
 */
private fun DrawScope.drawSpectrumBars(
    amplitudes: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    isPlaying: Boolean,
    phase: Float
) {
    val totalWidth = size.width
    val canvasHeight = size.height
    val count = amplitudes.size.coerceIn(16, 32)
    val barSpacing = totalWidth / (count * 1.55f)
    val barWidth = barSpacing * 0.72f
    val startX = (totalWidth - (count * (barWidth + barSpacing) - barSpacing)) / 2f

    val gradient = Brush.verticalGradient(
        colors = listOf(
            accentColor,
            secondaryColor,
            primaryColor
        )
    )

    for (i in 0 until count) {
        val rawAmp = amplitudes.getOrElse(i) { 0.15f }
        // Subtle natural oscillation when playing
        val oscillation = if (isPlaying) sin(phase + i * 0.35f) * 0.05f else 0f
        val amp = (rawAmp + oscillation).coerceIn(0.08f, 1.0f)

        val barH = canvasHeight * amp * 0.88f
        val x = startX + i * (barWidth + barSpacing)
        val y = (canvasHeight - barH) / 2f

        // Draw dynamic glowing drop bar
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(x, y),
            size = Size(barWidth, barH),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Peak Hold Caps (Studio Audio Rack Effect)
        if (isPlaying && amp > 0.35f) {
            val peakY = (y - 5.dp.toPx()).coerceAtLeast(2.dp.toPx())
            drawCircle(
                color = accentColor.copy(alpha = 0.9f),
                radius = barWidth * 0.45f,
                center = Offset(x + barWidth / 2f, peakY)
            )

            // Bottom mirror peak
            val bottomPeakY = (y + barH + 5.dp.toPx()).coerceAtMost(canvasHeight - 2.dp.toPx())
            drawCircle(
                color = primaryColor.copy(alpha = 0.7f),
                radius = barWidth * 0.35f,
                center = Offset(x + barWidth / 2f, bottomPeakY)
            )
        }
    }
}

/**
 * 2. NEON WAVE: Dual continuous flowing ribbons with translucent underbelly and glowing crests.
 */
private fun DrawScope.drawNeonWave(
    amplitudes: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    phase: Float,
    isPlaying: Boolean
) {
    val w = size.width
    val h = size.height
    val centerY = h / 2f
    val points = amplitudes.size.coerceAtLeast(16)

    // Wave 1: Primary Cyan/Violet Ribbon
    val wavePath1 = Path()
    val fillPath1 = Path()

    wavePath1.moveTo(0f, centerY)
    fillPath1.moveTo(0f, h)
    fillPath1.lineTo(0f, centerY)

    val stepX = w / (points - 1).toFloat()

    for (i in 0 until points) {
        val amp = amplitudes.getOrElse(i) { 0.2f }
        val waveMod = if (isPlaying) sin(phase + i * 0.45f) * 12.dp.toPx() else 0f
        val y = centerY - (amp * (h * 0.42f)) + waveMod
        val x = i * stepX

        if (i == 0) {
            wavePath1.moveTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevAmp = amplitudes.getOrElse(i - 1) { 0.2f }
            val prevMod = if (isPlaying) sin(phase + (i - 1) * 0.45f) * 12.dp.toPx() else 0f
            val prevY = centerY - (prevAmp * (h * 0.42f)) + prevMod

            val cx = (prevX + x) / 2f
            wavePath1.quadraticTo(prevX, prevY, cx, (prevY + y) / 2f)
            fillPath1.lineTo(x, y)
        }
    }
    fillPath1.lineTo(w, h)
    fillPath1.close()

    // Draw wave 1 fill & stroke
    drawPath(
        path = fillPath1,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.25f),
                secondaryColor.copy(alpha = 0.08f),
                Color.Transparent
            )
        )
    )

    drawPath(
        path = wavePath1,
        brush = Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, accentColor)
        ),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Wave 2: Phase-Shifted Neon Pink Counter-Wave
    val wavePath2 = Path()
    for (i in 0 until points) {
        val amp = amplitudes.getOrElse((points - 1 - i)) { 0.15f }
        val waveMod = if (isPlaying) cos(phase * 1.2f + i * 0.5f) * 8.dp.toPx() else 0f
        val y = centerY + (amp * (h * 0.35f)) - waveMod
        val x = i * stepX

        if (i == 0) {
            wavePath2.moveTo(x, y)
        } else {
            val prevX = (i - 1) * stepX
            val prevAmp = amplitudes.getOrElse((points - i)) { 0.15f }
            val prevMod = if (isPlaying) cos(phase * 1.2f + (i - 1) * 0.5f) * 8.dp.toPx() else 0f
            val prevY = centerY + (prevAmp * (h * 0.35f)) - prevMod

            val cx = (prevX + x) / 2f
            wavePath2.quadraticTo(prevX, prevY, cx, (prevY + y) / 2f)
        }
    }

    drawPath(
        path = wavePath2,
        brush = Brush.horizontalGradient(
            colors = listOf(accentColor.copy(alpha = 0.85f), secondaryColor.copy(alpha = 0.7f))
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * 3. CIRCULAR RADIAL PULSE: 360-degree frequency needles radiating outward.
 */
private fun DrawScope.drawRadialPulse(
    amplitudes: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    phase: Float,
    isPlaying: Boolean,
    bassEnergy: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.height.coerceAtMost(size.width) / 2f) - 6.dp.toPx()
    val baseRadius = maxRadius * 0.38f + (if (isPlaying) bassEnergy * 8.dp.toPx() else 0f)

    // Glowing core pulsating circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = 0.3f * bassEnergy.coerceIn(0.3f, 1f)),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 1.4f
        ),
        radius = baseRadius * 1.4f,
        center = center
    )

    drawCircle(
        color = Color(0xFF161B2A),
        radius = baseRadius,
        center = center
    )

    drawCircle(
        color = primaryColor.copy(alpha = 0.6f),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    val count = amplitudes.size.coerceIn(24, 48)
    val angleStep = (2f * PI.toFloat()) / count

    for (i in 0 until count) {
        val angle = i * angleStep + (if (isPlaying) phase * 0.2f else 0f)
        val amp = amplitudes.getOrElse(i % amplitudes.size) { 0.2f }
        val needleLength = (maxRadius - baseRadius) * amp * 0.95f

        val innerX = center.x + baseRadius * cos(angle)
        val innerY = center.y + baseRadius * sin(angle)
        val outerX = center.x + (baseRadius + needleLength) * cos(angle)
        val outerY = center.y + (baseRadius + needleLength) * sin(angle)

        val needleColor = if (i % 3 == 0) accentColor else if (i % 2 == 0) primaryColor else secondaryColor

        drawLine(
            color = needleColor.copy(alpha = 0.85f),
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Pulsing outer dot
        if (amp > 0.45f && isPlaying) {
            drawCircle(
                color = needleColor,
                radius = 2.dp.toPx(),
                center = Offset(outerX, outerY)
            )
        }
    }
}

/**
 * 4. ENERGY PARTICLES: Audio constellation nodes reacting to intensity.
 */
private fun DrawScope.drawEnergyParticles(
    amplitudes: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    phase: Float,
    isPlaying: Boolean,
    bassEnergy: Float
) {
    val w = size.width
    val h = size.height
    val count = amplitudes.size.coerceIn(16, 28)

    val nodePositions = mutableListOf<Offset>()

    for (i in 0 until count) {
        val amp = amplitudes.getOrElse(i) { 0.2f }
        val normX = (i + 0.5f) / count.toFloat()
        val driftX = if (isPlaying) sin(phase + i * 0.8f) * 16.dp.toPx() else 0f
        val driftY = if (isPlaying) cos(phase * 1.3f + i * 0.6f) * (amp * 28.dp.toPx()) else 0f

        val x = (normX * w + driftX).coerceIn(10f, w - 10f)
        val y = (h / 2f + driftY).coerceIn(10f, h - 10f)
        val pos = Offset(x, y)
        nodePositions.add(pos)

        val nodeRadius = (3.dp.toPx() + amp * 5.dp.toPx()) * (if (isPlaying) 1f else 0.6f)
        val nodeColor = when (i % 3) {
            0 -> primaryColor
            1 -> accentColor
            else -> secondaryColor
        }

        // Halo
        drawCircle(
            color = nodeColor.copy(alpha = 0.25f * amp),
            radius = nodeRadius * 2.2f,
            center = pos
        )

        // Center Node
        drawCircle(
            color = nodeColor,
            radius = nodeRadius,
            center = pos
        )
    }

    // Connect neighbouring nodes with neon constellation filament lines
    for (i in 0 until nodePositions.size - 1) {
        val p1 = nodePositions[i]
        val p2 = nodePositions[i + 1]
        val amp = amplitudes.getOrElse(i) { 0.2f }

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(primaryColor.copy(alpha = 0.4f * amp), accentColor.copy(alpha = 0.4f * amp)),
                start = p1,
                end = p2
            ),
            start = p1,
            end = p2,
            strokeWidth = 1.2.dp.toPx()
        )
    }
}
