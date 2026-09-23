package io.github.karlquerel.workout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.karlquerel.workout.R

val Bg = Color(0xFF0B0C0E)
val Tile = Color(0xFF16181B)
val TileRaised = Color(0xFF1D2024)
val Control = Color(0xFF25282C)
val Ink = Color(0xFFF4F5F6)
val Dim = Color(0xFFA3A8AE)
val Faint = Color(0xFF7D828A)
val Heat = Color(0xFFFF5A1F)
val Amber = Color(0xFFFFB020)
val Better = Color(0xFF7EE0A1)
val Danger = Color(0xFFF87171)
val OnHeat = Color(0xFF1B0A00)

val HeatBrush = Brush.linearGradient(listOf(Heat, Amber))

// Muscle-map shading by working sets over the last 7 days.
val LoadCold = Color(0xFF2A2E34)
val LoadLow = Color(0xFF5E3524)
val LoadMid = Color(0xFFB8481C)
val LoadHot = Heat
val BodySkin = Color(0xFF3A3F46)

fun loadColor(sets: Int): Color = when {
	sets <= 0 -> LoadCold
	sets < 6 -> LoadLow
	sets < 12 -> LoadMid
	else -> LoadHot
}

@OptIn(ExperimentalTextApi::class)
private fun geist(weight: FontWeight) =
	Font(R.font.geist, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val Geist = FontFamily(
	geist(FontWeight.Normal),
	geist(FontWeight.Medium),
	geist(FontWeight.SemiBold),
	geist(FontWeight.Bold),
)

@OptIn(ExperimentalTextApi::class)
val GeistMono = FontFamily(
	Font(
		R.font.geist_mono,
		FontWeight.Normal,
		variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Normal.weight)),
	),
)

// Weights and timers tick in place instead of jittering.
val Numeric = TextStyle(fontFamily = Geist, fontFeatureSettings = "tnum")

val Kicker = TextStyle(fontFamily = GeistMono, fontSize = 10.sp, letterSpacing = 1.2.sp, color = Faint)

private fun TextStyle.geist() = copy(fontFamily = Geist)

private val AppTypography = Typography().run {
	copy(
		displayLarge = displayLarge.geist(),
		displayMedium = displayMedium.geist(),
		displaySmall = displaySmall.geist(),
		headlineLarge = headlineLarge.geist(),
		headlineMedium = headlineMedium.geist(),
		headlineSmall = headlineSmall.geist().copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
		titleLarge = titleLarge.geist().copy(fontWeight = FontWeight.SemiBold),
		titleMedium = titleMedium.geist().copy(fontWeight = FontWeight.SemiBold),
		titleSmall = titleSmall.geist(),
		bodyLarge = bodyLarge.geist(),
		bodyMedium = bodyMedium.geist(),
		bodySmall = bodySmall.geist(),
		labelLarge = labelLarge.geist().copy(fontWeight = FontWeight.SemiBold),
		labelMedium = labelMedium.geist(),
		labelSmall = labelSmall.geist(),
	)
}

private val PerformanceColorScheme = darkColorScheme(
	primary = Heat,
	onPrimary = OnHeat,
	secondary = Amber,
	onSecondary = OnHeat,
	background = Bg,
	onBackground = Ink,
	surface = Tile,
	onSurface = Ink,
	surfaceVariant = Control,
	onSurfaceVariant = Dim,
	outline = Control,
	error = Danger,
	onError = Color(0xFF300B0B),
)

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colorScheme = PerformanceColorScheme,
		typography = AppTypography,
		content = content,
	)
}
