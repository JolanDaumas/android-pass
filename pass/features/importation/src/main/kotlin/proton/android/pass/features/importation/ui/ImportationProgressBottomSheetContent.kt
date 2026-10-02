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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiState
import proton.android.pass.features.importation.presentation.ImportProgressStatus

@Composable
fun ImportationProgressBottomSheetContent(state: ImportationUiState) {
    val completedCount = state.importProgressEntries.count { it.status == ImportProgressStatus.Imported }
    val totalCount = state.importProgressEntries.size

    Column(
        modifier = Modifier
            .bottomSheet(horizontalPadding = Spacing.medium)
            .fillMaxWidth()
            .padding(bottom = Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        CircularProgressIndicator(color = PassTheme.colors.interactionNormMajor2)
        Text(
            text = stringResource(R.string.importation_progress_title),
            style = PassTheme.typography.body3Bold(),
            color = ProtonTheme.colors.textNorm
        )
        Text(
            text = "$completedCount/$totalCount",
            style = PassTheme.typography.body3Bold(),
            color = ProtonTheme.colors.textWeak
        )
    }
}
