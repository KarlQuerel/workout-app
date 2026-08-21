package io.github.karlquerel.workout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.karlquerel.workout.data.Effort
import io.github.karlquerel.workout.data.Muscle

val Void = Color(0xFF000000)
val Panel = Color(0xFF0D0D0D)
val Ink = Color(0xFFE8E8E8)
val Dim = Color(0xFF8A8A8A)
val Outline = Color(0xFF2A2A2A)
val Green = Color(0xFF50FA7B)
val Yellow = Color(0xFFF1FA8C)
val Red = Color(0xFFFF5555)
val Orange = Color(0xFFFFB86C)
val Purple = Color(0xFFBD93F9)
val Cyan = Color(0xFF8BE9FD)
val Blue = Color(0xFF6EA8FF)
val Pink = Color(0xFFFF79C6)
val Tan = Color(0xFFCFA97E)

fun muscleColor(muscle: Muscle): Color = when (muscle) {
	Muscle.CHEST -> Red
	Muscle.BACK -> Blue
	Muscle.TRICEPS -> Orange
	Muscle.BICEPS -> Purple
	Muscle.SHOULDERS -> Yellow
	Muscle.LEGS -> Green
	Muscle.ABS -> Cyan
	Muscle.WAIST -> Pink
	Muscle.FOREARMS -> Tan
}

fun effortColor(effort: Effort): Color = when (effort) {
	Effort.EASY -> Green
	Effort.MEDIUM -> Yellow
	Effort.HARD -> Red
}

private val base = Typography()

private val MonoTypography = Typography(
	headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
	titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
	titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
	bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Monospace),
	bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Monospace),
	bodySmall = base.bodySmall.copy(fontFamily = FontFamily.Monospace),
	labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
	labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Monospace),
	labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace),
)

private val VoidColorScheme = darkColorScheme(
	primary = Green,
	onPrimary = Void,
	secondary = Yellow,
	onSecondary = Void,
	background = Void,
	onBackground = Ink,
	surface = Panel,
	onSurface = Ink,
	surfaceVariant = Panel,
	onSurfaceVariant = Dim,
	outline = Outline,
	error = Red,
	onError = Void,
)

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colorScheme = VoidColorScheme,
		typography = MonoTypography,
		content = content,
	)
}
