package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.data.mapper.toEntity
import app.ilecto.agenda.core.database.dao.CalendarAccountDao
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.model.CalendarAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: CalendarAccountDao,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<CalendarAccount>> =
        accountDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveLinkedAccount(account: CalendarAccount): AppResult<Unit> = try {
        accountDao.upsert(account.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun unlinkAccount(accountId: String): AppResult<Unit> = try {
        accountDao.deleteById(accountId)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
