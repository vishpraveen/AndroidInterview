# Answers — §10 Architecture and Design Patterns

Questions: [questions.md](../questions.md#10-architecture-and-design-patterns).

---

### Clean Architecture

**1.** Layers: presentation / domain / data — what lives where.

- [x]

- **Thesis:** **Domain**: entities, use cases, **pure Kotlin** business rules—**no** Android imports. **Data**: repositories impl, DTOs, Retrofit/Room, mappers to domain. **Presentation**: ViewModel/UI state, mapping domain→UI models.
- **Mechanism / order:** **Dependency rule**: outer depends inner interfaces only—domain defines `Repository` ports, data implements.
- **Edge cases:** **Parcelize** models belong presentation/data edges not domain if you want purity—use domain `Id` value types mapped.
- **Production hook:** `module` graph test: domain module zero `android.*` imports.
- **When wrong:** `Context` in domain “just once”.

**2.** Use case / interactor; VM → repo directly?

- [x]

- **Thesis:** **Use case** wraps one business action orchestrating repos—useful when **non-trivial** coordination, **reused** across ViewModels, or **test slice** clarity. Simple CRUD screens: VM→repo acceptable if rules stay in domain layer functions.
- **Mechanism / order:** `class ObserveUser(private val repo: UserRepo) { operator fun invoke(): Flow<User> }`.
- **Edge cases:** **Anemic** use cases that only delegate one call—noise.
- **Production hook:** Introduce use cases when second caller appears or logic > N lines with branches.
- **When wrong:** God `UserInteractor` 3000 lines.

**3.** Dependency rule; domain without Android.

- [x]

- **Thesis:** Inner layers **know nothing** of frameworks—enables **JVM unit tests**, reuse in **KMP**, prevents **leaky** lifecycle into business rules.
- **Mechanism / order:** Inversion via **interfaces** in domain, implementations in data.
- **Edge cases:** **Time**/`UUID`—inject `Clock` interface.
- **Production hook:** ArchUnit `noClassesThat().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAPackage("android..")`.
- **When wrong:** Importing `Uri` in domain because convenient.

**4.** Clean Architecture over-engineering threshold.

- [x]

- **Thesis:** Small prototypes / hackathons: **layered modules** may slow iteration; threshold crosses when **multiple devs**, **longevity >1y**, **test debt** painful, **KMP** planned—then boundaries pay rent.
- **Mechanism / order:** Start **logical packages** before physical modules if unsure.
- **Edge cases:** **Superapp** needs stricter boundaries early.
- **Production hook:** Measure **build time** + **onboarding** time gains from modules.
- **When wrong:** 3-screen app with 12 modules “because clean”.

**5.** Mappers between layers; worth boilerplate?

- [x]

- **Thesis:** Map at **boundaries** where shapes diverge (API DTO vs domain vs UI state). Worth it when **invariants** differ (e.g., cents vs dollars). For 1:1 trivial fields, **mapper generation** or inline extension acceptable.
- **Mechanism / order:** Avoid leaking **Gson** annotations into domain.
- **Edge cases:** **Pagination** models—map page metadata once.
- **Production hook:** `toDomain()` / `toUi()` naming convention.
- **When wrong:** Triple mapping identical data classes “for purity”.

---

### MVVM

**6.** MVVM; ViewModel survives rotation; who holds instance.

- [x]

- **Thesis:** **ViewModel** stores **UI-related** state/logic surviving **config changes** via **`ViewModelStore`** owned by **scope** (`Activity`, `Fragment`, `NavBackStackEntry`).
- **Mechanism / order:** `ViewModelProvider(factory)` retrieves same instance until scope cleared (finish or fragment removed permanently).
- **Edge cases:** **Nav** scoped VM tied to graph lifecycle not just fragment attach order surprises.
- **Production hook:** `viewModels()` delegate with default factory.
- **When wrong:** Storing **Activity** in VM.

**7.** `ViewModelStore` / `ViewModelStoreOwner`.

- [x]

- **Thesis:** **Store** is map `String -> ViewModel`; **Owner** exposes `getViewModelStore()` + default factory; Activity implements; Fragment has **child** store; Navigation creates **nested** owners per back stack entry for scoped VMs.
- **Mechanism / order:** `ViewModelProvider(owner, factory)`.
- **Edge cases:** **Manual** `ViewModelStore` for Compose navigation custom hosts.
- **Production hook:** Clear stores on logout by `viewModelStore.clear()` rare pattern.
- **When wrong:** Creating `ViewModelProvider(this).get` with wrong owner—new VM each time.

**8.** Should ViewModel hold `Context`? `AndroidViewModel`?

- [x]

- **Thesis:** Avoid **Activity Context**—leaks rotations if long ops hold reference. **`AndroidViewModel`** provides **`Application` context** for resources/strings only—still keep IO off main.
- **Mechanism / order:** Prefer injecting **`@ApplicationContext Context`** via Hilt narrow interface `ResourcesProvider`.
- **Edge cases:** **Theme** needs Activity context—resolve in UI layer not VM.
- **Production hook:** Wrap `Resources.getString` calls in test doubles.
- **When wrong:** `AndroidViewModel` as excuse for DI everything.

**9.** One-time events without `SingleLiveEvent` hacks.

- [x]

- **Thesis:** **`Channel` CONFLATED** / **`SharedFlow` replay=0 extraBuffer=1** with **`tryEmit`** / **`Channel`** for events; or **Navigation** handle events as **state transition** with **consumed** flag reducer pattern (`IfSome(consumed=false)`).
- **Mechanism / order:** `SharedFlow` + `resetReplayCache` not needed if not replaying.
- **Edge cases:** Rotation replay—mark consumed in **`SavedStateHandle`**.
- **Production hook:** Official pattern: **`Channel<Event>`** collected in UI `LaunchedEffect`.
- **When wrong:** `LiveData` postValue race losing events—same class of bug in naive SharedFlow.

**10.** `LiveData` vs `StateFlow` from ViewModel.

- [x]

- **Thesis:** **`StateFlow`**: Kotlin-first, operators, cold-to-hot via `stateIn`, **testable** with Turbine. **`LiveData`**: lifecycle-aware delivery built-in but **main-thread** setValue constraints, fewer operators—still fine in Java interop legacy.
- **Mechanism / order:** Expose **`StateFlow` + `asLiveData()`** bridge if needed.
- **Edge cases:** **`LiveData.distinctUntilChanged`** default vs `StateFlow` structural equality.
- **Production hook:** New code: **StateFlow** default.
- **When wrong:** Mixing both for same screen without boundary—confusing.

---

### MVI

**11.** MVI: Intent, State, SideEffect.

- [x]

- **Thesis:** **Intent** user/system events; **State** single immutable snapshot of UI; **SideEffect** imperative world (nav, toast) emitted alongside or via channel to avoid polluting state.
- **Mechanism / order:** `reduce(old, intent) -> newState` pure where possible.
- **Edge cases:** **Navigation** args as part of state vs side effect—pick consistently.
- **Production hook:** Time-travel debugging with **event log**.
- **When wrong:** Giant `when` reducer without decomposition.

**12.** UDF benefits debugging/testing.

- [x]

- **Thesis:** Single direction **event↓ state↑** makes reproduction **deterministic** given intents sequence; tests assert **reducer** pure functions easily.
- **Mechanism / order:** Log intents in debug drawer.
- **Edge cases:** **Async** intents need explicit `Loading` states—not magic booleans scattered.
- **Production hook:** Redux DevTools-like internal overlay optional.
- **When wrong:** Sneaking side effects inside reducer.

**13.** Side effects in MVI without losing them.

- [x]

- **Thesis:** Dedicated **`Flow<Effect>`** or **`Channel`** consumed once in UI; or **`reduce` returns `State+Effects`** wrapper type processed by runtime separating application order.
- **Mechanism / order:** Process effects **after** state commit to avoid inconsistent UI.
- **Edge cases:** Process death—persist critical nav intents via **SavedState**.
- **Production hook:** Orbit **sideEffect** container pattern.
- **When wrong:** Launching navigation inside reducer coroutine without structured scope.

**14.** Orbit MVI vs MVIKotlin vs custom.

- [x]

- **Thesis:** **Orbit**: ergonomic DSL, testing support. **MVIKotlin**: decompose/binder multiplatform-ish concepts. **Custom**: minimal deps, you own threading—more work.
- **Mechanism / order:** Evaluate **KMP** alignment if needed.
- **Edge cases:** Library **version** coupling with Kotlin.
- **Production hook:** Spike one screen before standardizing.
- **When wrong:** Adopting heavy DSL team hates reading.

**15.** Bloated MVI state — mitigation.

- [x]

- **Thesis:** **Split** state by feature area with **nested** data classes + **selectors** (`derivedStateOf` Compose side); **multiple** reducers composed; **UDF per tab** with shared session slice.
- **Mechanism / order:** `copy` ergonomics with `data class` decomposition.
- **Edge cases:** Cross-field validation—domain validator returns **errors map** not booleans everywhere.
- **Production hook:** `State` per **navigation destination** scoped VM.
- **When wrong:** 80-field flat `UiState` updated per keystroke.

---

### Modularization

**16.** Module types: app, feature, core, data, domain, design-system.

- [x]

- **Thesis:** **`:app`** wires DI + navigation; **`:feature:x`** UI+VM+feature nav; **`:core:*`** networking, analytics interfaces; **`:data`** repo implementations; **`:domain`** pure; **`:designsystem`** Compose/theme/components only public API.
- **Mechanism / order:** Gradle **`implementation`** arrows point inward to core/domain.
- **Edge cases:** **Dynamic feature** modules optional delivery.
- **Production hook:** `includeBuild` for build-logic.
- **When wrong:** `:featureA` depends `:featureB` directly.

**17.** Module boundaries; prevent feature→feature.

- [x]

- **Thesis:** **ArchUnit** / **Gradle dependency analysis** `junit` rules: `feature.*` cannot depend on sibling `feature.*`; only `:core:navigation-api` with **sealed routes** implemented centrally.
- **Mechanism / order:** **Interface modules** (`:feature:api`) exposing only DTO/events if cross-feature communication needed.
- **Edge cases:** **Shared** models duplication vs `:core:model` shared kernel.
- **Production hook:** Codeowners per module.
- **When wrong:** “Temporary” dependency becomes permanent.

**18.** Navigation across modules without mutual deps.

- [x]

- **Thesis:** **Deep link routes** / **navigation graph includes** resolved in app module; feature exposes **`NavGraphBuilder` extension** functions via **plugin** pattern registered in app; **Compose** type-safe routes in shared module.
- **Mechanism / order:** **R** class not shared across dynamic features historically—use **Gradle`android.nonTransitiveRClass`** patterns.
- **Edge cases:** **Hilt** multibinding `NavGraph` contributors.
- **Production hook:** Public **`Routes`** object in `:core:navigation`.
- **When wrong:** Reflection navigation string in each feature.

**19.** Build-time benefit; parallel compilation.

- [x]

- **Thesis:** **ABI stability** boundaries let Gradle compile modules in **parallel** workers; **incremental** scope smaller per change.
- **Mechanism / order:** **Configuration cache** + many modules can increase config time—balance.
- **Edge cases:** **KAPT** in leaf modules still bottlenecks graph.
- **Production hook:** Measure **compile** time per module in CI.
- **When wrong:** One mega-module `internal` everything—no wins.

**20.** Share design system across features.

- [x]

- **Thesis:** **`:designsystem`** module exposes **public** composables/tokens only; **no** business logic; **themes** via `CompositionLocal` providers; **versioned** artifact consumed as normal dependency.
- **Mechanism / order:** **Screenshot tests** in design system module.
- **Edge cases:** **Theming** per brand flavor—parameterize tokens.
- **Production hook:** Figma token export pipeline optional.
- **When wrong:** Copy-paste composables between features diverging styles.

---

### Dependency Injection Patterns

**21.** Service Locator vs DI.

- [x]

- **Thesis:** **SL** hides dependencies behind global registry—**implicit**, hard to test/fake. **DI** wires **explicit** constructor params—graph visible, testable.
- **Mechanism / order:** Even `EntryPoint` is controlled escape hatch vs `ServiceLocator.get()`.
- **Edge cases:** **Framework** requires default ctor (Android `Worker`)—use `EntryPoint`/`AssistedInject`.
- **Production hook:** Hilt **testing replace modules**.
- **When wrong:** `object ServiceLocator` in library SDK.

**22.** Constructor vs field injection; Hilt preference.

- [x]

- **Thesis:** **Constructor injection** immutable, required dependencies obvious, easier tests. **Field** `@Inject lateinit` only where framework mandates fragments sometimes—still discouraged.
- **Mechanism / order:** Hilt generates **members injectors** for Android types with lifecycle.
- **Edge cases:** **AssistedInject** for runtime args + graph deps.
- **Production hook:** Detekt rule ban `lateinit var` injections in new Kotlin classes.
- **When wrong:** `@Inject lateinit` everywhere in ViewModels.

**23.** Composition root in Android app.

- [x]

- **Thesis:** Where **graph starts**: `@HiltAndroidApp` `Application` + **Activity** `super.onCreate` injection; manual DI: **custom `Application`** builds graph once.
- **Mechanism / order:** **Test** composition root builds fakes.
- **Edge cases:** **Multi-process** has **multiple** composition roots.
- **Production hook:** Keep graph creation **off main** if heavy—use lazy + App Startup.
- **When wrong:** Creating subcomponents in random `Util` class.

---

### Reactive and Repository Patterns

**24.** Repository returns Flow vs LiveData vs suspend.

- [x]

- **Thesis:** Prefer **`Flow`** for observable data streams; **`suspend`** for one-shot; **`LiveData`** legacy interop only. Repository abstracts **sources**—caller chooses collection strategy.
- **Mechanism / order:** `Flow` allows **operators** + structured concurrency.
- **Edge cases:** **Cold vs hot**—`shareIn` at right layer (often VM not repo) depends on multi-collector policy.
- **Production hook:** Repo exposes **`Flow<Domain>`**, VM applies `stateIn`.
- **When wrong:** Returning `LiveData` from Room DAO through repo to Compose without bridge.

**25.** SSOT with Room + Retrofit.

- [x]

- **Thesis:** **Room** table is authoritative local; network refresh **writes** DB; UI reads **only** Room `Flow`—**no** parallel memory cache unless derived with clear invalidation.
- **Mechanism / order:** `RemoteMediator` or custom `NetworkBoundResource` pattern.
- **Edge cases:** **Pagination** cursors stored in DB.
- **Production hook:** `distinctUntilChanged` on stable row hashes optional optimization.
- **When wrong:** Keeping `mutableListOf` in VM synced manually to Room.

**26.** Offline-first; optimistic UI.

- [x]

- **Thesis:** Apply UI change **immediately** writing **pending** row + enqueue **sync job**; on server ack flip **confirmed** flag; on failure **rollback** + user message.
- **Mechanism / order:** **Idempotency keys** on API.
- **Edge cases:** **Conflict resolution** strategies (LWW, merge, CRDT).
- **Production hook:** `WorkManager` retry with backoff for sync queue.
- **When wrong:** Optimistic delete without tombstone—cannot undo sync.

**27.** Data sources; local vs remote behind repo.

- [x]

- **Thesis:** **`UserRemoteDataSource`**, **`UserLocalDataSource`** interfaces; **repo** orchestrates caching policy, mapping, error translation to domain **`Result`** types.
- **Mechanism / order:** Remote uses Retrofit; local uses DAO.
- **Edge cases:** **Bypass cache** flag for pull-to-refresh.
- **Production hook:** Fake both in integration tests.
- **When wrong:** ViewModel calling Retrofit directly “temporary”.

**28.** `NetworkBoundResource` mediator pattern.

- [x]

- **Thesis:** **Flow** builder: emit **`Resource.Loading` + cached** if any, fetch network, map to domain, **persist** to DB, emit **`Success`** from DB flow; errors become **`Error`** with last cache optional.
- **Mechanism / order:** Now often replaced by **`remoteMediator`** + Room `Flow` simpler.
- **Edge cases:** **First launch** no cache—handle empty state.
- **Production hook:** Central `executeWithRetry` for network step.
- **When wrong:** Emitting success with stale DB because forgot `withTransaction` invalidation.
