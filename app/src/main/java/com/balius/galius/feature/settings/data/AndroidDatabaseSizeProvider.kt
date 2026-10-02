package com.balius.galius.feature.settings.data

import android.content.Context
import com.balius.galius.feature.settings.domain.repository.DatabaseSizeProvider
import java.io.File

class AndroidDatabaseSizeProvider(
    private val context: Context,
) : DatabaseSizeProvider {
    override fun databaseBytes(): Long {
        val dbFile = context.getDatabasePath(DB_NAME)
        return listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm"))
            .sumOf { file -> file.takeIf { it.exists() }?.length() ?: 0L }
    }

    private companion object {
        const val DB_NAME = "galius.db"
    }
}
