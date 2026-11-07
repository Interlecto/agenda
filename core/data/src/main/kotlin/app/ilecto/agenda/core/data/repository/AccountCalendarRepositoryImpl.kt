package app.ilecto.agenda.core.data.repository

import app.ilecto.agenda.core.data.mapper.toDomain
import app.ilecto.agenda.core.database.dao.AccountCalendarDao
import app.ilecto.agenda.core.domain.repository.AccountCalendarRepository
import app.ilecto.agenda.core.model.AccountCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountCalendarRepositoryImpl @Inject constructor(
    private val accountCalendarDao: AccountCalendarDao,
) : AccountCalendarRepository {

    override fun observeCalendarsForAccount(accountId: String): Flow<List<AccountCalendar>> =
        accountCalendarDao.observeForAccount(accountId).map { entities -> entities.map { it.toDomain() } }
}
