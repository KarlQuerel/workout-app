# Play Store listing and release checklist

## Store listing

- **App name**: `Workout`
- **Category**: Health & Fitness
- **Tags**: fitness, workout tracker, strength training
- **Contact email**: karl.querel@gmail.com
- **Privacy policy URL**: `https://karlquerel.com/workout-app/privacy-policy.html`
(enable GitHub Pages first: repo Settings > Pages > branch `master`, folder `/docs`. The URL sits under `karlquerel.com`, not `karlquerel.github.io`, because the user site carries that CNAME and project pages inherit it. Load the URL once before submitting.)

### Short description (80 char max)

```
Offline gym tracker: weekly split, set logging, rest timers with alarms.
```

### Full description (4000 char max)

```
Workout is a no-nonsense gym tracker that works entirely offline.

Open the app, pick today's session from your weekly split, and log every set as you go: weight, repetitions, and how hard it felt. Between sets, the rest timer runs a live countdown in your notifications and fires an alarm when it is time to get back under the bar, even with the screen locked and the phone in your pocket.

FEATURES

- Weekly split: your training days, laid out as you actually train them.
- Set-by-set logging: weight, reps and effort, one tap at a time.
- Rest timers: per-exercise rest periods with a lock-screen countdown and alarm.
- History: every past session, browsable per exercise.
- Progress charts: see your working weights move over time.
- Overload hints: a nudge when it is time to add weight.
- CSV export: your data is yours, take it with you.

FULLY OFFLINE, NO ACCOUNT

There is no sign-up, no cloud, no ads, and no analytics. The app does not even ask for internet access, so it physically cannot send your training data anywhere. Everything lives in a database on your phone, and uninstalling removes it.

BUILT FOR THE GYM FLOOR

High-contrast dark interface with oversized numbers, readable at arm's length under bad gym lighting, and quick enough to use between sets without breaking your rhythm.

Open source: github.com/KarlQuerel/workout-app
```

## Data safety form

- Does your app collect or share any of the required user data types? **No**
- Is all of the user data collected by your app encrypted in transit? N/A (no data leaves the device)
- Do you provide a way for users to request that their data is deleted? N/A (uninstalling deletes everything)

Rationale if challenged: the app declares no `INTERNET` permission, so no data can be transmitted. CSV export is a user-initiated file write, not collection.

## Content rating questionnaire

Category: Utility, Productivity, Communication or Other. Answer **No** to every content question (no violence, no sexual content, no profanity, no gambling, no user-generated content, no sharing of location or personal info). Expected outcome: rated for everyone.

## Restricted permission declaration: exact alarms

The app declares `USE_EXACT_ALARM`. Play restricts this to apps whose core function needs precise timing (alarm, timer and calendar apps). Declaration text to submit:

```
The app is a strength-training tracker whose core in-session feature is a rest timer between sets. The user sets a rest duration, and the alarm must fire at exactly that moment: firing late makes the feature useless, as the rest period is the trained variable. The alarm fires with the screen locked and the app in the background, which is the normal state while the user is lifting. No other feature uses alarms.
```

If Play rejects it, the fallback is `SCHEDULE_EXACT_ALARM` with a user-granted toggle, which needs no declaration but adds a settings prompt on first run.

## Health apps declaration

Not applicable: the app does not integrate Health Connect and reads no health data from the device. It only stores what the user types in.

## Graphics

| Asset | Status | Where |
|---|---|---|
| App icon 512x512 PNG | Done | `docs/store/play-icon-512.png` |
| Feature graphic 1024x500 PNG | Done | `docs/store/feature-graphic-1024x500.png` |
| Phone screenshots (2 to 8, min 320px side) | **You must take these** | on your phone |

Regenerate the first two with `python3 tools/store_assets.py` (needs Pillow).

Screenshots have to come off a real device or emulator. Suggested set of four: home screen with the weekly split, a session mid-logging, the rest countdown, the history or progress chart. Capture with the phone plugged in:

```bash
adb exec-out screencap -p > screenshot-1.png
```

## Release checklist

1. `keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload`
2. Write `keystore.properties` at the repo root (gitignored, see README).
3. `./gradlew bundleRelease` and upload `app/build/outputs/bundle/release/app-release.aab`.
4. **Back up the keystore off this machine.** Losing it means losing the ability to
update the app under this package name.
5. Bump `versionCode` in `app/build.gradle.kts` for every upload.
6. New personal developer accounts: 12 testers opted in to a closed test for 14
continuous days before production access can be requested.
