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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ImportVaultResult
import proton.android.pass.data.api.usecases.ImportVaultUseCase
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ItemContents
import javax.inject.Inject

class ImportVaultUseCaseImpl @Inject constructor(
    private val createItem: CreateItem,
    private val observeDefaultVault: ObserveDefaultVault
) : ImportVaultUseCase {

    override fun invoke(
        entries: List<ItemContents.Login>
    ): Flow<ImportVaultResult> = channelFlow {
        send(ImportVaultResult.Started)
        if (entries.isEmpty()) {
            send(ImportVaultResult.Failed)
            return@channelFlow
        }
        val defaultVault = observeDefaultVault().first().value()
            ?: error("No writable default vault is available")
        val folderId = defaultVault.folderId.value()

        val imports = entries.mapIndexed { index, itemContents ->
            val uuid = index.toString()
            val progress = index.toFloat() / entries.size
            send(ImportVaultResult.Uploading(uuid, progress))

            async {
                createItem(
                    shareId = defaultVault.shareId,
                    folderId = folderId,
                    itemContents = itemContents
                )
                send(ImportVaultResult.ItemImported(uuid, (index + 1f) / entries.size))
            }
        }
        imports.awaitAll()
        send(ImportVaultResult.Imported)
    }

}
