# Smart Release Radar Architecture & Technical Specification

## 1. Overview & Objective
**Smart Release Radar** is an intelligent, low-power background worker built on Android **WorkManager** that notifies users when movies bookmarked in their **Watchlist** premiere in theaters or arrive on their subscribed streaming services.

### Core Goals
- **Retention Driver**: Brings cinephiles back to the app at high-intent moments (theatrical premiere day or streaming arrival).
- **Zero API Quota Burn**: Implements a strict two-tier architecture that uses **0 network calls** for theatrical releases and caps streaming lookups at a maximum of **3 TMDB API calls per day**.
- **Zero False Positives**: Differentiates subscription SVOD (`flatrate`, `free`, `ads`) from digital storefront rent/buy TVOD, matching availability exclusively against the user's active streaming services.
- **Enterprise Resilience**: Complete crash-proofing against network timeouts, malformed dates, and missing payloads.
- **Instant Remote Control**: Guarded at the data layer, background worker, and UI by the Firebase Remote Config kill-switch `remote_release_radar_enabled`.

---

## 2. Two-Tier Verification Architecture

```mermaid
flowchart TD
    Start([WorkManager: ReleaseRadarWorker]) --> KillSwitch{remote_release_radar_enabled == true?}
    KillSwitch -- No --> ExitSuccess[Exit: ReleaseRadarResult.Success]
    KillSwitch -- Yes --> LocalPref{Local Notifications & Radar Enabled?}
    LocalPref -- No --> ExitSuccess
    LocalPref -- Yes --> PermCheck{POST_NOTIFICATIONS Permission?}
    PermCheck -- No --> ExitSuccess
    PermCheck -- Yes --> Tier1[Tier 1: Theatrical Premiere Evaluation]

    subgraph "Tier 1: Theatrical Radar (0 TMDB API Calls)"
        Tier1 --> LocalDb1[(Room Watchlist: releaseDate == todayIso)]
        LocalDb1 --> HasTheatrical{Candidate found?}
        HasTheatrical -- Yes --> DispatchTheatrical[Dispatch Theatrical Push Alert<br/>Mark hasNotifiedTheatrical = true]
        HasTheatrical -- No --> Tier2[Tier 2: Streaming Arrival Radar]
        DispatchTheatrical --> SkipStreaming[Skip Streaming Today<br/>To avoid notification spam]
        SkipStreaming --> Done([Complete Run])
    end

    subgraph "Tier 2: Streaming Radar (Max 3 TMDB API Calls/Day)"
        Tier2 --> LocalDb2[(Room Watchlist Query:<br/>30d <= releaseAge <= 120d<br/>lastCheck < now - 7d<br/>LIMIT 3)]
        LocalDb2 --> CandidatesExist{Candidates found?}
        CandidatesExist -- No --> Done
        CandidatesExist -- Yes --> QueryTMDB[Query TMDB: /movie/{id}/watch/providers]
        QueryTMDB --> FilterSVOD{Available on User SVOD?<br/>flatrate, free, ads}
        FilterSVOD -- Yes --> DispatchStreaming[Dispatch Streaming Push Alert<br/>Update providers & hasNotifiedStreaming = true]
        FilterSVOD -- No --> SetCooldown[Set 7-Day Recheck Cooldown<br/>lastStreamingCheckEpochMs = now]
        DispatchStreaming --> Done
        SetCooldown --> Done
    end
```

### Tier 1: Theatrical Premiere Alerts (0 Network Calls)
- Evaluates titles in the local Room database (`WatchlistEntity`) where `releaseDate == currentDateIso` and `hasNotifiedTheatrical == false`.
- **Pure Local Room Query**: 0 TMDB API calls executed.
- Dispatches a rich local push notification:
  - **Title**: `🍿 [Movie Title] in theaters today!`
  - **Body**: `Now playing on the big screen. Check showtimes and details.`
  - **Deep Link**: `showtime://showtime.ssverma.in/movie/{mediaId}`
  - **Thumbnail**: Movie poster or backdrop image.
- Marks `hasNotifiedTheatrical = true` in SQLite to ensure no duplicate alerts.

### Tier 2: Subscription Streaming Arrival Radar (Strictly Rate-Limited)
- **Temporal Window**: Only queries movies whose theatrical release was between **30 days** (`STREAMING_WINDOW_MIN_DAYS`) and **120 days** (`STREAMING_WINDOW_MAX_DAYS`) ago.
- **Recheck Cooldown**: Enforces a minimum **7-day cooldown** (`STREAMING_RECHECK_COOLDOWN_DAYS`) before re-checking TMDB for a movie that was not streaming on previous runs.
- **Daily Hard Cap**: At most **3 TMDB API calls per daily run** (`MAX_STREAMING_CALLS_PER_DAY = 3`).
- **SVOD vs TVOD False-Positive Prevention**:
  - TMDB provides `flatrate`, `rent`, `buy`, `free`, and `ads`.
  - Movies available only for rent or buy (e.g. Apple TV, Amazon VOD purchases) are **ignored**.
  - Matches `flatRate + free + ads` providers against the user's subscribed streaming providers (`appConfigRepository.userStreamingSubscriptions`).
