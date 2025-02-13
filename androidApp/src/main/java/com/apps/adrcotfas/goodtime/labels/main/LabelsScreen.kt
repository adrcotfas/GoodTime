/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.labels.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.common.ConfirmationDialog
import com.apps.adrcotfas.goodtime.common.IconButtonWithBadge
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.main.AddEditLabelDest
import com.apps.adrcotfas.goodtime.ui.DraggableItem
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.ui.dragContainer
import com.apps.adrcotfas.goodtime.ui.rememberDragDropState
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Archive
import compose.icons.evaicons.outline.Plus
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(
    onNavigateToLabel: (Any) -> Unit,
    onNavigateToArchivedLabels: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LabelsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isLoading) return
    val labels = uiState.unarchivedLabels
    val activeLabelName = uiState.activeLabelName
    val defaultLabelName = stringResource(id = R.string.label_default)

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var labelToDelete by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val dragDropState =
        rememberDragDropState(listState) { fromIndex, toIndex ->
            viewModel.rearrangeLabel(fromIndex, toIndex)
        }

    val activeLabelIndex = labels.indexOfFirst { it.name == activeLabelName }
    if (labels.isNotEmpty()) {
        LaunchedEffect(Unit) {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let {
                if (activeLabelIndex > it) {
                    listState.scrollToItem(activeLabelIndex)
                }
            }
        }
    }

    val showFab = listState.isScrollingUp()

    Scaffold(
        topBar = {
            TopBar(
                title = "Labels",
                onNavigateBack = { onNavigateBack() },
                actions = {
                    ArchivedLabelsButton(uiState.archivedLabelCount) {
                        onNavigateToArchivedLabels()
                    }
                },
                showSeparator = listState.canScrollBackward,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                visible = showFab,
            ) {
                LargeFloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        onNavigateToLabel(AddEditLabelDest(name = ""))
                    },
                ) {
                    Icon(EvaIcons.Outline.Plus, "Localized description")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            itemsIndexed(
                labels,
                key = { _, item -> item.name },
            ) { index, label ->
                DraggableItem(dragDropState, index) { isDragging ->
                    LabelListItem(
                        label = label,
                        isDragging = isDragging,
                        dragModifier = Modifier.dragContainer(
                            dragDropState = dragDropState,
                            key = label.name,
                            onDragFinished = { viewModel.rearrangeLabelsToDisk() },
                        ),
                        onEdit = {
                            onNavigateToLabel(AddEditLabelDest(label.name))
                        },
                        onDuplicate = {
                            viewModel.duplicateLabel(
                                if (label.isDefault()) defaultLabelName else label.name,
                                label.isDefault(),
                            )
                        },
                        onArchive = { viewModel.setArchived(label.name, true) },
                        onDelete = {
                            labelToDelete = label.name
                            showDeleteConfirmationDialog = true
                        },
                    )
                }
            }
        }
        if (showDeleteConfirmationDialog) {
            ConfirmationDialog(
                title = "Delete $labelToDelete?",
                subtitle = "Deleting this label will remove it from associated completed sessions.",
                onConfirm = {
                    viewModel.deleteLabel(labelToDelete)
                    showDeleteConfirmationDialog = false
                },
                onDismiss = { showDeleteConfirmationDialog = false },
            )
        }
    }
}

@Composable
fun ArchivedLabelsButton(count: Int, onClick: () -> Unit) {
    IconButtonWithBadge(
        icon = {
            Icon(
                imageVector = EvaIcons.Outline.Archive,
                contentDescription = "Navigate to archived labels",
            )
        },
        showWhenNothingSelected = false,
        count = count,
        onClick = onClick,
    )
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
