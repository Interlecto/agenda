package app.ilecto.agenda.core.domain.repository

import app.ilecto.agenda.core.model.AccountCalendar
import kotlinx.coroutines.flow.Flow

/**
 * The calendars within a linked account (cached from each refresh's `listCalendars()` call),
 * used to build the sync-rule editor's account+calendar pickers.
 */
interface AccountCalendarRepository {
    fun observeCalendarsForAccount(accountId: String): Flow<List<AccountCalendar>>
}
