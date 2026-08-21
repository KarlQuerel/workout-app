package io.github.karlquerel.workout

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.provider.Settings
import io.github.karlquerel.workout.data.db.WorkoutDatabase

class WorkoutApp : Application() {
	val database: WorkoutDatabase by lazy { WorkoutDatabase.get(this) }

	override fun onCreate() {
		super.onCreate()

		val channel = NotificationChannel(
			REST_CHANNEL_ID,
			getString(R.string.rest_channel_name),
			NotificationManager.IMPORTANCE_HIGH,
		).apply {
			// Alarm-grade sound + vibration so it cuts through a locked screen.
			setSound(
				Settings.System.DEFAULT_ALARM_ALERT_URI,
				AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_ALARM)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build(),
			)
			enableVibration(true)
			vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
		}
		// Silent channel for the ongoing lock-screen countdown — no beep per set.
		val progressChannel = NotificationChannel(
			REST_PROGRESS_CHANNEL_ID,
			getString(R.string.rest_progress_channel_name),
			NotificationManager.IMPORTANCE_LOW,
		).apply {
			lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
		}

		getSystemService(NotificationManager::class.java).let {
			it.createNotificationChannel(channel)
			it.createNotificationChannel(progressChannel)
		}
	}

	companion object {
		const val REST_CHANNEL_ID = "rest_timer"
		const val REST_PROGRESS_CHANNEL_ID = "rest_progress"
	}
}
