# Answers — §6 Database and Storage

Questions: [questions.md](../questions.md#6-database-and-storage).

---

### Room

**1.** Room architecture: `@Entity`, `@Dao`, `@Database`, `@TypeConverter`.

- [x]

- **Thesis:** **SQLite** ORM layer: **entities** map tables/FTS; **DAOs** expose queries as **suspend/Flow** methods; **Database** abstract class wires **schema + migrations**; **TypeConverters** marshal non-SQLite types ↔ columns.
- **Mechanism / order:** Codegen at compile time (KSP) generates `_Impl` classes.
- **Edge cases:** **Immutable** entities preferred; **relations** via projection POJOs.
- **Production hook:** Single `@Database` per SQLite file; version bump discipline.
- **When wrong:** `@Transaction` missing on multi-table write that must be atomic.

**2.** Compile-time query verification; annotation processing.

- [x]

- **Thesis:** Room **parses** SQL at compile time against **schema** model—catches **unknown columns/tables**, bad **bind parameter counts**.
- **Mechanism / order:** KSP/KAPT reads DAO SQL strings + `@Query` annotations.
- **Edge cases:** **Dynamic** table names not verifiable—use `@RawQuery` consciously.
- **Production hook:** Enable **Room Gradle plugin** strict mode when available.
- **When wrong:** Concatenating SQL strings defeating verification + SQL injection.

**3.** Migrations; missing migration; `fallbackToDestructiveMigration`.

- [x]

- **Thesis:** **`Migration(start,end)`** supplies `ALTER`/`CREATE` steps; if **missing**, Room **throws** on open (good). **`fallbackToDestructiveMigration`** drops data—**dev only** or **non-critical** caches.
- **Mechanism / order:** `autoMigration = true` for simple column add/remove with annotations.
- **Edge cases:** **Complex** transform needs manual SQL + data copy tables.
- **Production hook:** **MigrationTestHelper** instrumented tests.
- **When wrong:** Destructive fallback in **prod** user data app.

**4.** Auto-migration; limitations vs manual.

- [x]

- **Thesis:** `@AutoMigration(from=…, to=…)` + `@DeleteColumn`/`@RenameColumn` metadata generates migrations for **supported** simple changes.
- **Mechanism / order:** Cannot express **data backfills** requiring Kotlin logic—manual required.
- **Edge cases:** **Rename** vs drop+add ambiguity—annotate explicitly.
- **Production hook:** Pair with **baseline schema export** JSON in CI.
- **When wrong:** Expecting auto-migration for **splitting** one table into two with derived columns.

**5.** Room + Flow; what happens internally for `Flow<List<Entity>>`.

- [x]

- **Thesis:** Room registers **invalidation tracker** **observer** on tables touched by query; on **change**, re-runs query and **emits** new list on **dispatcher** (usually arch executor).
- **Mechanism / order:** **Distinct until changed** optimizations internal.
- **Edge cases:** **Large** lists emit full snapshot each change—use **Paging** or narrow query.
- **Production hook:** `Flow` collection with **`distinctUntilChanged`** if cheap equality expensive.
- **When wrong:** Doing heavy mapping on **main** in `map` operator post-Room.

**6.** `@Embedded`, `@Relation`, `@Junction`; 1-N and N-N.

- [x]

- **Thesis:** **`Embedded`** flattens nested object columns into parent row; **`@Relation`** defines **object graph** fetch (separate queries under hood—**not** single SQL join by default); **`@Junction`** names join table for **N-N**.
- **Mechanism / order:** Relations loaded **lazily** unless `@Transaction` query returns POJO with both.
- **Edge cases:** **N+1** if naïvely accessing relations in loops—use **@Transaction @Query** with JOIN or write explicit query.
- **Production hook:** Prefer **explicit query + DTO** for performance-critical lists.
- **When wrong:** Assuming `@Relation` is a SQL join.

**7.** Full-text search (FTS) in Room.

- [x]

- **Thesis:** **`Fts4Entity`** / **`Fts5`** virtual table entities + **content sync** options; DAO queries use **MATCH**.
- **Mechanism / order:** **External content** table sync triggers.
- **Edge cases:** **Tokenizer** choice (`unicode61`, porter).
- **Production hook:** Precompute **search tokens** column for simpler LIKE when FTS overkill.
- **When wrong:** FTS on **main thread** large index build at migration—background.

**8.** `@RawQuery`; security.

- [x]

- **Thesis:** Pass **`SupportSQLiteQuery`** built with **bound args**—still **compile-time unchecked** SQL strings—risk of **SQL injection** if concatenating user input.
- **Mechanism / order:** Use **`SimpleSQLiteQuery`** with `bindArgs` array.
- **Edge cases:** **Observed** raw queries still tracked if `observesTables` set (Room 2.4+ API variants).
- **Production hook:** Centralize **whitelist** query builders; never string concat `WHERE` from raw user text.
- **When wrong:** `RawQuery("SELECT * FROM u WHERE name='" + name + "'")`.

**9.** Testing Room; in-memory strategy.

- [x]

- **Thesis:** **`Room.inMemoryDatabaseBuilder`** for fast **hermetic** tests; or **`MigrationTestHelper`** with extracted **schema** files for migration verification.
- **Mechanism / order:** Use same **DAOs** as prod; run on **instrumentation** JVM for SQLite native behavior.
- **Edge cases:** **WAL** mode differences vs memory—still OK for logic tests.
- **Production hook:** Robolectric possible for some DAO tests with limitations.
- **When wrong:** Sharing singleton DB across tests without close—flaky.

**10.** `MultiInstanceInvalidationService`.

- [x]

- **Thesis:** Enables **cross-process** Room invalidation when **same DB file** accessed from **multiple processes** (rare)—coordinates **content observers** via IPC service.
- **Mechanism / order:** Enable `enableMultiInstanceInvalidation()` on builder.
- **Edge cases:** **Performance** + complexity—prefer **single process** app architecture.
- **Production hook:** Widget process + app process sharing DB edge case.
- **When wrong:** Enabling without **multi-process** need—unnecessary overhead.

---

### SQLite Internals

**11.** WAL; Room; performance.

- [x]

- **Thesis:** **Write-Ahead Logging** writes new pages to **`-wal` file**; readers continue old snapshot; **writer** doesn’t block readers as much as rollback journal.
- **Mechanism / order:** Room enables WAL by default on Android SQLiteOpenHelper path.
- **Edge cases:** **Checkpoint** size; **backup** must use **Online backup API** correctly.
- **Production hook:** Long-running transactions prevent checkpoint—keep transactions short.
- **When wrong:** Deleting `-wal` manually while DB open—corruption.

**12.** Default SQLite threading on Android; Room concurrency.

- [x]

- **Thesis:** Android SQLite **connection per helper** by default; Room uses **Arch executor** + optional **`queryExecutor`**; **serialized** writes per database file unless multi-connection pool (Room 2.4+ `useQueryExecutor` patterns / driver updates).
- **Mechanism / order:** **`@Transaction`** ensures single connection serial use.
- **Edge cases:** **Write** contention from many coroutines—batch writes.
- **Production hook:** Enable **SQLiteConnectionPool** features via SupportSQLiteOpenHelperFactory when needed.
- **When wrong:** Long **read** transaction blocking **write** from UI thread.

**13.** Transactions; `@Transaction`.

- [x]

- **Thesis:** Room **`@Transaction`** wraps DAO method body in **BEGIN…COMMIT** ensuring **atomic** multi-step operations + **relation queries** that need consistent snapshot.
- **Mechanism / order:** Suspend functions still **single thread** per config.
- **Edge cases:** Nested transactions not SQLite native—Room flattens.
- **Production hook:** Use for **insert parent + children** foreign keys.
- **When wrong:** Partial updates without transaction—orphan rows.

---

### DataStore and SharedPreferences

**14.** Why DataStore vs SharedPreferences problems.

- [x]

- **Thesis:** **SharedPreferences** lacks **async API**, **`apply()`** fsync races, **no** type safety, **no** transactional multi-key updates, **main-thread** ANR risk on large files.
- **Mechanism / order:** DataStore **Kotlin Flow** + **proto/preferences** atomic updates.
- **Edge cases:** **Migration** path from SP one-time.
- **Production hook:** New projects: **no new SP keys**.
- **When wrong:** Using DataStore for **large blobs**—still wrong tool.

**15.** Preferences vs Proto DataStore; choose Proto.

- [x]

- **Thesis:** **Preferences** key-value typed accessors—simple flags. **Proto** schema-backed structured settings with **evolution** & validation.
- **Mechanism / order:** Proto requires **schema** + generated serializer.
- **Edge cases:** **Default values** on field presence semantics.
- **Production hook:** Proto when **nested** settings or **backward compatible** evolution needed.
- **When wrong:** Storing entire JSON string in single preference “because proto is hard”.

**16.** DataStore atomic read-modify-write.

- [x]

- **Thesis:** **`edit { transform }`** transactional at DataStore level—serializes writers on single mutex + versioning; **handles corruption** by reset policies optional.
- **Mechanism / order:** `DataStore.updateData { current -> next }` suspends until durable.
- **Edge cases:** **IOException** retries with backoff internal.
- **Production hook:** Single writer coroutine for related keys if cross-store consistency needed.
- **When wrong:** Two processes writing same preferences file—use **single process** or accept OS file level races (DataStore file is one).

**17.** Can SharedPreferences cause ANR?

- [x]

- **Thesis:** Yes—**`commit()`** on main blocks disk; heavy **`getAll()`** on large XML parse on main; **first** load triggers disk IO.
- **Mechanism / order:** `apply()` posts memory write but **fsync** can still block shutdown paths.
- **Edge cases:** **Multi-process** `MODE_MULTI_PROCESS` deprecated disaster.
- **Production hook:** StrictMode disk read detection in debug.
- **When wrong:** Loading **entire** SP map at startup on UI thread.

---

### Scoped Storage and Files

**18.** Scoped storage (Android 10+); file access patterns.

- [x]

- **Thesis:** Apps get **sandbox** dirs by default; **shared media** via **MediaStore**; **Downloads/Documents** via **SAF**; **legacy** `WRITE_EXTERNAL_STORAGE` largely neutered on target 10+.
- **Mechanism / order:** **`requestLegacyExternalStorage`** temporary opt-out (deprecated timeline).
- **Edge cases:** **MANAGE_EXTERNAL_STORAGE** rare special apps policy.
- **Production hook:** Use **`MediaStore` APIs** + **Photo Picker**.
- **When wrong:** Hard-coded `/sdcard` paths.

**19.** `getFilesDir` vs `cache` vs `getExternalFilesDir` vs MediaStore.

- [x]

- **Thesis:** **Internal files**: private app storage always available. **Cache**: system can evict. **`getExternalFilesDir`**: app-specific external **no permission** modern API. **MediaStore**: shared media collection URIs with right permissions/intents.
- **Mechanism / order:** Backup rules exclude cache by default.
- **Edge cases:** **Scoped** access via `StorageAccessFramework` `OPEN_DOCUMENT`.
- **Production hook:** Migrate downloads to **app-specific** dirs when privacy allows.
- **When wrong:** Storing secrets in external unencrypted dirs.

**20.** Large downloads survive process death.

- [x]

- **Thesis:** Use **`DownloadManager`** or **foreground service** + **resumable** HTTP (`Range`) + **persisted** cursor state in **Room/DataStore** (download id, bytes completed).
- **Mechanism / order:** `WorkManager` + `ForegroundInfo` for long transfers policy-compliant.
- **Edge cases:** **Doze** batching—use FGS type **dataSync** / exemptions correctly per policy.
- **Production hook:** OkHttp **resume** + DB checkpointing every N MB.
- **When wrong:** In-memory only download state.
