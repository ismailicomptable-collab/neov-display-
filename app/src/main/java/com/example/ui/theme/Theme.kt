package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class TelemetryPalette(
  val cyan: Color,
  val emerald: Color,
  val amber: Color,
  val coral: Color,
  val violet: Color,
  val blue: Color,
  val cardElevated: Color,
  val gridLine: Color,
)

val LocalTelemetryPalette = staticCompositionLocalOf {
  TelemetryPalette(
    cyan = CyanTelemetry,
    emerald = EmeraldTelemetry,
    amber = AmberTelemetry,
    coral = CoralTelemetry,
    violet = VioletTelemetry,
    blue = BlueTelemetry,
    cardElevated = DarkSurfaceElevated,
    gridLine = DarkOutline,
  )
}

private val CominitechDarkScheme =
  darkColorScheme(
    primary = CyanTelemetry,
    onPrimary = Color(0xFF04131D),
    primaryContainer = Color(0xFF0E3A4D),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = EmeraldTelemetry,
    onSecondary = Color(0xFF031A12),
    secondaryContainer = Color(0xFF0C3B2B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = VioletTelemetry,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2E1E59),
    onTertiaryContainer = Color(0xFFDDD6FE),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1E293B),
    error = CoralTelemetry,
    onError = Color.White,
  )

private val CominitechLightScheme =
  lightColorScheme(
    primary = CyanDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = EmeraldDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = Color(0xFF6D28D9),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF2E1065),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFE11D48),
    onError = Color.White,
  )

private val CominitechShapes =
  Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
  )

@Composable
fun CominitechTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) CominitechDarkScheme else CominitechLightScheme
  val telemetryPalette =
    if (darkTheme) {
      TelemetryPalette(
        cyan = CyanTelemetry,
        emerald = EmeraldTelemetry,
        amber = AmberTelemetry,
        coral = CoralTelemetry,
        violet = VioletTelemetry,
        blue = BlueTelemetry,
        cardElevated = DarkSurfaceElevated,
        gridLine = DarkOutline,
      )
    } else {
      TelemetryPalette(
        cyan = CyanDeep,
        emerald = EmeraldDeep,
        amber = Color(0xFFD97706),
        coral = Color(0xFFE11D48),
        violet = Color(0xFF7C3AED),
        blue = Color(0xFF2563EB),
        cardElevated = LightSurfaceElevated,
        gridLine = LightOutline,
      )
    }

  CompositionLocalProvider(LocalTelemetryPalette provides telemetryPalette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = CominitechTypography,
      shapes = CominitechShapes,
      content = content,
    )
  }
}
