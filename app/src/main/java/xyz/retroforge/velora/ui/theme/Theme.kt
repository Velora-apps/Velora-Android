package xyz.retroforge.velora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Blurple = Color(0xFF5865F2)
private val DarkBg = Color(0xFF313338)
private val DarkSurface = Color(0xFF2B2D31)

private val VeloraDarkColors = darkColorScheme(
    primary = Blurple,
    background = DarkBg,
    surface = DarkSurface,
)

private val VeloraLightColors = lightColorScheme(
    primary = Blurple,
)

@Composable
fun VeloraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) VeloraDarkColors else VeloraLightColors
    MaterialTheme(colorScheme = colors, content = content)
}
