package io.github.karlquerel.workout.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.karlquerel.workout.WorkoutApp
import io.github.karlquerel.workout.data.Block
import io.github.karlquerel.workout.data.Exercise
import io.github.karlquerel.workout.data.Single
import io.github.karlquerel.workout.data.Superset
import io.github.karlquerel.workout.data.dayById
import io.github.karlquerel.workout.data.db.SessionEntity
import io.github.karlquerel.workout.data.db.SetLogEntity
import io.github.karlquerel.workout.data.db.WorkoutDao
import io.github.karlquerel.workout.timer.RestTimer
import io.github.karlquerel.workout.ui.theme.Cyan
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Green
import io.github.karlquerel.workout.ui.theme.Orange
import io.github.karlquerel.workout.ui.theme.Outline
import io.github.karlquerel.workout.ui.theme.Panel
import io.github.karlquerel.workout.ui.theme.Red
import io.github.karlquerel.workout.ui.theme.Yellow
import io.github.karlquerel.workout.ui.theme.effortColor
import io.github.karlquerel.workout.ui.theme.muscleColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SessionScreen(dayId: String, onExit: () -> Unit) {
	val day = remember(dayId) { dayById(dayId) }
	if (day == null) {
		LaunchedEffect(Unit) { onExit() }
		return
	}

	val context = LocalContext.current
	val dao = remember { (context.applicationContext as WorkoutApp).database.dao() }
	val scope = rememberCoroutineScope()

	var sessionId by remember { mutableStateOf<Long?>(null) }
	LaunchedEffect(Unit) {
		sessionId = dao.insertSession(
			SessionEntity(dayId = day.id, startedAt = System.currentTimeMillis())
		)
	}

	// Gym sessions outlive the screen timeout — keep the display on.
	val activity = context as? Activity
	DisposableEffect(Unit) {
		activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		onDispose {
			activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	var blockIndex by rememberSaveable { mutableIntStateOf(0) }
	val block = day.blocks[blockIndex]
	val isLast = blockIndex == day.blocks.lastIndex

	val finish: () -> Unit = {
		RestTimer.cancel(context)
		scope.launch {
			sessionId?.let { sid ->
				if (dao.setsForSession(sid).isEmpty()) dao.deleteSession(sid)
				else dao.endSession(sid, System.currentTimeMillis())
			}
			onExit()
		}
	}

	Column(modifier = Modifier.fillMaxSize()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column {
				Text(day.kind, style = MaterialTheme.typography.titleMedium)
				Text(
					"block ${blockIndex + 1}/${day.blocks.size}",
					style = MaterialTheme.typography.bodySmall,
					color = Dim,
				)
			}
			TextButton(onClick = finish) { Text("END", color = Red) }
		}

		RestBar()

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp),
		) {
			when (block) {
				is Single -> ExerciseCard(block.exercise, sessionId, dao)
				is Superset -> {
					Text(
						"SUPERSET — alternate A ↔ B",
						style = MaterialTheme.typography.labelMedium,
						color = Yellow,
					)
					Spacer(Modifier.height(8.dp))
					ExerciseCard(block.a, sessionId, dao, step = "A")
					Spacer(Modifier.height(12.dp))
					ExerciseCard(block.b, sessionId, dao, step = "B")
				}
			}
			Spacer(Modifier.height(24.dp))
		}

		if (!isLast) {
			Text(
				"next up: ${blockName(day.blocks[blockIndex + 1])}",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
				modifier = Modifier.padding(horizontal = 16.dp),
			)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			OutlinedButton(
				onClick = { blockIndex-- },
				enabled = blockIndex > 0,
				shape = RectangleShape,
				modifier = Modifier.weight(1f),
			) {
				Text("◀ PREV")
			}
			Button(
				onClick = { if (isLast) finish() else blockIndex++ },
				shape = RectangleShape,
				modifier = Modifier.weight(1f),
			) {
				Text(if (isLast) "FINISH" else "NEXT ▶")
			}
		}
	}
}

