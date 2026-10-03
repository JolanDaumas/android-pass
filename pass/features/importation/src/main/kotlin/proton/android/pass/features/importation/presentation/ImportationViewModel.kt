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

package proton.android.pass.features.importation.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.data.api.usecases.ExtractVaultUseCase
import proton.android.pass.data.api.usecases.ImportVaultResult
import proton.android.pass.data.api.usecases.ImportVaultUseCase
import proton.android.pass.features.importation.R
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class ImportationViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val extractVaultUseCase: ExtractVaultUseCase,
    private val importVaultUseCase: ImportVaultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ImportationUiState())
    val state: StateFlow<ImportationUiState> = _state.asStateFlow()

    fun onEvent(event: ImportationUiEvent) {
        when (event) {
            is ImportationUiEvent.OnFileSelected -> {
                _state.update {
                    it.copy(
                        selectedFileUri = event.uri,
                        selectedFileName = event.fileName,
                        fileError = null,
                        importedEntries = emptyList(),
                        selectableEntries = emptyList(),
                        importProgressEntries = emptyList()
                    )
                }
            }

            is ImportationUiEvent.OnPasswordChange -> {
                _state.update {
                    it.copy(
                        masterPassword = event.password,
                        passwordError = null
                    )
                }
            }

            is ImportationUiEvent.OnTogglePasswordVisibility -> {
                _state.update {
                    it.copy(isPasswordVisible = event.isVisible)
                }
            }

            is ImportationUiEvent.OnToggleEntrySelection -> {
                _state.update { state ->
                    state.copy(
                        selectableEntries = state.selectableEntries.map { entry ->
                            if (entry.uuid == event.uuid) entry.copy(isSelected = event.isSelected) else entry
                        }
                    )
                }
            }

            ImportationUiEvent.OnSubmit -> {
                if (_state.value.isLoading) return
                val uri = _state.value.selectedFileUri ?: return
                val password = _state.value.masterPassword
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            fileError = null,
                            passwordError = null,
                            importError = null
                        )
                    }
                    val result = extractVaultUseCase(URI(uri.toString()), password)
                    result.fold(
                        onSuccess = { entries ->
                            val extractedItems = entries.extractedItems
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    step = ImportationStep.SelectItems,
                                    importedEntries = extractedItems,
                                    selectableEntries = extractedItems.toSelectableEntryUiModels()
                                )
                            }
                        },
                        onFailure = { error ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    passwordError = error.localizedMessage
                                        ?: context.getString(R.string.importation_error_generic)
                                )
                            }
                        }
                    )
                }
            }

            ImportationUiEvent.OnConfirmSelection -> {
                val currentState = _state.value
                if (currentState.isLoading || !currentState.isSubmitEnabled) return
                val selectedEntryUuids = currentState.selectableEntries
                    .filter(SelectableEntryUiModel::isSelected)
                    .map(SelectableEntryUiModel::uuid)
                    .toSet()
                val entries = currentState.importedEntries.selectEntries(selectedEntryUuids)
                if (entries.isEmpty()) return
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            step = ImportationStep.UploadEntries,
                            isLoading = true,
                            importError = null,
                            importProgressEntries = entries.indices.map { index ->
                                val uuid = index.toString()
                                ImportProgressEntry(
                                    uuid = uuid
                                )
                            }
                        )
                    }

                    try {
                        var isImportComplete = false
                        importVaultUseCase(entries).collect { result ->
                            when (result) {
                                ImportVaultResult.Started -> Unit
                                is ImportVaultResult.Uploading ->
                                    updateImportProgress(result.uuid, ImportProgressStatus.Uploading)
                                is ImportVaultResult.ItemImported ->
                                    updateImportProgress(result.uuid, ImportProgressStatus.Imported)
                                ImportVaultResult.Imported -> {
                                    isImportComplete = true
                                    _state.update { state ->
                                        state.copy(
                                            isLoading = false,
                                            isImportComplete = true,
                                            importProgressEntries = state.importProgressEntries.map {
                                                it.copy(status = ImportProgressStatus.Imported)
                                            }
                                        )
                                    }
                                }
                                ImportVaultResult.Failed -> Unit
                            }
                        }
                        if (!isImportComplete) {
                            _state.update {
                                it.copy(
                                    step = ImportationStep.SelectItems,
                                    isLoading = false,
                                    importError = context.getString(R.string.importation_error_import),
                                    importProgressEntries = it.importProgressEntries.map { progressEntry ->
                                        if (progressEntry.status == ImportProgressStatus.Imported) {
                                            progressEntry
                                        } else {
                                            progressEntry.copy(status = ImportProgressStatus.Failed)
                                        }
                                    }
                                )
                            }
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _state.update {
                            it.copy(
                                step = ImportationStep.SelectItems,
                                isLoading = false,
                                importError = context.getString(R.string.importation_error_import),
                                importProgressEntries = it.importProgressEntries.map { progressEntry ->
                                    if (progressEntry.status == ImportProgressStatus.Imported) {
                                        progressEntry
                                    } else {
                                        progressEntry.copy(status = ImportProgressStatus.Failed)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            ImportationUiEvent.OnBackStep -> {
                if (_state.value.isLoading) return
                _state.update {
                    it.copy(
                        step = when (it.step) {
                            ImportationStep.InputCredentials -> ImportationStep.InputCredentials
                            ImportationStep.SelectItems -> ImportationStep.InputCredentials
                            ImportationStep.UploadEntries -> ImportationStep.SelectItems
                        }
                    )
                }
            }
        }
    }

    private fun updateImportProgress(uuid: String, status: ImportProgressStatus) {
        _state.update { state ->
            state.copy(
                importProgressEntries = state.importProgressEntries.map { entry ->
                    if (entry.uuid == uuid) entry.copy(status = status) else entry
                }
            )
        }
    }

}
