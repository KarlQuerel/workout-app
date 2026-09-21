package io.github.karlquerel.workout.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "sessions")
data class SessionEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val dayId: String,
	val startedAt: Long,
	val endedAt: Long? = null,
)

@Entity(tableName = "set_logs")
data class SetLogEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val sessionId: Long,
	val exerciseName: String,
	val weightKg: Double,
	val reps: Int,
	val loggedAt: Long,
)

@Database(entities = [SessionEntity::class, SetLogEntity::class], version = 1, exportSchema = true)
abstract class WorkoutDatabase : RoomDatabase() {
	abstract fun dao(): WorkoutDao

	companion object {
		@Volatile
		private var instance: WorkoutDatabase? = null

		fun get(context: Context): WorkoutDatabase =
			instance ?: synchronized(this) {
				instance ?: Room.databaseBuilder(
					context.applicationContext,
					WorkoutDatabase::class.java,
					"workout.db",
				).addMigrations(*MIGRATIONS).build().also { instance = it }
			}
	}
}
