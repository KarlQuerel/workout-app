package io.github.karlquerel.workout.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.karlquerel.workout.data.WEEKLY_SPLIT
import io.github.karlquerel.workout.data.WorkoutDay
import io.github.karlquerel.workout.data.todaysDay
import io.github.karlquerel.workout.ui.theme.Accent
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Line
import io.github.karlquerel.workout.ui.theme.PixelFont
import io.github.karlquerel.workout.ui.theme.muscleColor

@Composable
fun HomeScreen(onStartDay: (String) -> Unit, onHistory: () -> Unit) {
	val today = remember { todaysDay() }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(20.dp),
	) {
		Row(verticalAlignment = Alignment.Bottom) {
			Text("split", fontFamily = PixelFont, fontSize = 46.sp, color = Accent)
			BlinkingCursor()
		}
		Text(
			text = if (today != null) "today · ${today.title.lowercase()} — ${today.kind}"
			else "today · rest day",
			style = MaterialTheme.typography.bodyMedium,
			color = Dim,
		)
		Spacer(Modifier.height(20.dp))

		WEEKLY_SPLIT.forEach { day ->
			DayCard(day = day, isToday = day.id == today?.id, onClick = { onStartDay(day.id) })
			Spacer(Modifier.height(12.dp))
		}

		Spacer(Modifier.height(8.dp))
		OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
			Text("History")
		}
	}
}

@Composable
private fun BlinkingCursor() {
	val transition = rememberInfiniteTransition(label = "cursor")
	val alpha by transition.animateFloat(
		initialValue = 1f,
		targetValue = 0f,
		animationSpec = infiniteRepeatable(
			animation = keyframes {
				durationMillis = 1000
				1f at 0
				1f at 499
				0f at 500
				0f at 999
			},
		),
		label = "cursorAlpha",
	)
	Text(
		"▊",
		fontFamily = PixelFont,
		fontSize = 40.sp,
		color = Accent,
		modifier = Modifier.alpha(alpha),
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayCard(day: WorkoutDay, isToday: Boolean, onClick: () -> Unit) {
	val accent = muscleColor(day.accent)
	AccentCard(
		accent = accent,
		borderColor = if (isToday) accent else Line,
		onClick = onClick,
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				day.title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
			)
			Text(day.kind, style = MaterialTheme.typography.labelLarge, color = accent)
		}
		Spacer(Modifier.height(10.dp))
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			day.muscles.forEach { muscle ->
				Chip(text = muscle.label.lowercase(), color = muscleColor(muscle))
			}
		}
		if (isToday) {
			Spacer(Modifier.height(10.dp))
			Text(
				"Start today's session ▶",
				style = MaterialTheme.typography.labelLarge,
				color = accent,
			)
		}
	}
}
