package com.balius.galius.feature.settings.domain.repository

fun interface DatabaseSizeProvider {
    fun databaseBytes(): Long
}
