package app.ilecto.agenda.core.data.mapper

import app.ilecto.agenda.core.data.mail.MailHeuristics
import app.ilecto.agenda.core.database.entity.MailSuggestionEntity
import app.ilecto.agenda.core.model.MailSuggestion
import app.ilecto.agenda.core.model.SuggestionStatus
import app.ilecto.agenda.core.network.google.GmailMessageDto
import app.ilecto.agenda.core.network.outlook.GraphMessageDto
import java.time.Instant

/** Returns null when the heuristic doesn't find both a keyword and a parseable date. */
fun GmailMessageDto.toSuggestionOrNull(accountId: String, now: Instant): MailSuggestionEntity? {
    val subject = payload?.headers?.firstOrNull { it.name.equals("Subject", ignoreCase = true) }?.value.orEmpty()
    val preview = snippet.orEmpty()
    val match = MailHeuristics.match(subject, preview) ?: return null
    return buildSuggestionEntity(accountId, messageId = id, subject, preview, match, now)
}

/** Returns null when the heuristic doesn't find both a keyword and a parseable date. */
fun GraphMessageDto.toSuggestionOrNull(accountId: String, now: Instant): MailSuggestionEntity? {
    val subject = subject.orEmpty()
    val preview = bodyPreview.orEmpty()
    val match = MailHeuristics.match(subject, preview) ?: return null
    return buildSuggestionEntity(accountId, messageId = id, subject, preview, match, now)
}

private fun buildSuggestionEntity(
    accountId: String,
    messageId: String,
    subject: String,
    snippet: String,
    match: MailHeuristics.Match,
    now: Instant,
) = MailSuggestionEntity(
    id = "$accountId:$messageId",
    accountId = accountId,
    messageId = messageId,
    title = subject.ifBlank { "(no subject)" },
    start = match.start,
    end = match.end,
    isAllDay = match.isAllDay,
    sourceSnippet = snippet,
    status = SuggestionStatus.PENDING,
    detectedAt = now,
)

fun MailSuggestionEntity.toDomain() = MailSuggestion(
    id = id,
    accountId = accountId,
    messageId = messageId,
    title = title,
    start = start,
    end = end,
    isAllDay = isAllDay,
    sourceSnippet = sourceSnippet,
    status = status,
    detectedAt = detectedAt,
)
