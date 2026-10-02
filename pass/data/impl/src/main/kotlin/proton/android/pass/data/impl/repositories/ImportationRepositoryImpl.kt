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
import proton.android.pass.data.api.repositories.ImportationRepository
import proton.android.pass.domain.ImportedEntry
import proton.android.pass.domain.ImportedGroup
import proton.android.pass.domain.ImportedVault
import java.net.URI
import javax.inject.Inject

class ImportationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImportationRepository {
    override suspend fun importVault(uri: URI, masterPassword: String): Result<ImportedVault> = withContext(Dispatchers.IO) {
        runCatching {
            val contentUri = uri.toString().toUri()
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val credentials = Credentials.from(EncryptedValue.fromString(masterPassword))
                val keePassDatabase = KeePassDatabase.decode(inputStream, credentials)
                keePassDatabase.toImportedVault()
            } ?: throw IllegalStateException("Cannot open input stream for uri: $uri")
        }
    }
}

private fun KeePassDatabase.toImportedVault(): ImportedVault {
    val rootGroup = this.content.group
    return ImportedVault(
        name = rootGroup.name,
        groups = rootGroup.groups.map { it.toImportedGroup() },
        entries = rootGroup.entries.map { it.toImportedEntry() }
    )
}

private fun Group.toImportedGroup(): ImportedGroup {
    return ImportedGroup(
        uuid = uuid.toString(),
        name = name,
        groups = groups.map { it.toImportedGroup() },
        entries = entries.map { it.toImportedEntry() }
    )
}

private fun Entry.toImportedEntry(): ImportedEntry {
    return ImportedEntry(
        uuid = uuid.toString(),
        title = fields.title?.content.orEmpty(),
        userName = fields.userName?.content.orEmpty(),
        password = fields.password?.content.orEmpty(),
        url = fields.url?.content.orEmpty(),
        notes = fields.notes?.content.orEmpty()
    )
}
