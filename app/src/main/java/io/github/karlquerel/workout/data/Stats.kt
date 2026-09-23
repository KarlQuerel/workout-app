package io.github.karlquerel.workout.data

import io.github.karlquerel.workout.data.db.SetLogEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Logs store the exercise name only, so the muscle comes from the program.
private val MUSCLE_BY_EXERCISE: Map<String, Muscle> = WEEKLY_SPLIT
	.flatMap { it.blocks }
	.flatMap { block ->
		when (block) {
			is Single -> listOf(block.exercise)
			is Superset -> listOf(block.a, block.b)
		}
	}
	.filterNot { it.isWarmup }
	.associate { it.name to it.muscle }

fun muscleOf(exerciseName: String): Muscle? = MUSCLE_BY_EXERCISE[exerciseName]

fun setsPerMuscle(sets: List<SetLogEntity>, since: Long): Map<Muscle, Int> =
	sets.asSequence()
		.filter { it.loggedAt >= since }
		.mapNotNull { muscleOf(it.exerciseName) }
		.groupingBy { it }
		.eachCount()

fun weekStart(date: LocalDate, zone: ZoneId): Long =
	date.with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant().toEpochMilli()

// Volume in kg per week, oldest first, ending with the current week.
fun weeklyVolumes(sets: List<SetLogEntity>, weeks: Int, today: LocalDate, zone: ZoneId): List<Double> {
	val starts = (weeks - 1 downTo 0).map { weekStart(today.minusWeeks(it.toLong()), zone) }
	return starts.mapIndexed { i, start ->
		val end = starts.getOrNull(i + 1) ?: Long.MAX_VALUE
		sets.filter { it.loggedAt in start until end }.sumOf { it.weightKg * it.reps }
	}
}

// Consecutive weeks with a logged session; the current week joins the run once it has one.
fun weekStreak(sessionStarts: List<Long>, today: LocalDate, zone: ZoneId): Int {
	val weeks = sessionStarts
		.map { weekStart(Instant.ofEpochMilli(it).atZone(zone).toLocalDate(), zone) }
		.toSet()
	var cursor = if (weekStart(today, zone) in weeks) today else today.minusWeeks(1)
	var streak = 0
	while (weekStart(cursor, zone) in weeks) {
		streak++
		cursor = cursor.minusWeeks(1)
	}
	return streak
}

fun daysSinceTrained(sets: List<SetLogEntity>, muscle: Muscle, now: Long): Int? =
	sets.filter { muscleOf(it.exerciseName) == muscle }
		.maxOfOrNull { it.loggedAt }
		?.let { ((now - it) / 86_400_000L).toInt() }
