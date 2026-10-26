package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.CalendarAccount
import kotlinx.coroutines.flow.Flow

/**
 * Manages linked calendar accounts (Phase 1 sign-in / account management). Interactive sign-in
 * itself (Credential Manager / MSAL, both Activity-bound) happens in feature:auth; this is
 * called only afterward, to persist the already-authorized account.
 */
interface AccountRepository {
    fun observeAccounts(): Flow<List<CalendarAccount>>

    suspend fun saveLinkedAccount(account: CalendarAccount): AppResult<Unit>

    suspend fun unlinkAccount(accountId: String): AppResult<Unit>
}
