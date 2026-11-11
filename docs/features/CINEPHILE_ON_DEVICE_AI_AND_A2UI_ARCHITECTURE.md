# Cinephile AI & Dynamic Agent-to-UI (A2UI) Architecture

## 1. Executive Summary & Capabilities

**Cinephile AI** is ShowTime's native on-device film concierge, built on top of **Google Gemini Nano** via the **ML Kit GenAI Agent API**. It allows users to discover movies, explore complex cinema vibes, query directors and eras, and manage their watchlists entirely on-device with zero server latency, zero cloud costs, and complete privacy.

Instead of outputting plain text or static Markdown, Cinephile AI leverages **Agent-to-UI (A2UI)**: a streaming JSON protocol that compiles agent responses into rich, interactive Jetpack Compose components (interactive media carousels, rich movie cards, quick-action chips, and one-tap watchlist buttons).

### Core Capabilities Matrix

| User Query / Intent | Agent Autonomous Action | Rendered A2UI Component | User Action Affordance |
| :--- | :--- | :--- | :--- |
| *"Find me noir psychological thrillers from the 70s"* | Executes `discoverMoviesByFilter(genre="Thriller", era="1970s")` | `A2UiComponent.MediaCarousel` with canonical TMDB poster art and ratings | Tap poster to open `MovieDetailNavKey`; tap "Add to Watchlist" button |
| *"Who directed Interstellar and what else did they make?"* | Explains Christopher Nolan's style and fetches filmography | `A2UiComponent.Text` + `A2UiComponent.MediaCard` for *Oppenheimer*, *Inception* | Tap to view movie detail or explore director credits |
| *"I need something funny and uplifting for tonight"* | Resolves vibe query to comedy genre and mood chips | `A2UiComponent.ChipGroup` ("Feel Good", "Romantic Comedy", "Animation") | Tap chip to immediately send contextual follow-up prompt |
| *"Add The Dark Knight to my watchlist"* | Emits direct `A2UiAction.AddWatchlist` | Interactive action button indicating added status | Directly saves to `LibraryRepository` locally |
| *Multi-turn conversation with topic pivot* | Taps "+ New Chat" in assistant header | Session memory resets; previous conversation saved to Room database | Thread preserved in "Past Conversations" dropdown |

---

## 2. Multi-Tier Modular Architecture

The architecture strictly adheres to ShowTime's [Modular Architecture & Capability Taxonomy](../MODULAR_ARCHITECTURE_AND_CAPABILITY_TAXONOMY.md):

```mermaid
graph TD
    subgraph UI & Application ["UI & App Tier"]
        App[":app<br/>ShowTimeBottomBar (Floating Pill & Circular AI Button)<br/>ShowTime (Scroll Coordination & Sheet State)"]
        CommonUi[":common-ui<br/>CinephileAssistantBottomSheet<br/>CinephileAssistantViewModel<br/>A2UiComposeRenderer"]
    end

    subgraph Intelligence ["Application Intelligence Tier"]
        SharedAi[":shared-ai<br/>CinephileAssistantService<br/>A2UiParser & A2UiModels<br/>CinephileMovieTools (AppFunctions)<br/>CinephileChatRepository (Room DB)"]
    end

    subgraph CorePlumbing ["Platform Infrastructure Tier"]
        CoreAi[":core-ai<br/>OnDeviceAgentEngine<br/>MlKitOnDeviceAgentEngine (Gemini Nano / AICore)<br/>AiModelAvailability & AiAgentEvent"]
    end

    subgraph ConfigAndDomain ["Config & Domain Tier"]
        SharedDomain[":shared-domain<br/>AppConfigRepository (isCinephileAiEnabled)<br/>MovieRepository & LibraryRepository"]
        SharedData[":shared-data<br/>DefaultAppConfigRepository (Local + Remote CCM)"]
    end

    App --> CommonUi
    App --> SharedAi
    CommonUi --> SharedAi
    CommonUi --> SharedDomain
    SharedAi --> CoreAi
    SharedAi --> SharedDomain
    SharedData --> SharedDomain
```

### Module Responsibilities:

1. **`:core-ai` (Platform Infrastructure Tier)**:
   - Low-level wrapper around `com.google.android.gms:play-services-mlkit-genai-agent`.
   - Domain-agnostic contracts: `OnDeviceAgentEngine`, `OnDeviceAgentSession`, `AiModelAvailability`, `AiAgentEvent`.
   - Zero dependencies on TMDB, movie models, or UI code.
