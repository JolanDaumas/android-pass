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

package proton.android.pass.features.importation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiError
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.ui.asText
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ImportationMasterPasswordField(
    masterPassword: String,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    passwordError: ImportationUiError?,
    isSubmitEnabled: Boolean,
    onEvent: (ImportationUiEvent) -> Unit
) {
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
        value = masterPassword,
        editable = !isLoading,
        textStyle = ProtonTheme.typography.defaultNorm(!isLoading),
        onChange = { onEvent(ImportationUiEvent.OnPasswordChange(it)) },
        label = {
            ProtonTextFieldLabel(
                text = stringResource(R.string.importation_master_password_label),
                isError = passwordError != null
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
            PasswordVisibilityButton(
                isVisible = isPasswordVisible,
                onClick = {
                    onEvent(ImportationUiEvent.OnTogglePasswordVisibility(!isPasswordVisible))
                }
            )
        },
        visualTransformation = if (isPasswordVisible) {
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
            if (isSubmitEnabled) {
                onEvent(ImportationUiEvent.OnSubmit)
            }
        },
        isError = passwordError != null,
        errorMessage = passwordError?.asText().orEmpty()
    )
}

@Preview
@Composable
private fun ImportationMasterPasswordFieldPreview() {
    PassTheme {
        ImportationMasterPasswordField(
            masterPassword = "secret_password",
            isPasswordVisible = false,
            isLoading = false,
            passwordError = null,
            isSubmitEnabled = true,
            onEvent = {}
        )
    }
}
