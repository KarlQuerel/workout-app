package io.github.karlquerel.workout.timer

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.karlquerel.workout.MainActivity
import io.github.karlquerel.workout.R
import io.github.karlquerel.workout.WorkoutApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RestState(
	val label: String,
	val endAt: Long,
	val totalSeconds: Int,
)

// One rest timer at a time: countdown state for the UI plus an exact alarm so
// the "rest over" notification fires even when the screen is locked.
object RestTimer {
	private const val REQUEST_CODE = 1001
	const val NOTIFICATION_ID = 1

	private val _state = MutableStateFlow<RestState?>(null)
	val state: StateFlow<RestState?> = _state

	fun start(context: Context, seconds: Int, label: String) {
		val endAt = System.currentTimeMillis() + seconds * 1000L
		_state.value = RestState(label = label, endAt = endAt, totalSeconds = seconds)

		val alarmManager = context.getSystemService(AlarmManager::class.java)
		val pending = pendingIntent(context, label)
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
			alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pending)
		} else {
			alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pending)
		}

		postCountdownNotification(context, label, endAt)
	}

	fun cancel(context: Context) {
		val label = _state.value?.label ?: return
		_state.value = null
		context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, label))
		context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
	}

	// Live countdown on the lock screen and status bar while resting; the
	// alarm notification replaces it (same id) when the timer fires.
	private fun postCountdownNotification(context: Context, label: String, endAt: Long) {
		val tapIntent = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)
		val notification = NotificationCompat.Builder(context, WorkoutApp.REST_PROGRESS_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(context.getString(R.string.resting_title))
			.setContentText(label)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setShowWhen(true)
			.setWhen(endAt)
			.setUsesChronometer(true)
			.setChronometerCountDown(true)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setCategory(NotificationCompat.CATEGORY_STOPWATCH)
			.setContentIntent(tapIntent)
			.build()
		context.getSystemService(NotificationManager::class.java)
			.notify(NOTIFICATION_ID, notification)
	}

	fun clearFinished() {
		val current = _state.value ?: return
		if (current.endAt <= System.currentTimeMillis()) _state.value = null
	}

	private fun pendingIntent(context: Context, label: String): PendingIntent {
		val intent = Intent(context, RestAlarmReceiver::class.java)
			.putExtra(RestAlarmReceiver.EXTRA_LABEL, label)
		return PendingIntent.getBroadcast(
			context,
			REQUEST_CODE,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)
	}
}
