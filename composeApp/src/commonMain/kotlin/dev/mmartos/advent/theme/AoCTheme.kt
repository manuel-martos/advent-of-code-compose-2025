package dev.mmartos.advent.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AoCDarkBackground = Color(0xFF464646)

private val AoCDarkColorScheme = darkColorScheme(
    background = AoCDarkBackground,
    surface = AoCDarkBackground,
)


@Composable
fun AoCTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AoCDarkColorScheme,
        content = content
    )
}