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

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationUiState
import me.proton.core.presentation.R as CoreR

@Composable
fun ImportationContent(
    modifier: Modifier = Modifier,
    state: ImportationUiState,
    onEvent: (ImportationUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileName(context, uri)
            onEvent(ImportationUiEvent.OnFileSelected(uri, fileName))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.importation_title),
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(end = Spacing.small),
                        color = PassTheme.colors.interactionNormMajor2,
                        isLoading = state.isLoading,
                        buttonEnabled = state.isSubmitEnabled,
                        onClick = { onEvent(ImportationUiEvent.OnSubmit) },
                        text = {
                            Text(
                                text = stringResource(R.string.importation_next_button),
                                style = PassTheme.typography.body3Bold(),
                                color = PassTheme.colors.interactionNormMinor1
                            )
                        }
                    )
                },
                onUpClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(Spacing.small))

            // File selection field
            ProtonTextField(
                modifier = Modifier
                    .roundedContainerNorm()
                    .fillMaxWidth()
                    .clickable { pickFileLauncher.launch("*/*") }
                    .padding(
                        start = Spacing.none,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = state.selectedFileName ?: "",
                editable = false,
                textStyle = ProtonTheme.typography.defaultNorm,
                onChange = {},
                label = {
                    ProtonTextFieldLabel(
                        text = stringResource(R.string.importation_file_label),
                        isError = state.fileError != null
                    )
                },
                placeholder = {
                    ProtonTextFieldPlaceHolder(text = stringResource(R.string.importation_file_placeholder))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_file),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                },
                trailingIcon = {
                    CircleIconButton(
                        backgroundColor = Color.Unspecified,
                        onClick = { pickFileLauncher.launch("*/*") }
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_folder),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconNorm
                        )
                    }
                },
                isError = state.fileError != null,
                errorMessage = state.fileError.orEmpty()
            )

            // Master password input field
            ProtonTextField(
                modifier = Modifier
                    .roundedContainerNorm()
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.none,
                        top = Spacing.medium,
                        end = Spacing.extraSmall,
                        bottom = Spacing.medium
                    ),
                value = state.masterPassword,
                editable = !state.isLoading,
                textStyle = ProtonTheme.typography.defaultNorm(!state.isLoading),
                onChange = { onEvent(ImportationUiEvent.OnPasswordChange(it)) },
                label = {
                    ProtonTextFieldLabel(
                        text = stringResource(R.string.importation_master_password_label),
                        isError = state.passwordError != null
                    )
                },
                placeholder = {
                    ProtonTextFieldPlaceHolder(text = stringResource(R.string.importation_master_password_placeholder))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_lock),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                },
                trailingIcon = {
                    val iconRes = if (state.isPasswordVisible) {
                        CoreR.drawable.ic_proton_eye
                    } else {
                        CoreR.drawable.ic_proton_eye_slash
                    }
                    val iconDesc = if (state.isPasswordVisible) {
                        stringResource(R.string.importation_action_conceal_password)
                    } else {
                        stringResource(R.string.importation_action_reveal_password)
                    }
                    CircleIconButton(
                        backgroundColor = Color.Unspecified,
                        onClick = {
                            onEvent(ImportationUiEvent.OnTogglePasswordVisibility(!state.isPasswordVisible))
                        }
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = iconDesc,
                            tint = ProtonTheme.colors.iconNorm
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                onDoneClick = {
                    if (state.isSubmitEnabled) {
                        onEvent(ImportationUiEvent.OnSubmit)
                    }
                },
                isError = state.passwordError != null,
                errorMessage = state.passwordError.orEmpty()
            )
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) return cursor.getString(index)
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ImportationContentPreview() {
    PassTheme {
        ImportationContent(
            state = ImportationUiState(
                selectedFileName = "passwords_export.csv",
                masterPassword = "secret_password"
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
