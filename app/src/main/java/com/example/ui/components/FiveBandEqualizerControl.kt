package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset

// Audiophile Dark Steel & Amber Color Palette
private val DarkSteelSurface = Color(0xFF10141D)
private val DarkSteelPanel = Color(0xFF181F2C)
private val DarkSteelCard = Color(0xFF141923)
private val DarkSteelBorder = Color(0xFF2B3649)
private val DarkSteelGroove = Color(0xFF0A0D13)

private val WarmAmberGlow = Color(0xFFFF9500)
private val BrightAmberLed = Color(0xFFFFB703)
private val DeepAmberGold = Color(0xFFD97706)
private val AmberMuted = Color(0xFF8A5A12)

/**
 * Tactical 5-Band Equalizer UI Control with dedicated preset buttons for
 * the 5 requested common profiles: Bass Boost, Vocal Clarity, Rock, Jazz, Flat.
 */
@Composable
fun FiveBandEqualizerControl(
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
    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandDescriptions = listOf("Sub-Bass", "Bass", "Mids", "Presence", "Air")

    var isBypassed by remember { mutableStateOf(false) }

    // Pre-resolve the 5 required profiles from allPresets with robust fallbacks
    val bassBoostPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_bass_boost" || it.name.equals("Bass Boost", ignoreCase = true) }
            ?: EqualizerPreset(
                id = "preset_bass_boost",
                name = "Bass Boost",
                band0 = 9f, band1 = 6f, band2 = 1f, band3 = -1f, band4 = 1f,
                bassBoost = 750, virtualizer = 250
            )
    }

    val vocalClarityPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_vocal" || it.name.equals("Vocal Clarity", ignoreCase = true) }
            ?: EqualizerPreset(
                id = "preset_vocal",
                name = "Vocal Clarity",
                band0 = -3f, band1 = 0f, band2 = 6f, band3 = 4f, band4 = 1.5f,
                bassBoost = 100, virtualizer = 200
            )
    }

    val rockPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_rock" || it.name.contains("Rock", ignoreCase = true) }
            ?: EqualizerPreset(
                id = "preset_rock",
                name = "Rock",
                band0 = 5.5f, band1 = 3.0f, band2 = -1.5f, band3 = 3.5f, band4 = 5.5f,
                bassBoost = 450, virtualizer = 350
            )
    }

    val jazzPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_jazz" || it.name.contains("Jazz", ignoreCase = true) }
            ?: EqualizerPreset(
                id = "preset_jazz",
                name = "Jazz",
                band0 = 3.5f, band1 = 2.0f, band2 = 1.0f, band3 = 2.5f, band4 = 2.0f,
                bassBoost = 250, virtualizer = 400
            )
    }

    val flatPreset = remember(allPresets) {
        allPresets.find { it.id == "preset_flat" || it.name.contains("Flat", ignoreCase = true) }
            ?: EqualizerPreset(
                id = "preset_flat",
                name = "Flat",
                band0 = 0f, band1 = 0f, band2 = 0f, band3 = 0f, band4 = 0f,
                bassBoost = 0, virtualizer = 0
            )
    }

    val fivePresets = listOf(
        Triple("Bass Boost", "+9dB Sub", bassBoostPreset),
        Triple("Vocal Clarity", "+6dB Mid", vocalClarityPreset),
        Triple("Rock", "V-Curve", rockPreset),
        Triple("Jazz", "Warm Silk", jazzPreset),
        Triple("Flat", "Linear 0dB", flatPreset)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("five_band_equalizer_control")
    ) {
        // Top Hardware Chassis Header & Power Switch
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSteelPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, DarkSteelBorder, RoundedCornerShape(18.dp))
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
                                .background(Brush.radialGradient(listOf(BrightAmberLed, DeepAmberGold, DarkSteelGroove)))
                                .border(1.dp, WarmAmberGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "EQ Engine",
                                tint = DarkSteelSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "5-BAND STUDIO EQUALIZER",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = BrightAmberLed
                            )
                            Text(
                                text = "Parametric Biquad Filters • ±12 dB Precision",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Bypass Hardware Toggle Switch
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBypassed) WarmAmberGlow.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (!isBypassed) WarmAmberGlow else DarkSteelBorder, RoundedCornerShape(10.dp))
                            .clickable { isBypassed = !isBypassed }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("eq_bypass_toggle")
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
                                fontWeight = FontWeight.ExtraBold,
                                color = if (!isBypassed) BrightAmberLed else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dedicated Preset Buttons Row for the 5 Common Profiles
        Text(
            text = "COMMON DSP PROFILES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = BrightAmberLed,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preset_buttons_row"),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            fivePresets.forEach { (label, subtitle, preset) ->
                val isSelected = activePreset.id == preset.id ||
                        activePreset.name.equals(preset.name, ignoreCase = true) ||
                        (label == "Flat" && activePreset.name.contains("Flat", true)) ||
                        (label == "Rock" && activePreset.name.contains("Rock", true))

                EqualizerProfileButton(
                    title = label,
                    subtitle = subtitle,
                    isSelected = isSelected,
                    onClick = { onSelectPreset(preset) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5 Frequency Band Sliders Chassis
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSteelPanel),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, DarkSteelBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FREQUENCY RESPONSE CURVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = BrightAmberLed
                    )

                    Text(
                        text = "Range: -12.0 to +12.0 dB",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The 5 Vertical Sliders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .testTag("sliders_container"),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (index in 0 until 5) {
                        val gainDb = currentGains.getOrElse(index) { 0f }
                        val freq = bandFrequencies.getOrElse(index) { "${index + 1}" }
                        val desc = bandDescriptions.getOrElse(index) { "" }

                        VerticalBandSlider(
                            bandIndex = index,
                            freqLabel = freq,
                            descLabel = desc,
                            gainDb = if (isBypassed) 0f else gainDb,
                            isBypassed = isBypassed,
                            onGainChange = { newGain -> onBandGainChange(index, newGain) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Harmonics & Spatial Audio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bass Boost Sub-harmonic Dial Card
            AudioEnhancementCard(
                title = "Bass Boost",
                subtitle = "Sub-Harmonics",
                value = if (isBypassed) 0 else bassBoostStrength,
                maxValue = 1000,
                displayValue = "${((if (isBypassed) 0 else bassBoostStrength) / 10)}%",
                onValueChange = { onBassBoostChange(it) },
                icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.VolumeUp,
                testTag = "bass_boost_slider",
                modifier = Modifier.weight(1f)
            )

            // 3D Virtualizer Card
            AudioEnhancementCard(
                title = "3D Surround",
                subtitle = "Soundstage",
                value = if (isBypassed) 0 else virtualizerStrength,
                maxValue = 1000,
                displayValue = "${((if (isBypassed) 0 else virtualizerStrength) / 10)}%",
                onValueChange = { onVirtualizerChange(it) },
                icon = Icons.Default.SurroundSound,
                testTag = "virtualizer_slider",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Tactical Preset Button for the 5 common profiles (Bass Boost, Vocal Clarity, Rock, Jazz, Flat).
 */
@Composable
private fun EqualizerProfileButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        if (isSelected) WarmAmberGlow else DarkSteelCard,
        label = "presetBg"
    )

    val animatedTextColor by animateColorAsState(
        if (isSelected) DarkSteelSurface else Color.White,
        label = "presetText"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) BrightAmberLed else DarkSteelBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("preset_button_${title.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = animatedTextColor,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) DarkSteelSurface.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Individual vertical slider column for a single frequency band.
 */
@Composable
private fun VerticalBandSlider(
    bandIndex: Int,
    freqLabel: String,
    descLabel: String,
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
        // Glowing Amber Nixie readout pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSteelGroove)
                .border(
                    width = 1.dp,
                    color = if (!isBypassed) WarmAmberGlow.copy(alpha = 0.6f) else DarkSteelBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = formattedGain,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (!isBypassed) BrightAmberLed else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Slider Track
        Box(
            modifier = Modifier
                .weight(1f)
                .width(38.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = 5.dp.toPx()
                val centerX = size.width / 2f
                val topY = 8.dp.toPx()
                val bottomY = size.height - 8.dp.toPx()
                val centerY = (topY + bottomY) / 2f

                // Track groove
                drawRoundRect(
                    color = DarkSteelGroove,
                    topLeft = Offset(centerX - trackWidth / 2f, topY),
                    size = Size(trackWidth, bottomY - topY),
                    cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f)
                )

                // Zero line (0 dB reference)
                drawLine(
                    color = WarmAmberGlow.copy(alpha = 0.45f),
                    start = Offset(centerX - 8.dp.toPx(), centerY),
                    end = Offset(centerX + 8.dp.toPx(), centerY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Slider rotated 90 degrees CCW
            Slider(
                value = gainDb,
                onValueChange = { if (!isBypassed) onGainChange(it) },
                valueRange = -12f..12f,
                colors = SliderDefaults.colors(
                    thumbColor = if (!isBypassed) BrightAmberLed else Color.Gray,
                    activeTrackColor = if (!isBypassed) WarmAmberGlow else Color.DarkGray,
                    inactiveTrackColor = DarkSteelGroove
                ),
                modifier = Modifier
                    .rotate(-90f)
                    .fillMaxSize()
                    .testTag("slider_band_$bandIndex")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency Label & Classification
        Text(
            text = freqLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = descLabel,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

/**
 * Card for Bass Boost or 3D Virtualizer strength adjustment.
 */
@Composable
private fun AudioEnhancementCard(
    title: String,
    subtitle: String,
    value: Int,
    maxValue: Int,
    displayValue: String,
    onValueChange: (Int) -> Unit,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSteelPanel),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, DarkSteelBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmAmberGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = subtitle,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Text(
                    text = displayValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = BrightAmberLed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..maxValue.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = BrightAmberLed,
                    activeTrackColor = WarmAmberGlow,
                    inactiveTrackColor = DarkSteelGroove
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            )
        }
    }
}
