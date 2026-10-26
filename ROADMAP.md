# Agenda Unificada — Roadmap

This expands the original product idea in `README.md` into phases, with a concrete "done when" for each
and the modules it touches. Phases are sequential but 3–6 are independently "desirable" — they can be
reordered based on what turns out to matter most once Phase 1 is in daily use.

## Architecture

**Layering** (each module only depends on the ones below it):

```
feature:*  ──depends on──>  core:domain, core:designsystem, core:common
core:data  ──implements──>  core:domain, using core:database + core:network
core:domain, core:database, core:network  ──depend on──>  core:model
app  ──wires everything together (Hilt graph, single Activity, root NavHost)
```

- **Single Activity.** `app` hosts one `MainActivity` with a single Compose `NavHost`; every screen
  (auth, calendar, planner, …) is a `@Composable` destination, not a Fragment or separate Activity.
- **Blocking/mirrored events.** When Phase 2 creates a redacted event on a target calendar, it tags it
  with a hidden extended property (a marker the remote APIs support) identifying it as agenda-managed.
  This lets the sync engine tell its own mirrored events apart from user-created ones, so edits/deletes
  on the source propagate without re-mirroring loops.
- **Auth.** Google accounts via Credential Manager + Google Calendar REST API. Outlook accounts
  (personal *and* corporate, same provider) via MSAL + Microsoft Graph API. Both providers support
  linking more than one account at once, per the README's requirement.
- **Toolchain baseline:** Kotlin 2.2.x, AGP 8.7.x, Gradle 8.13, compileSdk/targetSdk 36, minSdk 26.
  Pinned in `gradle/libs.versions.toml` — check for updates before serious Phase 1 work; versions drift
  fast and some (MSAL in particular) are marked `TODO verify`.

---

## Phase 0 — Project Foundation *(this commit)*

Multi-module skeleton: Compose + Hilt + Room + Navigation wired together, version catalog, git
initialized. No functional features yet — `feature:auth` and `feature:calendar` are navigable
placeholders; `feature:sync/mail/planner/activitylog/reports` are empty modules reserved for later
phases.

**Done when:** `./gradlew assembleDebug` succeeds and the app launches to a placeholder screen with
navigation between "Sign in" and "Calendar".

## Phase 1 — MVP: Unified Read-Only View

Sign in to at least one Google account and one Outlook account (including linking a personal *and* a
corporate Outlook account simultaneously); fetch events from each and cache them locally; show one
merged agenda/day/week view, color-coded per account; manage linked accounts (add / remove / re-auth).

**Modules:** `feature:auth`, `feature:calendar`, `core:network` (real Google Calendar + Graph API
clients), `core:database` (event cache), `core:data`, `core:domain`.

**Done when:** a user links ≥1 Google + ≥1 Outlook calendar and sees one merged schedule in the app.
*(This is the README's stated MVP.)*

## Phase 2 — Cross-Calendar Blocking / Sync *(MVP-desirable)*

Define a `SyncRule`: source account/calendar → target account/calendar, a redaction template (e.g.
"Busy — Medical"), and trigger conditions (keyword match, calendar-wide, etc.). A WorkManager-driven
background job creates/updates/deletes the corresponding marked event on the target calendar whenever
the source event changes, without ever re-mirroring its own mirrored events.

**Modules:** `feature:sync` (becomes real), `core:data`, `core:network`, `core:database`.

**Done when:** a personal "Doctor appointment" event automatically creates a generic "Busy — Medical"
out-of-office block on the linked corporate Outlook calendar, and stays in sync when edited or deleted.
*(This is the README's medical-appointment example.)*

## Phase 3 — Email-Derived Events *(desirable)*

Read-only access to Gmail / Outlook Mail. Local heuristics (dates, times, confirmation-style keywords)
flag candidate appointment/deadline emails; ambiguous cases can optionally go through an LLM-assisted
parse. Nothing is added automatically — detected items land in a review/suggestion inbox for the user
to accept or dismiss.

**Modules:** `feature:mail` (becomes real), `core:network` (mail scopes), `core:domain`.

**Done when:** an incoming flight/appointment confirmation email surfaces as an "Add to calendar?"
suggestion the user can accept in one tap.

## Phase 4 — Planner (Projects & Tasks) *(desirable)*

Local-first projects and tasks (Room), with due dates and priority. Tasks with due dates can optionally
be pushed (one-way) onto the unified calendar as events/reminders.

**Modules:** `feature:planner` (becomes real), `core:database`, `core:domain`.

**Done when:** a user creates a project, adds tasks with deadlines, and can optionally see those
deadlines on the unified calendar.

## Phase 5 — Activity Log *(desirable)*

Track what was actually done: completed tasks, attended events (manual check-off, or automatic for
past events the user didn't cancel), and free-form log entries. A simple timeline view.

**Modules:** `feature:activitylog` (becomes real), `core:database`.

**Done when:** a day's timeline shows attended appointments, completed tasks, and any manual entries
together.

## Phase 6 — Filtered Reports *(desirable)*

A date-range + account/project filtered report combining appointments, completed tasks, and activity-log
entries for a period, with CSV or share-sheet export.

**Modules:** `feature:reports` (becomes real), `core:domain`, `core:database`.

**Done when:** a user can ask "what did I do between these two dates" and get a filterable, exportable
answer.

## Phase 7 — Hardening & Release

Offline/error-state polish, accessibility pass, release signing + R8/ProGuard rules, GitHub Actions CI
(build + unit tests on every PR), Play Store listing.

## Phase 8 — iOS Port (Swift)

Re-express the same module/layer boundaries in Swift/SwiftUI once the Android app is stable and the
sync/data model has proven itself. Out of scope for now beyond the `ios/` placeholder — see
`ios/README.md`.
