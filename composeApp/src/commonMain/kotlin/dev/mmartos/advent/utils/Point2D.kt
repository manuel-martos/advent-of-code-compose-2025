package dev.mmartos.advent.utils

data class Point2D(
    val x: Long,
    val y: Long,
) : Comparable<Point2D> {
    // Implementing Comparable allows automatic sorting/comparisons
    override fun compareTo(other: Point2D): Int =
        compareValuesBy(this, other, { it.y }, { it.x })
}
