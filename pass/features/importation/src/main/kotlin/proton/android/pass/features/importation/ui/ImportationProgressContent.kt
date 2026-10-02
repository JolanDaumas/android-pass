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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.importation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiState
import proton.android.pass.features.importation.presentation.ImportProgressEntry
import proton.android.pass.features.importation.presentation.ImportProgressStatus
import me.proton.core.presentation.R as CoreR

@Composable
fun ImportationProgressContent(
    modifier: Modifier = Modifier,
    state: ImportationUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.importation_progress_title),
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

            state.vaultName?.let { name ->
                Text(
                    text = name,
                    style = PassTheme.typography.body3Bold(),
                    color = ProtonTheme.colors.textNorm
                )
            }

            state.importError?.let { error ->
                Text(
                    text = error,
                    style = PassTheme.typography.body3Norm(),
                    color = ProtonTheme.colors.notificationError
                )
            }

            Column(
                modifier = Modifier
                    .roundedContainerNorm()
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                state.importProgressEntries.forEach { entry ->
                    ImportProgressEntryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun ImportProgressEntryRow(entry: ImportProgressEntry) {
    var dotCount by remember(entry.uuid) { mutableIntStateOf(1) }
    val isUploading = entry.status == ImportProgressStatus.Uploading

    LaunchedEffect(isUploading) {
        if (isUploading) {
            while (true) {
                delay(400)
                dotCount = dotCount % 3 + 1
            }
        } else {
            dotCount = 1
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = PassTheme.typography.body3Norm(),
                color = ProtonTheme.colors.textNorm
            )
            if (entry.userName.isNotBlank()) {
                Text(
                    text = entry.userName,
                    style = PassTheme.typography.body3Norm(),
                    color = ProtonTheme.colors.textWeak
                )
            }
            when (entry.status) {
                ImportProgressStatus.Uploading -> Text(
                    text = stringResource(R.string.importation_progress_sending) + ".".repeat(dotCount),
                    style = PassTheme.typography.body3Norm(),
                    color = ProtonTheme.colors.textWeak
                )
                ImportProgressStatus.Failed -> Text(
                    text = stringResource(R.string.importation_progress_failed),
                    style = PassTheme.typography.body3Norm(),
                    color = ProtonTheme.colors.notificationError
                )
                ImportProgressStatus.Pending,
                ImportProgressStatus.Imported -> Unit
            }
        }

        when (entry.status) {
            ImportProgressStatus.Imported -> Icon(
                painter = painterResource(CoreR.drawable.ic_proton_checkmark),
                contentDescription = stringResource(R.string.importation_progress_sent),
                tint = PassTheme.colors.signalSuccess,
                modifier = Modifier.size(20.dp)
            )
            ImportProgressStatus.Failed -> Icon(
                painter = painterResource(CoreR.drawable.ic_proton_exclamation_circle_filled),
                contentDescription = stringResource(R.string.importation_progress_failed),
                tint = ProtonTheme.colors.notificationError,
                modifier = Modifier.size(20.dp)
            )
            ImportProgressStatus.Pending,
            ImportProgressStatus.Uploading -> Spacer(modifier = Modifier.size(20.dp))
        }
    }
}
