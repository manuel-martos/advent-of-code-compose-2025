package dev.mmartos.advent.utils

import kotlinx.coroutines.delay

actual object Delay {
    actual suspend fun tinyDelay(reason: DelayReason) {
        delay(1)
    }

    actual suspend fun shortDelay(reason: DelayReason) {
        delay(2)
    }

    actual suspend fun regularDelay(reason: DelayReason) {
        delay(5)
    }

    actual suspend fun longDelay(reason: DelayReason) {
        delay(10)
    }

    actual suspend fun extraLongDelay(reason: DelayReason) {
        delay(20)
    }

    actual suspend fun customDelay(reason: DelayReason, timeInMs: Long) {
        delay(timeInMs)
    }

}