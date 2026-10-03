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

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.features.importation.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun PasswordVisibilityButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    val iconRes = if (isVisible) {
        CoreR.drawable.ic_proton_eye
    } else {
        CoreR.drawable.ic_proton_eye_slash
    }
    val iconDesc = if (isVisible) {
        stringResource(R.string.importation_action_conceal_password)
    } else {
        stringResource(R.string.importation_action_reveal_password)
    }

    CircleIconButton(
        backgroundColor = Color.Unspecified,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = iconDesc,
            tint = ProtonTheme.colors.iconNorm
        )
    }
}

@Preview
@Composable
private fun PasswordVisibilityButtonPreview() {
    PassTheme {
        PasswordVisibilityButton(
            isVisible = false,
            onClick = {}
        )
    }
}
