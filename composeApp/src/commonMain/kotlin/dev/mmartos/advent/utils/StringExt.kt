package dev.mmartos.advent.utils

fun Number.leadingZeros(zeros: Int): String =
    toString().padStart(zeros, '0')

fun Number.leadingSpaces(spaces: Int): String =
    toString().padStart(spaces, ' ')
