package dev.mmartos.advent.utils

sealed interface DelayReason {
    object Parser : DelayReason
    object Solver : DelayReason
}

expect object Delay {
    suspend fun tinyDelay(reason: DelayReason)
    suspend fun shortDelay(reason: DelayReason)
    suspend fun regularDelay(reason: DelayReason)
    suspend fun longDelay(reason: DelayReason)
    suspend fun extraLongDelay(reason: DelayReason)
    suspend fun customDelay(reason: DelayReason, timeInMs: Long)
}