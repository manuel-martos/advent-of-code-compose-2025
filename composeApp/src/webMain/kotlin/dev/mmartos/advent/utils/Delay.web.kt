package dev.mmartos.advent.utils

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

actual object Delay {
    actual suspend fun tinyDelay(reason: DelayReason) {
        if (reason == DelayReason.Solver) {
            delay(1L)
        }
    }

    actual suspend fun shortDelay(reason: DelayReason) {
        if (reason == DelayReason.Solver) {
            delay(2L)
        }
    }

    actual suspend fun regularDelay(reason: DelayReason) {
        if (reason == DelayReason.Solver) {
            delay(3L)
        }
    }

    actual suspend fun longDelay(reason: DelayReason) {
        if (reason == DelayReason.Solver) {
            delay(4L)
        }
    }

    actual suspend fun extraLongDelay(reason: DelayReason) {
        if (reason == DelayReason.Solver) {
            delay(5L)
        }
    }

    actual suspend fun customDelay(reason: DelayReason, timeInMs: Long) {
        if (reason == DelayReason.Solver) {
            delay(timeInMs / 10)
        }
    }

    private suspend fun delay(ms: Long): Unit = suspendCancellableCoroutine { continuation ->
        setTimeout({
            continuation.resume(Unit)
        }, ms)
    }
}

external fun setTimeout(function: () -> Unit, delay: Long)