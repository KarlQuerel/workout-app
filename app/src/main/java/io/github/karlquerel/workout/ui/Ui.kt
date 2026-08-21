package io.github.karlquerel.workout.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun Chip(text: String, color: Color) {
	Text(
		text = text,
		style = MaterialTheme.typography.labelSmall,
		color = color,
		modifier = Modifier
			.border(1.dp, color)
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}

fun fmtWeight(weightKg: Double): String = when {
	weightKg == 0.0 -> "BW"
	weightKg % 1.0 == 0.0 -> weightKg.toInt().toString()
	else -> weightKg.toString()
}

fun fmtClock(totalSeconds: Int): String =
	String.format(Locale.ROOT, "%d:%02d", totalSeconds / 60, totalSeconds % 60)

fun fmtSet(weightKg: Double, reps: Int): String =
	if (weightKg == 0.0) "BW×$reps" else "${fmtWeight(weightKg)}kg×$reps"
