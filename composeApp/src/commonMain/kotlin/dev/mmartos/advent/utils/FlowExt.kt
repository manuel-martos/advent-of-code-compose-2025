package dev.mmartos.advent.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <T> MutableStateFlow<T>.threadSafeUpdate(function: (T) -> T) {
    synchronized(this) {
        update(function)
    }
}
