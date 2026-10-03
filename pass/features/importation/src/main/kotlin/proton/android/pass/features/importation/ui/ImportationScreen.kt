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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
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
            }
        },
        content = {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    val isInputStep = state.step == ImportationStep.InputCredentials
                    BackArrowTopAppBar(
                        title = stringResource(
                            if (isInputStep) {
                                R.string.importation_title
                            } else {
                                R.string.importation_selection_title
                            }
                        ),
                        actions = {
                            LoadingCircleButton(
                                modifier = Modifier.padding(end = Spacing.small),
                                color = PassTheme.colors.interactionNormMajor2,
                                isLoading = state.isLoading,
                                buttonEnabled = state.isSubmitEnabled,
                                onClick = {
                                    handleEvent(
                                        if (isInputStep) {
                                            ImportationUiEvent.OnSubmit
                                        } else {
                                            ImportationUiEvent.OnConfirmSelection
                                        }
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            if (isInputStep) {
                                                R.string.importation_next_button
                                            } else {
                                                R.string.importation_confirm_button
                                            }
                                        ),
                                        style = PassTheme.typography.body3Bold(),
                                        color = PassTheme.colors.interactionNormMinor1
                                    )
                                }
                            )
                        },
                        onUpClick = {
                            if (isInputStep) {
                                onNavigated(ImportationNavDestination.CloseScreen)
                            } else {
                                viewModel.onBackStep()
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (state.step) {
                        ImportationStep.InputCredentials -> ImportationContent(
                            state = state,
                            onEvent = handleEvent
                        )
                        ImportationStep.SelectItems,
                        ImportationStep.UploadEntries -> ImportationSelectionContent(
                            state = state,
                            onEvent = handleEvent
                        )
                    }
                }
            }
        }
    )
}
