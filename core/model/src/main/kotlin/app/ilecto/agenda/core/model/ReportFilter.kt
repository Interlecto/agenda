package app.ilecto.agenda.core.model

import java.time.Instant

/** Phase 6: the filter criteria for a generated report. */
data class ReportFilter(
    val from: Instant,
    val to: Instant,
    val accountIds: Set<String> = emptySet(),
    val projectIds: Set<String> = emptySet(),
)