2. **`:shared-ai` (Application Intelligence & Grounding Tier)**:
   - Houses the Cinephile persona instructions, tool definitions (`CinephileMovieTools`), and streaming JSON parser (`A2UiParser`).
   - TMDB Catalog Grounding: query-to-filter mapping and poster URL verification to prevent hallucinations.
   - Local Room persistence: `CinephileChatDatabase` storing threads and messages.
3. **`:common-ui` (Stateful Dynamic UI Composite Tier)**:
   - `CinephileAssistantBottomSheet`: Smooth slide-up sheet with Gemini pill input, thinking animations, and history menu.
   - `CinephileAssistantViewModel`: State coordination, thread switching, and feature-flag gating.
   - Component Renderers: `A2UiMediaCardRenderer`, `A2UiCarouselRenderer`, `A2UiChipGroupRenderer`, `A2UiActionButtonRenderer`.
4. **`:app` (Entry Point & UX Coordination)**:
   - Hosts the Google Photos-style floating bottom bar (`ShowTimeBottomBar`).
   - Connects navigation routes (`MovieDetailNavKey`, `TvShowDetailNavKey`).

---

## 3. Core AI Engine & AICore Lifecycle (`core-ai`)

The on-device model runs entirely through Google's Android AICore system service. `MlKitOnDeviceAgentEngine` manages model readiness and execution:

```mermaid
stateDiagram-v2
    [*] --> FeatureFlagCheck: App Launched
    FeatureFlagCheck --> Disabled: isCinephileAiEnabled == false
    Disabled --> [*]: AI Button Hidden / AICore Untouched

    FeatureFlagCheck --> CheckAICore: isCinephileAiEnabled == true
    CheckAICore --> Unavailable: AICore missing / Incompatible SOC
    CheckAICore --> Downloading: Model weights downloading in background
    CheckAICore --> Available: Gemini Nano ready in memory

    Unavailable --> AIButtonHidden
    Downloading --> AIButtonDisabledWithProgress
    Available --> AIButtonInteractive
```

### Core AI Runtime Interaction Architecture

The diagram below illustrates the complete end-to-end runtime interaction loop between the user, UI layer, intelligence service, autonomous AppFunctions tools, platform engine, and on-device Gemini Nano execution in AICore:

```mermaid
flowchart TD
    subgraph UI ["Compose Presentation Layer (common-ui)"]
        UserPrompt["User Prompt<br/>(e.g., 'Recommend sci-fi thrillers')"]
        ViewModel["CinephileAssistantViewModel<br/>• Observes isCinephileAiEnabled<br/>• Checks checkAvailability()<br/>• Holds CinephileAssistantUiState"]
        BottomSheet["CinephileAssistantBottomSheet<br/>• Animated Thinking & Generating Badge<br/>• Tool Execution Chip ('Searching catalog...')"]
        A2UiRenderer["A2UiComposeRenderer<br/>• A2UiMediaCardRenderer<br/>• A2UiCarouselRenderer<br/>• A2UiActionButtonRenderer"]
    end

    subgraph ServiceLayer ["Intelligence & Grounding Tier (shared-ai)"]
        Service["CinephileAssistantService<br/>• Configures CinephileAgentConfig<br/>• Manages OnDeviceAgentSession Cache"]
        Tools["CinephileMovieTools (AppFunctions)<br/>• search_movies()<br/>• discover_movies_by_filter()"]
        TMDBRepo["MovieRepository / TMDB Client<br/>• Fetches verified movie metadata<br/>• Programmatic posterPath hydration"]
        Parser["A2UiParser<br/>• LIFO Bracket Balancer<br/>• Fault-tolerant Partial JSON Compiler"]
        RoomDB[("CinephileChatDatabase (Room)<br/>• cinephile_chat_threads<br/>• cinephile_chat_messages")]
    end

    subgraph CoreAiTier ["Platform Infrastructure Tier (core-ai)"]
        Engine["MlKitOnDeviceAgentEngine<br/>(Implements OnDeviceAgentEngine)"]
        Session["MlKitOnDeviceAgentSession<br/>(Implements OnDeviceAgentSession)"]
        EventFlow["Flow&lt;AiAgentEvent&gt;<br/>• Token<br/>• ToolStarted<br/>• ToolCompleted<br/>• Error"]
    end

    subgraph SystemLayer ["Android OS & Hardware Tier"]
        PlayServices["Google Play Services ML Kit<br/>(play-services-mlkit-genai-agent)"]
        AICore["Android AICore System Service<br/>(com.google.android.aicore)"]
        NanoModel[("Gemini Nano<br/>(On-Device NPU / GPU Execution)")]
    end

    UserPrompt --> ViewModel
    ViewModel --> Service
    ViewModel --> RoomDB
    Service --> Engine
    Engine --> Session
    Session --> PlayServices
    PlayServices --> AICore
    AICore --> NanoModel

    NanoModel -.->|Autonomous FunctionCall| PlayServices
    PlayServices -.->|FunctionCall Event| Session
    Session -.->|ToolStarted Event| EventFlow
    EventFlow -.->|Trigger Tool| Tools
    Tools --> TMDBRepo
    TMDBRepo -.->|Verified Metadata| Tools
    Tools -.->|FunctionResponse| Session
    Session -.->|FunctionResponse| PlayServices
    PlayServices -.-> NanoModel

    NanoModel -->|Stream Chunks| PlayServices
    PlayServices -->|Stream Tokens| Session
    Session -->|Tokens Flow| EventFlow
    EventFlow -->|Tokens Stream| Parser
    EventFlow -->|Status Updates| ViewModel
    ViewModel -->|StateFlow&lt;UiState&gt;| BottomSheet
    Parser -->|Compiled A2UiDocument| ViewModel
    ViewModel -->|A2UiDocument| A2UiRenderer
```

