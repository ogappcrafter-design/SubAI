package com.ogapp.subai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SubAiDarkColors = darkColorScheme(
    primary = AmpAmber,
    onPrimary = CarbonBlack,
    secondary = BassCyan,
    onSecondary = CarbonBlack,
    tertiary = BassCyanDeep,
    background = CarbonBlack,
    onBackground = OffWhite,
    surface = CarbonSurface,
    onSurface = OffWhite,
    surfaceVariant = CarbonSurfaceHi,
    onSurfaceVariant = SteelGray,
    error = WireRed,
    outline = SteelDim
)

// The app is dark-first (built for a dim garage/car interior). A light scheme
// is provided so the system toggle never produces an unreadable screen.
private val SubAiLightColors = lightColorScheme(
    primary = AmpAmberDeep,
    secondary = BassCyanDeep,
    background = OffWhite,
    surface = Color(0xFFFFFFFF),
    error = WireRed
)

@Composable
fun SubAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) SubAiDarkColors else SubAiLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SubAiTypography,
        content = content
    )
}
