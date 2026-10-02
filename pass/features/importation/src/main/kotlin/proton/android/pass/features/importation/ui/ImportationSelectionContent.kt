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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.importation.R
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.ImportationUiState
import proton.android.pass.features.importation.presentation.SelectableEntry
import proton.android.pass.features.importation.presentation.SelectableGroup
import me.proton.core.presentation.R as CoreR

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

@Composable
fun SelectableGroupItem(
    group: SelectableGroup,
    onEvent: (ImportationUiEvent) -> Unit,
    depth: Int
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = (depth * 16).dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_folder),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
            Text(
                text = group.name,
                style = PassTheme.typography.body3Norm(),
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = group.isSelected,
                onCheckedChange = { isChecked ->
                    onEvent(ImportationUiEvent.OnToggleGroupSelection(group.uuid, isChecked))
                }
            )
        }

        group.groups.forEach { subGroup ->
            SelectableGroupItem(group = subGroup, onEvent = onEvent, depth = depth + 1)
        }

        group.entries.forEach { entry ->
            SelectableEntryItem(entry = entry, onEvent = onEvent, depth = depth + 1)
        }
    }
}

@Composable
fun SelectableEntryItem(
    entry: SelectableEntry,
    onEvent: (ImportationUiEvent) -> Unit,
    depth: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_key),
            contentDescription = null,
            tint = ProtonTheme.colors.iconNorm
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title.ifEmpty { "Untitled" },
                style = PassTheme.typography.body3Norm(),
                color = ProtonTheme.colors.textNorm
            )
            if (entry.userName.isNotEmpty()) {
                Text(
                    text = entry.userName,
                    style = PassTheme.typography.body3Norm(),
                    color = ProtonTheme.colors.textWeak
                )
            }
        }
        Checkbox(
            checked = entry.isSelected,
            onCheckedChange = { isChecked ->
                onEvent(ImportationUiEvent.OnToggleEntrySelection(entry.uuid, isChecked))
            }
        )
    }
}
