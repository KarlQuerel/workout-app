import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.ksp)
}

// Upload-key material: keystore.properties (gitignored) locally, env vars in CI.
val keystoreProperties = Properties().apply {
	val file = rootProject.file("keystore.properties")
	if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String, env: String): String? =
	keystoreProperties.getProperty(key) ?: System.getenv(env)

android {
	namespace = "io.github.karlquerel.workout"
	compileSdk = 36

	defaultConfig {
		applicationId = "io.github.karlquerel.workout"
		minSdk = 26
		targetSdk = 36
		versionCode = 3
		versionName = "1.3"
	}

	signingConfigs {
		create("release") {
			val store = signingValue("storeFile", "WORKOUT_STORE_FILE")
			if (store != null) {
				storeFile = rootProject.file(store)
				storePassword = signingValue("storePassword", "WORKOUT_STORE_PASSWORD")
				keyAlias = signingValue("keyAlias", "WORKOUT_KEY_ALIAS")
				keyPassword = signingValue("keyPassword", "WORKOUT_KEY_PASSWORD")
			}
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			// Left unsigned when no keystore is configured, so a bare clone still builds.
			signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}

	buildFeatures {
		compose = true
	}
}

// Room schemas are committed so every version bump has a diffable, migratable reference.
ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.compose.ui)
	implementation(libs.compose.material3)
	implementation(libs.compose.ui.tooling.preview)
	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	ksp(libs.room.compiler)
	debugImplementation(libs.compose.ui.tooling)
}
