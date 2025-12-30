package dev.mmartos.advent.screen.day11

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.customDelay
import dev.mmartos.advent.utils.Delay.regularDelay
import dev.mmartos.advent.utils.DelayReason
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

typealias Graph = PersistentMap<String, PersistentList<String>>

private data class Key(
    val node: String,
    val hasFft: Boolean,
    val hasDac: Boolean,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialGraph: Graph,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val graph: Graph,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val sourceNode: String,
        val targetNode: String,
        val visitedGraph: Graph,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val sourceNode: String,
        val targetNode: String,
        val lastGraph: Graph,
        val solution: String,
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val sourceNode: String,
        val targetNode: String,
        val middleNodes: PersistentList<String>,
        val visitedGraph: Graph,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val sourceNode: String,
        val targetNode: String,
        val middleNodes: PersistentList<String>,
        val lastGraph: Graph,
        val solution: String,
    ) : SolverStage2(), SolvedStage
}

typealias Day11UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day11ViewModel : BaseViewModel<ParserStage, Graph, SolverStage1, SolverStage2>() {

    override val Day11UiState.parsedData: Graph?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.graph

    fun startParser(input: List<String>) = doParsing {
        val graph = mutableMapOf<String, List<String>>()
        runCatching {
            input.forEach { line ->
                val currentNode = line.substringBefore(": ")
                val outputs = line.substringAfter(": ").split(" ")
                graph[currentNode] = outputs
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialGraph = graph.mapValues { (_, v) -> v.toPersistentList() }.toPersistentMap(),
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
                        graph = graph.mapValues { (_, v) -> v.toPersistentList() }.toPersistentMap(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        uiState.value.parsedData?.run {
            val sourceNode = "you"
            val targetNode = "out"
            val stack = ArrayDeque<String>()
            val visitedNodes = mutableSetOf<String>()
            var lastGraph: Graph = persistentMapOf()
            stack.add(sourceNode)
            visitedNodes.add(sourceNode)

            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                for (output in this[current]!!) {
                    visitedNodes.add(output)
                    if (output == targetNode) {
                        result++
                        lastGraph = visitedNodes.toGraph(this)
                        uiStateUpdater.update {
                            it.copy(
                                solverStage1 = SolverStage1.Solving(
                                    sourceNode = sourceNode,
                                    targetNode = targetNode,
                                    visitedGraph = lastGraph,
                                    partialSolution = result.toString(),
                                ),
                            )
                        }
                    } else {
                        stack.add(output)
                    }
                }
                regularDelay(DelayReason.Solver)
            }

            uiStateUpdater.update {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        sourceNode = sourceNode,
                        targetNode = targetNode,
                        lastGraph = lastGraph,
                        solution = result.toString(),
                    ),
                )
            }
        }
    }

    fun solvePart2() = doSolving {
        uiState.value.parsedData?.run {
            val sourceNode = "svr"
            val targetNode = "out"
            val middleNodes = persistentListOf("fft", "dac")
            var result = 0L

            data class Key(val node: String, val hasFft: Boolean, val hasDac: Boolean)

            data class Frame(
                val key: Key,
                var idx: Int = 0,
                var acc: Long = 0L
            )

            // --- Visualization knobs ---
            val stepDelayMs = 8L           // delay after each processed edge
            val hitDelayMs = 30L           // extra delay when a valid solution edge is found

            val stack = ArrayDeque<Frame>()
            val visitedNodes = mutableSetOf<String>()
            val cache = mutableMapOf<Key, Long>()
            var lastGraph: Graph = persistentMapOf()

            val root = Key(sourceNode, false, false)
            stack.addLast(Frame(root))
            visitedNodes.add(sourceNode)

            var processedEdges = 0L
            var foundValidHits = 0L

            // Initial UI state
            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        sourceNode = sourceNode,
                        targetNode = targetNode,
                        middleNodes = middleNodes,
                        visitedGraph = lastGraph,
                        partialSolution = foundValidHits.toString(),
                    ),
                )
            }
            customDelay(DelayReason.Solver, stepDelayMs)

            while (stack.isNotEmpty()) {
                val frame = stack.last()
                val key = frame.key

                // If this state is memoized, "return" to parent.
                cache[key]?.let { memo ->
                    stack.removeLast()
                    if (stack.isNotEmpty()) stack.last().acc += memo
                    continue
                }

                val outs = this[key.node] ?: emptyList()

                // Finished exploring this node/state -> memoize and return
                if (frame.idx >= outs.size) {
                    cache[key] = frame.acc
                    stack.removeLast()
                    if (stack.isNotEmpty()) stack.last().acc += frame.acc

                    val partial = cache[root] ?: (stack.firstOrNull()?.acc ?: 0L)
                    uiStateUpdater.update {
                        it.copy(
                            solverStage2 = SolverStage2.Solving(
                                sourceNode = sourceNode,
                                targetNode = targetNode,
                                middleNodes = middleNodes,
                                visitedGraph = lastGraph,
                                partialSolution = foundValidHits.toString(),
                            ),
                        )
                    }
                    customDelay(DelayReason.Solver, stepDelayMs)
                    continue
                }

                // Process next outgoing edge
                val output = outs[frame.idx++]
                visitedNodes.add(output)
                processedEdges++

                // Periodic UI update for intermediate stages (what we're exploring now)
                lastGraph = visitedNodes.toGraph(this)
                val partial = cache[root] ?: (stack.firstOrNull()?.acc ?: 0L)
                uiStateUpdater.update {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            sourceNode = sourceNode,
                            targetNode = targetNode,
                            middleNodes = middleNodes,
                            visitedGraph = lastGraph,
                            partialSolution = foundValidHits.toString(),
                        ),
                    )
                }

                if (output == targetNode) {
                    // EXACT match to reference logic
                    if (key.hasFft && key.hasDac) {
                        frame.acc += 1L
                        foundValidHits++

                        // More immediate UI update on a valid hit (this is fun to watch)
                        lastGraph = visitedNodes.toGraph(this)
                        val partial = cache[root] ?: (stack.firstOrNull()?.acc ?: 0L)
                        uiStateUpdater.update {
                            it.copy(
                                solverStage2 = SolverStage2.Solving(
                                    sourceNode = sourceNode,
                                    targetNode = targetNode,
                                    middleNodes = middleNodes,
                                    visitedGraph = lastGraph,
                                    partialSolution = foundValidHits.toString(),
                                ),
                            )
                        }
                        customDelay(DelayReason.Solver, hitDelayMs)
                    } else {
                        // Still delay a bit so you can see it touch "out" but not count
                        customDelay(DelayReason.Solver, stepDelayMs)
                    }
                } else {
                    val child = Key(
                        node = output,
                        hasFft = key.hasFft || output == "fft",
                        hasDac = key.hasDac || output == "dac"
                    )

                    val childMemo = cache[child]
                    if (childMemo != null) {
                        frame.acc += childMemo
                    } else {
                        stack.addLast(Frame(child))
                    }

                    customDelay(DelayReason.Solver, stepDelayMs)
                }
            }

            result = cache[root] ?: 0L

            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        sourceNode = sourceNode,
                        targetNode = targetNode,
                        middleNodes = middleNodes,
                        lastGraph = lastGraph,
                        solution = result.toString(),
                    ),
                )
            }
        }
    }

    private fun Set<String>.toGraph(graph: Graph): Graph =
        this.associateWith { graph[it] ?: persistentListOf() }.toPersistentMap()
}