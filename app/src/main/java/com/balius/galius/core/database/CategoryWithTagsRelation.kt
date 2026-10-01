package com.balius.galius.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithTagsRelation(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId",
    )
    val tags: List<TagEntity>,
)
