# Setting up OAuth credentials (Phase 1)

Agenda talks to Google Calendar and Microsoft Graph directly from the device — there is no
backend. Both require an app registration that only you can create (they're tied to your own
Google Cloud / Microsoft Entra accounts). This page is the exact steps; the app builds and runs
without these (you'll just get a sign-in error when you tap "Add Google/Outlook account" until
you fill them in).

Your debug keystore's signature (`~/.android/debug.keystore`, the default Android Studio/Gradle
debug key) is already resolved below, so you don't need to run `keytool` yourself:

- **SHA-1:** `EA:38:F3:95:3F:C1:99:C3:2A:6C:FC:EF:44:FE:D4:44:F7:BF:FD:D2`
- **MSAL redirect URI (debug):** `msauth://app.ilecto.agenda/6jjzlT%2FBmcMqbPzvRP7URPe%2F%2FdI%3D`

A release build later (Phase 7, once you sign with a real release key) will need its own SHA-1
and its own redirect URI / Google Android OAuth client — this page only covers the debug key you
already have.

## 1. Google Calendar

Google requires **two** OAuth client registrations in the same Cloud project for this flow —
one for identity sign-in (Credential Manager), one for Calendar scope access (Authorization
API). This is a genuine Google quirk, not a mistake: the client *type* each API expects differs.

1. Go to the [Google Cloud Console](https://console.cloud.google.com/) and create a project (or
   reuse one).
2. **APIs & Services → Library** → enable **Google Calendar API**.
3. **APIs & Services → Credentials → Create Credentials → OAuth client ID** →
   **Android** — package name `app.ilecto.agenda`, SHA-1 fingerprint the one above. This is
   what lets `AuthorizationClient` grant the Calendar scope; its client ID never appears in
   code or `local.properties` — Google resolves it from the package name + signature.
4. **Create Credentials → OAuth client ID** again → **Web application** this time (no
   redirect URIs needed). This is the one `GetGoogleIdOption.setServerClientId` needs for
   sign-in — Credential Manager always expects a Web-type client ID here, even for a pure
   Android app with no backend of its own.
5. You'll also need an **OAuth consent screen** configured (External is fine while testing;
   add your own Google account as a test user if the app is in "Testing" publishing status).
6. Copy the **Web** client's ID (looks like `1234567890-abc123.apps.googleusercontent.com`)
   into `local.properties`:
   ```
   GOOGLE_OAUTH_CLIENT_ID=1234567890-abc123.apps.googleusercontent.com
   ```

## 2. Microsoft Graph (Outlook — personal + corporate)

1. Go to [Microsoft Entra admin center](https://entra.microsoft.com/) →
   **Applications → App registrations → New registration**.
2. Name it (e.g. "Agenda"), and under **Supported account types** choose
   **Accounts in any organizational directory and personal Microsoft accounts** — this is what
   lets the same app sign in a personal Outlook.com account *and* a corporate/school account.
3. Under **Redirect URI**, pick platform **Android**... Entra's Android quickstart flow usually
   asks for package name + signature hash and derives the URI for you; it should match (or you
   can directly add) the custom-scheme redirect URI above:
   `msauth://app.ilecto.agenda/6jjzlT%2FBmcMqbPzvRP7URPe%2F%2FdI%3D`.
4. **API permissions → Add a permission → Microsoft Graph → Delegated permissions** → add
   `Calendars.Read`. Grant admin consent if your tenant requires it (personal/consumer sign-in
   doesn't need this step).
5. Copy the **Application (client) ID** from the registration's Overview page into
   `local.properties`:
   ```
   MSAL_CLIENT_ID=00001111-aaaa-2222-bbbb-3333cccc4444
   MSAL_REDIRECT_URI=msauth://app.ilecto.agenda/6jjzlT%2FBmcMqbPzvRP7URPe%2F%2FdI%3D
   ```
6. Copy `core/network/msal_config.json.example` to
   `core/network/src/main/res/raw/msal_config.json` (gitignored — it's created with placeholder
   values already so the app builds; this step is only needed again if you ever delete it) and
   replace `client_id`/`redirect_uri` with the same two values as above.

## 3. Rebuild

```bash
./gradlew assembleDebug
```

No other code changes are needed — `core:network`'s `BuildConfig` and `msal_config.json` are
read at runtime.