### Event Streaming Architecture:
`OnDeviceAgentSession.sendStreamingPrompt()` returns `Flow<AiAgentEvent>`:
- `AiAgentEvent.Token(text)`: Incremental output chunk from Gemini Nano.
- `AiAgentEvent.ToolStarted(toolName)`: Emitted when the agent invokes an autonomous tool (e.g., `search_movies`). The UI displays an animated pulsing badge ("Searching movie catalog...").
- `AiAgentEvent.ToolCompleted(toolName)`: Emitted when the tool returns data to the model.
- `AiAgentEvent.Error(message, isRecoverable)`: Emitted if runtime context bounds are exceeded or hardware throttling occurs.

---

## 4. Dynamic Agent-to-UI (A2UI) Protocol & Tolerant Parser

Rather than forcing the model to format tables or markdown links, Cinephile AI instructs Gemini Nano to output an ````a2ui ... ```` code fence wrapping valid JSON components.

### Component Hierarchy:
- **`A2UiDocument`**: Root document containing a list of `A2UiComponent`s.
- **`A2UiComponent.Text`**: Rendered text paragraph with typography and color styling.
- **`A2UiComponent.MediaCard`**: Single film card with poster, year, rating, and overview.
- **`A2UiComponent.MediaCarousel`**: Horizontal scrollable row of film cards.
- **`A2UiComponent.ChipGroup`**: Horizontal flow of suggested prompt chips.
- **`A2UiComponent.ActionButton`**: Contextual action (e.g. "Add to Watchlist").

### Fault-Tolerant LIFO Bracket Balancing:
During streaming, Gemini Nano generates partial JSON. Standard JSON parsers (Moshi, Gson, Jackson) throw exceptions on incomplete tokens.

`A2UiParser` incorporates a custom LIFO syntax stack parser:
1. Tracks open `{`, `}`, `[`, `]`, and string quotes.
2. Closes unclosed quotes and appends missing closing braces/brackets to synthesize a structurally valid JSON string at every partial emission.
3. Automatically strips preamble conversational text preceding the ````a2ui` block and trailing thought signatures.

---

## 5. Catalog Grounding & Hallucination Defense

Large Language Models frequently hallucinate release dates, cast members, and poster URLs. Cinephile AI implements a **Two-Tier Defense**:

```mermaid
sequenceDiagram
    actor User
    participant VM as CinephileAssistantViewModel
    participant Svc as CinephileAssistantService
    participant Nano as Gemini Nano (AICore)
    participant TMDB as MovieRepository (TMDB)

    User->>VM: "Recommend sci-fi thrillers from 2024"
    VM->>Svc: sendPrompt(...)
    Svc->>Nano: Prompt with CinephileMovieTools
    Nano->>Svc: FunctionCall: discover_movies(genre="Sci-Fi", year=2024)
    Svc->>TMDB: discoverMovies(genre="878", year=2024)
    TMDB-->>Svc: [Movie(id=101, title="Dune: Part Two", poster="/path.jpg")]
    Svc->>Nano: FunctionResponse: Verified film metadata
    Nano-->>Svc: Emits A2UiDocument JSON
    Svc->>Svc: Programmatic Poster Hydration (verify posterPaths against TMDB)
    Svc-->>VM: CinephileResponseState.Success(A2UiDocument)
    VM-->>User: Renders A2UiMediaCarousel with verified poster art
