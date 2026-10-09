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
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.data.api.repositories.VaultExtractionRepository
import proton.android.pass.domain.ExtractedVault
import java.net.URI

internal class ExtractVaultUseCaseImplTest {

    @Test
    fun `forwards uri and password and returns extracted vault`() = runTest {
        val uri = URI("content://test/vault")
        val password = "master-password"
        val expected = ExtractedVault(emptyList())
        val repository = RecordingVaultExtractionRepository(Result.success(expected))
        val useCase = ExtractVaultUseCaseImpl(repository)

        val result = useCase(uri, password)

        assertThat(repository.receivedUri).isEqualTo(uri)
        assertThat(repository.receivedPassword).isEqualTo(password)
        assertThat(result).isEqualTo(Result.success(expected))
    }

    @Test
    fun `returns repository failure`() = runTest {
        val failure = IllegalArgumentException("invalid database")
        val repository = RecordingVaultExtractionRepository(Result.failure(failure))
        val useCase = ExtractVaultUseCaseImpl(repository)

        val result = useCase(URI("content://test/vault"), "password")

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
    }

    private class RecordingVaultExtractionRepository(
        private val result: Result<ExtractedVault>
    ) : VaultExtractionRepository {
        var receivedUri: URI? = null
            private set
        var receivedPassword: String? = null
            private set

        override suspend fun extractVault(uri: URI, masterPassword: String): Result<ExtractedVault> {
            receivedUri = uri
            receivedPassword = masterPassword
            return result
        }
    }
}
