# Answers — §11 Android System Design

Questions: [questions.md](../questions.md#11-android-system-design).

_Note: system-design answers stay **brief** but name **components, protocols, and trade-offs** interviewers expect._

---

### Chat Application

**1.** WhatsApp-like chat: realtime, persistence, offline, receipts, media, pagination.

- [x]

- **Thesis:** **Transport**: WebSocket/MQTT for duplex low-latency or **HTTP/2 streams** + **FCM** fallback wake; **persistence**: Room messages + outbox; **offline queue**: `WorkManager` + pending rows; **receipts**: server seq + local `READ` flags; **media**: resumable upload (chunked PUT signed URLs) + **cache** on disk; **history**: **cursor/keyset** paging (`PagingSource` on `(conversationId, serverTime)` index).
- **Mechanism / order:** **Idempotent** `clientMessageId` dedup; **E2E** optional Olm/Signal protocol layer if security scope.
- **Edge cases:** **Multi-device** sync via server `deviceId` watermarks; **OEM** kill background socket—FCM silent ping to reconnect.
- **Production hook:** **SQLite FTS** for search; **Macrobenchmark** scroll jank budget.
- **When wrong:** Offset pagination on huge history—O(n) scans.

**2.** Message ordering + delivery guarantees.

- [x]

- **Thesis:** Server assigns **monotonic sequence** per conversation; client sorts by `(seq, tie-breaker)`; **at-least-once** network → dedup keys; **exactly-once** semantics end-to-end impossible—simulate with idempotency + server state machine.
- **Mechanism / order:** **Vector clocks** rarely needed vs simple server ordering for most consumer apps.
- **Edge cases:** **Clock skew**—never trust client time for ordering authority.
- **Production hook:** Show **pending** state until ACK seq ≥ local provisional.
- **When wrong:** Using wall-clock millis alone for ordering.

**3.** Local DB schema: conversations, messages, participants.

- [x]

- **Thesis:** Tables: **`conversations(id, title, lastMessageAt, unreadCount)`**, **`messages(id, convId, clientId, serverSeq, senderId, body, type, status, createdAt)`** indexes on `(convId, serverSeq DESC)`**, **`participants(convId, userId, role)`** with **`@Junction`** if normalized; **FTS** virtual table optional referencing `messages(content)`.
- **Mechanism / order:** **Foreign keys ON** with cascade rules deliberate.
- **Edge cases:** **Soft delete** tombstones for sync.
- **Production hook:** **Room auto-migrations** for additive columns only.
- **When wrong:** Storing media blobs in SQLite rows—use file paths + URIs.

---

### Offline-First Application

**4.** Offline-first notes: sync on reconnect; conflicts.

- [x]

- **Thesis:** **Local SSOT** Room; **mutation log** of ops; sync pushes **ops** or **snapshots** with **versions**; conflicts: **LWW** with `updatedAt` + **vector** optional; show merge UI for text if both sides edited.
- **Mechanism / order:** `WorkManager` **constraints** `NetworkType.CONNECTED`.
- **Edge cases:** **Large note** binary diff expensive—operational transform optional scope.
- **Production hook:** **Backoff** + **exponential** retry; **snapshot** compaction after N ops.
- **When wrong:** Last write wins silently losing user paragraphs.

**5.** CRDTs for offline-first.

- [x]

- **Thesis:** **CRDTs** merge commutative/lattice structures without central server ordering for every op—great for **presence**, **counters**, **rich text** (Yjs-like) with overhead.
- **Mechanism / order:** Choose **LWW-Element-Set**, **OR-Set**, **RGA** per data type.
- **Edge cases:** **Text CRDT** storage size growth—garbage collection/compaction policies.
- **Production hook:** Only where **true concurrent multi-master** editing required.
- **When wrong:** CRDT for simple single-user notes—overkill.

**6.** Work queue for pending ops.

- [x]

- **Thesis:** **Room table `outbox(opId, type, payload, state, attempts, nextRunAt)`** + **WM worker** drains with **mutex** per user; **idempotency** keys on API; **DLQ** after max attempts with user-visible error.
- **Mechanism / order:** **Transaction** enqueue + local state update atomic.
- **Edge cases:** **Process death** mid-flight—mark `IN_FLIGHT` stale with timeout reclaim job.
- **Production hook:** Observability per **op type** success rate.
- **When wrong:** In-memory queue only.

---

### Image Loading Library

**7.** Design Coil/Glide-like library.

- [x]

- **Thesis:** **API**: `load(url)` builder with **lifecycle** (Glide requests tied to fragment/compose). **Memory**: `LruCache` keyed by `(url, size, transformations)`; **Disk**: OkHttp cache or custom journal LRU; **Fetch**: OkHttp streaming; **Decode**: `BitmapFactory.Options.inSampleSize` / **ImageDecoder** + hardware **Bitmap.Config**; **lifecycle**: cancel on destroy, pause on stop optional.
- **Mechanism / order:** **Thread pools**: decode vs network; **coalescing** duplicate in-flight requests.
- **Edge cases:** **GIF/WebP** decoders; **HEIF** API gates.
- **Production hook:** **Target** size from `ImageView` measured dims or Compose constraints.
- **When wrong:** Full-res decode on main—OOM + ANR.

**8.** Animated GIF/WebP.

- [x]

- **Thesis:** Use **ImageDecoder**/`Movie`/`AnimatedImageDrawable` paths; separate **frame** scheduler vs static bitmap cache; cap **max frames** memory.
- **Mechanism / order:** Hardware drawable where supported.
- **Edge cases:** **Infinite** animations battery—pause off-screen.
- **Production hook:** **Coil** `ImageDecoderDecoder` factory pattern.
- **When wrong:** Decoding all frames into list of full bitmaps.

**9.** Prevent OOM large images.

- [x]

- **Thesis:** **Downsample** to view size; **RGB_565** when acceptable; **Region decoder** tiling for huge; **do not** hold multiple full-res in memory; **BitmapPool** reuse (Glide internal).
- **Mechanism / order:** `inJustDecodeBounds` pre-read dimensions.
- **Edge cases:** **PhotoPicker** exif orientation rotate.
- **Production hook:** **Coil size** + **precision** EXACT vs INEXACT.
- **When wrong:** `Bitmap.createBitmap(original.width, original.height)` always ARGB_8888.

---

### Feed / Timeline

**10.** Instagram-like feed: pagination, cache, optimistic like, infinite scroll, pull-to-refresh.

- [x]

- **Thesis:** **Cursor** paging API + **RemoteMediator** Room; **prefetch** next page on near-end; **optimistic like** toggles local `liked` + outbox op; **pull-to-refresh** invalidates `PagingSource` + clears `remote_keys` transactionally; **image** pipeline as above.
- **Mechanism / order:** **ETag**/`If-None-Match` optional for feed JSON.
- **Edge cases:** **Ad** injection shifts positions—stable `itemId` keys crucial.
- **Production hook:** **RecyclerView prefetch** + `setItemViewCacheSize` tuned.
- **When wrong:** Offset `page=2` after inserts shifts duplicates.

**11.** Real-time new posts without scroll jump.

- [x]

- **Thesis:** Insert **above** with `notifyItemRangeInserted` careful; better: **separate “new posts” chip** queue until user taps refresh; or **prepend** with `RecyclerView` keep anchor via `ConcatAdapter` header row.
- **Mechanism / order:** For Compose `LazyListState` animate scroll only on user action.
- **Edge cases:** **Paging3** prepend invalidates layout—use **placeholder** strategy or manual `channel` of events.
- **Production hook:** Twitter-style **“See new posts”** bar simplest UX.
- **When wrong:** Auto-scroll top on websocket message while user reading old.

**12.** Pre-fetch for instant scroll.

- [x]

- **Thesis:** **Prefetch next N items** network + decode ahead of `RecyclerView` gap worker; **warm** disk cache on Wi-Fi; **predict** scroll velocity increase lookahead.
- **Mechanism / order:** Custom `RecyclerView.OnScrollListener` triggers `repository.prefetch(nextCursor)`.
- **Edge cases:** **Metered** network—respect user data saver.
- **Production hook:** **Coil** `memoryCachePolicy` + `diskCachePolicy`.
- **When wrong:** Preloading entire feed unlimited.

---

### Analytics SDK

**13.** Analytics SDK: batching, persistence, retry, footprint, thread safety.

- [x]

- **Thesis:** **Client API** thread-safe queue (`Mutex`/`ConcurrentLinkedQueue`) batches events to **disk ring buffer** (append-only file or Room) with **max size**; **flush** timer + **count threshold**; **worker** thread or `WorkManager` uploads gzip JSON; **exponential backoff** + **jitter**; **SDK init** lazy + **no** heavy work on main beyond enqueue.
- **Mechanism / order:** **PII scrubbing** hooks before persist.
- **Edge cases:** **Multi-process** host—document single-process requirement or IPC.
- **Production hook:** **Sampling** + dynamic config Remote Config `sampleRate`.
- **When wrong:** HTTP call per tap on main.

**14.** Avoid impacting host performance/battery.

- [x]

- **Thesis:** **Batch** + **compress**; **defer** until charging/Wi-Fi optional; **coalesce** duplicate events; **strict size caps** drop safely; **never** spin busy loops; **FGS** only if policy demands real-time—usually not.
- **Mechanism / order:** Use **OkHttp** connection pooling shared optional.
- **Edge cases:** **ANR** if disk flush blocks host main—always background executor.
- **Production hook:** Macrobenchmark host app with/without SDK in CI.
- **When wrong:** High-frequency GPS + analytics same thread.

---

### Notification System

**15.** E-commerce notifications: FCM, channels, deep link, scheduled local, grouping.

- [x]

- **Thesis:** **FCM data messages** wake app to show **NotificationCompat** with **channels** per category (`orders`, `promos`); **deep link** `PendingIntent` to Nav deep link URI; **local** `AlarmManager`+`WorkManager` for reminders respecting **exact alarm** permission; **group** `setGroup` summary notification.
- **Mechanism / order:** **POST_NOTIFICATIONS** runtime permission Android 13+.
- **Edge cases:** **Image** big picture URL fetch off main before notify.
- **Production hook:** **FCM topic** subscriptions per user interest.
- **When wrong:** Default channel everything—users disable all.

**16.** Delivery reliability across OEMs.

- [x]

- **Thesis:** OEMs defer **high priority FCM** unless **TTL**/`priority` justified; **data** vs **notification** messages differ wake behavior; mitigate with **user education**, **battery whitelist** prompts (careful UX), **fallback** email/SMS not app’s job.
- **Mechanism / order:** Track **delivery receipts** server-side if FCM HTTP v1 APIs support analytics.
- **Edge cases:** **Huawei** without GMS—alternate push provider.
- **Production hook:** **Inbox** inside app polling as last resort for critical alerts.
- **When wrong:** Assuming `priority_high` always wakes killed app.

---

### Payment Flow

**17.** Payment: PCI, tokenization, Google Pay, retry, idempotency.

- [x]

- **Thesis:** **Never** store PAN/CVV; use **PSP tokenization** (Stripe `PaymentMethod`), **Google Pay** returns tokenized payload; **PCI SAQ-A** scope when WebView/PSP handles card fields; **retry** idempotent with **`Idempotency-Key`** header per attempt; **client** stores only **intent client secret** ephemeral.
- **Mechanism / order:** **3DS2** Web flow hosted by PSP.
- **Edge cases:** **Split** tender partial failures—state machine explicit.
- **Production hook:** Server is **source of truth** for payment status polling.
- **When wrong:** Logging full card PAN to Crashlytics breadcrumbs.

**18.** Payment state across process death / rotation.

- [x]

- **Thesis:** Persist **`paymentIntentId` + status** in `SavedStateHandle`/`DataStore`; **resume** polling after restore; **do not** duplicate charges—disable pay button while `PROCESSING`.
- **Mechanism / order:** `SingleTop` activity for deep return URLs.
- **Edge cases:** **User leaves app** during 3DS—handle `onNewIntent`.
- **Production hook:** **WorkManager** unique work for finalize step.
- **When wrong:** Relying on in-memory `isPaid` flag only.

---

### Large-Scale App Architecture

**19.** Super-app modularization; DFM; plugins.

- [x]

- **Thesis:** **Core platform modules** (auth, design, networking) + **feature DFMs** delivered on demand; **plugin** pattern via **ServiceLoader**/`AppComponentFactory` risky—prefer **explicit DFMs** + **Play Feature Delivery**; **strict DI graph** boundaries per team.
- **Mechanism / order:** **Navigation** maps feature module routes registered from app shell.
- **Edge cases:** **Shared user session** encryption keys in **TEE** per device not per feature.
- **Production hook:** **ABI** contracts + consumer-driven contract tests between teams.
- **When wrong:** Dynamic code loading from internet—policy + security nightmare.

**20.** Shared auth, theming, navigation across 50+ feature modules.

- [x]

- **Thesis:** **`:core:session`** exposes `SessionRepository` interface; **`:core:designsystem`** tokens; **`:core:navigator`** facade implemented in app; **feature modules** depend only on those **API** modules; **versioned** binary compatibility checks.
- **Mechanism / order:** **Single activity** shell hosts **NavHost**; **SSO** tokens in encrypted storage.
- **Edge cases:** **Theme** per tenant—`CompositionLocal` dynamic provider from session.
- **Production hook:** **Feature flags** gate unfinished modules.
- **When wrong:** Copy-paste auth fragment into each feature.

**21.** Logging/observability across modules.

- [x]

- **Thesis:** **`:core:telemetry`** defines `Logger`/`Tracer` interfaces; **OpenTelemetry** Android SDK with **correlation** `trace_id` propagated via OkHttp interceptor + manual **Span** in features; **Breadcrumb** bus to Crashlytics.
- **Mechanism / order:** **PII** scrub centralized pipeline.
- **Edge cases:** **High volume** debug logs—sampling + rate limit.
- **Production hook:** **BigQuery** sink dashboards by `featureName` attribute.
- **When wrong:** Each module `Log.d` random strings unsearchable.

**22.** A/B testing at scale.

- [x]

- **Thesis:** **Remote Config**/`LaunchDarkly` assignments **bucketed** stable per `userId` hash; **analytics** exposure event with experiment key + variant; **server-side** experiments for pricing where client tamper matters.
- **Mechanism / order:** **Holdouts** for control; **guardrails** metrics auto-stop bad variants.
- **Edge cases:** **Cold start** path must not block on remote fetch—local defaults + async apply.
- **Production hook:** **Stats engine** (sequential testing) not naive t-test on peeking data optional advanced.
- **When wrong:** Client-only paywall experiments easily bypassed.
