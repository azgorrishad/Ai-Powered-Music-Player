AURALIS UI OVERHAUL PATCH

This is a source-level change script for the repository:
https://github.com/azgorrishad/Ai-Powered-Music-Player

WHAT IT CHANGES
- Replaces the over-saturated palette with warm monochrome and restrained champagne.
- Removes gold-gradient border treatment from shared section headings.
- Replaces the crowded home header and tiny featured track tile with a cleaner editorial header and larger artwork.
- Redesigns the persistent mini-player with clearer artwork, real controls, a slim progress line, and no unsupported HI-RES label.
- Reduces bottom navigation from six primary destinations to four. EQ and Insights remain accessible from Settings.
- Backs up every changed file to ui_overhaul_backup/ before changing it.

HOW TO APPLY
1. Download or clone the repository to your computer.
2. Extract apply_auralis_ui_overhaul.py into the repository root.
3. From that folder, run:
   python apply_auralis_ui_overhaul.py .
4. Open the project in Android Studio and run the Gradle build.
5. Test the Home screen, Mini Player, Library, Settings, Equalizer, Insights, AI Maestro, Now Playing, and playback controls.

If anything goes wrong, restore the files in ui_overhaul_backup/.

LIMITATION
This script modifies source files. It does not produce an APK and has not been run against an Android build in this environment. Build and device verification are still required.
