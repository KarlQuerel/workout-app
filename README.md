# Workout — gym tracker

Native Android app (Kotlin + Jetpack Compose) for tracking gym sessions:
the weekly split from [karlquerel.github.io/sport](https://karlquerel.github.io/sport),
set-by-set weight/rep logging, rest timers with lock-screen alarms, and
per-exercise history — all stored on-device (Room), fully offline.

## Structure

```
app/src/main/java/io/github/karlquerel/workout/
  data/Program.kt        # the weekly split — edit exercises/rest times here
  data/db/               # Room: sessions + set logs
  timer/                 # rest timer state + exact-alarm notification
  ui/                    # Compose screens: Home, Session, History
  ui/theme/              # void palette + monospace typography
```

## Requirements

- JDK 17+ (`java -version`)
- Android SDK at `~/Android/Sdk` (path set in `local.properties`, not committed).
  Installed via cmdline-tools: `platform-tools`, `platforms;android-35`, `build-tools;35.0.0`.

## Build

```bash
./gradlew assembleDebug        # APK at app/build/outputs/apk/debug/app-debug.apk
```

## Install on the phone

One-time phone setup: Settings → About phone → tap "Build number" 7× to enable
Developer options, then enable **USB debugging**. Plug in over USB and accept
the fingerprint prompt.

```bash
./gradlew installDebug         # build + install over USB
# or: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Re-run the same command to update the app after changes — data survives reinstalls.

## Notes

- Rest alarms use exact alarms (`USE_EXACT_ALARM`) + an alarm-sound notification
  channel, so they fire even when the screen is locked.
- The session screen keeps the display awake; ending a session with no logged
  sets discards it.
- Editing the program = editing `data/Program.kt` and reinstalling.
