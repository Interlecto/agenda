package app.ilecto.agenda.core.domain.usecase

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.domain.mail.MailScanEngine
import javax.inject.Inject

/**
 * Phase 3: scans every mail-scan-enabled account for candidate appointment/deadline emails.
 * Used by both the mail screen's "Scan now" button and the background WorkManager job.
 */
class ScanMailUseCase @Inject constructor(
    private val mailScanEngine: MailScanEngine,
) {
    suspend operator fun invoke(context: Context): AppResult<Unit> = mailScanEngine.scanAll(context)
}
