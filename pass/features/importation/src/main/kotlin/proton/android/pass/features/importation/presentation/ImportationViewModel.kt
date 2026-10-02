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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.data.api.usecases.ImportVaultUseCase
import proton.android.pass.features.importation.R
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class ImportationViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
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
                        fileError = null
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

            is ImportationUiEvent.OnToggleGroupSelection -> {
                _state.update { state ->
                    val (newGroups, newEntries) = updateGroupAndEntrySelection(
                        state.selectableGroups,
                        state.selectableEntries,
                        event.uuid,
                        event.isSelected,
                        isGroup = true
                    )
                    state.copy(selectableGroups = newGroups, selectableEntries = newEntries)
                }
            }

            is ImportationUiEvent.OnToggleEntrySelection -> {
                _state.update { state ->
                    val (newGroups, newEntries) = updateGroupAndEntrySelection(
                        state.selectableGroups,
                        state.selectableEntries,
                        event.uuid,
                        event.isSelected,
                        isGroup = false
                    )
                    state.copy(selectableGroups = newGroups, selectableEntries = newEntries)
                }
            }

            ImportationUiEvent.OnSubmit -> {
                val uri = _state.value.selectedFileUri ?: return
                val password = _state.value.masterPassword
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, fileError = null, passwordError = null) }
                    val result = importVaultUseCase(java.net.URI(uri.toString()), password)
                    result.fold(
                        onSuccess = { vault ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    step = ImportationStep.SelectItems,
                                    vaultName = vault.name,
                                    selectableGroups = vault.toSelectableGroups(),
                                    selectableEntries = vault.toSelectableEntries()
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
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    // Final import action using selected items
                    _state.update { it.copy(isLoading = false) }
                }
            }

            ImportationUiEvent.OnBackStep -> {
                _state.update {
                    it.copy(step = ImportationStep.InputCredentials)
                }
            }
        }
    }

    private fun updateGroupAndEntrySelection(
        groups: List<SelectableGroup>,
        entries: List<SelectableEntry>,
        uuid: String,
        isSelected: Boolean,
        isGroup: Boolean
    ): Pair<List<SelectableGroup>, List<SelectableEntry>> {
        if (isGroup) {
            val newGroups = groups.map { group ->
                if (group.uuid == uuid) {
                    cascadeSelectGroup(group, isSelected)
                } else {
                    val (subGroups, subEntries) = updateGroupAndEntrySelection(group.groups, group.entries, uuid, isSelected, true)
                    group.copy(groups = subGroups, entries = subEntries)
                }
            }
            return newGroups to entries
        } else {
            val newEntries = entries.map { if (it.uuid == uuid) it.copy(isSelected = isSelected) else it }
            val newGroups = groups.map { group ->
                val (subGroups, subEntries) = updateGroupAndEntrySelection(group.groups, group.entries, uuid, isSelected, false)
                group.copy(groups = subGroups, entries = subEntries)
            }
            return newGroups to newEntries
        }
    }

    private fun cascadeSelectGroup(group: SelectableGroup, isSelected: Boolean): SelectableGroup {
        return group.copy(
            isSelected = isSelected,
            groups = group.groups.map { cascadeSelectGroup(it, isSelected) },
            entries = group.entries.map { it.copy(isSelected = isSelected) }
        )
    }
}
