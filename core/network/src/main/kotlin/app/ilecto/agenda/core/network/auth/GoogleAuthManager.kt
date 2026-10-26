package app.ilecto.agenda.core.network.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import app.ilecto.agenda.core.common.AppResult
import app.ilecto.agenda.core.network.BuildConfig
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GoogleIdentity(val email: String, val displayName: String)

private val CALENDAR_READONLY_SCOPE = Scope("https://www.googleapis.com/auth/calendar.readonly")

/**
 * Wraps Credential Manager (identity/sign-in) and the Play Services Authorization API
 * (Calendar scope access) — two separate Google APIs with two separate OAuth client
 * registrations, see docs/SETUP_OAUTH.md.
 */
@Singleton
class GoogleAuthManager @Inject constructor() {

    /** Shows the system account picker and returns the chosen account's email/display name. */
    suspend fun signIn(context: Context): AppResult<GoogleIdentity> = try {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            AppResult.Success(
                GoogleIdentity(
                    email = idTokenCredential.id,
                    displayName = idTokenCredential.displayName ?: idTokenCredential.id,
                ),
            )
        } else {
            AppResult.Error(IllegalStateException("Unexpected credential type: ${credential.type}"))
        }
    } catch (e: GetCredentialException) {
        AppResult.Error(e)
    }

    /**
     * Requests the Calendar readonly scope for whichever Google account the device currently
     * has signed in. [launchConsent] is supplied by the UI layer (an Activity Result launcher)
     * and is only invoked when Google needs to show a consent screen
     * ([AuthorizationResult.hasResolution]); re-calling this later for an account that already
     * granted the scope resolves without it, so it also doubles as the token-refresh path.
     */
    suspend fun requestCalendarAccess(
        context: Context,
        launchConsent: suspend (IntentSenderRequest) -> Intent?,
    ): AppResult<String> = try {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(CALENDAR_READONLY_SCOPE))
            .build()
        var result = Identity.getAuthorizationClient(context).authorize(request).await()
        if (result.hasResolution()) {
            val pendingIntent = result.pendingIntent
                ?: return AppResult.Error(
                    IllegalStateException("Authorization needs a resolution but returned no PendingIntent"),
                )
            val data = launchConsent(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                ?: return AppResult.Error(IllegalStateException("Calendar access consent was not granted"))
            result = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
        }
        val token = result.accessToken
            ?: return AppResult.Error(IllegalStateException("Authorization succeeded but returned no access token"))
        AppResult.Success(token)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}

/** Bridges a Play Services [Task] into a suspend call without pulling in the kotlinx-coroutines-play-services artifact. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
