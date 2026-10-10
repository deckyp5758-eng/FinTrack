package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = Emerald80,
  onPrimary = Slate950,
  primaryContainer = EmeraldGrey40,
  onPrimaryContainer = Emerald80,
  secondary = Gold80,
  onSecondary = Slate950,
  secondaryContainer = Gold40,
  onSecondaryContainer = Gold80,
  tertiary = InfoCyan,
  background = Slate950,
  onBackground = Color(0xFFF1F5F9),
  surface = Slate900,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = Slate800,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Slate700,
  error = DangerRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = Emerald40,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD1FAE5),
  onPrimaryContainer = Color(0xFF065F46),
  secondary = Gold40,
  onSecondary = Color.White,
  tertiary = InfoCyan,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color.White,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFE2E8F0),
  error = DangerRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent finance theme styling
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
