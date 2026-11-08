package app.ilecto.agenda.core.model

import java.time.Instant

/**
 * A candidate event or deadline the Phase 3 mail scanner detected in a message's subject/snippet
 * via local heuristics only (see `core:data`'s `MailHeuristics`) — never added to a calendar
 * automatically. A suggestion only ever exists when the heuristic matched *both* a
 * confirmation-style keyword and a parseable date, so [start]/[end] are always present (a
 * message with a keyword but no detectable date never becomes a suggestion at all).
 */
data class MailSuggestion(
    val id: String,
    val accountId: String,
    val messageId: String,
    val title: String,
    val start: Instant,
    val end: Instant,
    val isAllDay: Boolean,
    val sourceSnippet: String,
    val status: SuggestionStatus,
    val detectedAt: Instant,
)

enum class SuggestionStatus { PENDING, ACCEPTED, DISMISSED }
