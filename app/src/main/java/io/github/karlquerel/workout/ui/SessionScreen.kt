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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.karlquerel.workout.WorkoutApp
import io.github.karlquerel.workout.data.Block
import io.github.karlquerel.workout.data.Effort
import io.github.karlquerel.workout.data.Exercise
import io.github.karlquerel.workout.data.Single
import io.github.karlquerel.workout.data.Superset
import io.github.karlquerel.workout.data.dayById
import io.github.karlquerel.workout.data.db.ProgressPoint
import io.github.karlquerel.workout.data.db.SessionEntity
import io.github.karlquerel.workout.data.db.SetLogEntity
import io.github.karlquerel.workout.data.db.WorkoutDao
import io.github.karlquerel.workout.timer.RestTimer
import io.github.karlquerel.workout.ui.theme.Amber
import io.github.karlquerel.workout.ui.theme.Better
import io.github.karlquerel.workout.ui.theme.Control
import io.github.karlquerel.workout.ui.theme.Danger
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Faint
import io.github.karlquerel.workout.ui.theme.Heat
import io.github.karlquerel.workout.ui.theme.HeatBrush
import io.github.karlquerel.workout.ui.theme.Ink
import io.github.karlquerel.workout.ui.theme.Numeric
import io.github.karlquerel.workout.ui.theme.OnHeat
import io.github.karlquerel.workout.ui.theme.Tile
import io.github.karlquerel.workout.ui.theme.TileRaised
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
				.padding(start = 20.dp, end = 8.dp, top = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			KickerText("${day.kind} · ${blockIndex + 1} / ${day.blocks.size}")
			Text(
				"End",
				style = MaterialTheme.typography.labelLarge,
				fontSize = 16.sp,
				color = Danger,
				modifier = Modifier
					.clip(RoundedCornerShape(12.dp))
					.clickable(onClick = finish)
					.padding(horizontal = 16.dp, vertical = 14.dp),
			)
		}

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp),
			horizontalArrangement = Arrangement.spacedBy(3.dp),
		) {
			day.blocks.indices.forEach { i ->
				Box(
					Modifier
						.weight(1f)
						.height(4.dp)
						.clip(RoundedCornerShape(2.dp))
						.then(if (i <= blockIndex) Modifier.background(HeatBrush) else Modifier.background(Control))
				)
			}
		}
		Spacer(Modifier.height(12.dp))

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 14.dp),
		) {
			when (block) {
				is Single -> ExerciseCard(block.exercise, sessionId, dao)
				is Superset -> {
					KickerText("Superset · alternate A and B", Modifier.padding(start = 6.dp), color = Amber)
					Spacer(Modifier.height(8.dp))
					ExerciseCard(block.a, sessionId, dao, step = "A")
					Spacer(Modifier.height(8.dp))
					ExerciseCard(block.b, sessionId, dao, step = "B")
				}
			}
			Spacer(Modifier.height(16.dp))
		}

		// Pinned below the scroll area so starting a rest never shifts the exercise.
		RestBar()

		if (!isLast) {
			KickerText(
				"next up · ${blockName(day.blocks[blockIndex + 1])}",
				Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
			)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			QuietButton(
				text = "Prev",
				onClick = { blockIndex-- },
				enabled = blockIndex > 0,
				modifier = Modifier.weight(1f),
			)
			QuietButton(
				text = if (isLast) "Finish" else "Next",
				onClick = { if (isLast) finish() else blockIndex++ },
				modifier = Modifier.weight(1f),
			)
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
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 14.dp)
			.clip(RoundedCornerShape(18.dp))
			.background(Tile)
			.clickable { RestTimer.cancel(context) }
			.padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		HeatRing(
			progress = remaining.toFloat() / r.totalSeconds,
			size = 58.dp,
			stroke = 6.dp,
			urgent = urgent,
		)
		Column(Modifier.weight(1f)) {
			Text(
				fmtClock(remaining),
				style = Numeric,
				fontSize = 26.sp,
				fontWeight = FontWeight.SemiBold,
				color = if (urgent) Danger else Ink,
			)
			Text("rest · tap to skip", style = MaterialTheme.typography.bodySmall, color = Dim)
		}
		Text(
			"+15s",
			style = MaterialTheme.typography.labelLarge,
			fontSize = 16.sp,
			color = Ink,
			modifier = Modifier
				.clip(RoundedCornerShape(10.dp))
				.background(Control)
				.clickable { RestTimer.extend(context, 15) }
				.padding(horizontal = 18.dp, vertical = 16.dp),
		)
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

	TileCard(Modifier.fillMaxWidth()) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Column(Modifier.weight(1f)) {
				Text(
					text = (step?.let { "$it · " } ?: "") + exercise.name,
					style = MaterialTheme.typography.headlineSmall,
				)
				Spacer(Modifier.height(6.dp))
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(6.dp),
					verticalArrangement = Arrangement.spacedBy(6.dp),
				) {
					Chip(exercise.muscle.label.lowercase(), Dim)
					Chip(exercise.effort.label.lowercase(), if (exercise.effort == Effort.HARD) Heat else Dim)
					if (exercise.dropSet) Chip("drop set", Amber)
					Chip(
						if (exercise.isWarmup) "for ${fmtClock(exercise.restSeconds)}"
						else "rest ${fmtClock(exercise.restSeconds)}",
						Dim,
					)
				}
			}
			BodyMap(
				side = sideShowing(exercise.muscle),
				colorOf = highlightOnly(exercise.muscle, Heat),
				modifier = Modifier
					.padding(start = 8.dp)
					.width(44.dp)
					.height(76.dp),
			)
		}
		exercise.cues.forEach { cue ->
			Text(
				"${cue.title}: ${cue.text}",
				style = MaterialTheme.typography.bodySmall,
				color = Dim,
			)
		}

		if (exercise.isWarmup) {
			Spacer(Modifier.height(6.dp))
			GradientButton(
				text = "Start ${fmtClock(exercise.restSeconds)} timer",
				onClick = { RestTimer.start(context, exercise.restSeconds, exercise.name) },
				modifier = Modifier.fillMaxWidth(),
			)
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
	var weight by rememberSaveable(exercise.name) { mutableDoubleStateOf(0.0) }
	var reps by rememberSaveable(exercise.name) { mutableIntStateOf(DEFAULT_REPS) }
	var prefilled by rememberSaveable(exercise.name) { mutableStateOf(false) }
	var editing by remember { mutableStateOf<EditField?>(null) }
	var pendingDelete by remember { mutableStateOf<Long?>(null) }

	LaunchedEffect(sessionId) {
		val sid = sessionId ?: return@LaunchedEffect
		logged = dao.setsForSession(sid).filter { it.exerciseName == exercise.name }
		lastTime = dao.lastSessionSets(exercise.name, sid)
		// Progressive overload: start from last session's final working weight.
		if (!prefilled) {
			lastTime.lastOrNull()?.let { weight = it.weightKg }
			lastTime.getOrNull(logged.size)?.let { reps = it.reps }
			prefilled = true
		}
	}

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		KickerText(
			if (lastTime.isEmpty()) "last time · none"
			else "last time · " + lastTime.joinToString("  ") { fmtSet(it.weightKg, it.reps) },
			Modifier.weight(1f),
		)
		KickerText(
			"progress ›",
			Modifier
				.clickable { showProgress = true }
				.padding(start = 12.dp, top = 14.dp, bottom = 14.dp),
			color = Heat,
		)
	}
	if (
		lastTime.isNotEmpty() &&
		lastTime.all { it.reps >= REPS_TARGET } &&
		lastTime.last().weightKg > 0.0
	) {
		Text(
			"Hit $REPS_TARGET+ reps on every set last time. Try +2.5 kg.",
			style = MaterialTheme.typography.bodySmall,
			color = Amber,
		)
	}

	if (showProgress) {
		ExerciseProgressDialog(exercise = exercise, dao = dao, onClose = { showProgress = false })
	}

	SetRow(index = null, last = "LAST", weight = "KG", reps = "REPS", header = true)
	logged.forEachIndexed { index, set ->
		SetRow(
			index = index + 1,
			last = lastTime.getOrNull(index)?.let { fmtSet(it.weightKg, it.reps) } ?: "–",
			weight = fmtWeight(set.weightKg),
			reps = set.reps.toString(),
			done = true,
			confirmingDelete = pendingDelete == set.id,
			onClick = { pendingDelete = if (pendingDelete == set.id) null else set.id },
			onDelete = {
				scope.launch {
					dao.deleteSet(set.id)
					logged = logged.filterNot { it.id == set.id }
					pendingDelete = null
				}
			},
		)
	}
	SetRow(
		index = logged.size + 1,
		last = lastTime.getOrNull(logged.size)?.let { fmtSet(it.weightKg, it.reps) } ?: "–",
		weight = fmtWeight(weight),
		reps = reps.toString(),
		current = true,
	)

	Stepper(
		value = fmtWeight(weight),
		unit = "kg",
		onMinus = { weight = (weight - WEIGHT_STEP).coerceAtLeast(0.0) },
		onPlus = { weight += WEIGHT_STEP },
		onPick = { editing = EditField.WEIGHT },
		modifier = Modifier.fillMaxWidth(),
	)
	Stepper(
		value = reps.toString(),
		unit = "reps",
		onMinus = { reps = (reps - 1).coerceAtLeast(1) },
		onPlus = { reps++ },
		onPick = { editing = EditField.REPS },
		modifier = Modifier.fillMaxWidth(),
	)

	GradientButton(
		text = "Log set ${logged.size + 1}",
		enabled = sessionId != null,
		modifier = Modifier.fillMaxWidth(),
		onClick = {
			val sid = sessionId ?: return@GradientButton
			val entity = SetLogEntity(
				sessionId = sid,
				exerciseName = exercise.name,
				weightKg = weight,
				reps = reps,
				loggedAt = System.currentTimeMillis(),
			)
			scope.launch {
				val id = dao.insertSet(entity)
				logged = logged + entity.copy(id = id)
				lastTime.getOrNull(logged.size)?.let { reps = it.reps }
			}
			haptic.performHapticFeedback(HapticFeedbackType.LongPress)
			RestTimer.start(context, exercise.restSeconds, exercise.name)
		},
	)

	when (editing) {
		EditField.WEIGHT -> WeightPickerDialog(
			initialKg = weight,
			onDismiss = { editing = null },
			onConfirm = {
				weight = it
				editing = null
			},
		)
		EditField.REPS -> RepsPickerDialog(
			initialReps = reps,
			onDismiss = { editing = null },
			onConfirm = {
				reps = it
				editing = null
			},
		)
		null -> Unit
	}
}

