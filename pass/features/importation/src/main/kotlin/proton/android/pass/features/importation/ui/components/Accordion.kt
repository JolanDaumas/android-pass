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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon

@Stable
class AccordionState internal constructor() {
    private val expandedItems = mutableStateMapOf<String, Boolean>()

    fun isExpanded(key: String): Boolean = expandedItems[key] ?: false

    fun toggle(key: String) {
        expandedItems[key] = !isExpanded(key)
    }
}

@Composable
fun rememberAccordionState(): AccordionState = remember { AccordionState() }

@Composable
fun Accordion(
    state: AccordionState,
    key: String,
    isExpandable: Boolean,
    modifier: Modifier = Modifier,
    headerContent: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val expanded = state.isExpanded(key)
    val onToggle = { state.toggle(key) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isExpandable) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(vertical = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            if (isExpandable) {
                ExpandCollapseIcon(
                    expanded = expanded,
                    onClick = onToggle
                )
            } else {
                Box(modifier = Modifier.size(36.dp))
            }
            headerContent()
        }

        AnimatedVisibility(visible = isExpandable && expanded) {
            Column(content = content)
        }
    }
}
