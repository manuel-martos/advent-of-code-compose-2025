package dev.mmartos.advent.screen.day08

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.regularDelay
import dev.mmartos.advent.utils.Delay.shortDelay
import dev.mmartos.advent.utils.DelayReason
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

data class Point3D<T : Number>(
    val x: T,
    val y: T,
    val z: T,
) {
    fun distance(other: Point3D<T>): Double {
        val squaredX = (x.toDouble() - other.x.toDouble()) * (x.toDouble() - other.x.toDouble())
        val squaredY = (y.toDouble() - other.y.toDouble()) * (y.toDouble() - other.y.toDouble())
        val squaredZ = (z.toDouble() - other.z.toDouble()) * (z.toDouble() - other.z.toDouble())
        return sqrt(squaredX + squaredY + squaredZ)
    }


    companion object {
        operator fun Double.times(point3D: Point3D<Double>): Point3D<Double> =
            Point3D(this * point3D.x, this * point3D.y, this * point3D.z)

        operator fun Point3D<Double>.plus(other: Point3D<Double>): Point3D<Double> =
            Point3D(x + other.x, y + other.y, z + other.z)

        operator fun Point3D<Double>.minus(other: Point3D<Double>): Point3D<Double> =
            Point3D(x - other.x, y - other.y, z - other.z)
    }
}

