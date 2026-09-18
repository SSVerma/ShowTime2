# Rewarded Ads, Freemium Monetization & Remote Config Architecture

## 1. Overview & Architectural Philosophy

ShowTime employs a balanced, user-centric freemium monetization strategy. Free users enjoy full access to core discovery, library management, and cinephile exploration, while high-utility power tools and luxury export features can be unlocked either via **ShowTime Pro** or by engaging with **Rewarded Video Ads**.

### Architectural Pillars
1. **Decoupled Engine (`:shared-ads` + `:core-ads`)**: Low-level AdMob SDK orchestration is isolated in `:core-ads`. Feature-agnostic pass management, quota tracking, and unified presentation UI live in `:shared-ads`. Feature modules define their own strongly typed `PassKey` identifiers locally and never leak domain logic into ads infrastructure.
2. **The 3-Tier Monetization Model**: Rather than blanket 24-hour passes that dilute recurring revenue and Pro conversions, capabilities operate across three intentional tiers:
   - **Tier 1: On-Click Consumable Slots/Tokens** (Permanent list/goal capacity expansion, reminder tokens).
   - **Tier 2: Single-Action & Export Sessions (2 Hours)** (Vintage receipts, Wrapped story cards, Secret share themes).
   - **Tier 3: Context-Aware Browsing Sessions (6 Hours)** (Universal discovery multi-service streaming filter, Trakt cloud sync, Taste Profile analytics).
3. **Over-The-Air (OTA) Remote Config**: All pass durations and free quota thresholds are connected to Firebase Remote Config via `AppConfigProvider`, allowing live adjustments and A/B testing without Play Store releases.
4. **100% Standardized Bottom Sheet Presentation**: All gates render as modal bottom sheets via `ShowTimeBottomSheet` (`GatePresentationStyle.BottomSheet`), guaranteeing visual consistency, dark/light theme purity, and zero layout jarring.

---

## 2. System Architecture & Component Interactions

```mermaid
flowchart TD
    subgraph CoreLayer[Core Infrastructure Layer]
        RewardedMgr["RewardedAdManager (:core-ads)"]
        CCM["AppConfigProvider (:core-ccm / Firebase)"]
        KVStorage["KeyValueStorage (:core-storage / DataStore)"]
    end

    subgraph SharedLayer[Shared Capabilities Layer]
        RewardMgr["RewardManager & RewardManagerImpl (:shared-ads)"]
        PassPolicy["FeaturePassPolicy & PassDurations (:shared-ads)"]
        FeatureGate["ShowTimeFeatureGate & ShowTimeBottomSheet (:shared-ads)"]
    end

    subgraph FeatureConsumers[Feature Modules]
        DiscoveryFilter["Universal Discovery Multi-Filter (:feature-filter)"]
        CinemaReceipt["Vintage Cinema Receipt (:feature-library)"]
        CinemaWrapped["Cinephile Wrapped Stories (:feature-library)"]
        TasteProfile["Taste Profile & Decades Radar (:feature-library)"]
        CustomLists["Custom Library Lists (:feature-library)"]
        BacklogChallenges["Backlog Challenges (:feature-library)"]
        SecretShare["Secret Share Luxury Themes (:feature-library)"]
        AiringReminders["Airing Reminders Quota (:feature-movie / :feature-tv)"]
        MovieMatch["Movie Match Room Extra Decks (:feature-match)"]
        TraktSync["Trakt.tv Bidirectional Sync (:feature-account)"]
        CloudBackup["Manual Cloud Backup Push (:feature-account)"]
    end

    FeatureConsumers -->|Defines PassKey & GateConfig| FeatureGate
    FeatureGate -->|Preloads Ad on Gate Open| RewardedMgr
    FeatureConsumers -->|Grants & Observes Passes| RewardMgr
    RewardMgr -->|Reads Duration Overrides| CCM
    RewardMgr -->|Persists Timestamps & Slots| KVStorage
    PassPolicy -->|Preset Durations (2h, 6h, 30m)| RewardMgr
```

---

## 3. Feature Inventory & Policy Matrix

