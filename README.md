# Workout

Native Android gym tracker: weekly split, set-by-set logging, rest timers with
lock-screen alarms, per-exercise history and progress charts. Fully offline, no account,
no analytics, and no `INTERNET` permission, so training data cannot leave the phone.

## Stack

Kotlin, Jetpack Compose (Material 3), Room, Navigation Compose.
minSdk 26, targetSdk 36, AGP 8.13.2 / Gradle 8.13, JDK 17+.

Build, release and migration instructions: [docs/BUILDING.md](docs/BUILDING.md).

## Legal

- **Privacy policy**: [docs/privacy-policy.html](docs/privacy-policy.html). The app
  collects, transmits and shares nothing; everything you log stays on the device.
- **VT323 font** (`app/src/main/res/font/vt323.ttf`): Copyright 2011, The VT323 Project
  Authors (peter.hull@oikoi.com), licensed under the SIL Open Font License 1.1. Full text
  in [licenses/VT323-OFL.txt](licenses/VT323-OFL.txt).
- **This project ships no license file**, so all rights are reserved by default.
