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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.usecases.ImportVaultResult
import proton.android.pass.data.api.usecases.defaultvault.VaultWithFolder
import proton.android.pass.data.fakes.usecases.FakeCreateItem
import proton.android.pass.data.fakes.usecases.FakeObserveDefaultVault
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ExtractedItem
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class ImportVaultUseCaseImplTest {

    private lateinit var createItem: FakeCreateItem
    private lateinit var observeDefaultVault: FakeObserveDefaultVault
    private lateinit var useCase: ImportVaultUseCaseImpl

    @Before
    fun setup() {
        createItem = FakeCreateItem().apply {
            sendItem(Result.success(ItemTestFactory.createLogin()))
        }
        observeDefaultVault = FakeObserveDefaultVault()
        useCase = ImportVaultUseCaseImpl(
            createItem = createItem,
            observeDefaultVault = observeDefaultVault,
            encryptionContextProvider = FakeEncryptionContextProvider()
        )
    }

    @Test
    fun `imports extracted entries into default vault and emits progress`() = runTest {
        val shareId = ShareId("default-share")
        val folderId = FolderId("default-folder")
        emitDefaultVault(shareId, folderId)
        val entries = listOf(
            extractedItem(
                uuid = "first",
                title = "First entry",
                note = "First note",
                username = "first-user",
                password = "first-password",
                urls = listOf("https://first.example")
            ),
            extractedItem(
                uuid = "second",
                title = "Second entry",
                note = "Second note",
                username = "second-user",
                password = "",
                urls = listOf("https://second.example", "https://alternate.example")
            )
        )

        val events = useCase(entries).toList()

        assertThat(events.first()).isEqualTo(ImportVaultResult.Started)
        assertThat(events.filterIsInstance<ImportVaultResult.Uploading>())
            .containsExactly(
                ImportVaultResult.Uploading("first"),
                ImportVaultResult.Uploading("second")
            ).inOrder()
        assertThat(events.filterIsInstance<ImportVaultResult.ItemImported>())
            .containsExactly(
                ImportVaultResult.ItemImported("first"),
                ImportVaultResult.ItemImported("second")
            )
        assertThat(events.last()).isEqualTo(ImportVaultResult.Imported)
        assertThat(createItem.memory()).hasSize(2)
        assertThat(createItem.memory().map { it.shareId }).containsExactly(shareId, shareId)
        assertThat(createItem.memory().map { it.folderId }).containsExactly(folderId, folderId)

        val importedContents = createItem.memory().map { it.itemContents as ItemContents.Login }
        assertThat(importedContents.map { it.title }).containsExactly("First entry", "Second entry")
        assertThat(importedContents.map { it.note }).containsExactly("First note", "Second note")
        assertThat(importedContents.map { it.itemUsername }).containsExactly("first-user", "second-user")
        assertThat(importedContents[0].password).isEqualTo(
            HiddenState.Revealed(
                encrypted = FakeEncryptionContext.encrypt("first-password"),
                clearText = "first-password"
            )
        )
        assertThat(importedContents[1].password).isEqualTo(
            HiddenState.Empty(FakeEncryptionContext.encrypt(""))
        )
        assertThat(importedContents.map { it.urls }).containsExactly(
            listOf("https://first.example"),
            listOf("https://second.example", "https://alternate.example")
        )
        assertThat(importedContents.map { it.autofillUrls.map { url -> url.url } }).containsExactly(
            listOf("https://first.example"),
            listOf("https://second.example", "https://alternate.example")
        )
        assertThat(importedContents.flatMap { it.autofillUrls }.map { it.mode })
            .containsExactly(AutofillUrlMode.Default, AutofillUrlMode.Default, AutofillUrlMode.Default)
    }

    @Test
    fun `empty entries emit started and imported without creating items`() = runTest {
        emitDefaultVault()

        val events = useCase(emptyList()).toList()

        assertThat(events).containsExactly(
            ImportVaultResult.Started,
            ImportVaultResult.Imported
        ).inOrder()
        assertThat(createItem.memory()).isEmpty()
    }

    private fun emitDefaultVault(
        shareId: ShareId = ShareId("default-share"),
        folderId: FolderId = FolderId("default-folder")
    ) {
        observeDefaultVault.emitValue(
            Some(
                VaultWithFolder(
                    vault = VaultWithItemCount(
                        vault = VaultTestFactory.create(shareId = shareId),
                        activeItemCount = 0,
                        trashedItemCount = 0
                    ),
                    folderId = Some(folderId)
                )
            )
        )
    }

    private fun extractedItem(
        uuid: String,
        title: String,
        note: String,
        username: String,
        password: String,
        urls: List<String>
    ) = ExtractedItem(
        uuid = uuid,
        title = title,
        note = note,
        username = username,
        encryptedPassword = if (password.isEmpty()) {
            HiddenState.Empty(FakeEncryptionContext.encrypt(password))
        } else {
            HiddenState.Revealed(FakeEncryptionContext.encrypt(password), password)
        },
        urls = urls
    )
}
