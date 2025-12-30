package dev.mmartos.advent.screen.day09

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.regularDelay
import dev.mmartos.advent.utils.Delay.tinyDelay
import dev.mmartos.advent.utils.DelayReason
import dev.mmartos.advent.utils.Point2D
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

data class Locations(
    val points: PersistentList<Point2D>,
    val min: Point2D,
    val max: Point2D,
    val middle: Point2D,
)

data class RedTilesRect(
    val start: Point2D,
    val end: Point2D,
    val width: Long,
    val height: Long,
    val area: Long,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialLocations: PersistentList<Point2D>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val locations: Locations,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val locations: Locations,
        val currentRect: RedTilesRect,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val locations: Locations,
        val solution: String,
        val largestRect: RedTilesRect,
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val locations: Locations,
        val currentRect: RedTilesRect,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val locations: Locations,
        val solution: String,
        val largestRect: RedTilesRect,
    ) : SolverStage2(), SolvedStage
}

typealias Day09UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day09ViewModel : BaseViewModel<ParserStage, Locations, SolverStage1, SolverStage2>() {

    override val Day09UiState.parsedData: Locations?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.locations

    fun startParser(input: List<String>) = doParsing {
        val locations = mutableListOf<Point2D>()
        runCatching {
            input.forEach { line ->
                locations.add(line.parse())
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialLocations = locations.toPersistentList(),
                        ),
                    )
                }
                regularDelay(DelayReason.Parser)
            }
        }.onFailure {
            uiStateUpdater.update {
                it.copy(
                    parserStage = ParserStage.Error,
                )
            }
        }.onSuccess {
            uiStateUpdater.update {
                val min = locations.reduce { acc, d -> Point2D(min(acc.x, d.x), min(acc.y, d.y)) }
                val max = locations.reduce { acc, d -> Point2D(max(acc.x, d.x), max(acc.y, d.y)) }
                val middle = Point2D(min.x + (max.x - min.x) / 2, min.y + (max.y - min.y) / 2)
                val locations = Locations(
                    points = (locations + locations.first()).toPersistentList(),
                    min = min,
                    max = max,
                    middle = middle,
                )
                it.copy(
                    parserStage = ParserStage.Parsed(
                        locations = locations,
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        var largestRect: RedTilesRect? = null
        uiState.value.parsedData?.run {
            points.flatMap { a ->
                points.map { b ->
                    val width = abs(a.x - b.x) + 1L
                    val height = abs(a.y - b.y) + 1L
                    val area = width * height
                    uiStateUpdater.update {
                        val currentRect = RedTilesRect(
                            start = a,
                            end = b,
                            width = width,
                            height = height,
                            area = area,
                        )
                        if (result < area) {
                            result = area
                            largestRect = currentRect
                        }
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                locations = this,
                                currentRect = currentRect,
                                partialSolution = result.toString(),
                            )
                        )
                    }
                    tinyDelay(DelayReason.Solver)
                }
            }

            uiStateUpdater.update {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        locations = this,
                        solution = result.toString(),
                        largestRect = largestRect!!,
                    )
                )
            }
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        var largestRect: RedTilesRect? = null
        uiState.value.parsedData?.run {
            val lines = (points + points.first()).windowed(2).map { it[0] to it[1] }
            for (a in points) {
                for (b in points - a) {
                    // Inner rectangle bounds
                    val xMin = minOf(a.x, b.x) + 1
                    val xMax = maxOf(a.x, b.x) - 1
                    val yMin = minOf(a.y, b.y) + 1
                    val yMax = maxOf(a.y, b.y) - 1

                    val width = xMax - xMin + 1 + 2L
                    val height = yMax - yMin + 1 + 2L
                    val area = width * height
                    if (area <= result) continue

                    val isCrossing = listOf(
                        Point2D(xMin, yMin) to Point2D(xMax, yMin),
                        Point2D(xMax, yMin) to Point2D(xMax, yMax),
                        Point2D(xMax, yMax) to Point2D(xMin, yMax),
                        Point2D(xMin, yMax) to Point2D(xMin, yMin),
                    ).any { lineA ->
                        val isAHorizontal = lineA.first.y == lineA.second.y
                        lines.any { lineB ->
                            val isBHorizontal = lineB.first.y == lineB.second.y
                            if (isAHorizontal && !isBHorizontal) {
                                // Check for vertical crossing
                                val xMinA = minOf(lineA.first.x, lineA.second.x)
                                val xMaxA = maxOf(lineA.first.x, lineA.second.x)
                                val yMinB = minOf(lineB.first.y, lineB.second.y)
                                val yMaxB = maxOf(lineB.first.y, lineB.second.y)
                                val xB = lineB.first.x
                                val aY = lineA.first.y
                                xB in xMinA..xMaxA && aY in yMinB..yMaxB
                            } else if (!isAHorizontal && isBHorizontal) {
                                // Check for horizontal crossing
                                val yMinA = minOf(lineA.first.y, lineA.second.y)
                                val yMaxA = maxOf(lineA.first.y, lineA.second.y)
                                val xMinB = minOf(lineB.first.x, lineB.second.x)
                                val xMaxB = maxOf(lineB.first.x, lineB.second.x)
                                val yB = lineB.first.y
                                val aX = lineA.first.x
                                yB in yMinA..yMaxA && aX in xMinB..xMaxB
                            } else false
                        }
                    }
                    val currentRect = RedTilesRect(
                        start = a,
                        end = b,
                        width = width,
                        height = height,
                        area = area,
                    )
                    uiStateUpdater.update {
                        it.copy(
                            solverStage2 = SolverStage2.Solving(
                                locations = this,
                                currentRect = currentRect,
                                partialSolution = result.toString(),
                            )
                        )
                    }
                    tinyDelay(DelayReason.Solver)
                    if (isCrossing) continue
                    result = area
                    largestRect = currentRect
                }
            }
            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        locations = this,
                        solution = result.toString(),
                        largestRect = largestRect!!,
                    )
                )
            }
        }
    }

    private fun String.parse(): Point2D {
        val (x, y) = split(",").map { it.toLong() }
        return Point2D(x, y)
    }
}