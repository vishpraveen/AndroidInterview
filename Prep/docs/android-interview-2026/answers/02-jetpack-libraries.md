# Answers — §2 Android Jetpack Libraries

Questions: [questions.md](../questions.md#2-android-jetpack-libraries).

---

### Navigation Component

**1.** Back stack; `popUpTo` vs `popUpToInclusive`.

- [x]

- **Thesis:** NavController maintains a **fragment back stack** (or composable stack) as destinations; `popUpTo` rewinds that stack when navigating.
- **Mechanism / order:** `popUpTo(id|route)` pops destinations **up to** the target **excluded** by default; **`popUpToInclusive(true)`** also **removes** the target itself—useful to make a destination the **new root** (e.g. login → home clears auth flow).
- **Edge cases:** `launchSingleTop`, deep link **implicit** pops; state loss if popping non-saved destinations.
- **Production hook:** `NavOptions` builder in Kotlin DSL; test with **navigation-testing** artifact.
- **When wrong:** `popUpToInclusive(false)` expecting the named graph root to remain when you needed it gone.

**2.** Deep links; deferred deep links after auth.

- [x]

- **Thesis:** Nav handles **explicit** URI → destination via manifest `<nav-graph>` intent-filter + `navigate(Uri)`; **deferred** = capture pending intent/URI, **authenticate**, then `navigate` with same args or `NavDeepLinkRequest`.
- **Mechanism / order:** `NavController.handleDeepLink()`; for login gate: store **PendingIntent** or deep link bundle in **ViewModel/SavedState**, post-auth call `navigate` with **popUpTo** splash/login.
- **Edge cases:** **duplicate** deep links; cold start vs in-app; **Play App Links** assetlinks.json.
- **Production hook:** Firebase Dynamic Links deprecation → **App Links** + backend token in path/query.
- **When wrong:** Navigating before graph is set → **IllegalStateException**; losing deep link on process death without **SavedState**.

**3.** Multi-module graphs without circular deps.

- [x]

- **Thesis:** Each feature owns a **nested graph**; **app** module wires `include` graphs; dependencies point **inward** to **:core:navigation** contracts (routes sealed/interfaces), not feature→feature.
- **Mechanism / order:** `:app` depends on all features; features depend on **:core:model** + **navigation API**; use **Gradle `api` vs `implementation`** so graphs compile only where included.
- **Edge cases:** **Hilt** `@Module` in app aggregating feature bindings; Compose **typed routes** (KSP) to replace stringly routes.
- **Production hook:** **Navigation multi-module** sample pattern: `includeDynamicFeature` for optional features.
- **When wrong:** Feature A imports Feature B’s Fragment class for navigation—creates **compile cycle**.

**4.** Nav component limitations; custom nav.

- [x]

- **Thesis:** Great for **fragment/screen** stacks + args; weak for **complex transitions**, **per-tab** independent stacks, **non-fragment** hosts, or **highly dynamic** graphs from CMS.
- **Mechanism / order:** Custom: **Compose** own back stack + `BackHandler`; **single-activity multi-stack** with multiple NavHosts; **Coordinator** pattern.
- **Edge cases:** **OnBackPressedDispatcher** ordering; **predictive back** custom animations.
- **Production hook:** Bottom nav + multiple back stacks: `NavigationUI` setup or manual `saveState`/`restoreState`.
- **When wrong:** Fighting Nav with **fragments inside ViewPager** without `BottomNavigationView` save state—use right abstraction.

**5.** `SavedStateHandle` + Navigation arguments.

- [x]

- **Thesis:** Nav passes **args** into the **SavedStateHandle** of the destination’s `ViewModel` when using `hiltNavGraphViewModels` / `createSavedStateHandle`—args survive **process death** if parcelable-safe.
- **Mechanism / order:** `by navArgs()` for one-shot; `SavedStateHandle` keys mirror argument names; **nullable** vs default values in safe-args.
- **Edge cases:** **Large** payloads—use **ID** in args, fetch body from repo.
- **Production hook:** Safe Args / Compose Navigation type-safe routes.
- **When wrong:** Putting non-parcelable objects in nav args expecting ViewModel magic.

---

### WorkManager

**6.** WM architecture; JobScheduler vs AlarmManager vs GCMNetworkManager.

- [x]

- **Thesis:** WM is a **scheduler façade** + SQLite **job store**; runtime picks **JobScheduler** (L+), else **AlarmManager**+**BroadcastReceiver**, legacy **GcmNetworkManager** path removed on modern Play services devices.
- **Mechanism / order:** `Scheduler` impl chosen at **runtime** from Google Play **WorkManager** artifact; constraints (network, charging) compiled to platform APIs.
- **Edge cases:** **Pre-L** devices rare today; **OEM** killing alarms—WM still **best-effort** not real-time.
- **Production hook:** `adb shell dumpsys jobscheduler` for debugging scheduling.
- **When wrong:** Using WM for **sub-minute** periodic work—**minimum interval** policy blocks it.

**7.** OneTime vs Periodic; minimum interval.

- [x]

- **Thesis:** **PeriodicWork** is **approximate** batched work; **minimum interval 15 minutes** (platform policy + battery); **OneTime** runs once subject to constraints.
- **Mechanism / order:** Periodic cannot chain the same tight loop; use **unique work** names for dedupe.
- **Edge cases:** **Expedited** one-time for out-of-band urgent tasks (quota).
- **Production hook:** `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate schedulers.
- **When wrong:** Expecting wall-clock **every 15m sharp** on Chinese OEMs.

**8.** Chaining, input mergers, unique work policies.

- [x]

- **Thesis:** **WorkContinuation** `then`/`combine`; **InputMerger** merges `Data` from parallel predecessors; **unique work** enforces single logical job name.
- **Mechanism / order:** `KEEP` ignore new; `REPLACE` cancel old; `APPEND` queue after; `APPEND_OR_REPLACE` append or replace if finished—**ordering** semantics differ.
- **Edge cases:** `REPLACE` drops in-flight; test **idempotency** of workers.
- **Production hook:** Log **runAttemptCount** + chain UUID in Crashlytics breadcrumbs.
- **When wrong:** `APPEND` on unique work when first never runs—**stuck chain**.

**9.** Testing WM; `WorkManagerTestInitHelper`, `TestDriver`.

- [x]

- **Thesis:** Use **test** artifact that swaps **TestDriver** for real scheduler—**synchronous** execution in JVM tests.
- **Mechanism / order:** `WorkManagerTestInitHelper.initializeTestWorkManager(context)`; `TestDriver.setInitialDelayMet` / `setAllConstraintsMet` / `setExecutionState`.
- **Edge cases:** **Robolectric** minSdk; **integration** tests on device still valuable for OEM.
- **Production hook:** `CoroutineWorker` unit-test logic by extracting **use case** from `doWork`.
- **When wrong:** Testing real `JobScheduler` in pure JVM without test WM—flaky.

**10.** Periodic sync delayed on Xiaomi/Huawei; mitigate.

- [x]

- **Thesis:** **Aggressive battery** policies + **app standby** buckets defer background WM unless **whitelist** or **FGS** / user-facing sync.
- **Mechanism / order:** OEM “autostart”, **ignore battery optimizations** prompt (UX trade-off); **FCM** high-priority to nudge sync; **expedited** WM when allowed.
- **Edge cases:** **Exact alarm** permission (S+) separate from WM.
- **Production hook:** In-app **“allow unrestricted”** education screens; Play policy compliance.
- **When wrong:** Busy-looping **AlarmManager.setExact** for sync—policy + battery disaster.

---

### Paging 3

**11.** Paging 3: `PagingSource`, `RemoteMediator`, `PagingData`, `PagingDataAdapter`; data flow.

- [x]

- **Thesis:** **PagingSource** loads **pages** (offset/keyed); **RemoteMediator** bridges **network + DB** for **prepend/append** refresh; **PagingData** is cold stream of **LoadResult** pages; **PagingDataAdapter** diffs **PagingData** into `RecyclerView`/`LazyPagingItems`.
- **Mechanism / order:** Room DAO returns `PagingSource`; `Pager.flow` → `cachedIn(scope)` → collect in VM → submit to adapter; RemoteMediator `load` triggers **network** then **DB insert** then **invalidate**.
- **Edge cases:** **jumping** in `PagingSource`; **placeholder** vs no placeholder.
- **Production hook:** `CombinedLoadStates` for UI errors/empty.
- **When wrong:** Calling `refresh()` without invalidating correct **PagingSource** factory.

**12.** `RemoteMediator` vs `PagingSource` only.

- [x]

- **Thesis:** Use **RemoteMediator** when **network is source of truth** but **Room** backs UI (offline-first, end-of-list fetch). **PagingSource** alone when **local or remote** single source with simple paging API.
- **Mechanism / order:** Mediator returns `MediatorResult.Success(endOfPaginationReached)`; errors map to **retry** signals.
- **Edge cases:** **Stale** cache while offline—show **header** state.
- **Production hook:** `remoteKeys` table for next/prev keys.
- **When wrong:** Remote API paging in `PagingSource` **and** duplicate Room writes without mediator—race conditions.

**13.** Item-level updates (like) without full reload.

- [x]

- **Thesis:** Mutate **DB row** Room observes → **PagingSource** invalidates **affected pages** or use **`snapshot()`** + adapter **notifyItemChanged** patterns; prefer **DB as SSOT** with small invalidation.
- **Mechanism / order:** `PagingDataAdapter` with **diffUtil** item ids; `recordPagingSource` invalidated by **transaction** updating one row.
- **Edge cases:** **Jump** in scroll position if keys shift—use **stable IDs**.
- **Production hook:** `getItemId` + `setHasStableIds` on RV side (interop).
- **When wrong:** `adapter.refresh()` on every like—drops scroll state.

**14.** Paging 3 + Room offline-first.

- [x]

- **Thesis:** Room **`PagingSource<Int, Entity>`** reads **local** pages; **RemoteMediator** fills DB from network; UI always reads **DB** only.
- **Mechanism / order:** Network errors surface as **LoadState.Error** + **retry** button calling `adapter.retry()`.
- **Edge cases:** **DB migrations** changing keys—clear `remote_keys` table in migration.
- **Production hook:** `Transaction` in mediator for keys+rows insert atomicity.
- **When wrong:** Dual writes to in-memory list + Room—**two SSOTs**.

---

### Hilt / DI

**15.** Hilt component hierarchy; `SingletonComponent` vs `ActivityRetainedComponent` vs `ViewModelComponent`.

- [x]

- **Thesis:** **DAG layers**: `SingletonComponent` (app-wide) → `ActivityRetainedComponent` (survives config, not process death) → `ActivityComponent` → `FragmentComponent` → **`ViewModelComponent`** (per VM) → `ViewComponent`.
- **Mechanism / order:** `@HiltViewModel` gets `@ViewModelScoped`; **retained** bindings for **Nav back stack** VM scope use `ActivityRetainedScoped`.
- **Edge cases:** **AssistedInject** factories for runtime args.
- **Production hook:** Component **timers** in generated `Hilt_*` classes—read when debugging scope bugs.
- **When wrong:** Putting **Context** in singleton without `ApplicationContext`—leak.

**16.** `@EntryPoint`; when needed.

- [x]

- **Thesis:** Escape hatch to obtain **graph** from non-`@AndroidEntryPoint` types (ContentProvider, WorkerFactory pre-`@HiltWorker`, BroadcastReceiver without entry point in old code).
- **Mechanism / order:** `EntryPointAccessors.fromApplication(app, FooEntryPoint::class.java)`.
- **Edge cases:** Prefer **`@HiltWorker`** / **`EntryPoint` interfaces kept tiny**.
- **Production hook:** Lint: ban new EntryPoints except allowlist.
- **When wrong:** EntryPoint as **service locator** everywhere—defeats DI testability.

**17.** Same interface, different impls (`@Named`, `@Qualifier`).

- [x]

- **Thesis:** **Qualifier annotations** (`@Retention` `RUNTIME`, `BINARY`) disambiguate bindings; `@Named("foo")` is stringly qualifier—prefer **typed `@Qualifier`**.
- **Mechanism / order:** `@Binds @IntoSet` for multibinding strategies.
- **Edge cases:** **Test** fakes: `@TestInstallIn` modules replace bindings.
- **Production hook:** One qualifier per axis (base URL, dispatchers).
- **When wrong:** Typos in `@Named` strings—runtime only.

**18.** Hilt compile-time codegen; KSP role.

- [x]

- **Thesis:** Hilt = **Dagger2** + **processors** generating **component implementations** and **member injectors**; **KSP** parses Kotlin symbols faster than **kapt** for **Hilt extensions** / Room / Moshi where supported.
- **Mechanism / order:** `@HiltAndroidApp` triggers root component; KAPT still used where processors not on KSP—**AGP matrix** evolving.
- **Edge cases:** **incremental** annotation processing flags.
- **Production hook:** `--scan` build time; enable **KSP** for eligible libs.
- **When wrong:** Mixing **two** `@HiltAndroidApp` classes.

**19.** Hilt vs Koin vs manual DI (large project).

- [x]

- **Thesis:** **Hilt**: compile-time **graph validation**, scales in **multi-module** with standard patterns. **Koin**: runtime DSL, faster to prototype, **no compile-time** cycle detection. **Manual** Dagger only: max control, more boilerplate.
- **Mechanism / order:** Large orgs pick **Hilt** for **consistent** onboarding + static analysis.
- **Edge cases:** **Dynamic feature** modules need **optional** bindings / component dependencies.
- **Production hook:** ArchUnit module dependency tests + DI lint.
- **When wrong:** Koin **global** `startKoin` in library SDK—host app coupling.

---

### Other Jetpack

**20.** Lifecycle-aware components; `LifecycleObserver` vs `DefaultLifecycleObserver` vs `LifecycleEventObserver`.

- [x]

- **Thesis:** Subscribe to **Lifecycle** state transitions; prefer **`DefaultLifecycleObserver`** interface methods (`onStart`, etc.) over `@OnLifecycleEvent` (deprecated) for **Java 1.8 default methods** clarity.
- **Mechanism / order:** `LifecycleEventObserver` receives **every** `Event`—use when you need raw stream; avoid heavy work in `ON_CREATE`.
- **Edge cases:** **INITIALIZED** vs **CREATED** edge; **repeatOnLifecycle** for Flow collection.
- **Production hook:** `ProcessLifecycleOwner` for app foreground detection.
- **When wrong:** Starting camera in `ON_START` without checking **permission** revoked—crash.

**21.** App Startup vs `ContentProvider` init.

- [x]

- **Thesis:** **App Startup** merges initializer **DAG** into **one** `ContentProvider` ordering **explicit dependencies** vs **manifest order lottery**.
- **Mechanism / order:** `Initializer<Dependencies>` `create()` runs on **same** startup path; **lazy** `AppInitializer.getInstance(context).initializeComponent`.
- **Edge cases:** **debug** only initializers via manifest merge tools.
- **Production hook:** Replace many SDK `ContentProvider` auto-init with **manual** Startup entries where SDK supports it.
- **When wrong:** Heavy I/O in `Initializer.create`—blocks **first frame**.

**22.** DataStore Preferences vs Proto; consistency vs `SharedPreferences`.

- [x]

- **Thesis:** **DataStore** is **Kotlin coroutine**-first, **transactional** `edit { }` with **serialization** guarantees and **type safety** (proto); **no** blocking `apply()`/`commit()` on main.
- **Mechanism / order:** **Single-writer** `Data` updates; **migrations** from SharedPreferences via `replacePreferences`.
- **Edge cases:** **Proto** schema evolution—`FieldMask` / reserved fields.
- **Production hook:** `dataStore` delegate + `Okio` atomic moves on disk.
- **When wrong:** Reading DataStore synchronously on main—use **`runBlocking`** only in tests.

**23.** CameraX lifecycle-aware API.

- [x]

- **Thesis:** Binds **UseCase**s to **`LifecycleOwner` + CameraSelector**; handles **rotation**, **surface** lifecycle, **auto-unbind**.
- **Mechanism / order:** `ProcessCameraProvider.bindToLifecycle(owner, selector, preview, analysis)`.
- **Edge cases:** **Concurrent** use cases max resolution; **extensions** vendor HDR.
- **Production hook:** `ImageAnalysis` for QR on worker executor.
- **When wrong:** Binding before **camera permission** granted—IllegalArgument.

**24.** Macrobenchmark vs Microbenchmark.

- [x]

- **Thesis:** **Macrobenchmark**: **real APK**, **device**, measures **startup/scroll/jank** with **Perfetto** traces—integration level. **Microbenchmark**: **JVM/AndroidJUnit** tiny hot loops—JIT noise controlled with **BaselineProfile** warmup / `@BenchmarkRule`.
- **Mechanism / order:** Macro uses **`BenchmarkRule.measureRepeated`** + `CompilationMode` partial vs full.
- **Edge cases:** **Debuggable** false required; **minSdk** for Macro module.
- **Production hook:** CI **Macrobenchmark** on physical device pool nightly.
- **When wrong:** Micro results without **isolation**—misleading “5ns” fantasies.

**25.** Baseline Profiles; startup + runtime.

- [x]

- **Thesis:** **Human-readable** list of hot classes/methods **AOT** compiled at install/update → fewer **interpret/JIT** hits on **cold/warm** start and critical paths.
- **Mechanism / order:** `BaselineProfileGenerator` scenario → `baseline-prof.txt` packaged in **main** assets; **R8** merges rules.
- **Edge cases:** **Library** profiles merge; **Compose** strong skipping separate concern.
- **Production hook:** `collectBaselineProfile` Gradle task in CI artifact.
- **When wrong:** Profile **stale** after refactor—regenerate each release.
