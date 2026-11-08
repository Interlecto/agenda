package app.ilecto.agenda.core.network.auth

import android.app.Activity
import android.content.Context
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.network.R
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Phase 2: the sync engine needs to create/update/delete the redacted "busy" blocks it mirrors
// onto a target Outlook calendar, so this supersedes the Phase 1 read-only scope outright
// (Calendars.ReadWrite is a superset — Graph has no separate "events-only write" scope the way
// Google does).
private val CALENDAR_READ_WRITE_SCOPE = arrayOf("Calendars.ReadWrite")

// Phase 3: a separate, opt-in grant — requested only when the user turns on mail scanning for
// an account, not bundled into the Calendar scope above (incremental authorization).
private val MAIL_READ_SCOPE = arrayOf("Mail.Read")

data class MsalIdentity(val homeAccountId: String, val username: String)

/**
 * Wraps MSAL's multi-account public client application — the same instance is used for both
 * personal Microsoft and work/school (Entra ID) accounts via the `common`-equivalent
 * `AzureADandPersonalMicrosoftAccount` authority configured in res/raw/msal_config.json.
 */
@Singleton
class OutlookAuthManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private var client: IMultipleAccountPublicClientApplication? = null

    private suspend fun client(): IMultipleAccountPublicClientApplication =
        client ?: withContext(Dispatchers.IO) {
            // MSAL's own factory is synchronous/blocking; always do this off the main thread.
            PublicClientApplication.createMultipleAccountPublicClientApplication(
                appContext,
                R.raw.msal_config,
            ).also { client = it }
        }

    suspend fun signIn(activity: Activity): AppResult<MsalIdentity> {
        val app = client()
        return when (
            val result = acquireTokenInteractive(app) {
                startAuthorizationFromActivity(activity).withScopes(CALENDAR_READ_WRITE_SCOPE.toList())
            }
        ) {
            is AppResult.Success ->
                AppResult.Success(MsalIdentity(homeAccountId = result.data.account.id, username = result.data.account.username))
            is AppResult.Error -> result
        }
    }

    suspend fun acquireTokenSilent(homeAccountId: String): AppResult<String> =
        acquireSilent(homeAccountId, CALENDAR_READ_WRITE_SCOPE.toList())

    /**
     * Phase 3: requests the Mail.Read scope for an *already-linked* account, independently of
     * [signIn] — see [MAIL_READ_SCOPE]'s doc comment. Shows a consent screen scoped to that one
     * account rather than a new account picker.
     */
    suspend fun requestMailAccess(activity: Activity, homeAccountId: String): AppResult<Unit> {
        val app = client()
        val account = withContext(Dispatchers.IO) { app.getAccount(homeAccountId) }
            ?: return AppResult.Error(IllegalStateException("Outlook account is no longer cached on this device; reconnect it"))
        return when (
            val result = acquireTokenInteractive(app) {
                startAuthorizationFromActivity(activity).forAccount(account).withScopes(MAIL_READ_SCOPE.toList())
            }
        ) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }
    }

    suspend fun acquireMailTokenSilent(homeAccountId: String): AppResult<String> =
        acquireSilent(homeAccountId, MAIL_READ_SCOPE.toList())

    private suspend fun acquireTokenInteractive(
        app: IMultipleAccountPublicClientApplication,
        configure: AcquireTokenParameters.Builder.() -> AcquireTokenParameters.Builder,
    ): AppResult<IAuthenticationResult> = try {
        val result = suspendCancellableCoroutine<IAuthenticationResult> { continuation ->
            val parameters = AcquireTokenParameters.Builder()
                .configure()
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult) {
                        continuation.resume(authenticationResult)
                    }

                    override fun onError(exception: MsalException) {
                        continuation.resumeWithException(exception)
                    }

                    override fun onCancel() {
                        continuation.resumeWithException(IllegalStateException("Sign-in was cancelled"))
                    }
                })
                .build()
            app.acquireToken(parameters)
        }
        AppResult.Success(result)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    private suspend fun acquireSilent(homeAccountId: String, scopes: List<String>): AppResult<String> = try {
        withContext(Dispatchers.IO) {
            val app = client()
            val account: IAccount? = app.getAccount(homeAccountId)
            if (account == null) {
                AppResult.Error(IllegalStateException("Outlook account is no longer cached on this device; reconnect it"))
            } else {
                val authority = app.configuration.defaultAuthority.authorityURL.toString()
                val parameters = AcquireTokenSilentParameters.Builder()
                    .forAccount(account)
                    .withScopes(scopes)
                    .fromAuthority(authority)
                    .build()
                val result = app.acquireTokenSilent(parameters)
                AppResult.Success(result.accessToken)
            }
        }
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    suspend fun removeAccount(homeAccountId: String): AppResult<Unit> = try {
        withContext(Dispatchers.IO) {
            val app = client()
            val account = app.getAccount(homeAccountId)
            if (account != null) app.removeAccount(account)
            AppResult.Success(Unit)
        }
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
