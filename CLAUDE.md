# CLAUDE.md

Agenda Unificada: an Android app (Kotlin, Jetpack Compose) that connects to several calendar services (Google Calendar, Outlook) and shows one unified view. It can sync or send blocks between calendars, e.g. mark a personal appointment on a work calendar as a generic out-of-office event without details. See `README.md` for goals and `ROADMAP.md` for planned work.

## Layout

- `app/` — Android application module (Kotlin sources under `app/src/main/kotlin/app`).
- `core/`, `feature/` — Gradle modules for shared code and feature modules.
- `ios/` — iOS client.
- `docs/`, `site/` — documentation and project website.

## Build

Gradle wrapper, Kotlin DSL, version catalog (`libs.*`). From the repo root:

```
./gradlew assembleDebug      # Windows: gradlew.bat assembleDebug
./gradlew test
```

- compileSdk/targetSdk 37, minSdk 26.
- Stack: Compose, Hilt, KSP.
- Outlook sign-in uses MSAL, which pulls a Surface Duo artifact that only exists on the Azure feed declared in `settings.gradle.kts`; keep that repository.
- Copy `local.properties.example` to `local.properties` and fill in local values; never commit secrets.
- `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`: declare repositories only there, not in module build files.

## Conventions

- Match the surrounding Kotlin style and the existing module boundaries (`app`, `core`, `feature`).
