package io.github.karlquerel.workout.timer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.github.karlquerel.workout.MainActivity
import io.github.karlquerel.workout.R
import io.github.karlquerel.workout.WorkoutApp

class RestAlarmReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val label = intent.getStringExtra(EXTRA_LABEL) ?: context.getString(R.string.app_name)

		val tapIntent = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)

		val notification = NotificationCompat.Builder(context, WorkoutApp.REST_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(context.getString(R.string.rest_over_title))
			.setContentText(label)
			.setCategory(NotificationCompat.CATEGORY_ALARM)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setAutoCancel(true)
			.setContentIntent(tapIntent)
			.build()

		context.getSystemService(NotificationManager::class.java)
			.notify(NOTIFICATION_ID, notification)
	}

	companion object {
		const val EXTRA_LABEL = "label"
		private const val NOTIFICATION_ID = 1
	}
}
