package com.greatnotes.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.greatnotes.domain.model.CoverStyle

@Entity(
    tableName = "notebooks",
    indices = [Index(value = ["isArchived", "sortIndex"])],
)
data class NotebookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val coverStyle: CoverStyle,
    val coverColor: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val pageCount: Int,
    val isArchived: Boolean,
    val sortIndex: Int,
)
