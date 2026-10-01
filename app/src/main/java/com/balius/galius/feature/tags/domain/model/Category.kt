package com.balius.galius.feature.tags.domain.model

data class Category(
    val id: String,
    val name: String,
    val colorKey: TagColorKey,
    val createdAtMillis: Long,
)

data class Tag(
    val id: String,
    val categoryId: String,
    val name: String,
    val colorKey: TagColorKey,
    val createdAtMillis: Long,
)

data class CategoryWithTags(
    val category: Category,
    val tags: List<Tag>,
)
