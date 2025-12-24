package dev.mmartos.advent.common

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

abstract class BaseViewModel<PS : ParserStage, PD, SS1, SS2>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    protected val _uiState: MutableStateFlow<UiState<PS, SS1, SS2>> = MutableStateFlow(UiState.initialState())
    val uiState: StateFlow<UiState<PS, SS1, SS2>> = _uiState.asStateFlow()

    private val currentJobs: MutableList<Job> = mutableListOf()

    abstract val UiState<PS, SS1, SS2>.parsedData: PD?

    protected fun doParsing(block: suspend CoroutineScope.() -> Unit) {
        currentJobs += viewModelScope.launch(dispatcher) {
            _uiState.update { UiState.initialState() }
            block()
        }
    }

    protected fun doSolving(block: suspend CoroutineScope.() -> Unit) {
        currentJobs += viewModelScope.launch(dispatcher) {
            block()
        }
    }

    fun stop() {
        viewModelScope.launch(dispatcher) {
            currentJobs.forEach { it.cancel() }
            currentJobs.joinAll()
            _uiState.update { UiState.initialState() }
        }
    }
}

@Stable
interface ParserStage {
    fun isParsing(): Boolean
    fun isParsed(): Boolean
    fun isError(): Boolean
}

@Stable
interface ParsingStage : ParserStage {
    override fun isParsing(): Boolean = true
    override fun isParsed(): Boolean = false
    override fun isError(): Boolean = false
}

@Stable
interface ParsedStage : ParserStage {
    override fun isParsing(): Boolean = false
    override fun isParsed(): Boolean = true
    override fun isError(): Boolean = false
}

@Stable
interface ErrorStage : ParserStage {
    override fun isParsing(): Boolean = false
    override fun isParsed(): Boolean = false
    override fun isError(): Boolean = true
}

@Immutable
data class UiState<PS : ParserStage, SS1, SS2>(
    val parserStage: PS? = null,
    val solverStage1: SS1? = null,
    val solverStage2: SS2? = null,
) {
    fun isSolving(): Boolean = solverStage1 != null || solverStage2 != null

    companion object {
        fun <PS : ParserStage, SS1, SS2> initialState() = UiState<PS, SS1, SS2>()
    }
}