```

1. **Tool Grounding (`CinephileMovieTools`)**:
   - The model calls `search_movies` and `discover_movies` via Google ML Kit AppFunctions annotations.
   - It cannot invent fake IDs or fake titles because it must query the verified TMDB catalog.
2. **Programmatic Poster Hydration (`enrichDocumentWithPosters`)**:
   - If the model emits an empty `posterPath` or hallucinated URL, `CinephileAssistantService` performs a fast lookup in the TMDB database by `mediaId` or normalized `title`, injecting the verified `/cdn-path.jpg`.

---

## 6. Multi-Turn Conversation & Room Persistence

Conversations are persisted locally using SQLite via Android Room in `cinephile_ai_chat.db`.

### Database Schema:
- **`cinephile_chat_threads`**:
  - `id` (String UUID, Primary Key)
  - `title` (String, derived from first prompt)
  - `created_at` (Long epoch millis)
  - `updated_at` (Long epoch millis)
- **`cinephile_chat_messages`**:
  - `id` (String UUID, Primary Key)
  - `thread_id` (String, Foreign Key with `CASCADE` delete)
  - `sender` (String: `USER` or `ASSISTANT`)
  - `text` (String message content)
  - `a2ui_json` (String nullable, serialized `A2UiDocument`)
  - `timestamp` (Long epoch millis)

### Session Context Isolation:
When the user taps "+ New Chat":
1. The active `threadId` in `CinephileAssistantViewModel` is cleared.
2. A new `OnDeviceAgentSession` is allocated in `CinephileAssistantService`.
3. Gemini Nano starts with a clean memory buffer, eliminating prompt pollution from earlier disparate topics.

---

## 7. Remote Kill-Switch & Feature Flag Gating

ShowTime enforces complete remote configuration control via `AppConfigRepository`:

- **Remote Config Key**: `cinephile_ai_enabled` (Boolean, default `true`).
- **DataStore Key**: `cinephile_ai_enabled`.
- **Implementation**:
  ```kotlin
  override val isCinephileAiEnabled: Flow<Boolean>
      get() = combine(
          keyValueStorage.observe(CinephileAiEnabledKey, true),
          appConfigProvider.observeBoolean(KEY_CINEPHILE_AI_ENABLED, true)
      ) { local, remote -> local && remote }
  ```

### Critical Zero-Footprint Guarantee:
If `isCinephileAiEnabled` evaluates to `false`:
1. `CinephileAssistantViewModel` **never calls `assistantService.checkAvailability()`**.
2. **Gemini Nano / AICore is NEVER initialized, loaded into RAM, or queried**.
3. In `ShowTimeBottomBar`, the floating circular AI button is omitted, and the main navigation pill expands to fill the bottom bar space smoothly without layout jitter.

---

## 8. UX Design: Floating Navigation Entry Point

The entry point is inspired by the modern **Google Photos / M3 Expressive Floating Bar**:

1. **Floating Pill + Detached AI Action Button**:
   - The navigation destinations sit inside an elevated capsule pill.
   - Detached 10dp to the right is a 56dp circular action button (`Icons.Rounded.AutoAwesome`) providing high-visibility access to Cinephile AI.
2. **Zero Text Truncation Guarantee**:
   - Inactive tabs show icons only (`weight = 1.0f`).
   - The active tab smoothly expands (`weight = 1.6f`) revealing Icon + Label with animated text fade-in.
   - This eliminates text clipping ("Libr", "Ho") on small screens while retaining a compact footprint.
3. **Scroll-Aware Slide Animations**:
   - Connected via `NestedScrollConnection` in `ShowTime.kt`.
   - Scrolling down slides both the navigation pill and the AI button off-screen.
   - Scrolling up reveals them immediately.

---

## 9. Verification & Quality Assurance

The Cinephile AI subsystem is rigorously tested across all architectural layers:

| Test Suite | Location | Verification Focus | Status |
| :--- | :--- | :--- | :--- |
| `A2UiParserTest` | `:shared-ai` | Streaming JSON bracket balancing, chunk completion, malformed recovery | Passed (4/4) |
| `CinephileAssistantServiceTest` | `:shared-ai` | TMDB grounding, poster hydration, session management | Passed (3/3) |
| `CinephileChatRepositoryTest` | `:shared-ai` | Room DB entity mapping, cascading thread deletes | Passed (2/2) |
| `CinephileAssistantViewModelTest` | `:common-ui` | Remote feature flag gating, AICore availability checks, thread switching | Passed (5/5) |
| Device Verification | Physical Device | Deployed and verified end-to-end on Android device running AICore | Verified |
