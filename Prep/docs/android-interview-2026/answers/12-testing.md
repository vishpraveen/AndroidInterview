# Answers — §12 Testing

Questions: [questions.md](../questions.md#12-testing).

---

### Unit Testing

**1.** Testing pyramid on Android; ratio guidance.

- [x]

- **Thesis:** Many **fast unit** tests (domain/VM), fewer **integration** (DB/Retrofit fakes), fewest **UI** (Espresso/Compose) due to cost/flakiness—**no magic ratio**, but **70/20/10** heuristic often cited; prioritize **business risk** coverage over percentages.
- **Mechanism / order:** JVM tests run in CI every PR; UI nightly.
- **Edge cases:** **Compose** screenshot tests blur line integration/visual.
- **Production hook:** Track **test duration** budget per module.
- **When wrong:** 0 unit, 400 flaky UI tests “we have coverage”.

**2.** Unit-test ViewModel; mock; `TestDispatcher`.

- [x]

- **Thesis:** Instantiate VM with **fake repos** + **`StandardTestDispatcher`**; `Dispatchers.setMain(testDispatcher)`; drive `onIntent` methods; `advanceUntilIdle()`; assert **`StateFlow`** values via `Turbine` or `value` snapshots.
- **Mechanism / order:** Avoid real `Dispatchers.IO`—inject dispatchers.
- **Edge cases:** **`viewModelScope`** uses `Dispatchers.Main`—must set main test dispatcher.
- **Production hook:** `MainDispatcherRule` JUnit extension.
- **When wrong:** `runBlocking` in every test hiding missing dispatcher rules.

**3.** MockK vs Mockito for Kotlin.

- [x]

- **Thesis:** **MockK** handles **final classes**, **object mocks**, **extension** functions, **coroutines** `coEvery` naturally; **Mockito** + **inline-mock-maker** workable but more ceremony for Kotlin specifics.
- **Mechanism / order:** `relaxed = true` sparingly.
- **Edge cases:** **Static** mocks possible but smell—wrap in class.
- **Production hook:** MockK default for greenfield Kotlin Android.
- **When wrong:** Mockito cannot mock `data class` final—workarounds brittle.

**4.** Unit-test Repository with Room + Retrofit.

- [x]

- **Thesis:** **Fake local** in-memory Room or `HashMap` DAO fake; **MockWebServer** for HTTP; drive repo public API; assert **DB rows** + **HTTP** request paths. Alternatively **contract tests** split: pure mapper unit tests + integration with real Room `MigrationTestHelper` separate layer.
- **Mechanism / order:** Keep **mapper** tests pure JVM without Android if possible.
- **Edge cases:** **Idling** not needed in JVM repo tests.
- **Production hook:** **Turbine** on `Flow` emissions sequence `awaitItem()`.
- **When wrong:** Hitting production sandbox URLs in unit tests.

**5.** Turbine for Flow testing.

- [x]

- **Thesis:** DSL `test { awaitItem(); awaitComplete() }` with **timeouts**, **cancellation** assertions, **expectNoEvents()` gaps—clearer than manual `take(1)`.
- **Mechanism / order:** `testScheduler` integration with `runTest`.
- **Edge cases:** **Hot** flows never complete—use `cancelAndConsumeRemainingEvents`.
- **Production hook:** Assert **error** terminal events cleanly.
- **When wrong:** Forgetting `test { }` closure leads to hanging.

**6.** Testing time-dependent code; `Clock`.

- [x]

- **Thesis:** Inject **`Clock` interface** (`java.time.Clock.fixed`) or Kotlin `TimeSource` abstraction; in tests **advance** fake clock for TTL/expiry logic.
- **Mechanism / order:** Avoid `System.currentTimeMillis()` static in domain.
- **Edge cases:** **Time zones**—use `ZonedDateTime` explicit in tests.
- **Production hook:** `Clock` bean in Hilt test module replaces with fixed.
- **When wrong:** `Thread.sleep` in tests to wait TTL.

---

### Integration Testing

**7.** Unit vs integration (Android).

- [x]

- **Thesis:** **Unit**: single class collaborators mocked/faked, JVM fast. **Integration**: multiple real Android components (Room+SQL, WorkManager test, Hilt+Activity) on **device/Robolectric** verifying **wiring**.
- **Mechanism / order:** Name directories `androidTest` vs `test`.
- **Edge cases:** **Robolectric** “integration” still not real graphics stack.
- **Production hook:** Tag `@LargeTest` for CI sharding.
- **When wrong:** Calling 5-layer stack “unit test” because it runs on JVM with Robolectric.

**8.** Robolectric integration; limitations.

- [x]

- **Thesis:** Runs Android framework **shadows** on JVM—great for **logic** using `Context`, `PackageManager`, **SQLite** shadows; **not** real **HW UI**, **Camera**, **EGL**, some **APIs** incomplete/behavior drift vs device.
- **Mechanism / order:** `@Config(sdk = [33])` pin behavior.
- **Edge cases:** **Native** code `.so` won’t load same as device.
- **Production hook:** Use for **Presenter/VM** + `LocalBroadcastManager` legacy patterns.
- **When wrong:** Testing **OpenGL** filters with Robolectric only.

**9.** Hilt integration tests: `@HiltAndroidTest`, `@UninstallModules`.

- [x]

- **Thesis:** **`@HiltAndroidTest`** + **`HiltAndroidRule`** replaces bindings with **`@TestInstallIn`/`@Replace`** modules; **`@UninstallModules`** removes production module to substitute fakes for **network** endpoints.
- **Mechanism / order:** Use **`@BindValue`** in test to override specific instances quickly.
- **Edge cases:** **Custom TestRunner** `HiltTestApplication` manifest merge.
- **Production hook:** Keep **fakes** in `debug/` source set for manual QA too.
- **When wrong:** Production `NetworkModule` still hitting internet in `androidTest`.

---

### UI Testing

**10.** Espresso vs Compose Testing; `ComposeTestRule`.

- [x]

- **Thesis:** **Espresso**: ViewInteraction on view hierarchy, Idling resources. **Compose**: `createComposeRule()` sets **content** + provides **`onNodeWithTag`/`Text` matchers**, **synchronization** via idle APIs + **testTag** semantics.
- **Mechanism / order:** Compose tests **recomposition** aware waits.
- **Edge cases:** **Animations** infinite—use `disableAnimations` test option or global rule.
- **Production hook:** **`semantics { testTagsAsResourceId = true }`** for hybrid.
- **When wrong:** Using sleep instead of idle synchronization.

**11.** Test navigation end-to-end.

- [x]

- **Thesis:** **NavController** test with `TestNavHostController` for VM unit scope; **instrumented** tests drive UI clicks verifying **destination** id; **Compose Navigation** `navController.currentBackStackEntry`.
- **Mechanism / order:** Avoid testing framework internals—assert **screen outcomes**.
- **Edge cases:** **Deep links** require `ActivityScenario` + `Intents` stubbing.
- **Production hook:** **Maestro**/`UIAutomator` for black-box cross-app flows optional.
- **When wrong:** Tight coupling tests to animation durations.

**12.** Robot Pattern.

- [x]

- **Thesis:** Encapsulate screen interactions in **`LoginRobot`** methods (`typeEmail`, `tapSignIn`) hiding matchers—tests read **Given/When/Then** fluent.
- **Mechanism / order:** Robots compose for flows `LoginRobot().submitValidUser()`.
- **Edge cases:** Robots still need **shared** idling setup.
- **Production hook:** Reduce duplication when redesign changes selectors in one file.
- **When wrong:** God robot 800 lines—split per screen.

**13.** Async + animations in UI tests.

- [x]

- **Thesis:** **IdlingResource** wraps espresso; Compose has **auto-advance main clock** APIs `mainClock.autoAdvance = false` then advance frames; **Espresso** `IdlingPolicies` timeouts tuning; replace **infinite** animations in test build flavors.
- **Mechanism / order:** `registerIdlingResource` for your executor queues.
- **Edge cases:** **Lottie** `repeatCount` 0 in tests via `BuildConfig.DEBUG` injection.
- **Production hook:** `InstantTaskExecutorRule` legacy LiveData tests.
- **When wrong:** `Thread.sleep(2000)` everywhere.

---

### Screenshot Testing

**14.** Screenshot testing; Paparazzi without device.

- [x]

- **Thesis:** **Paparazzi** renders composables/previews using **layoutlib** on JVM producing images compared to golden PNGs—**deterministic** CI without emulator cost.
- **Mechanism / order:** `@Paparazzi` test functions record/verify modes.
- **Edge cases:** **Font** rendering differences OS—pin **Roboto** test fonts.
- **Production hook:** **CI** fails on diff; designers approve via PR image viewer.
- **When wrong:** Testing full-device system bars in Paparazzi—wrong tool.

**15.** Screenshots across theme/locale/font scale.

- [x]

- **Thesis:** Parameterize tests with **`Paparazzi` device config** + `NightMode` + `Locale` + `FontScale` options (APIs evolve—use `DeviceConfig`). Generate matrix of goldens or separate test methods.
- **Mechanism / order:** Keep matrix size manageable—focus high-risk screens.
- **Edge cases:** **RTL** mirroring layout issues only visible in screenshots—great value.
- **Production hook:** **Record** script in CI nightly updating goldens PR optional bot.
- **When wrong:** Single light-only golden missing dark regressions.

**16.** PR workflow for screenshot approval.

- [x]

- **Thesis:** CI posts **image diff** artifact; **CODEOWNERS** for `**/snapshots/**`; **policy**: if diff intentional, reviewer approves and merges updated goldens in same PR; **LFS** if large.
- **Mechanism / order:** Block merge if `verifyPaparazzi` fails.
- **Edge cases:** **Compression** noise—tolerance settings limited; prefer vector deterministic assets.
- **Production hook:** `paparazzi.maxPercentDifference` thresholds if supported cautiously.
- **When wrong:** Developers locally never run record—CI always red.

---

### Testing Strategy and Culture

**17.** Lead testing strategy; minimum coverage enforcement.

- [x]

- **Thesis:** Define **risk-based** targets: payment/auth **near 100%** critical paths; UI smoke suite; **coverage %** as **signal** not goal—enforce **non-decreasing** coverage on touched modules via Jacoco diff maybe.
- **Mechanism / order:** **Definition of Ready** includes test plan; **DoD** includes tests + screenshots for UI.
- **Edge cases:** **Generated code** excluded from jacoco.
- **Production hook:** **Sonar** quality gate on new code coverage only.
- **When wrong:** 90% coverage with meaningless asserts.

**18.** Legacy zero tests — where to start.

- [x]

- **Thesis:** **Characterization tests** around **bug fixes** first (prevent regression); add **tests** when touching module for feature; carve **domain** pure Kotlin extract easiest wins; **critical user journeys** instrumented smoke next.
- **Mechanism / order:** **Strangler** rewrite hotspots with tests behind interface.
- **Edge cases:** **Flaky** legacy UI—stabilize infra before broadening.
- **Production hook:** Track **MTTR** regressions metric.
- **When wrong:** “Stop features 3 months to write tests” unrealistic—incremental.

**19.** TDD on Android; when valuable.

- [x]

- **Thesis:** **Red-green-refactor** loop great for **pure logic** (pricing rules, parsers). For **UI-heavy** exploratory design, **test-after** sometimes faster—hybrid adult engineering.
- **Mechanism / order:** Outside-in vs inside-out approaches team choice.
- **Edge cases:** **Compose** previews complement TDD for visual iteration.
- **Production hook:** Mob programming spikes for algorithmic modules TDD.
- **When wrong:** Dogmatic TDD for every composable pixel tweak.

**20.** Coverage tracking; is 100% good?

- [x]

- **Thesis:** **Line/branch** coverage metrics via **JaCoCo** in CI artifacts; **100%** often **wastes** effort testing trivial getters/boilerplate and encourages **bad tests** gaming metric; better: **mutation testing** (PIT) selective on domain.
- **Mechanism / order:** Exclude DTOs, generated, DI modules from gates optionally.
- **Edge cases:** **Integration** paths uncovered despite high line coverage.
- **Production hook:** Track **coverage on changed lines** only.
- **When wrong:** Removing `if (BuildConfig.DEBUG)` branches from coverage by deleting safety checks.
