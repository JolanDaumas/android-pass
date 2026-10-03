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

package proton.android.pass.data.impl.repositories

import android.content.Context
import androidx.core.net.toUri
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Group
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.VaultExtractionRepository
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ExtractedVault
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import java.net.URI
import javax.inject.Inject

class VaultExtractionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : VaultExtractionRepository {
    override suspend fun extractVault(uri: URI, masterPassword: String): Result<ExtractedVault> = withContext(Dispatchers.IO) {
        runCatching {
            val contentUri = uri.toString().toUri()
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val credentials = Credentials.from(EncryptedValue.fromString(masterPassword))
                val keePassDatabase = KeePassDatabase.decode(inputStream, credentials)
                encryptionContextProvider.withEncryptionContextSuspendable {
                    ExtractedVault(keePassDatabase.toLoginItems(::encrypt))
                }
            } ?: throw IllegalStateException("Cannot open input stream for uri: $uri")
        }
    }
}

private fun KeePassDatabase.toLoginItems(
    encrypt: (String) -> EncryptedString
): List<ItemContents.Login> =
    content.group.toLoginItems(encrypt)

private fun Group.toLoginItems(
    encrypt: (String) -> EncryptedString
): List<ItemContents.Login> =
    entries.map { it.toLoginItem(encrypt) } + groups.flatMap { it.toLoginItems(encrypt) }

private fun Entry.toLoginItem(
    encrypt: (String) -> EncryptedString
): ItemContents.Login {
    val password = fields.password?.content.orEmpty()
    val urls = fields.url?.content?.takeIf(String::isNotBlank)?.let(::listOf).orEmpty()
    return ItemContents.Login(
        title = fields.title?.content.orEmpty(),
        note = fields.notes?.content.orEmpty(),
        customFields = emptyList(),
        itemEmail = "",
        itemUsername = fields.userName?.content.orEmpty(),
        password = if (password.isEmpty()) {
            HiddenState.Empty(encrypt(password))
        } else {
            HiddenState.Revealed(encrypt(password), password)
        },
        urls = urls,
        packageInfoSet = emptySet(),
        primaryTotp = HiddenState.Empty(encrypt("")),
        passkeys = emptyList(),
        autofillUrls = urls.map { url ->
            AutofillUrl(url = url, mode = AutofillUrlMode.Default)
        }
    )
}
