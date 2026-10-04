/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI

internal class VaultExtractionRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var instance: VaultExtractionRepositoryImpl
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private var openInputStream: (URI) -> InputStream? = { error("Input stream opener is not configured") }

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        openInputStream = { error("Input stream opener is not configured") }
        instance = VaultExtractionRepositoryImpl(
            openInputStream = { uri -> openInputStream(uri) },
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `extracts KeePass database fixture`() = runTest {
        openInputStream = {
            requireNotNull(javaClass.getResourceAsStream(FIXTURE_RESOURCE))
        }
        val result = instance.extractVault(URI("content://test/vault"), FIXTURE_PASSWORD)

        assertThat(result.isSuccess).isTrue()
        assertThat(requireNotNull(result.getOrNull()).extractedItems.size).isGreaterThan(0)
    }

    @Test
    fun `returns failure when KeePass database password is wrong`() = runTest {
        openInputStream = {
            requireNotNull(javaClass.getResourceAsStream(FIXTURE_RESOURCE))
        }
        val result = instance.extractVault(URI("content://test/vault"), "wrong-password")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `returns failure when database file does not exist`() = runTest {
        val missingFile = File(temporaryFolder.root, "missing-vault.kdbx")
        assertThat(missingFile.exists()).isFalse()
        openInputStream = { uri -> File(uri).inputStream() }
        val result = instance.extractVault(missingFile.toURI(), FIXTURE_PASSWORD)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FileNotFoundException::class.java)
    }

    private companion object {
        const val FIXTURE_RESOURCE = "/fixtures/sample-vault.kdbx"
        const val FIXTURE_PASSWORD = "test"
    }
}
