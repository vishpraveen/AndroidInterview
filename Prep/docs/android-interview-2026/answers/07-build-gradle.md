# Answers — §7 Android Build System, Gradle, and Obfuscation

Questions: [questions.md](../questions.md#7-android-build-system-gradle-and-obfuscation).

---

### Gradle Fundamentals

**1.** Gradle phases: initialization, configuration, execution.

- [x]

- **Thesis:** **Init**: discover settings/buildSrc included builds. **Configuration**: run build scripts, wire task graph, **may run** lazy configuration incorrectly if eager APIs. **Execution**: run **selected** tasks actions.
- **Mechanism / order:** Configuration-time work should be **minimal**—use **lazy properties**.
- **Edge cases:** **Configuration cache** replays serialized graph—side effects forbidden.
- **Production hook:** `--scan` to find config hotspots.
- **When wrong:** `allprojects { repositories }` heavy resolution at config—slow.

**2.** `buildscript` vs module dependencies.

- [x]

- **Thesis:** Legacy **`buildscript` classpath** pulled plugin jars for **root** script; modern **plugins DSL** + **version catalogs** declare plugins per **plugins {}** block; module **`dependencies`** are **project** compile/runtime classpaths separate concern.
- **Mechanism / order:** Prefer **`plugins { id(...) version ... }`** in `plugins` block not `buildscript`.
- **Edge cases:** **Composite builds** plugin substitution.
- **Production hook:** Remove `buildscript` when migrating to plugins DSL fully.
- **When wrong:** Duplicating dependency versions in `buildscript` and `dependencies`.

**3.** `implementation` vs `api` vs `compileOnly` vs `runtimeOnly` vs `testImplementation`.

- [x]

- **Thesis:** **`api`**: leaks dependency **transitively** to consumers’ compile classpath—**exposes** types in your public API. **`implementation`**: **hidden** from consumers’ compile—faster compile avoidance. **`compileOnly`**: provided at compile (annotations). **`runtimeOnly`**: not compile-visible. **`testImplementation`**: tests only.
- **Mechanism / order:** Multi-module: prefer **`implementation`** to reduce **recompilation** fan-out.
- **Edge cases:** **`compileOnly` for compileOnly** annotation processors patterns.
- **Production hook:** Dependency Analysis Plugin to suggest `api` vs `impl`.
- **When wrong:** `api` everything—compile time explosion.

**4.** Gradle version catalogs vs `ext` / `buildSrc`.

- [x]

- **Thesis:** **`libs.versions.toml`** centralizes versions + bundles with **IDE assist**, **cacheable** resolution vs **`buildSrc`** forcing **whole project** rebuild on any Kotlin change in `buildSrc`.
- **Mechanism / order:** `libs.androidx.core.ktx` accessors generated.
- **Edge cases:** **Catalog** + **convention plugins** compose well.
- **Production hook:** Bundles for Compose BOM import.
- **When wrong:** Giant `buildSrc` as dumping ground—invalidates cache constantly.

**5.** Convention plugins.

- [x]

- **Thesis:** **`build-logic`** included build with **precompiled script plugins** or Kotlin `Plugin<Project>` applying shared Android/Kotlin config to many modules.
- **Mechanism / order:** `plugins { id("com.example.android.library") }` local plugin id.
- **Edge cases:** **Composite** included build version alignment.
- **Production hook:** Single place for `compileSdk`, lint, JaCoCo flags.
- **When wrong:** Copy-paste `android {}` across 40 modules.

---

### Build Variants and Flavors

**6.** Build types, flavors, variants.

- [x]

- **Thesis:** **Build types** (`debug/release`) configure minify, signing, debuggable. **Product flavors** orthogonal dimensions (`free/prod`). **Variant** = **flavorDimensions** Cartesian product × build type (`freeDebug`).
- **Mechanism / order:** `matchingFallbacks` for missing combos.
- **Edge cases:** **Source sets** `src/free/java` overlay.
- **Production hook:** `flavorDimensions += listOf("env", "store")`.
- **When wrong:** O(N²) duplicate `buildTypes` per flavor—use `beforeVariants`.

**7.** Different endpoints/icons/feature flags per variant.

- [x]

- **Thesis:** **`buildConfigField`**, **`resValue`**, **`manifestPlaceholders`**, **`productFlavors { buildConfigField("String","API_URL", "\"...\"") }`**; resources per flavor folder; **Remote Config** still runtime.
- **Mechanism / order:** `debug` non-minified for faster iteration.
- **Edge cases:** **R8** removes unused BuildConfig fields—keep referenced.
- **Production hook:** `network_security_config` per flavor srcSet.
- **When wrong:** Secrets committed per flavor—use CI injection.

**8.** Flavor dimension example.

- [x]

- **Thesis:** `dimension "environment"` (`dev/stage/prod`) × `dimension "audience"` (`internal/play`) ⇒ variants like `internalDevDebug`.
- **Mechanism / order:** `flavorDimensions` order defines **priority** in conflict resolution.
- **Edge cases:** **Feature module** matching missing flavor with fallbacks.
- **Production hook:** Map to **different applicationIdSuffix** for side-by-side installs.
- **When wrong:** Too many dimensions—combinatorial CI explosion.

**9.** Conditional dependencies per flavor.

- [x]

- **Thesis:** `freeImplementation` / `prodReleaseImplementation` configurations; or **`dependencies.add` in `afterEvaluate` with variant API**—prefer **declarative** config names.
- **Mechanism / order:** **`missingDimensionStrategy`** in consumer modules.
- **Edge cases:** **Duplicate class** if both add same transitive.
- **Production hook:** Keep **Play feature delivery** modules flavor aligned.
- **When wrong:** Imperative hacks in `gradle.projectsLoaded` brittle.

---

### Android Gradle Plugin (AGP)

**10.** AGP 7.x / 8.x major changes; namespace migration.

- [x]

- **Thesis:** **AGP 8** requires **namespace** in `build.gradle` not manifest package for R class; **JDK 17** toolchain; **non-transitive R** default; build speed improvements; **configuration cache** compatibility push; **API** removals of legacy transforms.
- **Mechanism / order:** `android { namespace "com.example" }` separate from `applicationId`.
- **Edge cases:** **K2** compiler + AGP matrix watch release notes.
- **Production hook:** `lint { checkDependencies true }` updates.
- **When wrong:** Forgetting namespace—R package mismatch.

**11.** `compileSdk` / `minSdk` / `targetSdk`.

- [x]

- **Thesis:** **`compileSdk`**: APIs **available at compile**; not shipped. **`minSdk`**: lowest installable device. **`targetSdk`**: **behavior** toggles for compatibility framework—raising triggers new runtime enforcement (storage, notifications, BLUETOOTH_CONNECT, etc.).
- **Mechanism / order:** Bump **target** only after **auditing** behavior changes each release year.
- **Edge cases:** **`uses-sdk` in library manifests** merged upward.
- **Production hook:** Play **target API** requirements deadlines.
- **When wrong:** `compileSdk` low with new APIs used—compile errors; `targetSdk` stuck low—policy rejection.

**12.** Desugaring; `coreLibraryDesugaring`.

- [x]

- **Thesis:** **D8 desugar** rewrites newer **Java library APIs** (e.g. `java.time`) to **backport** shims for **`minSdk` < API providing natively**.
- **Mechanism / order:** Add **`coreLibraryDesugaring`** dependency + `compileOptions.isCoreLibraryDesugaringEnabled = true`.
- **Edge cases:** Not all APIs desugarable; **API** surface limits documented.
- **Production hook:** Use **NIO path** shims carefully.
- **When wrong:** Expecting **all** JDK 21 APIs on API 24.

---

### R8, ProGuard, and Obfuscation

**13.** R8 vs ProGuard.

- [x]

- **Thesis:** **R8** is Google’s **Dex** shrinker/optimizer/obfuscator replacing **ProGuard** toolchain for Android—**default** in AGP; still reads **ProGuard rule** syntax.
- **Mechanism / order:** Runs shrinking + optimization + obfuscation + D8 dexing pipeline integrated.
- **Edge cases:** **Full mode** R8 more aggressive than “compat”.
- **Production hook:** `-printusage` for what was removed.
- **When wrong:** Thinking ProGuard still shrinks separately on modern AGP.

**14.** R8: shrinking, optimization, obfuscation toggles.

- [x]

- **Thesis:** **Shrinking** tree shakes unused code; **optimization** inlines/removes dead branches at dex level; **obfuscation** renames identifiers; can configure **`minifyEnabled`** bundles shrink+opt+obf typical; **separate toggles** limited—`isMinifyEnabled` main switch; finer via rules and `android.enableR8.fullMode`.
- **Mechanism / order:** `consumerProguardFiles` in libraries ship rules to apps.
- **Edge cases:** **Reflection** needs `-keep`.
- **Production hook:** `mapping.txt` per release artifact.
- **When wrong:** Disabling shrink but enabling obfuscation inconsistently—usually coupled.

**15.** Rules: `-keep`, `-keepclassmembers`, `-dontwarn`, `-keepattributes`.

- [x]

- **Thesis:** **`-keep`**: preserve classes/members matching pattern from removal/rename. **`-keepclassmembers`**: keep members but allow class shrink if unused? Actually keep members if class kept—nuanced. **`-dontwarn`**: suppress missing class warnings (use sparingly). **`-keepattributes`**: retain `Signature`, `InnerClasses`, `EnclosingMethod` for reflection/GSON/Moshi.
- **Mechanism / order:** Prefer **narrow** `-if` rules.
- **Edge cases:** **R8 full mode** may remove seemingly used code via whole-program analysis.
- **Production hook:** Generated rules from libraries (`retrofit2` consumer rules).
- **When wrong:** `-keep class ** { *; }` nukes benefits.

**16.** Release-only crash diagnosis R8.

- [x]

- **Thesis:** Suspect **reflection**, **serialization**, **JNI**, **ServiceLoader** missing classes; compare **mapping**; build **`minifiedDebug`** variant; add targeted **`-keep`**; use **`retrace`** stack.
- **Mechanism / order:** Temporarily `-dontoptimize` / `-dontshrink` bisect.
- **Edge cases:** **DataBinding/ViewBinding** generated keep needs.
- **Production hook:** Play **pre-launch report** catches missing classes.
- **When wrong:** Shipping `-dontwarn` for real missing dependencies.

**17.** Read obfuscated trace; `mapping.txt`; upload Crashlytics/Play.

- [x]

- **Thesis:** **`retrace mapping.txt obfuscated.txt`** or Play Console **Deobfuscation files** upload per version code; Crashlytics Gradle plugin uploads mapping on release.
- **Mechanism / order:** **UUID** per build must match uploaded mapping.
- **Edge cases:** **Proguard seeds** + multiple modules mapping merge.
- **Production hook:** CI artifact **immutable** mapping tied to VC.
- **When wrong:** Losing mapping for shipped build—unreadable crashes.

**18.** Minification vs obfuscation.

- [x]

- **Thesis:** **Minification** removes unused code (size + attack surface). **Obfuscation** renames symbols (harder reverse engineering, smaller dex). You might want **shrink without aggressive renaming** rarely—usually both on release.
- **Mechanism / order:** `-dontobfuscate` exists for debugging size-only experiments.
- **Edge cases:** Crash readability trade-off vs security.
- **Production hook:** Keep **line numbers** with `-keepattributes SourceFile,LineNumberTable` + `-renamesourcefileattribute SourceFile` pattern for stack traces.
- **When wrong:** Thinking obfuscation hides **secrets**—strings still visible.

**19.** Protect sensitive string literals from decompilation.

- [x]

- **Thesis:** **No perfect local secret**—attackers decompile. Mitigate: **remote attestation**, **server-held secrets**, **NDK** string splitting + **runtime assembly** only raises cost; **never** ship private API keys for your backend alone in app.
- **Mechanism / order:** Play Integrity + short-lived tokens from server.
- **Edge cases:** **JNI** `NewStringUTF` still recoverable.
- **Production hook:** OAuth **PKCE** public clients pattern.
- **When wrong:** XOR “encryption” of strings in Kotlin.

---

### Build Performance

**20.** Speed Gradle: configuration cache, build cache, parallel, configuration avoidance, incremental Kotlin.

- [x]

- **Thesis:** **Configuration cache** serializes configured model—skips config on hits. **Remote build cache** shares action outputs in CI. **`org.gradle.parallel=true`**. **Lazy task configuration** (`tasks.register`). **Kotlin incremental** + **compile avoidance** across modules.
- **Mechanism / order:** **KSP** over KAPT reduces processor time.
- **Edge cases:** **Antivirus** scanning `.gradle` caches—exclude paths.
- **Production hook:** Gradle Enterprise scans.
- **When wrong:** `subprojects { afterEvaluate { android { } } }` breaks configuration cache.

**21.** Gradle daemon.

- [x]

- **Thesis:** Long-lived JVM holding **warm JIT**, **cached classloaders**, **in-memory** dependency metadata—speeds subsequent builds.
- **Mechanism / order:** `org.gradle.daemon=true` default; **`./gradlew --stop`** clears stuck state.
- **Edge cases:** **Memory** leaks in custom plugins—restart daemon occasionally in CI with TTL.
- **Production hook:** CI **persistent** workers with warm daemon vs ephemeral trade-off.
- **When wrong:** Disabling daemon in CI for “cleanliness” paying 30s JVM tax each build.

**22.** Profile slow build: `--scan`, `--profile`.

- [x]

- **Thesis:** **`--scan`** uploads build scan (Gradle Enterprise) showing task timeline, configuration time, dependency resolution. **`--profile`** local HTML timeline.
- **Mechanism / order:** **`--dry-run`** inspect graph without execution sometimes.
- **Edge cases:** **Kotlin compiler** task dominates—module split helps.
- **Production hook:** **`buildSrc` avoidance**; modularization.
- **When wrong:** Optimizing execution when **configuration** is 80% of time.

**23.** Modularization effect on build time.

- [x]

- **Thesis:** Smaller modules **parallelize** compilation, improve **incremental** invalidation scope, enable **ABI** stable boundaries—but **graph complexity** can hurt configuration if over-fragmented.
- **Mechanism / order:** **api vs impl** boundaries reduce fan-out.
- **Edge cases:** **Circular deps** force coupling—break with interfaces module.
- **Production hook:** Measure **Kotlin compile** time per module in scan.
- **When wrong:** 200 tiny one-class modules—overhead dominates.

**24.** Kotlin compile avoidance / incremental multi-module.

- [x]

- **Thesis:** If **public ABI** of upstream module unchanged, downstream **Kotlin compile** may skip; incremental tracks **fine-grained** file deps.
- **Mechanism / order:** Avoid **constant** changing `const val` in `api` surface causing cascade.
- **Edge cases:** **KAPT** stubs break avoidance—KSP better.
- **Production hook:** **`export` API** explicitly with `internal` by default.
- **When wrong:** Marking everything `public` in `api` modules.

**25.** KAPT vs KSP build performance.

- [x]

- **Thesis:** **KAPT** generates Java stubs + runs Java AP—**slower**, more tasks. **KSP** Kotlin-first—**faster**, better incremental, fewer IO passes.
- **Mechanism / order:** Migrate Room/Moshi/Hilt where supported.
- **Edge cases:** Some processors still **KAPT-only**.
- **Production hook:** Track **`kapt`** task time in build scan baseline.
- **When wrong:** Running both on same module unnecessarily.
