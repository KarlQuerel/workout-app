package io.github.karlquerel.workout.data

import java.time.DayOfWeek
import java.time.LocalDate

enum class Muscle(val label: String) {
	CHEST("Chest"),
	BACK("Back"),
	TRICEPS("Triceps"),
	BICEPS("Biceps"),
	SHOULDERS("Shoulders"),
	LEGS("Legs"),
	ABS("Abs"),
	WAIST("Waist"),
	FOREARMS("Forearms"),
}

enum class Effort(val label: String) {
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard"),
}

data class Cue(val title: String, val text: String)

data class Exercise(
	val name: String,
	val muscle: Muscle,
	val effort: Effort,
	// Rest between sets; for warm-up blocks this is the block duration.
	val restSeconds: Int,
	val isWarmup: Boolean = false,
	val dropSet: Boolean = false,
	val cues: List<Cue> = emptyList(),
)

sealed interface Block

data class Single(val exercise: Exercise) : Block

data class Superset(val a: Exercise, val b: Exercise) : Block

data class WorkoutDay(
	val id: String,
	val title: String,
	val kind: String,
	val accent: Muscle,
	val muscles: List<Muscle>,
	val blocks: List<Block>,
)

val BASE_WARMUP = Exercise(
	name = "Base Warm-up",
	muscle = Muscle.SHOULDERS,
	effort = Effort.EASY,
	restSeconds = 300,
	isWarmup = true,
	cues = listOf(
		Cue("Joint Circles", "Wrists, neck, and elbows — smooth circles in both directions"),
		Cue("Shoulders", "Goalpost rotations and band pulleys — light tension, full range"),
	),
)

