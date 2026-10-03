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
import androidx.compose.runtime.Immutable

@Immutable
data class ImportationUiState(
    val step: ImportationStep = ImportationStep.InputCredentials,
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val masterPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fileError: ImportationUiError? = null,
    val passwordError: ImportationUiError? = null,
    val importError: ImportationUiError? = null,
    val isImportComplete: Boolean = false,
    val selectableEntries: List<SelectableEntryUiModel> = emptyList(),
    val importProgressEntries: List<ImportProgressEntry> = emptyList()
) {
    val selectedEntryCount: Int
        get() = selectableEntries.count { it.isSelected }

    val isSubmitEnabled: Boolean
        get() = when (step) {
            ImportationStep.InputCredentials -> selectedFileUri != null && masterPassword.isNotBlank() && !isLoading
            ImportationStep.SelectItems -> selectedEntryCount > 0 && !isLoading
            ImportationStep.UploadEntries -> false
        }
}

sealed interface ImportationUiError {
    data object Generic : ImportationUiError
    data object ImportFailed : ImportationUiError
    data class Message(val value: String) : ImportationUiError
}

@Immutable
data class ImportProgressEntry(
    val uuid: String,
    val status: ImportProgressStatus = ImportProgressStatus.Pending
)

enum class ImportProgressStatus {
    Pending,
    Uploading,
    Imported,
    Failed
}
