package io.github.karlquerel.workout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.karlquerel.workout.WorkoutApp
import io.github.karlquerel.workout.data.Muscle
import io.github.karlquerel.workout.data.WEEKLY_SPLIT
import io.github.karlquerel.workout.data.WorkoutDay
import io.github.karlquerel.workout.data.daysSinceTrained
import io.github.karlquerel.workout.data.setsPerMuscle
import io.github.karlquerel.workout.data.todaysDay
import io.github.karlquerel.workout.data.weekStart
import io.github.karlquerel.workout.data.weekStreak
import io.github.karlquerel.workout.data.weeklyVolumes
import io.github.karlquerel.workout.ui.theme.Amber
import io.github.karlquerel.workout.ui.theme.Better
import io.github.karlquerel.workout.ui.theme.Control
import io.github.karlquerel.workout.ui.theme.Danger
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Heat
import io.github.karlquerel.workout.ui.theme.HeatBrush
import io.github.karlquerel.workout.ui.theme.LoadCold
import io.github.karlquerel.workout.ui.theme.LoadHot
import io.github.karlquerel.workout.ui.theme.LoadLow
import io.github.karlquerel.workout.ui.theme.LoadMid
import io.github.karlquerel.workout.ui.theme.Numeric
import io.github.karlquerel.workout.ui.theme.loadColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale
import kotlin.math.roundToInt

private const val DAY_MS = 86_400_000L
private const val VOLUME_WEEKS = 6
private const val STREAK_BARS = 7

@Composable
fun HomeScreen(onStartDay: (String) -> Unit, onHistory: () -> Unit) {
	val context = LocalContext.current
	val dao = remember { (context.applicationContext as WorkoutApp).database.dao() }
	val zone = remember { ZoneId.systemDefault() }
	val today = remember { LocalDate.now() }
	val now = remember { System.currentTimeMillis() }
	val todayDay = remember { todaysDay() }

	val setsFlow = remember { dao.setsSince(weekStart(today.minusWeeks(8), zone)) }
	val sessionsFlow = remember { dao.loggedSessions() }
	val sets by setsFlow.collectAsState(initial = emptyList())
	val sessions by sessionsFlow.collectAsState(initial = emptyList())

	val thisWeek = weekStart(today, zone)
	val doneThisWeek = sessions.filter { it.startedAt >= thisWeek }.map { it.dayId }.toSet()
	val load = setsPerMuscle(sets, now - 7 * DAY_MS)
	val volumes = weeklyVolumes(sets, VOLUME_WEEKS, today, zone)
	val streak = weekStreak(sessions.map { it.startedAt }, today, zone)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 14.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column {
				Text(
					today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
					style = MaterialTheme.typography.titleLarge,
				)
				KickerText(
					"week ${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)} · " +
						"${doneThisWeek.size} / ${WEEKLY_SPLIT.size}",
				)
			}
			TextButton(onClick = onHistory) { Text("History", color = Dim) }
		}

		TodayTile(
			day = todayDay,
			nextDay = remember { nextTrainingDay(today) },
			rested = todayDay?.let { daysSinceTrained(sets, it.accent, now) },
			done = todayDay != null && todayDay.id in doneThisWeek,
			onStart = { todayDay?.let { onStartDay(it.id) } },
		)

		TileCard(Modifier.fillMaxWidth()) {
			KickerText("Muscle load · last 7 days")
			Row(verticalAlignment = Alignment.Bottom) {
				val colorOf = { m: Muscle -> loadColor(load[m] ?: 0) }
				BodyMap(BodySide.FRONT, colorOf, Modifier.weight(1f).height(128.dp))
				BodyMap(BodySide.BACK, colorOf, Modifier.weight(1f).height(128.dp))
				LoadLegend()
			}
		}

		Row(
			modifier = Modifier.height(IntrinsicSize.Min),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			VolumeTile(volumes, Modifier.weight(1f).fillMaxHeight())
			StreakTile(
				streak = streak,
				sessionStarts = sessions.map { it.startedAt },
				today = today,
				zone = zone,
				modifier = Modifier.weight(1f).fillMaxHeight(),
			)
		}

		KickerText("Split", Modifier.padding(start = 4.dp, top = 8.dp))
		WEEKLY_SPLIT.forEach { day ->
			SplitRow(day = day, done = day.id in doneThisWeek, onClick = { onStartDay(day.id) })
		}
		Spacer(Modifier.height(8.dp))
	}
}

