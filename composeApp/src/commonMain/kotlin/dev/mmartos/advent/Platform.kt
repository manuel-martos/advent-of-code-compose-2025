package dev.mmartos.advent

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform