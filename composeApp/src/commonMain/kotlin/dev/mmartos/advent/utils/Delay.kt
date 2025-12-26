package dev.mmartos.advent.utils

fun delayNanos(nanos: Long) {
    val start = System.nanoTime()
    while (System.nanoTime() - start < nanos) {
        // spin
    }
}