@Composable
private fun TodayTile(
	day: WorkoutDay?,
	nextDay: WorkoutDay?,
	rested: Int?,
	done: Boolean,
	onStart: () -> Unit,
) {
	TileCard(Modifier.fillMaxWidth()) {
		if (day == null) {
			KickerText("Rest day")
			Text("Recover", style = MaterialTheme.typography.headlineSmall)
			nextDay?.let {
				Text(
					"Next up: ${it.title.lowercase().replaceFirstChar(Char::titlecase)}, ${it.kind}",
					style = MaterialTheme.typography.bodyMedium,
					color = Dim,
				)
			}
			return@TileCard
		}
		KickerText("Today")
		Text(day.kind, style = MaterialTheme.typography.headlineSmall)
		val muscle = day.accent.label
		Text(
			text = when {
				done -> "Done for this week. Nice work."
				rested == null -> "No $muscle sets in the last 8 weeks."
				rested == 0 -> "$muscle trained earlier today."
				rested == 1 -> "$muscle last trained yesterday."
				else -> "$muscle rested $rested days, ready to push."
			},
			style = MaterialTheme.typography.bodyMedium,
			color = Dim,
		)
		Spacer(Modifier.height(4.dp))
		GradientButton(
			text = if (done) "Train again" else "Start session",
			onClick = onStart,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Composable
private fun LoadLegend() {
	Column(
		verticalArrangement = Arrangement.spacedBy(5.dp),
		modifier = Modifier.padding(bottom = 6.dp),
	) {
		listOf(LoadHot to "12+", LoadMid to "6-11", LoadLow to "1-5", LoadCold to "0 sets").forEach { (color, label) ->
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					Modifier
						.size(8.dp)
						.clip(RoundedCornerShape(2.dp))
						.background(color)
				)
				KickerText(label, Modifier.padding(start = 5.dp))
			}
		}
	}
}

@Composable
private fun VolumeTile(volumes: List<Double>, modifier: Modifier) {
	val current = volumes.last()
	val previous = volumes[volumes.lastIndex - 1]
	TileCard(modifier) {
		KickerText("Volume · this week")
		Text(fmtVolume(current), style = Numeric, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
		Sparkline(volumes, Modifier.fillMaxWidth().height(26.dp))
		if (previous > 0) {
			val pct = ((current - previous) / previous * 100).roundToInt()
			KickerText(
				text = (if (pct >= 0) "+$pct%" else "$pct%") + " vs last week",
				color = if (pct >= 0) Better else Danger,
			)
		} else {
			KickerText("first logged week")
		}
	}
}

@Composable
private fun Sparkline(values: List<Double>, modifier: Modifier) {
	Canvas(modifier) {
		val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
		val stepX = size.width / (values.size - 1)
		val points = values.mapIndexed { i, v ->
			Offset(i * stepX, size.height - (v / max * (size.height - 4f)).toFloat() - 2f)
		}
		val line = Path().apply {
			moveTo(points.first().x, points.first().y)
			points.drop(1).forEach { lineTo(it.x, it.y) }
		}
		val fill = Path().apply {
			addPath(line)
			lineTo(size.width, size.height)
			lineTo(0f, size.height)
			close()
		}
		drawPath(fill, Brush.verticalGradient(listOf(Heat.copy(alpha = 0.28f), Color.Transparent)))
		drawPath(line, HeatBrush, style = Stroke(2.dp.toPx()))
		drawCircle(Amber, 3.dp.toPx(), points.last())
	}
}

@Composable
private fun StreakTile(
	streak: Int,
	sessionStarts: List<Long>,
	today: LocalDate,
	zone: ZoneId,
	modifier: Modifier,
) {
	val activeWeeks = sessionStarts.map { weekStart(Instant.ofEpochMilli(it).atZone(zone).toLocalDate(), zone) }.toSet()
	TileCard(modifier) {
		KickerText("Streak")
		Row(verticalAlignment = Alignment.Bottom) {
			Text("$streak", style = Numeric, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
			Text(
				if (streak == 1) " week" else " weeks",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
				modifier = Modifier.padding(bottom = 3.dp),
			)
		}
		Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
			(STREAK_BARS - 1 downTo 0).forEach { back ->
				val on = weekStart(today.minusWeeks(back.toLong()), zone) in activeWeeks
				Box(
					Modifier
						.weight(1f)
						.height(22.dp)
						.clip(RoundedCornerShape(4.dp))
						.then(if (on) Modifier.background(HeatBrush) else Modifier.background(Control))
				)
			}
		}
		KickerText("last $STREAK_BARS weeks")
	}
}

@Composable
private fun SplitRow(day: WorkoutDay, done: Boolean, onClick: () -> Unit) {
	TileCard(Modifier.fillMaxWidth(), onClick = onClick) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column {
				Text(day.kind, style = MaterialTheme.typography.titleMedium)
				Text(
					day.title.lowercase().replaceFirstChar(Char::titlecase),
					style = MaterialTheme.typography.bodySmall,
					color = Dim,
				)
			}
			if (done) KickerText("done", color = Better) else KickerText("start ›", color = Heat)
		}
	}
}

private fun nextTrainingDay(today: LocalDate): WorkoutDay? =
	(1..7).firstNotNullOfOrNull { offset ->
		val date = today.plusDays(offset.toLong())
		WEEKLY_SPLIT.firstOrNull { it.title.equals(date.dayOfWeek.name, ignoreCase = true) }
	}
