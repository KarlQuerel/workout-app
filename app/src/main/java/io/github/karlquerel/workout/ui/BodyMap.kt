package io.github.karlquerel.workout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.karlquerel.workout.data.Muscle
import io.github.karlquerel.workout.ui.theme.BodySkin
import io.github.karlquerel.workout.ui.theme.LoadCold

enum class BodySide { FRONT, BACK }

// One body part in a 100x165 design grid; null muscle = head, neck, hips.
private class Part(
	val muscle: Muscle?,
	val x: Float,
	val y: Float,
	val w: Float,
	val h: Float,
	val r: Float,
	val oval: Boolean = false,
)

private fun mirrored(muscle: Muscle?, x: Float, y: Float, w: Float, h: Float, r: Float, oval: Boolean = false) =
	listOf(Part(muscle, x, y, w, h, r, oval), Part(muscle, 100 - x - w, y, w, h, r, oval))

private val HEAD = listOf(
	Part(null, 41f, 3f, 18f, 18f, 9f, oval = true),
	Part(null, 46f, 20f, 8f, 7f, 2f),
)

private val DELTS = mirrored(Muscle.SHOULDERS, 21f, 27f, 16f, 14f, 7f, oval = true)
private val FOREARMS = mirrored(Muscle.FOREARMS, 15f, 62f, 8f, 21f, 4f)
private val CALVES = mirrored(Muscle.LEGS, 36f, 125f, 11f, 32f, 5f)

private val FRONT = HEAD + DELTS + FOREARMS + CALVES +
	mirrored(Muscle.CHEST, 35f, 28f, 14f, 17f, 6f) +
	mirrored(Muscle.BICEPS, 18f, 41f, 9f, 19f, 4.5f) +
	listOf(47f, 56.5f, 66f).flatMap { y -> mirrored(Muscle.ABS, 41f, y, 8.5f, 8f, 2.5f) } +
	mirrored(Muscle.WAIST, 34f, 47f, 6f, 26f, 3f) +
	Part(null, 37f, 75f, 26f, 8f, 4f) +
	mirrored(Muscle.LEGS, 35f, 84f, 13f, 38f, 6f)

private val BACK = HEAD + DELTS + FOREARMS + CALVES +
	Part(Muscle.BACK, 37f, 23f, 26f, 10f, 5f, oval = true) +
	mirrored(Muscle.BACK, 35f, 33f, 14f, 30f, 7f) +
	Part(Muscle.BACK, 41f, 64f, 18f, 11f, 4f) +
	mirrored(Muscle.TRICEPS, 18f, 41f, 9f, 19f, 4.5f) +
	mirrored(Muscle.LEGS, 36f, 77f, 13f, 13f, 6f) +
	mirrored(Muscle.LEGS, 35f, 92f, 13f, 30f, 6f)

fun sideShowing(muscle: Muscle): BodySide =
	if (muscle == Muscle.BACK || muscle == Muscle.TRICEPS) BodySide.BACK else BodySide.FRONT

@Composable
fun BodyMap(side: BodySide, colorOf: (Muscle) -> Color, modifier: Modifier = Modifier) {
	val parts = if (side == BodySide.FRONT) FRONT else BACK
	Canvas(modifier) {
		val scale = minOf(size.width / 100f, size.height / 165f)
		val dx = (size.width - 100f * scale) / 2
		val dy = (size.height - 165f * scale) / 2
		parts.forEach { p ->
			val color = p.muscle?.let(colorOf) ?: BodySkin
			val topLeft = Offset(dx + p.x * scale, dy + p.y * scale)
			val partSize = Size(p.w * scale, p.h * scale)
			if (p.oval) drawOval(color, topLeft, partSize)
			else drawRoundRect(color, topLeft, partSize, CornerRadius(p.r * scale))
		}
	}
}

fun highlightOnly(muscle: Muscle, color: Color): (Muscle) -> Color =
	{ if (it == muscle) color else LoadCold }
