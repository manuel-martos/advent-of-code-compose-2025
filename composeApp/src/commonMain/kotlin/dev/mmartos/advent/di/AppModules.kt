package dev.mmartos.advent.di

import org.koin.dsl.module

val appModules = module {
    includes(
        listOf(
            viewModelModules,
        )
    )
}