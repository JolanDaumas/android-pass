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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ImportSelectedPasswordsUseCase
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ImportedEntry
import proton.android.pass.domain.ItemContents
import javax.inject.Inject

class ImportSelectedPasswordsUseCaseImpl @Inject constructor(
    private val createItem: CreateItem,
    private val observeDefaultVault: ObserveDefaultVault,
    private val encryptionContextProvider: EncryptionContextProvider
) : ImportSelectedPasswordsUseCase {

    override suspend fun invoke(entries: List<ImportedEntry>): Result<Unit> = try {
        require(entries.isNotEmpty()) { "No password entries were selected" }
        val defaultVault = observeDefaultVault().first().value()
            ?: error("No writable default vault is available")

        encryptionContextProvider.withEncryptionContextSuspendable {
            entries.forEach { entry ->
                val password = if (entry.password.isEmpty()) {
                    HiddenState.Empty(encrypt(entry.password))
                } else {
                    HiddenState.Revealed(encrypt(entry.password), entry.password)
                }
                val urls = entry.url.takeIf(String::isNotBlank)?.let(::listOf).orEmpty()

                createItem(
                    shareId = defaultVault.shareId,
                    folderId = defaultVault.folderId.value(),
                    itemContents = ItemContents.Login(
                        title = entry.title,
                        note = entry.notes,
                        customFields = emptyList(),
                        itemEmail = "",
                        itemUsername = entry.userName,
                        password = password,
                        urls = urls,
                        packageInfoSet = emptySet(),
                        primaryTotp = HiddenState.Empty(encrypt("")),
                        passkeys = emptyList(),
                        autofillUrls = urls.map { url ->
                            AutofillUrl(url = url, mode = AutofillUrlMode.Default)
                        }
                    )
                )
            }
        }
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
