package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AgentAction
import com.example.ai.AgentExecutionReport
import com.example.ai.BrainTelemetry
import com.example.ai.CognitiveState
import com.example.data.model.Track
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LuminousViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.VinylGold

@Composable
fun AiMaestroScreen(
    currentTrack: Track?,
    latestReport: AgentExecutionReport?,
    isThinking: Boolean,
    actionHistory: List<AgentAction>,
    onExecuteDirective: (String) -> Unit,
    onOpenVoiceSearch: () -> Unit,
    cognitiveState: CognitiveState = CognitiveState.IDLE,
    brainTelemetry: BrainTelemetry? = null,
    modifier: Modifier = Modifier
) {
    var directiveInput by remember { mutableStateOf("") }

    val presetDirectives = listOf(
        "Automate my workout: pump up bass to 80% & queue high-octane tracks",
        "Deep focus state: smooth acoustic frequencies & start binaural flow",
        "Winding down: set chill lo-fi with spatial virtualizer",
        "Harmonize library: run cross-device sync now",
        "Prepare flight: download current tracks for offline playback"
    )

    // Pulsing core animation for the AI Agent
    val infiniteTransition = rememberInfiniteTransition(label = "agent_orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_maestro_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Maestro",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(ElectricCyan, LuminousViolet)))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUTONOMOUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF00363D)
                            )
                        }
                    }
                    Text(
                        text = "Executive multi-modal control over audio DSP, queues, sync & offline storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Holographic AI Core Orb Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF080D1A),
                                    Color(0xFF140D2B),
                                    Color(0xFF05121F)
                                )
                            )
                        )
                        .border(1.2.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Animated AI Agent Core Canvas
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val baseRadius = size.width * 0.38f

                                // Outer glowing pulse ring
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(ElectricCyan.copy(alpha = 0.4f), Color.Transparent),
                                        center = center,
                                        radius = baseRadius * pulseScale * 1.3f
                                    ),
                                    radius = baseRadius * pulseScale * 1.3f,
                                    center = center
                                )

                                // Mid Violet Ring
                                drawCircle(
                                    color = LuminousViolet.copy(alpha = 0.6f),
                                    radius = baseRadius * pulseScale,
                                    center = center,
                                    style = Stroke(width = 2.5f)
                                )

                                // Inner Core
                                drawCircle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ElectricCyan, LuminousViolet)
                                    ),
                                    radius = baseRadius * 0.75f,
                                    center = center
                                )
                            }

                            Icon(
                                imageVector = if (isThinking) Icons.Default.Psychology else Icons.Default.SmartToy,
                                contentDescription = "AI Maestro Core",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isThinking) "AI Maestro Synthesizing Directives..." else "Agent Operational • Monitoring Audio Stream",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isThinking) VinylGold else ElectricCyan
                        )

                        Text(
                            text = currentTrack?.let { "Controlling playback of ${it.title} by ${it.artist}" } ?: "Ready for executive commands",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cognitive State Phase Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (cognitiveState) {
                                        CognitiveState.NEURAL_REASONING -> VinylGold.copy(alpha = 0.2f)
                                        CognitiveState.DISPATCHING_CONTROLLER_ACTIONS -> ElectricCyan.copy(alpha = 0.2f)
                                        CognitiveState.PERCEIVING_CONTEXT -> LuminousViolet.copy(alpha = 0.2f)
                                        CognitiveState.HARMONIZED -> ElectricCyan.copy(alpha = 0.15f)
                                        else -> Color.White.copy(alpha = 0.08f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PHASE: ${cognitiveState.name.replace('_', ' ')}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = when (cognitiveState) {
                                    CognitiveState.NEURAL_REASONING -> VinylGold
                                    CognitiveState.DISPATCHING_CONTROLLER_ACTIONS -> ElectricCyan
                                    CognitiveState.PERCEIVING_CONTEXT -> LuminousViolet
                                    CognitiveState.HARMONIZED -> ElectricCyan
                                    else -> Color.White.copy(alpha = 0.8f)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3-Pillar PulseAgent Architecture Flow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("1. INTENT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = ElectricCyan)
                                Text("User Directives", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("➔", color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 4.dp), fontSize = 12.sp)
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("2. GEMINI AI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = LuminousViolet)
                                Text("Context Reasoning", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("➔", color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 4.dp), fontSize = 12.sp)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("3. MEDIA3", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = VinylGold)
                                Text("Audio Controller", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (brainTelemetry != null && brainTelemetry.totalDirectivesProcessed > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = "Directives: ${brainTelemetry.totalDirectivesProcessed}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Actions: ${brainTelemetry.totalActionsDispatched}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Model: ${brainTelemetry.activeModel}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Directive Input Bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = directiveInput,
                    onValueChange = { directiveInput = it },
                    placeholder = { Text("Command the agent (e.g. 'boost bass to 80% and play workout beats')", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("maestro_directive_input")
                )

                IconButton(
                    onClick = onOpenVoiceSearch,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Directive", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = {
                        if (directiveInput.isNotBlank()) {
                            onExecuteDirective(directiveInput)
                            directiveInput = ""
                        }
                    },
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .testTag("submit_maestro_directive_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Execute", tint = Color(0xFF00363D), modifier = Modifier.size(20.dp))
                }
            }
        }

        // Quick Preset Directives
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "EXECUTIVE QUICK COMMANDS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetDirectives) { directive ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { onExecuteDirective(directive) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = VinylGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = directive, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Latest Execution Report Card (Thoughts + Action Receipts + Speech)
        if (latestReport != null) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().border(1.dp, LuminousViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = LuminousViolet, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AGENT REASONING & ACTIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LuminousViolet
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "EXECUTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Internal Thought Process
                        Text(
                            text = "“${latestReport.thoughtProcess}”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Receipts Timeline
                        Text(
                            text = "Invoked Tools:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            latestReport.actions.forEach { act ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${act.toolName}: ${act.description}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Agent Verbal Confirmation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Maestro: “${latestReport.agentSpeech}”",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // Live Action Log History
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "AGENT ACTIVITY LOG",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(actionHistory.reversed().take(10)) { act ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            act.toolName.contains("EQUALIZER") -> Icons.Default.Tune
                            act.toolName.contains("SHUFFLE") -> Icons.Default.Speed
                            else -> Icons.Default.Build
                        },
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = act.toolName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Text(
                            text = act.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
