package io.github.karlquerel.workout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.karlquerel.workout.ui.theme.Amber
import io.github.karlquerel.workout.ui.theme.Control
import io.github.karlquerel.workout.ui.theme.Danger
import io.github.karlquerel.workout.ui.theme.Heat
import io.github.karlquerel.workout.ui.theme.HeatBrush
import io.github.karlquerel.workout.ui.theme.Ink
import io.github.karlquerel.workout.ui.theme.Kicker
import io.github.karlquerel.workout.ui.theme.Numeric
import io.github.karlquerel.workout.ui.theme.OnHeat
import io.github.karlquerel.workout.ui.theme.Tile
import io.github.karlquerel.workout.ui.theme.TileRaised
import java.util.Locale

val BUTTON_HEIGHT = 56.dp

@Composable
fun TileCard(
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	content: @Composable ColumnScope.() -> Unit,
) {
	var base = modifier
		.clip(RoundedCornerShape(20.dp))
		.background(Tile)
	if (onClick != null) base = base.clickable(onClick = onClick)
	Column(
		modifier = base.padding(14.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp),
		content = content,
	)
}

@Composable
fun KickerText(text: String, modifier: Modifier = Modifier, color: Color = Kicker.color) {
	Text(text.uppercase(Locale.ROOT), style = Kicker, color = color, modifier = modifier)
}

@Composable
fun GradientButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	Box(
		modifier = modifier
			.alpha(if (enabled) 1f else 0.4f)
			.clip(RoundedCornerShape(14.dp))
			.background(HeatBrush)
			.clickable(enabled = enabled, onClick = onClick)
			.heightIn(min = BUTTON_HEIGHT)
			.padding(horizontal = 12.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(text, style = MaterialTheme.typography.labelLarge, color = OnHeat, fontSize = 17.sp)
	}
}

@Composable
fun QuietButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	Box(
		modifier = modifier
			.alpha(if (enabled) 1f else 0.4f)
			.clip(RoundedCornerShape(14.dp))
			.background(Tile)
			.clickable(enabled = enabled, onClick = onClick)
			.heightIn(min = BUTTON_HEIGHT)
			.padding(horizontal = 12.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(text, style = MaterialTheme.typography.labelLarge, color = Ink, fontSize = 17.sp)
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
			.background(Control)
			.padding(horizontal = 9.dp, vertical = 3.dp),
	)
}

// Gym-sized -/+ with the value in the middle; tapping the value opens a wheel for big jumps.
@Composable
fun Stepper(
	value: String,
	unit: String,
	onMinus: () -> Unit,
	onPlus: () -> Unit,
	onPick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val haptic = LocalHapticFeedback.current
	Row(
		modifier = modifier
			.clip(RoundedCornerShape(18.dp))
			.background(TileRaised)
			.padding(6.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		StepKey("−") {
			haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
			onMinus()
		}
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.weight(1f)
				.clip(RoundedCornerShape(12.dp))
				.clickable(onClick = onPick)
				.padding(vertical = 6.dp),
		) {
			Text(value, style = Numeric, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Ink)
			KickerText("$unit · tap to pick")
		}
		StepKey("+") {
			haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
			onPlus()
		}
	}
}

@Composable
private fun StepKey(label: String, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.size(width = 76.dp, height = 64.dp)
			.clip(RoundedCornerShape(14.dp))
			.background(Control)
			.clickable(onClick = onClick),
		contentAlignment = Alignment.Center,
	) {
		Text(label, fontSize = 30.sp, color = Ink)
	}
}

@Composable
fun HeatRing(
	progress: Float,
	size: Dp,
	stroke: Dp,
	modifier: Modifier = Modifier,
	urgent: Boolean = false,
) {
	val brush = if (urgent) Brush.linearGradient(listOf(Danger, Danger)) else Brush.linearGradient(listOf(Heat, Amber))
	Canvas(modifier.size(size)) {
		val w = stroke.toPx()
		val arcSize = Size(this.size.width - w, this.size.height - w)
		val topLeft = Offset(w / 2, w / 2)
		drawArc(Control, 0f, 360f, false, topLeft, arcSize, style = Stroke(w))
		drawArc(
			brush = brush,
			startAngle = -90f,
			sweepAngle = 360f * progress.coerceIn(0f, 1f),
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = Stroke(w, cap = StrokeCap.Round),
		)
	}
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

fun fmtVolume(kg: Double): String =
	if (kg >= 1000) String.format(Locale.ROOT, "%.1f t", kg / 1000) else "${kg.toInt()} kg"
