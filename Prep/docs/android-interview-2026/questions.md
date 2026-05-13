<!--
  Upstream: https://github.com/raaviswami/AndroidInterview2026/blob/main/android-interview-questions.md
  Frozen local copy for offline prep. Refresh from raw GitHub when the bank updates.
-->

# Android Interview Questions — Senior / Lead (12+ Years Experience)

---

## Table of Contents

1. [Android SDK and Platform Internals](#1-android-sdk-and-platform-internals)
2. [Android Jetpack Libraries](#2-android-jetpack-libraries)
3. [Jetpack Compose — Deep Dive](#3-jetpack-compose--deep-dive)
4. [Kotlin — Fundamentals to Advanced](#4-kotlin--fundamentals-to-advanced)
5. [Kotlin Coroutines and Flows](#5-kotlin-coroutines-and-flows)
6. [Database and Storage](#6-database-and-storage)
7. [Android Build System, Gradle, and Obfuscation](#7-android-build-system-gradle-and-obfuscation)
8. [Security](#8-security)
9. [Android DevOps and CI/CD](#9-android-devops-and-cicd)
10. [Architecture and Design Patterns](#10-architecture-and-design-patterns)
11. [Android System Design](#11-android-system-design)
12. [Testing](#12-testing)
13. [Performance and Optimization](#13-performance-and-optimization)
14. [Miscellaneous and Leadership](#14-miscellaneous-and-leadership)

---

## 1. Android SDK and Platform Internals

### Activity and Fragment Lifecycle

1. Walk through the complete Activity lifecycle when a user rotates the device with `android:configChanges` **not** set. What is the exact callback order, and where does `onSaveInstanceState` fit in relative to `onStop`?

2. Explain the difference between `onSaveInstanceState` and `onRetainNonConfigurationInstance` (now replaced by `ViewModel`). When would the system still kill state that a `ViewModel` holds?

3. What is the difference between `finish()`, `finishAffinity()`, and `finishAndRemoveTask()`? Give a real-world scenario for each.

4. Describe the Fragment lifecycle callbacks that have **no** Activity counterpart (e.g., `onCreateView`, `onViewCreated`, `onDestroyView`). Why does `onDestroyView` exist separately from `onDestroy`?

5. Explain `setMaxLifecycle()` on `FragmentTransaction`. How does ViewPager2 use it, and why is it important for off-screen fragments?

6. What happens when a `Dialog` is shown on top of an Activity — does `onPause` get called? What about a `DialogFragment`?

### Process Death and State Restoration

7. How does the Android OS decide which process to kill? Explain the process priority hierarchy (foreground, visible, service, cached).

8. How would you **simulate** process death during development and verify your app handles it correctly?

9. Your app crashes after process restoration because a singleton lost its state. How do you architect singletons to survive process death?

10. What is the `SavedStateHandle` in ViewModel, and how does it differ from regular ViewModel state?

### Services, Broadcasts, and IPC

11. Compare `Service`, `IntentService` (deprecated), `JobIntentService`, and `WorkManager`. What is the modern recommendation and why?

12. Explain the Binder framework in Android. How does AIDL work under the hood? What are the thread implications for the calling and receiving sides?

13. What are the limitations on implicit broadcasts starting from Android 8.0 (Oreo)? How do you work around them?

14. Explain `Messenger` vs `AIDL` for IPC. When would you choose one over the other?

15. What is a `bound service` vs a `started service`? Can a service be both? Explain the lifecycle implications.

16. How do `PendingIntent` flags (`FLAG_IMMUTABLE`, `FLAG_MUTABLE`, `FLAG_UPDATE_CURRENT`, `FLAG_ONE_SHOT`) affect security and behavior? What changed in Android 12?

### Content Providers and System Components

17. When would you implement a custom `ContentProvider`? Can it be used purely for in-app modular data access, and is that a good idea?

18. Explain how `FileProvider` works. What is the URI permission model, and why is it more secure than `file://` URIs?

19. What is the `Application` class used for? What are the pitfalls of doing heavy initialization there?

20. Explain the difference between `Context`, `Activity`, `Application`, and `Service` as Context. When does using the wrong one cause memory leaks or crashes?

### Window and View System

21. Explain the Android view rendering pipeline: `measure` → `layout` → `draw`. What triggers a re-layout vs a re-draw?

22. What is `hardware acceleration`, and when might you need to disable it for specific views?

23. Explain `WindowInsets` and how `edge-to-edge` rendering works on Android 15+. How do you handle system bars, display cutouts, and IME insets?

24. What is `SurfaceView` vs `TextureView`? When would you use each?

25. Explain how `RecyclerView` view recycling works internally. What is the difference between the scrap heap and the recycled view pool?

---

## 2. Android Jetpack Libraries

### Navigation Component

1. How does the Navigation component handle the back stack? What is the difference between `popUpTo` and `popUpToInclusive`?

2. How do you handle deep links with the Navigation component? What about deferred deep links that require authentication first?

3. In a multi-module project, how do you manage navigation graphs across feature modules without creating circular dependencies?

4. What are the limitations of the Navigation component? When might you build a custom navigation solution?

5. How does `SavedStateHandle` integrate with Navigation component arguments?

### WorkManager

6. Explain the internal architecture of WorkManager. How does it decide between `JobScheduler`, `AlarmManager`, and `GCMNetworkManager` under the hood?

7. How do `OneTimeWorkRequest` and `PeriodicWorkRequest` differ? What is the minimum interval for periodic work and why?

8. Explain work chaining, input mergers, and unique work policies (`KEEP`, `REPLACE`, `APPEND`, `APPEND_OR_REPLACE`).

9. How do you test WorkManager tasks? Explain `WorkManagerTestInitHelper` and `TestDriver`.

10. Your periodic background sync keeps getting delayed on certain OEMs (Xiaomi, Huawei). What is happening and how do you mitigate it?

### Paging 3

11. Explain the Paging 3 architecture: `PagingSource`, `RemoteMediator`, `PagingData`, `PagingDataAdapter`. How does the data flow?

12. What is a `RemoteMediator` and when would you use it instead of just a `PagingSource`?

13. How do you handle item-level updates (e.g., like/unlike) in a paged list without reloading the entire dataset?

14. How does Paging 3 integrate with Room for offline-first pagination?

### Hilt / Dependency Injection

15. Explain Hilt's component hierarchy. How does `SingletonComponent` differ from `ActivityRetainedComponent` and `ViewModelComponent`?

16. What is `@EntryPoint` in Hilt, and when would you need it?

17. How do you provide different implementations for the same interface in Hilt (e.g., `@Named`, `@Qualifier`)?

18. How does Hilt generate code at compile time? What role does KSP play?

19. Compare Hilt, Koin, and manual DI. What are the trade-offs for a large-scale project?

### Other Jetpack Libraries

20. Explain `Lifecycle-aware components`. What is the difference between `LifecycleObserver`, `DefaultLifecycleObserver`, and `LifecycleEventObserver`?

21. What is the `App Startup` library? How does it improve over `ContentProvider`-based initialization?

22. Explain `DataStore` — both `Preferences DataStore` and `Proto DataStore`. How do they guarantee data consistency compared to `SharedPreferences`?

23. What is `CameraX`, and how does its lifecycle-aware API simplify camera integration?

24. What is the `Macrobenchmark` library? How does it differ from `Microbenchmark`?

25. Explain `Baseline Profiles`. How do they improve app startup and runtime performance?

---

## 3. Jetpack Compose — Deep Dive

### Fundamentals and Mental Model

1. Explain how Compose's declarative paradigm differs from the imperative View system. What is the "gap" that the Compose compiler plugin bridges?

2. What is the Compose compiler plugin? What does it do with `@Composable` functions at compile time?

3. Explain the Compose phases: **Composition**, **Layout**, **Drawing**. How do they relate to the traditional View pipeline?

4. What is the `Slot Table`? How does Compose internally track the composition tree?

### Recomposition and Performance

5. What triggers recomposition? Explain `structural equality` vs `referential equality` in the context of recomposition skipping.

6. What makes a type "stable" in Compose? Explain `@Stable`, `@Immutable`, and how the Compose compiler infers stability.

7. A `LazyColumn` with 1000 items is janky. Walk through your performance investigation: tools, common causes, and fixes.

8. Explain `derivedStateOf`. When should you use it, and when is it overkill?

9. What is `Snapshot` system in Compose? How does `mutableStateOf` use it for change detection?

10. Explain `remember` vs `rememberSaveable`. What types can `rememberSaveable` handle, and how do you save custom objects?

### State Management

11. Compare state hoisting, `ViewModel` state, and `rememberSaveable` for managing state in Compose. When do you use each?

12. Explain `collectAsStateWithLifecycle()`. Why is it preferred over `collectAsState()` for Flow collection in Compose?

13. How do you share state between composables that are far apart in the tree without passing it through every parameter?

14. What is `CompositionLocal`? Give examples of when to create custom ones and when it becomes an anti-pattern.

15. Explain the difference between `State<T>`, `MutableState<T>`, and `SnapshotStateList<T>`.

### Side Effects

16. Explain every side-effect handler in Compose: `LaunchedEffect`, `rememberCoroutineScope`, `DisposableEffect`, `SideEffect`, `produceState`, `derivedStateOf`, `snapshotFlow`. When do you use each?

17. What is the key parameter in `LaunchedEffect`? What happens when the key changes?

18. How would you observe a `Flow` in a composable and cancel collection when the composable leaves the composition?

### Layout and Modifiers

19. How does the Compose layout system enforce single-pass measurement? What is `intrinsic measurement`, and when do you need it?

20. Explain `Modifier` ordering. Why does `Modifier.padding(16.dp).background(Color.Red)` look different from `Modifier.background(Color.Red).padding(16.dp)`?

21. How do you create a custom layout in Compose? Explain `Layout` composable and `MeasurePolicy`.

22. Explain `SubcomposeLayout`. When is it needed? Give a real example (e.g., `BoxWithConstraints`, `LazyColumn`).

23. What is `Modifier.Node`? How does it improve over the legacy `Modifier.composed` approach?

### Animations

24. Compare `animateAsState`, `AnimatedVisibility`, `AnimatedContent`, `Crossfade`, `updateTransition`, and `Animatable`. When do you use each?

25. How do you create a shared-element transition in Compose?

26. Explain `InfiniteTransition`. How do you create a pulsating or shimmer effect?

### Interop and Migration

27. How do you embed a Compose UI inside an existing XML layout (`ComposeView`)? What about embedding a traditional `View` inside Compose (`AndroidView`)?

28. What is your strategy for incrementally migrating a large XML-based app to Compose? Where do you start?

29. How do you handle theming when your app has both View-based and Compose-based screens?

30. Explain `Compose Multiplatform`. What are its capabilities and limitations compared to Android-only Compose?

---

## 4. Kotlin — Fundamentals to Advanced

### Basics and Type System

1. Explain the difference between `val`, `var`, and `const val`. Where can `const val` be used, and why?

2. What is the difference between `==` and `===` in Kotlin? How does this relate to `data class` equality?

3. Explain Kotlin's null safety system. What are `?.`, `!!`, `?:`, `let`, and `run` in the context of null handling? When is `!!` acceptable?

4. What is the difference between `String` and `String?` at the JVM bytecode level?

5. Explain Kotlin's type hierarchy: `Any`, `Unit`, `Nothing`. What is `Nothing` used for, and how does it affect control flow analysis?

6. What are `typealias` and when should you use them vs creating a new type?

### Classes and OOP

7. Explain `data class` — what does the compiler generate? What are the pitfalls of using `data class` with inheritance?

8. What are `sealed class` and `sealed interface`? How do they differ, and when would you use one over the other?

9. Explain `value class` (formerly `inline class`). What problem does it solve, and what are the boxing/unboxing implications?

10. What is `object` in Kotlin — `object declaration`, `companion object`, and `object expression`? How are each compiled to JVM bytecode?

11. Explain delegation in Kotlin: class delegation (`by`) and property delegation (`by lazy`, `by Delegates.observable`, custom delegates).

12. What is the `init` block execution order when a class has a primary constructor, property initializers, and `init` blocks?

### Functions and Lambdas

13. Explain `inline`, `noinline`, and `crossinline` functions. What performance benefit does `inline` provide, and when should you avoid it?

14. What are `reified` type parameters? Why do they require `inline` functions?

15. Explain higher-order functions and lambdas. What is the difference between `() -> Unit` and `Function0<Unit>` at the bytecode level?

16. What are extension functions? Can they be overridden? How are they resolved (statically or dynamically)?

17. Explain `scope functions`: `let`, `run`, `with`, `apply`, `also`. How do you choose between them?

18. What are `operator` functions? Give examples of useful operator overloads.

### Generics

19. Explain Kotlin generics variance: `in` (contravariance), `out` (covariance), and invariance. Give real-world examples.

20. What is `star projection` (`*`)? How does it differ from `Any?`?

21. What is type erasure? How does it affect generic type checks at runtime, and how do `reified` types help?

22. Explain `where` clause for multiple generic constraints.

### Advanced Features

23. What are `contracts` in Kotlin? How does `callsInPlace` work, and how does it help the compiler with smart casts and initialization?

24. Explain `context receivers` (or context parameters in newer Kotlin). What problem do they solve compared to extension functions?

25. What are `suspend` functions at the bytecode level? How does the compiler transform them using `Continuation Passing Style (CPS)`?

26. Explain Kotlin `Multiplatform` — `expect`/`actual` mechanism, source sets, and shared module architecture.

27. What is the Kotlin Symbol Processing (KSP) API? How does it improve over KAPT, and what are its limitations?

28. Explain `@JvmStatic`, `@JvmField`, `@JvmOverloads`, `@JvmName`. When are they needed?

29. What is structured concurrency from a Kotlin design perspective (not coroutines-specific)?

30. How does Kotlin's `Sequence` differ from `Iterable`? When should you prefer one over the other?

---

## 5. Kotlin Coroutines and Flows

### Coroutine Fundamentals

1. What is a coroutine? How does it differ from a thread? Can you have millions of coroutines but not millions of threads?

2. Explain `CoroutineScope`, `CoroutineContext`, and `Job`. How are they related?

3. What are `Dispatchers.Main`, `Dispatchers.IO`, `Dispatchers.Default`, and `Dispatchers.Unconfined`? When do you use each?

4. Explain structured concurrency. What happens when a child coroutine fails — does the parent get cancelled? What about siblings?

5. What is `SupervisorJob`? How does `supervisorScope` differ from `coroutineScope`?

6. Explain the difference between `launch` and `async`. When should you use `async`, and what is the pitfall of calling `async` without `await`?

### Cancellation and Exception Handling

7. How does coroutine cancellation work internally? What is cooperative cancellation, and why must long-running computations check `isActive` or use `ensureActive()`?

8. Explain `CancellationException`. Why is it treated differently from other exceptions in coroutines?

9. What is `NonCancellable`? When would you use `withContext(NonCancellable) { ... }`?

10. How does `CoroutineExceptionHandler` work? Where in the coroutine hierarchy should it be installed?

11. Explain exception propagation differences between `coroutineScope`, `supervisorScope`, `launch`, and `async`.

### Flows

12. What is a `Flow`? How does it differ from `LiveData`, `RxJava Observable`, and `Sequence`?

13. Explain the difference between `cold` flows and `hot` flows. Give examples of each.

14. What is `StateFlow`? How does it differ from `LiveData`? What are its limitations (e.g., conflation, initial value requirement)?

15. What is `SharedFlow`? Explain `replay`, `extraBufferCapacity`, and `onBufferOverflow`. When would you use `SharedFlow` over `StateFlow`?

16. Explain `callbackFlow`. How do you bridge a callback-based API to a Flow?

17. What is `channelFlow` and how does it differ from `flow { }` and `callbackFlow`?

18. Explain Flow operators: `map`, `filter`, `flatMapLatest`, `flatMapConcat`, `flatMapMerge`, `combine`, `zip`, `debounce`, `distinctUntilChanged`, `conflate`. Give a practical scenario for each.

19. How does `flowOn` change the upstream dispatcher? What is the difference between `flowOn` and collecting on a different dispatcher?

20. What is `stateIn` and `shareIn`? Explain `SharingStarted.WhileSubscribed(5000)` and why the 5-second timeout is commonly used.

### Channels

21. What are `Channels`? How do they differ from Flows?

22. Explain channel types: `RENDEZVOUS`, `BUFFERED`, `CONFLATED`, `UNLIMITED`. When do you use each?

23. What is the `produce` builder? How does it relate to `ReceiveChannel`?

### Advanced Coroutines

24. Explain `Mutex` and `Semaphore` in coroutines. How do they differ from Java's `synchronized` and `ReentrantLock`?

25. What is `ThreadLocal` in the context of coroutines? How does `asContextElement()` work?

26. How do you unit-test coroutines? Explain `runTest`, `TestDispatcher`, `advanceUntilIdle`, and `StandardTestDispatcher` vs `UnconfinedTestDispatcher`.

27. Explain the `select` expression for coroutines. Give a real-world scenario.

28. How would you implement a retry mechanism with exponential backoff using coroutines and Flow?

---

## 6. Database and Storage

### Room

1. Explain Room's architecture. What are `@Entity`, `@Dao`, `@Database`, and `@TypeConverter`?

2. How does Room verify queries at compile time? What is the role of annotation processing?

3. Explain Room database migrations. What happens if you don't provide a migration? What is `fallbackToDestructiveMigration` and why should you be cautious with it?

4. How do you write an `auto-migration` in Room? What are its limitations compared to manual migrations?

5. How does Room integrate with Kotlin Flows and coroutines? What happens internally when a DAO method returns `Flow<List<Entity>>`?

6. What are `@Embedded`, `@Relation`, and `@Junction` in Room? Explain how to model one-to-many and many-to-many relationships.

7. How do you handle full-text search (FTS) in Room?

8. What is the `@RawQuery` annotation? When would you use it, and what are the security implications?

9. How do you test Room databases? Explain in-memory database testing strategy.

10. What is Room's `MultiInstanceInvalidationService` and when would you need it?

### SQLite Internals

11. Explain Write-Ahead Logging (WAL) in SQLite. How does Room enable it, and what are the performance implications?

12. What is the default threading model for SQLite on Android? How does Room handle concurrent reads and writes?

13. How do database transactions work in Room? What is `@Transaction` and when must you use it?

### DataStore and SharedPreferences

14. Why was `DataStore` created as a replacement for `SharedPreferences`? What problems does `SharedPreferences` have?

15. Explain `Preferences DataStore` vs `Proto DataStore`. When would you choose Proto DataStore?

16. How does `DataStore` guarantee atomic read-modify-write operations?

17. Can `SharedPreferences` cause ANRs? Explain how and why.

### Scoped Storage and Files

18. Explain scoped storage (Android 10+). How does it change file access patterns?

19. What is the difference between `getFilesDir()`, `getCacheDir()`, `getExternalFilesDir()`, and `MediaStore` APIs?

20. How do you handle large file downloads that should survive process death?

---

## 7. Android Build System, Gradle, and Obfuscation

### Gradle Fundamentals

1. Explain the three phases of a Gradle build: **initialization**, **configuration**, and **execution**. What happens in each?

2. What is the difference between `buildscript` dependencies and module-level dependencies?

3. Explain `implementation`, `api`, `compileOnly`, `runtimeOnly`, and `testImplementation` dependency configurations. What are the implications of using `api` vs `implementation` in a multi-module project?

4. What are `Gradle version catalogs`? How do they improve dependency management over `ext` blocks or `buildSrc`?

5. What is a `convention plugin`? How do you use them to share build logic across modules?

### Build Variants and Flavors

6. Explain `build types`, `product flavors`, and `build variants`. How do they combine?

7. How do you configure different API endpoints, app icons, or feature flags per build variant?

8. What is `flavor dimension`? Give an example of multi-dimensional flavoring (e.g., `environment` × `audience`).

9. How do you conditionally include dependencies based on build flavor?

### Android Gradle Plugin (AGP)

10. What major changes came in AGP 7.x and 8.x? How does the namespace migration work?

11. Explain the `android` block in `build.gradle.kts`. What are `compileSdk`, `minSdk`, `targetSdk`, and what are the implications of each?

12. What is `desugaring`? How does `coreLibraryDesugaring` work to enable Java 8+ APIs on older Android versions?

### R8, ProGuard, and Obfuscation

13. What is R8? How does it differ from ProGuard?

14. Explain the three things R8 does: **shrinking**, **optimization**, and **obfuscation**. Can you enable them independently?

15. What are ProGuard/R8 rules? Explain `-keep`, `-keepclassmembers`, `-dontwarn`, `-keepattributes`. When would you use each?

16. Your app crashes in the release build but works in debug. How do you diagnose R8-related issues?

17. How do you read an obfuscated stack trace? What is the `mapping.txt` file, and how do you upload it to Crashlytics or Play Console?

18. What is `minification` vs `obfuscation`? Why might you want obfuscation disabled but minification enabled?

19. How do you protect sensitive string literals from decompilation even with obfuscation?

### Build Performance

20. What techniques do you use to speed up Gradle builds? Explain `configuration cache`, `build cache`, `parallel execution`, `configuration avoidance`, and `incremental compilation`.

21. What is the `Gradle daemon`? How does it persist between builds?

22. How do you profile a slow Gradle build? Explain `--scan` and `--profile`.

23. What is `modularization`'s effect on build time? How does it enable parallel compilation?

24. Explain Kotlin compile avoidance and incremental compilation in the context of multi-module projects.

25. What is the impact of annotation processors (KAPT) vs KSP on build performance?

---

## 8. Security

### Network Security

1. Explain SSL/TLS pinning. What is the difference between pinning the **certificate** vs the **public key**? Which is recommended and why?

2. How do you implement certificate pinning using OkHttp's `CertificatePinner`? What happens when the pinned certificate rotates?

3. What is the `network_security_config.xml`? How do you use it to configure trusted CAs, certificate pinning, and cleartext traffic policies?

4. How do you prevent Man-in-the-Middle (MITM) attacks in your app?

5. What tools can an attacker use to bypass SSL pinning (e.g., Frida, Magisk, Xposed)? How do you detect and counter these?

6. Explain certificate transparency and how it adds an additional layer of trust.

### Data Storage Security

7. What is `EncryptedSharedPreferences`? How does it work under the hood with Android Keystore?

8. Explain the `Android Keystore System`. What types of keys can it store, and what hardware-backed guarantees does it provide?

9. How do you encrypt a Room database? Explain the `SQLCipher` integration. What is the performance impact?

10. What is the difference between hardware-backed Keystore and software-backed Keystore?

11. How do you securely store API keys in an Android app? Discuss the trade-offs of `BuildConfig`, NDK, and remote config approaches.

12. Explain the `BiometricPrompt` API. How does it integrate with Android Keystore for cryptographic authentication?

### Code and App Security

13. What is code obfuscation, and why is it not sufficient as the sole security measure?

14. How do you detect whether your app is running on a rooted device? What are the limitations of root detection?

15. Explain `SafetyNet Attestation` (now `Play Integrity API`). How does it work, and what does it verify?

16. What is runtime application self-protection (RASP)? Give examples relevant to Android.

17. How do you prevent your app from being repackaged or tampered with? Explain signature verification techniques.

18. What is the `StrictMode` policy, and how can it be used during development to catch security issues?

### OWASP Mobile Top 10

19. Walk through the OWASP Mobile Top 10 risks. For each, describe the risk and the Android-specific mitigation.

20. How do you handle sensitive data in the app's recent tasks screenshot? (`FLAG_SECURE`)

21. What are `WebView` security concerns? How do you prevent JavaScript injection and insecure content loading?

22. Explain `Intent` redirection vulnerabilities. How does an exported Activity or BroadcastReceiver become an attack vector?

### Authentication and Authorization

23. How do you implement secure token storage and refresh logic?

24. What is `OAuth 2.0` with `PKCE` for mobile apps? Why is PKCE important for public clients?

25. How do you implement session management securely — timeout, re-authentication, biometric gate?

---

## 9. Android DevOps and CI/CD

### CI/CD Pipelines

1. Describe your ideal CI/CD pipeline for an Android project — stages, gates, and artifacts.

2. Compare CI/CD platforms for Android: **GitHub Actions**, **Bitrise**, **CircleCI**, **Jenkins**, **GitLab CI**. What are the trade-offs?

3. How do you cache Gradle dependencies and build outputs in CI to speed up builds?

4. How do you handle signing keys and secrets in CI? Explain approaches for managing `keystore` files and passwords.

5. What checks do you enforce in a PR pipeline before merging? (lint, tests, code coverage, detekt, dependency analysis)

### Automated Testing in CI

6. How do you run instrumented tests in CI without physical devices? Explain `Firebase Test Lab`, `emulator.wtf`, and `Gradle Managed Devices`.

7. What is `Gradle Managed Devices`? How does it simplify emulator management in CI?

8. How do you handle flaky tests in CI? What strategies prevent them from blocking deployments?

9. Explain screenshot testing in CI. How do tools like `Paparazzi` or `Roborazzi` work?

### Release and Deployment

10. How do you automate Play Store deployments? Explain `Fastlane`, `Gradle Play Publisher plugin`, and the Play Developer API.

11. What is a staged rollout? How do you monitor it and decide when to go from 10% to 100%?

12. How do you implement `feature flags` in Android? Compare tools like `Firebase Remote Config`, `LaunchDarkly`, and custom solutions.

13. Explain `App Bundle` vs `APK`. What are `dynamic feature modules`, and how do they affect the delivery pipeline?

14. How do you automate version code and version name management in CI?

### Monitoring and Observability

15. How do you integrate crash reporting (Crashlytics, Sentry, Bugsnag) into your CI/CD workflow?

16. What key metrics do you track post-release? (ANR rate, crash rate, startup time, vitals)

17. How do you set up automated alerting when crash rate exceeds a threshold?

18. Explain Google Play's `Android Vitals` dashboard. What metrics does it track, and how do they affect app visibility on the store?

### Infrastructure and Tooling

19. How do you manage multiple environments (dev, staging, production) in your CI pipeline?

20. What is your strategy for managing Gradle wrapper versions and AGP updates across a team of 20+ developers?

21. How do you enforce code quality with tools like `detekt`, `ktlint`, `lint`, and `dependency-analysis-plugin`?

22. What is `Renovate` or `Dependabot`? How do you automate dependency updates safely?

---

## 10. Architecture and Design Patterns

### Clean Architecture

1. Explain Clean Architecture layers in Android: **presentation**, **domain**, and **data**. What should and should not live in each?

2. What is a `Use Case` (interactor)? Do you always need one, or can a ViewModel talk directly to a Repository?

3. How does the dependency rule work in Clean Architecture? Why does the domain layer have no Android framework dependencies?

4. Debate: Is Clean Architecture over-engineering for a small app? Where is the threshold?

5. How do you handle mappers between data, domain, and presentation models? Is it worth the boilerplate?

### MVVM

6. Explain `MVVM` in Android. How does `ViewModel` survive configuration changes, and what holds the `ViewModel` instance?

7. What is the `ViewModelStore` and `ViewModelStoreOwner`? How do Activity, Fragment, and NavBackStackEntry each own ViewModels?

8. Should a `ViewModel` ever reference a `Context`? What about `AndroidViewModel`?

9. How do you handle one-time events (toasts, navigation, snackbars) in MVVM without using `SingleLiveEvent` hacks?

10. What is the difference between exposing `LiveData` vs `StateFlow` from a ViewModel?

### MVI (Model-View-Intent)

11. Explain the `MVI` pattern. What are `Intent` (user action), `State`, and `SideEffect`?

12. How does MVI enforce unidirectional data flow (UDF)? Why is this beneficial for debugging and testing?

13. How do you handle side effects (navigation, dialogs, toasts) in MVI without losing them?

14. Compare MVI libraries/approaches: Orbit MVI, MVIKotlin, custom reducers. What are the trade-offs?

15. In a large app, MVI state objects become bloated. How do you handle this?

### Modularization

16. What are the different module types in a multi-module Android project? (app, feature, core, data, domain, common, design-system)

17. How do you define module boundaries? What rules prevent a feature module from depending on another feature module?

18. How do you handle navigation across modules when modules cannot depend on each other?

19. What is the build-time benefit of modularization? How does it enable parallel compilation?

20. How do you share common UI components (design system) across feature modules?

### Dependency Injection Patterns

21. Compare `Service Locator` vs `Dependency Injection`. Why is DI preferred?

22. How does constructor injection differ from field injection? Which does Hilt encourage, and why?

23. Explain the `composition root` concept. Where is it in an Android app?

### Reactive and Repository Patterns

24. Explain the `Repository` pattern. Should a Repository return `Flow`, `LiveData`, or suspend functions?

25. What is the `Single Source of Truth (SSOT)` pattern? How does it work with Room + Retrofit?

26. Explain the `offline-first` architectural pattern. How do you implement optimistic UI updates?

27. What are `Data Sources`? How do you separate local and remote data sources behind a Repository?

28. How does the `Mediator` pattern (e.g., `NetworkBoundResource`) decide between cache and network?

---

## 11. Android System Design

### Chat Application

1. Design a chat application like WhatsApp for Android. Cover: real-time messaging (WebSocket vs Firebase Realtime DB), message persistence, offline queue, read receipts, media uploads, and pagination of chat history.

2. How do you handle message ordering and delivery guarantees in a chat app?

3. How do you design the local database schema for conversations, messages, and participants?

### Offline-First Application

4. Design an offline-first note-taking app. How do you sync data when connectivity is restored? How do you handle conflicts?

5. Explain `CRDTs` (Conflict-free Replicated Data Types). How could they help in offline-first sync?

6. How do you design a work queue for pending operations that must be synced to the server?

### Image Loading Library

7. Design an image loading library like Coil or Glide from scratch. Cover: memory cache (LRU), disk cache, network fetching, image decoding, downsampling, and lifecycle awareness.

8. How would you handle animated GIFs and WebP images?

9. How do you prevent out-of-memory errors when loading large images?

### Feed / Timeline

10. Design a social media feed (like Instagram). Cover: pagination (cursor-based vs offset-based), caching strategy, optimistic likes, infinite scroll, and pull-to-refresh.

11. How do you handle real-time feed updates (new posts appearing) without disrupting the scroll position?

12. How do you pre-fetch data to make scrolling feel instant?

### Analytics SDK

13. Design an analytics SDK that other apps integrate. Cover: event batching, persistence, retry logic, minimal footprint, and thread safety.

14. How do you ensure the SDK does not impact the host app's performance or battery life?

### Notification System

15. Design a notification system for an e-commerce app. Cover: FCM integration, notification channels, deep linking, scheduled local notifications, and notification grouping.

16. How do you handle notification delivery reliability across different OEMs?

### Payment Flow

17. Design a payment flow for an Android app. Cover: PCI compliance, tokenization, Google Pay integration, retry logic, and idempotency.

18. How do you handle payment state across process death and configuration changes?

### Large-Scale App Architecture

19. You are designing the architecture for a super-app (banking + shopping + social). How do you modularize? Dynamic feature modules? Plugin architecture?

20. How do you handle shared authentication, theming, and navigation across 50+ feature modules owned by different teams?

21. Design the app's logging and observability layer. How do you correlate events across modules?

22. How do you handle A/B testing infrastructure at scale?

---

## 12. Testing

### Unit Testing

1. What is the testing pyramid? How does it apply to Android projects — what ratio of unit/integration/UI tests do you target?

2. How do you unit-test a ViewModel? What do you mock, and what do you use for coroutines (`TestDispatcher`)?

3. Explain `MockK` vs `Mockito`. Which do you prefer for Kotlin, and why?

4. How do you unit-test a Repository that combines local (Room) and remote (Retrofit) data sources?

5. What is `Turbine`? How does it simplify testing Kotlin Flows?

6. How do you test code that depends on `System.currentTimeMillis()` or `Clock`?

### Integration Testing

7. What is the difference between unit tests and integration tests in Android context?

8. How do you use `Robolectric` for integration testing? What are its limitations?

9. How do you set up test doubles for DI in integration tests with Hilt? Explain `@HiltAndroidTest` and `@UninstallModules`.

### UI Testing

10. Compare `Espresso` and `Compose Testing`. How does `ComposeTestRule` work?

11. How do you test navigation flows end-to-end?

12. What is `Robot Pattern` in UI testing? How does it improve test readability?

13. How do you handle async operations (network calls, animations) in UI tests?

### Screenshot Testing

14. What is screenshot testing? How do tools like `Paparazzi` generate reference screenshots without a device?

15. How do you handle screenshot tests across different themes (light/dark), locales, and font sizes?

16. How do you review and approve screenshot changes in PR workflows?

### Testing Strategy and Culture

17. As a lead, how do you define a testing strategy for a team? What minimum coverage do you enforce?

18. How do you handle legacy code with zero tests? Where do you start?

19. What is `Test-Driven Development (TDD)`? Do you practice it in Android development? When is it most valuable?

20. How do you measure and track code coverage? Is 100% coverage a good goal?

---

## 13. Performance and Optimization

### Memory

1. What causes memory leaks in Android? Give five real-world examples.

2. How do you detect memory leaks? Explain `LeakCanary`, `Android Studio Profiler`, and `dumpsys meminfo`.

3. What is the difference between a memory leak and high memory usage? How do you address each?

4. Explain the Android garbage collector. How does it differ from JVM GC, and why do GC pauses cause jank?

5. How does `WeakReference` help prevent memory leaks? When is it appropriate?

### ANR and Responsiveness

6. What causes an ANR? What are the time thresholds for input events (5s) and BroadcastReceiver (10s)?

7. How do you diagnose ANRs using the `traces.txt` file and Play Console ANR reports?

8. What is `StrictMode`? How do you use it during development to catch disk/network operations on the main thread?

9. How do you identify main-thread blocking in production? Explain frame drop metrics and `FrameTimeline`.

### Startup Optimization

10. What are `cold start`, `warm start`, and `hot start`? What metrics do you track?

11. How do you reduce cold start time? Explain `App Startup` library, lazy initialization, and `Baseline Profiles`.

12. What is a `splash screen` in Android 12+? How does the `SplashScreen` API work?

13. How do you measure startup time accurately in production? (`reportFullyDrawn()`, Macrobenchmark)

### Rendering Performance

14. What is `jank`? How do you identify janky frames using `Systrace`, `Perfetto`, or `Android Studio Profiler`?

15. How do you optimize a `RecyclerView` that stutters during fast scrolling? (setHasStableIds, prefetch, view pool)

16. How do you optimize Compose performance? Explain `strong skipping mode`, `Stability`, and the Compose compiler reports.

17. What are `Baseline Profiles`? How do they improve runtime performance through AOT compilation?

### Network Optimization

18. How do you optimize network performance? Explain HTTP caching (`ETag`, `Cache-Control`), connection pooling, and request batching.

19. What is `OkHttp Interceptor`? How do you use it for logging, retry, and caching?

20. How do you reduce APK size? Explain resource shrinking, code shrinking, WebP conversion, and `splits` for ABI/density.

### Battery Optimization

21. How does `Doze mode` and `App Standby` affect your app? How do you work within these constraints?

22. What is the `Battery Historian` tool? How do you use it to identify battery drain issues?

23. How does `WorkManager` respect battery optimization while still performing background work?

---

## 14. Miscellaneous and Leadership

### Technical Leadership

1. You are inheriting a 6-year-old Android codebase with Java, RxJava, MVP, and no tests. What is your modernization roadmap? How do you prioritize?

2. How do you conduct effective code reviews? What do you look for beyond syntax — architecture, performance, security, testability?

3. A team member proposes using a new architecture (e.g., switching from MVVM to MVI mid-project). How do you evaluate and decide?

4. How do you balance feature delivery with tech debt reduction?

5. How do you mentor junior developers on Android best practices? What learning path do you recommend?

### API Design and SDK Authoring

6. You are building an internal SDK used by 10 teams. What are your API design principles? (backward compatibility, semantic versioning, documentation)

7. How do you version an SDK to avoid breaking changes? Explain semantic versioning and deprecation strategies.

8. How do you design a library module's public API surface? What Kotlin visibility modifiers and ProGuard rules help?

### Migration Strategies

9. How do you migrate from Java to Kotlin incrementally in a large codebase? What are the common interop pitfalls?

10. How do you migrate from XML views to Jetpack Compose incrementally? What do you migrate first?

11. How do you migrate from RxJava to Kotlin Coroutines/Flow? What is the mapping between Rx operators and Flow operators?

12. How do you migrate from `kapt` to `KSP`? What libraries still require KAPT?

### Emerging Topics

13. What is `Kotlin Multiplatform (KMP)`? How does it differ from Flutter and React Native? When would you choose KMP?

14. What is `Compose Multiplatform`? Can you share UI code across Android, iOS, Desktop, and Web?

15. What is `Gemini Nano` (on-device AI)? How does the Android `AICore` service work?

16. Explain Android's `Health Connect` API. When would you integrate with it?

17. What are `Adaptive Layouts` in Android? How do you design for foldables, tablets, and ChromeOS with Compose `WindowSizeClass`?

18. What is `Predictive Back Gesture` in Android 14+? How do you implement it in your app?

### Process and Collaboration

19. How do you structure an Android team for a large project — feature teams vs platform teams?

20. What documentation do you maintain for a shared Android codebase? (ADRs, architecture docs, onboarding guide)

21. How do you handle disagreements on technical decisions within your team?

22. Describe your release process — branching strategy, release trains, hotfix procedures.

23. How do you define "done" for a feature? What quality gates must pass before shipping?

---

*This document covers 300+ questions across 14 sections, targeting a senior/lead Android developer with 12+ years of experience. Questions range from fundamental concepts to scenario-based system design, suitable for both the interviewer and the candidate preparing for a lead-level interview.*
