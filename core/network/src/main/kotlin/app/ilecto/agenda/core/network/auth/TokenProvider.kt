package app.ilecto.agenda.core.network.auth

import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.model.CalendarProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves a short-lived API access token for a linked account's [app.ilecto.agenda.core.model.CalendarAccount.authRef].
 * Used only from a foreground refresh (Phase 1 has no background sync yet), so a [Context] is
 * always available if a provider needs it.
 */
interface TokenProvider {
    suspend fun getAccessToken(context: Context, authRef: String): AppResult<String>
}

@Singleton
class GoogleTokenProvider @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
) : TokenProvider {
    // No UI available during a foreground-but-not-interactive refresh: if the scope was
    // revoked and Google needs a fresh consent screen, surface that as a distinct error
    // instead of trying to launch UI from here (the Phase 1 "please reconnect" fallback).
    override suspend fun getAccessToken(context: Context, authRef: String): AppResult<String> =
        googleAuthManager.requestCalendarAccess(context) { null }
}

@Singleton
class OutlookTokenProvider @Inject constructor(
    private val outlookAuthManager: OutlookAuthManager,
) : TokenProvider {
    override suspend fun getAccessToken(context: Context, authRef: String): AppResult<String> =
        outlookAuthManager.acquireTokenSilent(homeAccountId = authRef)
}

@Singleton
class TokenProviderFactory @Inject constructor(
    private val googleTokenProvider: GoogleTokenProvider,
    private val outlookTokenProvider: OutlookTokenProvider,
) {
    fun forProvider(provider: CalendarProvider): TokenProvider = when (provider) {
        CalendarProvider.GOOGLE -> googleTokenProvider
        CalendarProvider.OUTLOOK -> outlookTokenProvider
    }
}
