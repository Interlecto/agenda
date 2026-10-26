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

private val CALENDAR_READ_SCOPE = arrayOf("Calendars.Read")

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

    suspend fun signIn(activity: Activity): AppResult<MsalIdentity> = try {
        val app = client()
        val result = suspendCancellableCoroutine<IAuthenticationResult> { continuation ->
            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(CALENDAR_READ_SCOPE.toList())
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
        AppResult.Success(MsalIdentity(homeAccountId = result.account.id, username = result.account.username))
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    suspend fun acquireTokenSilent(homeAccountId: String): AppResult<String> = try {
        withContext(Dispatchers.IO) {
            val app = client()
            val account: IAccount? = app.getAccount(homeAccountId)
            if (account == null) {
                AppResult.Error(IllegalStateException("Outlook account is no longer cached on this device; reconnect it"))
            } else {
                val authority = app.configuration.defaultAuthority.authorityURL.toString()
                val parameters = AcquireTokenSilentParameters.Builder()
                    .forAccount(account)
                    .withScopes(CALENDAR_READ_SCOPE.toList())
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