data class JunctionBoxes(
    val minPoint: Point3D<Long>,
    val maxPoint: Point3D<Long>,
    val center: Point3D<Double>,
    val boxes: PersistentList<Point3D<Long>>
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialBoxes: PersistentList<Point3D<Long>>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val junctionBoxes: JunctionBoxes,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val junctionBoxes: JunctionBoxes,
        val circuits: PersistentMap<Point3D<Long>, PersistentList<Point3D<Long>>>,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val junctionBoxes: JunctionBoxes,
        val circuits: PersistentMap<Point3D<Long>, PersistentList<Point3D<Long>>>,
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val junctionBoxes: JunctionBoxes,
        val circuits: PersistentMap<Point3D<Long>, PersistentList<Point3D<Long>>>,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val junctionBoxes: JunctionBoxes,
        val circuits: PersistentMap<Point3D<Long>, PersistentList<Point3D<Long>>>,
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day08UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day08ViewModel : BaseViewModel<ParserStage, JunctionBoxes, SolverStage1, SolverStage2>() {

    override val Day08UiState.parsedData: JunctionBoxes?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.junctionBoxes

    fun startParser(input: List<String>) = doParsing {
        val boxes = mutableListOf<Point3D<Long>>()
        runCatching {
            input.forEachIndexed { index, line ->
                boxes.add(line.parse())
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialBoxes = boxes.toPersistentList(),
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
                it.copy(
                    parserStage = ParserStage.Parsed(
                        junctionBoxes = JunctionBoxes(
                            minPoint = boxes.reduce { acc, d ->
                                Point3D(
                                    min(acc.x, d.x),
                                    min(acc.y, d.y),
                                    min(acc.z, d.z)
                                )
                            },
                            maxPoint = boxes.reduce { acc, d ->
                                Point3D(
                                    max(acc.x, d.x),
                                    max(acc.y, d.y),
                                    max(acc.z, d.z)
                                )
                            },
                            center = boxes
                                .reduce { acc, d -> Point3D(acc.x + d.x, acc.y + d.y, acc.z + d.z) }
                                .run {
                                    Point3D(
                                        this.x.toDouble() / boxes.size,
                                        this.y.toDouble() / boxes.size,
                                        this.z.toDouble() / boxes.size
                                    )
                                },
                            boxes = boxes.toPersistentList(),
                        ),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        uiState.value.parsedData?.run {
            // Provide initial solution before sorting boxes
            uiStateUpdater.update {
                it.copy(
                    solverStage1 = SolverStage1.Solving(
                        junctionBoxes = this,
                        circuits = persistentMapOf(),
                        partialSolution = "0",
                    )
                )
            }

            val pairs = boxes.flatMap { a ->
                (boxes - a).map { b ->
                    listOf(a, b).sortedWith(compareBy({ it.x }, { it.y }, { it.z }))
                        .let { it[0] to it[1] } to a.distance(b)
                }
            }.toMap().entries.sortedBy { it.value }.map { it.key }

            val circuits = mutableMapOf<Point3D<Long>, List<Point3D<Long>>>()
            var connectionsLeft = 1000
            for ((boxA, boxB) in pairs) {
                val circuitOfA = circuits[boxA] ?: setOf(boxA)
                val circuitOfB = circuits[boxB] ?: setOf(boxB)
                if (boxA !in circuitOfB) {
                    circuitOfA.forEach { inCircuit ->
                        circuits[inCircuit] = (circuits[inCircuit] ?: setOf(inCircuit)) + circuitOfB
                    }
                    circuitOfB.forEach { inCircuit ->
                        circuits[inCircuit] = (circuits[inCircuit] ?: setOf(inCircuit)) + circuitOfA
                    }
                }
                uiStateUpdater.update {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            junctionBoxes = this,
                            circuits = circuits.mapValues { entry -> entry.value.toPersistentList() }.toPersistentMap(),
                            partialSolution = circuits.calcResult().toString(),
                        )
                    )
                }
                shortDelay(DelayReason.Solver)

                connectionsLeft--
                if (connectionsLeft == 0) break
            }

            uiStateUpdater.update {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        junctionBoxes = this,
                        circuits = circuits.mapValues { entry -> entry.value.toPersistentList() }.toPersistentMap(),
                        solution = circuits.calcResult().toString(),
                    )
                )
            }
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        uiState.value.parsedData?.run {
            // Provide initial solution before sorting boxes
            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        junctionBoxes = this,
                        circuits = persistentMapOf(),
                        partialSolution = result.toString(),
                    )
                )
            }

            val pairs = boxes.flatMap { a ->
                (boxes - a).map { b ->
                    listOf(a, b).sortedWith(compareBy({ it.x }, { it.y }, { it.z }))
                        .let { it[0] to it[1] } to a.distance(b)
                }
            }.toMap().entries.sortedBy { it.value }.map { it.key }

            val circuits = mutableMapOf<Point3D<Long>, List<Point3D<Long>>>()
            for ((boxA, boxB) in pairs) {
                val circuitOfA = circuits[boxA] ?: setOf(boxA)
                val circuitOfB = circuits[boxB] ?: setOf(boxB)
                if (boxA !in circuitOfB) {
                    circuitOfA.forEach { inCircuit ->
                        circuits[inCircuit] = (circuits[inCircuit] ?: setOf(inCircuit)) + circuitOfB
                    }
                    circuitOfB.forEach { inCircuit ->
                        circuits[inCircuit] = (circuits[inCircuit] ?: setOf(inCircuit)) + circuitOfA
                    }
                }
                result = boxA.x * boxB.x
                uiStateUpdater.update {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            junctionBoxes = this,
                            circuits = buildMap {
                                val entry = circuits.entries.firstOrNull()
                                if (entry != null) {
                                    put(entry.key, entry.value.toPersistentList())
                                }
                            }.toPersistentMap(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                shortDelay(DelayReason.Solver)
                if (circuits[boxA]!!.size == boxes.size) break
            }

            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        junctionBoxes = this,
                        circuits = buildMap {
                            val entry = circuits.entries.firstOrNull()
                            if (entry != null) {
                                put(entry.key, entry.value.toPersistentList())
                            }
                        }.toPersistentMap(),
                        solution = result.toString(),
                    )
                )
            }
        }
    }

    private fun String.parse(): Point3D<Long> {
        val (x, y, z) = split(",").map { it.toLong() }
        return Point3D(x, y, z)
    }

    private fun Map<Point3D<Long>, List<Point3D<Long>>>.calcResult(): Long {
        val top = asSequence().map { it.value.toSet() }.distinct().map { it.size.toLong() }.sortedDescending().take(3)
            .toList()
        if (top.size < 3) return 0
        return top[0] * top[1] * top[2]
    }

}