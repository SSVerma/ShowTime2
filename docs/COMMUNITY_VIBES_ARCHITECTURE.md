# Community Mood & Vibe Reactions: Real-Time Sync & Architecture Spec

## 1. Overview & Vision
ShowTime's **1-Tap Mood & Vibe Reactions** provides instant sentiment feedback for movies and TV shows. It enables users to vote on emotional resonance and viewing contexts (`Mind-Bending`, `Comfort Watch`, `Plot Twist King`, `Must Watch in IMAX`, `Cried My Eyes Out`, `Overrated`) with **0ms optimistic latency** and **real-time bidirectional cloud synchronization**.

---

## 2. End-to-End System Architecture

```mermaid
flowchart TB
    subgraph Client ["Android Client (Jetpack Compose & Kotlin Flow)"]
        UI["UI Layer: MediaReactionsSection & ReactionPills"]
        VM["ViewModel: MovieDetailsViewModel / TvShowDetailsViewModel"]
        UC["Domain Use Cases: GetMediaReactionsUseCase / ToggleMediaReactionUseCase"]
        REPO["Data Layer: CommunityRepositoryImpl"]
        CACHE["In-Memory StateFlow Cache (0ms Optimistic UI)"]
        STORE["DataStore KeyValueStorage (Anonymous ID / Profile Token)"]
    end

    subgraph Firebase ["Google Cloud Firestore Backend"]
        GLOBAL_DOC["media_reactions/{mediaType}_{mediaId}"]
        USER_DOC["user_media_reactions/{userId}_{mediaType}_{mediaId}"]
    end

    UI -->|1. Tap Reaction Pill| VM
    VM -->|2. Dispatch Action| UC
    UC -->|3. Invoke Toggle| REPO
    
    REPO -->|4. Immediate 0ms Update| CACHE
    CACHE -->|5. Instant State Emission| UI
    
    REPO -->|6. Atomic WriteBatch| Firebase
    GLOBAL_DOC -.->|7. Real-Time Snapshot Listener| REPO
    USER_DOC -.->|7. Real-Time Snapshot Listener| REPO
    REPO -->|8. Combined Reactive Stream| VM
    STORE -->|Resolved User ID| REPO
```

---

## 3. Cloud Firestore Data Schema

### A. Global Aggregate Document
* **Path**: `/media_reactions/{mediaType.lowercase()}_{mediaId}`
* **Example**: `/media_reactions/movie_550`

```json
{
  "totalReactions": 2660,
  "tagCounts": {
    "mind_bending": 1420,
    "plot_twist": 890,
    "comfort_watch": 350
  },
  "updatedAt": "2026-08-28T18:30:00Z"
}
```

### B. Per-User Reaction Document
* **Path**: `/user_media_reactions/{userId}_{mediaType.lowercase()}_{mediaId}`
* **Example**: `/user_media_reactions/user_john_doe_gmail_com_movie_550`

```json
{
  "selectedTags": [
    "mind_bending",
    "plot_twist"
  ],
  "updatedAt": "2026-08-28T18:30:00Z"
}
```

---

## 4. Frictionless Identity Management

To ensure zero barriers to community interaction:
1. **Authenticated Users**:
   - If signed in via Google Auth (`GoogleAuthClient.currentUser`), `userId = "user_" + sanitizedEmail`.
2. **Anonymous / Guest Users**:
   - If not signed in, ShowTime auto-generates a high-entropy UUID stored in local DataStore (`community_prefs.pb`), format: `anon_<uuid>`.
   - Users can react immediately without signing in or hitting permission roadblocks.

```mermaid
flowchart LR
    A["Check GoogleAuthClient.currentUser"] -->|Logged In| B["userId = user_ + sanitizedEmail"]
    A -->|Logged Out| C["Check Local DataStore Installation ID"]
    C -->|Exists| D["userId = anon_ + cachedInstallationId"]
    C -->|New Device| E["Generate UUID -> Cache in DataStore -> anon_ + newId"]
```

---

## 5. 0ms Optimistic UI & Atomic Batching

### Execution Flow Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as ReactionPill Composable
    participant Repo as CommunityRepositoryImpl
    participant Memory as Optimistic StateFlow Cache
    participant Firestore as Cloud Firestore Engine

    User->>UI: Taps Mind-Bending Pill
    UI->>Repo: toggleMediaReaction(Movie, 550, MIND_BENDING)
    
    rect rgb(230, 245, 230)
    Note over Repo,UI: Phase 1: Local Optimistic Mutation (0ms)
    Repo->>Memory: Update counts (+1) and active selection
    Memory-->>UI: Emits updated MediaReactions
    UI-->>User: Plays Haptic & Animates 50% Percentage Badge
    end

    rect rgb(240, 240, 255)
    Note over Repo,Firestore: Phase 2: Background Atomic Batch Sync
    Repo->>Firestore: WriteBatch.commit()
    Note right of Firestore: FieldValue.increment(+1) on tagCounts and totalReactions
    Firestore-->>Repo: Batch Commit Acknowledged
    end

    opt Another user reacts on a remote device
    Firestore-->>Repo: SnapshotListener fires with updated global totals
    Repo-->>UI: UI re-renders with new live percentages
    end
```

### Race Condition Prevention: `FieldValue.increment`
Rather than performing client-side Read-Modify-Write cycles (which fail under heavy concurrency), ShowTime leverages atomic database operations:
* `FieldValue.increment(1L)` on selection.
* `FieldValue.increment(-1L)` on deselection.
* `FieldValue.arrayUnion(tag.tagKey)` & `FieldValue.arrayRemove(tag.tagKey)` for set-based idempotency.

---

## 6. Dynamic Percentage & Total Calculations

In `MediaReactions.kt`, dynamic percentages and formatting are calculated on-the-fly:

$$\text{Percentage}(tag) = \begin{cases} \left( \frac{\text{tagCount}}{\text{totalReactions}} \right) \times 100 & \text{if } \text{totalReactions} > 0 \\ 0 & \text{if } \text{totalReactions} = 0 \end{cases}$$

* **Total Reaction Counter Badge**: Fades in with smooth animation (`fadeIn()` / `fadeOut()`) beside the section header when `totalReactions > 0`.
* **Percentage Badges**: Displayed inside active reaction pills to show the consensus breakdown across all users.

---

## 7. Offline Resilience & Persistence
* **Persistent Cache**: Firestore's `PersistentCacheSettings` is enabled in `CommunityModule`, ensuring all previously fetched reaction maps remain readable offline.
* **Network Reconnection**: Any pending writes made while offline are queued locally by Firestore SQLite engine and synced automatically once internet connectivity is restored.
