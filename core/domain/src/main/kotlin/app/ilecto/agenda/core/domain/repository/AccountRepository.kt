package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarProvider
import kotlinx.coroutines.flow.Flow

/** Manages linked calendar accounts (Phase 1 sign-in / account management). */
interface AccountRepository {
    fun observeAccounts(): Flow<List<CalendarAccount>>

    suspend fun linkAccount(provider: CalendarProvider): AppResult<CalendarAccount>

    suspend fun unlinkAccount(accountId: String): AppResult<Unit>
}
