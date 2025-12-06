package dev.mmartos.advent.di

import dev.mmartos.advent.screen.main.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { HomeViewModel() }
}