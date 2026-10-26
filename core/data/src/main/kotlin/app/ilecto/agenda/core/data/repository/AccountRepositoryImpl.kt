package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: CalendarAccountDao,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<CalendarAccount>> =
        accountDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun linkAccount(provider: CalendarProvider): AppResult<CalendarAccount> {
        // Phase 1 TODO: launch Credential Manager (Google) or MSAL (Outlook) sign-in,
        // then persist the resulting account via accountDao.upsert(...).
        return AppResult.Error(NotImplementedError("Account linking ships in Phase 1"))
    }

    override suspend fun unlinkAccount(accountId: String): AppResult<Unit> {
        accountDao.deleteById(accountId)
        return AppResult.Success(Unit)
    }
}
