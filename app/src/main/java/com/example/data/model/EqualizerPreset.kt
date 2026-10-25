package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a studio DSP Equalizer Preset.
 * Contains 5 frequency band gains (-12dB to +12dB), Bass Boost sub-harmonic strength (0-1000),
 * and 3D Spatial Virtualizer strength (0-1000), along with genre categorization.
 */
@Entity(tableName = "equalizer_presets")
data class EqualizerPreset(
    @PrimaryKey val id: String,
    val name: String,
    val genre: String = "General",
    val description: String = "",
    val band0: Float = 0f, // 60 Hz (-12dB to +12dB) Sub-Bass
    val band1: Float = 0f, // 230 Hz Bass
    val band2: Float = 0f, // 910 Hz Midrange
    val band3: Float = 0f, // 3.6 kHz High-Mids
    val band4: Float = 0f, // 14 kHz Treble / Air
    val bassBoost: Int = 0, // 0 to 1000
    val virtualizer: Int = 0, // 0 to 1000
    val isCustom: Boolean = false
) {
    fun toGainsList(): List<Float> = listOf(band0, band1, band2, band3, band4)

    companion object {
        val DEFAULT_PRESETS = listOf(
            EqualizerPreset(
                id = "preset_bass_boost",
                name = "Bass Boost",
                genre = "Bass & Club",
                description = "Maximum low-end punch and sub-harmonic elevation for intense beats and club tracks.",
                band0 = 9f, band1 = 6f, band2 = 1f, band3 = -1f, band4 = 1f,
                bassBoost = 750, virtualizer = 250
            ),
            EqualizerPreset(
                id = "preset_acoustic",
                name = "Acoustic",
                genre = "Acoustic & Folk",
                description = "Warm organic resonance, natural wooden guitar body depth, and articulate string air.",
                band0 = 4.5f, band1 = 2.5f, band2 = 1.0f, band3 = 3.0f, band4 = 4.5f,
                bassBoost = 200, virtualizer = 350
            ),
            EqualizerPreset(
                id = "preset_jazz",
                name = "Jazz",
                genre = "Jazz & Soul",
                description = "Silky smooth brass presence, warm upright bass resonance, and intimate acoustic soundstage.",
                band0 = 3.5f, band1 = 2.0f, band2 = 1.0f, band3 = 2.5f, band4 = 2.0f,
                bassBoost = 250, virtualizer = 400
            ),
            EqualizerPreset(
                id = "preset_edm",
                name = "Cyber EDM",
                genre = "Electronic & Dance",
                description = "Deep synthetic sub-drops paired with ultra-crisp synth leads and wide stereo spatialization.",
                band0 = 7.5f, band1 = 3.5f, band2 = -2.0f, band3 = 4.5f, band4 = 6.5f,
                bassBoost = 600, virtualizer = 650
            ),
            EqualizerPreset(
                id = "preset_rock",
                name = "Rock Stage",
                genre = "Rock & Metal",
                description = "Aggressive electric guitar distortion drive, punchy drum transients, and screaming presence.",
                band0 = 5.5f, band1 = 3.0f, band2 = -1.5f, band3 = 3.5f, band4 = 5.5f,
                bassBoost = 450, virtualizer = 350
            ),
            EqualizerPreset(
                id = "preset_classical",
                name = "Classical",
                genre = "Classical & Symphonic",
                description = "Expansive concert hall acoustics, lush string sections, and breathtaking dynamic headroom.",
                band0 = 3.0f, band1 = 1.0f, band2 = -1.0f, band3 = 3.0f, band4 = 5.0f,
                bassBoost = 150, virtualizer = 550
            ),
            EqualizerPreset(
                id = "preset_hip_hop",
                name = "Hip-Hop",
                genre = "Bass & Club",
                description = "Deep 808 rumble, punchy snare cracks, and upfront vocal delivery for modern rap and trap.",
                band0 = 8.5f, band1 = 5.0f, band2 = 0.5f, band3 = 2.0f, band4 = 3.5f,
                bassBoost = 700, virtualizer = 200
            ),
            EqualizerPreset(
                id = "preset_pop",
                name = "Pop Energy",
                genre = "Pop & Commercial",
                description = "Bright, radio-ready polish with punchy modern bass and glittering commercial high-end.",
                band0 = 3.5f, band1 = 2.0f, band2 = 3.5f, band3 = 3.0f, band4 = 3.5f,
                bassBoost = 300, virtualizer = 250
            ),
            EqualizerPreset(
                id = "preset_vocal",
                name = "Vocal Clarity",
                genre = "Vocal & Spoken",
                description = "Clear speech intelligibility, low-end proximity rumble reduction, and focused mid presence.",
                band0 = -3.0f, band1 = 0.0f, band2 = 6.0f, band3 = 4.0f, band4 = 1.5f,
                bassBoost = 100, virtualizer = 200
            ),
            EqualizerPreset(
                id = "preset_chill",
                name = "Chill Lo-Fi",
                genre = "Lo-Fi & Ambient",
                description = "Cozy tape saturation warmth, gently rolled-off digital harshness, and relaxing atmospheric low-end.",
                band0 = 4.0f, band1 = 1.5f, band2 = -2.0f, band3 = -1.0f, band4 = -2.5f,
                bassBoost = 400, virtualizer = 500
            ),
            EqualizerPreset(
                id = "preset_flat",
                name = "Studio Flat",
                genre = "Studio Reference",
                description = "Uncolored bit-perfect linear frequency response engineered for master audio reference.",
                band0 = 0f, band1 = 0f, band2 = 0f, band3 = 0f, band4 = 0f,
                bassBoost = 0, virtualizer = 0
            )
        )
    }
}