@Composable
private fun RestBar() {
	val context = LocalContext.current
	val rest by RestTimer.state.collectAsState()
	val r = rest ?: return

	var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
	LaunchedEffect(r) {
		while (true) {
			nowMs = System.currentTimeMillis()
			if (nowMs >= r.endAt) {
				RestTimer.clearFinished()
				break
			}
			delay(200)
		}
	}

	val remaining = (((r.endAt - nowMs + 999) / 1000).toInt()).coerceAtLeast(0)
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.border(2.dp, Green)
			.background(Panel)
			.padding(12.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text("REST — ${r.label}", style = MaterialTheme.typography.labelMedium, color = Dim)
		Text(fmtClock(remaining), style = MaterialTheme.typography.headlineLarge, color = Green)
		Spacer(Modifier.height(8.dp))
		LinearProgressIndicator(
			progress = { remaining.toFloat() / r.totalSeconds },
			modifier = Modifier.fillMaxWidth(),
			color = Green,
			trackColor = Outline,
		)
		TextButton(onClick = { RestTimer.cancel(context) }) { Text("SKIP", color = Dim) }
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseCard(
	exercise: Exercise,
	sessionId: Long?,
	dao: WorkoutDao,
	step: String? = null,
) {
	val context = LocalContext.current
	val accent = muscleColor(exercise.muscle)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.border(2.dp, accent)
			.background(Panel)
			.padding(14.dp),
	) {
		Text(
			text = (step?.let { "[$it] " } ?: "") + exercise.name,
			style = MaterialTheme.typography.titleMedium,
		)
		Spacer(Modifier.height(8.dp))
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			Chip(exercise.muscle.label.lowercase(), accent)
			Chip(exercise.effort.label.lowercase(), effortColor(exercise.effort))
			if (exercise.dropSet) Chip("drop set", Orange)
			Chip(
				if (exercise.isWarmup) "for ${fmtClock(exercise.restSeconds)}"
				else "rest ${fmtClock(exercise.restSeconds)}",
				Cyan,
			)
		}
		Spacer(Modifier.height(10.dp))
		exercise.cues.forEach { cue ->
			Text(
				"› ${cue.title} — ${cue.text}",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
			)
			Spacer(Modifier.height(4.dp))
		}

		if (exercise.isWarmup) {
			Spacer(Modifier.height(10.dp))
			Button(
				onClick = { RestTimer.start(context, exercise.restSeconds, exercise.name) },
				shape = RectangleShape,
				modifier = Modifier.fillMaxWidth(),
			) {
				Text("START ${fmtClock(exercise.restSeconds)} TIMER")
			}
		} else {
			SetLogger(exercise, sessionId, dao)
		}
	}
}

@Composable
private fun SetLogger(
	exercise: Exercise,
	sessionId: Long?,
	dao: WorkoutDao,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var logged by remember(exercise.name, sessionId) { mutableStateOf<List<SetLogEntity>>(emptyList()) }
	var lastTime by remember(exercise.name) { mutableStateOf<List<SetLogEntity>>(emptyList()) }
	var weightText by rememberSaveable(exercise.name) { mutableStateOf("") }
	var repsText by rememberSaveable(exercise.name) { mutableStateOf("") }

	LaunchedEffect(sessionId) {
		val sid = sessionId ?: return@LaunchedEffect
		logged = dao.setsForSession(sid).filter { it.exerciseName == exercise.name }
		lastTime = dao.lastSessionSets(exercise.name, sid)
		// Progressive overload: start from last session's final working weight.
		if (weightText.isEmpty()) {
			val lastWeight = lastTime.lastOrNull()?.weightKg
			if (lastWeight != null && lastWeight != 0.0) weightText = fmtWeight(lastWeight)
		}
	}

	Spacer(Modifier.height(10.dp))
	Text(
		text = "last time: " +
			if (lastTime.isEmpty()) "—"
			else lastTime.joinToString("  ") { fmtSet(it.weightKg, it.reps) },
		style = MaterialTheme.typography.bodySmall,
		color = Dim,
	)
	Spacer(Modifier.height(8.dp))

	logged.forEachIndexed { index, set ->
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				"set ${index + 1}   ${fmtSet(set.weightKg, set.reps)}",
				style = MaterialTheme.typography.bodyMedium,
				color = Green,
			)
			TextButton(
				onClick = {
					scope.launch {
						dao.deleteSet(set.id)
						logged = logged.filterNot { it.id == set.id }
					}
				},
			) {
				Text("✕", color = Dim)
			}
		}
	}

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		OutlinedTextField(
			value = weightText,
			onValueChange = { weightText = it },
			label = { Text("kg") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
			shape = RectangleShape,
			modifier = Modifier.weight(1f),
		)
		OutlinedTextField(
			value = repsText,
			onValueChange = { repsText = it },
			label = { Text("reps") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			shape = RectangleShape,
			modifier = Modifier.weight(1f),
		)
		Button(
			onClick = {
				val sid = sessionId
				val reps = repsText.toIntOrNull()
				if (sid != null && reps != null && reps > 0) {
					val entity = SetLogEntity(
						sessionId = sid,
						exerciseName = exercise.name,
						weightKg = weightText.replace(',', '.').toDoubleOrNull() ?: 0.0,
						reps = reps,
						loggedAt = System.currentTimeMillis(),
					)
					scope.launch {
						val id = dao.insertSet(entity)
						logged = logged + entity.copy(id = id)
					}
					RestTimer.start(context, exercise.restSeconds, exercise.name)
				}
			},
			enabled = sessionId != null && (repsText.toIntOrNull() ?: 0) > 0,
			shape = RectangleShape,
		) {
			Text("LOG")
		}
	}
}

private fun blockName(block: Block): String = when (block) {
	is Single -> block.exercise.name
	is Superset -> "${block.a.name} ⇄ ${block.b.name}"
}
