package com.greatnotes.data.local.database

import androidx.room.TypeConverter
import com.greatnotes.domain.model.CoverStyle
import com.greatnotes.domain.model.PageTemplate

/**
 * Enums are persisted by name (not ordinal) so reordering constants never corrupts data.
 * Unknown names (e.g. after a downgrade) fall back to a safe default instead of crashing.
 */
class EnumConverters {
    @TypeConverter
    fun pageTemplateToString(value: PageTemplate): String = value.name

    @TypeConverter
    fun stringToPageTemplate(value: String): PageTemplate =
        PageTemplate.entries.firstOrNull { it.name == value } ?: PageTemplate.BLANK

    @TypeConverter
    fun coverStyleToString(value: CoverStyle): String = value.name

    @TypeConverter
    fun stringToCoverStyle(value: String): CoverStyle =
        CoverStyle.entries.firstOrNull { it.name == value } ?: CoverStyle.PLAIN
}
