package com.balius.galius.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MediaEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        MediaTagEntity::class,
        MediaCategoryEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class GaliusDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun taxonomyDao(): TaxonomyDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE media_items ADD COLUMN originalRelativePath TEXT",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id TEXT NOT NULL PRIMARY KEY,
                        categoryId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        colorKey TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_tags_categoryId ON tags(categoryId)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS media_tags (
                        mediaId TEXT NOT NULL,
                        tagId TEXT NOT NULL,
                        PRIMARY KEY(mediaId, tagId),
                        FOREIGN KEY(mediaId) REFERENCES media_items(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_tags_mediaId ON media_tags(mediaId)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_tags_tagId ON media_tags(tagId)",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE categories ADD COLUMN colorKey TEXT NOT NULL DEFAULT 'indigo'",
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS media_categories (
                        mediaId TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        PRIMARY KEY(mediaId, categoryId),
                        FOREIGN KEY(mediaId) REFERENCES media_items(id) ON DELETE CASCADE,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_categories_mediaId ON media_categories(mediaId)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_media_categories_categoryId ON media_categories(categoryId)",
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO media_categories (mediaId, categoryId)
                    SELECT DISTINCT mt.mediaId, t.categoryId
                    FROM media_tags mt
                    INNER JOIN tags t ON t.id = mt.tagId
                    """.trimIndent(),
                )
            }
        }
    }
}
