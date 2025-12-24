package dev.mmartos.advent.screen.day03

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import dev.mmartos.advent.common.ParserStage as BaseParserStage

data class SolvedBatteriesBank(
    val batteriesBank: String,
    val selectedBatteries: Map<Int, Char>,
) {
    fun toJoltage(): Long =
        selectedBatteries.values.joinToString("").toLong()
}

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val batteriesBanks: PersistentList<String>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val batteriesBanks: PersistentList<String>,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 {
    data class Solving(
        val partialSolvedBatteriesBank: SolvedBatteriesBank?,
        val solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
        val solution: String
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val partialSolvedBatteriesBank: SolvedBatteriesBank?,
        val solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
        val solution: String
    ) : SolverStage2()
}

typealias Day03UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day03ViewModel : BaseViewModel<ParserStage, List<String>, SolverStage1, SolverStage2>() {

    override val Day03UiState.parsedData: List<String>?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.batteriesBanks

    fun startParser(input: List<String>) = doParsing {
        val batteriesBanks = mutableListOf<String>()
        runCatching {
            input.forEach { line ->
                batteriesBanks += line
                _uiState.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            batteriesBanks = batteriesBanks.toPersistentList(),
                        )
                    )
                }
                delay(10)
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
                        batteriesBanks = batteriesBanks.toPersistentList(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        val solvedBatteriesBanks = mutableListOf<SolvedBatteriesBank>()
        _uiState.value.parsedData?.forEach { batteriesBank ->
            val selectedBatteries = calcSelectedBatteries(
                batteriesBank = batteriesBank,
                maxBatteries = 2
            ) { partialSolution ->
                val partialSolvedBatteriesBank = SolvedBatteriesBank(
                    batteriesBank = batteriesBank,
                    selectedBatteries = partialSolution
                )
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            partialSolvedBatteriesBank = partialSolvedBatteriesBank,
                            solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(1)
            }
            val solvedBatteriesBank = SolvedBatteriesBank(
                batteriesBank = batteriesBank,
                selectedBatteries = selectedBatteries
            )
            solvedBatteriesBanks.add(solvedBatteriesBank)
            result += solvedBatteriesBank.toJoltage()
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage1 = SolverStage1.Solving(
                        partialSolvedBatteriesBank = null,
                        solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                        partialSolution = result.toString(),
                    )
                )
            }
        }
        _uiState.threadSafeUpdate {
            it.copy(
                solverStage1 = SolverStage1.Solved(
                    solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                    solution = result.toString(),
                )
            )
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        val solvedBatteriesBanks = mutableListOf<SolvedBatteriesBank>()
        _uiState.value.parsedData?.forEach { batteriesBank ->
            val selectedBatteries = calcSelectedBatteries(
                batteriesBank = batteriesBank,
                maxBatteries = 12
            ) { partialSolution ->
                val partialSolvedBatteriesBank = SolvedBatteriesBank(
                    batteriesBank = batteriesBank,
                    selectedBatteries = partialSolution
                )
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            partialSolvedBatteriesBank = partialSolvedBatteriesBank,
                            solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(1)
            }
            val solvedBatteriesBank = SolvedBatteriesBank(
                batteriesBank = batteriesBank,
                selectedBatteries = selectedBatteries
            )
            solvedBatteriesBanks.add(solvedBatteriesBank)
            result += solvedBatteriesBank.toJoltage()
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        partialSolvedBatteriesBank = null,
                        solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                        partialSolution = result.toString(),
                    )
                )
            }
        }
        _uiState.threadSafeUpdate {
            it.copy(
                solverStage2 = SolverStage2.Solved(
                    solvedBatteriesBanks = solvedBatteriesBanks.toPersistentList(),
                    solution = result.toString(),
                )
            )
        }
    }

    private suspend fun calcSelectedBatteries(
        batteriesBank: String,
        maxBatteries: Int,
        onPartialSolution: suspend (Map<Int, Char>) -> Unit
    ): Map<Int, Char> {
        val length = batteriesBank.length
        var toRemove = length - maxBatteries
        val selectedBatteries = mutableListOf<Pair<Int, Char>>()

        batteriesBank.forEachIndexed { index, ch ->
            while (toRemove > 0 && selectedBatteries.isNotEmpty() && selectedBatteries[selectedBatteries.size - 1].second < ch) {
                selectedBatteries.removeLast()
                toRemove--
            }
            selectedBatteries.add(index to ch)
            onPartialSolution(selectedBatteries.toMap())
        }

        repeat(toRemove) {
            selectedBatteries.removeLast()
        }
        return selectedBatteries.toMap()
    }

}