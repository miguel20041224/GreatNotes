package com.greatnotes.data.local.database

import androidx.room.migration.Migration

/**
 * Registry of Room migrations. Empty while the schema is at version 1.
 *
 * Add each new Migration here (never use destructive fallback: this DB holds user notes)
 * and commit the exported schema JSON from app/schemas.
 */
val ALL_MIGRATIONS: Array<Migration> = emptyArray()
