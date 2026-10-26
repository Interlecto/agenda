package app.ilecto.agenda.core.data.mapper

import app.ilecto.agenda.core.database.entity.CalendarAccountEntity
import app.ilecto.agenda.core.database.entity.CalendarEventEntity
import app.ilecto.agenda.core.model.CalendarAccount
import app.ilecto.agenda.core.model.CalendarEvent

fun CalendarAccountEntity.toDomain() = CalendarAccount(
    id = id,
    provider = provider,
    displayName = displayName,
    emailAddress = emailAddress,
    isCorporate = isCorporate,
    colorArgb = colorArgb,
)

fun CalendarAccount.toEntity() = CalendarAccountEntity(
    id = id,
    provider = provider,
    displayName = displayName,
    emailAddress = emailAddress,
    isCorporate = isCorporate,
    colorArgb = colorArgb,
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
)
