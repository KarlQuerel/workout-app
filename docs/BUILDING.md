# Building, releasing, migrating

## Requirements

- JDK 17+ (`java -version`)
- Android SDK at `~/Android/Sdk` (path in `local.properties`, not committed).
  Installed via cmdline-tools: `platform-tools`, `platforms;android-36`, `build-tools;36.0.0`.

## Structure

```
app/src/main/java/io/github/karlquerel/workout/
  data/Program.kt        # the weekly split - edit exercises/rest times here
  data/db/               # Room: sessions + set logs, schemas, migrations
  timer/                 # rest timer state + exact-alarm notification
  ui/                    # Compose screens: Home, Session, History
  ui/theme/              # void palette + monospace typography
```

## Build and install

```bash
./gradlew assembleDebug   # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug    # build + install over USB
```

One-time phone setup: Settings > About phone > tap "Build number" 7x to enable Developer
options, then enable USB debugging. Plug in over USB and accept the fingerprint prompt.
Re-run `installDebug` to update; data survives reinstalls.

## Release build

Release builds are signed with an upload key that never enters the repo. Create one once:

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias upload
```

Then write `keystore.properties` at the repo root (gitignored):

```properties
storeFile=upload-keystore.jks
storePassword=your-store-password
keyAlias=upload
keyPassword=your-key-password
```

CI can supply `WORKOUT_STORE_FILE`, `WORKOUT_STORE_PASSWORD`, `WORKOUT_KEY_ALIAS` and
`WORKOUT_KEY_PASSWORD` instead. With neither present the release variant still builds,
just unsigned.

```bash
./gradlew bundleRelease     # AAB for Play, at app/build/outputs/bundle/release/app-release.aab
./gradlew assembleRelease   # APK, for sideloading outside the store
```

Back up `upload-keystore.jks` somewhere off this machine. Losing it means never being able
to update the app under this package name again.

Store listing copy, data-safety answers and the restricted-permission declaration are in
[play-store-listing.md](play-store-listing.md).

## Database migrations

Room schemas are exported to `app/schemas/` and **committed**. Published builds carry real
training history, so a schema change has to migrate it, never drop it: there is no
destructive fallback, and a version bump without a matching migration crashes the app on
launch for anyone holding older data.

1. Edit the entities in `data/db/WorkoutDatabase.kt`.
2. Bump `version` in the `@Database` annotation.
3. Append the migration to `MIGRATIONS` in `data/db/Migrations.kt`:

```kotlin
val MIGRATIONS: Array<Migration> = arrayOf(
	object : Migration(1, 2) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE set_logs ADD COLUMN notes TEXT")
		}
	},
)
```

4. Build once, then commit the new `app/schemas/<db>/<version>.json` alongside the code.

Adding a nullable column or a new table is a one-line `execSQL`. Renaming or dropping a
column means creating the new table, copying the rows across, dropping the old one, and
renaming, all inside one `migrate`.

## Notes

- Rest alarms use exact alarms (`USE_EXACT_ALARM`) + an alarm-sound notification channel,
  so they fire even when the screen is locked.
- The session screen keeps the display awake; ending a session with no logged sets
  discards it.
- Editing the program = editing `data/Program.kt` and reinstalling.
