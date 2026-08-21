package io.github.karlquerel.workout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.karlquerel.workout.data.db.ProgressPoint
import io.github.karlquerel.workout.ui.theme.Dim
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CHART_DATE_FMT = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

// Top-set weight per session over time. Expects at least two points.
@Composable
fun ProgressChart(points: List<ProgressPoint>, color: Color) {
	val minW = points.minOf { it.w }
	val maxW = points.maxOf { it.w }
	val span = (maxW - minW).takeIf { it > 0 } ?: 1.0

	Column {
		Text(
			"top-set weight · ${points.size} sessions",
			style = MaterialTheme.typography.bodySmall,
			color = Dim,
		)
		Spacer(Modifier.height(10.dp))
		Canvas(
			Modifier
				.fillMaxWidth()
				.height(160.dp),
		) {
			val padX = 6.dp.toPx()
			val padY = 8.dp.toPx()
			val stepX = (size.width - padX * 2) / (points.size - 1)
			val h = size.height - padY * 2
			fun x(i: Int) = padX + i * stepX
			fun y(w: Double) = padY + ((maxW - w) / span * h).toFloat()

			for (i in 0 until points.size - 1) {
				drawLine(
					color = color,
					start = Offset(x(i), y(points[i].w)),
					end = Offset(x(i + 1), y(points[i + 1].w)),
					strokeWidth = 3.dp.toPx(),
				)
			}
			points.forEachIndexed { i, p ->
				drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x(i), y(p.w)))
			}
		}
		Spacer(Modifier.height(6.dp))
		Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
			Text(dateShort(points.first().ts), style = MaterialTheme.typography.bodySmall, color = Dim)
			Text(dateShort(points.last().ts), style = MaterialTheme.typography.bodySmall, color = Dim)
		}
		Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
			Text("min ${fmtWeight(minW)} kg", style = MaterialTheme.typography.bodySmall, color = Dim)
			Text("max ${fmtWeight(maxW)} kg", style = MaterialTheme.typography.bodySmall, color = Dim)
		}
	}
}

private fun dateShort(ts: Long): String =
	Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).format(CHART_DATE_FMT)
