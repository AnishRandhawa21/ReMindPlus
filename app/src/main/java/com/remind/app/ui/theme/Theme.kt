package com.remind.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

private val LightColorScheme = lightColorScheme(
    primary             = CharcoalDark,
    onPrimary           = Cream,
    primaryContainer    = PastelBlueLight,
    onPrimaryContainer  = CharcoalDark,

    secondary           = PastelGreen,
    onSecondary         = CharcoalDark,
    secondaryContainer  = PastelGreenLight,
    onSecondaryContainer= CharcoalDark,

    tertiary            = PastelPink,
    onTertiary          = CharcoalDark,
    tertiaryContainer   = PastelPinkLight,
    onTertiaryContainer = CharcoalDark,

    background          = Cream,
    onBackground        = TextPrimary,

    surface             = CardWhite,
    onSurface           = TextPrimary,
    surfaceVariant      = CreamDark,
    onSurfaceVariant    = TextSecondary,

    outline             = TextTertiary,
    outlineVariant      = CreamDark,

    error               = StatusOverdue,
    onError             = CardWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary             = PastelYellow,
    onPrimary           = CharcoalDark,
    primaryContainer    = CharcoalMedium,
    onPrimaryContainer  = PastelYellow,

    secondary           = PastelGreen,
    onSecondary         = CharcoalDark,
    secondaryContainer  = SurfaceVariantDark,
    onSecondaryContainer= PastelGreenLight,

    tertiary            = PastelPink,
    onTertiary          = CharcoalDark,
    tertiaryContainer   = SurfaceVariantDark,
    onTertiaryContainer = PastelPinkLight,

    background          = SurfaceDark,
    onBackground        = TextPrimaryDark,

    surface             = SurfaceVariantDark,
    onSurface           = TextPrimaryDark,
    surfaceVariant      = SurfaceCardDark,
    onSurfaceVariant    = TextSecondaryDark,

    outline             = TextTertiaryDark,
    outlineVariant      = SurfaceVariantDark,

    error               = StatusOverdue,
    onError             = CharcoalDark,
)

@Composable
fun ReMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accentColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val accentColors = listOf(
        PastelBlue, PastelGreen, PastelPink, PastelYellow, PastelLavender, PastelPeach
    )
    val selectedAccent = accentColors.getOrElse(accentColorIndex) { PastelBlue }

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Override the primary colors with our custom accent
    val colorScheme = baseColorScheme.copy(
        primary = selectedAccent,
        onPrimary = CharcoalDark, // Text on accent color should be dark for contrast
        primaryContainer = selectedAccent.copy(alpha = 0.3f),
        onPrimaryContainer = if (darkTheme) selectedAccent else CharcoalDark
    )

    // ── Status bar icon colour ────────────────────────────────────────────────
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            // Transparent status bar so background colour shows through
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, view).apply {
                // Light icons (white) in dark mode; dark icons in light mode
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}