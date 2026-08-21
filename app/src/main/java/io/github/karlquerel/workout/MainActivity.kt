package io.github.karlquerel.workout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.karlquerel.workout.ui.HistoryScreen
import io.github.karlquerel.workout.ui.HomeScreen
import io.github.karlquerel.workout.ui.SessionScreen
import io.github.karlquerel.workout.ui.theme.WorkoutTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val permissionLauncher =
			registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
		if (
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
			checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
		) {
			permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
		}

		setContent {
			WorkoutTheme {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.background)
						.safeDrawingPadding()
				) {
					AppNav()
				}
			}
		}
	}
}

@Composable
private fun AppNav() {
	val nav = rememberNavController()
	NavHost(navController = nav, startDestination = "home") {
		composable("home") {
			HomeScreen(
				onStartDay = { dayId -> nav.navigate("session/$dayId") },
				onHistory = { nav.navigate("history") },
			)
		}
		composable("session/{dayId}") { entry ->
			SessionScreen(
				dayId = entry.arguments?.getString("dayId").orEmpty(),
				onExit = { nav.popBackStack() },
			)
		}
		composable("history") {
			HistoryScreen(onBack = { nav.popBackStack() })
		}
	}
}
