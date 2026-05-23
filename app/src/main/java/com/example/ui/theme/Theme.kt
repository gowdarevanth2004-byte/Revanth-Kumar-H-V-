package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ScannerCyan,
    secondary = MetabolicGreen,
    tertiary = HelixViolet,
    background = CosmicMidnight,
    surface = DarkCardBg,
    onPrimary = CosmicMidnight,
    onSecondary = CosmicMidnight,
    onBackground = PaleSlate,
    onSurface = PaleSlate
  )

private val LightColorScheme = DarkColorScheme // Enforce dark biotech aesthetic for both theme modes to represent an immersive lab experience

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode for biometric vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to protect the brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
