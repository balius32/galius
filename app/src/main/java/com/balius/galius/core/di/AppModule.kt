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
import com.balius.galius.feature.media.domain.usecase.ObserveBrowseMediaUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByCategoryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByTagUseCase
import com.balius.galius.feature.media.domain.usecase.RestoreAndRemoveMediaUseCase
import com.balius.galius.feature.media.presentation.ImportSessionViewModel
import com.balius.galius.feature.media.presentation.viewer.MediaViewerReducer
import com.balius.galius.feature.media.presentation.viewer.MediaViewerViewModel
import com.balius.galius.feature.more.presentation.MoreReducer
import com.balius.galius.feature.more.presentation.MoreViewModel
import com.balius.galius.feature.search.presentation.SearchReducer
import com.balius.galius.feature.search.presentation.SearchViewModel
import com.balius.galius.feature.tags.data.repository.TaxonomyRepositoryImpl
import com.balius.galius.feature.tags.domain.repository.TaxonomyRepository
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
            )
            .build()
    }
    single { get<GaliusDatabase>().mediaDao() }
    single { get<GaliusDatabase>().taxonomyDao() }
    single { VaultFileStore(androidContext()) }
    single { MediaStoreDeleteUriResolver(androidContext()) }
    single { MediaStoreRestorer(androidContext()) }
    single {
        MediaRepositoryImpl(androidContext(), get(), get(), get(), get())
    } bind MediaRepository::class
    single { TaxonomyRepositoryImpl(get()) } bind TaxonomyRepository::class

    factoryOf(::ImportMediaUseCase)
    factoryOf(::ObserveLibraryUseCase)
    factoryOf(::ObserveMediaByCategoryUseCase)
    factoryOf(::ObserveMediaByTagUseCase)
    factoryOf(::ObserveBrowseMediaUseCase)
    factoryOf(::RestoreAndRemoveMediaUseCase)
    factoryOf(::ObserveCategoriesUseCase)
    factoryOf(::CreateCategoryUseCase)
    factoryOf(::CreateTagUseCase)
    factoryOf(::DeleteTagUseCase)
    factoryOf(::ObserveMediaTagsUseCase)
    factoryOf(::SetMediaTagUseCase)

    factoryOf(::HomeReducer)
    factoryOf(::SearchReducer)
    factoryOf(::MoreReducer)
    factoryOf(::ManageTagsReducer)
    factoryOf(::MediaViewerReducer)

    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MoreViewModel)
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
        )
    }
}

val appModules = listOf(appModule)
