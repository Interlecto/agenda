# Phase 3 — Email-Derived Events

Implemented — a mail-suggestions screen (`MailSuggestionsScreen`) with a per-account toggle that
requests a separate, opt-in `gmail.readonly`/`Mail.Read` grant, a `MailScanEngine` (`core:data`)
that scans a 2-day trailing window of recent messages via real Gmail/Microsoft Graph calls, and
local, on-device-only heuristics (`MailHeuristics`, no NLP/LLM) that flag candidate
appointment/deadline emails — nothing is ever added to a calendar without the user tapping "Add
to calendar" and picking a target account/calendar. Runs on "Scan now" and on the same
15-minute WorkManager job Phase 2 uses. See the "Phase 3" section of
[ROADMAP.md](../../ROADMAP.md) for the full design, and the root `README.md`'s "Project status"
for what's untested.
