# Agenda Unificada

An app that connects to different calendar applications including Google Calendar and Outlook, and present a unified interface to the user. It should support corporate and personal calendars from one same provider (v.g. Outlook).

It should allow to either synchronize or send blocks between calendars, v.g. if Google personal Calendar has a medical apointment, it would mark in the corporate Outlook an out-of-office blocking event with a generic "medical" message, without details.

Desirable: read emails, recognize if they are about apointments, so that it can be uploaded to a calendar, recognize deadlines, and other priorities.

Desirable: a planer with projects and to do tasks

Desirable: keep a track of performed activities

Desirable: filtered reports showing apointments and performed activities and completed task for a period of time.

---

developed for Android (kotlin) and iOS (swift), with Android developing first.
android architecture: single activity, multiple fragments, hilt, room.

MVP, read at least one Google and one Ooutook calendar, and show a unified view.
MVP desirable: blocking.

---

## Project status

Phase 0 (project skeleton), Phase 1 (MVP: unified read-only view), Phase 2 (cross-calendar
blocking) and Phase 3 (email-derived events) are all implemented — Google (Credential Manager +
Play Services Authorization API) and Outlook (MSAL, personal + work/school accounts) sign-in,
real Google Calendar / Microsoft Graph API calls (read *and* write), a local Room cache, a
merged color-coded agenda view, a sync-rule editor that mirrors matching events onto another
calendar as a redacted "busy" block, and a mail-suggestions inbox that scans opted-in accounts
for candidate appointment/deadline emails via local, on-device heuristics (no NLP/LLM) and lets
you add a match to any linked calendar in one tap — but untested against real OAuth credentials:
see **[docs/SETUP_OAUTH.md](docs/SETUP_OAUTH.md)** to fill those in and actually try it. If you
linked a test account under Phase 1, it needs to be unlinked and relinked to pick up Phase 2's
wider scopes (see that doc's "If you already linked a test account" section); Phase 3's mail
scope is separate and opt-in per account, no relinking needed.

See **[ROADMAP.md](ROADMAP.md)** for the full phase-by-phase plan and architecture notes.

A landing page is live at **[agenda.ilecto.app](https://agenda.ilecto.app)** ([source](site/)).

## Project structure

```
app/                 single Activity, root NavHost, DI entry point
core/common/         dispatcher providers, Result wrapper, shared utils
core/model/          plain domain models (CalendarAccount, CalendarEvent, Task, …)
core/domain/         repository interfaces + use cases
core/database/       Room database, entities, DAOs
core/network/        Retrofit/OkHttp clients for Google Calendar + Microsoft Graph
core/data/           repository implementations wiring database + network
core/designsystem/   Compose theme + shared composables
feature/auth/        sign-in (Phase 1)
feature/calendar/    unified calendar view (Phase 1)
feature/sync/        cross-calendar blocking rule editor (Phase 2)
feature/mail/        email-derived event suggestions (Phase 3)
feature/planner/     projects & tasks (Phase 4, placeholder)
feature/activitylog/ activity tracking (Phase 5, placeholder)
feature/reports/     filtered reports (Phase 6, placeholder)
ios/                 iOS port placeholder (Phase 8)
```

## Getting started

1. Copy `local.properties.example` to `local.properties` and set `sdk.dir` to your Android SDK path.
   The app builds and runs without OAuth credentials filled in — see
   **[docs/SETUP_OAUTH.md](docs/SETUP_OAUTH.md)** for how to get a real Google/Outlook sign-in
   working, including the write scopes Phase 2's sync engine needs and the optional mail scopes
   Phase 3's suggestion inbox needs.
2. `./gradlew assembleDebug` to build, or open the project root in Android Studio.
3. Dependency versions in `gradle/libs.versions.toml` were checked live against Maven/Google
   metadata on 2026-10-26 (not just the assistant's training data) — still worth a refresh if
   you pick this back up much later.