package dev.mmartos.advent.screen.day08

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.screen.day08.Vec3.Companion.times
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.SolutionLayout
import dev.mmartos.advent.ui.SolverSection
import dev.mmartos.advent.utils.leadingSpaces
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day08Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day08ViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    DayScaffold(
        dayDetails = dayDetails,
        puzzleInput = puzzleInput,
        uiState = uiState,
        onStart = { input -> vm.startParser(input) },
        onBackClicked = onBackClicked,
        onDispose = { vm.stop() },
        parserContent = { parserStage, modifier ->
            ParserSection(
                parserStage = parserStage,
                modifier = modifier,
            )
        },
        solverContent1 = { solverStage, modifier ->
            Solver1Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        solverContent2 = { solverStage, modifier ->
            Solver2Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        modifier = modifier,
        parsingHeight = 360.dp,
        solvingHeight = 560.dp,
    )
}

@Composable
private fun ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
) {
    ParserSection(
        parserStage = parserStage,
        modifier = modifier,
        parsingContent = { parsingStage: ParserStage.Parsing, modifier: Modifier ->
            ParsingContent(
                parserStage = parsingStage,
                modifier = modifier
            )
        },
        parsedContent = { parsedStage: ParserStage.Parsed, modifier: Modifier ->
            ParsedContent(
                parserStage = parsedStage,
                modifier = modifier
            )
        },
    )
}

