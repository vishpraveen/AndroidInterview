# Answers — §14 Miscellaneous and Leadership

Questions: [questions.md](../questions.md#14-miscellaneous-and-leadership).

_Leadership prompts use a **compact STAR** (one line each) inside the cap where useful._

---

### Technical Leadership

**1.** Legacy 6y codebase: Java, RxJava, MVP, no tests — modernization roadmap.

- [x]

- **Thesis:** **Stabilize** first: CI, lint, crash baseline, **characterization tests** on hotspots; **strangler** migrate **vertical slices** to Kotlin+Coroutines+MVVM+Compose optional; **parallel** keep shipping via **feature flags**; **deprecate** Rx on touched modules (`asFlow` bridges); **module boundaries** then **delete** dead MVP presenters.
- **STAR:** **S** aging app blocking hires; **T** safe modernization without stop-the-line; **A** CI+tests-on-touch+KMP-ready core extraction roadmap quarters; **R** crash rate down, lead time stable, team velocity up measurable.

**2.** Effective code reviews beyond syntax.

- [x]

- **Thesis:** Check **architecture fit** (layering, DI scope), **performance** (main thread, allocations in hot paths), **security** (exports, logging PII), **testability** (seams, pure functions), **UX edge** (empty/error), **telemetry** updates, **docs** ADR link if architectural.
- **STAR:** **S** regressions escaped review; **T** raise review bar without slowing merges; **A** checklist + SLO-linked comments + mentor pairing; **R** defect rate −X% quarter.

**3.** Teammate wants MVVM→MVI mid-project — evaluate.

- [x]

- **Thesis:** Score **cost** (training, rewrite risk, test rewrite) vs **benefit** (debuggability for specific pain—racey state?). Prefer **pilot** one flow + metrics; reject if **deadline** critical without staffing; adopt if **state complexity** truly hurting.
- **STAR:** **S** inconsistent event handling bugs; **T** decide pattern; **A** spike 2-week screen + retro; **R** chose MVI for checkout only, MVVM elsewhere documented.

**4.** Balance features vs tech debt.

- [x]

- **Thesis:** **Allocate capacity** (e.g., 20% each sprint), **debt backlog** visible prioritized by **risk × interest**; **link** debt paydown to **feature reliability** narratives for PM buy-in; **stop-the-line** rules for **S0** stability issues.
- **STAR:** **S** velocity slowing from flaky CI; **T** reclaim capacity; **A** fixed CI + caching + debt budget in OKRs; **R** cycle time −30%.

**5.** Mentor juniors — learning path.

- [x]

- **Thesis:** Path: **Kotlin** → **Coroutines+Flow** → **Android fundamentals lifecycle** → **Compose** → **architecture (MVVM)** → **testing pyramid** → **performance/security** modules; pair on **real tickets** with **review feedback loop**; readings: official docs + **Now in Android**.
- **STAR:** **S** junior blocked often; **T** ramp in 90 days; **A** curriculum + weekly pairing + small owned features; **R** independent shipping medium stories.

---

### API Design and SDK Authoring

**6.** Internal SDK for 10 teams — API principles.

- [x]

- **Thesis:** **Backward compatibility** by default; **semantic versioning**; **minimal** public surface; **documented** threading contracts; **binary compatibility** checks (Metalava); **dogfood** sample app; **SLA** for breaking changes with migration guides.
- **Mechanism / order:** `@RequiresOptIn` for experimental APIs.
- **Edge cases:** **ProGuard consumer rules** shipped tested.
- **Production hook:** **Binary compatibility validator** in CI.
- **When wrong:** Frequent breaking semver minors—trust erosion.

**7.** SDK versioning; semver; deprecation.

- [x]

- **Thesis:** **MAJOR** breaking binary/source; **MINOR** additive; **PATCH** fixes; **deprecate** with **replacement** + **sunset timeline**; `@Deprecated(message=..., replaceWith=...)`.
- **Mechanism / order:** **Keep** old symbols one major cycle minimum internal policy.
- **Edge cases:** **Kotlin inline** changes can be binary breaking unexpectedly—Metalava catches.
- **Production hook:** **CHANGELOG** automation from conventional commits.
- **When wrong:** Silent behavior change without version bump.

**8.** Library public API surface; Kotlin visibility; ProGuard.

- [x]

- **Thesis:** Default **`internal`** for impl packages; **`public`** only `api` module; **`@Keep`** only where reflection; ship **`consumer-rules.pro`** for Gson models etc.; **KDoc** samples compile-tested.
- **Mechanism / order:** **`explicitApi()`** mode on `api` modules.
- **Edge cases:** **Inline** functions expand into consumer bytecode—semver implications.
- **Production hook:** **API lint** (Metalava) `public`, `system`, `removed` tracking.
- **When wrong:** Accidentally `public` `internalRepository` in `api` jar.

---

### Migration Strategies

**9.** Java→Kotlin incremental; interop pitfalls.

- [x]

- **Thesis:** **New code Kotlin**, convert touched Java; **`@JvmField`**/`lateinit` interop; **nullability** annotations on Java consumed by Kotlin as **platform types**—annotate `@Nullable/@NonNull`; watch **`static` + default interface** methods; **Remove `-Xjvm-default=all`** timelines; **Kotlin test** calling Java package-private tricky.
- **Mechanism / order:** **Android Studio** convert then **human review** `!!`.
- **Edge cases:** **SAM** conversions subtle overloads.
- **Production hook:** **Detekt** enforce new files `.kt`.
- **When wrong:** Mass automated conversion PR 50k lines unreviewable.

**10.** XML→Compose incremental; migrate first.

- [x]

- **Thesis:** Start **islands** new screens Compose; **interleave** `ComposeView` in RecyclerView rows; migrate **design system** first; **navigation** unify; **complex custom views** last (Map, Camera) via `AndroidView`.
- **Mechanism / order:** **Shared ViewModel** between XML/Compose during transition.
- **Edge cases:** **Fragment** `FragmentComposeView` back stack saved state.
- **Production hook:** **Feature flags** per screen compose toggle.
- **When wrong:** Rewriting stable low-change admin screens first—low ROI.

**11.** RxJava→Coroutines/Flow mapping.

- [x]

- **Thesis:** `Observable`→`Flow` via `asFlow`/`callbackFlow`; `single`→`suspend`; `flatMap`→`flatMapMerge`; `switchMap`→`flatMapLatest`; `combineLatest`→`combine`; `zip`→`zip`; **schedulers**→`dispatchers`; **Disposable`→`Job` cancellation.
- **Mechanism / order:** Bridge at **repository** boundary first.
- **Edge cases:** **Cold vs hot** Rx nuances map carefully (`publish` etc.).
- **Production hook:** `rxkotlin` interop or kotlinx-coroutines-rx3 adapters.
- **When wrong:** `runBlocking` bridging everywhere.

**12.** kapt→KSP; what still needs kapt.

- [x]

- **Thesis:** Move **Room/Moshi/Hilt** where supported to **KSP**; **Dagger/Hilt** historically **kapt** for component generation—check current matrix; **Glide** processor etc.
- **Mechanism / order:** `ksp { arg("room.schemaLocation", ...) }` same as kapt.
- **Edge cases:** **DataBinding/ViewBinding** still AGP processors not KSP path same.
- **Production hook:** Gradle build scan compare **task times** before/after.
- **When wrong:** Removing kapt while Hilt still requires it—build break.

---

### Emerging Topics

**13.** KMP vs Flutter/RN; when KMP.

- [x]

- **Thesis:** **KMP** shares **Kotlin** business logic + optional UI (CMP) with **native** UX per platform; **Flutter** single UI engine canvas; **RN** JS bridge ecosystem. Choose KMP when **Android+iOS** teams Kotlin-skilled, need **native** integrations performance, incremental adoption.
- **Edge cases:** **Build** complexity vs Flutter speed to generic UI.
- **Production hook:** Start **network/models** shared only.
- **When wrong:** KMP for identical simple CRUD app—Flutter might ship faster.

**14.** Compose Multiplatform; shared UI scope.

- [x]

- **Thesis:** **CMP** shares **Compose UI** across Android/Desktop/iOS (Skiko); **iOS** interop bridges UIKit; **Web** experimental maturing; limitations: **platform-specific** modifiers, **expect/actual** for sensors, **resource** pipeline differs.
- **Production hook:** Evaluate **2025+** maturity release notes per target.
- **When wrong:** Expect pixel-perfect iOS HIG compliance from shared Material only.

**15.** Gemini Nano; Android `AICore`.

- [x]

- **Thesis:** **On-device LLM** small model via **Google AI Edge** / **AICore** (where available) reduces latency/privacy for **classification/summarization**; **Play services** delivered model updates; **API** surfaces via **GenAI SDK** patterns evolving—verify current package names in docs.
- **Edge cases:** **Device eligibility** fragmented; **fallback** cloud.
- **Production hook:** Feature detect + degrade gracefully.
- **When wrong:** Blocking UX on model download without Wi-Fi consent.

**16.** Health Connect — when to integrate.

- [x]

- **Thesis:** Central **privacy-first** hub for **fitness/medical** reads/writes replacing fragmented Google Fit APIs; integrate when app is **health/wellness** aggregator needing **standardized permissions** UX and **cross-app** data reads user consented.
- **Mechanism / order:** **Permissions** granular types; **background** read restrictions policy-heavy.
- **Edge cases:** **OEM** not shipping HC—availability check.
- **Production hook:** **Minimum APK** module optional dynamic delivery.
- **When wrong:** Pulling sensitive vitals without clear user value.

**17.** Adaptive layouts; foldables; `WindowSizeClass`.

- [x]

- **Thesis:** Use **`WindowSizeClass` breakpoints** (`compact/medium/expanded`) + **`Posture`/`FoldFeature`** APIs to choose **two-pane** vs **single-pane**; **drag-and-drop** across panes; **ChromeOS** resizable free-form windows—**responsive** constraints not fixed dp widths only.
- **Mechanism / order:** Compose **`calculateWindowSizeClass`**, Material3 adaptive nav scaffold.
- **Edge cases:** **Hinge** occlusion insets for fold inner screen.
- **Production hook:** **Screenshot tests** for `medium` width at least.
- **When wrong:** `values-sw600dp` only ignoring **fold state**.

**18.** Predictive Back (Android 14+); implement.

- [x]

- **Thesis:** System shows **preview** of previous destination during back gesture; app opts in **`android:enableOnBackInvokedCallback="true"`** (manifest) + **Navigation/Compose** `PredictiveBackHandler` / `OnBackPressedCallback` updates; **custom** animations via **Material Motion** `MaterialSharedAxis` etc.
- **Mechanism / order:** **Edge-to-edge** + back animation coordination.
- **Edge cases:** **WebView** internal history vs Android back—handle carefully.
- **Production hook:** Test on **API 34** emulator with gesture nav enabled.
- **When wrong:** Finishing activity on first back edge causing blank predictive preview.

---

### Process and Collaboration

**19.** Team structure: feature vs platform teams.

- [x]

- **Thesis:** **Feature teams** own vertical slices (Android+iOS+BE sometimes) faster product iteration; **platform team** owns **core SDK**, CI, performance, design system—reduces **duplication**. Hybrid: **embedded platform liaisons** in squads.
- **STAR:** **S** duplicated networking bugs; **T** consolidate expertise; **A** small platform guild + feature squads consume releases; **R** incident count down, velocity steady.

**20.** Documentation for shared codebase.

- [x]

- **Thesis:** **ADRs** for major decisions; **architecture diagram** C4 level; **onboarding** runbook (build, secrets, flags); **CODEOWNERS**; **README per module** boundaries; **Playbook** for releases/on-call.
- **Mechanism / order:** Docs live **near code** to stale less.
- **Edge cases:** **Generated API docs** from KDoc for public SDK modules.
- **Production hook:** Quarterly **doc freshness** ticket in sprint zero.
- **When wrong:** Confluence only outdated after 1 month.

**21.** Disagreements on technical decisions.

- [x]

- **Thesis:** **Align on goals** (risk, timeline, users); **gather data** (prototype, benchmarks); **decision log** ADR with **alternatives**; **disagree and commit** once decided; escalate **staff/arch** tie-break with **timebox**.
- **STAR:** **S** Compose vs XML debate; **T** pick default for new screens; **A** spike metrics build time + dev satisfaction survey; **R** ADR chose Compose new-only, XML maintained.

**22.** Release process: branching, trains, hotfix.

- [x]

- **Thesis:** **Trunk-based** or **GitFlow-lite**: `main` protected; **release branch** `release/x.y` cherry-picks; **weekly train** cut RC; **hotfix** branch from tag + merge back; **Play** internal→prod staged; **feature flags** decouple deploy from release.
- **Mechanism / order:** **Semantic version** tags drive CI promote.
- **Edge cases:** **Schema migrations** coordinated with server **API** version gates.
- **Production hook:** **Runbook** checklist sign-off roles Eng/QA/PM.
- **When wrong:** Hotfix directly on `main` without tag discipline.

**23.** Definition of Done for a feature.

- [x]

- **Thesis:** Code merged with **tests** (unit+UI as policy), **lint clean**, **analytics** events reviewed, **docs** updated, **feature flag** default safe, **accessibility** checklist (TalkBack, contrast), **security** review if sensitive, **rollout plan** with owner on-call, **translations** in or ticket filed.
- **STAR:** **S** “done” meant code merged only—bugs in prod; **T** tighten DoD; **A** checklist in PR template enforced by reviewers; **R** escaped defects down.
