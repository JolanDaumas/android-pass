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

package proton.android.pass.features.importation.presentation

import proton.android.pass.domain.ImportedEntry
import proton.android.pass.domain.ImportedGroup
import proton.android.pass.domain.ImportedVault

data class SelectableGroup(
    val uuid: String,
    val name: String,
    val isSelected: Boolean = true,
    val groups: List<SelectableGroup> = emptyList(),
    val entries: List<SelectableEntry> = emptyList()
)

data class SelectableEntry(
    val uuid: String,
    val title: String,
    val userName: String,
    val isSelected: Boolean = true
)

fun ImportedVault.toSelectableGroups(): List<SelectableGroup> {
    return groups.map { it.toSelectableGroup() }
}

fun ImportedVault.toSelectableEntries(): List<SelectableEntry> {
    return entries.map { it.toSelectableEntry() }
}

fun ImportedGroup.toSelectableGroup(): SelectableGroup {
    return SelectableGroup(
        uuid = uuid,
        name = name,
        isSelected = true,
        groups = groups.map { it.toSelectableGroup() },
        entries = entries.map { it.toSelectableEntry() }
    )
}

fun ImportedEntry.toSelectableEntry(): SelectableEntry {
    return SelectableEntry(
        uuid = uuid,
        title = title,
        userName = userName,
        isSelected = true
    )
}
