package com.example.ai

data class AgentAction(
    val toolName: String, // e.g. "SET_EQUALIZER", "SMART_SHUFFLE", "PLAY_TRACK", "DOWNLOAD_TRACKS", "SYNC_DEVICES", "NAVIGATE"
    val parameters: Map<String, String> = emptyMap(),
    val description: String
)

data class AgentExecutionReport(
    val thoughtProcess: String,
    val actions: List<AgentAction>,
    val agentSpeech: String
)

data class VoiceCommandResult(
    val action: String,
    val target: String,
    val description: String
)