@Composable
private fun ParsingContent(
    parserStage: ParserStage.Parsing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CurrentElement(
                title = "Current line:",
                currentItem = parserStage.currentLine,
                layout = CurrentElementLayout.Vertical,
            )
            Spacer(modifier = Modifier.height(8.dp))
            JunctionBoxes(
                boxes = parserStage.partialBoxes,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ParsedContent(
    parserStage: ParserStage.Parsed,
    modifier: Modifier = Modifier
) {
    JunctionBoxes(
        boxes = parserStage.junctionBoxes.boxes,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun JunctionBoxes(
    boxes: PersistentList<Point3D<Long>>,
    modifier: Modifier = Modifier
) {
    AutoScrollingTitledList(
        items = boxes,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(4),
        modifier = modifier,
        title = { Text("Junction Boxes") },
        itemContent = {
            Text(
                text = "[${it.x.leadingSpaces(5)}, ${it.y.leadingSpaces(5)}, ${it.z.leadingSpaces(5)}]",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                maxLines = 1,
            )
        }
    )
}


@Composable
private fun Solver1Section(
    solverStage: SolverStage1,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val junctionBoxes = (solverStage as? SolverStage1.Solving)?.junctionBoxes
            ?: (solverStage as? SolverStage1.Solved)?.junctionBoxes
        val circuits = (solverStage as? SolverStage1.Solving)?.circuits
            ?: (solverStage as? SolverStage1.Solved)?.circuits
            ?: persistentMapOf()
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        junctionBoxes?.run {
            JunctionBoxesSpace(
                junctionBoxes = junctionBoxes,
                circuits = circuits,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z
    fun length(): Float = sqrt(dot(this))
    fun normalized(): Vec3 {
        val len = length()
        require(len > 1e-8f) { "Cannot normalize near-zero vector" }
        return this * (1f / len)
    }

    fun cross(o: Vec3): Vec3 =
        Vec3(
            y * o.z - z * o.y,
            z * o.x - x * o.z,
            x * o.y - y * o.x
        )

    companion object {
        operator fun Float.times(o: Vec3): Vec3 =
            Vec3(this * o.x, this * o.y, this * o.z)
    }
}

data class Vec4(val x: Float, val y: Float, val z: Float, val w: Float)

class Mat4 {
    // row-major storage: m[row*4 + col]
    private val m = FloatArray(16)

    operator fun get(r: Int, c: Int): Float = m[r * 4 + c]
    operator fun set(r: Int, c: Int, v: Float) {
        m[r * 4 + c] = v
    }

    operator fun times(v: Vec4): Vec4 {
        // Column vector: out = M * v
        val x = this[0, 0] * v.x + this[0, 1] * v.y + this[0, 2] * v.z + this[0, 3] * v.w
        val y = this[1, 0] * v.x + this[1, 1] * v.y + this[1, 2] * v.z + this[1, 3] * v.w
        val z = this[2, 0] * v.x + this[2, 1] * v.y + this[2, 2] * v.z + this[2, 3] * v.w
        val w = this[3, 0] * v.x + this[3, 1] * v.y + this[3, 2] * v.z + this[3, 3] * v.w
        return Vec4(x, y, z, w)
    }

    operator fun times(o: Mat4): Mat4 {
        val r = Mat4()
        for (row in 0..3) for (col in 0..3) {
            var sum = 0f
            for (k in 0..3) sum += this[row, k] * o[k, col]
            r[row, col] = sum
        }
        return r
    }

    companion object {
        fun identity(): Mat4 = Mat4().apply { for (i in 0..3) this[i, i] = 1f }
        fun zero(): Mat4 = Mat4()
    }
}

fun lookAt(eye: Vec3, target: Vec3, up: Vec3): Mat4 {
    val f = (target - eye).normalized()
    val r = f.cross(up).normalized()
    val u = r.cross(f)

    // NOTE: translation goes in LAST COLUMN for column-vector math.
    val m = Mat4.identity()

    m[0, 0] = r.x; m[0, 1] = r.y; m[0, 2] = r.z; m[0, 3] = -r.dot(eye)
    m[1, 0] = u.x; m[1, 1] = u.y; m[1, 2] = u.z; m[1, 3] = -u.dot(eye)
    m[2, 0] = -f.x; m[2, 1] = -f.y; m[2, 2] = -f.z; m[2, 3] = f.dot(eye)
    m[3, 0] = 0f; m[3, 1] = 0f; m[3, 2] = 0f; m[3, 3] = 1f

    return m
}

fun frustumMatrix(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 {
    require(near > 0f) { "near must be > 0" }
    require(far > near) { "far must be > near" }
    require(right != left) { "right must != left" }
    require(top != bottom) { "top must != bottom" }

    val m = Mat4.zero()

    m[0, 0] = (2f * near) / (right - left)
    m[1, 1] = (2f * near) / (top - bottom)

    m[0, 2] = (right + left) / (right - left)
    m[1, 2] = (top + bottom) / (top - bottom)

    m[2, 2] = -(far + near) / (far - near)
    m[2, 3] = -(2f * far * near) / (far - near)

    m[3, 2] = -1f
    // m[3,3] = 0 by default

    return m
}

fun Point3D<Double>.toVec3(): Vec3 =
    Vec3(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())

data class Projection(
    val x: Float,
    val y: Float,
    val depth: Float,
)

fun projectWorldToScreen(world: Vec3, view: Mat4, proj: Mat4, viewport: Size): Projection {
    val p = Vec4(world.x, world.y, world.z, 1f)
    val viewPos = view * p
    val clip = proj * viewPos

    require(clip.w != 0f) { "Perspective divide failed: w == 0. Likely matrix convention mismatch." }

    val ndc = Vec3(clip.x / clip.w, clip.y / clip.w, clip.z / clip.w)

    val xPx = (ndc.x + 1f) * 0.5f * viewport.width
    val y01 = (ndc.y + 1f) * 0.5f
    val yPx = y01 * viewport.height

    return Projection(xPx, yPx, ndc.z * 0.5f + 0.5f)
}

@Composable
private fun JunctionBoxesSpace(
    junctionBoxes: JunctionBoxes,
    circuits: PersistentMap<Point3D<Long>, PersistentList<Point3D<Long>>>,
    modifier: Modifier = Modifier
) {
    val time by produceState(0f) {
        var initialTime = 0L
        withInfiniteAnimationFrameMillis {
            if (initialTime == 0L) initialTime = it
        }
        while (isActive) {
            withInfiniteAnimationFrameMillis {
                value = (it - initialTime) / 1000f
            }
        }
    }

    Canvas(
        modifier = modifier
            .background(color = Color.Black, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.small)
            .graphicsLayer(
                compositingStrategy = CompositingStrategy.Offscreen,
            )
    ) {
        val maxDistance = junctionBoxes.minPoint.distance(junctionBoxes.maxPoint).toFloat()
        val target = junctionBoxes.center.toVec3()
        val eye = target + 2f * maxDistance * Vec3(sin(0.2f * time), 0.0f, cos(0.2f * time))
        val up = Vec3(0.0f, 1.0f, 0.0f)

        val view = lookAt(eye, target, up)
        val aspect = size.width / size.height

        val near = 1.5f * maxDistance - 50000f
        val far = 2.5f * maxDistance
        val proj = frustumMatrix(-50000f * aspect, 50000f * aspect, -50000f, 50000f, near, far)

        val projections = junctionBoxes.boxes
            .map { Vec3(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
            .map { projectWorldToScreen(it, view, proj, size) }
            .sortedByDescending { it.depth }

        projections.forEach { proj ->
            drawCircle(
                color = Color(red = 1f - proj.depth, green = 1f - proj.depth, blue = 1f - proj.depth),
                radius = 3f * density / proj.depth,
                center = Offset(proj.x, proj.y),
            )
        }

        circuits.forEach { (_, circuit) ->
            val projections = circuit
                .map { Vec3(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
                .map { projectWorldToScreen(it, view, proj, size) }

            (1..projections.indices.last).forEach { endIdx ->
                val startIdx = endIdx - 1
                val start = projections[startIdx]
                val end = projections[endIdx]
                drawLine(
                    color = Color(red = 0.75f, green = 0.75f, blue = 0.75f),
                    start = Offset(start.x, start.y),
                    end = Offset(end.x, end.y),
                )
            }
        }
    }
}

@Composable
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val junctionBoxes = (solverStage as? SolverStage2.Solving)?.junctionBoxes
            ?: (solverStage as? SolverStage2.Solved)?.junctionBoxes
        val circuits = (solverStage as? SolverStage2.Solving)?.circuits
            ?: (solverStage as? SolverStage2.Solved)?.circuits
            ?: persistentMapOf()
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        junctionBoxes?.run {
            JunctionBoxesSpace(
                junctionBoxes = junctionBoxes,
                circuits = circuits,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

