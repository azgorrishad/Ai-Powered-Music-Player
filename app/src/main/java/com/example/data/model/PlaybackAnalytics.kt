package com.example.data.model

data class ListeningTrendPoint(
    val label: String, // e.g. "Mon", "Tue", "Week 1", etc.
    val minutes: Int
)

data class HourlyActivityPoint(
    val hour: Int, // 0 to 23
    val label: String, // e.g. "9 AM", "2 PM", "9 PM"
    val plays: Int
)

data class GenreInsight(
    val genre: String,
    val percentage: Float,
    val trackCount: Int
)

data class PlaybackAnalytics(
    val totalMinutesListened: Int = 184,
    val totalTracksPlayed: Int = 46,
    val topMood: String = "Focus",
    val topGenre: String = "Electronic",
    val topArtists: List<Pair<String, Int>> = listOf(
        "Aether Void" to 14,
        "Komorebi Beats" to 11,
        "Neural Resonance" to 9,
        "Hyperion Core" to 8,
        "Shibuya Midnight" to 4
    ),
    val mostPlayedTracks: List<Pair<String, Int>> = listOf(
        "Neon Horizons" to 14,
        "Midnight Reverie" to 11,
        "Solar Flare" to 9,
        "Tokyo Rain" to 7,
        "Quantum Flux" to 5
    ),
    val moodDistribution: Map<String, Float> = mapOf(
        "Focus" to 0.38f,
        "Chill" to 0.28f,
        "Energetic" to 0.20f,
        "Workout" to 0.14f
    ),
    val dailyTrends: List<ListeningTrendPoint> = listOf(
        ListeningTrendPoint("Mon", 25),
        ListeningTrendPoint("Tue", 40),
        ListeningTrendPoint("Wed", 15),
        ListeningTrendPoint("Thu", 35),
        ListeningTrendPoint("Fri", 50),
        ListeningTrendPoint("Sat", 65),
        ListeningTrendPoint("Sun", 45)
    ),
    val weeklyTrends: List<ListeningTrendPoint> = listOf(
        ListeningTrendPoint("Week 1", 180),
        ListeningTrendPoint("Week 2", 220),
        ListeningTrendPoint("Week 3", 195),
        ListeningTrendPoint("Week 4", 260)
    ),
    val monthlyTrends: List<ListeningTrendPoint> = listOf(
        ListeningTrendPoint("Jul", 620),
        ListeningTrendPoint("Aug", 780),
        ListeningTrendPoint("Sep", 840),
        ListeningTrendPoint("Oct", 950)
    ),
    val peakHours: List<HourlyActivityPoint> = listOf(
        HourlyActivityPoint(6, "6 AM", 2),
        HourlyActivityPoint(9, "9 AM", 12),
        HourlyActivityPoint(12, "12 PM", 8),
        HourlyActivityPoint(15, "3 PM", 15),
        HourlyActivityPoint(18, "6 PM", 18),
        HourlyActivityPoint(21, "9 PM", 26),
        HourlyActivityPoint(23, "11 PM", 14)
    ),
    val genreInsights: List<GenreInsight> = listOf(
        GenreInsight("Synthwave & Cyber", 0.36f, 18),
        GenreInsight("Lo-Fi & Chillhop", 0.28f, 14),
        GenreInsight("Binaural & Ambient", 0.22f, 11),
        GenreInsight("Electronic & Bass", 0.14f, 7)
    ),
    val averageValence: Float = 0.68f,
    val averageEnergy: Float = 0.62f,
    val listeningStreakDays: Int = 6,
    val peakHourWindow: String = "8:30 PM - 10:30 PM",
    val aiPsychologicalInsight: String = "Your listening frequency spikes during late evening hours, where mid-tempo synth harmonics lower pre-sleep cortisol while preserving relaxed cognitive reflection."
)
