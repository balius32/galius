package com.balius.galius.core.di

import androidx.room.Room
import com.balius.galius.core.database.GaliusDatabase
import com.balius.galius.feature.home.presentation.HomeReducer
import com.balius.galius.feature.home.presentation.HomeViewModel
import com.balius.galius.feature.media.data.local.MediaStoreDeleteUriResolver
import com.balius.galius.feature.media.data.local.MediaStoreRestorer
import com.balius.galius.feature.media.data.local.VaultFileStore
import com.balius.galius.feature.media.data.repository.MediaRepositoryImpl
import com.balius.galius.feature.media.domain.repository.MediaRepository
import com.balius.galius.feature.media.domain.usecase.ImportMediaUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.RestoreAndRemoveMediaUseCase
import com.balius.galius.feature.media.presentation.ImportSessionViewModel
import com.balius.galius.feature.more.presentation.MoreReducer
import com.balius.galius.feature.more.presentation.MoreViewModel
import com.balius.galius.feature.search.presentation.SearchReducer
import com.balius.galius.feature.search.presentation.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GaliusDatabase::class.java,
            "galius.db",
        )
            .addMigrations(GaliusDatabase.MIGRATION_1_2)
            .build()
    }
    single { get<GaliusDatabase>().mediaDao() }
    single { VaultFileStore(androidContext()) }
    single { MediaStoreDeleteUriResolver(androidContext()) }
    single { MediaStoreRestorer(androidContext()) }
    single {
        MediaRepositoryImpl(androidContext(), get(), get(), get(), get())
    } bind MediaRepository::class

    factoryOf(::ImportMediaUseCase)
    factoryOf(::ObserveLibraryUseCase)
    factoryOf(::RestoreAndRemoveMediaUseCase)

    factoryOf(::HomeReducer)
    factoryOf(::SearchReducer)
    factoryOf(::MoreReducer)

    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MoreViewModel)
    viewModelOf(::ImportSessionViewModel)
}

val appModules = listOf(appModule)
