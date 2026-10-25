package com.example.data.model

/**
 * Result model representing an AI-generated dynamic mood playlist.
 * Generated via OpenRouter / Gemini AI or rule-based cognitive heuristics.
 */
data class DynamicMoodPlaylist(
    val title: String,
    val description: String,
    val rationale: String,
    val targetMood: String,
    val targetBpmRange: String,
    val tracks: List<Track>,
    val energyLevel: Float = 0.5f,
    val valenceLevel: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis()
)
