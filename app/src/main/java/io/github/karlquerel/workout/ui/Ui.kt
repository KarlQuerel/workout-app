package io.github.karlquerel.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.karlquerel.workout.ui.theme.CardBg
import io.github.karlquerel.workout.ui.theme.Line
import java.util.Locale

// Card with a colored accent strip down the left edge — the app's core surface.
@Composable
fun AccentCard(
	accent: Color,
	modifier: Modifier = Modifier,
	borderColor: Color = Line,
	onClick: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit,
) {
	val shape = RoundedCornerShape(14.dp)
	var base = modifier
		.fillMaxWidth()
		.clip(shape)
		.background(CardBg)
		.border(1.dp, borderColor, shape)
	if (onClick != null) base = base.clickable(onClick = onClick)

	Row(base.height(IntrinsicSize.Min)) {
		Box(
			Modifier
				.width(5.dp)
				.fillMaxHeight()
				.background(accent)
		)
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(14.dp),
			content = content,
		)
	}
}

@Composable
fun Chip(text: String, color: Color) {
	Text(
		text = text,
		style = MaterialTheme.typography.labelMedium,
		color = color,
		modifier = Modifier
			.clip(RoundedCornerShape(50))
			.background(color.copy(alpha = 0.14f))
			.padding(horizontal = 9.dp, vertical = 3.dp),
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
	if (weightKg == 0.0) "BW × $reps" else "${fmtWeight(weightKg)} kg × $reps"
