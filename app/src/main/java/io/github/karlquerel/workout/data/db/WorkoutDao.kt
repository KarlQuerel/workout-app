package io.github.karlquerel.workout.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
	@Insert
	suspend fun insertSession(session: SessionEntity): Long

	@Query("UPDATE sessions SET endedAt = :endedAt WHERE id = :sessionId")
	suspend fun endSession(sessionId: Long, endedAt: Long)

	@Insert
	suspend fun insertSet(set: SetLogEntity): Long

	@Query("DELETE FROM set_logs WHERE id = :setId")
	suspend fun deleteSet(setId: Long)

	// Sets from the most recent past session in which this exercise was logged.
	@Query(
		"""SELECT * FROM set_logs
		WHERE exerciseName = :name
		AND sessionId = (
			SELECT MAX(sessionId) FROM set_logs
			WHERE exerciseName = :name AND sessionId != :excludeSessionId
		)
		ORDER BY id"""
	)
	suspend fun lastSessionSets(name: String, excludeSessionId: Long): List<SetLogEntity>

	@Query("SELECT * FROM sessions ORDER BY startedAt DESC")
	fun sessions(): Flow<List<SessionEntity>>

	@Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY id")
	suspend fun setsForSession(sessionId: Long): List<SetLogEntity>

	@Query("DELETE FROM sessions WHERE id = :sessionId")
	suspend fun deleteSession(sessionId: Long)

	@Query("DELETE FROM set_logs WHERE sessionId = :sessionId")
	suspend fun deleteSetsForSession(sessionId: Long)
}
