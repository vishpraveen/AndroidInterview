# Answers — §3 Jetpack Compose — Deep Dive

Questions: [questions.md](../questions.md#3-jetpack-compose--deep-dive).

---

### Fundamentals and Mental Model

**1.** Declarative vs imperative Views; compiler “gap”.

- [x]

- **Thesis:** Compose **re-runs** `@Composable` functions to **describe** UI for current state; Views **mutate** a tree. The **compiler plugin** tracks **call sites**, **skips** stable unchanged work, and generates **remember** / **slot** machinery the runtime cannot infer from plain Kotlin alone.
- **Mechanism / order:** Compiler lifts composables into **restartable/movable** groups; bridges **gap** between Kotlin control flow and **incremental** UI updates.
- **Edge cases:** **Non-deterministic** composables break assumptions.
- **Production hook:** `ComposeCompilerReports` for **skipping** stats.
- **When wrong:** Side effects in composition body without effect APIs.

**2.** Compose compiler plugin; what it does to `@Composable`.

- [x]

- **Thesis:** Transforms composable calls into **restartable scopes**, inserts **composer** calls, tracks **state reads** for **invalidation**, applies **stability** inference.
- **Mechanism / order:** IR plugin (Kotlin 2.0+ **K2** path) emits **group keys**, **source info** for tooling.
- **Edge cases:** **Strong skipping mode** toggles stricter skip rules.
- **Production hook:** `-P` compiler metrics in CI for regressions.
- **When wrong:** `@Composable` on interface methods expecting override like Views—wrong model.

**3.** Compose phases: Composition, Layout, Drawing vs View pipeline.

- [x]

- **Thesis:** **Composition** builds/updates tree; **Layout** measures/places nodes; **Drawing** emits canvas records—**same conceptual** pipeline as Views but **finer-grained** invalidation.
- **Mechanism / order:** Recomposition can skip layout/draw if only data changed with same constraints; **intrinsics** still part of layout pass.
- **Edge cases:** **LookaheadLayout** two-phase layout.
- **Production hook:** Layout Inspector **recomposition counts**.
- **When wrong:** Assuming **one** full pass per frame always—often not.

**4.** Slot table; tracking composition tree.

- [x]

- **Thesis:** **Slot table** stores **group** metadata (keys, object anchors, state holders) enabling **O(1)** moves/restarts and **diff** of composition across frames.
- **Mechanism / order:** Composer writes slots during compose; **recomposition** walks slots comparing **invalidations**.
- **Edge cases:** **Movable content** APIs relocate subtrees.
- **Production hook:** Internal only—reason with **group** terminology in interviews.
- **When wrong:** Thinking runtime walks full **View-like** tree each frame.

---

### Recomposition and Performance

**5.** What triggers recomposition; structural vs referential equality for skipping.

- [x]

- **Thesis:** **State snapshot** changes (`mutableStateOf`, etc.) **invalidate** readers; compiler **skips** calling composables when **inputs unchanged** per **equals**—**structural** (`==`) for stable types, **referential** for unstable unless compiler proves stability.
- **Mechanism / order:** Read during composition **registers** dependency; write schedules **applyChanges**.
- **Edge cases:** **Unstable** `List` from default module → frequent recomposition.
- **Production hook:** `@Immutable` / `kotlinx.collections.immutable`.
- **When wrong:** New `data class` instance each frame with same values but no `@Stable` contract—no skip.

**6.** Stable types; `@Stable`, `@Immutable`, inference.

- [x]

- **Thesis:** **Stable** = **equals implies same UI** if public props equal; **Immutable** stronger (no mutability after publish). Compiler **infers** from file visibility + kotlin collections rules.
- **Mechanism / order:** `runtime` stability configuration file for **third-party** classes.
- **Edge cases:** Var properties break immutability guarantees.
- **Production hook:** Compose compiler **report** lists “unstable” parameters.
- **When wrong:** Slapping `@Stable` on mutable `ArrayList`—lying to compiler.

**7.** `LazyColumn` 1000 items janky — investigation.

- [x]

- **Thesis:** Profile **composition vs layout**; usual culprits: **unstable** item models, heavy work in `item {}`, **no keys**, **image decode** on main, **overdraw**, **nested scrollables**.
- **Mechanism / order:** Android Studio **Layout Inspector** recomposition; **Systrace** `Choreographer`; enable **strong skipping**; use **`key`**; hoist state; **`derivedStateOf`** for derived lists.
- **Edge cases:** `remember` inside item without stable inputs.
- **Production hook:** **Baseline profile** for `LazyList` scroll; **Coil** `AsyncImage` with size.
- **When wrong:** Using `Column` with scroll instead of `LazyColumn`.

**8.** `derivedStateOf`; when / overkill.

- [x]

- **Thesis:** Derives **read** from multiple states **without** recomposing readers unless **derived result** changes—reduces churn.
- **Mechanism / order:** Reads sources inside lambda tracked as dependency of **derived** state only.
- **Edge cases:** Cheap single `map` may not need it—measure first.
- **Production hook:** Use for **filter/sort** of large list from query+facet state.
- **When wrong:** Wrapping every trivial `a+b`—noise.

**9.** Snapshot system; `mutableStateOf`.

- [x]

- **Thesis:** **Snapshot** MVCC-like **transaction** for state reads/writes in composition; **mutableStateOf** registers **record** per read; writes **notify** observers atomically on **apply**.
- **Mechanism / order:** `Snapshot.sendApplyNotifications()` drives frame apply.
- **Edge cases:** **Thread** confinement—snapshot not thread-safe across arbitrary threads without `SnapshotThreadLocal` patterns.
- **Production hook:** `snapshotFlow { }` bridges to **Flow**.
- **When wrong:** Mutating state off main without snapshot rules—rare crashes.

**10.** `remember` vs `rememberSaveable`; types; custom saver.

- [x]

- **Thesis:** `remember` survives **recomposition** only; `rememberSaveable` survives **process death** + config via **`Saver`** + `Bundle` mechanics.
- **Mechanism / order:** Built-ins for primitives, `Parcelable`, `Bundleable`; custom: `Saver<T, Any>(save, restore)`.
- **Edge cases:** **Size** limits like any Bundle.
- **Production hook:** Prefer **ViewModel/SavedState** for non-UI critical state.
- **When wrong:** Storing **non-stable** graph objects in Saveable without custom saver.

---

### State Management

**11.** Hoisting vs ViewModel vs `rememberSaveable`.

- [x]

- **Thesis:** **Hoist** to lowest common ancestor for **UI ephemeral** state; **ViewModel** for **survives rotation**, business logic, **coroutine** scope; **rememberSaveable** for **UI** that must survive **process death** without VM (text field scroll).
- **Mechanism / order:** Single source of truth upward; events down.
- **Edge cases:** **Nav** back stack entry scoped VM for feature state.
- **Production hook:** UDF with `StateFlow` in VM + `collectAsStateWithLifecycle`.
- **When wrong:** VM references **Context**—leak.

**12.** `collectAsStateWithLifecycle()` vs `collectAsState()`.

- [x]

- **Thesis:** **Lifecycle-aware** collection starts at **`STARTED`**+ and cancels below—prevents **background** collectors wasting work / violating policy.
- **Mechanism / order:** Uses **`repeatOnLifecycle`** under the hood.
- **Edge cases:** `SharingStarted.Lazily` vs `WhileSubscribed` pairing.
- **Production hook:** Default for **Flow** from VM in Compose UI layer.
- **When wrong:** `collectAsState` on infinite hot Flow—battery drain when paused.

**13.** Share state far apart without prop drilling.

- [x]

- **Thesis:** **`CompositionLocal`**, **scoped ViewModel** (`hiltNavGraphViewModels`), **global store** (disciplined), **callback/event bus** (last resort).
- **Mechanism / order:** `compositionLocalOf` vs `staticCompositionLocalOf` default semantics.
- **Edge cases:** Testing locals require **providers** in tests.
- **Production hook:** Theme/density locals are canonical pattern.
- **When wrong:** `CompositionLocal` for **everything**—opaque dependencies.

**14.** `CompositionLocal`; custom examples; anti-pattern.

- [x]

- **Thesis:** Implicit DI of **ambient** values (theme, navigator). Good for **cross-cutting** stable providers; bad as **hidden global mutable state**.
- **Mechanism / order:** `ProvidableCompositionLocal` + `CompositionLocalProvider`.
- **Edge cases:** Defaults throwing—fail fast vs silent.
- **Production hook:** Provide **test doubles** in `@Preview`.
- **When wrong:** Business flags in locals changing unpredictably.

**15.** `State<T>` vs `MutableState<T>` vs `SnapshotStateList<T>`.

- [x]

- **Thesis:** `MutableState` value holder with **snapshot** tracking; `State` read-only facade; **`SnapshotStateList`** tracks **per-element** invalidations for list mutations without replacing whole list identity each time.
- **Mechanism / order:** Use **`mutableStateListOf`** for dynamic lists in composition.
- **Edge cases:** Mutate list off thread—same snapshot rules.
- **Production hook:** `toMutableStateList()` from collections.
- **When wrong:** `var list by remember { mutableStateOf(listOf()) }` + copy each add—extra allocations.

---

### Side Effects

**16.** Side-effect APIs: `LaunchedEffect`, `rememberCoroutineScope`, `DisposableEffect`, `SideEffect`, `produceState`, `derivedStateOf`, `snapshotFlow`.

- [x]

- **Thesis:** **`LaunchedEffect`**: suspend work tied to **keys** + lifecycle of composition. **`rememberCoroutineScope`**: manual launch tied to **composition cancel**. **`DisposableEffect`**: setup/teardown (listeners). **`SideEffect`**: publish compose state to **non-compose** owners each successful composition. **`produceState`**: async **State** builder. **`derivedStateOf`**: derived snapshot. **`snapshotFlow`**: push snapshot reads into **Flow**.
- **Mechanism / order:** Choose by **lifecycle** boundary needed.
- **Edge cases:** `LaunchedEffect(Unit)` runs once per **composition** lifetime—not process.
- **Production hook:** `DisposableEffect` for **LifecycleObserver** register.
- **When wrong:** `LaunchedEffect` without keys but depends on **changing** id—stale job.

**17.** `LaunchedEffect` key; key change behavior.

- [x]

- **Thesis:** **Key** tuple identity change **cancels** previous coroutine and **restarts** block—pattern for `userId`-scoped fetches.
- **Mechanism / order:** Compare with `equals`; use `Unit` for run-once per enter.
- **Edge cases:** Unstable objects as keys cause restart storms—use primitives.
- **Production hook:** `LaunchedEffect(viewModel.uiState.userId)`.
- **When wrong:** Omitting keys then wondering why effect never re-runs.

**18.** Observe `Flow` in composable with cancel on leave.

- [x]

- **Thesis:** **`collectAsStateWithLifecycle`** or `LaunchedEffect` + `repeatOnLifecycle` + `collect`—both cancel when composable leaves or lifecycle drops.
- **Mechanism / order:** `flow.collect` is suspending until cancelled.
- **Edge cases:** `callbackFlow` needs `awaitClose`.
- **Production hook:** Avoid `collect` in `rememberCoroutineScope` without lifecycle unless you manage cancel manually.
- **When wrong:** Global `scope.launch { flow.collect }` in `@Composable`—leak.

---

### Layout and Modifiers

**19.** Single-pass measurement; intrinsic measurement.

- [x]

- **Thesis:** Children measured **once** per pass unless intrinsics requested; **intrinsics** (`minIntrinsicWidth`, etc.) run **extra** queries for parents that need child size before child constraints known (e.g. `wrap` text in chip).
- **Mechanism / order:** `IntrinsicSize.Min/Max` modifiers trigger alternate measurement passes—**expensive**.
- **Edge cases:** `SubcomposeLayout` can measure twice by design.
- **Production hook:** Prefer **`Layout`/`Modifier`** designs avoiding intrinsics when possible.
- **When wrong:** Intrinsics inside `LazyColumn` items at scale—jank.

**20.** Modifier order — padding vs background example.

- [x]

- **Thesis:** Modifiers wrap **outward**; order matters: **`padding` then `background`** paints **margin** outside colored box? Actually **padding before background** applies background **inside** padded area—visual: red fills padded inner; reversed, background extends under padding depending on implementation—**padding after background** keeps inner content inset from colored edge.
- **Mechanism / order:** Read chain **left-to-right** as outer-to-inner for child; drawing order reversed.
- **Edge cases:** `clickable` placement affects touch target size.
- **Production hook:** Modifier factory docs diagrams.
- **When wrong:** Assuming commutative modifiers.

**21.** Custom layout; `Layout`; `MeasurePolicy`.

- [x]

- **Thesis:** **`Layout` composable** provides **measureables** + constraints; you implement **`MeasurePolicy`** to assign **placeables** with `placeRelative`.
- **Mechanism / order:** Call `measureable.measure(constraints)` then `layout(width,height) { placeables.place }`.
- **Edge cases:** **ZIndex** ordering in `place`.
- **Production hook:** Custom **flow** layouts, overlaid badges.
- **When wrong:** Measuring same child twice in one pass without `SubcomposeLayout`—assertions.

**22.** `SubcomposeLayout`; when; examples.

- [x]

- **Thesis:** Allows **subcomposition** where **child composition** depends on measured size of another—powers **`BoxWithConstraints`**, **`LazyColumn`** item measurement before full subtree known.
- **Mechanism / order:** `subcompose(slotId) { ... }` then measure resulting `Measurable`.
- **Edge cases:** **Cost**—use sparingly outside lazy machinery.
- **Production hook:** Implement **responsive** slot layouts.
- **When wrong:** Using for simple row/column—overkill.

**23.** `Modifier.Node` vs legacy `Modifier.composed`.

- [x]

- **Thesis:** **`Modifier.Node`** chain is **alloc-light**, **reusable** nodes with **typed** state vs `composed` factory allocating lambdas each recomposition in old patterns.
- **Mechanism / order:** Prefer **`Modifier.Node`** APIs in new modifiers (pointer input, etc.).
- **Edge cases:** Migration ongoing—some samples still `composed`.
- **Production hook:** Read **Modifier.Node** samples in Accompanist replacements.
- **When wrong:** `composed` capturing unstable captures each frame.

---

### Animations

**24.** Compare animation APIs.

- [x]

- **Thesis:** **`animate*AsState`**: simple **target** lerps. **`AnimatedVisibility`**: enter/exit transitions. **`AnimatedContent`**: keyed content crossfade/slide. **`Crossfade`**: lightweight content swap. **`updateTransition`**: orchestrate **multiple** related values synchronously. **`Animatable`**: imperative spring/tween with **snap**, **animateTo**, gestures.
- **Mechanism / order:** Pick by **coordination** needs.
- **Edge cases:** `InfiniteTransition` for repeating shimmer.
- **Production hook:** `remember { Animatable(0f) }` for drag-driven springs.
- **When wrong:** Nesting many `animate*AsState` that should share one transition.

**25.** Shared element transition in Compose.

- [x]

- **Thesis:** **SharedTransitionLayout** + **`Modifier.sharedElement`** / **`sharedBounds`** (Compose 1.7+ **Predictive Back** integration evolving APIs)—animates **bounds** + **overlay** between start/end composables with matched **tags**.
- **Mechanism / order:** Requires **scoped** `SharedTransitionScope` and **matched keys** across navigation.
- **Edge cases:** **Different aspect ratios** clip behavior; **Lazy** list items leaving composition.
- **Production hook:** Follow **AndroidX release notes**—API renamed across versions.
- **When wrong:** Expecting **Activity** shared element without `movableContentOf` bridging.

**26.** `InfiniteTransition`; shimmer.

- [x]

- **Thesis:** Drives **repeating** animations without manual coroutine loop; **`rememberInfiniteTransition`** + `animateFloat` with **`RepeatMode.Restart`**.
- **Mechanism / order:** Phase offset for staggered rows.
- **Edge cases:** Pause when not visible—tie to lifecycle or `DisposableEffect`.
- **Production hook:** Combine with **Brush.linearGradient** translation for shimmer.
- **When wrong:** Infinite animations on **battery** sensitive screens without gating.

---

### Interop and Migration

**27.** `ComposeView` in XML; `AndroidView` for Views in Compose.

- [x]

- **Thesis:** **`ComposeView`** hosts composition in legacy layouts; **`AndroidView(factory, update)`** embeds imperative **View** with **update** lambda on state changes.
- **Mechanism / order:** `AndroidViewBinding` for view binding interop.
- **Edge cases:** **Dispose** View holders properly; **mixing** themes.
- **Production hook:** `MapView`, `WebView`, `CameraPreview` patterns.
- **When wrong:** Creating heavy `View` in `factory` each recomposition—remember factory.

**28.** Incremental XML → Compose migration strategy.

- [x]

- **Thesis:** Start **leaf** screens (islands), new features Compose-only, share **ViewModel**; introduce **design system** in Compose first; **Interop** at boundaries.
- **Mechanism / order:** `Fragment` + `ComposeView` content; migrate **RecyclerView** rows to Compose **items** via interop adapters last mile.
- **Edge cases:** **Navigation** unify on single activity early.
- **Production hook:** Strangler fig pattern per module.
- **When wrong:** Big-bang rewrite without **parity** tests.

**29.** Theming View + Compose coexistence.

- [x]

- **Thesis:** **`MdcTheme`** / **Material3** `MaterialTheme` reads **XML theme** attributes once or maps tokens; keep **single token source** (Material Theme Builder export).
- **Mechanism / order:** `composeView.setContent { AppTheme { … } }` with same color typography spacing.
- **Edge cases:** **Status bar** controlled once from Activity edge-to-edge.
- **Production hook:** Accompanist **SystemUiController** legacy; platform `WindowCompat`.
- **When wrong:** Two sources of truth for **primary** color drifting.

**30.** Compose Multiplatform vs Android-only Compose.

- [x]

- **Thesis:** **CMP** shares **UI code** across JVM/Android/Desktop/iOS (Skia-based); **expect/actual** for platform services; maturity **lags** Android Compose for some Material APIs.
- **Mechanism / order:** **Skiko** rendering; iOS **interop** with UIKit bridges.
- **Edge cases:** **Resources** pipeline differs; **R8** only Android side.
- **Production hook:** Evaluate **KMP** business logic first, UI second.
- **When wrong:** Assuming **100%** API parity with AOSP Compose.
