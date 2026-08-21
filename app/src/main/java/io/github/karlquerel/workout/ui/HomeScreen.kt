package io.github.karlquerel.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.karlquerel.workout.data.WEEKLY_SPLIT
import io.github.karlquerel.workout.data.WorkoutDay
import io.github.karlquerel.workout.data.todaysDay
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Outline
import io.github.karlquerel.workout.ui.theme.Panel
import io.github.karlquerel.workout.ui.theme.Yellow
import io.github.karlquerel.workout.ui.theme.muscleColor

@Composable
fun HomeScreen(onStartDay: (String) -> Unit, onHistory: () -> Unit) {
	val today = remember { todaysDay() }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(16.dp),
	) {
		Row {
			Text("WEEKLY ", style = MaterialTheme.typography.titleLarge)
			Text("SPLIT", style = MaterialTheme.typography.titleLarge, color = Yellow)
		}
		Spacer(Modifier.height(4.dp))
		Text(
			text = if (today != null) "today: ${today.title} — ${today.kind}" else "today: rest day",
			style = MaterialTheme.typography.bodyMedium,
			color = Dim,
		)
		Spacer(Modifier.height(16.dp))

		WEEKLY_SPLIT.forEach { day ->
			DayCard(day = day, isToday = day.id == today?.id, onClick = { onStartDay(day.id) })
			Spacer(Modifier.height(12.dp))
		}

		Spacer(Modifier.height(8.dp))
		OutlinedButton(
			onClick = onHistory,
			shape = RectangleShape,
			modifier = Modifier.fillMaxWidth(),
		) {
			Text("LOG HISTORY")
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayCard(day: WorkoutDay, isToday: Boolean, onClick: () -> Unit) {
	val accent = muscleColor(day.accent)
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.border(2.dp, if (isToday) accent else Outline)
			.background(Panel)
			.clickable(onClick = onClick)
			.padding(14.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(day.title, style = MaterialTheme.typography.titleMedium)
			Text(day.kind, style = MaterialTheme.typography.labelLarge, color = accent)
		}
		Spacer(Modifier.height(8.dp))
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			day.muscles.forEach { muscle ->
				Chip(text = muscle.label.lowercase(), color = muscleColor(muscle))
			}
		}
		if (isToday) {
			Spacer(Modifier.height(8.dp))
			Text("▶ START TODAY'S SESSION", style = MaterialTheme.typography.labelMedium, color = accent)
		}
	}
}
