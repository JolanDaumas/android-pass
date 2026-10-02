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
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.SelectableEntry
import proton.android.pass.features.importation.presentation.SelectableGroup
import me.proton.core.presentation.R as CoreR

@Composable
fun SelectableGroupItem(
    modifier: Modifier = Modifier,
    group: SelectableGroup,
    onEvent: (ImportationUiEvent) -> Unit,
    accordionState: AccordionState,
    depth: Int = 0
) {
    val hasContent = group.groups.isNotEmpty() || group.entries.isNotEmpty()

    Accordion(
        modifier = modifier.fillMaxWidth().padding(start = (depth * 16).dp),
        state = accordionState,
        key = group.uuid,
        isExpandable = hasContent,
        headerContent = {
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
        },
        content = {
            group.groups.forEach { subGroup ->
                key(subGroup.uuid) {
                    SelectableGroupItem(
                        group = subGroup,
                        onEvent = onEvent,
                        accordionState = accordionState,
                        depth = depth + 1
                    )
                }
            }

            group.entries.forEach { entry ->
                SelectableEntryItem(
                    entry = entry,
                    onEvent = onEvent,
                    depth = depth + 1
                )
            }
        }
    )
}

@Preview
@Composable
private fun SelectableGroupItemPreview() {
    PassTheme {
        SelectableGroupItem(
            group = SelectableGroup(
                uuid = "1",
                name = "Personal",
                isSelected = true,
                entries = listOf(
                    SelectableEntry(
                        uuid = "2",
                        title = "GitHub",
                        userName = "user@proton.me"
                    )
                )
            ),
            onEvent = {},
            accordionState = rememberAccordionState()
        )
    }
}
