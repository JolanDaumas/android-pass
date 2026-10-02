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

package proton.android.pass.features.home.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.home.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ImportSuccessBottomSheetContent(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Column(
        modifier = modifier
            .bottomSheet(horizontalPadding = Spacing.medium)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_checkmark_circle),
            contentDescription = null,
            tint = PassTheme.colors.signalSuccess
        )
        Text(
            text = stringResource(R.string.home_import_success_title),
            style = PassTheme.typography.body3Bold(),
            color = ProtonTheme.colors.textNorm
        )
        Text(
            text = stringResource(R.string.home_import_success_message),
            style = PassTheme.typography.body3Norm(),
            color = ProtonTheme.colors.textWeak
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = PassTheme.colors.interactionNormMajor2,
                contentColor = PassTheme.colors.interactionNormMinor1
            )
        ) {
            Text(
                text = stringResource(R.string.home_import_success_close),
                style = PassTheme.typography.body3Bold()
            )
        }
    }
}
