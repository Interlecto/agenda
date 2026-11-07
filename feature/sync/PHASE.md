# Phase 2 — Cross-Calendar Blocking/Sync

Implemented — a rule editor (`SyncRulesScreen`) backed by `SyncRuleRepository`, and a
`SyncEngine` (`core:data`) that reconciles every enabled rule against the local calendar cache,
mirroring matching source events onto a target calendar as a redacted "busy" block via real
Google Calendar / Microsoft Graph write calls. Runs on pull-to-refresh and on a 15-minute
WorkManager background job (`SyncAgendaUseCase`). See the "Phase 2" section of
[ROADMAP.md](../../ROADMAP.md) for the full design, and the root `README.md`'s "Project status"
for what's untested.
