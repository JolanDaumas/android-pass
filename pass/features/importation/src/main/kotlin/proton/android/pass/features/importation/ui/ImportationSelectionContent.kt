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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationUiState
import proton.android.pass.features.importation.presentation.SelectableEntry
import proton.android.pass.features.importation.presentation.SelectableGroup
import proton.android.pass.features.importation.ui.components.SelectableEntryItem
import proton.android.pass.features.importation.ui.components.SelectableGroupItem

@Composable
fun ImportationSelectionContent(
    modifier: Modifier = Modifier,
    state: ImportationUiState,
    onEvent: (ImportationUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.importation_selection_title),
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(end = Spacing.small),
                        color = PassTheme.colors.interactionNormMajor2,
                        isLoading = state.isLoading,
                        buttonEnabled = state.isSubmitEnabled,
                        onClick = { onEvent(ImportationUiEvent.OnConfirmSelection) },
                        text = {
                            Text(
                                text = stringResource(R.string.importation_confirm_button),
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

            if (state.vaultName != null) {
                Text(
                    text = state.vaultName,
                    style = PassTheme.typography.heroNorm(),
                    color = ProtonTheme.colors.textNorm
                )
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
            }

            Column(
                modifier = Modifier
                    .roundedContainerNorm()
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                state.selectableGroups.forEach { group ->
                    SelectableGroupItem(group = group, onEvent = onEvent, depth = 0)
                }

                state.selectableEntries.forEach { entry ->
                    SelectableEntryItem(entry = entry, onEvent = onEvent, depth = 0)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ImportationSelectionContentPreview() {
    PassTheme {
        ImportationSelectionContent(
            state = ImportationUiState(
                vaultName = "My KeePass Vault",
                selectableGroups = listOf(
                    SelectableGroup(
                        uuid = "1",
                        name = "Social",
                        isSelected = true,
                        entries = listOf(
                            SelectableEntry(uuid = "2", title = "Twitter", userName = "user", isSelected = true)
                        )
                    )
                ),
                selectableEntries = listOf(
                    SelectableEntry(uuid = "3", title = "Bank", userName = "client", isSelected = true)
                )
            ),
            onEvent = {},
            onBackClick = {}
        )
    }
}
