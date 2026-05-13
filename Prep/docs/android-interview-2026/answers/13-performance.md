# Answers — §13 Performance and Optimization

Questions: [questions.md](../questions.md#13-performance-and-optimization).

---

### Memory

**1.** Memory leak causes — five examples.

- [x]

- **Thesis:** **Static** field holding `Activity`/`View`; **anonymous inner** class holding outer Activity ref; **Handler**/`Runnable` posted after Activity dead; **singleton** with `Context` not application; **listener** not unregistered (`LocationManager`, `SensorManager`); **Glide** request without lifecycle (bonus sixth).
- **Mechanism / order:** Leak = **unreachable** from GC roots? Actually still reachable via wrong path—**strong** reference chain to dead Activity.
- **Edge cases:** **ViewModel** holding `Context`—classic.
- **Production hook:** **LeakCanary** in debugInternal builds.
- **When wrong:** “GC will eventually fix it” — leaks until OOM.

**2.** Detect leaks: LeakCanary, Profiler, `dumpsys meminfo`.

- [x]

- **Thesis:** **LeakCanary** heap dumps + heuristics on **retained objects** size thresholds; **Android Studio Profiler Memory** live allocations + heap dump dominator tree; **`adb shell dumpsys meminfo <package>`** RSS/PrivateDirty summary field ops.
- **Mechanism / order:** MAT/Profiler **path to GC roots** analysis.
- **Edge cases:** **Native** leaks not shown in Java heap—use **Native Memory Profiler**.
- **Production hook:** Play **Memory** vitals + custom **Reporting`onTrimMemory`** telemetry.
- **When wrong:** Only watching Java heap while **Bitmap.native** huge.

**3.** Leak vs high memory usage.

- [x]

- **Thesis:** **Leak** = monotonic growth / retained unreachable Activity graph. **High usage** legitimate big dataset resident—**bounded** but large (e.g., image cache) needs **tuning/eviction**, not just “GC”.
- **Mechanism / order:** **Profiler** timeline slope + user journey correlation.
- **Edge cases:** **Bitmap pool** high but stable OK.
- **Production hook:** `onTrimMemory` callbacks shrink caches.
- **When wrong:** Calling legitimate LRU “memory leak”.

**4.** Android GC vs JVM; GC pauses and jank.

- [x]

- **Thesis:** ART **concurrent mostly** collector generations differ by version (**CC**, **CMC**, **Generational CC**); still **pause** phases for compaction/mark compact; **jank** when pause overlaps **VSYNC** deadline causing **missed frames**.
- **Mechanism / order:** **Heap size** growth triggers more frequent GC; **large** allocations on UI thread amplify risk.
- **Edge cases:** **GC for alloc** on binder thread blocking reply.
- **Production hook:** **Perfetto** `sched` slices + `android.track` frame timeline.
- **When wrong:** Blaming GC for all jank without measuring main thread work.

**5.** `WeakReference` when appropriate.

- [x]

- **Thesis:** Breaks **strong** reference chain for **cache** entries or **listener** maps where **owner** lifecycle should dominate; **does not replace** proper lifecycle unregister—**weak** alone if **only** reference may be collected unexpectedly causing **null** surprises.
- **Mechanism / order:** `WeakHashMap` for ancillary metadata keyed by object identity (rare on Android due to **implicit** strong keys pitfalls—prefer explicit).
- **Edge cases:** **Finalization** lag—object survives longer than expected.
- **Production hook:** `ReferenceQueue` cleanup pattern for bitmap caches advanced.
- **When wrong:** WeakReference to fix architecture smell.

---

### ANR and Responsiveness

**6.** ANR causes; thresholds.

- [x]

- **Thesis:** **Main thread** blocked: **input dispatch ~5s** no response; **BroadcastReceiver `onReceive` ~10s` (foreground broadcast queue)**; **Service** start foreground timeout cases; **ContentProvider** main thread slow startup.
- **Mechanism / order:** Watchdog writes **traces.txt** with all threads stacks.
- **Edge cases:** **Binder** thread starvation indirectly surfaces as ANR.
- **Production hook:** **Firebase Performance** traces + **ANR** clustering by Play.
- **When wrong:** Heavy work in `onReceive` “it’s quick usually”.

**7.** Diagnose ANR: `traces.txt`, Play Console.

- [x]

- **Thesis:** Pull **ANR trace** from device bugreport or Play **ANRs & crashes** page shows **main thread** stack at timeout + **Binder` transactions`**; compare with **symbolicated** native if JNI.
- **Mechanism / order:** Look for **lock** contention (`waiting to lock`), **I/O** in main, **deadlock** cycles.
- **Edge cases:** **Misleading** last frame if thread was runnable but CPU starved—check **cpu** sched slice.
- **Production hook:** **Sentry** ANR integration with breadcrumbs.
- **When wrong:** Only reading app threads ignoring **system_server** interactions.

**8.** `StrictMode` disk/network on main.

- [x]

- **Thesis:** `ThreadPolicy` detects **implicit disk/network** on main thread in **debug**; `penaltyLog`/`penaltyDeath` variants; `VmPolicy` leaks.
- **Mechanism / order:** Enable in `Application` **if (BuildConfig.DEBUG)**.
- **Edge cases:** **Third-party SDK** violations—wrap calls off main or replace SDK.
- **Production hook:** Combine with **Firebase** debug builds dogfooding.
- **When wrong:** StrictMode in release.

**9.** Main-thread blocking in production; frame drops; `FrameTimeline`.

- [x]

- **Thesis:** **Perfetto** `FrameTimeline` shows **expected vs actual** frame deadlines, **jank** types (`SurfaceFlinger` scheduling, **app** stage); Firebase Performance **Screen traces** custom; **Play Vitals** frozen/slow frames rates.
- **Mechanism / order:** Tag main sections with **custom trace** APIs `Trace.beginSection`.
- **Edge cases:** **GPU** bounded vs **UI** thread bounded jank differ fixes.
- **Production hook:** **Macrobenchmark** `FrameTimingMetric` regression tests.
- **When wrong:** Optimizing **overdraw** when actual issue **layout thrash**.

---

### Startup Optimization

**10.** Cold / warm / hot start metrics.

- [x]

- **Thesis:** **Cold**: process not running—**heaviest**. **Warm**: process alive, activity recreated. **Hot**: resume existing activity. Track **time to first frame (TTFF)**, **time to interactive**, **reportFullyDrawn` latency**.
- **Mechanism / order:** Macrobenchmark `StartupMode.COLD` etc.
- **Edge cases:** **Baseline profile** affects cold most.
- **Production hook:** **Firebase Performance** custom trace `app_start`.
- **When wrong:** Measuring only **Activity.onCreate** ignoring inflation.

**11.** Reduce cold start: App Startup, lazy init, Baseline Profiles.

- [x]

- **Thesis:** **Remove** heavy work from `Application.onCreate`; **lazy** init on first feature use; **App Startup** merges initializers; **Baseline Profiles** AOT hot methods; **avoid** blocking content providers; **R8** shrink.
- **Mechanism / order:** **Firebase init** `dataCollectionDefaultEnabled` defer patterns.
- **Edge cases:** **Multi-dex** cold—baseline profiles critical.
- **Production hook:** **Macrobenchmark** startup regression CI.
- **When wrong:** Eager SDK init “vendor said call in Application”.

**12.** Android 12+ SplashScreen API.

- [x]

- **Thesis:** System **splash screen** shows **icon + background** from theme attrs until first draw; **`SplashScreen.installSplashScreen()`** allows **custom exit** animation hook `setOnExitAnimationListener`.
- **Mechanism / order:** **Adaptive icons** day/night attributes `windowSplashScreenAnimatedIcon`.
- **Edge cases:** **Branded** vs **legacy** `postSplashScreenTheme` switch.
- **Production hook:** Keep splash **fast**—don’t add artificial delays.
- **When wrong:** 3-second sleep “for branding” causing ANR perception.

**13.** Measure startup in production: `reportFullyDrawn`, Macrobenchmark.

- [x]

- **Thesis:** **`Activity.reportFullyDrawn()`** signals system meaningful draw complete (Play vitals uses); **Macrobenchmark** local device measurement with **tracing**; **Firebase** manual trace start at `Application` attach end? Careful overhead—sampled.
- **Mechanism / order:** Align definition cross-platform for dashboards.
- **Edge cases:** **Lazy** lists first item not loaded—user sees spinner—false “drawn”.
- **Production hook:** Combine **fully drawn** with **first successful network** optional second trace.
- **When wrong:** Logging startup every launch at **verbose** cost.

---

### Rendering Performance

**14.** Jank; Systrace / Perfetto / Profiler.

- [x]

- **Thesis:** **Jank** = missed **frame deadline**; **Systrace/Perfetto** timeline shows **UI thread** slices vs **RenderThread** vs **GPU**; **Android Studio Profiler CPU** captures callstack sampling.
- **Mechanism / order:** Identify **long layout/measure** vs **long draw** vs **binder** stalls.
- **Edge cases:** **Triple buffering** hides some issues—still measure.
- **Production hook:** **FrameMetricsAggregator** aggregated in CI optional rare.
- **When wrong:** Eyeballing “feels slow” without numbers.

**15.** RecyclerView fast-scroll stutter fixes.

- [x]

- **Thesis:** **`setHasStableIds(true)`** + proper `getItemId`; **prefetch** with `GapWorker`; **recycled view pool** sharing & **maxRecycledViews` tuning**; **reduce `onBind` work**—move to **`payloads`** diff; **images** sized correctly; **avoid** heavy work; **sync** prefetch disabled if harmful.
- **Mechanism / order:** **Profile binding** with Android Studio.
- **Edge cases:** **Nested** RVs—share pool + disable nested prefetch sometimes.
- **Production hook:** **Systrace** `RV Prefetch` track.
- **When wrong:** `notifyDataSetChanged` always.

**16.** Compose optimization: strong skipping, stability, compiler reports.

- [x]

- **Thesis:** Enable **strong skipping mode** compiler flag to skip more composables; fix **stability** (`@Immutable` models); **compiler report** lists unstable params; use **`key`**, **`remember`**, **`derivedStateOf`**, **`LazyList` content** lambda stability.
- **Mechanism / order:** **Recomposition** counts in Layout Inspector.
- **Edge cases:** **StateFlow** updates at 120Hz—`distinctUntilChanged` / batch updates.
- **Production hook:** **Baseline profile** for Compose runtime hot methods.
- **When wrong:** `mutableStateListOf` entire screen list replaced each frame.

**17.** Baseline Profiles + AOT runtime perf.

- [x]

- **Thesis:** Shipping **`baseline-prof.txt`** guides ART **AOT/AOT-like** compilation of hot methods reducing **JIT/interpret** overhead in steady state scrolling—not only startup.
- **Mechanism / order:** Generated via **Macrobenchmark** generator scenario.
- **Edge cases:** **Library** profiles merge order.
- **Production hook:** Track **dex2oat** compile time on install acceptable trade-off.
- **When wrong:** Profile includes debug-only code paths inflating compile.

---

### Network Optimization

**18.** HTTP caching: `ETag`, `Cache-Control`, pooling, batching.

- [x]

- **Thesis:** **OkHttp cache** honors **`Cache-Control: max-age`**, **`ETag`/`If-None-Match`** revalidation 304 saves bandwidth; **connection pooling** reuse TLS sessions; **batch** endpoints reduce chatty mobile radio tail energy.
- **Mechanism / order:** `CacheInterceptor` ordering: **conditional** GET after cache hit stale policy.
- **Edge cases:** **Authenticated** responses `Cache-Control: private` only.
- **Production hook:** **Chucker** debug validate headers.
- **When wrong:** `cache-control: no-store` ignored by custom hack.

**19.** OkHttp Interceptor: logging, retry, caching.

- [x]

- **Thesis:** **Application interceptors** see **original** request; **Network interceptors** see **possibly** retried/overridden; implement **logging** (sanitize auth headers), **retry** with backoff idempotent GET only, **cache** control custom header injection.
- **Mechanism / order:** Order matters—**auth** before **logging** to strip secrets.
- **Edge cases:** **Interceptor** recursion if mutates incorrectly.
- **Production hook:** **HttpLoggingInterceptor` BODY` level never prod**.
- **When wrong:** Logging bearer tokens to Logcat in release.

**20.** Reduce APK size: shrink, WebP, splits.

- [x]

- **Thesis:** **R8 shrink** unused code/resources; **`resConfigs` limit** languages; **WebP/AVIF** drawables; **vector** where simple; **`abiSplits`/`densitySplits`** or **Play App Bundle** automatic splits; **remove** dead modules; **compress** native libs `useLegacyPackaging` trade-offs.
- **Mechanism / order:** **`androidResources { ignoreAssetsPattern }`** patterns.
- **Edge cases:** **Dynamic feature** reduces base size.
- **Production hook:** **APK Analyzer** CI diff report.
- **When wrong:** Shipping all language resources “just in case”.

---

### Battery Optimization

**21.** Doze and App Standby effects.

- [x]

- **Thesis:** **Doze** batches network jobs when device stationary screen off; **App Standby** buckets **ACTIVE/WORKING_SET/FREQUENT/RARE** limiting jobs, **FCM high-priority** quotas; **FGS** exempt but policy constrained.
- **Mechanism / order:** `setRequiresBatteryNotLow` etc. in WM constraints.
- **Edge cases:** **Maintenance windows** white bursts.
- **Production hook:** **Battery Historian** trace wake locks.
- **When wrong:** Polling every minute in background ignoring buckets.

**22.** Battery Historian.

- [x]

- **Thesis:** Parses **bugreport** (`adb bugreport`) into **HTML timeline** of **wake locks**, **GPS**, **jobs**, **alarms**, **CPU**—identify **spikes** vs baseline.
- **Mechanism / order:** Run on **desktop** docker image `battery-historian`.
- **Edge cases:** **Attribution** to your app vs Play services tricky—read package rows.
- **Production hook:** Pair with **internal** analytics of feature usage timestamps.
- **When wrong:** Expecting Historian to auto-fix bug—diagnostic only.

**23.** WorkManager vs battery optimization.

- [x]

- **Thesis:** WM **defers** work respecting **Doze/standby** windows unless **expedited** path within quota; **persistent** work still not guaranteed exact-time.
- **Mechanism / order:** Use **FGS** only when user-visible long task policy requires.
- **Edge cases:** **Exact alarm** permission separate API for calendar-type apps.
- **Production hook:** Log **runAttemptCount** + constraints at execution start.
- **When wrong:** Replacing WM with infinite `while(true)` worker thread.
