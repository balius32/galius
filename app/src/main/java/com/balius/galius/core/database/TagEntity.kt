package com.balius.galius.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class TagEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    /** Stored as TagColorKey name: mint, amber, lavender, sky, rose, neutral. */
    val colorKey: String,
    val createdAtMillis: Long,
)
