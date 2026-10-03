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

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.ExtractedItem
import proton.android.pass.domain.HiddenState

fun List<ExtractedItem>.toUiModel(
    decrypt: (EncryptedString) -> String
): List<SelectableEntryUiModel> =
    map { extractedItem ->
        SelectableEntryUiModel(
            uuid = extractedItem.uuid,
            title = extractedItem.title,
            userName = extractedItem.username,
            note = extractedItem.note,
            encryptedPassword = extractedItem.encryptedPassword.toUiModel(decrypt),
            urls = extractedItem.urls
        )
    }

fun List<SelectableEntryUiModel>.toDomain(
    encrypt: (String) -> EncryptedString
): List<ExtractedItem> =
    map { entry ->
        ExtractedItem(
            uuid = entry.uuid,
            title = entry.title,
            note = entry.note,
            username = entry.userName,
            encryptedPassword = entry.encryptedPassword.toDomain(encrypt),
            urls = entry.urls
        )
    }

private fun HiddenState.toUiModel(decrypt: (EncryptedString) -> String): String =
    when (this) {
        is HiddenState.Empty -> ""
        is HiddenState.Concealed -> decrypt(encrypted)
        is HiddenState.Revealed -> clearText
    }

private fun String.toDomain(encrypt: (String) -> EncryptedString): HiddenState =
    if (isEmpty()) {
        HiddenState.Empty(encrypt(this))
    } else {
        HiddenState.Revealed(encrypt(this), this)
    }
