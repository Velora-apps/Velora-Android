package xyz.retroforge.velora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette pulled straight from the web app's :root theme variables
// (public/assets/css/style.css) so the app matches the Velora brand
// instead of generic Material/Discord defaults.
val VeloraBrand = Color(0xFF3D8BFF)
val VeloraBrandHover = Color(0xFF2F70DD)
val VeloraGlow = Color(0xFF7EC8FF)
val VeloraGreen = Color(0xFF2DD4A7)
val VeloraRed = Color(0xFFFF5470)
val VeloraYellow = Color(0xFFFFB84D)

private val BgDarkest = Color(0xFF060B18)
private val BgDark = Color(0xFF0C1425)
private val BgMid = Color(0xFF101A30)
private val BgLight = Color(0xFF1A2740)
private val BgLighter = Color(0xFF24334F)
private val TextNormal = Color(0xFFC4D0E8)
private val TextMuted = Color(0xFF7286AC)
private val TextBright = Color(0xFFEEF4FF)
private val BorderColor = Color(0xFF1B2740)

private val VeloraDarkColors = darkColorScheme(
    primary = VeloraBrand,
    onPrimary = Color.White,
    secondary = VeloraGlow,
    background = BgDarkest,
    onBackground = TextNormal,
    surface = BgDark,
    onSurface = TextNormal,
    surfaceVariant = BgMid,
    onSurfaceVariant = TextMuted,
    inverseSurface = BgLighter,
    outline = BorderColor,
    outlineVariant = BorderColor,
    error = VeloraRed,
    tertiary = VeloraGreen,
)

// Velora is dark-first (the web app has no light theme), but a light
// scheme is kept as a graceful fallback for users with system light mode.
private val VeloraLightColors = lightColorScheme(
    primary = VeloraBrandHover,
    secondary = VeloraGlow,
    tertiary = VeloraGreen,
    error = VeloraRed,
)

// The web app pairs a geometric display face (Space Grotesk) for headers
// with Inter for body text. Both are Google Fonts; rather than bundle font
// files we lean on FontWeight to get a close visual match with the system
// sans, per your "adapt to Material 3 conventions" choice.
private val VeloraTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun VeloraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) VeloraDarkColors else VeloraLightColors
    MaterialTheme(colorScheme = colors, typography = VeloraTypography, content = content)
}
