package dev.mmartos.advent.di

import dev.mmartos.advent.screen.day01.Day01ViewModel
import dev.mmartos.advent.screen.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { HomeViewModel() }
    viewModel { Day01ViewModel() }
}