package io.github.karlquerel.workout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.karlquerel.workout.R
import io.github.karlquerel.workout.data.Effort
import io.github.karlquerel.workout.data.Muscle

// Dark gym console: near-black ground, one energetic accent, color-coded muscles.
val Bg = Color(0xFF0B0F14)
val CardBg = Color(0xFF151C24)
val InkHi = Color(0xFFE8EDF3)
val Dim = Color(0xFF8B98A7)
val Line = Color(0xFF243039)
val Accent = Color(0xFF4ADE80)
val Warn = Color(0xFFF5C842)
val Danger = Color(0xFFF87171)

// Pixel display font — reserved for the timer digits and the brand mark.
val PixelFont = FontFamily(Font(R.font.vt323))

fun muscleColor(muscle: Muscle): Color = when (muscle) {
	Muscle.CHEST -> Color(0xFF3B9EFF)
	Muscle.BACK -> Color(0xFFFF7C38)
	Muscle.TRICEPS -> Color(0xFFB56BFF)
	Muscle.BICEPS -> Color(0xFF2FCC8B)
	Muscle.SHOULDERS -> Color(0xFFFF4F8B)
	Muscle.LEGS -> Color(0xFFF5C842)
	Muscle.ABS -> Color(0xFF35D4FF)
	Muscle.WAIST -> Color(0xFF9EFF6B)
	Muscle.FOREARMS -> Color(0xFFFFB347)
}

fun effortColor(effort: Effort): Color = when (effort) {
	Effort.EASY -> Color(0xFF2FCC8B)
	Effort.MEDIUM -> Warn
	Effort.HARD -> Danger
}

private val ConsoleColorScheme = darkColorScheme(
	primary = Accent,
	onPrimary = Color(0xFF03240F),
	secondary = Warn,
	onSecondary = Color(0xFF2A2005),
	background = Bg,
	onBackground = InkHi,
	surface = CardBg,
	onSurface = InkHi,
	surfaceVariant = CardBg,
	onSurfaceVariant = Dim,
	outline = Line,
	error = Danger,
	onError = Color(0xFF300B0B),
)

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colorScheme = ConsoleColorScheme,
		typography = Typography(),
		content = content,
	)
}
