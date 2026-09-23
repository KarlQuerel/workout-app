package io.github.karlquerel.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.karlquerel.workout.ui.theme.Control
import io.github.karlquerel.workout.ui.theme.Dim
import io.github.karlquerel.workout.ui.theme.Ink
import io.github.karlquerel.workout.ui.theme.Numeric
import io.github.karlquerel.workout.ui.theme.Tile
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val ITEM_HEIGHT = 52.dp
private const val VISIBLE = 5
private const val PAD = VISIBLE / 2
private const val MAX_KG = 300
private const val MAX_REPS = 100
private val QUARTERS = listOf(".00", ".25", ".50", ".75")

// Scroll-and-snap picker; the centered row is the value, a tap on any row centers it.
@Composable
fun WheelPicker(
	values: List<String>,
	initialIndex: Int,
	onSelect: (Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
	val fling = rememberSnapFlingBehavior(state, SnapPosition.Start)
	val scope = rememberCoroutineScope()
	val haptic = LocalHapticFeedback.current
	val itemPx = with(LocalDensity.current) { ITEM_HEIGHT.toPx() }

	// Spacer rows above the values mean the top row's index is the centered value's index.
	val selected by remember {
		derivedStateOf {
			val top = state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > itemPx / 2) 1 else 0
			top.coerceIn(0, values.lastIndex)
		}
	}
	var last by remember { mutableIntStateOf(initialIndex) }
	LaunchedEffect(selected) {
		if (selected != last) {
			last = selected
			haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
			onSelect(selected)
		}
	}

	Box(modifier.height(ITEM_HEIGHT * VISIBLE), contentAlignment = Alignment.Center) {
		Box(
			Modifier
				.fillMaxWidth()
				.height(ITEM_HEIGHT)
				.clip(RoundedCornerShape(14.dp))
				.background(Control)
		)
		LazyColumn(
			state = state,
			flingBehavior = fling,
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			items(PAD) { Spacer(Modifier.height(ITEM_HEIGHT)) }
			itemsIndexed(values) { index, value ->
				val distance = abs(index - selected)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(ITEM_HEIGHT)
						.clickable { scope.launch { state.animateScrollToItem(index) } },
					contentAlignment = Alignment.Center,
				) {
					Text(
						value,
						style = Numeric,
						fontSize = if (distance == 0) 30.sp else 22.sp,
						fontWeight = if (distance == 0) FontWeight.SemiBold else FontWeight.Normal,
						color = Ink.copy(alpha = listOf(1f, 0.45f, 0.2f).getOrElse(distance) { 0.1f }),
					)
				}
			}
			items(PAD) { Spacer(Modifier.height(ITEM_HEIGHT)) }
		}
	}
}

@Composable
private fun PickerDialog(
	title: String,
	hint: String?,
	onDismiss: () -> Unit,
	onConfirm: () -> Unit,
	wheels: @Composable () -> Unit,
) {
	Dialog(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier
				.clip(RoundedCornerShape(24.dp))
				.background(Tile)
				.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp),
		) {
			KickerText(title)
			wheels()
			hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Dim) }
			Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
				QuietButton("Cancel", onDismiss, Modifier.weight(1f))
				GradientButton("Set", onConfirm, Modifier.weight(1f))
			}
		}
	}
}

@Composable
fun WeightPickerDialog(initialKg: Double, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
	val startWhole = floor(initialKg).toInt().coerceIn(0, MAX_KG)
	val startQuarter = ((initialKg - startWhole) * 4).roundToInt().coerceIn(0, QUARTERS.lastIndex)
	var whole by remember { mutableIntStateOf(startWhole) }
	var quarter by remember { mutableIntStateOf(startQuarter) }
	val wholeValues = remember { (0..MAX_KG).map(Int::toString) }

	PickerDialog(
		title = "Weight",
		hint = "0 kg logs as bodyweight",
		onDismiss = onDismiss,
		onConfirm = { onConfirm(whole + quarter * 0.25) },
	) {
		Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			WheelPicker(wholeValues, startWhole, { whole = it }, Modifier.weight(1.3f))
			WheelPicker(QUARTERS, startQuarter, { quarter = it }, Modifier.weight(1f))
			KickerText("kg")
		}
	}
}

@Composable
fun RepsPickerDialog(initialReps: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
	val start = (initialReps.coerceIn(1, MAX_REPS)) - 1
	var index by remember { mutableIntStateOf(start) }
	val values = remember { (1..MAX_REPS).map(Int::toString) }

	PickerDialog(
		title = "Reps",
		hint = null,
		onDismiss = onDismiss,
		onConfirm = { onConfirm(index + 1) },
	) {
		WheelPicker(values, start, { index = it }, Modifier.fillMaxWidth())
	}
}
