package dev.mmartos.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList

sealed class AutoScrollingTitledListLayout {
    data object ListTitled : AutoScrollingTitledListLayout()
    data class GridLayoutTitled(val columns: Int) : AutoScrollingTitledListLayout()
}

@Composable
fun <T> AutoScrollingTitledList(
    items: PersistentList<T>,
    layout: AutoScrollingTitledListLayout,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    itemContent: @Composable (T) -> Unit,
) {
    when (layout) {
        is AutoScrollingTitledListLayout.ListTitled ->
            AutoScrollingTitledList(
                title = title,
                items = items,
                modifier = modifier,
                itemContent = itemContent,
            )

        is AutoScrollingTitledListLayout.GridLayoutTitled ->
            AutoScrollingTitledGrid(
                title = title,
                items = items,
                columns = layout.columns,
                modifier = modifier,
                itemContent = itemContent,
            )

    }
}

@Composable
private fun <T> AutoScrollingTitledList(
    title: @Composable () -> Unit,
    items: PersistentList<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        title.invoke()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.small)
                .padding(8.dp)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
        LaunchedEffect(items.size) {
            lazyListState.scrollToItem(items.size)
        }
    }
}

@Composable
private fun <T> AutoScrollingTitledGrid(
    title: @Composable () -> Unit,
    items: PersistentList<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        title.invoke()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.small)
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = lazyGridState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
        LaunchedEffect(items.size) {
            if (items.isNotEmpty()) {
                lazyGridState.scrollToItem(items.size)
            }
        }
    }
}
