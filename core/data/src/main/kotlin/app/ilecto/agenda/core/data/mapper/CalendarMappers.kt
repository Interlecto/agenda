package app.ilecto.agenda.core.data.mapper

import app.ilecto.agenda.core.database.entity.AccountCalendarEntity
import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.database.entity.SyncRuleEntity
import app.ilecto.agenda.core.model.AccountCalendar
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarEvent
import app.ilecto.agenda.core.model.SyncRule

fun CalendarAccountEntity.toDomain() = CalendarAccount(
    id = id,
    provider = provider,
    displayName = displayName,
    emailAddress = emailAddress,
    isCorporate = isCorporate,
    colorArgb = colorArgb,
    authRef = authRef,
    mailScanEnabled = mailScanEnabled,
)

fun CalendarAccount.toEntity() = CalendarAccountEntity(
    id = id,
    provider = provider,
    displayName = displayName,
    emailAddress = emailAddress,
    isCorporate = isCorporate,
    colorArgb = colorArgb,
    authRef = authRef,
    mailScanEnabled = mailScanEnabled,
)

fun CalendarEventEntity.toDomain() = CalendarEvent(
    id = id,
    accountId = accountId,
    calendarId = calendarId,
    title = title,
    description = description,
    location = location,
    start = start,
    end = end,
    isAllDay = isAllDay,
    mirroredFromEventId = mirroredFromEventId,
    mirroredByRuleId = mirroredByRuleId,
)

fun SyncRuleEntity.toDomain() = SyncRule(
    id = id,
    sourceAccountId = sourceAccountId,
    sourceCalendarId = sourceCalendarId,
    targetAccountId = targetAccountId,
    targetCalendarId = targetCalendarId,
    redactionTemplate = redactionTemplate,
    keywordTrigger = keywordTrigger,
    enabled = enabled,
)

fun SyncRule.toEntity() = SyncRuleEntity(
    id = id,
    sourceAccountId = sourceAccountId,
    sourceCalendarId = sourceCalendarId,
    targetAccountId = targetAccountId,
    targetCalendarId = targetCalendarId,
    redactionTemplate = redactionTemplate,
    keywordTrigger = keywordTrigger,
    enabled = enabled,
)

fun AccountCalendarEntity.toDomain() = AccountCalendar(
    accountId = accountId,
    calendarId = calendarId,
    displayName = displayName,
)