| # | Feature Area | Module | `PassKey` | Default Policy & Duration | Presentation Component | Revenue vs UX Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Universal Discovery Multi-Service Filter** | `:feature-filter`, `:common-ui` | `multi_service_filter` | `TimedPass(6h)` | `StreamingMultiServiceGateDialog` (BottomSheet) | 6h pass covers full evening browsing session; prompts fresh ad opportunity on subsequent days. |
| **2** | **Vintage Cinema Receipt HD Export** | `:feature-library` | `watermark_free_receipt` | `TimedPass(2h)` | `ShowTimeFeatureGate` (BottomSheet) | Short 2h window allows customizing and exporting current receipt haul without giving away weeks of free exports. |
| **3** | **Cinephile Wrapped Story Card Export** | `:feature-library` | `cinema_wrapped` | `TimedPass(2h)` | `ShowTimeFeatureGate` (BottomSheet) | 2h window covers full recap sharing session across social channels. |
| **4** | **Taste Profile & Decades Radar Analytics** | `:feature-library` | `taste_analytics_radar` | `TimedPass(6h)` | `ShowTimeFeatureGate` (BottomSheet) | 6h window allows in-depth stats exploration without mid-session lockouts. |
| **5** | **Custom Library Lists Quota** | `:feature-library` | `custom_list_slots` | `ConsumableSlot(1)` | `ShowTimeFeatureGate` (BottomSheet) | Free limit is 3 lists. Watching an ad permanently adds +1 list capacity slot. |
| **6** | **Custom Backlog Challenge Goals** | `:feature-library` | `extra_custom_goal` | `ConsumableSlot(1)` | `ShowTimeFeatureGate` (BottomSheet) | Free limit is 1 goal. Watching an ad permanently adds +1 active challenge slot. |
| **7** | **Secret Share Luxury Themes & Export** | `:feature-library` | `list_share_themes` | `TimedPass(2h)` | `SecretShareLuxuryGateDialog` (BottomSheet) | 2h window allows styling and exporting themed story cards and secret links. |
| **8** | **Airing Reminders Quota** | `:feature-movie`, `:feature-tv` | `airing_reminders` | `ConsumableSlot(1)` | `ShowTimeFeatureGate` (BottomSheet) | Free limit is 3 reminders. Watching an ad grants +1 consumable reminder token. |
| **9** | **Movie Match Room Daily Bonus Decks** | `:feature-match` | `match_room` | `ActionUnlock(30m)` | `MatchQuotaModal` (BottomSheet) | Watching an ad immediately grants +1 bonus swipe deck (15-20 cards). |
| **10** | **Trakt.tv Two-Way Cloud Sync** | `:feature-account` | `trakt_sync` | `TimedPass(6h)` | `ShowTimeFeatureGate` (BottomSheet) | 6h window allows full bidirectional initial sync and history verification. |
| **11** | **Manual Cloud Backup Trigger** | `:feature-account` | `auto_backup` | `ActionUnlock(30m)` | `ShowTimeFeatureGate` (BottomSheet) | Free users trigger immediate manual backup push on ad completion. |
| **12** | **Cinema Trivia Streak Saver (Revive)** | `:feature-movie` | `cinema_game_revive` | On-Click Action | `CinemaGameSecondChanceBanner` | Immediate consumable second-chance puzzle revive. |
| **13** | **Community Discussions Quota** | `:feature-community`, `:feature-movie`, `:feature-tv` | `community_comments` | `ConsumableSlot(3)` | `ShowTimeFeatureGate` (BottomSheet) | Free limit is 3 comments/day. Watching an ad grants +3 bonus thoughts. Pro users get unlimited comments and Verified Pro badge. |

---

## 4. Remote Config Key Taxonomy & Over-the-Air Tuning

All rewarded pass durations and free quotas can be tuned dynamically via Firebase Remote Config without releasing an app update:

### A. Pass Duration Overrides
`RewardManagerImpl` resolves durations dynamically using the following key convention:
`config_pass_duration_<pass_key_value>_hours` (`Long`)

| Remote Config Key | Type | Default Fallback | Target Feature | Description |
| :--- | :--- | :--- | :--- | :--- |
| `config_pass_duration_multi_service_filter_hours` | `Long` | `6` | Discovery Multi-Service | Hours granted when user watches an ad for multi-service filtering. |
| `config_pass_duration_watermark_free_receipt_hours` | `Long` | `2` | Cinema Receipt | Hours granted for watermark-free receipt export and VIP styles. |
| `config_pass_duration_cinema_wrapped_hours` | `Long` | `2` | Cinema Wrapped | Hours granted for Wrapped story card exports. |
| `config_pass_duration_taste_analytics_radar_hours` | `Long` | `6` | Taste Profile | Hours granted for Decades Time Machine and AI recommendations. |
| `config_pass_duration_list_share_themes_hours` | `Long` | `2` | Secret Share Themes | Hours granted for luxury card themes and unlisted link sharing. |
| `config_pass_duration_trakt_sync_hours` | `Long` | `6` | Trakt.tv Cloud Sync | Hours granted for bidirectional Trakt cloud sync. |

### B. Free Tier Quota Overrides

| Remote Config Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `config_free_reminders_limit` | `Long` | `3` | Maximum active release reminders allowed before requiring Pro or ad tokens. |
| `config_free_custom_lists_limit` | `Long` | `3` | Maximum custom lists allowed for free users before requiring ad slots. |
| `config_free_custom_goals_limit` | `Long` | `1` | Maximum active backlog challenges allowed for free users. |
| `config_free_match_daily_decks` | `Long` | `1` | Daily free match decks allowed per room before requiring ad unlocks. |
| `config_free_comments_daily_limit` | `Long` | `3` | Maximum daily community comments for free users before requiring Pro or ad pass. |

---

## 5. UI Gating & Presentation Guidelines

### A. The Dumb UI Gate Pattern
All feature gates delegate orchestration to `ShowTimeFeatureGate` or `ShowTimeBottomSheet`.
- **Preloading**: `ShowTimeFeatureGate` automatically calls `rewardedAdManager.loadAd()` on appearance via `LaunchedEffect(Unit)`.
- **Dumb Content**: All presentation text is encapsulated in Compose-stable `UiText`.
- **Pro Toggle**: When `isProPaymentEnabled` is true, gates render the Pro CTA as primary filled button and Rewarded Ad CTA as tonal/outlined. When Pro billing is disabled remotely, Rewarded Ad dynamically ascends to primary filled button.

### B. Anti-Fraud & Security
- Ad rewards are only credited in the verified `onUserEarnedRewardListener` callback.
- Pass timestamps are stored in encrypted/private DataStore with monotonic timestamp validation.
