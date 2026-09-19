# ShowTime: Telemetry & Observability Architecture Guide

> **Authoritative Technical Standard** — Engineering guidelines for product analytics, crash diagnostics, and privacy-preserving observability in ShowTime.

---

## 1. Core Architecture & Philosophy

ShowTime enforces a **High-Signal, Actionable, and Lean Telemetry Architecture** built around a strict **Two-Pillar Model**:

```
                                  SHOWTIME TELEMETRY ENGINE
                                              │
               ┌──────────────────────────────┴──────────────────────────────┐
               ▼                                                             ▼
     PILLAR 1: PRODUCT ANALYTICS                               PILLAR 2: OPERATIONAL OBSERVABILITY
    (User Journeys & Engagement)                                (App Stability & Diagnostics)
               │                                                             │
      Interface: Analytics                                          Interface: CrashReporter
  Implementation: FirebaseAnalyticsImpl                         Implementation: FirebaseCrashReporter
               │                                                             │
  ┌────────────┴────────────┐                                   ┌────────────┴────────────┐
  ▼                         ▼                                   ▼                         ▼
Screen Views          Action Events                       Breadcrumbs               Non-Fatal 5xx
(Compose Lifecycle)   (ViewModels)                        (HTTP 4xx / Offline)      (CrashForensics)
```

### The Two Pillars:
1. **Pillar 1: Product Analytics (`Analytics`)**
   - **Contract**: [`com.ssverma.core.analytics.Analytics`](../core-analytics/src/main/java/com/ssverma/core/analytics/Analytics.kt)
   - **Purpose**: Tracks screen navigation, user journeys, feature adoption, and intentional user actions.
   - **Rule**: Purely user-driven actions and UX milestones. **Zero system diagnostic noise, network errors, or stack traces.**

2. **Pillar 2: Operational Observability (`CrashReporter`)**
   - **Contract**: [`com.ssverma.core.analytics.CrashReporter`](../core-analytics/src/main/java/com/ssverma/core/analytics/CrashReporter.kt)
   - **Purpose**: Diagnostics, remote server 5xx outages, and breadcrumbs leading up to unexpected states.
   - **Rule**: **Zero user funnel spam or screen tracking.** Reserved for crash forensics, stability monitoring, and service degradation detection.

---

## 2. Privacy & Open-Source Invariants

As an open-source project, ShowTime upholds strict privacy and transparency standards:

1. **Zero Personally Identifiable Information (PII)**:
   - No email addresses, names, search keystrokes, IP addresses, or payment tokens are ever logged.
   - User references use anonymous installation tokens (`FID`) or one-way cryptographic hashes.
2. **Local Debug Builds Run 100% Free of Cloud Telemetry**:
   - In debug builds, `Analytics` resolves to `DebugAnalytics` and `CrashReporter` resolves to `DebugCrashReporter`.
   - All telemetry events and breadcrumbs are pretty-printed to Logcat (`tag = "ShowTimeAnalytics"`, `tag = "ShowTimeCrashReporter"`). Zero packets leave the device.
3. **No Over-Logging Policy**:
   - **Forbidden**: Tracking character-by-character text changes, intermediate search debounces, list scroll pixel offsets, item bind events, or HTTP 200 OK responses.
   - **Client Network Drops**: Airplane mode, weak signal, and socket timeouts are logged as **ephemeral breadcrumbs**, never as exceptions or analytics events.
   - **Signal-to-Noise Ratio (SNR)**: Telemetry must be high-signal and actionable.

---

## 3. Module Placement & Taxonomy

Following [`MODULAR_ARCHITECTURE_AND_CAPABILITY_TAXONOMY.md`](MODULAR_ARCHITECTURE_AND_CAPABILITY_TAXONOMY.md):

| Module | Responsibility | Public APIs |
| :--- | :--- | :--- |
| `:core-analytics` | Low-level SDK abstraction and engine bindings (Firebase / Debug Logcat). Zero domain awareness. | `Analytics`, `CrashReporter`, `AnalyticsModule` |
| `:shared-analytics` | Domain-specific analytics contracts, sealed event taxonomy, screen name registry, and network trackers. | `AnalyticsEvent`, `TrackScreenView`, `NetworkErrorTracker`, `DefaultNetworkErrorTracker`, Cross-cutting events |
| `:feature-*` | Emits high-level business events via injected `Analytics` and `TrackScreenView`. | Feature ViewModels and Screen composables |

---

## 4. Technical Event Taxonomy

Every event in ShowTime is strongly typed using sealed interfaces extending `AnalyticsEvent`.

