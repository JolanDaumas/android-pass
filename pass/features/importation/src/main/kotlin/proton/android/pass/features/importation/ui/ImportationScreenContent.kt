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
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportProgressEntry
import proton.android.pass.features.importation.presentation.ImportProgressStatus
import proton.android.pass.features.importation.presentation.ImportationStep
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationUiState
import proton.android.pass.features.importation.presentation.SelectableEntryUiModel

@Composable
fun ImportationScreenContent(
    modifier: Modifier = Modifier,
    state: ImportationUiState,
    onEvent: (ImportationUiEvent) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(
                    if (state.step.isInputStep) {
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
                            onEvent(
                                if (state.step.isInputStep) {
                                    ImportationUiEvent.OnSubmit
                                } else {
                                    ImportationUiEvent.OnConfirmSelection
                                }
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    if (state.step.isInputStep) {
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
                    if (state.step.isInputStep) {
                        onClose()
                    } else {
                        onEvent(ImportationUiEvent.OnBackStep)
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
                    selectedFileName = state.selectedFileName,
                    fileError = state.fileError,
                    masterPassword = state.masterPassword,
                    isPasswordVisible = state.isPasswordVisible,
                    isLoading = state.isLoading,
                    passwordError = state.passwordError,
                    isSubmitEnabled = state.isSubmitEnabled,
                    onEvent = onEvent
                )

                ImportationStep.SelectItems,
                ImportationStep.UploadEntries -> ImportationSelectionContent(
                    selectedFileName = state.selectedFileName,
                    importError = state.importError,
                    selectableEntries = state.selectableEntries,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Preview
@Composable
private fun ImportationScreenContentCredentialsPreview() {
    PassTheme {
        ImportationScreenContent(
            state = ImportationUiState.initial().copy(
                selectedFileName = "passwords_export.csv",
                masterPassword = "secret_password"
            ),
            onEvent = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ImportationScreenContentSelectionPreview() {
    PassTheme {
        ImportationScreenContent(
            state = ImportationUiState.initial().copy(
                step = ImportationStep.SelectItems,
                selectedFileName = "passwords_export.csv",
                selectableEntries = listOf(
                    SelectableEntryUiModel(
                        uuid = "1",
                        title = "Email",
                        userName = "user@example.com",
                        isSelected = true
                    ),
                    SelectableEntryUiModel(
                        uuid = "2",
                        title = "Bank",
                        userName = "user",
                        isSelected = false
                    )
                )
            ),
            onEvent = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ImportationScreenContentUploadPreview() {
    PassTheme {
        ImportationScreenContent(
            state = ImportationUiState.initial().copy(
                step = ImportationStep.UploadEntries,
                selectedFileName = "passwords_export.csv",
                isLoading = true,
                selectableEntries = listOf(
                    SelectableEntryUiModel(
                        uuid = "1",
                        title = "Email",
                        userName = "user@example.com",
                        isSelected = true
                    )
                ),
                importProgressEntries = listOf(
                    ImportProgressEntry(uuid = "1", status = ImportProgressStatus.Imported),
                    ImportProgressEntry(uuid = "2", status = ImportProgressStatus.Uploading),
                    ImportProgressEntry(uuid = "3", status = ImportProgressStatus.Pending)
                )
            ),
            onEvent = {},
            onClose = {}
        )
    }
}
