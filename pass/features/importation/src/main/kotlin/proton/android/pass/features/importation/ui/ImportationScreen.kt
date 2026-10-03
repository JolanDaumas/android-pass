/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.importation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.features.importation.navigation.ImportationNavDestination
import proton.android.pass.features.importation.presentation.ImportationStep
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImportationScreen(
    modifier: Modifier = Modifier,
    onNavigated: (ImportationNavDestination) -> Unit,
    viewModel: ImportationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val handleEvent: (ImportationUiEvent) -> Unit = { event ->
        when (event) {
            is ImportationUiEvent.OnFileSelected ->
                viewModel.onFileSelected(event.uri, event.fileName)
            is ImportationUiEvent.OnPasswordChange ->
                viewModel.onPasswordChange(event.password)
            is ImportationUiEvent.OnTogglePasswordVisibility ->
                viewModel.onTogglePasswordVisibility(event.isVisible)
            is ImportationUiEvent.OnToggleEntrySelection ->
                viewModel.onToggleEntrySelection(event.uuid, event.isSelected)
            ImportationUiEvent.OnSubmit -> viewModel.onSubmit()
            ImportationUiEvent.OnConfirmSelection -> viewModel.onConfirmSelection()
            ImportationUiEvent.OnBackStep -> viewModel.onBackStep()
        }
    }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
        confirmValueChange = { target ->
            target != ModalBottomSheetValue.Hidden || !state.isLoading
        }
    )

    LaunchedEffect(state.isLoading, state.step) {
        if (state.isLoading && state.step == ImportationStep.UploadEntries) {
            sheetState.show()
        } else if (sheetState.isVisible) {
            sheetState.hide()
        }
    }

    LaunchedEffect(state.isImportComplete) {
        if (state.isImportComplete) {
            onNavigated(ImportationNavDestination.ShowImportSuccess)
        }
    }

    PassModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (state.step == ImportationStep.UploadEntries) {
                ImportationProgressBottomSheetContent(state = state)
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }
        },
        content = {
            when (state.step) {
                ImportationStep.InputCredentials -> {
                    ImportationContent(
                        modifier = modifier,
                        state = state,
                        onEvent = handleEvent,
                        onBackClick = { onNavigated(ImportationNavDestination.CloseScreen) }
                    )
                }
                ImportationStep.SelectItems,
                ImportationStep.UploadEntries -> {
                    ImportationSelectionContent(
                        modifier = modifier,
                        state = state,
                        onEvent = handleEvent,
                        onBackClick = viewModel::onBackStep
                    )
                }
            }
        }
    )
}
