package dev.mmartos.advent.di

import dev.mmartos.advent.screen.day01.Day01ViewModel
import dev.mmartos.advent.screen.day02.Day02ViewModel
import dev.mmartos.advent.screen.day03.Day03ViewModel
import dev.mmartos.advent.screen.day04.Day04ViewModel
import dev.mmartos.advent.screen.day05.Day05ViewModel
import dev.mmartos.advent.screen.day06.Day06ViewModel
import dev.mmartos.advent.screen.day07.Day07ViewModel
import dev.mmartos.advent.screen.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { HomeViewModel() }
    viewModel { Day01ViewModel() }
    viewModel { Day02ViewModel() }
    viewModel { Day03ViewModel() }
    viewModel { Day04ViewModel() }
    viewModel { Day05ViewModel() }
    viewModel { Day06ViewModel() }
    viewModel { Day07ViewModel() }
}