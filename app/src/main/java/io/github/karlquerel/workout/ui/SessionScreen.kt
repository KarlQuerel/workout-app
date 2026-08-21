package io.github.karlquerel.workout.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import io.github.karlquerel.workout.data.db.ProgressPoint
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

	val sessionStart = remember { System.currentTimeMillis() }
	var sessionId by remember { mutableStateOf<Long?>(null) }
	var summary by remember { mutableStateOf<SessionSummary?>(null) }
	LaunchedEffect(Unit) {
		sessionId = dao.insertSession(SessionEntity(dayId = day.id, startedAt = sessionStart))
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
			val sid = sessionId
			val sets = sid?.let { dao.setsForSession(it) }.orEmpty()
			if (sets.isEmpty()) {
				sid?.let { dao.deleteSession(it) }
				onExit()
			} else {
				summary = SessionSummary(
					durationMin = ((System.currentTimeMillis() - sessionStart) / 60_000).toInt(),
					volumeKg = sets.sumOf { it.weightKg * it.reps },
					setCount = sets.size,
					rows = sets.groupBy { it.exerciseName }.map { (name, list) ->
						SummaryRow(
							name = name,
							topKg = list.maxOf { it.weightKg },
							prevTopKg = dao.lastSessionSets(name, sid!!).maxOfOrNull { it.weightKg },
						)
					},
				)
			}
		}
	}

	summary?.let { s ->
		SessionSummaryDialog(
			summary = s,
			onDone = {
				scope.launch {
					sessionId?.let { dao.endSession(it, System.currentTimeMillis()) }
					onExit()
				}
			},
		)
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
	val haptic = LocalHapticFeedback.current
	val rest by RestTimer.state.collectAsState()
	val r = rest ?: return

	var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
	LaunchedEffect(r) {
		while (true) {
			nowMs = System.currentTimeMillis()
			if (nowMs >= r.endAt) {
				haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
	val haptic = LocalHapticFeedback.current

	var showProgress by remember(exercise.name) { mutableStateOf(false) }
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
			(if (lastTime.isEmpty()) "—"
			else lastTime.joinToString("   ") { fmtSet(it.weightKg, it.reps) }) +
			"   📈",
		style = MaterialTheme.typography.bodySmall,
		color = Dim,
		modifier = Modifier.clickable { showProgress = true },
	)
	if (
		lastTime.isNotEmpty() &&
		lastTime.all { it.reps >= REPS_TARGET } &&
		lastTime.last().weightKg > 0.0
	) {
		Spacer(Modifier.height(4.dp))
		Text(
			"hit $REPS_TARGET+ reps on every set last time — try +2.5 kg",
			style = MaterialTheme.typography.bodySmall,
			color = Warn,
		)
	}
	Spacer(Modifier.height(6.dp))

	if (showProgress) {
		ExerciseProgressDialog(exercise = exercise, dao = dao, onClose = { showProgress = false })
	}

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
					haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

// A last session where every set hit this many reps earns a "+2.5 kg" hint.
private const val REPS_TARGET = 12

private data class SummaryRow(val name: String, val topKg: Double, val prevTopKg: Double?)

private data class SessionSummary(
	val durationMin: Int,
	val volumeKg: Double,
	val setCount: Int,
	val rows: List<SummaryRow>,
)

@Composable
private fun SessionSummaryDialog(summary: SessionSummary, onDone: () -> Unit) {
	AlertDialog(
		onDismissRequest = onDone,
		containerColor = CardBg,
		title = { Text("Session done 💪") },
		text = {
			Column {
				Text(
					"${summary.durationMin} min · ${summary.setCount} sets · " +
						"${summary.volumeKg.toInt()} kg total volume",
					style = MaterialTheme.typography.bodyMedium,
					color = Dim,
				)
				Spacer(Modifier.height(12.dp))
				summary.rows.forEach { row ->
					val delta = row.prevTopKg?.let { row.topKg - it }
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(
							row.name,
							style = MaterialTheme.typography.bodyMedium,
							modifier = Modifier.weight(1f),
						)
						Text(
							text = when {
								delta == null -> "${fmtWeight(row.topKg)} kg · new"
								delta > 0 -> "${fmtWeight(row.topKg)} kg ↑${fmtWeight(delta)}"
								delta < 0 -> "${fmtWeight(row.topKg)} kg ↓${fmtWeight(-delta)}"
								else -> "${fmtWeight(row.topKg)} kg ="
							},
							style = MaterialTheme.typography.bodyMedium,
							color = when {
								delta == null -> Dim
								delta > 0 -> Accent
								delta < 0 -> Danger
								else -> Dim
							},
						)
					}
					Spacer(Modifier.height(4.dp))
				}
			}
		},
		confirmButton = { TextButton(onClick = onDone) { Text("Done") } },
	)
}

@Composable
private fun ExerciseProgressDialog(exercise: Exercise, dao: WorkoutDao, onClose: () -> Unit) {
	val points by produceState<List<ProgressPoint>?>(initialValue = null, exercise.name) {
		value = dao.progressFor(exercise.name)
	}
	AlertDialog(
		onDismissRequest = onClose,
		containerColor = CardBg,
		title = { Text(exercise.name) },
		text = {
			val p = points
			when {
				p == null -> Text("Loading…", color = Dim)
				p.size < 2 -> Text(
					"Not enough sessions yet — log this exercise a few more times to see the trend.",
					color = Dim,
				)
				else -> ProgressChart(points = p, color = muscleColor(exercise.muscle))
			}
		},
		confirmButton = { TextButton(onClick = onClose) { Text("Close") } },
	)
}
