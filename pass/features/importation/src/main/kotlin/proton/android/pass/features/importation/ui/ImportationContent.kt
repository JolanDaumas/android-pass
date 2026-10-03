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

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.importation.extensions.getFileName
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationUiState

@Composable
fun ImportationContent(
    modifier: Modifier = Modifier,
    state: ImportationUiState,
    onEvent: (ImportationUiEvent) -> Unit
) {
    val context = LocalContext.current
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.getFileName(context)
            onEvent(ImportationUiEvent.OnFileSelected(uri, fileName))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Spacer(modifier = Modifier.height(Spacing.small))

        ImportationFileField(
            state = state,
            onPickFile = { pickFileLauncher.launch("*/*") }
        )

        ImportationMasterPasswordField(
            state = state,
            onEvent = onEvent
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ImportationContentPreview() {
    PassTheme {
        ImportationContent(
            state = ImportationUiState.initial().copy(
                selectedFileName = "passwords_export.csv",
                masterPassword = "secret_password"
            ),
            onEvent = {}
        )
    }
}
