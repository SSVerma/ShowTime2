# ShowTime: AI Agent & Developer Directives

This document defines the mandatory engineering protocol for all AI assistants (Antigravity, Cursor, Claude Code, GitHub Copilot, Windsurf, etc.) and human developers working on the ShowTime codebase.

---

## 1. Master Protocol: Three Foundational Authorities

Before planning, creating, or modifying any code in this repository, you **MUST** consult and strictly adhere to three core documents:

### A. Architecture & Module Design (Phase 1: Planning & System Design)
* **Authoritative Guide**: [`docs/MODULAR_ARCHITECTURE_AND_CAPABILITY_TAXONOMY.md`](docs/MODULAR_ARCHITECTURE_AND_CAPABILITY_TAXONOMY.md)
* **When to consult**:
  - Whenever you are deciding where a new class, function, or capability belongs.
  - Whenever adding or refactoring dependencies across Gradle modules.
  - Whenever considering placing a component in `common-ui` or `shared-*`.
* **Mandatory Rules**:
  - Evaluate placement against the **Capability Placement Decision Tree** (Section 2).
  - Any component targeted for `common-ui` **MUST satisfy all 4 criteria** of the **4-Gate Admission Test** (Section 5.A).
  - Observe the **Allowed vs. Forbidden Dependencies Matrix** (Section 5.B): `common-ui` and UI modules must NEVER depend on `shared-data` or `feature-*` implementation modules.
  - Cross-cutting capabilities (like Ads, Analytics) must live in their dedicated twin modules (`core-ads` + `shared-ads`, `core-analytics` + `shared-analytics`) and must NEVER bloat `shared-ui` or `common-ui`.
  - Feature navigation is strictly routed via `NavKey` in `feature-*-navigation` modules; non-backstack capabilities must never create artificial navigation modules.

### B. Compose Performance & Compiler Stability (High-Performance Rendering)
* **Authoritative Guide**: [`docs/COMPOSE_PERFORMANCE_AND_STABILITY_GUIDE.md`](docs/COMPOSE_PERFORMANCE_AND_STABILITY_GUIDE.md)
* **When to consult**:
  - Whenever designing or authoring UI State models, Lazy lists, animations, or composable functions.
* **Mandatory Rules**:
  - **Compiler Stability**: All UI state data classes MUST be annotated with `@Immutable` (to guarantee stability for nested `List<T>` collections and enable skippable recomposition).
  - **Phased State Reads**: Defer rapidly changing state reads to layout/draw phases using lambda modifiers (`Modifier.offset { ... }`, `Modifier.graphicsLayer { ... }`) to avoid recomposition loops.
  - **`derivedStateOf`**: Wrap high-frequency observables (like scroll state index) in `derivedStateOf` when driving UI thresholds.
  - **Lazy Lists**: Always provide explicit stable `key` and `contentType` on every `items(...)` block.
  - **Coil Image Downsampling**: Always downsample remote images with `.size(width, height)`.

### C. Code Quality, Design System & Security (Phase 2: Implementation & Verification)
* **Authoritative Guide**: [`docs/CODE_QUALITY_AND_SECURITY_GUIDE.md`](docs/CODE_QUALITY_AND_SECURITY_GUIDE.md)
* **When to consult**:
  - While authoring any Kotlin, Compose, or XML file.
  - Before committing or finishing any code task.
* **Mandatory Rules**:
  - **File Size Hard Cap (Section 6.E)**:
    - No source file may exceed **~300–400 lines of code**. Monolithic 1000+ line files are strictly forbidden.
    - Screens and sheets are lean orchestrators (~150–250 lines); extract layout sections and dialogs into a dedicated `component/` subpackage.
  - **Reuse Discovery Protocol (Section 6.E)**:
    - Always search `shared-ui` and `common-ui` before creating new UI components.
  - **Lean ViewModels & UseCases (Section 6.F)**:
    - ViewModels must be lean (~200–300 lines) with UI State and Actions in separate files; offload calculations into `processor/` or `mapper/` subpackages.
    - UseCases follow Single Responsibility Principle with a single `operator fun invoke(...)`.
  - **Design System & Token Purity (Section 2)**:
    - Zero hardcoded hex colors: Use semantic `MaterialTheme.colorScheme.*` tokens.
    - Zero arbitrary magic numbers for padding/spacing: Use `MaterialTheme.spacing.*` tokens.
    - Surface/Card ripples: Never use `Modifier.clickable` on container surfaces; always use container `onClick = { ... }`.
  - **Localization & Accessibility (Section 3)**:
    - Zero hardcoded English strings: All user-facing strings must reside in `res/values/strings.xml`.
    - Every interactive icon and button must have a meaningful `contentDescription`.
  - **Code Hygiene (Section 4)**:
    - Zero wildcard imports (`import foo.bar.*` is forbidden).
    - Zero inline fully qualified class names (must use top-level imports).
    - Use named arguments for multi-parameter calls.
    - Encapsulate callbacks with >1 parameter into dedicated `*Args` data classes.
  - **Dumb UI Principle (Section 5.E)**:
    - Zero data transformations, calculations, date parsing, regex, or sorting in composables. All state must be pre-calculated in ViewModels/Domain on `Dispatchers.Default`.
  - **Open-Source Security & Zero Hardcoded Data (Sections 7 & 8)**:
    - Zero secrets committed: use gitignored `core.properties` with sanitized `core.properties.example` template (BYOK pattern).
    - All non-entry Android components must declare `android:exported="false"`.
    - Zero synthetic mock data in production builds (`src/main`).
  - **Pre-Commit Verification (Section 9)**:
    - Every change must pass `git add -A && ./.githooks/pre-commit` and `./gradlew testDebugUnitTest`.

---

## 2. Environment & Tooling Constraints

1. **NO ADB Commands**: Never execute `adb` or attempt to connect to Android emulators/devices directly.
2. **Deterministic Quality Verification**: Always run `git add -A && ./.githooks/pre-commit` before presenting completed code to the user.
