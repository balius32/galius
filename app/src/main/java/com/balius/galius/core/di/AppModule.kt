package com.balius.galius.core.di

import androidx.room.Room
import com.balius.galius.core.database.GaliusDatabase
import com.balius.galius.feature.home.presentation.HomeReducer
import com.balius.galius.feature.home.presentation.HomeViewModel
import com.balius.galius.feature.media.data.local.MediaShareUriFactory
import com.balius.galius.feature.media.data.local.MediaStoreDeleteUriResolver
import com.balius.galius.feature.media.data.local.MediaStoreRestorer
import com.balius.galius.feature.media.data.local.VaultFileStore
import com.balius.galius.feature.media.domain.repository.MediaShareUriProvider
import com.balius.galius.feature.media.data.repository.MediaRepositoryImpl
import com.balius.galius.feature.media.domain.repository.MediaRepository
import com.balius.galius.feature.media.domain.usecase.ImportMediaUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveBrowseMediaUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByCategoryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByAllTagsUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByTagUseCase
import com.balius.galius.feature.media.domain.usecase.PrepareMediaShareUseCase
import com.balius.galius.feature.media.domain.usecase.RestoreAndRemoveMediaUseCase
import com.balius.galius.feature.media.presentation.ImportSessionViewModel
import com.balius.galius.feature.media.presentation.player.VideoPlayerReducer
import com.balius.galius.feature.media.presentation.player.VideoPlayerViewModel
import com.balius.galius.feature.media.presentation.viewer.MediaViewerReducer
import com.balius.galius.feature.media.presentation.viewer.MediaViewerViewModel
import com.balius.galius.feature.search.presentation.SearchReducer
import com.balius.galius.feature.search.presentation.SearchViewModel
import com.balius.galius.feature.settings.data.AndroidDatabaseSizeProvider
import com.balius.galius.feature.settings.data.BiometricAppLockAuthenticator
import com.balius.galius.feature.settings.data.SettingsRepositoryImpl
import com.balius.galius.feature.settings.domain.AppLockAuthenticator
import com.balius.galius.feature.settings.domain.repository.DatabaseSizeProvider
import com.balius.galius.feature.settings.domain.repository.SettingsRepository
import com.balius.galius.feature.settings.domain.usecase.ObserveLibraryStorageUseCase
import com.balius.galius.feature.settings.domain.usecase.ObserveSettingsPreferencesUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAccentUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.balius.galius.feature.settings.domain.usecase.SetThemeModeUseCase
import com.balius.galius.feature.settings.presentation.SettingsReducer
import com.balius.galius.feature.settings.presentation.SettingsViewModel
import com.balius.galius.feature.tags.data.repository.TaxonomyRepositoryImpl
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository
import com.balius.galius.feature.tags.domain.usecase.AddMediaToCategoryUseCase
import com.balius.galius.feature.tags.domain.usecase.CreateCategoryUseCase
import com.balius.galius.feature.tags.domain.usecase.CreateTagUseCase
import com.balius.galius.feature.tags.domain.usecase.DeleteTagUseCase
import com.balius.galius.feature.tags.domain.usecase.ObserveCategoriesUseCase
import com.balius.galius.feature.tags.domain.usecase.ObserveMediaTagsUseCase
import com.balius.galius.feature.tags.domain.usecase.SetMediaTagUseCase
import com.balius.galius.feature.tags.presentation.ManageTagsReducer
import com.balius.galius.feature.tags.presentation.ManageTagsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
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
            .addMigrations(
                GaliusDatabase.MIGRATION_1_2,
                GaliusDatabase.MIGRATION_2_3,
                GaliusDatabase.MIGRATION_3_4,
                GaliusDatabase.MIGRATION_4_5,
            )
            .build()
    }
    single { get<GaliusDatabase>().mediaDao() }
    single { get<GaliusDatabase>().taxonomyDao() }
    single { VaultFileStore(androidContext()) }
    single { MediaShareUriFactory(androidContext(), get()) } bind MediaShareUriProvider::class
    single { MediaStoreDeleteUriResolver(androidContext()) }
    single { MediaStoreRestorer(androidContext()) }
    single {
        MediaRepositoryImpl(androidContext(), get(), get(), get(), get())
    } bind MediaRepository::class
    single { TaxonomyRepositoryImpl(get()) } bind TaxonomyRepository::class
    single { SettingsRepositoryImpl(androidContext()) } bind SettingsRepository::class
    single { AndroidDatabaseSizeProvider(androidContext()) } bind DatabaseSizeProvider::class
    single { BiometricAppLockAuthenticator(androidContext()) } bind AppLockAuthenticator::class

    factoryOf(::ImportMediaUseCase)
    factoryOf(::ObserveLibraryUseCase)
    factoryOf(::ObserveMediaByCategoryUseCase)
    factoryOf(::ObserveMediaByTagUseCase)
    factoryOf(::ObserveMediaByAllTagsUseCase)
    factoryOf(::ObserveBrowseMediaUseCase)
    factoryOf(::RestoreAndRemoveMediaUseCase)
    factoryOf(::PrepareMediaShareUseCase)
    factoryOf(::ObserveCategoriesUseCase)
    factoryOf(::CreateCategoryUseCase)
    factoryOf(::CreateTagUseCase)
    factoryOf(::DeleteTagUseCase)
    factoryOf(::ObserveMediaTagsUseCase)
    factoryOf(::SetMediaTagUseCase)
    factoryOf(::AddMediaToCategoryUseCase)
    factoryOf(::ObserveSettingsPreferencesUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::SetAccentUseCase)
    factoryOf(::SetAppLockEnabledUseCase)
    factoryOf(::ObserveLibraryStorageUseCase)

    factoryOf(::HomeReducer)
    factoryOf(::SearchReducer)
    factoryOf(::SettingsReducer)
    factoryOf(::ManageTagsReducer)
    factoryOf(::MediaViewerReducer)
    factoryOf(::VideoPlayerReducer)

    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ImportSessionViewModel)
    viewModelOf(::ManageTagsViewModel)
    viewModel { params ->
        MediaViewerViewModel(
            startMediaId = params.get(),
            source = params.get(),
            reducer = get(),
            observeBrowseMediaUseCase = get(),
            observeMediaTagsUseCase = get(),
            observeCategoriesUseCase = get(),
            setMediaTagUseCase = get(),
            prepareMediaShareUseCase = get(),
        )
    }
    viewModel { params ->
        VideoPlayerViewModel(
            startMediaId = params.get(),
            source = params.get(),
            reducer = get(),
            observeBrowseMediaUseCase = get(),
        )
    }
}

val appModules = listOf(appModule)
