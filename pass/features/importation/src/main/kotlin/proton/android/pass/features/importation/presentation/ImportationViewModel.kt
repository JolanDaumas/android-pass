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

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ExtractVaultUseCase
import proton.android.pass.data.api.usecases.ImportVaultResult
import proton.android.pass.data.api.usecases.ImportVaultUseCase
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class ImportationViewModel @Inject constructor(
    private val extractVaultUseCase: ExtractVaultUseCase,
    private val importVaultUseCase: ImportVaultUseCase,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val formState = MutableStateFlow(FormState())
    private val importState = MutableStateFlow(ImportState())
    private val entriesState = MutableStateFlow(EntriesState())

    val state: StateFlow<ImportationUiState> = combine(
        formState,
        importState,
        entriesState
    ) { form, workflow, entries ->
        ImportationUiState(
            step = workflow.step,
            selectedFileUri = form.uri,
            selectedFileName = form.name,
            masterPassword = form.password,
            isPasswordVisible = form.isPasswordVisible,
            isLoading = workflow.isLoading,
            fileError = form.fileError,
            passwordError = form.passwordError,
            importError = workflow.error,
            isImportComplete = workflow.isComplete,
            selectableEntries = entries.selectable,
            importProgressEntries = entries.progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ImportationUiState()
    )

    fun onFileSelected(uri: Uri, fileName: String?) {
        formState.update {
            it.copy(
                uri = uri,
                name = fileName,
                fileError = null
            )
        }
        entriesState.update {
            it.copy(selectable = emptyList(), progress = emptyList())
        }
    }

    fun onPasswordChange(password: String) {
        formState.update {
            it.copy(
                password = password,
                passwordError = null
            )
        }
    }

    fun onTogglePasswordVisibility(isVisible: Boolean) {
        formState.update {
            it.copy(isPasswordVisible = isVisible)
        }
    }

    fun onToggleEntrySelection(uuid: String, isSelected: Boolean) {
        entriesState.update { state ->
            state.copy(
                selectable = state.selectable.map { entry ->
                    if (entry.uuid == uuid) entry.copy(isSelected = isSelected) else entry
                }
            )
        }
    }

    fun onSubmit() {
        if (importState.value.isLoading) return
        val uri = formState.value.uri ?: return
        val password = formState.value.password
        viewModelScope.launch {
            importState.update {
                it.copy(isLoading = true, error = null)
            }
            formState.update {
                it.copy(fileError = null, passwordError = null)
            }
            extractVaultUseCase(URI(uri.toString()), password)
                .fold(
                    onSuccess = { entries ->
                        val selectableEntries =
                            encryptionContextProvider.withEncryptionContext {
                                entries.extractedItems.toUiModel(::decrypt)
                            }

                        importState.update {
                            it.copy(isLoading = false, step = ImportationStep.SelectItems)
                        }
                        entriesState.update {
                            it.copy(selectable = selectableEntries)
                        }
                    },
                    onFailure = { error ->
                        importState.update { it.copy(isLoading = false) }
                        formState.update {
                            it.copy(
                                passwordError = error.localizedMessage
                                    ?.let(ImportationUiError::Message)
                                    ?: ImportationUiError.Generic
                            )
                        }
                    }
                )
        }
    }

    fun onConfirmSelection() {
        viewModelScope.launch {
            val entries = encryptionContextProvider.withEncryptionContext {
                entriesState.value.selectable
                    .filter(SelectableEntryUiModel::isSelected)
                    .toDomain(::encrypt)
            }

            importVaultUseCase(entries)
                .collect { result ->
                    when (result) {
                        ImportVaultResult.Started -> {
                            importState.update {
                                it.copy(
                                    step = ImportationStep.UploadEntries,
                                    isLoading = true,
                                    error = null
                                )
                            }
                            entriesState.update {
                                it.copy(
                                    progress = entries.map { entry ->
                                        ImportProgressEntry(
                                            uuid = entry.uuid
                                        )
                                    }
                                )
                            }
                        }

                        is ImportVaultResult.Uploading ->
                            updateImportProgress(
                                result.uuid,
                                ImportProgressStatus.Uploading
                            )

                        is ImportVaultResult.ItemImported ->
                            updateImportProgress(result.uuid, ImportProgressStatus.Imported)

                        ImportVaultResult.Imported -> {
                            importState.update {
                                it.copy(isLoading = false, isComplete = true)
                            }
                            entriesState.update { state ->
                                state.copy(
                                    progress = state.progress.map {
                                        it.copy(status = ImportProgressStatus.Imported)
                                    }
                                )
                            }
                        }

                        ImportVaultResult.Failed -> {
                            importState.update {
                                it.copy(
                                    step = ImportationStep.SelectItems,
                                    isLoading = false,
                                    error = ImportationUiError.ImportFailed
                                )
                            }
                            entriesState.update { state ->
                                state.copy(progress = state.progress.map { progressEntry ->
                                    if (progressEntry.status == ImportProgressStatus.Imported) {
                                        progressEntry
                                    } else {
                                        progressEntry.copy(status = ImportProgressStatus.Failed)
                                    }
                                })
                            }
                        }
                    }
                }
        }
    }

    fun onBackStep() {
        if (importState.value.isLoading) return
        importState.update {
            it.copy(
                step = when (it.step) {
                    ImportationStep.InputCredentials -> ImportationStep.InputCredentials
                    ImportationStep.SelectItems -> ImportationStep.InputCredentials
                    ImportationStep.UploadEntries -> ImportationStep.SelectItems
                }
            )
        }
    }

    private fun updateImportProgress(uuid: String, status: ImportProgressStatus) {
        entriesState.update { state ->
            state.copy(
                progress = state.progress.map { entry ->
                    if (entry.uuid == uuid) entry.copy(status = status) else entry
                }
            )
        }
    }

    private data class FormState(
        val uri: Uri? = null,
        val name: String? = null,
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val fileError: ImportationUiError? = null,
        val passwordError: ImportationUiError? = null
    )

    private data class ImportState(
        val step: ImportationStep = ImportationStep.InputCredentials,
        val isLoading: Boolean = false,
        val error: ImportationUiError? = null,
        val isComplete: Boolean = false
    )

    private data class EntriesState(
        val selectable: List<SelectableEntryUiModel> = emptyList(),
        val progress: List<ImportProgressEntry> = emptyList()
    )

}
