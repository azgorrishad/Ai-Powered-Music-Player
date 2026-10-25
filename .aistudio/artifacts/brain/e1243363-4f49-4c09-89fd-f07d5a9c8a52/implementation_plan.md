# PulseFlow - Intelligent Mood & Stream Music Player

PulseFlow is a next-generation Android music player that unites local device audio with online streaming, powered by Gemini AI algorithms that understand human psychology and emotional states to dynamically adapt playback, playlists, and smart shuffling. It features a studio-grade 5-band equalizer, real-time waveform visualizers, unified voice search, deep listening analytics, and social story sharing.

## User Review & Critical Decisions

> [!IMPORTANT]
> Please review the architecture decisions below. The system is designed to seamlessly integrate local audio files, cloud/streaming audio sources, and multi-tier Gemini intelligence.

- **Audio Playback Engine**: Android `MediaPlayer` / `ExoPlayer` audio pipeline supporting local device storage (`MediaStore`), web streaming audio endpoints, and cached offline playback.
- **AI Intelligence Hierarchy**:
  - `gemini-3.1-flash-lite`: Sub-second smart shuffle decisions and dynamic song-to-song mood transitions.
  - `gemini-3.8-flash`: General discovery, search grounding for trending music, and conversational Music Assistant chatbot.
  - `gemini-3.5-transcribe`: Voice search speech-to-text and hands-free player commands.
  - `gemini-4-argon`: High-thinking deep psychological mood profiling (arousal/valence music therapy principles, circadian rhythms, emotional regulation).
- **Audio DSP & Customization**: 5-band Equalizer with presets (Bass Boost, Acoustic, Cyber-Bass, Vocal Boost, Rock, Electronic, Chill) and custom sliders with persistent profiles.
- **Persistence & Offline Mode**: Device-local Room database caching library metadata, playlists, mood tags, playback analytics, and equalizer settings, combined with offline audio caching.

---

## 1. Overview & Core Concept

- **What It Does**: PulseFlow is an all-in-one music companion that bridges the gap between stored local music and streaming discovery. Users can listen to tracks, organize playlists, adjust audio frequencies via a visual equalizer, and let an AI DJ craft custom playlists based on their current mood, activity, or emotional goal.
- **Target Audience / Persona**: Music enthusiasts, daily commuters, gym-goers, and focused workers who want tailored sound quality, zero friction between local and online tracks, and smart recommendations that match their mood.
- **Key Value**: Delivers audio fidelity with customizable DSP, intelligent shuffle algorithms that eliminate playlist fatigue, and a sleek dark/light aesthetic built strictly on Material 3.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Discovery & Smart Shuffle**:
   - Home tab presents "Current Mood Pulse" chips (Energetic, Deep Focus, Melancholic, Euphoric, Late Night Ambient, Workout Pump).
   - Tapping "Smart Shuffle" uses Gemini intelligence to calculate the optimal track progression based on energy, tempo (BPM), and psychological valence.
2. **Now Playing & Audio Visualizer**:
   - Immersive full-screen player with rotating vinyl artwork, synchronized waveform audio spectrum bars, lyrics panel, and gesture controls (swipe track to queue or skip).
   - Bottom mini-player accessible across all screens with fluid expand/collapse animations.
3. **Unified Voice & Text Search**:
   - Search bar with instant voice input button: records user speech and transcribes queries via `gemini-3.5-transcribe` to search both local tracks and streaming catalogs simultaneously.
4. **Equalizer & Sound Effects**:
   - Interactive 5-band sliders (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz) with pre-configured curves and visual gain response curves.
5. **Listening Analytics & Social Sharing**:
   - Comprehensive dashboard showing total listening minutes, mood distribution radar/pie chart, top artists, and peak listening hours.
   - One-tap "Share Story" generator creating stylized visual music cards ready for social sharing.
6. **Conversational AI DJ Chat**:
   - Dedicated interactive chat with an AI Musicologist to request bespoke playlist recipes, ask for music history, or generate listening themes.

### Visual Identity & Theme