val WEEKLY_SPLIT = listOf(
	WorkoutDay(
		id = "monday",
		title = "MONDAY",
		kind = "Push",
		accent = Muscle.CHEST,
		muscles = listOf(Muscle.CHEST, Muscle.SHOULDERS, Muscle.TRICEPS, Muscle.LEGS, Muscle.ABS),
		blocks = listOf(
			Single(BASE_WARMUP),
			Single(
				Exercise(
					name = "Hack Squat Calf Raises",
					muscle = Muscle.LEGS,
					effort = Effort.HARD,
					restSeconds = 90,
					cues = listOf(
						Cue("Setup", "Face outward on the hack squat; legs straight with soft knees"),
						Cue("Form", "Rise onto your toes; pause at the bottom for a deep calf stretch"),
					),
				)
			),
			Single(
				Exercise(
					name = "Seated Calf Raises",
					muscle = Muscle.LEGS,
					effort = Effort.EASY,
					restSeconds = 60,
					cues = listOf(
						Cue("Focus", "Lighter weight, higher reps — chase the pump, not max load"),
					),
				)
			),
			Single(
				Exercise(
					name = "Specific Warm-up",
					muscle = Muscle.SHOULDERS,
					effort = Effort.EASY,
					restSeconds = 180,
					isWarmup = true,
					cues = listOf(
						Cue("Drill", "Scapular push-ups — 12 slow reps, protract and retract fully"),
					),
				)
			),
			Single(
				Exercise(
					name = "Cable Flyes",
					muscle = Muscle.CHEST,
					effort = Effort.EASY,
					restSeconds = 60,
					cues = listOf(
						Cue("Setup", "Arms wide; cables at chest height"),
						Cue("Tempo", "2-second pause at the deepest stretch"),
						Cue("Focus", "Squeeze your chest at the top — not your hands"),
					),
				)
			),
			Single(
				Exercise(
					name = "Incline Bench Press",
					muscle = Muscle.CHEST,
					effort = Effort.HARD,
					restSeconds = 180,
					dropSet = true,
					cues = listOf(
						Cue("Setup", "Bench at setting 2 with a weight plate under the back"),
						Cue("Form", "Pin shoulder blades to the bench throughout the set"),
						Cue("Cue", "Drive feet into the floor for stability on heavy sets"),
					),
				)
			),
			Single(
				Exercise(
					name = "Weighted Dips",
					muscle = Muscle.CHEST,
					effort = Effort.HARD,
					restSeconds = 180,
					dropSet = true,
					cues = listOf(
						Cue("Form", "Lean forward slightly; flare elbows to target the chest"),
						Cue("Range", "Lower until shoulders sit just below elbows — no bouncing"),
					),
				)
			),
			Single(
				Exercise(
					name = "Skullcrushers",
					muscle = Muscle.TRICEPS,
					effort = Effort.MEDIUM,
					restSeconds = 120,
					dropSet = true,
					cues = listOf(
						Cue("Form", "Lower dumbbells to ear level behind the head"),
						Cue("Grip", "Neutral (palms facing) to protect wrists and elbows"),
						Cue("Cue", "Keep elbows tucked and fixed in place"),
					),
				)
			),
			Superset(
				a = Exercise(
					name = "Lateral Raises",
					muscle = Muscle.SHOULDERS,
					effort = Effort.EASY,
					restSeconds = 10,
					cues = listOf(
						Cue("Form", "Lead with elbows — hands stay below elbow height"),
						Cue("Tempo", "Slow on the way down; no swinging or momentum"),
					),
				),
				b = Exercise(
					name = "Incline Rear Delt Fly",
					muscle = Muscle.SHOULDERS,
					effort = Effort.EASY,
					restSeconds = 120,
					cues = listOf(
						Cue("Setup", "Incline bench at setting 2; chest on the pad"),
						Cue("Cue", "Pinky finger is the highest point of each hand"),
					),
				),
			),
		),
	),
	WorkoutDay(
		id = "tuesday",
		title = "TUESDAY",
		kind = "Pull",
		accent = Muscle.BACK,
		muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.FOREARMS, Muscle.SHOULDERS, Muscle.ABS),
		blocks = listOf(
			Single(BASE_WARMUP),
			Single(
				Exercise(
					name = "Specific Warm-up",
					muscle = Muscle.BACK,
					effort = Effort.EASY,
					restSeconds = 180,
					isWarmup = true,
					cues = listOf(
						Cue("Drill", "Dead hangs — 2 sets × 45 seconds; relax the shoulders"),
					),
				)
			),
			Single(
				Exercise(
					name = "Bodyweight Pull-ups",
					muscle = Muscle.BACK,
					effort = Effort.MEDIUM,
					restSeconds = 180,
					cues = listOf(
						Cue("Volume", "3 sets × ~12 clean reps; full range, no kipping"),
					),
				)
			),
			Single(
				Exercise(
					name = "Single-Arm Machine Row",
					muscle = Muscle.BACK,
					effort = Effort.HARD,
					restSeconds = 180,
					dropSet = true,
					cues = listOf(
						Cue("Form", "Start with the left arm; match reps on the right for symmetry"),
						Cue("Cue", "Sternum glued to the pad — pull elbow to hip, hands are just hooks"),
						Cue("Setup", "Grip the machine frame with your free hand for stability"),
					),
				)
			),
			Single(
				Exercise(
					name = "Neutral-Grip Lat Pulldown",
					muscle = Muscle.BACK,
					effort = Effort.MEDIUM,
					restSeconds = 120,
					dropSet = true,
					cues = listOf(
						Cue("Focus", "Pull with elbows; avoid leaning back excessively"),
						Cue("Form", "Squeeze shoulder blades down at the bottom of each rep"),
					),
				)
			),
			Superset(
				a = Exercise(
					name = "Straight Arm Pulldown",
					muscle = Muscle.BACK,
					effort = Effort.EASY,
					restSeconds = 10,
					cues = listOf(
						Cue("Form", "Flat back, abs braced — don't arch or stick hips out"),
						Cue("Cue", "Slight bend in elbows; pull with lats, not arms"),
					),
				),
				b = Exercise(
					name = "Bayesian Curl",
					muscle = Muscle.BICEPS,
					effort = Effort.EASY,
					restSeconds = 120,
					cues = listOf(
						Cue("Setup", "Step forward from the pulley for constant tension"),
						Cue("Form", "Full stretch behind the body at the bottom of each rep"),
					),
				),
			),
			Single(
				Exercise(
					name = "Hammer Curl",
					muscle = Muscle.FOREARMS,
					effort = Effort.EASY,
					restSeconds = 60,
					dropSet = true,
					cues = listOf(
						Cue("Cue", "Crush the handles hard for forearm activation"),
						Cue("Tempo", "Controlled negatives — no swinging at the shoulder"),
					),
				)
			),
			Single(
				Exercise(
					name = "Dumbbell Shrugs",
					muscle = Muscle.SHOULDERS,
					effort = Effort.EASY,
					restSeconds = 120,
					cues = listOf(
						Cue("Volume", "3 sets × 12–15 reps"),
						Cue("Tempo", "2-second hold at the top of each shrug"),
					),
				)
			),
		),
	),
	WorkoutDay(
		id = "thursday",
		title = "THURSDAY",
		kind = "Legs + Shoulders",
		accent = Muscle.LEGS,
		muscles = listOf(Muscle.LEGS, Muscle.SHOULDERS, Muscle.ABS),
		blocks = listOf(
			Single(BASE_WARMUP),
			Single(
				Exercise(
					name = "Specific Warm-up",
					muscle = Muscle.LEGS,
					effort = Effort.EASY,
					restSeconds = 180,
					isWarmup = true,
					cues = listOf(
						Cue("Drill", "Leg swings — 15 reps per leg, front-to-back and side-to-side"),
						Cue("Drill", "Deep goblet squat hold — 45 seconds, elbows push knees out"),
					),
				)
			),
			Single(
				Exercise(
					name = "Seated Calf Raises",
					muscle = Muscle.LEGS,
					effort = Effort.HARD,
					restSeconds = 90,
					cues = listOf(
						Cue("Tempo", "2-second pause at the bottom; explosive up, 3-second eccentric"),
						Cue("Form", "Squeeze hard at the top; drive through the big toe"),
					),
				)
			),
			Single(
				Exercise(
					name = "Hack Squat Calf Raises",
					muscle = Muscle.LEGS,
					effort = Effort.EASY,
					restSeconds = 60,
					cues = listOf(
						Cue("Focus", "Lighter weight, high reps — flush the muscle with blood"),
					),
				)
			),
			Single(
				Exercise(
					name = "Hack Squat",
					muscle = Muscle.LEGS,
					effort = Effort.HARD,
					restSeconds = 180,
					cues = listOf(
						Cue("Form", "Full depth — ass to ground without rounding the lower back"),
						Cue("Cue", "Feet mid-platform; push through heels on the way up"),
					),
				)
			),
			Single(
				Exercise(
					name = "Leg Extension",
					muscle = Muscle.LEGS,
					effort = Effort.MEDIUM,
					restSeconds = 120,
					dropSet = true,
					cues = listOf(
						Cue("Tempo", "1-second hold at the top; slow controlled eccentric"),
						Cue("Focus", "Point toes up at the top to fully contract the quads"),
					),
				)
			),
			Single(
				Exercise(
					name = "Leg Curl",
					muscle = Muscle.LEGS,
					effort = Effort.EASY,
					restSeconds = 60,
					dropSet = true,
					cues = listOf(
						Cue("Focus", "Feel the hamstrings working — don't lean forward into the pad"),
						Cue("Tempo", "Pause briefly at the top; control the weight on the way down"),
					),
				)
			),
			Single(
				Exercise(
					name = "Hip Abduction",
					muscle = Muscle.LEGS,
					effort = Effort.EASY,
					restSeconds = 60,
					cues = listOf(
						Cue("Focus", "Outer hip and glute medius — press knees out against the pads"),
					),
				)
			),
			Single(
				Exercise(
					name = "Overhead Press",
					muscle = Muscle.SHOULDERS,
					effort = Effort.MEDIUM,
					restSeconds = 120,
					cues = listOf(
						Cue("Setup", "Seated on bench at setting 3; dumbbells at shoulder height"),
						Cue("Form", "Press straight up; don't flare elbows excessively"),
					),
				)
			),
		),
	),
	WorkoutDay(
		id = "friday",
		title = "FRIDAY",
		kind = "Upper Body",
		accent = Muscle.TRICEPS,
		muscles = listOf(Muscle.CHEST, Muscle.BACK, Muscle.BICEPS, Muscle.TRICEPS, Muscle.FOREARMS),
		blocks = listOf(
			Single(BASE_WARMUP),
			Single(
				Exercise(
					name = "Specific Warm-up",
					muscle = Muscle.CHEST,
					effort = Effort.EASY,
					restSeconds = 180,
					isWarmup = true,
					cues = listOf(
						Cue("Drill", "Push-ups — 15 reps, full range, controlled tempo"),
					),
				)
			),
			Single(
				Exercise(
					name = "Bench Press",
					muscle = Muscle.CHEST,
					effort = Effort.HARD,
					restSeconds = 180,
					dropSet = true,
					cues = listOf(
						Cue("Form", "Pin shoulder blades back and down before each rep"),
						Cue("Cue", "Drive feet into the floor; touch chest to the bar"),
					),
				)
			),
			Single(
				Exercise(
					name = "Bodyweight Pull-ups",
					muscle = Muscle.BACK,
					effort = Effort.HARD,
					restSeconds = 180,
					dropSet = true,
					cues = listOf(
						Cue("Volume", "Work to failure — every rep counts"),
						Cue("Form", "Full hang at the bottom; chin clears the bar at the top"),
					),
				)
			),
			Single(
				Exercise(
					name = "Pec Flyes",
					muscle = Muscle.CHEST,
					effort = Effort.MEDIUM,
					restSeconds = 120,
					dropSet = true,
					cues = listOf(
						Cue("Setup", "Machine or cable — whichever gives the best stretch"),
						Cue("Focus", "Squeeze chest at the top; don't let shoulders roll forward"),
					),
				)
			),
			Superset(
				a = Exercise(
					name = "Overhead Cable Triceps Extension",
					muscle = Muscle.TRICEPS,
					effort = Effort.MEDIUM,
					restSeconds = 15,
					cues = listOf(
						Cue("Setup", "Low pulley; rope or bar behind the head"),
						Cue("Focus", "Deep stretch at the bottom of each rep"),
					),
				),
				b = Exercise(
					name = "Cable Rope Pushdowns",
					muscle = Muscle.TRICEPS,
					effort = Effort.MEDIUM,
					restSeconds = 90,
					cues = listOf(
						Cue("Setup", "High pulley with rope attachment"),
						Cue("Form", "Split the rope apart at full extension"),
					),
				),
			),
			Superset(
				a = Exercise(
					name = "Seated Bicep Curl",
					muscle = Muscle.BICEPS,
					effort = Effort.EASY,
					restSeconds = 15,
					cues = listOf(
						Cue("Form", "Clean reps with controlled negatives — no body English"),
					),
				),
				b = Exercise(
					name = "High Plank",
					muscle = Muscle.WAIST,
					effort = Effort.MEDIUM,
					restSeconds = 90,
					cues = listOf(
						Cue("Volume", "Hold until failure"),
						Cue("Cue", "Pull belly button in; maintain a straight line head to heels"),
					),
				),
			),
		),
	),
)

fun dayById(id: String): WorkoutDay? = WEEKLY_SPLIT.find { it.id == id }

fun todaysDay(): WorkoutDay? = when (LocalDate.now().dayOfWeek) {
	DayOfWeek.MONDAY -> dayById("monday")
	DayOfWeek.TUESDAY -> dayById("tuesday")
	DayOfWeek.THURSDAY -> dayById("thursday")
	DayOfWeek.FRIDAY -> dayById("friday")
	else -> null
}
