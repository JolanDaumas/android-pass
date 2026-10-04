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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.pinning.PinItem
import proton.android.pass.features.importation.presentation.ImportationUiError
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.SelectableEntryUiModel
import proton.android.pass.features.importation.presentation.toPinItemUiModel

@Composable
fun ImportationSelectionContent(
    modifier: Modifier = Modifier,
    selectedFileName: String?,
    importError: ImportationUiError?,
    selectableEntries: List<SelectableEntryUiModel>,
    onEvent: (ImportationUiEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.medium),
        contentPadding = PaddingValues(
            top = Spacing.small,
            bottom = Spacing.medium
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        item(key = "header") {
            Column {
                if (selectedFileName != null) {
                    Text(
                        text = selectedFileName,
                        style = PassTheme.typography.heroNorm(),
                        color = ProtonTheme.colors.textNorm
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                }

                importError?.let { error ->
                    Text(
                        text = error.asText(),
                        style = PassTheme.typography.body3Norm(),
                        color = ProtonTheme.colors.notificationError
                    )
                }
            }
        }

        items(
            items = selectableEntries,
            key = { entry -> "entry:${entry.uuid}" }
        ) { entry ->
            PinItem(
                modifier = Modifier.fillMaxWidth(),
                item = entry.toPinItemUiModel(),
                canLoadExternalImages = false,
                onItemClick = {
                    onEvent(
                        ImportationUiEvent.OnToggleEntrySelection(
                            entry.uuid,
                            !entry.isSelected
                        )
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = Spacing.extraSmall),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = entry.title,
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
                            onEvent(
                                ImportationUiEvent.OnToggleEntrySelection(
                                    entry.uuid,
                                    isChecked
                                )
                            )
                        }
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun ImportationSelectionContentPreview() {
    PassTheme {
        ImportationSelectionContent(
            selectedFileName = "My KeePass Database",
            importError = null,
            selectableEntries = listOf(
                SelectableEntryUiModel(uuid = "2", title = "Twitter", userName = "user", isSelected = true),
                SelectableEntryUiModel(uuid = "3", title = "Bank", userName = "client", isSelected = true)
            ),
            onEvent = {}
        )
    }
}
