# Did My Team Win?

Spoiler-free game result notifications for Android. Track any team in any sport — get notified when their game finishes, with the score hidden until you tap.

Fully local. No backend, no accounts, no cloud sync. Everything runs on-device.

## How It Works

1. **Search** for your team by name (powered by [TheSportsDB](https://www.thesportsdb.com/))
2. **Track** multiple teams simultaneously
3. **Wait** — the app automatically schedules a background check for each team's next game
4. **Get notified** when the game finishes — score is hidden in the notification
5. **Tap to reveal** the score and result summary in-app

## Screenshots

_Coming soon — app is in early development._

## Architecture

```
User adds team
    → TheSportsDB searchteams API → select team → store in Room
    → Fetch next game (eventsnext API) → store event ID
    → WorkManager schedules job for ~3hrs after game start
    → Job fires → lookupevent API → check status
        → Finished? → Notify + schedule next game
        → Not finished? → Retry in 1hr (max 12 retries)
        → Cancelled/Postponed? → Notify + schedule next game
```

### Key Design Decisions

- **WorkManager** handles all background scheduling (survives Doze mode, reboots, battery optimization)
- **No exact alarms** — WorkManager timing is ±minutes, which is fine for this use case
- **BootReceiver** reschedules all pending jobs after device restart
- **Template-based summaries** in V1 (e.g., "MoDo Hockey beat Luleå HF 4-2 in SHL") — no LLM needed
- **TheSportsDB free API key `123`** — publicly documented, covers all sports globally

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| Background jobs | WorkManager |
| HTTP | Retrofit + Gson |
| Local storage | Room |
| Image loading | Coil |
| Min SDK | 26 (Android 8.0) |

## Project Structure

```
app/src/main/java/com/kristianolsson/didmyteamwin/
├── MainActivity.kt              # Single activity, Compose NavHost
├── data/
│   ├── api/
│   │   ├── SportsDbApi.kt       # Retrofit interface (search, eventsnext, lookupevent)
│   │   ├── Models.kt            # API response data classes
│   │   └── RetrofitInstance.kt  # Retrofit singleton
│   └── db/
│       ├── TrackedTeam.kt       # Room entity with scheduling state
│       ├── TrackedTeamDao.kt    # Room DAO
│       └── AppDatabase.kt      # Room database
├── worker/
│   ├── GameCheckWorker.kt       # CoroutineWorker — fetches results, handles retries
│   └── SchedulerHelper.kt      # Schedules WorkManager jobs per team
├── notification/
│   └── NotificationHelper.kt   # Notification channel + builders
├── receiver/
│   └── BootReceiver.kt         # Reschedules jobs after reboot
└── ui/
    ├── TeamListScreen.kt        # Home — tracked teams list
    ├── TeamListViewModel.kt     # Team list state + add/remove
    ├── TeamSearchScreen.kt      # Search + add teams
    ├── TeamSearchViewModel.kt   # Debounced search
    ├── ResultScreen.kt          # Spoiler-free score reveal
    └── theme/
        └── Theme.kt             # Material 3 dark/light theme
```

## Building

Requires Android SDK and JDK 17+.

```bash
./gradlew assembleDebug
```

Install on a connected device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## V1 Scope

- Personal sideload (no Play Store)
- Multi-team tracking
- Template-based result summaries
- Spoiler-free notification → tap to reveal score
- Works with any sport/team on TheSportsDB

## License

Personal project — not licensed for redistribution.
