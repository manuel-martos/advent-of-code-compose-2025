package dev.mmartos.advent.screen.day10

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import java.util.ArrayDeque
import java.util.StringTokenizer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import dev.mmartos.advent.common.ParserStage as BaseParserStage

data class Machine(
    val id: Int,
    val nLights: Int,
    val nButtons: Int,
    val target: String,
    val targetMask: Int,
    val buttonMasks: PersistentList<Int>,
    val joltage: PersistentList<Int>,
)

data class BFSPathStep(
    val buttonIndex: Int,
    val stateAfter: Int,
)

data class BFSPath(
    val presses: Int,
    val states: List<Int>,
    val steps: List<BFSPathStep>,
)

data class PropResult(
    val active: Int,
    val rem: List<Int>,
    val forced: List<Pair<Int, Int>>, // (sortedButtonIndex, times)
    val forcedCost: Int
)

data class PathStep(
    val buttonIndex: Int,
    val times: Int,
    val stateAfter: List<Int>,
)

data class SolutionPath(
    val presses: Int,
    val states: List<IntArray>,
    val steps: List<PathStep>,
    val pressCounts: List<Int>,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialMachines: PersistentList<Machine>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val machines: PersistentList<Machine>,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 {
    data class Solving(
        val currentMachine: Machine,
        val currentState: Int,
        val currentButtonIndex: Int?,
        val machineReady: Boolean,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val lastMachine: Machine,
        val solution: String,
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val currentMachine: Machine,
        val currentJoltages: PersistentList<Int>,
        val currentButtonIndex: Int?,
        val machineReady: Boolean,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val lastMachine: Machine,
        val solution: String,
    ) : SolverStage2()
}

typealias Day10UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day10ViewModel : BaseViewModel<ParserStage, PersistentList<Machine>, SolverStage1, SolverStage2>() {

    override val Day10UiState.parsedData: PersistentList<Machine>?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.machines

    fun startParser(input: List<String>) = doParsing {
        val machines = mutableListOf<Machine>()
        runCatching {
            val patternRegex = Regex("\\[([.#]+)]")
            val buttonRegex = Regex("\\(([^)]*)\\)")
            val joltageRegex = Regex("\\{([^}]*)}")
            input.forEachIndexed { index, line ->
                val patternMatch = patternRegex.find(line)!!
                val pattern = patternMatch.groupValues[1]
                val nLights = pattern.length

                // Build target mask: '#' -> 1, '.' -> 0
                var targetMask = 0
                for (i in pattern.indices) {
                    if (pattern[i] == '#') {
                        targetMask = targetMask or (1 shl i)
                    }
                }

                // --- Parse buttons ---
                val buttonMasks = mutableListOf<Int>()
                for (m in buttonRegex.findAll(line)) {
                    val inside = m.groupValues[1].trim()
                    if (inside.isEmpty()) continue

                    var mask = 0
                    val parts = inside.split(",")
                    for (p in parts) {
                        val idx = p.trim().toInt()
                        mask = mask or (1 shl idx)
                    }
                    buttonMasks += mask
                }

                // Parse target: {a,b,c,...}
                val jMatch = joltageRegex.find(line)!!
                val jStr = jMatch.groupValues[1]
                val tok = StringTokenizer(jStr, ",")
                val targetList = mutableListOf<Int>()
                while (tok.hasMoreTokens()) {
                    targetList += tok.nextToken().trim().toInt()
                }

                machines.add(
                    Machine(
                        id = index + 1,
                        nLights = nLights,
                        nButtons = buttonMasks.size,
                        target = patternMatch.groupValues[1],
                        targetMask = targetMask,
                        buttonMasks = buttonMasks.toPersistentList(),
                        joltage = targetList.toPersistentList(),
                    )
                )
                _uiState.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialMachines = machines.toPersistentList(),
                        ),
                    )
                }
                delay(5)
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
                        machines = machines.toPersistentList(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0
        var currentDelay = 1500L
        _uiState.value.parsedData?.run {
            var lastMachine: Machine = first()
            forEachIndexed { index, machine ->
                val path = machine.solveMachineWithStates()
                val initialState = path.states.first()
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            currentMachine = machine,
                            currentState = initialState,
                            currentButtonIndex = null,
                            partialSolution = result.toString(),
                            machineReady = initialState == machine.targetMask,
                        ),
                    )
                }
                delay(currentDelay)

                path.steps.forEach { step ->
                    _uiState.threadSafeUpdate {
                        result++
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                currentMachine = machine,
                                currentState = step.stateAfter,
                                currentButtonIndex = step.buttonIndex,
                                partialSolution = result.toString(),
                                machineReady = step.stateAfter == machine.targetMask,
                            ),
                        )
                    }
                    delay(currentDelay)
                }

                currentDelay = if (index < 12) (currentDelay * 0.75).toLong() else currentDelay
                lastMachine = machine
            }
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        lastMachine = lastMachine,
                        solution = result.toString(),
                    ),
                )
            }
        }
    }

    private fun Machine.solveMachineWithStates(): BFSPath {
        val totalStates = 1 shl nLights
        val dist = IntArray(totalStates) { -1 }
        val parent = IntArray(totalStates) { -1 }
        val parentButton = IntArray(totalStates) { -1 }

        val q = ArrayDeque<Int>()
        dist[0] = 0
        q.add(0)

        while (q.isNotEmpty()) {
            val curr = q.removeFirst()
            if (curr == targetMask) break

            for (i in buttonMasks.indices) {
                val next = curr xor buttonMasks[i]
                if (dist[next] == -1) {
                    dist[next] = dist[curr] + 1
                    parent[next] = curr
                    parentButton[next] = i
                    q.add(next)
                }
            }
        }

        if (dist[targetMask] == -1) return BFSPath(-1, emptyList(), emptyList())

        // Reconstruct states from target back to 0
        val revStates = mutableListOf<Int>()
        var s = targetMask
        revStates.add(s)
        while (s != 0) {
            s = parent[s]
            revStates.add(s)
        }
        val states = revStates.reversed() // 0 -> ... -> target

        // Reconstruct per-press info (button + resulting state)
        val steps = mutableListOf<BFSPathStep>()
        for (k in 1 until states.size) {
            val after = states[k]
            val btn = parentButton[after]
            steps.add(BFSPathStep(btn, after))
        }

        return BFSPath(dist[targetMask], states, steps)
    }


    fun solvePart2() = doSolving {
        var result = 0
        var currentDelay = 1500L
        _uiState.value.parsedData?.run {
            val solutions = map { machine ->
                async {
                    machine.solveMachineWithVectors()
                }
            }
            var lastMachine: Machine = first()
            forEachIndexed { index, machine ->
                val path = solutions[index].await()
                val currentState = path.states.first()
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            currentMachine = machine,
                            currentJoltages = currentState.toList().toPersistentList(),
                            currentButtonIndex = null,
                            partialSolution = result.toString(),
                            machineReady = false,
                        ),
                    )
                }
                delay(currentDelay)
                path.steps.forEachIndexed { index, step ->
                    repeat(step.times) { curTime ->
                        val isReady = index == path.steps.indices.last && curTime == step.times - 1
                        currentState.indices.forEach { index ->
                            if ((machine.buttonMasks[step.buttonIndex] and (1 shl index)) != 0) {
                                currentState[index] = currentState[index] + 1
                            }
                        }
                        result++
                        _uiState.threadSafeUpdate {
                            it.copy(
                                solverStage2 = SolverStage2.Solving(
                                    currentMachine = machine,
                                    currentJoltages = currentState.toList().toPersistentList(),
                                    currentButtonIndex = step.buttonIndex,
                                    partialSolution = result.toString(),
                                    machineReady = isReady,
                                ),
                            )
                        }
                        delay((0.7f * currentDelay).roundToLong())
                        _uiState.threadSafeUpdate {
                            it.copy(
                                solverStage2 = SolverStage2.Solving(
                                    currentMachine = machine,
                                    currentJoltages = currentState.toList().toPersistentList(),
                                    currentButtonIndex = null,
                                    partialSolution = result.toString(),
                                    machineReady = isReady,
                                ),
                            )
                        }
                        delay((0.3f * currentDelay).roundToLong())
                        currentDelay = if (result < 22) (currentDelay * 0.75).toLong() else currentDelay
                    }
                }
                lastMachine = machine
            }
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        lastMachine = lastMachine,
                        solution = result.toString(),
                    ),
                )
            }
        }
    }

    private fun Machine.solveMachineWithVectors(): SolutionPath {
        val joltages = this.joltage.size
        val buttons = this.nButtons

        // Sort buttons by descending coverage for speed, but keep mapping to original indices
        val coverIn = IntArray(buttons) { buttonMasks[it].countOneBits() }
        val order = (0 until buttons).sortedByDescending { coverIn[it] }.toIntArray()

        val masksSorted = IntArray(buttons) { buttonMasks[order[it]] }
        val coverSorted = IntArray(buttons) { coverIn[order[it]] }
        val sortedIndexToOriginal = IntArray(buttons) { order[it] }

        // touch[i] = bitmask of sorted-buttons that touch counter i
        val touch = IntArray(joltages)
        for (j in 0 until buttons) {
            val bm = masksSorted[j]
            for (i in 0 until joltages) if ((bm and (1 shl i)) != 0) touch[i] = touch[i] or (1 shl j)
        }

        // Feasibility
        for (i in 0 until joltages) {
            if (this.joltage[i] > 0 && touch[i] == 0) {
                return SolutionPath(INF, emptyList(), emptyList(), List(buttons) { 0 })
            }
        }

        val fullActive = (1 shl buttons) - 1
        val memo = HashMap<String, Int>(1 shl 16)

        // Solve and reconstruct in sorted-index space
        val optimal = solveCost(fullActive, this.joltage.toIntArray(), joltages, touch, masksSorted, memo, coverSorted)
        val actionsSorted = reconstruct(
            fullActive,
            this.joltage.toIntArray(),
            joltages,
            touch,
            masksSorted,
            memo,
            coverSorted
        ).filter { it.second > 0 }

        // Convert to original indices and accumulate press counts
        val pressCountsOriginal = IntArray(buttons) { 0 }
        val actionsOriginal = actionsSorted.map { (sortedIdx, times) ->
            val origIdx = sortedIndexToOriginal[sortedIdx]
            pressCountsOriginal[origIdx] += times
            origIdx to times
        }

        // Simulate to produce full vector states
        val states = ArrayList<IntArray>()
        val steps = ArrayList<PathStep>()
        val state = IntArray(joltages) { 0 }
        states.add(state.clone()) // initial

        for ((origIdx, times) in actionsOriginal) {
            val mask = this.buttonMasks[origIdx]
            // apply times presses
            for (t in 0 until times) {
                for (i in 0 until joltages) if ((mask and (1 shl i)) != 0) state[i]++
            }
            val snapshot = state.clone()
            steps.add(PathStep(buttonIndex = origIdx, times = times, stateAfter = snapshot.toList()))
            states.add(snapshot)
        }

        return SolutionPath(
            presses = optimal,
            states = states,
            steps = steps,
            pressCounts = pressCountsOriginal.toList(),
        )
    }

    fun key(active: Int, rem: IntArray, joltages: Int): String {
        val sb = StringBuilder(16 + joltages * 4)
        sb.append(active).append('|')
        for (i in 0 until joltages) sb.append(rem[i]).append(',')
        return sb.toString()
    }

    fun propagate(
        activeStart: Int,
        remStart: IntArray,
        joltages: Int,
        touch: IntArray,
        masksSorted: IntArray
    ): PropResult? {
        var active = activeStart
        val rem = remStart.clone()
        val forced = ArrayList<Pair<Int, Int>>()
        var forcedCost = 0

        while (true) {
            var changed = false
            for (i in 0 until joltages) {
                val need = rem[i]
                if (need == 0) continue
                val cand = touch[i] and active
                if (cand == 0) return null
                if (cand.countOneBits() == 1) {
                    val j = cand.countTrailingZeroBits()
                    val bm = masksSorted[j]

                    // must not overshoot
                    for (c in 0 until joltages) {
                        if ((bm and (1 shl c)) != 0 && rem[c] < need) return null
                    }

                    forced.add(j to need)
                    forcedCost += need
                    for (c in 0 until joltages) if ((bm and (1 shl c)) != 0) rem[c] -= need
                    active = active and (1 shl j).inv()

                    changed = true
                    break
                }
            }
            if (!changed) break
        }

        return PropResult(active, rem.toList(), forced, forcedCost)
    }

    /**
     * Minimal remaining presses from (active, rem).
     * Memo stores bestRest from canonical (activeP, remP), excluding forcedCost.
     */
    fun solveCost(
        active0: Int,
        rem0: IntArray,
        joltages: Int,
        touch: IntArray,
        masksSorted: IntArray,
        memo: HashMap<String, Int>,
        coverSorted: IntArray,
    ): Int {
        val prop = propagate(active0, rem0, joltages, touch, masksSorted) ?: return INF
        val active = prop.active
        val rem = prop.rem
        val forcedCost = prop.forcedCost

        var sum = 0
        for (v in rem) sum += v
        if (sum == 0) return forcedCost

        val k = key(active, rem.toIntArray(), joltages)
        memo[k]?.let { bestRest -> return forcedCost + bestRest }

        if (active == 0) return INF

        // choose most constrained counter (>1 candidates)
        var chosenI = -1
        var chosenCnt = INF
        for (i in 0 until joltages) {
            if (rem[i] == 0) continue
            val cnt = (touch[i] and active).countOneBits()
            if (cnt == 0) return INF
            if (cnt > 1 && cnt < chosenCnt) {
                chosenCnt = cnt; chosenI = i
            }
        }
        if (chosenI == -1) return INF

        val need = rem[chosenI]
        val candMask = touch[chosenI] and active

        // branch on candidate with largest coverage
        var branchJ = -1
        var bestCov = -1
        var tmp = candMask
        while (tmp != 0) {
            val j = tmp.takeLowestOneBit().countTrailingZeroBits()
            if (coverSorted[j] > bestCov) {
                bestCov = coverSorted[j]; branchJ = j
            }
            tmp = tmp and (tmp - 1)
        }
        val bm = masksSorted[branchJ]

        // cap(branchJ)
        var capBranch = Int.MAX_VALUE
        for (c in 0 until joltages) if ((bm and (1 shl c)) != 0) capBranch = min(capBranch, rem[c])
        if (capBranch == Int.MAX_VALUE) capBranch = 0

        // sumOtherCaps to bound minK
        var sumOtherCaps = 0
        tmp = candMask
        while (tmp != 0) {
            val j = tmp.takeLowestOneBit().countTrailingZeroBits()
            if (j != branchJ) {
                val bmask = masksSorted[j]
                var cap = Int.MAX_VALUE
                for (c in 0 until joltages) if ((bmask and (1 shl c)) != 0) cap = min(cap, rem[c])
                if (cap == Int.MAX_VALUE) cap = 0
                sumOtherCaps += cap
            }
            tmp = tmp and (tmp - 1)
        }

        val minK = max(0, need - sumOtherCaps)
        val maxK = min(capBranch, need)
        if (minK > maxK) return INF

        var bestRest = INF
        for (presses in maxK downTo minK) {
            val remNext = rem.toIntArray().clone()
            if (presses > 0) {
                for (c in 0 until joltages) if ((bm and (1 shl c)) != 0) remNext[c] -= presses
            }
            val sub =
                solveCost(active and (1 shl branchJ).inv(), remNext, joltages, touch, masksSorted, memo, coverSorted)
            if (sub != INF) bestRest = min(bestRest, presses + sub)
        }

        memo[k] = bestRest
        return forcedCost + bestRest
    }

    fun reconstruct(
        active0: Int,
        rem0: IntArray,
        joltages: Int,
        touch: IntArray,
        masksSorted: IntArray,
        memo: HashMap<String, Int>,
        coverSorted: IntArray,
    ): List<Pair<Int, Int>> {
        val prop = propagate(active0, rem0, joltages, touch, masksSorted) ?: return emptyList()
        val active = prop.active
        val rem = prop.rem
        val actions = prop.forced.toMutableList()

        var sum = 0
        for (v in rem) sum += v
        if (sum == 0) return actions

        val k = key(active, rem.toIntArray(), joltages)
        val bestRest = memo[k] ?: INF
        if (bestRest == INF) return actions

        // choose most constrained counter
        var chosenI = -1
        var chosenCnt = INF
        for (i in 0 until joltages) {
            if (rem[i] == 0) continue
            val cnt = (touch[i] and active).countOneBits()
            if (cnt > 1 && cnt < chosenCnt) {
                chosenCnt = cnt; chosenI = i
            }
        }
        if (chosenI == -1) return actions

        val need = rem[chosenI]
        val candMask = touch[chosenI] and active

        // branch button (same as solveCost)
        var branchJ = -1
        var bestCov = -1
        var tmp = candMask
        while (tmp != 0) {
            val j = tmp.takeLowestOneBit().countTrailingZeroBits()
            if (coverSorted[j] > bestCov) {
                bestCov = coverSorted[j]; branchJ = j
            }
            tmp = tmp and (tmp - 1)
        }
        val bm = masksSorted[branchJ]

        var capBranch = Int.MAX_VALUE
        for (c in 0 until joltages) if ((bm and (1 shl c)) != 0) capBranch = min(capBranch, rem[c])
        if (capBranch == Int.MAX_VALUE) capBranch = 0

        var sumOtherCaps = 0
        tmp = candMask
        while (tmp != 0) {
            val j = tmp.takeLowestOneBit().countTrailingZeroBits()
            if (j != branchJ) {
                val bmask = masksSorted[j]
                var cap = Int.MAX_VALUE
                for (c in 0 until joltages) if ((bmask and (1 shl c)) != 0) cap = min(cap, rem[c])
                if (cap == Int.MAX_VALUE) cap = 0
                sumOtherCaps += cap
            }
            tmp = tmp and (tmp - 1)
        }

        val minK = max(0, need - sumOtherCaps)
        val maxK = min(capBranch, need)

        for (presses in maxK downTo minK) {
            val remNext = rem.toIntArray().clone()
            if (presses > 0) for (c in 0 until joltages) if ((bm and (1 shl c)) != 0) remNext[c] -= presses

            val subCost =
                solveCost(active and (1 shl branchJ).inv(), remNext, joltages, touch, masksSorted, memo, coverSorted)
            if (subCost != INF && presses + subCost == bestRest) {
                actions.add(branchJ to presses)
                actions.addAll(
                    reconstruct(
                        active and (1 shl branchJ).inv(),
                        remNext,
                        joltages,
                        touch,
                        masksSorted,
                        memo,
                        coverSorted
                    )
                )
                return actions
            }
        }

        return actions
    }

    private companion object {
        private const val INF = 1_000_000_000
    }
}