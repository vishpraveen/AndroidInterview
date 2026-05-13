# Answers — §9 Android DevOps and CI/CD

Questions: [questions.md](../questions.md#9-android-devops-and-cicd).

---

### CI/CD Pipelines

**1.** Ideal Android CI/CD: stages, gates, artifacts.

- [x]

- **Thesis:** **Stages**: checkout → **cache restore** → **deterministic Gradle** wrapper → **static analysis** (lint/detekt/ktlint) → **unit tests** → **assemble** (debug + release maybe) → **instrumented** (optional nightly) → **signing** release AAB on protected branches → **distribute** (internal track) → **Play** promote with gates. **Gates**: no failing tests, coverage threshold optional, **dependency vulnerability** scan, **size** regression budget.
- **Mechanism / order:** **Artifacts**: AAB/APK, `mapping.txt`, lint reports, junit XML, **Baseline Profile** if generated in CI device pool.
- **Edge cases:** **Flaky** tests quarantine policy.
- **Production hook:** **GitHub Environments** approvals for prod.
- **When wrong:** Signing keys on developer laptops only.

**2.** Compare CI platforms (GitHub Actions, Bitrise, CircleCI, Jenkins, GitLab CI).

- [x]

- **Thesis:** **GHA**: tight GitHub integration, YAML matrix, **macOS** runners costly, BYO cache. **Bitrise**: mobile-first stacks, **emulator** support, visual workflows, $$ per minute. **CircleCI**: docker + machine executors, parallelism mature. **Jenkins**: infinite flexibility, self-hosted maintenance tax. **GitLab CI**: unified VCS+CI, powerful `rules`, self-hosted option.
- **Mechanism / order:** Pick by **secret management**, **Mac M1** availability, **Gradle remote cache** integration.
- **Edge cases:** **Apple Silicon** vs Intel emulator performance.
- **Production hook:** Gradle **configuration cache** compatible runners pinned image.
- **When wrong:** Choosing solely on price ignoring **emulator** SLA.

**3.** Cache Gradle deps + outputs in CI.

- [x]

- **Thesis:** Cache **`~/.gradle/caches`** + **`~/.gradle/wrapper`** + project **`.gradle`** selectively; add **remote build cache** node; avoid caching **`build/`** blindly if **clean** tasks break reproducibility—prefer **Gradle** cache keys on `gradle.properties` + lockfiles.
- **Mechanism / order:** GitHub `actions/cache` keyed by `hashFiles('**/*.gradle*', '**/gradle-wrapper.properties')`.
- **Edge cases:** **Too large** cache upload dominates savings—**trim** transforms-3 caches policy.
- **Production hook:** **Gradle Enterprise** remote cache.
- **When wrong:** Caching `local.properties` with SDK paths from wrong machine.

**4.** Signing keys + secrets in CI.

- [x]

- **Thesis:** **Encrypted secrets** store (GitHub Actions secrets, GCP Secret Manager); **inject** keystore as **base64** ephemeral file in job; **password** via env; **Play App Signing** means upload key less critical than **upload certificate** rotation discipline; avoid committing `jks`.
- **Mechanism / order:** **`android.injected.signing.store.*`** properties or `signingConfigs` env wiring.
- **Edge cases:** **Fork PRs** must not access secrets—`pull_request_target` dangers—use `workflow_dispatch` or restricted tokens.
- **Production hook:** Google **Play internal sharing** automated.
- **When wrong:** Keystore in repo “encrypted” with weak password in adjacent file.

**5.** PR pipeline checks before merge.

- [x]

- **Thesis:** **ktlint/detekt/android lint** non-warn-as-error optional; **unit** tests; **module dependency rules**; **API binary** compatibility if libs; **coverage** diff (not absolute); **Danger** bot for CHANGELOG; **size** diff **APK Analyzer** report optional.
- **Mechanism / order:** **Required status checks** on protected `main`.
- **Edge cases:** **Long running** UI tests moved nightly not every PR if cost high.
- **Production hook:** **Merge queue** / **bors** style batching optional.
- **When wrong:** Zero checks—regressions shipped daily.

---

### Automated Testing in CI

**6.** Instrumented tests without physical devices: FTL, emulator.wtf, Gradle Managed Devices.

- [x]

- **Thesis:** **Firebase Test Lab** device matrix cloud. **emulator.wtf** low-latency cloud emulators API. **Gradle Managed Devices** define virtual devices in Gradle for **CI** reproducible provisioning (API 31+ focus).
- **Mechanism / order:** `managedDevices { create("pixel2Api31") { device = "Pixel 2" apiLevel = 31 } }` + `Pixel2Api31DebugAndroidTest`.
- **Edge cases:** **ARM** vs **x86** image mismatch on M1 runners—use ARM system images.
- **Production hook:** Shard tests across **parallel** FTL matrices.
- **When wrong:** Assuming Robolectric replaces all integration tests.

**7.** Gradle Managed Devices simplify CI emulators.

- [x]

- **Thesis:** Declarative **AVD** definitions + Gradle tasks download system images and run tests—no bespoke shell scripts `sdkmanager` in CI.
- **Mechanism / order:** Pair with **Test Lab** or local hardware runners.
- **Edge cases:** **License** acceptance flags in CI headless.
- **Production hook:** Cache AVD snapshots where supported.
- **When wrong:** Old `connectedCheck` only without shard timeouts.

**8.** Flaky tests in CI; strategies.

- [x]

- **Thesis:** **Quarantine** bucket, **retry** limited with annotation only for known flakes, **root cause** fixes, **idling resources** / `runTest` advance time, **disable animations** on device, **isolate** network with MockWebServer, **hermetic** clocks.
- **Mechanism / order:** Track **flake rate** metric; block merge if new flake introduced.
- **Edge cases:** **Retry hides** product bug—cap retries.
- **Production hook:** **Marathon** test runner reruns with analytics.
- **When wrong:** `@Ignore` entire suite pre-release.

**9.** Screenshot testing CI; Paparazzi / Roborazzi.

- [x]

- **Thesis:** **Paparazzi**: JVM **layoutlib** renders composables/views off-device, compares PNG **golden** files in `src/test/snapshots`. **Roborazzi**: Robolectric-based screenshot capture. PR fails on image diff until approved.
- **Mechanism / order:** Store goldens in git LFS if huge; **CI** deterministic fonts/locale `Paparazzi.showSystemUi`.
- **Edge cases:** **Anti-aliasing** diffs across OS—stabilize rendering settings.
- **Production hook:** `verifyPaparazzi` in PR, `recordPaparazzi` locally.
- **When wrong:** Generating goldens only on CI non-deterministic hardware.

---

### Release and Deployment

**10.** Automate Play: Fastlane vs Play Publisher vs Play Developer API.

- [x]

- **Thesis:** **Fastlane** Ruby lanes orchestrate Gradle + API uploads + metadata screenshots. **Gradle Play Publisher** plugin publishes **AAB** directly from Gradle using service account JSON. **Raw API** for custom pipelines (HTTP JSON).
- **Mechanism / order:** Service account **JSON key** in CI secrets; **tracks** internal/alpha/prod.
- **Edge cases:** **Changes not sent for review** flag nuances.
- **Production hook:** **Internal testing** track per commit main optional.
- **When wrong:** Human `./gradlew bundleRelease` + manual drag-drop every week.

**11.** Staged rollout; 10% → 100% decision.

- [x]

- **Thesis:** Start **small %** to catch **crashes/ANRs** on real diversity; monitor **Android Vitals** + crash backend **version** segmentation; promote when metrics **flat or better** than baseline for **24–48h** depending risk appetite.
- **Mechanism / order:** Play Console **halt rollout** button ready.
- **Edge cases:** **Country** skew—watch top markets separately.
- **Production hook:** Automated promotion script gated on SLO queries.
- **When wrong:** 100% day-one on holiday release freeze panic.

**12.** Feature flags: Remote Config vs LaunchDarkly vs custom.

- [x]

- **Thesis:** **Firebase Remote Config**: simple key/value, free tier, **A/B** integration, eventual consistency. **LaunchDarkly**: enterprise targeting, **real-time** streaming updates, audit trails, $$$. **Custom**: full control, you own **consistency**, **security**, **latency** costs.
- **Mechanism / order:** **Kill switch** for bad remote features; **local defaults** bundled.
- **Edge cases:** **Stale** config offline—define safe defaults.
- **Production hook:** **Over-the-air** only for non-critical path toggles; **binary** gating for risky native code.
- **When wrong:** Remote flag changes **API contract** without client backward compatibility.

**13.** App Bundle vs APK; dynamic feature modules; pipeline.

- [x]

- **Thesis:** **AAB** is upload format; Play generates **split APKs** per device config; **DFM** adds **on-demand**/`install-time` modules affecting **delivery** graph and **versioning** alignment in CI (version codes unified rules).
- **Mechanism / order:** Internal sharing still uses AAB testing tracks.
- **Edge cases:** **Instant** modules constraints.
- **Production hook:** **bundletool** local `build-apks` QA smoke.
- **When wrong:** Sideloading AAB to testers without `bundletool`.

**14.** Automate versionCode/versionName in CI.

- [x]

- **Thesis:** CI sets **`VERSION_CODE` env** → Gradle `defaultConfig.versionCode = System.getenv(...).toInt()` or **Git commit count** / **calendar versioning**; **versionName** from **git tag** `v1.2.3`.
- **Mechanism / order:** **Play** requires monotonic **versionCode** per artifact.
- **Edge cases:** **Retraction** impossible—always bump forward.
- **Production hook:** Tag-driven workflow `on: push: tags: v*`.
- **When wrong:** Time-based versionCode collisions parallel branches.

---

### Monitoring and Observability

**15.** Crash reporting in CI/CD workflow.

- [x]

- **Thesis:** Upload **`mapping.txt`** + **native symbols** (`ndk { debugSymbolLevel 'FULL' }`) as CI artifacts tied to version; configure **Crashlytics Gradle** upload task on **release** build; **Sentry** release health with `dist` + commit SHA.
- **Mechanism / order:** **Source maps** for JS hybrid if any.
- **Edge cases:** **R8 full mode** line mapping verify.
- **Production hook:** **Release checklist** gate: mapping uploaded before promote.
- **When wrong:** Shipping release without mapping upload enabled.

**16.** Post-release metrics.

- [x]

- **Thesis:** **Crash-free sessions %**, **ANR** rate, **startup** P50/P95, **frozen frames** vitals, **battery** excessive wakeups, **retention** cohort, **custom** funnel KPIs.
- **Mechanism / order:** Play Vitals + backend analytics correlation by `versionCode`.
- **Edge cases:** **OEM** spikes vs real regression—segment device manufacturer.
- **Production hook:** **BigQuery** export Play Vitals for dashboards.
- **When wrong:** Watching only crashes ignoring **ANR** user pain.

**17.** Alerting when crash rate exceeds threshold.

- [x]

- **Thesis:** Crashlytics **velocity alerts**, Sentry **issue alerts**, Play **pre-launch / vitals** email; PagerDuty integration for **SEV** thresholds on **canary** track.
- **Mechanism / order:** Define **SLO** e.g. crash-free > 99.5% rolling 1h.
- **Edge cases:** **Low traffic** new version noisy alerts—min sample size filter.
- **Production hook:** Auto **halt rollout** webhook custom.
- **When wrong:** Email-only alerts ignored on weekends.

**18.** Android Vitals dashboard; store visibility.

- [x]

- **Thesis:** Google tracks **ANR**, **crash**, **excessive wakeups**, **slow rendering**, **permission denials** etc.; bad vitals vs peers → **lower ranking / warnings /** potential **removal** for extreme badness.
- **Mechanism / order:** **Peer benchmarks** in console.
- **Edge cases:** **Staged rollout** vitals lag actual due to sampling.
- **Production hook:** Treat vitals as **product** OKR not just eng metric.
- **When wrong:** Ignoring **bad behavior** threshold because “only 0.1% users”.

---

### Infrastructure and Tooling

**19.** Multiple environments in CI.

- [x]

- **Thesis:** **Product flavors** `dev/stage/prod` + **secrets** per GitHub environment; **inject** `API_BASE_URL` at build time; **runtime** Remote Config for non-security toggles only.
- **Mechanism / order:** Separate **Firebase projects** per env optional.
- **Edge cases:** **Accidental** prod API in dev flavor—lint string checks.
- **Production hook:** **Different applicationIdSuffix** side-by-side installs for QA.
- **When wrong:** One `google-services.json` checked in for all envs incorrectly merged.

**20.** Gradle wrapper + AGP updates for 20+ devs.

- [x]

- **Thesis:** **Wrapper** jar properties committed; **Renovate/Dependabot** PRs with **CI green** matrix; **release train** monthly AGP bump pilot branch; **document** breaking changes internal wiki; **binary compatibility** smoke app.
- **Mechanism / order:** **Gradle toolchain** pins JDK via `foojay` plugin.
- **Edge cases:** **Apple Silicon** Gradle native agent issues on upgrades.
- **Production hook:** **Gradle version catalog** central bump single PR.
- **When wrong:** Each dev different local Gradle version ignoring wrapper.

**21.** Enforce code quality: detekt, ktlint, lint, dependency-analysis.

- [x]

- **Thesis:** **detekt** complexity/rules; **ktlint** formatting (or `ktfmt`); **Android lint** + **fatal** selected issues; **dependency-analysis-plugin** fails unused deps & misaligned `api`.
- **Mechanism / order:** Pre-commit **hooks** optional; CI source of truth.
- **Edge cases:** **Baseline files** for gradual adoption with ratchet `maxIssues` decrease over time.
- **Production hook:** **Codeowners** for baseline changes review.
- **When wrong:** Thousands lint warnings ignored—signal dies.

**22.** Renovate / Dependabot safe updates.

- [x]

- **Thesis:** Bot opens PRs bumping **catalog versions** + **grouping** related AndroidX; **auto-merge** patch if CI green; **pin** major versions manual review.
- **Mechanism / order:** **Lockfile** or verification metadata for reproducible builds.
- **Edge cases:** **AGP** requires simultaneous Kotlin bump compatibility matrix.
- **Production hook:** Weekly schedule + **changelog** links in PR body.
- **When wrong:** Auto-merge major Compose without screenshot tests.
