package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App-wide Theme Management System supporting Obsidian, Aurora, Titanium, and Ember themes.
 */
enum class AppTheme(
    val displayName: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val accentColor: Color
) {
    OBSIDIAN(
        displayName = "Obsidian",
        subtitle = "Near-black OLED graphite with restrained champagne gold & platinum silver",
        primaryColor = Color(0xFFE5B869),
        secondaryColor = Color(0xFFCBD5E1),
        backgroundColor = Color(0xFF07090E),
        surfaceColor = Color(0xFF0E131F),
        accentColor = Color(0xFFD4AF37)
    ),
    AURORA(
        displayName = "Aurora",
        subtitle = "Northern lights deep teal with emerald green & subtle magenta glow",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFEC4899),
        backgroundColor = Color(0xFF06141B),
        surfaceColor = Color(0xFF0D2830),
        accentColor = Color(0xFF06B6D4)
    ),
    TITANIUM(
        displayName = "Titanium",
        subtitle = "Industrial dark slate steel with icy blue & polished silver accents",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF94A3B8),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B),
        accentColor = Color(0xFFCBD5E1)
    ),
    EMBER(
        displayName = "Ember",
        subtitle = "Warm dark charcoal with incandescent amber & glowing gold warmth",
        primaryColor = Color(0xFFFF8000),
        secondaryColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF180C08),
        surfaceColor = Color(0xFF26150F),
        accentColor = Color(0xFFEF4444)
    )
}

val ObsidianColorScheme = darkColorScheme(
    primary = Color(0xFFE5B869),
    onPrimary = Color(0xFF1A1305),
    primaryContainer = Color(0xFF2E2209),
    onPrimaryContainer = Color(0xFFFFE09C),
    secondary = Color(0xFFCBD5E1),
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = Color(0xFF273344),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF00354A),
    background = Color(0xFF07090E),
    onBackground = Color(0xFFF4F6FB),
    surface = Color(0xFF0E131F),
    onSurface = Color(0xFFF4F6FB),
    surfaceVariant = Color(0xFF161E2E),
    onSurfaceVariant = Color(0xFF8E99AC),
    outline = Color(0xFF222B3D)
)

val AuroraColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFF6FFBBE),
    secondary = Color(0xFFEC4899),
    onSecondary = Color(0xFF4A0027),
    secondaryContainer = Color(0xFF6E003C),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFF06B6D4),
    onTertiary = Color(0xFF003642),
    background = Color(0xFF06141B),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0D2830),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF143B47),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF1E5262)
)

val TitaniumColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF00354A),
    primaryContainer = Color(0xFF004D6A),
    onPrimaryContainer = Color(0xFFC2E8FF),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFFCBD5E1),
    onTertiary = Color(0xFF0F172A),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

val EmberColorScheme = darkColorScheme(
    primary = Color(0xFFFF8000),
    onPrimary = Color(0xFF4A1E00),
    primaryContainer = Color(0xFF6D2F00),
    onPrimaryContainer = Color(0xFFFFDCC7),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDDAE),
    tertiary = Color(0xFFEF4444),
    onTertiary = Color(0xFF490006),
    background = Color(0xFF180C08),
    onBackground = Color(0xFFFFF7F5),
    surface = Color(0xFF26150F),
    onSurface = Color(0xFFFFF7F5),
    surfaceVariant = Color(0xFF381E15),
    onSurfaceVariant = Color(0xFFA88F86),
    outline = Color(0xFF522F22)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.OBSIDIAN -> ObsidianColorScheme
        AppTheme.AURORA -> AuroraColorScheme
        AppTheme.TITANIUM -> TitaniumColorScheme
        AppTheme.EMBER -> EmberColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