- **Aesthetic Direction**: **Cyber-Acoustic Dark & Studio Minimalist**. Deep obsidian/slate backdrop (`#0D0F14`) accented by electric cyan (`#00E5FF`), luminous violet (`#A855F7`), and warm vinyl gold (`#F59E0B`). High contrast, crisp typography, and subtle glowing glass cards.
- **Light Theme**: Clean editorial porcelain (`#F8FAFC`) with deep indigo accents and subtle tonal elevation.
- **Typography**: Modern geometric sans-serif hierarchy with bold tabular numeric displays for timestamps and frequency sliders.
- **Interactive Feedback & Motion**:
  - Live animated waveform spectrum responding to playback state.
  - Spring-physics volume and seek scrubbing with haptic feedback.
  - Smooth bottom sheet transitions for queue, lyrics, and EQ.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid Audio Engine with Offline Cache**
  - *Chosen Approach*: Unified `AudioPlayerService` supporting local file URIs, pre-bundled high-fidelity royalty-free showcase tracks, streaming streams, and local cache.
  - *Why*: Ensures immediate, reliable playback in all network conditions, including airplane mode, while letting users access online streaming tracks.
- **Decision 2: Room Database for Local Persistence**
  - *Chosen Approach*: Multi-entity Room SQLite database for library tracks, favorite playlists, user mood logs, playback event timestamps, and custom EQ presets.
  - *Why*: Guaranteed fast, reactive UI updates via Kotlin Flow, offline-first reliability, zero latency for search and analytics.
- **Decision 3: Tiered Gemini Multi-Model Integration**
  - *Chosen Approach*: Direct REST API with secret key management via `BuildConfig.GEMINI_API_KEY`. Route voice commands to `gemini-3.5-transcribe`, rapid shuffle recommendations to `gemini-3.1-flash-lite`, and deep psychological playlist synthesis to `gemini-3.8-flash` / `gemini-4-argon`.
  - *Why*: Minimizes latency during playback actions while delivering depth for conversational DJ advice and emotional curation.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌───────────────┐ ┌────────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │  Home / Mood  │ │  Now Playing   │ │ Search &    │ │ Equalizer & │  │
│  │  Discovery    │ │  & Visualizer  │ │ Voice Bar   │ │ Analytics   │  │
│  └───────┬───────┘ └───────┬────────┘ └──────┬──────┘ └──────┬──────┘  │
└──────────┼─────────────────┼─────────────────┼───────────────┼─────────┘
           │                 │                 │               │
┌──────────▼─────────────────▼─────────────────▼───────────────▼─────────┐
│                        MusicPlayerViewModel                            │
│    - Player State (Playing, Paused, Buffering, Seek, Repeat, Shuffle)  │
│    - Current Queue, Active Mood, EQ Settings, Analytics Metrics       │
└──────────┬─────────────────┬─────────────────┬───────────────┬─────────┘
           │                 │                 │               │
┌──────────▼────────┐ ┌──────▼────────┐ ┌──────▼────────┐ ┌────▼────────┐
│   Audio Engine    │ │  Gemini AI    │ │ Room Database │ │ Voice Audio │
│  (MediaPlayer &   │ │  Repository   │ │ (Tracks, EQ,  │ │ Recorder &  │
│   DSP Equalizer)  │ │  (REST API)   │ │ Analytics)    │ │ Transcriber │
└───────────────────┘ └───────────────┘ └───────────────┘ └─────────────┘
```

### Data Model Entities

- `Track`: `id`, `title`, `artist`, `album`, `durationMs`, `audioUrl`, `artworkUrl`, `genre`, `moodTags`, `bpm`, `isLocal`, `isDownloaded`, `playCount`.
- `Playlist`: `id`, `name`, `description`, `moodCategory`, `trackIdsJson`, `createdAt`.
- `PlaybackHistoryItem`: `id`, `trackId`, `timestamp`, `listenedDurationMs`, `moodContext`, `completed`.
- `EqualizerPreset`: `id`, `name`, `bandGains` (List of Float dB), `bassBoost`, `virtualizer`.
- `AnalyticsData`: Total play time, top moods, peak listening hours, energy index, and weekly trends.
