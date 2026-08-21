package io.github.karlquerel.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.karlquerel.workout.WorkoutApp
import io.github.karlquerel.workout.data.dayById
import io.github.karlquerel.workout.data.db.SessionEntity
import io.github.karlquerel.workout.data.db.SetLogEntity
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Green
import io.github.karlquerel.workout.ui.theme.Outline
import io.github.karlquerel.workout.ui.theme.Panel
import io.github.karlquerel.workout.ui.theme.Red
import io.github.karlquerel.workout.ui.theme.muscleColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val DATE_FMT = DateTimeFormatter.ofPattern("EEE d MMM yyyy · HH:mm", Locale.ENGLISH)

@Composable
fun HistoryScreen(onBack: () -> Unit) {
	val context = LocalContext.current
	val dao = remember { (context.applicationContext as WorkoutApp).database.dao() }
	val scope = rememberCoroutineScope()

	val sessions by dao.sessions().collectAsState(initial = emptyList())
	var expandedId by remember { mutableStateOf<Long?>(null) }
	var expandedSets by remember { mutableStateOf<List<SetLogEntity>>(emptyList()) }

	LaunchedEffect(expandedId) {
		expandedSets = expandedId?.let { dao.setsForSession(it) } ?: emptyList()
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text("LOG HISTORY", style = MaterialTheme.typography.titleLarge)
			TextButton(onClick = onBack) { Text("BACK", color = Dim) }
		}
		Spacer(Modifier.height(12.dp))

		if (sessions.isEmpty()) {
			Text("no sessions logged yet", style = MaterialTheme.typography.bodyMedium, color = Dim)
		}

		LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			items(sessions, key = { it.id }) { session ->
				SessionRow(
					session = session,
					expanded = expandedId == session.id,
					sets = if (expandedId == session.id) expandedSets else emptyList(),
					onToggle = {
						expandedId = if (expandedId == session.id) null else session.id
					},
					onDelete = {
						scope.launch {
							dao.deleteSetsForSession(session.id)
							dao.deleteSession(session.id)
							expandedId = null
						}
					},
				)
			}
		}
	}
}

@Composable
private fun SessionRow(
	session: SessionEntity,
	expanded: Boolean,
	sets: List<SetLogEntity>,
	onToggle: () -> Unit,
	onDelete: () -> Unit,
) {
	val day = dayById(session.dayId)
	val accent: Color = day?.let { muscleColor(it.accent) } ?: Dim
	val date = Instant.ofEpochMilli(session.startedAt)
		.atZone(ZoneId.systemDefault())
		.format(DATE_FMT)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.border(2.dp, if (expanded) accent else Outline)
			.background(Panel)
			.clickable(onClick = onToggle)
			.padding(12.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(date, style = MaterialTheme.typography.bodyMedium)
			Text(day?.kind ?: session.dayId, style = MaterialTheme.typography.labelLarge, color = accent)
		}

		if (expanded) {
			Spacer(Modifier.height(8.dp))
			if (sets.isEmpty()) {
				Text("no sets logged", style = MaterialTheme.typography.bodySmall, color = Dim)
			}
			sets.groupBy { it.exerciseName }.forEach { (name, list) ->
				Text(name, style = MaterialTheme.typography.labelMedium)
				Text(
					list.joinToString("  ") { fmtSet(it.weightKg, it.reps) },
					style = MaterialTheme.typography.bodySmall,
					color = Green,
				)
				Spacer(Modifier.height(6.dp))
			}
			TextButton(onClick = onDelete) { Text("DELETE SESSION", color = Red) }
		}
	}
}
