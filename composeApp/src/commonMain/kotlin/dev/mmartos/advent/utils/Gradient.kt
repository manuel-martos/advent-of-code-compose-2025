package dev.mmartos.advent.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object Gradient {

    private val Start = Color(0xFF38BDF8)
    private val End = Color(0xFFFBBF24)

    /**
     * @param t value in range [0f, 1f]
     */
    fun colorAt(t: Float): Color {
        return lerp(Start, End, t.coerceIn(0f, 1f))
    }
}