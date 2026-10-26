package app.ilecto.agenda.core.model

/**
 * Phase 2: a rule that mirrors matching events from one calendar onto another as a
 * redacted "blocking" event (e.g. a personal medical appointment becomes a generic
 * "Busy — Medical" block on a linked corporate calendar).
 */
data class SyncRule(
    val id: String,
    val sourceAccountId: String,
    val sourceCalendarId: String,
    val targetAccountId: String,
    val targetCalendarId: String,
    val redactionTemplate: String,
    val keywordTrigger: String?,
    val enabled: Boolean,
)
