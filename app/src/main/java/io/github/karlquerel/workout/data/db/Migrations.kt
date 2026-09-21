package io.github.karlquerel.workout.data.db

import androidx.room.migration.Migration

// Published builds carry real training history, so schema changes migrate it rather
// than dropping it. Every version bump appends its Migration here.
val MIGRATIONS: Array<Migration> = arrayOf()