- Dispatches a rich push notification:
  - **Title**: `🎬 [Movie Title] is now streaming!`
  - **Body**: `Now available on [Provider 1, Provider 2].`
  - **Deep Link**: `showtime://showtime.ssverma.in/movie/{mediaId}`

---

## 3. Database Schema & Migration

Database version bumped from `11` to `12` in [`ShowTimeDatabase.kt`](file:///Users/ss/Projects/ShowTime/shared-data/src/main/java/com/ssverma/shared/data/local/db/ShowTimeDatabase.kt).

### Fields Added to `watchlist` Table:
| Column | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `hasNotifiedTheatrical` | `INTEGER` (`Boolean`) | `0` | Flag ensuring theatrical alert is sent at most once. |
| `hasNotifiedStreaming` | `INTEGER` (`Boolean`) | `0` | Flag ensuring streaming alert is sent at most once. |
| `lastStreamingCheckEpochMs` | `INTEGER` (`Long`) | `0` | Milliseconds timestamp of the last TMDB watch provider lookup. |
| `knownStreamingProviders` | `TEXT` (`String`) | `""` | Comma-separated names of streaming services detected. |

---

## 4. Remote Feature Flag & Kill-Switch

Defined in [`ReleaseRadarConfig.kt`](file:///Users/ss/Projects/ShowTime/shared-domain/src/main/java/com/ssverma/shared/domain/model/release/ReleaseRadarConfig.kt):

```kotlin
object ReleaseRadarConfig {
    const val REMOTE_KEY_RELEASE_RADAR_ENABLED = "remote_release_radar_enabled"
    const val DEFAULT_RELEASE_RADAR_ENABLED = true
    const val MAX_STREAMING_CALLS_PER_DAY = 3
    const val STREAMING_WINDOW_MIN_DAYS = 30L
    const val STREAMING_WINDOW_MAX_DAYS = 120L
    const val STREAMING_RECHECK_COOLDOWN_DAYS = 7L
    const val DEFAULT_REGION = "US"
}
```

### Tri-Layer Remote Gating:
1. **Background Processor**:
   At the very top of `ReleaseRadarProcessor.executeRadar()`, verifies `appConfigProvider.getBoolean(REMOTE_KEY_RELEASE_RADAR_ENABLED, true)`. If `false`, immediately returns `ReleaseRadarResult.Success` without database queries or network requests.
2. **Repository Layer**:
   [`DefaultAppConfigRepository.kt`](file:///Users/ss/Projects/ShowTime/shared-data/src/main/java/com/ssverma/shared/data/repository/DefaultAppConfigRepository.kt) combines the local DataStore preference with the Remote Config flow.
3. **UI Screen**:
   [`ProfileScreen.kt`](file:///Users/ss/Projects/ShowTime/feature-account/src/main/java/com/ssverma/feature/account/ui/profile/ProfileScreen.kt) conditionally hides the switch tile under Preferences if `isReleaseRadarRemoteEnabled == false`.

---

## 5. WorkManager Configuration & Battery Optimization

- **Worker Class**: [`ReleaseRadarWorker`](file:///Users/ss/Projects/ShowTime/shared-data/src/main/java/com/ssverma/shared/data/worker/ReleaseRadarWorker.kt)
- **Work Name**: `periodic_smart_release_radar`
- **Work Policy**: `ExistingPeriodicWorkPolicy.KEEP`
- **Cadence**: 24 hours (`1, TimeUnit.DAYS`)
- **System Constraints**:
  - `NetworkType.CONNECTED`: Only runs when connected to the internet.
  - `requiresBatteryNotLow(true)`: Postpones execution if device battery is depleted.
- **Reactive Scheduling & Cancellation**:
  - Managed by [`NotificationSyncManager`](file:///Users/ss/Projects/ShowTime/app/src/main/java/com/ssverma/showtime/notifications/NotificationSyncManager.kt), which observes `isNotificationsEnabled && isReleaseRadarEnabled`.
  - When enabled, calls `ReleaseRadarWorker.schedule(context)`.
  - When disabled (locally or via Remote Config), immediately cancels the work via `ReleaseRadarWorker.cancel(context)`.

---

## 6. Verification & Automated Test Suite

- [`ReleaseRadarProcessorTest.kt`](file:///Users/ss/Projects/ShowTime/shared-data/src/test/java/com/ssverma/shared/data/worker/ReleaseRadarProcessorTest.kt): 13 exhaustive unit tests covering kill-switch, local toggle, permission, theatrical dispatch, multi-candidate deduplication, blank title filtering, streaming match, rent/buy exclusion, malformed date defense, and network failure tolerance.
- [`ReleaseRadarWorkerTest.kt`](file:///Users/ss/Projects/ShowTime/shared-data/src/test/java/com/ssverma/shared/data/worker/ReleaseRadarWorkerTest.kt): Validates provider filtering, deep link URLs, and rate-limiting constants.
- [`ProfileViewModelTest.kt`](file:///Users/ss/Projects/ShowTime/feature-account/src/test/java/com/ssverma/feature/account/ui/profile/ProfileViewModelTest.kt): Validates reactive remote kill-switch UI state changes.
