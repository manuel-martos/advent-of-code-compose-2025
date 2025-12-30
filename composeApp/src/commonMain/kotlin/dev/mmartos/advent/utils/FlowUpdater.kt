package dev.mmartos.advent.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FlowUpdater<T>(initial: T) {
    private val mutex = Mutex()
    val state = MutableStateFlow(initial)

    suspend fun update(transform: (T) -> T) {
        mutex.withLock {
            state.update(transform)
        }
    }
}
