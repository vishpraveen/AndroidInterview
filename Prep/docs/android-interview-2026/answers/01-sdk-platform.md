# Answers — §1 Android SDK and Platform Internals

Questions: [questions.md](../questions.md#1-android-sdk-and-platform-internals). Numbers match upstream.

---

### Activity and Fragment Lifecycle

**1.** Rotation without `android:configChanges` — callback order; `onSaveInstanceState` vs `onStop`.

- [x] <!-- status: solid -->

- **Thesis:** Config change destroys the Activity instance and creates a new one; framework persists UI state via `onSaveInstanceState` / `Bundle` + optional non-config retainers (today: ViewModel + SavedState).
- **Mechanism / order (typical path):** Old instance: `onPause()` → `onStop()` → `onSaveInstanceState(Bundle)` → `onDestroy()`. New instance: `onCreate(Bundle?)` (non-null = restoration) → `onStart()` → `onPostCreate()` (if used) → `onRestoreInstanceState(Bundle)` (after `onStart`, only if system saved state) → `onResume()`.
- **Edge cases / API deltas:** From **API 28+**, when the user **puts the app in the background**, `onSaveInstanceState` may run **after** `onStop()` (lifecycle order changed for stop-from-background). **Config change** still always gives you a **non-null** `savedInstanceState` in `onCreate` when the system killed the activity for rotation. Do not memorize one order for all teardown reasons—state **why** the activity died.
- **Production hook:** Log + `FragmentStrictMode` / `ReportFragment` + “Don’t keep activities” to validate restoration paths.
- **When wrong:** Assuming `onSaveInstanceState` runs for `finish()` or user **Back** (it generally **does not**); storing large objects in the `Bundle` (TransactionTooLarge).

**2.** `onSaveInstanceState` vs `onRetainNonConfigurationInstance` / ViewModel; when ViewModel still loses state.

- [x]

- **Thesis:** `onSaveInstanceState` persists **small, parcelable, process-survivable** UI snapshot for **system-initiated** death; old `onRetainNonConfigurationInstance` survived **config** only; **ViewModel** survives config while the **same process** holds it.
- **Mechanism / order:** `onSaveInstanceState` → `Bundle` → `onCreate`/`onRestoreInstanceState`; ViewModel store cleared on **process death**.
- **Edge cases:** Low-memory **process kill** after `onSaveInstanceState`: ViewModel **gone**, `Bundle` may still restore if activity recreated in a new process. **Rooted** assumptions that “ViewModel = durable” cause restoration bugs.
- **Production hook:** `SavedStateHandle` + `rememberSaveable` for Compose; persistence (Room/DataStore) for authoritative domain state.
- **When wrong:** Big bitmaps / tokens in ViewModel only; expecting ViewModel after `Application` process death without disk restore.

**3.** `finish()` vs `finishAffinity()` vs `finishAndRemoveTask()`.

- [x]

- **Thesis:** They differ in **how much of the task/back stack** is torn down and whether the **task is removed from Recents**.
- **Mechanism / order:** `finish()` ends **this** activity. `finishAffinity()` finishes **this and all activities below with the same task affinity** in the current task. `finishAndRemoveTask()` finishes the **entire task** and removes it from **Recents** (launcher still defines what “task” means).
- **Edge cases:** Affinity + multi-task; split-screen; different launch modes alter what “below” means.
- **Production hook:** Deep-link flows that must not leave half-finished stacks; payment / auth wizards.
- **When wrong:** Calling `finishAffinity()` expecting only the current screen to close and accidentally nuking a sibling flow.

**4.** Fragment-only lifecycle (`onCreateView`, `onViewCreated`, `onDestroyView`) vs Activity; why `onDestroyView` exists.

- [x]

- **Thesis:** Fragment **decouples** `Fragment` object lifetime from its **view hierarchy** lifetime so you can retain the fragment instance while tearing down **views** (e.g. `replace` in `FragmentTransaction`).
- **Mechanism / order:** `onCreateView`/`onViewCreated`/`onDestroyView` have **no** Activity analog because Activity owns **one** window; Fragment can recreate **views** while the fragment instance stays in the FM back stack.
- **Edge cases:** `onDestroyView` clears **view-scoped** state; holding references to **old** `View`s → leaks; `viewLifecycleOwner` for observers tied to the view, not the fragment instance.
- **Production hook:** `viewModels()` default scopes to **fragment**; use `by viewModels(ownerProducer={requireParentFragment()})` etc. deliberately.
- **When wrong:** Registering `LiveData` on `this` instead of `viewLifecycleOwner` → observers fire after view destroyed.

**5.** `FragmentTransaction.setMaxLifecycle()`; ViewPager2; off-screen fragments.

- [x]

- **Thesis:** Caps how high a fragment’s lifecycle can go **without destroying the instance**, so off-screen pages do not run at `RESUMED`.
- **Mechanism / order:** ViewPager2 + Fragment adapter sets adjacent fragments to **`STARTED`** (not `RESUMED`) when not selected—via `setMaxLifecycle(Lifecycle.State.STARTED)` pattern—so heavy `onResume` work and `LiveData` collectors do not all run at once.
- **Edge cases:** Custom adapters forgetting to reset lifecycle when detaching; interactions with `FragmentTransaction` ordering.
- **Production hook:** Reduces **memory + work** for multi-tab / carousel UIs; pairs with `offscreenPageLimit`.
- **When wrong:** Expecting `onResume` on every page while using ViewPager2 max lifecycle—those pages are intentionally **not resumed**.

**6.** `Dialog` on Activity vs `DialogFragment` — `onPause`?

- [x]

- **Thesis:** A classic **window-attached** `Dialog` is **not** a separate activity; it does **not** automatically pause the hosting Activity the way a **new Activity** does.
- **Mechanism / order:** Underlying Activity usually stays **`RESUMED`** unless the window loses focus or another activity covers it. `DialogFragment` is still a **Fragment**; lifecycle follows fragment + host; showing it does **not** equal `onPause` on host by default.
- **Edge cases:** **Full-screen** or **input-method** focus changes can alter callbacks; **transparent** activities **do** pause the one behind.
- **Production hook:** Prefer **Material dialogs** / `DialogFragment` for rotation + back stack consistency.
- **When wrong:** Assuming “modal dialog ⇒ Activity paused” for leak/race reasoning.

---

### Process Death and State Restoration

**7.** How OS picks a process to kill; priority hierarchy.

- [x]

- **Thesis:** Linux OOM + Android **oom_adj** score: highest score (least important to user) dies first.
- **Mechanism / order (high → low importance):** **Foreground** activity/service → **visible** (not focused but shown) → **perceptible** (e.g. bound by foreground) → **service** → **cached** / **empty** (no components). Background **work** (`JobScheduler`, `WorkManager`) is constrained separately by **standby/doze**.
- **Edge cases:** **Foreground service** elevates; **bound** connections bump importance; **cached** LRU ordering.
- **Production hook:** `adb shell dumpsys meminfo`, `adb shell cat /proc/<pid>/oom_score_adj`.
- **When wrong:** Thinking “service always saves process”; without **START_STICKY** / FGS rules it is still killable under pressure.

**8.** Simulate process death and verify handling.

- [x]

- **Thesis:** Force **non-retained** process teardown then relaunch from launcher to exercise **cold restore**, not just rotation.
- **Mechanism / order:** Dev options **Don’t keep activities** + background app + `adb shell am kill <package>` or stop from **App info → Force stop** (stronger). For **saved state** path: trigger `onSaveInstanceState`, kill, relaunch.
- **Edge cases:** **Instant Run** / attach debugger changes timings; **Autofill** / **Picture-in-picture** alters lifecycle.
- **Production hook:** Firebase Test Lab robo + monkey; internal “kill switch” QA menu.
- **When wrong:** Only testing **config change** and calling process death “done”.

**9.** Singleton lost state after restoration — architecture fix.

- [x]

- **Thesis:** Treat **singletons as caches**, not **sources of truth**; authoritative state must be **reconstructable** from disk / network + `SavedState` for UI continuity.
- **Mechanism / order:** DI graph recreated after process death; **Application**-scoped holder is new JVM statics cleared. Persist IDs, tokens (secure storage), feature flags to **DataStore/Room**.
- **Edge cases:** **Session** tokens in memory only; **in-memory** caches keyed by old PIDs.
- **Production hook:** Hilt **SingletonComponent** + explicit `@Singleton` “cache” interfaces; migration tests with `am kill`.
- **When wrong:** `object Repo` in Kotlin holding `lateinit var session` with no disk backing.

**10.** `SavedStateHandle` in ViewModel vs “regular” ViewModel state.

- [x]

- **Thesis:** `SavedStateHandle` is the **system-saved Bundle pipeline** wired into ViewModel construction; survives **activity-scoped** death that persists `Bundle`, not arbitrary JVM fields by magic.
- **Mechanism / order:** Navigation args + `SavedStateHandle` keys; survives **process death** only when state is **parcelable-safe** and actually written into saved state registry.
- **Edge cases:** Size limits; **non-parcelable** graph objects still need custom `SavedStateProvider`.
- **Production hook:** `SavedStateViewModelFactory`, Compose Navigation `rememberSaveable` for UI-only bits.
- **When wrong:** Putting **large lists** in `SavedStateHandle` → `TransactionTooLargeException`.

---

### Services, Broadcasts, and IPC

**11.** `Service`, `IntentService`, `JobIntentService`, `WorkManager` — modern pick.

- [x]

- **Thesis:** Prefer **`WorkManager`** for **deferrable, guaranteed, constraint-aware** background work; **foreground service** only when user-visible/long-running is mandated by policy.
- **Mechanism / order:** `IntentService` **deprecated** (sequential worker on a service thread); `JobIntentService` bridged **JobScheduler** for pre-O; raw `Service` still valid for **bound** APIs or **foreground** media/location with notification.
- **Edge cases:** **Exact alarms** / **data sync** exemptions; **FGS types** (Android 14+); OEM battery killers vs `WorkManager` reliability.
- **Production hook:** `setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)` for urgent WM jobs within quotas.
- **When wrong:** Long blocking work in `onStartCommand` main thread → **ANR**.

**12.** Binder framework; AIDL under the hood; thread implications.

- [x]

- **Thesis:** Binder is kernel-driven **IPC** with token-identified proxies; AIDL generates **stub/proxy** marshalling code for cross-process typed calls.
- **Mechanism / order:** Client **proxy** `transact()` → kernel → server **stub** `onTransact()` on a **binder thread pool** (not the client’s thread for inbound on server side).
- **Edge cases:** **Re-entrant** calls; blocking remote calls on **UI thread** → jank; **dead** binder when service process dies (`DeadObjectException`).
- **Production hook:** `StrictMode` detect network on main; trace binder latency with **systrace**.
- **When wrong:** Mutating app state from binder threads without posting to a **single-thread** domain model executor.

**13.** Implicit broadcast limits from **Android 8+**; workarounds.

- [x]

- **Thesis:** Oreo blocks most **implicit** manifest-delivered broadcasts for **background** delivery to curb **wakeups** and **battery**.
- **Mechanism / order:** Still OK: **explicit** intents (component set), **same-app** local broadcasts (`LocalBroadcastManager` deprecated—use **Flow**/callback), **runtime-registered** receivers while app foreground, whitelisted **few** implicit (e.g. `BOOT_COMPLETED` with restrictions).
- **Edge cases:** **GOOGLE_PLAY** policy on exact alarm + FGS for replacements.
- **Production hook:** `WorkManager` periodic sync; **push** (FCM) for server-driven wake.
- **When wrong:** Shipping manifest implicit receiver expecting global device events for analytics.

**14.** `Messenger` vs `AIDL` for IPC.

- [x]

- **Thesis:** `Messenger` = **single-threaded Handler** + `Message` queue across processes (simple, ordered); AIDL = **arbitrary multi-method** interface, often **concurrent** binder threads.
- **Mechanism / order:** Messenger serializes work naturally; AIDL needs **thread-safety** on service implementation.
- **Edge cases:** Large payloads → still Binder **transaction limits** (~1MB practical smaller).
- **Production hook:** Messenger for **fire-and-forget** commands; AIDL for rich APIs / performance with careful threading.
- **When wrong:** AIDL for trivial ping-pong without concurrency control → races.

**15.** Bound vs started service; can both; lifecycle.

- [x]

- **Thesis:** **Started** = `startService` / `Context.startForegroundService` drives independent lifecycle until `stopSelf` / `stopService`. **Bound** = `bindService`; lifetime tied to **connections** (`unbind` when done).
- **Mechanism / order:** **Both**: can be started **and** bound; `stopSelf()` may not destroy until **last bind** released depending on flags (`START_STICKY` etc.).
- **Edge cases:** **Leak** from never unbinding; **foreground** promotion rules.
- **Production hook:** Track ref-counted binds in debug builds.
- **When wrong:** Binding from `Application` with **Activity** `Context` leaked through callback.

**16.** `PendingIntent` flags — security/behavior; Android 12 change.

- [x]

- **Thesis:** `PendingIntent` is a **capability token** another app/system can fire **as you**; flags control **mutability** and **identity** of matching intents.
- **Mechanism / order:** `FLAG_UPDATE_CURRENT` / `FLAG_CANCEL_CURRENT` control reuse; **`FLAG_IMMUTABLE`** (required default **since S for many creations**) prevents tampering with extras; **`FLAG_MUTABLE`** only when a **fill-in** must mutate (e.g. mutable extras on a **notification** action).
- **Edge cases:** **Mutability** misuse → **intent hijacking**; targeting **S+** without explicit mutability → `IllegalArgumentException`.
- **Production hook:** Lint + compile against latest SDK; audit all `PendingIntent.get*` sites.
- **When wrong:** `FLAG_MUTABLE` “to make it work” everywhere.

---

### Content Providers and System Components

**17.** Custom `ContentProvider` — when; in-app-only modular access?

- [x]

- **Thesis:** Use when you need **cross-process** data API with **uniform CRUD**, **URI permissions**, or **SearchIndexables** integration—not as a fancy in-app DAO.
- **Mechanism / order:** Exported provider = **attack surface**; `grantUriPermission` for least privilege.
- **Edge cases:** **FileProvider** already solves sharing files; Room is cheaper in-process.
- **Production hook:** Defensive `query` projection/selection whitelisting; SQL injection hygiene.
- **When wrong:** Provider as **internal-only** abstraction—adds **main-thread** `ContentResolver` pitfalls without benefit vs Repository + Room.

**18.** `FileProvider`; URI permission model vs `file://`.

- [x]

- **Thesis:** `FileProvider` maps **sandboxed files** to **`content://`** URIs with **temporary read grants** (`FLAG_GRANT_READ_URI_PERMISSION`) so receivers never see raw filesystem paths.
- **Mechanism / order:** `res/xml` paths + `getUriForFile`; permissions expire when **task stack** ends or explicitly revoked.
- **Edge cases:** **Scoped storage**; **MediaStore** for shared media; path traversal misconfiguration.
- **Production hook:** Never expose **internal-cache** URIs broadly; test with **Chooser**.
- **When wrong:** `file://` intents on **N+** → **FileUriExposedException**.

**19.** `Application` class role; heavy init pitfalls.

- [x]

- **Thesis:** `Application` is the **process-wide** entry for **dependency graph** wiring and **cheap** global hooks—not a second main method for **seconds-long** IO.
- **Mechanism / order:** Blocks **first frame** if `onCreate` does disk/network; **content providers** may run **before** `Application.onCreate` depending on manifest order.
- **Edge cases:** **Multi-process** app (`android:process`) — multiple `Application` instances.
- **Production hook:** **App Startup** library + lazy init; **Firebase** deferred; baseline profile for hot paths.
- **When wrong:** Full DB migration + network config fetch in `Application.onCreate`.

**20.** `Context` types — leaks/crashes when wrong.

- [x]

- **Thesis:** **Long-lived** objects must not hold **short-lived** `Activity` contexts; **UI** needs **themed** `Activity` context; **singletons** want **`Application`** context.
- **Mechanism / order:** `Activity` = `ContextWrapper` + `Window`/`theme`; `Service`/`Application` contexts lack those; `getApplicationContext()` strips activity leaks if used for **non-UI** singletons.
- **Edge cases:** `Dialog` with **Application** context → **crash** (no token); `LayoutInflater` from **Application** → wrong theme/density.
- **Production hook:** LeakCanary; avoid static `View`/`Activity` refs.
- **When wrong:** `static Drawable` inflated with activity `Context` held forever.

---

### Window and View System

**21.** View pipeline `measure` → `layout` → `draw`; relayout vs redraw.

- [x]

- **Thesis:** **Measure** computes sizes, **layout** assigns positions, **draw** renders; invalidation is cheaper when you only need **draw** again.
- **Mechanism / order:** `requestLayout()` bubbles measure+layout (and often draw); `invalidate()` schedules **draw** only for dirty rects.
- **Edge cases:** Layout thrash from mutual dependencies (`WRAP_CONTENT` + weighted children); hardware layers for animation.
- **Production hook:** **Layout inspector**; **Systrace** `Choreographer#doFrame`.
- **When wrong:** Calling `requestLayout()` per frame in `onDraw`.

**22.** Hardware acceleration; when to disable per view.

- [x]

- **Thesis:** Views render via **GPU** (display lists) by default on modern devices; some **Canvas** ops are unsupported or buggy accelerated.
- **Mechanism / order:** `setLayerType(LAYER_TYPE_SOFTWARE, null)` for specific legacy drawing / `Xfermode` quirks; whole-activity disable via manifest rare.
- **Edge cases:** **Overdraw** with excessive offscreen layers; **large** `Bitmap` uploads.
- **Production hook:** **Profile GPU rendering** bars; **Rendering** tab.
- **When wrong:** Global software acceleration “for stability” masking root layout bugs.

**23.** `WindowInsets`; edge-to-edge (Android 15+ posture); bars, cutout, IME.

- [x]

- **Thesis:** Insets describe **system UI** and **display cutouts** occupying window space; edge-to-edge draws **behind** bars and uses insets for **padding** / **IME animation** (`WindowInsetsAnimationCompat`).
- **Mechanism / order:** `WindowCompat.setDecorFitsSystemWindows(window, false)` + consume **`systemBars()`**, **`displayCutout()`**, **`ime()`** as needed; **`enableEdgeToEdge()`** helper on newer artifacts.
- **Edge cases:** **Gesture nav** vs 3-button insets differ; **per-display** cutouts on foldables.
- **Production hook:** Material3 `SystemBarStyle`; **`WindowInsetsControllerCompat`** for light/dark icons.
- **When wrong:** Hard-coded `statusBarHeight` dp instead of listening to inset changes.

**24.** `SurfaceView` vs `TextureView`.

- [x]

- **Thesis:** `SurfaceView` has a **separate composited surface** (better for camera/video performance, **independent** refresh); `TextureView` is a **normal View** texture (transforms/alpha easy, costs more memory/bandwidth).
- **Mechanism / order:** `SurfaceView` z-ordering / overlap quirks; `TextureView` participates in **animations** as a texture.
- **Edge cases:** **DRM** / **secure** output restrictions; **preview** sizing.
- **Production hook:** CameraX abstracts much of this choice.
- **When wrong:** `TextureView` for **full-screen 4K** preview on low-end GPU → thermal + dropped frames.

**25.** `RecyclerView` recycling — scrap heap vs recycled view pool.

- [x]

- **Thesis:** **Adapter** binds model → **ViewHolder**; RV caches **detached** views for **instant** rebinding while scrolling.
- **Mechanism / order:** **Scrap** = views **detached** during current layout pass (still “warm” for same pass); **RecycledViewPool** holds **typed** caches of **ViewHolders** across layouts (multiple RVs can **share** pool).
- **Edge cases:** **Stable IDs** + `setHasStableIds` reduce unnecessary rebound; **prefetch** (`GapWorker`) hides `onBind` latency.
- **Production hook:** **Too many view types** → pool misses; **nested** RVs need shared pool + `setRecycledViewPool`.
- **When wrong:** Creating **new** `View` in `onBindViewHolder` instead of binding fields.
