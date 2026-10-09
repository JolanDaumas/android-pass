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

import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.VaultExtractionRepository
import proton.android.pass.domain.ExtractedItem
import proton.android.pass.domain.ExtractedVault
import proton.android.pass.domain.HiddenState
import java.io.InputStream
import java.net.URI
import javax.inject.Inject

class VaultExtractionRepositoryImpl @Inject constructor(
    private val openInputStream: @JvmSuppressWildcards (URI) -> InputStream?,
    private val encryptionContextProvider: EncryptionContextProvider
) : VaultExtractionRepository {
    override suspend fun extractVault(uri: URI, masterPassword: String): Result<ExtractedVault> =
        extractVaultFromSource(uri, masterPassword) { openInputStream(uri) }

    private suspend fun extractVaultFromSource(
        uri: URI,
        masterPassword: String,
        openInputStream: () -> InputStream?
    ): Result<ExtractedVault> = withContext(Dispatchers.IO) {
        runCatching {
            openInputStream()?.use { inputStream ->
                inputStream.toExtractedVault(masterPassword)
            } ?: throw IllegalStateException("Cannot open input stream for uri: $uri")
        }
    }

    private suspend fun InputStream.toExtractedVault(
        masterPassword: String
    ): ExtractedVault {
        val credentials = Credentials.from(EncryptedValue.fromString(masterPassword))
        val keePassDatabase = KeePassDatabase.decode(this, credentials)
        return encryptionContextProvider.withEncryptionContextSuspendable {
            ExtractedVault(keePassDatabase.toExtractedItems(::encrypt))
        }
    }

    private fun KeePassDatabase.toExtractedItems(
        encrypt: (String) -> EncryptedString
    ): List<ExtractedItem> =
        content.group.toExtractedItems(encrypt)

    private fun Group.toExtractedItems(
        encrypt: (String) -> EncryptedString
    ): List<ExtractedItem> =
        entries.map { it.toExtractedItem(encrypt) } + groups.flatMap { it.toExtractedItems(encrypt) }

    private fun Entry.toExtractedItem(encrypt: (String) -> EncryptedString): ExtractedItem {
        val password = fields.password?.content.orEmpty()
        val urls = fields.url?.content?.takeIf(String::isNotBlank)?.let(::listOf).orEmpty()
        return ExtractedItem(
            uuid = uuid.toString(),
            title = fields.title?.content.orEmpty(),
            note = fields.notes?.content.orEmpty(),
            username = fields.userName?.content.orEmpty(),
            encryptedPassword = if (password.isEmpty()) {
                HiddenState.Empty(encrypt(password))
            } else {
                HiddenState.Revealed(encrypt(password), password)
            },
            urls = urls
        )
    }
}
