package dev.mmartos.advent

sealed interface Platform {
    object Desktop : Platform
    object Web : Platform
}

expect fun getPlatform(): Platform
