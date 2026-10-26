package app.ilecto.agenda.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AgendaBlue,
    surface = AgendaSurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = AgendaBlueDark,
    surface = AgendaSurfaceDark,
)

@Composable
fun AgendaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AgendaTypography,
        content = content,
    )
}
