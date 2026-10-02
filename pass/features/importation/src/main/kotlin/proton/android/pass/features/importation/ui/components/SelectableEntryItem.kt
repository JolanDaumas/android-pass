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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.features.importation.presentation.ImportationUiEvent
import proton.android.pass.features.importation.presentation.SelectableEntry
import me.proton.core.presentation.R as CoreR

@Composable
fun SelectableEntryItem(
    modifier: Modifier = Modifier,
    entry: SelectableEntry,
    onEvent: (ImportationUiEvent) -> Unit,
    depth: Int = 0
) {
    Row(
        modifier = modifier
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
                onEvent(ImportationUiEvent.OnToggleEntrySelection(entry.uuid, isChecked))
            }
        )
    }
}

@Preview
@Composable
private fun SelectableEntryItemPreview() {
    PassTheme {
        SelectableEntryItem(
            entry = SelectableEntry(
                uuid = "1",
                title = "GitHub",
                userName = "user@proton.me",
                isSelected = true
            ),
            onEvent = {}
        )
    }
}
