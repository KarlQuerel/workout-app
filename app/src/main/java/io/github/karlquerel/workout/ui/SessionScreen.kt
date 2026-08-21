package io.github.karlquerel.workout.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import io.github.karlquerel.workout.ui.theme.Accent
import io.github.karlquerel.workout.ui.theme.CardBg
import io.github.karlquerel.workout.ui.theme.Danger
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Line
import io.github.karlquerel.workout.ui.theme.PixelFont
import io.github.karlquerel.workout.ui.theme.Warn
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
	val accent = muscleColor(day.accent)

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
				.padding(horizontal = 20.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column {
				Text(
					day.kind,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
				)
				Text(
					"block ${blockIndex + 1} of ${day.blocks.size}",
					style = MaterialTheme.typography.bodySmall,
					color = Dim,
				)
			}
			TextButton(onClick = finish) { Text("End", color = Danger) }
		}

		// One segment per block — where you are in the session at a glance.
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
		) {
			day.blocks.indices.forEach { i ->
				Box(
					Modifier
						.weight(1f)
						.height(4.dp)
						.clip(RoundedCornerShape(2.dp))
						.background(if (i <= blockIndex) accent else Line)
				)
			}
		}
		Spacer(Modifier.height(12.dp))

		RestBar()

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp),
		) {
			when (block) {
				is Single -> ExerciseCard(block.exercise, sessionId, dao)
				is Superset -> {
					Text(
						"Superset — alternate A ↔ B",
						style = MaterialTheme.typography.labelLarge,
						color = Warn,
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
				"next up · ${blockName(day.blocks[blockIndex + 1])}",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
				modifier = Modifier.padding(horizontal = 20.dp),
			)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
		) {
			OutlinedButton(
				onClick = { blockIndex-- },
				enabled = blockIndex > 0,
				modifier = Modifier.weight(1f),
			) {
				Text("◀ Prev")
			}
			Button(
				onClick = { if (isLast) finish() else blockIndex++ },
				modifier = Modifier.weight(1f),
			) {
				Text(if (isLast) "Finish" else "Next ▶")
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
	val urgent = remaining <= 5
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 20.dp)
			.clip(RoundedCornerShape(14.dp))
			.background(CardBg)
			.border(1.dp, if (urgent) Danger else Accent, RoundedCornerShape(14.dp))
			.padding(14.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text("rest · ${r.label}", style = MaterialTheme.typography.labelMedium, color = Dim)
		Text(
			fmtClock(remaining),
			fontFamily = PixelFont,
			fontSize = 72.sp,
			color = if (urgent) Danger else Accent,
		)
		Spacer(Modifier.height(6.dp))
		LinearProgressIndicator(
			progress = { remaining.toFloat() / r.totalSeconds },
			modifier = Modifier.fillMaxWidth(),
			color = if (urgent) Danger else Accent,
			trackColor = Line,
		)
		TextButton(onClick = { RestTimer.cancel(context) }) { Text("Skip", color = Dim) }
	}
	Spacer(Modifier.height(12.dp))
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

	AccentCard(accent = accent) {
		Text(
			text = (step?.let { "$it · " } ?: "") + exercise.name,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
		)
		Spacer(Modifier.height(8.dp))
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp),
		) {
			Chip(exercise.muscle.label.lowercase(), accent)
			Chip(exercise.effort.label.lowercase(), effortColor(exercise.effort))
			if (exercise.dropSet) Chip("drop set", Warn)
			Chip(
				if (exercise.isWarmup) "for ${fmtClock(exercise.restSeconds)}"
				else "rest ${fmtClock(exercise.restSeconds)}",
				Dim,
			)
		}
		Spacer(Modifier.height(10.dp))
		exercise.cues.forEach { cue ->
			Text(
				"•  ${cue.title} — ${cue.text}",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
			)
			Spacer(Modifier.height(4.dp))
		}

		if (exercise.isWarmup) {
			Spacer(Modifier.height(10.dp))
			Button(
				onClick = { RestTimer.start(context, exercise.restSeconds, exercise.name) },
				modifier = Modifier.fillMaxWidth(),
			) {
				Text("Start ${fmtClock(exercise.restSeconds)} timer")
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
		text = "last time · " +
			if (lastTime.isEmpty()) "—"
			else lastTime.joinToString("   ") { fmtSet(it.weightKg, it.reps) },
		style = MaterialTheme.typography.bodySmall,
		color = Dim,
	)
	Spacer(Modifier.height(6.dp))

	logged.forEachIndexed { index, set ->
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				"set ${index + 1}    ${fmtSet(set.weightKg, set.reps)}",
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.SemiBold,
				color = Accent,
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

	Spacer(Modifier.height(4.dp))
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
			modifier = Modifier.weight(1f),
		)
		OutlinedTextField(
			value = repsText,
			onValueChange = { repsText = it },
			label = { Text("reps") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
		) {
			Text("Log")
		}
	}
}

private fun blockName(block: Block): String = when (block) {
	is Single -> block.exercise.name
	is Superset -> "${block.a.name} ⇄ ${block.b.name}"
}
