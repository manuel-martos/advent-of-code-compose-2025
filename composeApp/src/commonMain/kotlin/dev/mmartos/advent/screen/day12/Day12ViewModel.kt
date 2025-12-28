package dev.mmartos.advent.screen.day12

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Point2D
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import dev.mmartos.advent.common.ParserStage as BaseParserStage

data class Shape(
    val points: List<Point2D>,
)

data class Region(
    val width: Int,
    val height: Int,
    val presents: PersistentList<Int>,
)

data class PlacedShape(
    val id: Int,
    val position: Point2D,
    val shape: Shape,
)

data class Challenge(
    val shapes: PersistentList<Shape>,
    val regions: PersistentList<Region>,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialShapes: PersistentList<Shape>,
        val partialRegions: PersistentList<Region>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val challenge: Challenge,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 {
    data class Solving(
        val currentRegion: Region,
        val layout: PersistentList<PlacedShape>,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val lastRegion: Region,
        val lastLayout: PersistentList<PlacedShape>,
        val solution: String,
    ) : SolverStage1()
}

typealias Day12UiState = UiState<ParserStage, SolverStage1, Nothing>

class Day12ViewModel : BaseViewModel<ParserStage, Challenge, SolverStage1, Nothing>() {

    override val Day12UiState.parsedData: Challenge?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.challenge

    fun startParser(input: List<String>) = doParsing {
        val shapes = mutableListOf<Shape>()
        val regions = mutableListOf<Region>()
        val shapeHeaderRegex = Regex("""^(\d+):$""")
        val regionRegex = Regex("""^(\d+)x(\d+):\s*(.*)$""")
        val presentsRegex = Regex("""\s+""")
        runCatching {
            var i = 0
            var inRegions = false
            while (i < input.size) {
                val line = input[i]
                if (line.isBlank()) {
                    _uiState.update {
                        it.copy(
                            parserStage = ParserStage.Parsing(
                                currentLine = line,
                                partialShapes = shapes.toPersistentList(),
                                partialRegions = regions.toPersistentList(),
                            ),
                        )
                    }
                    delay(5)
                    i++; continue
                }

                val regionMatch = regionRegex.find(line)
                if (regionMatch != null) {
                    inRegions = true
                    val width = regionMatch.groupValues[1].toInt()
                    val height = regionMatch.groupValues[2].toInt()
                    val rest = regionMatch.groupValues[3].trim()
                    val presents = if (rest.isEmpty()) emptyList() else rest.split(presentsRegex).map { it.toInt() }
                    regions.add(
                        Region(
                            width = width,
                            height = height,
                            presents = presents.toPersistentList(),
                        )
                    )
                    i++
                    _uiState.update {
                        it.copy(
                            parserStage = ParserStage.Parsing(
                                currentLine = line,
                                partialShapes = shapes.toPersistentList(),
                                partialRegions = regions.toPersistentList(),
                            ),
                        )
                    }
                    delay(5)
                    continue
                }

                if (!inRegions) {
                    val shapeHeader = shapeHeaderRegex.find(line)
                    if (shapeHeader != null) {
                        val rows = ArrayList<String>()
                        i++
                        while (i < input.size) {
                            val r = input[i]
                            if (r.isBlank()) break
                            // Stop if next shape header or region line (defensive)
                            if (Regex("""^\d+:$""").matches(r) || Regex("""^\d+x\d+:.*$""").matches(r)) break
                            rows.add(r)
                            _uiState.update {
                                it.copy(
                                    parserStage = ParserStage.Parsing(
                                        currentLine = r,
                                        partialShapes = shapes.toPersistentList(),
                                        partialRegions = regions.toPersistentList(),
                                    ),
                                )
                            }
                            delay(5)
                            i++
                        }
                        val grid = Array(3) { r ->
                            val row = rows[r]
                            BooleanArray(3) { c -> row[c] == '#' }
                        }

                        shapes.add(Shape(grid.toSinglePolygon()))
                        _uiState.update {
                            it.copy(
                                parserStage = ParserStage.Parsing(
                                    currentLine = line,
                                    partialShapes = shapes.toPersistentList(),
                                    partialRegions = regions.toPersistentList(),
                                ),
                            )
                        }
                        delay(5)
                        continue
                    }
                }

                // If we get here, it's an unexpected line; skip it safely.
                i++
            }
        }.onFailure {
            _uiState.update {
                it.copy(
                    parserStage = ParserStage.Error,
                )
            }
        }.onSuccess {
            _uiState.update {
                it.copy(
                    parserStage = ParserStage.Parsed(
                        challenge = Challenge(
                            shapes = shapes.toPersistentList(),
                            regions = regions.toPersistentList()
                        ),
                    ),
                )
            }
            solvePart1()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        _uiState.value.parsedData?.run {
            var lastRegion = regions.first()
            var lastLayout = persistentListOf<PlacedShape>()
            regions.forEach { region ->
                val widthDiv3 = region.width / 3
                val heightDiv3 = region.height / 3
                val squares = widthDiv3 * heightDiv3
                val presents = region.presents.sum()
                val isFeasible = presents <= squares
                if (isFeasible) {
                    result++
                    _uiState.threadSafeUpdate {
                        val allPlacedPresents = buildList {
                            region.presents.forEachIndexed { index, presents ->
                                repeat(presents) {
                                    add(shapes[index])
                                }
                            }
                        }.withIndex().shuffled().mapIndexed { index, (id, shape) ->
                            PlacedShape(
                                id = id,
                                position = Point2D((index % widthDiv3).toLong() * 3, (index / widthDiv3).toLong() * 3),
                                shape = shape,
                            )
                        }.toPersistentList()
                        lastRegion = region
                        lastLayout = allPlacedPresents
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                currentRegion = region,
                                layout = allPlacedPresents,
                                partialSolution = result.toString()
                            )
                        )
                    }
                } else {
                    _uiState.threadSafeUpdate {
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                currentRegion = region,
                                layout = persistentListOf(),
                                partialSolution = result.toString()
                            )
                        )
                    }
                }
                delay(10)
            }
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        lastRegion = lastRegion,
                        lastLayout = lastLayout,
                        solution = result.toString()
                    )
                )
            }
        }
    }

    fun Array<BooleanArray>.toSinglePolygon(): List<Point2D> {
        val h = size
        val w = first().size

        // 1. Identify all unit edges from filled cells
        // 2. Group them by canonical coordinates (undirected)
        // 3. Keep only those appearing exactly once (the boundary)
        val boundaryEdges = (0 until h).flatMap { y ->
            (0 until w).filter { x -> this[y][x] }.flatMap { x ->
                val p00 = Point2D(x.toLong(), y.toLong())
                val p01 = Point2D(x.toLong(), (y + 1).toLong())
                val p10 = Point2D((x + 1).toLong(), y.toLong())
                val p11 = Point2D((x + 1).toLong(), (y + 1).toLong())

                // Define edges: Top, Right, Bottom, Left
                listOf(p00 to p10, p10 to p11, p11 to p01, p01 to p00)
            }
        }.groupingBy { (p1, p2) ->
            // Canonical key for undirected edge: (min, max)
            if (p1 < p2) p1 to p2 else p2 to p1
        }.eachCount()
            .filterValues { it == 1 }
            .keys

        if (boundaryEdges.isEmpty()) return emptyList()

        // Build adjacency list for traversal: Point -> Neighbors
        val adjacency = boundaryEdges.flatMap { (u, v) ->
            listOf(u to v, v to u)
        }.groupBy({ it.first }, { it.second })

        // Find top-leftmost point to start
        val startNode = adjacency.keys.minOrNull() ?: return emptyList()

        // Reconstruct the path
        return buildList {
            var current = startNode
            // Prefer moving Right initially if possible, otherwise any neighbor
            var next = adjacency[startNode]!!.maxByOrNull {
                if (it.y == startNode.y && it.x > startNode.x) 1 else 0
            }!!

            // We use a Set to track visited edges to handle traversal correctly
            val visitedEdges = mutableSetOf<Pair<Point2D, Point2D>>()

            add(current)

            while (next != startNode) {
                // Mark edge as visited (canonical form)
                val edge = if (current < next) current to next else next to current
                visitedEdges.add(edge)

                add(next)

                val prev = current
                current = next

                // Find next unvisited neighbor
                // Logic: A point in a simple polygon has 2 neighbors.
                // We want the one that isn't 'prev'.
                next = adjacency[current]!!.first { neighbor ->
                    neighbor != prev
                }
            }
        }.simplifyCollinear().normalizeOutline()
    }

    private fun List<Point2D>.simplifyCollinear(): List<Point2D> {
        if (size < 3) return this

        return filterIndexed { i, cur ->
            val prev = this[(i - 1 + size) % size]
            val next = this[(i + 1) % size]

            // Cross product approach to check collinearity
            val dx1 = cur.x - prev.x
            val dy1 = cur.y - prev.y
            val dx2 = next.x - cur.x
            val dy2 = next.y - cur.y

            // Keep point only if cross product is non-zero (direction changes)
            (dx1 * dy2 - dy1 * dx2) != 0L
        }
    }

    private fun List<Point2D>.normalizeOutline(): List<Point2D> {
        if (isEmpty()) return this

        // Helper to rotate a list so `startIndex` becomes index 0
        fun List<Point2D>.rotate(n: Int): List<Point2D> =
            drop(n) + take(n)

        val minIndex = withIndex().minBy { it.value }.index
        val rotated = rotate(minIndex)

        // Check if the first edge goes "Right". If not, we might be anti-clockwise
        // or starting vertically. The requirements imply a preference for Right.
        val p0 = rotated[0]
        val p1 = rotated[1]

        // If we are moving Right immediately, we are good.
        if (p1.y == p0.y && p1.x > p0.x) {
            return rotated
        }

        // Otherwise, reverse and re-orient
        val reversed = rotated.asReversed()
        // Re-find min index after reverse
        val revMinIndex = reversed.withIndex().minBy { it.value }.index
        val revRotated = reversed.rotate(revMinIndex)

        // Check if reversed version starts by going Right
        val rp0 = revRotated[0]
        val rp1 = revRotated[1]

        return if (rp1.y == rp0.y && rp1.x > rp0.x) revRotated else rotated
    }

}

