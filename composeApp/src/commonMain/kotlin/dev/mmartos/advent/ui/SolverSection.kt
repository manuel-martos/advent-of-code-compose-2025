package dev.mmartos.advent.ui

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.check_mark
import advent_of_code_compose_2025.composeapp.generated.resources.right_arrow
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolverStage

@Composable
@Suppress("UNCHECKED_CAST")
fun SolverSection(
    solverStage: SolverStage,
    modifier: Modifier = Modifier,
    solverContent: @Composable ColumnScope.(SolverStage) -> Unit,
) {
    SectionContainer(
        title = solverStage.resolveSectionTitle(),
        outline = solverStage.resolveSectionOutlineColor(),
        modifier = modifier.fillMaxSize(),
    ) {
        solverContent.invoke(this, solverStage)
    }
}

@Composable
@ReadOnlyComposable
private fun SolverStage.resolveSectionTitle(): Title =
    when {
        isSolving() -> Title(
            icon = Res.drawable.right_arrow,
            text = "${solverPart().resolvePartName()} - Solving"
        )

        else -> Title(
            icon = Res.drawable.check_mark,
            text = "${solverPart().resolvePartName()} - Solved"
        )
    }

@Composable
@ReadOnlyComposable
private fun SolverPart.resolvePartName(): String =
    when (this) {
        SolverPart.SOLVER_PART_1 -> "Part 1"
        SolverPart.SOLVER_PART_2 -> "Part 2"
    }

@Composable
private fun SolverStage.resolveSectionOutlineColor(): Color =
    when {
        isSolving() -> MaterialTheme.colorScheme.outline
        else -> Color(0xff98fb98)
    }
