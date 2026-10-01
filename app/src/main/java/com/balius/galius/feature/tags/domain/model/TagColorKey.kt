package com.balius.galius.feature.tags.domain.model

enum class TagColorKey {
    Indigo,
    Mint,
    Amber,
    Lavender,
    Sky,
    Rose,
    Neutral,
    ;

    companion object {
        /** Accents shown in Stitch create-category swatches. */
        val categoryAccents: List<TagColorKey> = listOf(
            Indigo,
            Sky,
            Rose,
            Mint,
            Amber,
        )

        fun fromStorage(value: String): TagColorKey =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Neutral

        fun nextForIndex(index: Int): TagColorKey {
            if (categoryAccents.isEmpty()) return Neutral
            return categoryAccents[index.mod(categoryAccents.size)]
        }
    }
}
