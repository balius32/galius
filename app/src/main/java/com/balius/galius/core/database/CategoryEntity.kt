package com.balius.galius.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Stored as TagColorKey name. */
    val colorKey: String,
    val createdAtMillis: Long,
)
