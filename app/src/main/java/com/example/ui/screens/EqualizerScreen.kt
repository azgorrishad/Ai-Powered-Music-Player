package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DeepObsidianNoir
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.SpaceGroteskFamily
import kotlin.math.cos
import kotlin.math.sin

// Analog Sound Lab Luxury Noir & Imperial Amber/Gold Colors
private val DarkSteelBase = DeepObsidianNoir
private val DarkSteelPanel = Color(0xFF0E1424)
private val DarkSteelBorder = DarkSurfaceBorder
private val DarkSteelHighlight = Color(0xFF2A364F)

private val WarmAmberGlow = ImperialGold
private val BrightAmberLed = ChampagneGold
private val DeepAmberGold = Color(0xFFD97706)
private val AmberBackgroundText = Color(0x22E5B869)

/**
 * Analog Sound Lab 5-Band Equalizer Screen with dark steel styling, amber indicators,
 * and quick-preset buttons for 'Studio', 'Acoustic', and 'Bass'.
 */
@Composable
fun EqualizerScreen(
    activePreset: EqualizerPreset,
    allPresets: List<EqualizerPreset>,
    currentGains: List<Float>,
    bassBoostStrength: Int,
    virtualizerStrength: Int,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandTitles = listOf("Sub-Bass", "Punch", "Mids", "Presence", "Air")

    var isBypassed by remember { mutableStateOf(false) }

    // Presets for Studio, Acoustic, Bass
    val studioPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_flat" || it.name.contains("Studio", ignoreCase = true) || it.name.contains("Flat", ignoreCase = true) }
            ?: EqualizerPreset(id = "preset_flat", name = "Studio", band0 = 0f, band1 = 0f, band2 = 0f, band3 = 0f, band4 = 0f)
    }

    val acousticPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_acoustic" || it.name.contains("Acoustic", ignoreCase = true) }
            ?: EqualizerPreset(id = "preset_acoustic", name = "Acoustic", band0 = 4.5f, band1 = 2.5f, band2 = 1f, band3 = 3f, band4 = 4.5f)
    }

    val bassPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_bass_boost" || it.name.contains("Bass", ignoreCase = true) }
            ?: EqualizerPreset(id = "preset_bass_boost", name = "Bass", band0 = 9f, band1 = 6f, band2 = 1f, band3 = -1f, band4 = 1f, bassBoost = 750)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSteelBase)
            .testTag("equalizer_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // Header Panel: Analog Sound Lab Banner & Vacuum Tube Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSteelPanel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkSteelBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Amber Vacuum Tube Glow Circle
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(BrightAmberLed, DeepAmberGold, DarkSteelPanel)
                                        )
                                    )
                                    .border(1.dp, WarmAmberGlow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Analog Sound Lab",
                                    tint = DarkSteelBase,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "ANALOG SOUND LAB",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = BrightAmberLed
                                )
                                Text(
                                    text = "Vacuum Tube Biquad DSP • Studio Reference Engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Power / Bypass Switch
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isBypassed) WarmAmberGlow.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (!isBypassed) WarmAmberGlow else DarkSteelBorder, RoundedCornerShape(12.dp))
                                .clickable { isBypassed = !isBypassed }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (!isBypassed) BrightAmberLed else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!isBypassed) "ACTIVE" else "BYPASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isBypassed) BrightAmberLed else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Analog VU Needle Meter
                    AnalogVuMeter(isBypassed = isBypassed)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 2: 5-Band Equalizer UI Control with Common Profiles (Bass Boost, Vocal Clarity, Rock, Jazz, Flat)
        item {
            com.example.ui.components.FiveBandEqualizerControl(
                activePreset = activePreset,
                allPresets = allPresets,
                currentGains = currentGains,
                bassBoostStrength = bassBoostStrength,
                virtualizerStrength = virtualizerStrength,
                onSelectPreset = onSelectPreset,
                onBandGainChange = onBandGainChange,
                onBassBoostChange = onBassBoostChange,
                onVirtualizerChange = onVirtualizerChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 3: Extended DSP Library Presets
        item {
            Text(
                text = "ALL DSP SOUND PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = BrightAmberLed,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allPresets) { preset ->
                    val isSelected = preset.id == activePreset.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmAmberGlow,
                            selectedLabelColor = DarkSteelBase,
                            containerColor = DarkSteelPanel,
                            labelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkSteelBorder,
                            selectedBorderColor = BrightAmberLed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Preset Quick Select Button styled for Dark Steel with Amber Indicator
 */
@Composable
private fun PresetQuickButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        if (isSelected) WarmAmberGlow else DarkSteelPanel,
        label = "presetBg"
    )

    val animatedText by animateColorAsState(
        if (isSelected) DarkSteelBase else Color.White,
        label = "presetText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BrightAmberLed else DarkSteelBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = animatedText
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) DarkSteelBase.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Single Vertical Slider Column with Amber dB readout and Amber indicator glow
 */
@Composable
private fun AnalogBandSliderColumn(
    bandIndex: Int,
    frequencyLabel: String,
    titleLabel: String,
    gainDb: Float,
    isBypassed: Boolean,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedGain = if (gainDb > 0f) "+%.1f dB".format(gainDb) else "%.1f dB".format(gainDb)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight()
    ) {
        // Glowing Amber Nixie-style dB Display Box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSteelBase)
                .border(1.dp, if (!isBypassed) WarmAmberGlow.copy(alpha = 0.5f) else DarkSteelBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = formattedGain,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (!isBypassed) BrightAmberLed else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vertical Slider Control
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Slider Track Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = 6.dp.toPx()
                val centerX = size.width / 2f
                val topY = 10.dp.toPx()
                val bottomY = size.height - 10.dp.toPx()

                // Dark steel groove
                drawRoundRect(
                    color = DarkSteelBase,
                    topLeft = Offset(centerX - trackWidth / 2f, topY),
                    size = Size(trackWidth, bottomY - topY),
                    cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f)
                )

                // Zero line indicator in center
                val centerY = (topY + bottomY) / 2f
                drawLine(
                    color = WarmAmberGlow.copy(alpha = 0.4f),
                    start = Offset(centerX - 10.dp.toPx(), centerY),
                    end = Offset(centerX + 10.dp.toPx(), centerY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Slider Component with -12dB to +12dB Range
            // Note: Material 3 Slider is horizontal, rotated -90 degrees for vertical layout
            Slider(
                value = gainDb,
                onValueChange = { if (!isBypassed) onGainChange(it) },
                valueRange = -12f..12f,
                colors = SliderDefaults.colors(
                    thumbColor = if (!isBypassed) BrightAmberLed else Color.Gray,
                    activeTrackColor = if (!isBypassed) WarmAmberGlow else Color.DarkGray,
                    inactiveTrackColor = DarkSteelBase
                ),
                modifier = Modifier
                    .rotate(-90f)
                    .fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Label
        Text(
            text = frequencyLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = titleLabel,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

/**
 * Tactile Dial Card for Bass Boost or 3D Virtualizer
 */
@Composable
private fun AnalogControlDialCard(
    title: String,
    subtitle: String,
    value: Int,
    maxValue: Int,
    unit: String,
    displayValue: String,
    onValueChange: (Int) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSteelBase),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, DarkSteelBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WarmAmberGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display readout
            Text(
                text = displayValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = BrightAmberLed
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Slider for control
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..maxValue.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = BrightAmberLed,
                    activeTrackColor = WarmAmberGlow,
                    inactiveTrackColor = DarkSteelPanel
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Animated Analog Dual VU Needle Meter
 */
@Composable
private fun AnalogVuMeter(isBypassed: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "vuMeter")
    val needleOscillation by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "needleOsc"
    )

    val activeNeedleAngle = if (isBypassed) 0f else (needleOscillation * 35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSteelBase)
            .border(1.dp, DarkSteelBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Dual Meters (Left and Right)
            val meterWidth = width / 2f - 12.dp.toPx()

            for (meterIndex in 0..1) {
                val meterLeft = if (meterIndex == 0) 0f else width / 2f + 12.dp.toPx()
                val meterCenterX = meterLeft + meterWidth / 2f
                val meterCenterY = height + 10.dp.toPx()

                // Draw Meter Arc Scale
                drawArc(
                    color = WarmAmberGlow.copy(alpha = 0.2f),
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(meterLeft + 10.dp.toPx(), 10.dp.toPx()),
                    size = Size(meterWidth - 20.dp.toPx(), height * 1.4f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Needle Line
                val needleAngleRad = Math.toRadians((270 + activeNeedleAngle + (meterIndex * 5f)).toDouble())
                val needleLength = height * 0.75f
                val needleEnd = Offset(
                    (meterCenterX + needleLength * cos(needleAngleRad)).toFloat(),
                    (meterCenterY + needleLength * sin(needleAngleRad)).toFloat()
                )

                drawLine(
                    color = if (!isBypassed) BrightAmberLed else Color.Gray,
                    start = Offset(meterCenterX, meterCenterY),
                    end = needleEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("L CHANNEL VU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WarmAmberGlow.copy(alpha = 0.7f))
            Text("ANALOG LEVEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrightAmberLed)
            Text("R CHANNEL VU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WarmAmberGlow.copy(alpha = 0.7f))
        }
    }
}
