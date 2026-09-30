package com.balius.galius.core.di

import com.balius.galius.feature.home.presentation.HomeReducer
import com.balius.galius.feature.home.presentation.HomeViewModel
import com.balius.galius.feature.more.presentation.MoreReducer
import com.balius.galius.feature.more.presentation.MoreViewModel
import com.balius.galius.feature.search.presentation.SearchReducer
import com.balius.galius.feature.search.presentation.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::HomeReducer)
    factoryOf(::SearchReducer)
    factoryOf(::MoreReducer)

    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::MoreViewModel)
}

val appModules = listOf(appModule)