@Composable
private fun SetRow(
	index: Int?,
	last: String,
	weight: String,
	reps: String,
	header: Boolean = false,
	done: Boolean = false,
	current: Boolean = false,
	confirmingDelete: Boolean = false,
	onClick: (() -> Unit)? = null,
	onDelete: () -> Unit = {},
) {
	val shape = RoundedCornerShape(12.dp)
	var modifier = Modifier.fillMaxWidth()
	if (!header) {
		modifier = modifier.clip(shape).background(TileRaised)
		if (current) modifier = modifier.border(1.5.dp, Heat, shape)
		if (onClick != null) modifier = modifier.clickable(onClick = onClick)
	}
	Row(
		modifier = modifier.padding(horizontal = 12.dp, vertical = if (header) 0.dp else 13.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		if (header) {
			KickerText("#", Modifier.width(18.dp))
			KickerText(last, Modifier.weight(1f))
			KickerText(weight, Modifier.width(48.dp))
			KickerText(reps, Modifier.width(36.dp))
			Spacer(Modifier.width(26.dp))
			return@Row
		}
		Text("$index", style = Numeric, fontSize = 12.sp, color = Faint, modifier = Modifier.width(18.dp))
		Text(last, style = Numeric, fontSize = 12.sp, color = Faint, modifier = Modifier.weight(1f))
		Text(weight, style = Numeric, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(48.dp))
		Text(reps, style = Numeric, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(36.dp))
		when {
			confirmingDelete -> Text(
				"Delete",
				style = MaterialTheme.typography.labelLarge,
				color = Danger,
				modifier = Modifier
					.clip(RoundedCornerShape(8.dp))
					.clickable(onClick = onDelete)
					.padding(horizontal = 10.dp, vertical = 10.dp),
			)
			done -> Box(
				Modifier
					.size(26.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(HeatBrush),
				contentAlignment = Alignment.Center,
			) { Text("✓", color = OnHeat, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
			else -> Box(
				Modifier
					.size(26.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(Control),
			)
		}
	}
}

private enum class EditField { WEIGHT, REPS }

private fun blockName(block: Block): String = when (block) {
	is Single -> block.exercise.name
	is Superset -> "${block.a.name} + ${block.b.name}"
}

// A last session where every set hit this many reps earns a "+2.5 kg" hint.
private const val REPS_TARGET = 12
private const val DEFAULT_REPS = 10
private const val WEIGHT_STEP = 2.5

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
		containerColor = Tile,
		title = { Text("Session done") },
		text = {
			Column {
				Text(
					"${summary.durationMin} min · ${summary.setCount} sets · " +
						"${fmtVolume(summary.volumeKg)} volume",
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
							style = Numeric,
							fontSize = 14.sp,
							color = when {
								delta == null -> Dim
								delta > 0 -> Better
								delta < 0 -> Danger
								else -> Dim
							},
						)
					}
					Spacer(Modifier.height(4.dp))
				}
			}
		},
		confirmButton = { TextButton(onClick = onDone) { Text("Done", color = Heat) } },
	)
}

@Composable
private fun ExerciseProgressDialog(exercise: Exercise, dao: WorkoutDao, onClose: () -> Unit) {
	val points by produceState<List<ProgressPoint>?>(initialValue = null, exercise.name) {
		value = dao.progressFor(exercise.name)
	}
	AlertDialog(
		onDismissRequest = onClose,
		containerColor = Tile,
		title = { Text(exercise.name) },
		text = {
			val p = points
			when {
				p == null -> Text("Loading…", color = Dim)
				p.size < 2 -> Text(
					"Not enough sessions yet. Log this exercise a few more times to see the trend.",
					color = Dim,
				)
				else -> ProgressChart(points = p, color = Heat)
			}
		},
		confirmButton = { TextButton(onClick = onClose) { Text("Close", color = Heat) } },
	)
}
