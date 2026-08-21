plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.ksp)
}

android {
	namespace = "io.github.karlquerel.workout"
	compileSdk = 35

	defaultConfig {
		applicationId = "io.github.karlquerel.workout"
		minSdk = 26
		targetSdk = 35
		versionCode = 3
		versionName = "1.3"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
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