### A. Screen Registry (`ScreenNames`)
Screens are tracked automatically when entering composition via `TrackScreenView(screenName)`:
- `ScreenNames.DiscoverFilter`
- `ScreenNames.CinemaDiary`
- `ScreenNames.TasteProfile`
- `ScreenNames.CinephileWrapped`
- `ScreenNames.BacklogChallenge`
- `ScreenNames.CinemaReceipt`
- `ScreenNames.SecretSharedList`
- `ScreenNames.CommunityDiscussions`
- `ScreenNames.CommunityCuratedLists`
- `ScreenNames.MovieMatchRoom`
- `ScreenNames.AccountCloudBackup`
- `ScreenNames.ProPaywall`
- `ScreenNames.SubscriptionManagement`

### B. Strongly Typed Event Schemas

| Feature / Domain | Event Class | Key Parameters |
| :--- | :--- | :--- |
| **Universal Discover** | `FilterAnalyticsEvent.FilterApplied`<br>`FilterAnalyticsEvent.FilterReset` | `media_type`, `genre_count`, `provider_count`, `sort_by`, `year_range` |
| **Cinema Diary** | `DiaryAnalyticsEvent.EntryLogged`<br>`DiaryAnalyticsEvent.EntryDeleted` | `media_id`, `media_type`, `rating`, `has_review`, `viewing_date` |
| **Taste Profile** | `TasteProfileAnalyticsEvent.ProfileViewed`<br>`TasteProfileAnalyticsEvent.ProfileShared` | `total_logged_entries`, `top_genre`, `top_decade`, `share_medium` |
| **Cinephile Wrapped** | `WrappedAnalyticsEvent.WrappedOpened`<br>`WrappedAnalyticsEvent.SlideViewed`<br>`WrappedAnalyticsEvent.SummaryShared` | `year`, `slide_index`, `slide_type`, `share_platform` |
| **Backlog Challenge** | `BacklogAnalyticsEvent.ChallengeCreated`<br>`BacklogAnalyticsEvent.ChallengeProgress`<br>`BacklogAnalyticsEvent.ChallengeCompleted` | `target_count`, `current_count`, `percent_complete`, `challenge_type` |
| **Cinema Receipt** | `CinemaReceiptAnalyticsEvent.ReceiptGenerated`<br>`CinemaReceiptAnalyticsEvent.ReceiptShared` | `receipt_timeframe`, `item_count`, `total_runtime_hours`, `share_format` |
| **Secret Shared Lists** | `SecretListAnalyticsEvent.ListCreated`<br>`SecretListAnalyticsEvent.ListUnlocked`<br>`SecretListAnalyticsEvent.ListShared` | `item_count`, `is_password_protected`, `unlock_method`, `share_method` |
| **Community Hub** | `DiscussionAnalyticsEvent.ThreadViewed`<br>`DiscussionAnalyticsEvent.ReplyPosted`<br>`CuratedListAnalyticsEvent.ListCloned` | `thread_id`, `media_id`, `has_spoiler_tag`, `list_id`, `item_count` |
| **Movie Match Room** | `MatchAnalyticsEvent.RoomCreated`<br>`MatchAnalyticsEvent.RoomJoined`<br>`MatchAnalyticsEvent.CardSwiped`<br>`MatchAnalyticsEvent.MatchAchieved` | `session_mode`, `action` (like/pass), `rounds_taken`, `participant_count` |
| **Cloud Backup** | `BackupAnalyticsEvent.SyncStarted`<br>`BackupAnalyticsEvent.SyncCompleted`<br>`BackupAnalyticsEvent.SyncFailed`<br>`BackupAnalyticsEvent.DataRestored` | `trigger` (manual/scheduled), `record_count`, `duration_ms`, `error_category` |
| **Pro Paywall & Gating** | `PaymentAnalyticsEvent.PaywallImpression`<br>`PaymentAnalyticsEvent.PlanSelected`<br>`PaymentAnalyticsEvent.PurchaseCompleted`<br>`FeatureGateAnalyticsEvent.GateEncountered` | `trigger_feature`, `offer_id`, `tier`, `gate_type` |
| **Release Radar** | `ReleaseRadarAnalyticsEvent.SyncExecuted`<br>`ReleaseRadarAnalyticsEvent.NotificationEngaged` | `tracked_titles_count`, `new_releases_found`, `days_until_release` |

---

## 5. Developer Implementation Playbook

### A. Screen Tracking in Jetpack Compose
```kotlin
@Composable
fun CinephileWrappedScreen(
    viewModel: CinephileWrappedViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = ScreenNames.CinephileWrapped)
    // Layout composables...
}
```

