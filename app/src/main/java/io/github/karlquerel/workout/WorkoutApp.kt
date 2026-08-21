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
		getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
	}

	companion object {
		const val REST_CHANNEL_ID = "rest_timer"
	}
}
