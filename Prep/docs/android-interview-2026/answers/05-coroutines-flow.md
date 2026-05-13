# Answers — §5 Kotlin Coroutines and Flows

Questions: [questions.md](../questions.md#5-kotlin-coroutines-and-flows).

---

### Coroutine Fundamentals

**1.** Coroutine vs thread; millions?

- [x]

- **Thesis:** Coroutine is **lightweight task** scheduled on **thread pool** (`Dispatchers`) with **suspend** points; **stack** fragmented into **continuation frames**—cheap to create vs OS thread (MB stacks, kernel objects).
- **Mechanism / order:** One thread runs **many** coroutines between suspensions.
- **Edge cases:** Still bounded by **memory** for huge channel buffers etc.
- **Production hook:** Default dispatcher thread count ≈ **CPU cores**.
- **When wrong:** Blocking `Thread.sleep` inside coroutine—ties up thread, kills scalability.

**2.** `CoroutineScope`, `CoroutineContext`, `Job`.

- [x]

- **Thesis:** **Scope** = lifecycle boundary for coroutines; **Context** = immutable **bag** of elements (`Job`, `Dispatcher`, `CoroutineName`, user data); **Job** = cancellable handle + parent-child hierarchy.
- **Mechanism / order:** `scope.launch` inherits context + structured concurrency.
- **Edge cases:** **`SupervisorJob`** vs regular `Job` failure propagation differs.
- **Production hook:** `SupervisorJob()` in `ViewModel` `viewModelScope` pattern variants.
- **When wrong:** Creating `CoroutineScope(Job())` without tying to lifecycle—orphans.

**3.** Dispatchers: Main / IO / Default / Unconfined.

- [x]

- **Thesis:** **Main** = UI thread (Android); **IO** = **blocking IO**-optimized larger pool; **Default** = **CPU** work sized to cores; **Unconfined** resumes caller **without** dispatcher affinity—**rare**, mostly tests/debug.
- **Mechanism / order:** `withContext(Dispatchers.IO)` offloads blocking.
- **Edge cases:** **Main.immediate** for same-thread continuation when already on main.
- **Production hook:** Never block Main; use **IO** for Retrofit? Actually Retrofit `suspend` uses OkHttp threads—still avoid heavy parse on Main.
- **When wrong:** `Default` for blocking JDBC—starves CPU pool.

**4.** Structured concurrency; child failure; siblings.

- [x]

- **Thesis:** Child **Job** failure **cancels** parent scope by default (`Job`); **siblings** cancelled when parent cancelled or fail-fast in same scope depending on builder.
- **Mechanism / order:** `coroutineScope { }` waits children; failure **throws** to caller.
- **Edge cases:** **`async`** deferred exception until **await**—still marks failed state.
- **Production hook:** Use **`supervisorScope`** for UI parallel requests where one failure should not kill others.
- **When wrong:** Swallowing child exception without handling—lost error.

**5.** `SupervisorJob` vs `supervisorScope`.

- [x]

- **Thesis:** **`SupervisorJob`**: child failures **do not** cancel parent/siblings (child cancelled only). **`supervisorScope`**: structured block using supervisor semantics for **scoped** launches.
- **Mechanism / order:** Install `SupervisorJob` in **ViewModel** scope factory when needed.
- **Edge cases:** Still must **handle** each child exception via **`CoroutineExceptionHandler`** or `try/catch` inside child.
- **Production hook:** Parallel section loads on detail screen.
- **When wrong:** Expecting supervisor to swallow exceptions automatically without handler—still can crash if unhandled.

**6.** `launch` vs `async`; `async` without `await` pitfall.

- [x]

- **Thesis:** **`launch`** = **fire-and-forget** `Job`, exceptions propagate to context handler. **`async`** = **Deferred** result, exceptions stored until **await**/**join**.
- **Mechanism / order:** Forgotten `async` may **lose** exception until scope completion.
- **Edge cases:** **`async(start = CoroutineStart.LAZY)`** needs explicit start/await.
- **Production hook:** Prefer **`async`** only when you need **parallel** decomposition with **results**.
- **When wrong:** `async` for side effect only—use `launch`.

---

### Cancellation and Exception Handling

**7.** Cancellation; cooperative; `isActive` / `ensureActive`.

- [x]

- **Thesis:** Cancellation sets **cancel flag**; suspend points **throw** `CancellationException` cooperatively; **CPU-bound** loops must **`ensureActive()`** or `yield()` or check `isActive`.
- **Mechanism / order:** `withContext` checks cancellation between steps.
- **Edge cases:** **Non-cancellable** suspend rare (`NonCancellable`).
- **Production hook:** Use **`coroutineCancellation`** experimental APIs when needed.
- **When wrong:** Catching `Exception` and swallowing **`CancellationException`**—breaks structured concurrency.

**8.** `CancellationException` special treatment.

- [x]

- **Thesis:** Not an **error**—used for **control flow**; not reported to **`CoroutineExceptionHandler`** as failure; rethrow always.
- **Mechanism / order:** Propagates to cancel parent unless supervisor rules.
- **Edge cases:** Custom subclasses still treated as cancellation if type matches.
- **Production hook:** `catch (e: CancellationException) { throw e }` pattern in library `invokeSuspend`.
- **When wrong:** Mapping cancellation to user-visible error toast.

**9.** `NonCancellable`.

- [x]

- **Thesis:** Context element that **suppresses cancellation** for critical sections (e.g. **release mutex** / **close resource** during cancel).
- **Mechanism / order:** `withContext(NonCancellable) { ... }`.
- **Edge cases:** Keep **tiny**—never network here.
- **Production hook:** DB txn rollback section.
- **When wrong:** Long computation in `NonCancellable`—ignores user back press.

**10.** `CoroutineExceptionHandler`; install where.

- [x]

- **Thesis:** Catches **uncaught** exceptions from **`launch`** (not `async` await path) on **CoroutineScope** context; must be **`+` on root** scope or **`SupervisorJob`** sibling contexts per rules.
- **Mechanism / order:** Effective on **handler scope** itself for children in `launch`.
- **Edge cases:** Does not catch if exception in **`async` awaited**—normal `try/catch`.
- **Production hook:** Root Android `Application` scope logging.
- **When wrong:** Installing only on **child** context but exception thrown in **parent**—missed.

**11.** Exception propagation: `coroutineScope` vs `supervisorScope` vs `launch` vs `async`.

- [x]

- **Thesis:** **`coroutineScope`**: first child failure **fails scope** and cancels others. **`supervisorScope`**: child failure **localized**. **`launch`**: bubble to parent unless handled. **`async`**: stored until **await**.
- **Mechanism / order:** Combine with **handlers** carefully.
- **Edge cases:** Multiple concurrent failures—**suppressed** exceptions attached.
- **Production hook:** Unit tests asserting behavior per pattern.
- **When wrong:** Mixing patterns without diagram of scope tree.

---

### Flows

**12.** `Flow` vs LiveData / Rx Observable / Sequence.

- [x]

- **Thesis:** **`Flow`**: **cold** reactive stream with **suspend** operators, structured concurrency, **no** built-in Android lifecycle awareness (add **`flowWithLifecycle`**). **LiveData**: lifecycle-aware **hot** value holder. **Observable**: **cold/hot** Rx with richer operator history + **schedulers**. **Sequence**: sync blocking iterator—not async.
- **Mechanism / order:** `flow { emit }` runs collector-driven.
- **Edge cases:** **Hot** `SharedFlow` vs cold `flow`.
- **Production hook:** `StateFlow` replaces many `LiveData` in VM.
- **When wrong:** Using `Flow` like hot stream without `shareIn`—repeats side effects.

**13.** Cold vs hot Flows.

- [x]

- **Thesis:** **Cold**: collector triggers upstream each subscription (`flow { }`). **Hot**: producer independent of collectors (`SharedFlow`, channel-backed, callbackFlow with external source).
- **Mechanism / order:** `shareIn`/`stateIn` convert cold to hot with policy.
- **Edge cases:** **callbackFlow** can be hot if source pushes regardless.
- **Production hook:** `SharingStarted.WhileSubscribed` to save work.
- **When wrong:** Multiple collectors each hitting network without `shareIn`.

**14.** `StateFlow` vs LiveData; limitations.

- [x]

- **Thesis:** **`StateFlow`**: **hot** always has **current** value, **conflates** rapid updates (collectors may miss intermediate values), requires **initial** state, **not** lifecycle-aware alone.
- **Mechanism / order:** `MutableStateFlow.update { }` atomic CAS style.
- **Edge cases:** **Equality** suppression via `distinctUntilChanged` behavior by default? `StateFlow` uses `Any.equals`—must avoid if same structural value new instance.
- **Production hook:** Expose as `StateFlow`, collect with lifecycle in UI.
- **When wrong:** Using for **one-shot events**—use `Channel`/`SharedFlow` replay=0 extraBuffer.

**15.** `SharedFlow`; replay, extraBuffer, `onBufferOverflow`.

- [x]

- **Thesis:** Configurable **replay cache**, optional **buffer**, overflow policy **`SUSPEND`/`DROP_OLDEST`/`DROP_LATEST`**.
- **Mechanism / order:** `SharedFlow` for events; tune **`extraBufferCapacity`** to avoid suspension of slow collectors if acceptable to drop/suspend producers carefully.
- **Edge cases:** **`tryEmit`** fails if no buffer and no collectors depending on config.
- **Production hook:** UI events `Channel` vs `SharedFlow` trade-off—`Channel` fair single consumer often simpler.
- **When wrong:** Unbounded replay causing memory growth on high-churn events.

**16.** `callbackFlow`.

- [x]

- **Thesis:** **Cold** builder bridging callback APIs: `awaitClose { unregister() }` ensures teardown; uses **`trySend`/`close`** patterns.
- **Mechanism / order:** Runs in coroutine context of collector unless `flowOn`.
- **Edge cases:** **Backpressure**—use `trySend` + `Channel` or conflate.
- **Production hook:** Location / sensor listeners.
- **When wrong:** Forgetting **`awaitClose`** → leak.

**17.** `channelFlow` vs `flow` vs `callbackFlow`.

- [x]

- **Thesis:** **`channelFlow`**: producer can **`send`** from **multiple** coroutines inside builder—buffered **fan-in**. **`flow`**: single coroutine emitter (except unsafe `channelFlow` patterns). **`callbackFlow`**: specialized for callbacks.
- **Mechanism / order:** `channelFlow { launch { send } }` patterns.
- **Edge cases:** **Concurrent** `send` needs capacity or suspends.
- **Production hook:** Merge multiple data sources into one stream.
- **When wrong:** Using `channelFlow` where simple `flow` merge operators suffice.

**18.** Flow operators — practical scenarios (brief).

- [x]

- **Thesis:** **`map/filter`**: transform/filter emissions. **`flatMapLatest`**: search-as-you-type cancels stale request. **`flatMapConcat`**: ordered sequential expansions. **`flatMapMerge`**: concurrent merges with concurrency param. **`combine`**: join latest A+B. **`zip`**: pairwise lockstep. **`debounce`**: keystrokes. **`distinctUntilChanged`**: UI diff noise. **`conflate`**: keep only latest (UI fps).
- **Mechanism / order:** Each operator returns new cold flow unless hot upstream.
- **Edge cases:** **`flatMapMerge` default concurrency** unbounded dangerous.
- **Production hook:** `timeout` for network flows.
- **When wrong:** `combine` with never-completing flows hanging merge.

**19.** `flowOn` vs collect dispatcher.

- [x]

- **Thesis:** **`flowOn`** shifts **upstream** (including context propagation for `emit` block) to dispatcher; **downstream** stays collector context unless more `flowOn`s.
- **Mechanism / order:** Context elements like **`CoroutineName`** only propagate upstream from `flowOn` boundary.
- **Edge cases:** **`withContext` inside operator** also jumps threads.
- **Production hook:** `flowOn(Dispatchers.IO)` around mapping heavy bytes.
- **When wrong:** Expecting `flowOn` to move **terminal** `collect` off Main—must `withContext` on collector or `flowOn` last operator still leaves collect on caller unless structured.

**20.** `stateIn` / `shareIn`; `WhileSubscribed(5000)`.

- [x]

- **Thesis:** Convert cold to hot **`StateFlow`/`SharedFlow`** with sharing scope; **`WhileSubscribed(5000)`** stops upstream **5s after last subscriber** gone—saves battery vs `Eagerly`.
- **Mechanism / order:** Needs **`scope` + `started` + `replay`** tuning.
- **Edge cases:** **`Lazily`** starts on first subscriber without extra stop delay semantics difference.
- **Production hook:** ViewModel `viewModelScope` + `WhileSubscribed` for UI-bound sharing.
- **When wrong:** `Eagerly` in background service without subscribers—wastes work.

---

### Channels

**21.** Channels vs Flows.

- [x]

- **Thesis:** **Channel** is **hot** communication primitive with **send/receive** suspending; great for **handshake** / actor patterns. **Flow** better for **reactive** transformations & cancellation composition.
- **Mechanism / order:** `Channel` closed explicitly; Flow completes from builder.
- **Edge cases:** **Fan-out** needs `broadcast` deprecated—use **`SharedFlow`** now.
- **Production hook:** Actor mailbox with **`CoroutineScope` + `Channel.CONFLATED`** for latest command.
- **When wrong:** Exposing `Channel` across layers—tight coupling.

**22.** Channel types: RENDEZVOUS, BUFFERED, CONFLATED, UNLIMITED.

- [x]

- **Thesis:** **RENDEZVOUS** (0 buffer) handoff sync; **BUFFERED(n)** queues; **CONFLATED** keeps latest send drops older; **UNLIMITED** linked list buffer—**OOM** risk.
- **Mechanism / order:** Pick based on **backpressure** semantics.
- **Edge cases:** **`Channel.Factory` SPY** testing.
- **Production hook:** `Channel.CONFLATED` for UI intents latest-wins.
- **When wrong:** `UNLIMITED` on high throughput producer.

**23.** `produce` builder; `ReceiveChannel`.

- [x]

- **Thesis:** **`produce`**: coroutine builder returning **`ReceiveChannel`** owned by scope—structured producer.
- **Mechanism / order:** Cancelling scope closes channel.
- **Edge cases:** **`actor` deprecated**—manual pattern.
- **Production hook:** Bridge legacy queue APIs.
- **When wrong:** Leaking `produce` scope—must tie to parent job.

---

### Advanced Coroutines

**24.** `Mutex` / `Semaphore` vs `synchronized` / `ReentrantLock`.

- [x]

- **Thesis:** **Mutex**/**Semaphore** suspend instead of blocking thread—compose with coroutines; Java locks **block** threads—bad on Main, wastes thread pool under load.
- **Mechanism / order:** `mutex.withLock { }` suspends fair/nonfair per impl.
- **Edge cases:** **Deadlock** still possible if lock order inconsistent.
- **Production hook:** Protect **in-memory** cache updates in suspend code.
- **When wrong:** Using `synchronized` inside suspend on **IO dispatcher**—still blocks that thread.

**25.** `ThreadLocal` + `asContextElement`.

- [x]

- **Thesis:** Propagate **thread-local** semantics across **`withContext` hops** by attaching **`ThreadLocal.asContextElement(value)`** to coroutine context—restores old value on exit.
- **Mechanism / order:** Used in frameworks (tracing, MDC logging).
- **Edge cases:** Must **remove** properly—leaks between pooled threads if misused.
- **Production hook:** OpenTelemetry coroutine context bridging.
- **When wrong:** Reading `ThreadLocal` without context element after suspend—wrong thread.

**26.** Unit-test coroutines: `runTest`, `TestDispatcher`, `advanceUntilIdle`, `StandardTestDispatcher` vs `UnconfinedTestDispatcher`.

- [x]

- **Thesis:** **`runTest`** replaces `runBlockingTest`; virtual time; **`StandardTestDispatcher`** queues tasks explicitly **`advanceUntilIdle`/`runCurrent`**; **`UnconfinedTestDispatcher`** runs immediately like old `TestCoroutineDispatcher` eager—harder for ordering tests.
- **Mechanism / order:** `Dispatchers.setMain(testDispatcher)` for Main.
- **Edge cases:** **`pauseDispatcher`** for idling resources.
- **Production hook:** Turbine + `runTest` for Flow assertions.
- **When wrong:** Using `runBlocking` in tests masking missing `TestDispatcher`.

**27.** `select` expression.

- [x]

- **Thesis:** Waits **first** of multiple suspending operations (channels, `onAwait` on deferreds) — useful **race** / **multiplex**.
- **Mechanism / order:** `select<Unit> { channel.onReceive { } deferred.onAwait { } }`.
- **Edge cases:** **Clause fairness** starvation—document ordering.
- **Production hook:** Timeout first wins with `onTimeout`.
- **When wrong:** `select` in hot path without backpressure—complexity bomb.

**28.** Retry + exponential backoff with coroutines/Flow.

- [x]

- **Thesis:** `retryWhen` / custom `flow` loop with **`delay`** doubling capped max + **jitter**; respect **`CancellationException`**.
- **Mechanism / order:** `repeat(Int.MAX_VALUE)` with break on success; or OkHttp **RetryInterceptor** at boundary.
- **Edge cases:** **Idempotency** keys for POST retries.
- **Production hook:** `kotlinx.coroutines.flow.retry` with predicate filtering non-retryable HTTP codes.
- **When wrong:** Tight infinite retry hammering dead service.