### B. Action Event Logging in ViewModels
```kotlin
@HiltViewModel
class CinemaDiaryViewModel @Inject constructor(
    private val diaryRepository: CinemaDiaryRepository,
    private val analytics: Analytics
) : ViewModel() {

    fun onSaveEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryRepository.saveEntry(entry)
            analytics.logEvent(
                DiaryAnalyticsEvent.EntryLogged(
                    mediaId = entry.mediaId,
                    mediaType = entry.mediaType.name,
                    rating = entry.rating,
                    hasReview = entry.review.isNotBlank(),
                    viewingDate = entry.viewingDate.toString()
                )
            )
        }
    }
}
```

### C. Network Diagnostics & Exception Routing
```kotlin
class DefaultNetworkErrorTracker @Inject constructor(
    private val crashReporter: CrashReporter
) : NetworkErrorTracker {

    override fun trackHttpError(code: Int, url: String, message: String) {
        if (code in 500..599) {
            // High severity: Remote server failure
            crashReporter.recordException(
                RemoteServerException(
                    message = "Remote server failure $code for $url: $message",
                    statusCode = code,
                    url = url
                )
            )
        } else {
            // Low severity: Diagnostic breadcrumb only
            crashReporter.logBreadcrumb("HTTP $code: $url - $message")
        }
    }

    override fun trackNetworkException(throwable: Throwable, url: String) {
        // Ephemeral breadcrumb - user network drop is not an application crash
        crashReporter.logBreadcrumb("Network error: $url - ${throwable.message}")
    }
}
```

### D. Firestore Query Auditing & Quota Telemetry
```kotlin
class DefaultFirestoreAuditTracker @Inject constructor(
    private val analytics: Analytics,
    private val crashReporter: CrashReporter,
    private val appConfigProvider: AppConfigProvider
) : FirestoreAuditTracker {

    override fun trackQuery(
        queryTag: String,
        screenName: String?,
        docsCount: Int,
        isFromCache: Boolean,
        durationMs: Long
    ) {
        // Asynchronously dispatches firestore_query_audit event (0ms UI overhead, 0 extra reads)
        analytics.logEvent(
            FirestoreAuditAnalyticsEvent(queryTag, screenName, docsCount, isFromCache, durationMs)
        )
    }

    override fun trackFirestoreError(queryTag: String, throwable: Throwable, durationMs: Long) {
        // Classifies RESOURCE_EXHAUSTED / PERMISSION_DENIED / UNAVAILABLE for instant Admin alerting
        if (throwable.message?.contains("RESOURCE_EXHAUSTED", ignoreCase = true) == true) {
            crashReporter.recordException(
                FirestoreQuotaExhaustedException("Firestore Quota Exceeded on $queryTag", throwable),
                mapOf("query_tag" to queryTag, "duration_ms" to durationMs.toString())
            )
        }
    }
}
```

---

## 6. ProGuard & R8 Symbolication Rules

To guarantee clean stack traces and accurate line numbers in release builds, the following rules are enforced in `core-analytics/consumer-rules.pro` and `app/proguard-rules.pro`:

```proguard
# Preserve source line numbers and file names for Crashlytics symbolication
-keepattributes SourceFile,LineNumberTable

# Preserve Firebase Crashlytics annotations and class signatures
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Prevent obfuscation of custom telemetry exceptions
-keep public class com.ssverma.shared.analytics.RemoteServerException { *; }

# Prevent coroutine continuation stripping during stack unwinding
-keepclassmembers class kotlinx.coroutines.internal.DispatchedContinuation {
    java.lang.Object _reusableCancellableContinuation;
}
```

---

## 7. Contributor Checklist (Pre-PR Quality Gate)

Before submitting any PR that introduces or touches telemetry:
- [ ] **No Hardcoded Strings**: Event names and parameter keys are defined in sealed `AnalyticsEvent` interfaces.
- [ ] **No Keystroke Tracking**: Never trigger analytics in `onValueChange` of text inputs.
- [ ] **No 200 OK Telemetry**: Successful API requests must not produce telemetry.
- [ ] **No Offline Crash Spam**: Never call `CrashReporter.recordException` for client network drops or HTTP 4xx errors. Use `logBreadcrumb`.
- [ ] **Privacy Check**: Zero user credentials, tokens, or PII in event payloads.
- [ ] **Deterministic Verification**: Pass `git add -A && ./.githooks/pre-commit` and `./gradlew testDebugUnitTest`.
